package com.zuehlke.fnf.masterbrain.akka.messages;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mhau on 09.07.2015.
 */
public class TrackSegment {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackSegment.class);
    private static final double SIZE_FACTOR = 120;
    private static final double SEGMENT_TIME_THRESHOLD = 25;
    private static final double PENALTY_TIME = 1.5;

    private double length;
    private double maxSpeed;
    private double v0;
    private double v1;
    private double accelerationFactor;
    private double transitTime;
    private int sector_ind;
    private String type;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length * SIZE_FACTOR;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getV0() {
        return v0;
    }

    public void setV0(double v0) {
        this.v0 = v0;
    }

    public double getV1() {
        return v1;
    }

    private void setV1(double v1) {
        if (Curve.TYPE.equals(type)) {
            this.v1 = Math.min(this.v1, v1);
        } else {
            this.v1 = Math.max(this.v1, v1);
        }
    }

    public double getAccelerationFactor() {
        return accelerationFactor;
    }

    public void setAccelerationFactor(double accelerationFactor) {

        this.accelerationFactor = accelerationFactor > 200 ? accelerationFactor * 0.95 : accelerationFactor;
    }

    public double getTransitTime() {
        return transitTime;
    }

    public int getSector_ind() {
        return sector_ind;
    }

    public void setSector_ind(int sector_ind) {
        this.sector_ind = sector_ind;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (Curve.TYPE.equals(type)) {
            this.v1 = Double.MAX_VALUE;
        }
        this.type = type;
    }

    public void drive(double powerSetting) {
        transitTime = getSegmentTime(length, powerSetting, v0);

        if (Curve.TYPE.equals(type)) {
            if (v0 > maxSpeed) {
                transitTime += PENALTY_TIME;
            }
        }

        if (transitTime < 0) {
            String logMsg = String.format("track segment [index = " + getSector_ind() + "] [v0 = %1$,.5f]\t[v1 = %2$,.5f]\t[t = %3$,.5f]" +
                    "\t[l = %4$,.5f]\t[pow = %5$,.5f]\ttype = " + type, v0, v1, transitTime, length, powerSetting);
            LOGGER.error(logMsg);
        }

    }

    private double getSegmentTime(double length, double powerSetting, double v0) {
        double acceleration = getAcceleration(v0, powerSetting);
        if (length > SEGMENT_TIME_THRESHOLD) {
            double t1 = getSegmentTime(length / 2, powerSetting, v0);
            double v0_1 = getV1(acceleration, v0, t1, powerSetting);
            double t2 = getSegmentTime(length / 2, powerSetting, v0_1);
            return t1 + t2;
        }
        double segmentTime = (1 / acceleration) * ((-v0) + Math.sqrt(Math.pow(v0, 2) + (2 * acceleration * length)));

        double v1;

        if (Double.isNaN(segmentTime) || segmentTime < 0) {
            v1 = getVMin(powerSetting);
            segmentTime = length / Math.abs((v1 - v0) / 2);
        } else {
            v1 = getV1(acceleration, v0, segmentTime, powerSetting);
        }

        //v1 =
        setV1(v1);
        return segmentTime;
    }

    private double getVMin(double powerSetting) {
        return 3 * Math.sqrt(powerSetting) + 5;
    }

    private double getV1(double acceleration, double v0, double time, double powerSettings) {
        return Math.max(getVMin(powerSettings), v0 + (acceleration * time));
    }

    private double getAcceleration(double v0, double power) {
        if (Straight.TYPE.equals(type)) {
            return getStraightAcceleration(v0, power);
        } else {
            return getCurveAcceleration(v0, power);
        }
    }

    private double getCurveAcceleration(double v0, double power) {
        double vMax = getVMax(power);
        double acceleration;
        if (v0 > vMax) {
            // acceleration = (-accelerationFactor) * v0 / getVMax(power);
            acceleration = (-accelerationFactor);
        } else {
            acceleration = -accelerationFactor * Math.pow(v0, 2) / Math.pow(vMax, 2);
        }
        return Math.min(acceleration, -0.01);
    }

    private double getStraightAcceleration(double v0, double power) {
        double vMax = getVMax(power);
        double acceleration = ((-accelerationFactor) * v0 / getVMax(power)) + accelerationFactor;
        if (acceleration < -accelerationFactor) {
            acceleration = -accelerationFactor;
        }
        return acceleration;
        // return Math.max(acceleration, 0.01);
    }

    private double getVMax(double power) {
        return 2.14 * power;
    }


}
