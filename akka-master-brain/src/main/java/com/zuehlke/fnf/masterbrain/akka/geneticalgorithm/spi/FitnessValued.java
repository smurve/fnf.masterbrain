package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;

/**
 * Created by tho on 14.08.2015.
 */
public class FitnessValued<T> implements Comparable<FitnessValued<T>> {
    private final ScoredGenom<T> scoredGenom;
    private final double fitnessValue;

    public FitnessValued(final double fitnessValue) {
        this.fitnessValue = fitnessValue;
        this.scoredGenom = null;
    }

    public FitnessValued(final ScoredGenom<T> scoredGenom, final double fitnessValue) {
        this.scoredGenom = scoredGenom;
        this.fitnessValue = fitnessValue;
    }

    @Override
    public int compareTo(final FitnessValued<T> o) {
        return Double.compare(fitnessValue, o.fitnessValue);
    }

    public double getFitnessValue() {
        return fitnessValue;
    }

    public ScoredGenom<T> getScoredGenom() {
        return scoredGenom;
    }
}
