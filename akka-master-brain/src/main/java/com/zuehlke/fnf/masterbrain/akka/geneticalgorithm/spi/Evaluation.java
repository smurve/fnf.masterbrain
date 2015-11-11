package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi;


import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import scala.concurrent.Future;

import java.io.Serializable;

/**
 * Stateless and default constructor mandatory
 */
public interface Evaluation<T> extends Serializable {
    Future<ScoredGenom<T>> evaluate(T element, Configuration configuration);
}
