package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack;

import akka.dispatch.Futures;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import scala.concurrent.Future;

import java.math.BigInteger;

/**
 * Created by mhan on 09.07.2015.
 */
public class KnapsackEvaluation implements Evaluation<BigInteger> {

    public static final int CAPACITY = 20;

    @Override
    public Future<ScoredGenom<BigInteger>> evaluate(final BigInteger element, Configuration config) {
        return Futures.future(() -> {
            double score = 0;
            int currentSize = 0;
            for (Item item : Item.values()) {
                if (element.testBit(item.ordinal())) {
                    currentSize += item.getSize();
                    score += item.getValue();
                    if (currentSize > CAPACITY) {
                        return new ScoredGenom<>(element, 0);
                    }
                }
            }

            return new ScoredGenom<>(element, score);
        }, config.getExecutionContextExecutor());
    }
}
