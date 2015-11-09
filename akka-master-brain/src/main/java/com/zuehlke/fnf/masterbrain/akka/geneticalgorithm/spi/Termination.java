package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;

/**
 * Statefull and default constructor mandatory
 */
public interface Termination<T> {
    boolean isFinished(Population<T> populationEvaluation, Configuration configuration);
}
