package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.numberguess;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Termination;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tho on 13.08.2015.
 */
public class NumberGuessingTermination implements Termination<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NumberGuessingTermination.class);
    private int counter;

    @Override
    public boolean isFinished(Population<Integer> populationEvaluation, Configuration configuration) {
        counter++;
        LOGGER.info("counter={}", counter);
        return counter >= 10;
    }
}
