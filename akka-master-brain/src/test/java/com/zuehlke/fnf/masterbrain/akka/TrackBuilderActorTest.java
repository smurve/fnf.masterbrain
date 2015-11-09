package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by tho on 22.07.2015.
 */
public class TrackBuilderActorTest {

    @Rule
    public AkkaRule akka = AkkaRule.createWithConfig("masterbrain{" +
            " trackBuilder{" +
            "  buildAfter=4," +
            "  bufferSize=5," +
            " }" +
            "}");

    @Test
    public void testThatWeGetNoAnswerIfAskForATrackButWeDontHaveATrackYet() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit probe = akka.newProbe();
            Props props = Props.create(TrackBuilderActor.class, () -> new TrackBuilderActor());

            ActorRef subject = akka.actorOf(props, TrackBuilderActor.NAME);

            subject.tell(new GetTrack(), probe.getRef());

            probe.expectNoMsg(duration("1 second"));
        }};
    }

    @Test
    public void testThatWeGetAnAnswerIfAskForATrackAndWeHaveATrack() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit probe = akka.newProbe();
            Props props = Props.create(TrackBuilderActor.class, () -> new TrackBuilderActor());

            ActorRef subject = akka.actorOf(props, TrackBuilderActor.NAME);

            Track track = new Track();
            track.setStatus(Status.from("ok", ""));
            subject.tell(track, ActorRef.noSender());
            subject.tell(new GetTrack(), probe.getRef());

            Track actualTrack = probe.expectMsgClass(Track.class);
            assertThat(actualTrack, is(track));
        }};
    }

    @Test
    public void testThatWeGetAnAnswerIfAskForTheTrackBuildingState() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit probe = akka.newProbe();
            Props props = Props.create(TrackBuilderActor.class, () -> new TrackBuilderActor());

            ActorRef subject = akka.actorOf(props, TrackBuilderActor.NAME);

            // Ask for the state
            subject.tell(new GetTrackBuildingState(), probe.getRef());

            TrackBuildingState actualState = probe.expectMsgClass(TrackBuildingState.class);
            assertThat(actualState.getN(), is(0));
            assertThat(actualState.getOf(), is(4));

            // Now, we feed in some data
            SensorData event = new SensorData(0, 1, 2, 3);
            subject.tell(event, ActorRef.noSender());

            // Aks for the state
            subject.tell(new GetTrackBuildingState(), probe.getRef());

            actualState = probe.expectMsgClass(TrackBuildingState.class);
            assertThat(actualState.getN(), is(1));
            assertThat(actualState.getOf(), is(4));
        }};
    }

    @Test
    public void testThatTrackBuildingStateGetsPublished() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit webPublisher = akka.newProbe();
            Props props = Props.create(TrackBuilderActor.class, () -> new TrackBuilderActor());

            ActorRef subject = akka.actorOf(props, TrackBuilderActor.NAME);

            // Now, we feed in some data
            SensorData event = new SensorData(0, 1, 2, 3);
            subject.tell(event, ActorRef.noSender());

            // But no WebPublisher has been registered so far
            webPublisher.expectNoMsg(duration("500 milliseconds"));

            // Now, we register a webPublisher
            subject.tell(new RegisterWebPublisher(), webPublisher.getRef());

            // Now, we feed in some data
            subject.tell(event, ActorRef.noSender());

            TrackBuildingState actualState = webPublisher.expectMsgClass(TrackBuildingState.class);
            assertThat(actualState.getN(), is(2));
            assertThat(actualState.getOf(), is(4));
        }};
    }

    @Test
    public void testThatTrackBuildervGetsCalledIfWeHaveEnoughData() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit rService = akka.newProbe();
            JavaTestKit probe = akka.newProbe();
            Props props = Props.create(TrackBuilderActor.class, () -> new TrackBuilderActor());

            ActorRef subject = akka.actorOf(props, TrackBuilderActor.NAME);

            // Now, we register RServe
            subject.tell(new RegisterTrackBuilder(), rService.getRef());
            rService.expectNoMsg(duration("500 milliseconds"));
            subject.tell(new TimeDelta(10L), ActorRef.noSender());
            // Now, we feed in some data to trigger the track creation
            SensorData event = new SensorData(0, 1, 2, 3);
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());

            BuildTrack actualMessage = rService.expectMsgClass(BuildTrack.class);
            assertThat(actualMessage.getData().getSensorEvents().length, is(4));

            // Ask for the state
            subject.tell(new GetTrackBuildingState(), probe.getRef());
            TrackBuildingState actualState = probe.expectMsgClass(TrackBuildingState.class);
            assertThat(actualState.getN(), is(0));
            assertThat(actualState.getOf(), is(4));

            // Now, we feed in some data to trigger the track creation
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());

            // A new BuildTrack must be sent. Usually we don't want this behaviour. But as we don't have simulated
            // the registration of a valid Track it behaves like this. See separate Test for the mentioned scenario.
            actualMessage = rService.expectMsgClass(BuildTrack.class);
            // Notice that we have a buffer size of 5 in the configuration!
            assertThat(actualMessage.getData().getSensorEvents().length, is(5));

        }};
    }

    @Test
    public void testThatTrackBuildervDoesNotGetsCalledIfWeHaveATrackAlready() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit rService = akka.newProbe();
            Props props = Props.create(TrackBuilderActor.class, () -> new TrackBuilderActor());

            ActorRef subject = akka.actorOf(props, TrackBuilderActor.NAME);

            // Now, we register RServe
            subject.tell(new RegisterTrackBuilder(), rService.getRef());
            rService.expectNoMsg(duration("500 milliseconds"));

            // Simulate the successful track creation by the R backend
            Track track = new Track();
            track.setStatus(Status.from("ok", ""));
            subject.tell(track, rService.getRef());

            // Now, we feed in some data to trigger the track creation but as we have a track already this must
            // not occur.
            SensorData event = new SensorData(0, 1, 2, 3);
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());

            // Nothing must happen now.
            rService.expectNoMsg(duration("500 milliseconds"));

        }};
    }
}
