package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Created by mhan on 10.07.2015.
 */
public class PowerProfileGenom {
    private final ImmutableList<Double> powerSettings;

    public PowerProfileGenom(final List<Double> powerSettings) {
        this.powerSettings = ImmutableList.copyOf(powerSettings);
    }

    public ImmutableList<Double> getPowerSettings() {
        return powerSettings;
    }
}
