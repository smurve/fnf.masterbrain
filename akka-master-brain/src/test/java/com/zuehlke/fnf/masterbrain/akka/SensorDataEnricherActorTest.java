package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UnhandledMessage;
import akka.testkit.JavaTestKit;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import com.zuehlke.fnf.masterbrain.akka.messages.RegisterWebPublisher;
import com.zuehlke.fnf.masterbrain.akka.messages.ResetCommand;
import com.zuehlke.fnf.masterbrain.akka.messages.SensorData;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by tho on 22.07.2015.
 */
public class SensorDataEnricherActorTest {

    @Rule
    public AkkaRule akka = AkkaRule.create();

    @Test
    public void testThatNewSensorDataGetsEnhancedAndSendToAvailableTargets() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit trackBuilder = akka.newProbe();
            JavaTestKit localization = akka.newProbe();
            JavaTestKit webPublisher = akka.newProbe();
            Props props = Props.create(SensorDataEnricherActor.class, () -> new SensorDataEnricherActor(trackBuilder.getRef(), localization.getRef()));

            ActorRef subject = akka.actorOf(props, SensorDataEnricherActor.NAME);

            // No Power set, yet. No SensorData should be processed.
            SensorData event = new SensorData(0, 1, 2, 3);
            subject.tell(event, ActorRef.noSender());

            trackBuilder.expectNoMsg(duration("1 second"));
            localization.expectNoMsg(duration("1 second"));
            webPublisher.expectNoMsg(duration("1 second"));

            // We simulate that the pilot sends a power command so the enricher starts processing sensor data.
            subject.tell(Power.of(42), ActorRef.noSender());

            // Now process some data
            subject.tell(event, ActorRef.noSender());

            SensorData tData = trackBuilder.expectMsgClass(SensorData.class);
            SensorData lData = localization.expectMsgClass(SensorData.class);
            // We haven't registered the WebPublisher, therefore not message expected here.
            webPublisher.expectNoMsg(duration("1 second"));
            assertThat(tData, is(lData));
            assertThat(tData.getForce(), is(42));
        }};
    }

    @Test
    public void testThatNewSensorDataGetsEnhancedAndSendToAvailableTargetsIncludingWebPublisher() {
        new JavaTestKit(akka.getSystem()) {{
                JavaTestKit trackBuilder = akka.newProbe();
                JavaTestKit localization = akka.newProbe();
                JavaTestKit webPublisher = akka.newProbe();
                Props props = Props.create(SensorDataEnricherActor.class, () -> new SensorDataEnricherActor(trackBuilder.getRef(), localization.getRef()));

                ActorRef subject = akka.actorOf(props, SensorDataEnricherActor.NAME);

                // We register a WebPublisher now. It must receive SensorData events from now on.
                subject.tell(new RegisterWebPublisher(), webPublisher.getRef());

                // We simulate that the pilot sends a power command so the enricher start processing sensordata.
                subject.tell(Power.of(42), ActorRef.noSender());

                // Now process some data
                SensorData event = new SensorData(0, 1, 2, 3);
                subject.tell(event, ActorRef.noSender());

                SensorData tData = trackBuilder.expectMsgClass(SensorData.class);
                SensorData lData = localization.expectMsgClass(SensorData.class);
                SensorData wData = webPublisher.expectMsgClass(SensorData.class);
                assertThat(tData, is(lData));
                assertThat(tData, is(wData));
                assertThat(tData.getForce(), is(42));
            }
        };
    }

    @Test
    public void testThatResetCommandDoesNotGetForwardedToAnyOtherActors() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit trackBuilder = akka.newProbe();
            JavaTestKit localization = akka.newProbe();
            JavaTestKit pilot = akka.newProbe();
            JavaTestKit webPublisher = akka.newProbe();
            Props props = Props.create(SensorDataEnricherActor.class, () -> new SensorDataEnricherActor(trackBuilder.getRef(), localization.getRef()));

            ActorRef subject = akka.actorOf(props, SensorDataEnricherActor.NAME);

            subject.tell(new RegisterWebPublisher(), webPublisher.getRef());

            subject.tell(ResetCommand.message(), ActorRef.noSender());

            pilot.expectNoMsg(duration("200 milliseconds"));
            trackBuilder.expectNoMsg(duration("200 milliseconds"));
            localization.expectNoMsg(duration("200 milliseconds"));
            webPublisher.expectNoMsg(duration("200 milliseconds"));
        }};
    }

    @Test
    public void testThatUnknownCommandDoesNotGetProcessed() {
        JavaTestKit trackBuilder = akka.newProbe();
        JavaTestKit localization = akka.newProbe();
        Props props = Props.create(SensorDataEnricherActor.class, () -> new SensorDataEnricherActor(trackBuilder.getRef(), localization.getRef()));

        ActorRef subject = akka.actorOf(props, SensorDataEnricherActor.NAME);

        JavaTestKit probe = akka.newProbe();
        akka.getSystem().eventStream().subscribe(probe.getRef(), UnhandledMessage.class);

        new JavaTestKit(akka.getSystem()) {
            {
                subject.tell("Hello, Drop Me!", ActorRef.noSender());

                trackBuilder.expectNoMsg(duration("200 milliseconds"));
                localization.expectNoMsg(duration("200 milliseconds"));

                probe.expectMsgClass(UnhandledMessage.class);
            }
        };


    }
}
