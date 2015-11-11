package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.numberguess;

import akka.dispatch.Futures;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import scala.concurrent.Future;

/**
 * Created by tho on 13.08.2015.
 */
public class NumberGuessingEvaluation implements Evaluation<Integer> {

    private int numberToGuess = 42;

    @Override
    public Future<ScoredGenom<Integer>> evaluate(Integer element, Configuration configuration) {
        return Futures.future(() -> {
            int diff = numberToGuess - element;
            diff = Math.abs(diff);
            return new ScoredGenom<>(element, diff);
        }, configuration.getExecutionContextExecutor());
    }
}
