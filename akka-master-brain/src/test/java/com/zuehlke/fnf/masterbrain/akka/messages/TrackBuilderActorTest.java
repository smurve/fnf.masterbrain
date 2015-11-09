package com.zuehlke.fnf.masterbrain.akka.messages;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import com.typesafe.config.ConfigFactory;
import com.zuehlke.fnf.masterbrain.akka.TrackBuilderActor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by tho on 09.07.2015.
 */
public class TrackBuilderActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("Test", ConfigFactory.parseString(
                "masterbrain{" +
                " trackBuilder{" +
                "  buildAfter=2," +
                "  bufferSize=4" +
                " }" +
                "}"));
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
    }

    @Test
    public void test() {
        final Props props = Props.create(TrackBuilderActor.class);
        final TestActorRef<TrackBuilderActor> ref = TestActorRef.create(system, props, "testA");

        final TestProbe rServiceProbe = new TestProbe(system);
        ref.tell(new RegisterTrackBuilder(), rServiceProbe.ref());
        rServiceProbe.expectNoMsg();

        ref.tell(new TimeDelta(10L), ActorRef.noSender());

        SensorData data = new SensorData(System.currentTimeMillis(), 0,0,0);
        ref.tell(data, ActorRef.noSender());
        rServiceProbe.expectNoMsg();

        ref.tell(data, ActorRef.noSender());
        rServiceProbe.expectMsgClass(BuildTrack.class);

        ref.tell(data, ActorRef.noSender());
        rServiceProbe.expectNoMsg();

        ref.tell(data, ActorRef.noSender());
        rServiceProbe.expectMsgClass(BuildTrack.class);
    }
}
