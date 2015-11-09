package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by tho on 13.08.2015.
 */
public class GRange {
    private static final Logger LOGGER = LoggerFactory.getLogger(GRange.class);
    private static final Random RANDOM = new Random();

    public static double[] calcualteW2ToWnRange(Track track, double scale) {
        double minG = Double.MAX_VALUE;
        double maxG = Double.MIN_VALUE;
        for (double d : track.getTrackPoints().getGs()) {
            if (d < minG) {
                minG = d;
            }
            if (d > maxG) {
                maxG = d;
            }
        }
        LOGGER.info("minG={}, maxG={}", minG, maxG);
        double[] range = new double[]{scale(minG, scale), scale(maxG, scale)};
        LOGGER.info("w-range={}", Arrays.toString(range));
        return range;
    }

    private static double scale(double g, double scale) {
        return (1 / (g / scale));
    }

    public static void generateWi(double[] values, int index, double minW0, double maxW0) {
        generateWi(values, index, 0, minW0, maxW0);
    }

    public static void generateWi(double[] values, int index, double initial, double minW0, double maxW0) {
        values[index] = initial + RANDOM.nextDouble() * (maxW0 - minW0) + minW0;
    }
}
