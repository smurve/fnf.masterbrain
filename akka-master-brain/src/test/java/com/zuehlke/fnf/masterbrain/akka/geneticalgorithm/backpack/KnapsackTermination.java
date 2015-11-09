package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Termination;

import java.math.BigInteger;

/**
 * Created by mhan on 09.07.2015.
 */
public class KnapsackTermination implements Termination<BigInteger> {
    private static final int MAX_ITERATIONS = 150;
    private int currentIteration = 0;

    @Override
    public boolean isFinished(final Population<BigInteger> population, Configuration config) {
        return currentIteration++ >= MAX_ITERATIONS;
    }
}
