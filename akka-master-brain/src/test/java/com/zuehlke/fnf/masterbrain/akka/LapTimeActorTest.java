package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.JavaTestKit;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.fnf.masterbrain.akka.messages.GetLapTimes;
import com.zuehlke.fnf.masterbrain.akka.messages.LapTime;
import com.zuehlke.fnf.masterbrain.akka.messages.LapTimes;
import com.zuehlke.fnf.masterbrain.akka.messages.RegisterWebPublisher;
import org.junit.Rule;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 05.08.2015.
 */
public class LapTimeActorTest {

    @Rule
    public AkkaRule akka = AkkaRule.create();

    @Test
    public void testThatItStoresLapTimes() throws Exception {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit webPublisher = akka.newProbe();
            Props props = Props.create(LapTimeActor.class);

            ActorRef subject = akka.actorOf(props, LapTimeActor.NAME);

            // Register WebPublisher
            subject.tell(new RegisterWebPublisher(), webPublisher.getRef());

            // Now, we feed in some data
            RoundTimeMessage msg1 = new RoundTimeMessage();
            msg1.setRoundDuration(600);
            msg1.setTimestamp(1000);
            RoundTimeMessage msg2 = new RoundTimeMessage();
            msg2.setRoundDuration(500);
            msg2.setTimestamp(1500);

            // Publish the RoundPassedMessages
            subject.tell(msg1, ActorRef.noSender());
            subject.tell(msg2, ActorRef.noSender());
            webPublisher.expectMsgClass(duration("1 second"), LapTime.class);

            // We ask for the LapTimes
            Future<Object> future = Patterns.ask(subject, new GetLapTimes(), 1000);
            Object result = Await.result(future, duration("1 second"));
            LapTimes actual = (LapTimes)result;
            assertThat(actual.getLapTimes().length, is(2));
            assertThat(actual.getLapTimes()[0].getDuration(), is(600L));
            assertThat(actual.getLapTimes()[1].getDuration(), is(500L));
        }};
    }
}
