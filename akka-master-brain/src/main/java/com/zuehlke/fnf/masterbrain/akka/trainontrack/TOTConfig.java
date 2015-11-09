package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.typesafe.config.Config;
import com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tho on 07.09.2015.
 */
public class TOTConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TOTConfig.class);
    private int populationSize;
    private int maxPower;
    private int minPower;
    private int safetyPower;
    private int curveLookBack;
    private int curveLookAhead;
    private int maxZeroPowerInArow;
    private int minLapDuration;
    private int safetyDuration;
    private List<Integer> w0range;
    private List<Double> w0mutationRange;
    private List<Double> w1range;
    private List<Double> w1mutationRange;
    private double w2init;
    private double w3init;
    private boolean useCache;

    public TOTConfig(Config config) {
        safetyPower = ConfigHelper.readValue("masterbrain.pilot.safetyPower", config::getInt, 160);
        minPower = ConfigHelper.readValue("masterbrain.pilot.minPower", config::getInt, 0);
        maxPower = ConfigHelper.readValue("masterbrain.pilot.maxPower", config::getInt, 255);
        populationSize = ConfigHelper.readValue("masterbrain.pilot.tot.populationSize", config::getInt, 10);
        curveLookAhead = ConfigHelper.readValue("masterbrain.pilot.tot.curveLookAhead", config::getInt, 40);
        curveLookBack = ConfigHelper.readValue("masterbrain.pilot.tot.curveLookBack", config::getInt, 30);
        maxZeroPowerInArow = ConfigHelper.readValue("masterbrain.pilot.tot.maxZeroPowerInARow", config::getInt, 20);
        useCache = ConfigHelper.readValue("masterbrain.pilot.tot.cache", config::getBoolean, true);
        w0range = ConfigHelper.readValue("masterbrain.pilot.tot.w0range", config::getIntList, Arrays.asList(140, 170));
        w1range = ConfigHelper.readValue("masterbrain.pilot.tot.w1range", config::getDoubleList, Arrays.asList(0.2, 0.4));
        w2init = ConfigHelper.readValue("masterbrain.pilot.tot.w2init", config::getDouble, -0.003);
        w3init = ConfigHelper.readValue("masterbrain.pilot.tot.w3init", config::getDouble, 0.01);
        minLapDuration = ConfigHelper.readValue("masterbrain.pilot.tot.minLapDuration", config::getInt, 4000);
        safetyDuration = ConfigHelper.readValue("masterbrain.pilot.tot.safetyDuration", config::getInt, 3000);

        w0mutationRange = ConfigHelper.readValue("masterbrain.pilot.tot.w0mutationRange", config::getDoubleList, Arrays.asList(-10d, 10d));
        w1mutationRange = ConfigHelper.readValue("masterbrain.pilot.tot.w1mutationRange", config::getDoubleList, Arrays.asList(-0.2, 0.2));

        LOGGER.info("{}", ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE));
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getMinPower() {
        return minPower;
    }

    public int getSafetyPower() {
        return safetyPower;
    }

    public int getCurveLookBack() {
        return curveLookBack;
    }

    public int getCurveLookAhead() {
        return curveLookAhead;
    }

    public int getMaxZeroPowerInArow() {
        return maxZeroPowerInArow;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public List<Integer> getW0range() {
        return w0range;
    }

    public List<Double> getW1range() {
        return w1range;
    }

    public double getW2init() {
        return w2init;
    }

    public double getW3init() {
        return w3init;
    }

    public int getMinLapDuration() {
        return minLapDuration;
    }

    public int getSafetyDuration() {
        return safetyDuration;
    }

    public List<Double> getW0mutationRange() {
        return w0mutationRange;
    }

    public List<Double> getW1mutationRange() {
        return w1mutationRange;
    }
}
