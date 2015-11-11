package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.GAActor;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zuehlke.fnf.masterbrain.akka.Publisher.tell;

/**
 * Created by tho on 10.08.2015.
 */
public class TOTPilotActor extends UntypedActor {
    public static final String KEY_LEARNER_REF = "learner";
    public static final String KEY_TRACK = "track";
    public static final String TYPE = "TOT";
    public static final String KEY_USE_CACHE = "KEY_USE_CACHE";
    public static final String KEY_CONFIG = "totConfig";
    private static final Logger LOGGER = LoggerFactory.getLogger(TOTPilotActor.class);
    private static final String CHECK_EVALUATION_QUEUE = "CHECK_EVALUATION_QUEUE";
    private static final String CHECK_STOPPED_CAR = "CHECK_STOPPED_CAR";
    private static final String PUBLISH_STATUS = "PUBLISH_STATUS";
    private ActorRef geneticAlgo;
    private Track track;
    private TOTConfig config;
    private LapEvaluation runningEvaluation;
    private ActorRef pilotController;
    private FeatureExtraxtor featureExtractor;
    private PopulationSampler populationSampler;
    private Queue<ActorRef> evaluationQueue = new ArrayBlockingQueue<>(10000);
    private ScoreDao scoreDao = new ScoreDao();
    private boolean waitForEvaluationStart;
    private int zeroPowerCounter;


    public TOTPilotActor() {
        Config akkaConfig = context().system().settings().config();
        config = new TOTConfig(akkaConfig);
        populationSampler = new PopulationSampler(config);
        scheduleEvaluationQueueTrigger();
        scheduleStoppedCarCheck();
        scheduleStatusPublishing();
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof Locations) {
            handleLocations((Locations) message);
        } else if (message instanceof Track) {
            handleTrack((Track) message);
        } else if (message instanceof PenaltyMessage) {
            handlePenalty((PenaltyMessage) message);
        } else if (message instanceof ScoredGenom) {
            handleHighestGuy((ScoredGenom) message);
        } else if (message instanceof TOTGenom) {
            runEvaluation((TOTGenom) message);
        } else if (message instanceof ActorRef) {
            pilotController = context().sender();
        } else if (message instanceof StartPermission) {
            if (StartPermission.ask.equals(message)) {
                handleStartPermissionRequest((StartPermission) message);
            } else if (StartPermission.terminate.equals(message)) {
                terminateLapEvaluation();
            }
        } else if (CHECK_EVALUATION_QUEUE.equals(message)) {
            handleEvaluationQueue();
        } else if (CHECK_STOPPED_CAR.equals(message)) {
            checkForStoppedCar();
        } else if (PUBLISH_STATUS.equals(message)) {
            publishStatus();
        } else {
            unhandled(message);
        }
    }

    private void publishStatus() {
        String msg = String.format("evaluations to do=%s", evaluationQueue.size());
        LOGGER.debug("{}", msg);
        fireInfo(new Info(TYPE, "status", msg));
    }

    private void terminateLapEvaluation() {
        LOGGER.info("Terminating running lap evaluation");
        if (runningEvaluation != null) {
            runningEvaluation.onTerminate();
        }
    }

    private void checkForStoppedCar() {
        if (zeroPowerCounter > config.getMaxZeroPowerInArow()) {
            LOGGER.info("Car is not moving. Too many low power in a row.");
            if (runningEvaluation != null) {
                runningEvaluation.onCarStopped();
            }
        }
    }

    private void handleEvaluationQueue() {
        if (runningEvaluation == null && !waitForEvaluationStart) {
            LOGGER.debug("can start a new evalutation");
            if (evaluationQueue.isEmpty()) {
                LOGGER.debug("no evaluation request in queue");
                return;
            }
            ActorRef requester = evaluationQueue.poll();
            waitForEvaluationStart = true;
            //LOGGER.debug("requests in queue={}", evaluationQueue.size());
            requester.tell(StartPermission.ok, getSelf());
        } else {
            LOGGER.debug("Evaluation still in process.");
        }
    }

    private void handleStartPermissionRequest(StartPermission request) {
        evaluationQueue.add(context().sender());
        LOGGER.debug("Current evaluation queue size={}", evaluationQueue.size());
    }

    private void handlePenalty(PenaltyMessage message) {
        if (runningEvaluation != null) {
            runningEvaluation.onPenalty(message);
        }
    }

    private void handleLocations(Locations message) {
        if (runningEvaluation != null) {
            runningEvaluation.onLocations(message, track);
        }
    }

    private void runEvaluation(TOTGenom genom) {
        LOGGER.debug("runEvaluation with genom={}", genom);
        if (runningEvaluation != null) {
            LOGGER.error("There's already a evaluation running!");
            return;
        }
        final ActorRef sender = sender();
        runningEvaluation = new LapEvaluation(genom, config.getMinPower(), config.getMaxPower(), config.getSafetyPower(), config.getMinLapDuration(), config.getSafetyDuration(), config.getMaxZeroPowerInArow(), featureExtractor, this);
        waitForEvaluationStart = false;
        Future<ScoredGenom<TOTGenom>> evaluate = runningEvaluation.getFuture();

        evaluate.onComplete(new OnComplete<ScoredGenom<TOTGenom>>() {
            @Override
            public void onComplete(Throwable throwable, ScoredGenom<TOTGenom> result) throws Throwable {
                if (throwable != null) {
                    LOGGER.warn("Evalutation failed.", throwable);
                } else {
                    sender.tell(result, getSelf());
                }
                LOGGER.debug("Evaluation done. {}", result);
                runningEvaluation = null;

            }
        }, getContext().dispatcher());
    }

    private void handleHighestGuy(ScoredGenom<TOTGenom> message) {
        LOGGER.debug("Learning done.");
        fireInfo(new Info(TYPE, "learning", "Learn cycle done."));

        double lapTime = message.getScore();// * -1;
        LOGGER.debug("Best setting in population: genom={}, lapTime={}", message.getGenom(), lapTime);
        fireInfo(new Info(TYPE, "learning", String.format("Result looks good. Genom=%s, Expected lap times around %s ms", message.getGenom(), (int) lapTime)));
        scoreDao.storeBestScore(message);

    }

    private void stopGa() {
        Future<Boolean> stopped = Patterns.gracefulStop(geneticAlgo, Duration.create(5, TimeUnit.SECONDS));
        stopped.andThen(new OnComplete<Boolean>() {
            @Override
            public void onComplete(Throwable throwable, Boolean aBoolean) throws Throwable {
                if (throwable == null) {
                    LOGGER.debug("GA stopped.");
                } else {
                    LOGGER.error("Graceful stop of GA actor failed.", throwable);
                }
                geneticAlgo = null;
            }
        }, context().dispatcher());
    }

    private void handleTrack(final Track message) throws InterruptedException {
        if (track != null) {
            this.track = message;
            featureExtractor.setTrack(track);
        } else {
            this.track = message;
            featureExtractor = new FeatureExtraxtor(track, config.getCurveLookAhead(), config.getCurveLookBack());
            Population<TOTGenom> population = populationSampler.createPopulation(this.track);
            startLearning(population);
        }
    }

    private void startLearning(Population<TOTGenom> population) {
        Map<Object, Object> customProperties = new HashMap<>();
        customProperties.put(KEY_TRACK, track);
        customProperties.put(KEY_LEARNER_REF, getSelf());
        customProperties.put(KEY_USE_CACHE, config.isUseCache());
        customProperties.put(KEY_CONFIG, config);

        Configuration configuration = new Configuration<>(config.getPopulationSize(), TOTEvaluation.class, TOTFitnessFunction.class, TOTPairingAndMutation.class,
                TOTTermination.class, customProperties, getSelf(), context().dispatcher());

        if (geneticAlgo != null) {
            stopGa();
        }
        if (geneticAlgo == null) {
            geneticAlgo = context().actorOf(GAActor.props(configuration), GAActor.class.getSimpleName() + "-LearnOnTrack");
            geneticAlgo.tell(population, getSelf());
        }
    }


    public void firePower(int power) {
        if (power < 110) {
            zeroPowerCounter++;
        } else {
            zeroPowerCounter = 0;
        }
        tell(Power.of(power), pilotController, getSelf()).onMissingSender().logInfo("no pilotController").andReturn();
    }

    private void scheduleEvaluationQueueTrigger() {
        FiniteDuration interval = Duration.create(500, TimeUnit.MILLISECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), CHECK_EVALUATION_QUEUE, context().system().dispatcher(), null);
    }

    private void scheduleStoppedCarCheck() {
        FiniteDuration interval = Duration.create(2, TimeUnit.SECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), CHECK_STOPPED_CAR, context().system().dispatcher(), null);
    }

    private void scheduleStatusPublishing() {
        FiniteDuration interval = Duration.create(30, TimeUnit.SECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), PUBLISH_STATUS, context().system().dispatcher(), null);
    }

    public void fireInfo(Info info) {
        tell(info, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }

    public void firePowerProfile(int[] powerValues) {
        List<Double> collect = Arrays.stream(powerValues).mapToDouble(Double::valueOf).boxed().collect(Collectors.toList());
        ImmutableList<Double> profile = new ImmutableList.Builder<Double>().addAll(collect).build();
        TrackWithPowerProfile p = TrackWithPowerProfile.from(track, profile);
        tell(p, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }
}
