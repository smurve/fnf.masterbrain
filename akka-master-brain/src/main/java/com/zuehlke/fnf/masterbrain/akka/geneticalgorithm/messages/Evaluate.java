package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages;

/**
 * Created by mhan on 08.07.2015.
 */
public class Evaluate<T> {
    private final T genom;

    public Evaluate(final T genom) {
        this.genom = genom;
    }

    public T getGenom() {
        return genom;
    }
}
