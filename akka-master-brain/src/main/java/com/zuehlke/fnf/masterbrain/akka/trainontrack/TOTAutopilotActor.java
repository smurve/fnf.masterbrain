package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.utils.LapSurveillant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zuehlke.fnf.masterbrain.akka.Publisher.tell;

/**
 * Created by tho on 07.09.2015.
 */
public class TOTAutopilotActor extends UntypedActor {

    public static final String ADJUST_POWER_FACTOR = "adjust.power.factor";
    private static final Logger LOGGER = LoggerFactory.getLogger(TOTAutopilotActor.class);
    private TOTConfig config;
    private ScoreDao scoreDao = new ScoreDao();
    private SpeedCalculator speedCalculator;
    private FeatureExtraxtor featureExtractor;
    private Track track;
    private ActorRef pilotController;
    private LapSurveillant lapSurveillant;
    private long lapStartNanos;
    private boolean safetyMode = false;
    private long safetyStartMillis;
    private final int safetyPower;
    
	private static final long SAFETY_MODE_DURATION = 5000;

    private double minPower = 0.8;
    private double maxPower = 1.0;
    private double currentPower = 0.8;

    public TOTAutopilotActor() {
        Config akkaConfig = context().system().settings().config();
        config = new TOTConfig(akkaConfig);
        safetyPower = config.getSafetyPower();
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (message instanceof Locations) {
            handleLocations((Locations) message);
        } else if (message instanceof Track) {
            handleTrack((Track) message);
        } else if (message instanceof ActorRef) {
            handlePiloController((ActorRef) message);
        } else if(message instanceof PenaltyMessage) {
        	handlePenalty((PenaltyMessage) message);

        } else if (message instanceof PilotProperties) {
            handleProperties((PilotProperties) message);
        } else {
            unhandled(message);
        }
    }

    private void handleProperties(PilotProperties message) {
        LOGGER.info("new properties: {}", message);
        try {
            currentPower = Double.valueOf(message.getProperties().get(ADJUST_POWER_FACTOR));
            minPower = currentPower;
            maxPower = 1.0;
        } catch (Exception e) {
            LOGGER.info("Invalid value for property={}", ADJUST_POWER_FACTOR);
            publishInfo(new Info(TOTPilotActor.TYPE, "config", "Invalid value for property " + ADJUST_POWER_FACTOR));
        }
        publishProperties();
    }

    private void handlePiloController(ActorRef message) {
        pilotController = message;
        publishProperties();
    }

    private void publishProperties() {
        PilotProperties props = new PilotProperties();
        props.getProperties().put(ADJUST_POWER_FACTOR, Double.toString(currentPower));
        tell(props, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }


    private void handleTrack(final Track message) throws InterruptedException {
        LOGGER.info("Got a new track.");
        if (this.track != null) {
            this.track = message;
            featureExtractor.setTrack(track);
        } else {
            this.track = message;
            featureExtractor = new FeatureExtraxtor(track, config.getCurveLookAhead(), config.getCurveLookBack());
            ScoredGenom<TOTGenom> bestScore = scoreDao.loadBestScore();
            speedCalculator = new SpeedCalculator(featureExtractor, bestScore.getGenom(), config.getMaxPower(), config.getMinPower());
            new SanityCheck(Integer.MAX_VALUE, speedCalculator.simulateLap()).execute(this::firePowerProfile, (s, aDouble) -> LOGGER.info("SHOULD NEVER HAPPEN"));
            publishInfo(new Info(TOTPilotActor.TYPE, "loaded", "Driving with " + bestScore));

        }
    }

    private void handleLocations(Locations locations) {
        if (speedCalculator == null) {
            LOGGER.warn("No speedCalculator, yet.");
        } else if (safetyMode) {
        	if (System.currentTimeMillis() - safetyStartMillis > SAFETY_MODE_DURATION) {
        		safetyMode = false;
        		publishInfo(new Info("safety", "off", null));
        	}
        } else {
            int power = (int)(speedCalculator.calculateSpeed(locations) * currentPower);
            tell(Power.of(power), pilotController, getSelf()).onMissingSender().logInfo("no pilotController").andReturn();

            int highestProbIndex = locations.getIndexWithHighestPropability();
            if (highestProbIndex >= 0) {
                if (lapSurveillant == null || lapSurveillant.isLapFinished(highestProbIndex)) {
                    if (lapStartNanos > 0) {
                        long lapTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lapStartNanos);
                        LOGGER.debug("Lap complete. time={} ms", lapTime);
                        publishInfo(new Info(TOTPilotActor.TYPE, "race", String.format("lap time=%s", lapTime)));
                        increasePower();
                    }

                    lapSurveillant = new LapSurveillant(track, locations);
                    lapStartNanos = System.nanoTime();
                }
            }
        }
    }
    
    private void handlePenalty(PenaltyMessage message) {
        if (track == null) {
            return;
        }
    	if(currentPower - 0.01 < minPower ) {
    		minPower = minPower - 0.01;
    		currentPower = currentPower - 0.01;
    	} else {
    		maxPower = currentPower;
    		currentPower = (minPower + maxPower) / 2;
    	}
        String msg = String.format("Decreased Power to %s. Min/Max power are %s/%s", currentPower, minPower, maxPower);
    	LOGGER.info(msg);
        publishInfo(new Info(TOTPilotActor.TYPE, "race", msg));
        enterSafeftyMode();
        publishProperties();
	}
    
    private void enterSafeftyMode() {
    	safetyMode = true;
        safetyStartMillis = System.currentTimeMillis();
        tell(Power.of(safetyPower), pilotController, getSelf()).onMissingSender().logInfo("no pilotController").andReturn();
    	lapSurveillant = null;
        lapStartNanos = -1;
        publishInfo(new Info("safety", "on", null));
    }
    
    private void increasePower() {
    	minPower = currentPower;
    	currentPower = (minPower + maxPower) / 2;
        String msg = String.format("Increased Power to %s. Min/Max power are %s/%s", currentPower, minPower, maxPower);
    	LOGGER.info(msg);
        publishInfo(new Info(TOTPilotActor.TYPE, "race", msg));
        publishProperties();
    }

    private void publishInfo(Info info) {
        tell(info, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }

    public void firePowerProfile(int[] powerValues) {
        List<Double> collect = Arrays.stream(powerValues).mapToDouble(Double::valueOf).boxed().collect(Collectors.toList());
        ImmutableList<Double> profile = new ImmutableList.Builder<Double>().addAll(collect).build();
        TrackWithPowerProfile p = TrackWithPowerProfile.from(track, profile);
        tell(p, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }

}
