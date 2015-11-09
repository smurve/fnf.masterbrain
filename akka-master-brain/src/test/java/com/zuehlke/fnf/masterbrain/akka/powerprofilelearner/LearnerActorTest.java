package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.typesafe.config.ConfigFactory;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.junit.*;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by mhan on 10.07.2015.
 */
public class LearnerActorTest {

    private static ActorSystem system;
    private static Track track;
    private static final Logger LOGGER = LoggerFactory.getLogger(LearnerActorTest.class);

    @BeforeClass
    public static void setup() throws IOException {
        system = ActorSystem.create("GeneticAlgorithm", ConfigFactory.parseString(
                "masterbrain{" +
                        " learner{" +
                        "  gaInstances=8," +
                        "  iterations=0," +
                        "  populationSize=16," +
                        "  segmentsPerTrack=180" +
                        " }" +
                        "}"));

        track = loadTrack("/testtrack.json");
    }

    private static Track loadTrack(final String fileName) throws IOException {
        InputStream is = LearnerActorTest.class.getResourceAsStream(fileName);
        String trackString = CharStreams.toString(new InputStreamReader(is));
        Track track = TrackMapper.fromJSON(trackString);
        track.compoundTrackParts();
        track.preparePhysics();
        track.setTrackpoints(TrackPoints.from(null, null, null, new double[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5, 5, 6, 7}));
        return track;
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
    }


    @Test
    public void testTrackLearning() throws InterruptedException {
        ActorRef learner = system.actorOf(Props.create(LearnerActor.class));

        Inbox inbox = Inbox.create(system);
        inbox.send(learner, track);



        while (true) {

            Object receive1 = inbox.receive(Duration.create(9, TimeUnit.SECONDS));
            System.out.println(receive1);
            if (receive1 instanceof LearningFinished) {
                LearningFinished finished = ((LearningFinished) receive1);
                LOGGER.info("Learning finished: " + finished.getScore());
                break;
            }
        }
    }

    @Test
    public void testScale() throws Exception {
        Track track = Mockito.mock(Track.class);
        when(track.getTrackPoints()).thenReturn(TrackPoints.from(null, null, null, d(1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 3, 3)));
        when(track.getSegments(6)).thenReturn(trackSegments(1, 1, 1, 2, 3, 3));


        List<Double> scale = LearnerActor.scale(track, ImmutableList.of(10.0, 11.0, 12.0, 20.0, 31.0, 32.0));

        double[] doubles = scale.stream().mapToDouble(d -> d).toArray();
        double[] expected = d(10.0, 10.0, 11.0, 11.0, 12.0, 12.0, 20.0, 20.0, 31.0, 31.0, 32.0);

        assertArrayEquals(expected, doubles, 0.0002);
    }

    private List<TrackSegment> trackSegments(final int... is) {
        return IntStream.of(is).mapToObj(i -> {
            TrackSegment segment = new TrackSegment();
            segment.setSector_ind(i);
            return segment;
        }).collect(Collectors.toList());
    }

    public static double[] d(double... ds) {
        return ds;
    }
}