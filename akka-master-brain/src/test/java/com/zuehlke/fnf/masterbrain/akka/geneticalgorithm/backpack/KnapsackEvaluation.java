package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;

import java.math.BigInteger;

/**
 * Created by mhan on 09.07.2015.
 */
public class KnapsackEvaluation implements Evaluation<BigInteger> {

    public static final int CAPACITY = 20;

    @Override
    public ScoredGenom<BigInteger> evaluate(final BigInteger element, Configuration config) {
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
    }
}
