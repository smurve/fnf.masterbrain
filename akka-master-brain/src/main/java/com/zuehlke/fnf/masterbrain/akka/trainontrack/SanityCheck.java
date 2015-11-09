package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by tho on 12.08.2015.
 */
public class SanityCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(SanityCheck.class);
    private int maxZeroPowerInARow;
    private int[] simulatedLap;

    public SanityCheck(int maxZeroPowerInARow, int[] simulatedLap) {
        this.maxZeroPowerInARow = maxZeroPowerInARow;
        this.simulatedLap = simulatedLap;
    }

    public void execute(Consumer<int[]> onSuccess, BiConsumer<String, Double> onFail) {
//        LOGGER.info("starting sanity check for simulation: {}", Arrays.toString(simulatedLap));
        int zeroInARow = 0;
        for (int i = 0; i < simulatedLap.length; i++) {
            if (simulatedLap[i] < 80) {
                zeroInARow++;
                if (zeroInARow > maxZeroPowerInARow) {
                    onFail.accept("This genom produces too many low power values in a row", 60000d);
                    return;
                }
            } else {
                zeroInARow = 0;
            }
        }
        LOGGER.debug("Simulation looks ok");
        onSuccess.accept(simulatedLap);
    }
}
