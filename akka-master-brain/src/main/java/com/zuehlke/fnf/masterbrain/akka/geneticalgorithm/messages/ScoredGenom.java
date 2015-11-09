package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages;

/**
 * Created by mhan on 08.07.2015.
 */
public class ScoredGenom<T> implements Comparable<ScoredGenom<?>> {
    private final T genom;
    private final double score;

    public ScoredGenom(final T genom, final double score) {
        this.genom = genom;
        this.score = score;
    }

    public T getGenom() {
        return genom;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(final ScoredGenom<?> o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "ScoredGenom{" +
                "genom=" + genom +
                ", score=" + score +
                '}';
    }
}
