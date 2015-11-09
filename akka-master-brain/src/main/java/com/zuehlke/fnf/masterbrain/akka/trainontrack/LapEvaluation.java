package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import akka.dispatch.Futures;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.utils.LapSurveillant;
import com.zuehlke.fnf.masterbrain.akka.messages.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.Promise;

import java.util.concurrent.TimeUnit;

/**
 * Created by tho on 10.08.2015.
 */
public class LapEvaluation {
    public static final String TYPE = "TOT";
    private static final Logger LOGGER = LoggerFactory.getLogger(LapEvaluation.class);
    private final TOTGenom genom;
    private final SpeedCalculator speedCalculator;
    private final TOTPilotActor feedback;
    private final int minPower;
    private final long minLapDuration;
    private final int maxPower;
    private final long safetyModeDurationMillis;
    private final int safetyPower;
    private Promise<ScoredGenom<TOTGenom>> promise = Futures.promise();
    private long lapStartNanos;
    private boolean safetyMode = false;
    private long safetyStartMillis;
    private double safetyThreshold = 0.04;
    private LapSurveillant lapSurveillant;
    private boolean exitSafetyInCurve = true;
    private int penaltyAddition = 0;

    public LapEvaluation(TOTGenom genom, int minPower, int maxPower, int safetyPower, long minLapDuration, long safetyModeDurationMillis, int maxZeroPowerInArow, FeatureExtraxtor featureExtraxtor, TOTPilotActor feedback) {
        this.genom = genom;
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.safetyPower = safetyPower;
        this.minLapDuration = minLapDuration;
        this.safetyModeDurationMillis = safetyModeDurationMillis;
        this.feedback = feedback;
        this.speedCalculator = new SpeedCalculator(featureExtraxtor, genom, this.maxPower, this.minPower);
        enterSafetyMode();
        new SanityCheck(maxZeroPowerInArow, speedCalculator.simulateLap()).execute(feedback::firePowerProfile, this::onFail);
    }

    public Future<ScoredGenom<TOTGenom>> getFuture() {
        return promise.future();
    }

    public void setExitSafetyInCurve(boolean exitSafetyInCurve) {
        this.exitSafetyInCurve = exitSafetyInCurve;
    }

    public void onLocations(Locations locations, Track track) {
        if (!locations.getStatus().isOk()) {
            return;
        }
        int highestProbIndex = locations.getIndexWithHighestPropability();
        if (highestProbIndex < 0) {
            LOGGER.debug("highestProbIndex={}", highestProbIndex);
            return;
        }
        double p = locations.getPs()[highestProbIndex];

        if (checkSafety(p, isCurve(track, highestProbIndex))) {
            calculateSpeed(locations);
            detectLapFinished(locations, highestProbIndex);
        }
    }

    private boolean isCurve(Track track, int highestProbIndex) {
        double g = track.getTrackPoints().getGs()[highestProbIndex];
        //LOGGER.info("g={}", g);
        return g > 1000;
    }

    private boolean checkSafety(double p, boolean isCurve) {
        if (safetyMode) {
            if (System.currentTimeMillis() - safetyStartMillis < safetyModeDurationMillis + penaltyAddition) {
                LOGGER.debug("Still in safety mode");
                return false;
            } else if (p > safetyThreshold && (!exitSafetyInCurve || isCurve)) {
                LOGGER.debug("Leaving safety mode");
                safetyMode = false;
                penaltyAddition = 0;
                feedback.fireInfo(new Info("safety", "off", null));
                return true;
            } else if (p > safetyThreshold && (!exitSafetyInCurve || !isCurve)) {
                LOGGER.debug("Not in a curve. Staying in safety mode");
                return false;
            } else {
                LOGGER.debug("low p={}", p);
                return false;
            }
        }
        return true;
    }


    private void calculateSpeed(Locations locations) {
        int speed = speedCalculator.calculateSpeed(locations);
        feedback.firePower(speed);
    }

    private void detectLapFinished(Locations message, int highestProbIndex) {
        if (startLapIfNeeded(message.getPs().length, highestProbIndex)) {
            LOGGER.debug("Lap just started.");
            return;
        }

        long lapTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lapStartNanos);
        if (lapTime < minLapDuration) {
            LOGGER.debug("Lap is still too young");
            return;
        }

        if (lapSurveillant.isLapFinished(highestProbIndex)) {
            LOGGER.debug("Lap complete. time={} ms", lapTime);
            fireInfo(String.format("lap time=%s", lapTime));
            if (!promise.isCompleted()) {
                double score = lapTime;
                ScoredGenom<TOTGenom> result = new ScoredGenom<>(genom, score);
                LOGGER.info("Completing promise. {}", result);
                promise.success(result);
            }
        } else {
            LOGGER.debug("Lap not finished");
        }
    }

    private boolean startLapIfNeeded(int trackLength, int startIndex) {
        if (lapSurveillant != null) {
            return false;
        }
        lapStartNanos = System.nanoTime();
        lapSurveillant = new LapSurveillant(trackLength, startIndex, (int) Math.max(2, Math.round(trackLength * 0.2)));
        LOGGER.debug("Lap started");
        return true;
    }

    public void onPenalty(PenaltyMessage message) {
    	penaltyAddition = 3000;
        onFail("Got a penalty. Discarding lap.", Double.MAX_VALUE);
        //fireInfo("Penalty. Drive on...");
        //enterSafetyMode();
    }

    private void onFail(String message, double score) {
        LOGGER.info("{}", message);
        double realScore = score; // * -1;
        ScoredGenom<TOTGenom> result = new ScoredGenom<>(genom, realScore);
        LOGGER.debug("Completing promise. {}", result);
        promise.success(result);
        fireInfo(message);

    }

    private void fireInfo(String message) {
        feedback.fireInfo(new Info(TYPE, "evaluation", message));
    }

    private void enterSafetyMode() {
        safetyMode = true;
        safetyStartMillis = System.currentTimeMillis();
        feedback.firePower(safetyPower);
        feedback.fireInfo(new Info("safety", "on", "New evaluation"));
    }

    public void onCarStopped() {
        onFail("Car stopped. Discarding lap.", Double.MAX_VALUE);
    }

    public void onTerminate() {
        onFail("Killing evaluation", Double.MAX_VALUE);
    }
}
