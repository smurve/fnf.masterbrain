package com.zuehlke.fnf.masterbrain.akka.messages;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Created by mhan on 10.07.2015.
 */
public class LearningFinished {
    private final ImmutableList<Double> powerSettings;
    private final double score;

    public LearningFinished(final List<Double> powerSettings, final double score) {
        this.score = score;
        this.powerSettings = ImmutableList.copyOf(powerSettings);
    }

    public double getScore() {
        return score;
    }

    public ImmutableList<Double> getPowerSettings() {
        return powerSettings;
    }
}
