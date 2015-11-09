package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 23.07.2015.
 */
public class LocalizationActorTest {

    @Rule
    public AkkaRule akka = AkkaRule.createWithConfig("masterbrain{" +
            " localization{" +
            "  bufferSize=5," +
            "  interval=500" + // We increase the interval so we can easily test if the schedule logic is working correctly
            " }" +
            "}");

    @Test
    public void testThatWeOnlyTriggerTheLocalizationIfWeHaveAValidTrack() throws InterruptedException {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit rServe = akka.newProbe();
            Props props = Props.create(LocalizationActor.class);

            ActorRef subject = akka.actorOf(props, LocalizationActor.NAME);

            // Register RServ
            subject.tell(new RegisterLocalizator(), rServe.getRef());
            subject.tell(new TimeDelta(10L), ActorRef.noSender());
            // Now, we feed in some data
            SensorData event = new SensorData(0, 1, 2, 3);
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());

            // Nothing must happen
            rServe.expectNoMsg(duration("600 milliseconds"));

            // Publish a Track
            Track track = new Track();
            track.setStatus(Status.from("ok", ""));
            subject.tell(track, ActorRef.noSender());

            // Now, we feed in some more data
            subject.tell(event, ActorRef.noSender());

            Object[] events = rServe.receiveN(1, duration("1200 milliseconds"));
            System.out.println(events[0]);
            assertThat(((Localize)events[0]).getData().getSensorEvents().length, is(3));

        }};
    }

    @Test
    public void testThatItIgnoresTheLocalizationTriggerIfNoLocalizerIsSet() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit rServe = akka.newProbe();
            Props props = Props.create(LocalizationActor.class);

            ActorRef subject = akka.actorOf(props, LocalizationActor.NAME);

            // Publish a Track
            Track track = new Track();
            track.setStatus(Status.from("ok", ""));
            subject.tell(track, ActorRef.noSender());
            subject.tell(new TimeDelta(10L), ActorRef.noSender());
            // Now, we feed in some data
            SensorData event = new SensorData(0, 1, 2, 3);
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());
            subject.tell(event, ActorRef.noSender());

            // Nothing must happen and nothing must crash.
            // Of course rServe can't receive anything as we haven't registered it, yet. But, we need
            // to wait a few ms so the scheduler kicks in a few times.
            rServe.expectNoMsg(duration("1 second"));


            // Register RServ
            subject.tell(new RegisterLocalizator(), rServe.getRef());

            Object[] events = rServe.receiveN(2, duration("1200 milliseconds"));
            // we've sent 6 events, buffer is 5
            assertThat(((Localize) events[0]).getData().getSensorEvents().length, is(5));

        }};
    }
}
