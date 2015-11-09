package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by tho on 12.08.2015.
 */
public class SpeedCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpeedCalculator.class);
    private final int maxSpeed;
    private final int minSpeed;
    private final FeatureExtraxtor featureExtraxtor;
    private final Perceptron perceptron;

    /**
     * @param featureExtraxtor
     * @param genom            1 + numOfVariables doubles. w0, g1, g2, g3
     * @param maxSpeed
     * @param minSpeed
     */
    public SpeedCalculator(FeatureExtraxtor featureExtraxtor, TOTGenom genom, int maxSpeed, int minSpeed) {
        this.featureExtraxtor = featureExtraxtor;
        this.maxSpeed = maxSpeed;
        this.minSpeed = minSpeed;
        perceptron = new Perceptron(genom.getValues()[0], Arrays.copyOfRange(genom.getValues(), 1, genom.getValues().length));
    }

    public int calculateSpeed(Locations locations) {
        double[] gFeatures = featureExtraxtor.extract(locations);
        double speed = perceptron.predict(gFeatures);
        int adjustedSpeed = Math.min((int) Math.round(speed), maxSpeed);
        adjustedSpeed = Math.max(minSpeed, adjustedSpeed);
        LOGGER.debug("speed={}, adjustedSpeed={}", speed, adjustedSpeed);
        return adjustedSpeed;
    }

    public int[] simulateLap() {
        int length = featureExtraxtor.getTrack().getTrackPoints().getGs().length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            Locations loc = new Locations();
            double[] ps = new double[length];
            ps[i] = 1;
            loc.setStatus(Status.ok());
            loc.setPs(ps);
            result[i] = calculateSpeed(loc);
        }
        return result;

    }
}


