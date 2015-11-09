package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.GAActor;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.messages.LearningFinished;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackSegment;
import com.zuehlke.fnf.masterbrain.simulator.Simulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by mhan on 10.07.2015.
 */
public class LearnerActor extends UntypedActor {
    public static final Double MAX_POWER = 255d;
    public static final double INIT_SPEED = 180d;
    public static final String KEY_SIMULATOR = "simulator";
    public static final String KEY_LEARNER_REF = "learner";
    public static final String KEY_MAX_ITERATIONS = "maxIterations";
    public static final String KEY_TRACK = "track";
    private static final Logger LOGGER = LoggerFactory.getLogger(LearnerActor.class);
    private static final Random RANDOM = new Random();
    private final int gaInstances;
    private final int gaIterations;
    private final int populationSize;
    private final int segmentsPerTrack;

    private ActorRef sender;
    private ScoredGenom<PowerProfileGenom> highestGuy;
    private int finishedInstances = 0;
    private Track track;
    private List<ActorRef> ref;

    public LearnerActor() {
        this.gaInstances = getContext().system().settings().config().getInt("masterbrain.learner.gaInstances");
        this.gaIterations = getContext().system().settings().config().getInt("masterbrain.learner.iterations");
        this.populationSize = getContext().system().settings().config().getInt("masterbrain.learner.populationSize");
        this.segmentsPerTrack = getContext().system().settings().config().getInt("masterbrain.learner.segmentsPerTrack");
    }

    static List<Double> scale(final Track track, final ImmutableList<Double> powerSettings) {
        List<Double> toReturn = new ArrayList<>(track.getTrackPoints().size());
        List<TrackSegment> segments = track.getSegments(powerSettings.size());
        TreeMap<Integer, Integer> sectorIndexToSegments = mapTrackSegmentToCount(segments);

        Multiset<Integer> set = HashMultiset.create();
        AtomicInteger currentIndex = new AtomicInteger();
        sectorIndexToSegments.forEach((sector, count) -> {
            int numberOfPoints = track.getTrackPoints().getNumberOfPointsForSectorIndex(sector);
            int quotient = numberOfPoints / count;

            int added = quotient;
            int toAdd = numberOfPoints;
            int rest = numberOfPoints % count;

            while (toAdd > 0 && quotient != 0) {
                if (toAdd < added) {
                    added = toAdd;
                }
                set.add(currentIndex.getAndIncrement(), added);
                toAdd -= added;
            }
            set.add(currentIndex.get() - 1, rest);
        });
        for (int i = 0; i < powerSettings.size(); i++) {
            Integer integer = set.count(i);
            Double aDouble = powerSettings.get(i);
            for (int j = 0; j < integer; j++) {
                toReturn.add(aDouble);
            }
        }
        return toReturn;
    }

    static TreeMap<Integer, Integer> mapTrackSegmentToCount(final List<TrackSegment> segments) {
        return new TreeMap<>(segments.stream().collect(Collectors.groupingBy(TrackSegment::getSector_ind, Collectors
                .summingInt(o -> 1))));
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof Track) {
            sender = getSender();
            handleTrack((Track) message);
        } else if (message instanceof ScoredGenom) {
            handleHighestGuy((ScoredGenom) message);
        } else if (message instanceof Population) {
            handleLearningPopulation((Population) message);
        } else {
            unhandled(message);
        }
    }

    private void handleLearningPopulation(final Population<ImmutableList<Double>> message) {
        LOGGER.debug("{}/{} Ga instances finished", finishedInstances + 1, this.gaInstances);
        if (sender != null && finishedInstances + 1 == this.gaInstances && highestGuy != null) {
            sender.tell(new LearningFinished(scale(track, highestGuy.getGenom().getPowerSettings()), highestGuy.getScore()), getSelf());

            finishedInstances = 0;
            highestGuy = null;
        } else {
            finishedInstances++;
        }
    }

    private void handleHighestGuy(final ScoredGenom message) {
        if (highestGuy == null || highestGuy.compareTo(message) < 0) {
            this.highestGuy = message;
            if (sender != null) {
                sender.tell(message, getSelf());
            }
        }
    }

    private void handleTrack(final Track message) throws InterruptedException {
        this.track = message;

        ref = new ArrayList<>();

        for (int i = 0; i < this.gaInstances; i++) {
            Simulator simulator = new Simulator(message, INIT_SPEED);
            Map<Object, Object> customProperties = new HashMap<>();
            customProperties.put(KEY_TRACK, message);
            customProperties.put(KEY_SIMULATOR, simulator);
            customProperties.put(KEY_LEARNER_REF, getSelf());
            customProperties.put(KEY_MAX_ITERATIONS, this.gaIterations);
            Configuration configuration = new Configuration<>(populationSize, PowerProfileEvaluation.class, PowerProfileFitnessFunction.class, PowerProfilePairingAndMutation.class,
                    PowerPorfileTermination.class, customProperties, getSelf());

            // GAActors killing themselfs after finish computation
            ref.add(getContext().actorOf(GAActor.props(configuration), GAActor.class.getSimpleName() + "-" + i));
        }
        LOGGER.info("{} Ga instances started", this.gaInstances);
        for (ActorRef actorRef : ref) {
            actorRef.tell(createInitialPowerSettings(segmentsPerTrack, populationSize), getSelf());
        }
    }

    private Population<PowerProfileGenom> createInitialPowerSettings(final int segemtsCount, final int populationSize) {

        List<PowerProfileGenom> powerProfileGenoms = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {

            List<Double> powerSettings = new ArrayList<>();
            for (int j = 0; j < segemtsCount; j++) {
                powerSettings.add(RANDOM.nextDouble() * MAX_POWER);
            }

            powerProfileGenoms.add(new PowerProfileGenom(powerSettings));
        }

        return new Population<>(powerProfileGenoms);
    }
}
