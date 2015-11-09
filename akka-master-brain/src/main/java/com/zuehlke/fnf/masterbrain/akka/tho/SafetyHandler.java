package com.zuehlke.fnf.masterbrain.akka.tho;

import com.typesafe.config.Config;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.fnf.masterbrain.akka.messages.Info;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper.readValue;

/**
 * Detects cases in which the car might be in danger. These are:
 * - penalty (too fast)
 * - signal loss (over time)
 * - bad localization quality (low probability)
 * <p/>
 * The handler will set the car into safety mode temporary. In case of a penalty it will tell the LocationHandler
 * where it should fix the track.
 * <p/>
 * Created by tho on 29.07.2015.
 */
public class SafetyHandler {

    public static final int DEFAULT_SEGMENTS_TO_FIX = 20;
    public static final int DEFUALT_POWER_DURING_PENALTY = 154;
    public static final int DEFAULT_SAFETY_CAR_DURATION = 5000;
    public static final long DEFAULT_SAFETY_MODE_AFTER_MILLIS = 100;
    public static final double DEFAULT_PROBABILITY_THRESHOLD = 0.04;
    public static final String PROPERTY_SEGMENTS_TO_FIX = "masterbrain.pilot.tho.segmentsToFixOnPenalty";
    public static final String PROPERTY_POWER_DURING_PENALTY = "masterbrain.pilot.tho.powerDuringPenalty";
    public static final String PROPERTY_SAFETY_CAR_DURATION = "masterbrain.pilot.tho.safetyCarDuration";
    public static final String PROPERTY_SAFETY_MODE_AFTER_MILLIS = "masterbrain.pilot.tho.safetyModeAfterMillis";
    public static final String PROPERTY_PROBABILITY_THRESHOLD = "masterbrain.pilot.tho.probablilityThreshold";
    private static final Logger LOGGER = LoggerFactory.getLogger(SafetyHandler.class);
    private long lastPenalty = 0;
    private int segmentsToFix;
    private int powerDuringPenalty;
    private int safetyCarDuration;
    private long lastValidLocationTime;
    private long safetyModeAfterMillis;
    private double probablilityThreshold;
    private boolean safetyMode;

    public SafetyHandler(Config config) {
        segmentsToFix = readValue(PROPERTY_SEGMENTS_TO_FIX, config::getInt, DEFAULT_SEGMENTS_TO_FIX);
        powerDuringPenalty = readValue(PROPERTY_POWER_DURING_PENALTY, config::getInt, DEFUALT_POWER_DURING_PENALTY);
        safetyCarDuration = readValue(PROPERTY_SAFETY_CAR_DURATION, config::getInt, DEFAULT_SAFETY_CAR_DURATION);
        safetyModeAfterMillis = readValue(PROPERTY_SAFETY_MODE_AFTER_MILLIS, config::getLong, DEFAULT_SAFETY_MODE_AFTER_MILLIS);
        probablilityThreshold = readValue(PROPERTY_PROBABILITY_THRESHOLD, config::getDouble, DEFAULT_PROBABILITY_THRESHOLD);
    }

    public void handlePenalty(PenaltyMessage message, BiConsumer<Integer, PilotFeedback> fixSegments, PilotFeedback feedback) {

        lastPenalty = System.currentTimeMillis();

        int diffSpeed = (int) (message.getActualSpeed() - message.getSpeedLimit());
        LOGGER.info("Penalty. diff={}, actualSpeed={}, speedLimit={}", diffSpeed, Math.round(message.getActualSpeed()), message.getSpeedLimit());
        int numOfSegments = segmentsToFix;
        numOfSegments = numOfSegments + Math.min(diffSpeed, 10);

        fixSegments.accept(numOfSegments, feedback);

        enterSafetyMode("Penalty", feedback);
    }

    public void checkAndContinue(Consumer<Void> onSuccess) {
        if (System.currentTimeMillis() - lastPenalty < safetyCarDuration) {
            LOGGER.debug("Safety mode. Recover localization");
        } else {
            onSuccess.accept(null);
        }

    }

    public void checkProbabiltyAndContinue(double p, PilotFeedback feedback, Consumer<PilotFeedback> succes, Consumer<Void> cutMaxPower) {
        LOGGER.debug("p={}", p);
        if (p < probablilityThreshold) {
            LOGGER.info("p={}. Going into safety mode", p);
            cutMaxPower.accept(null);
            enterSafetyMode("Low p of " + p, feedback);
            return;
        }
        lastValidLocationTime = System.currentTimeMillis();
        exitSafetyMode(feedback);
        succes.accept(feedback);
    }

    private void exitSafetyMode(PilotFeedback feedback) {
        if (safetyMode) {
            feedback.fireInfo(new Info("safety", "off", null));
            safetyMode = false;
        }
    }

    private void enterSafetyMode(String reason, PilotFeedback feedback) {
        feedback.firePower(Power.of(powerDuringPenalty));
        if (!safetyMode) {
            safetyMode = true;
            feedback.fireInfo(new Info("safety", "on", reason));
        }

    }

    public void checkSafety(PilotFeedback feedback) {
        checkAndContinue((o) -> {
            long diff = System.currentTimeMillis() - lastValidLocationTime;
            if (diff > safetyModeAfterMillis) {
                LOGGER.warn("No updated received. Safety mode");
                enterSafetyMode("No location for " + diff + " ms", feedback);
            } else {
                exitSafetyMode(feedback);
            }
        });
    }

    public void reset() {
        lastPenalty = 0;
    }
}
