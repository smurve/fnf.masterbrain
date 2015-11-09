package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by mhan on 06.07.2015.
 */
public class Curve extends TrackPart {
    @JsonIgnore
    public static final int CURVE_ACCELERATION_FACTOR = 90;
    @JsonIgnore
    public static final int MAX_SPEED_FOR_UNIT_CIRCLE = 300;
    public static final String TYPE = "curve";

    private double[] c_x;
    private double[] c_y;
    private double[] ang_start;
    private double[] ang_end;

    @JsonIgnore
    private int sector_ind;
    @JsonIgnore
    private double radius = 0;

    public double[] getC_x() {
        return c_x;
    }

    public void setC_x(final double[] c_x) {
        this.c_x = c_x;
    }

    public double[] getC_y() {
        return c_y;
    }

    public void setC_y(final double[] c_y) {
        this.c_y = c_y;
    }

    public double[] getAng_start() {
        return ang_start;
    }

    public void setAng_start(final double[] ang_start) {
        this.ang_start = ang_start;
    }

    public double[] getAng_end() {
        return ang_end;
    }

    public void setAng_end(final double[] ang_end) {
        this.ang_end = ang_end;
    }

    public int getSector_ind() {
        return sector_ind;
    }

    public void setSector_ind(int sector_ind) {
        this.sector_ind = sector_ind;
    }

    public void calculateRadius() {
        if (getPreviousTrackPart() == null || !(getPreviousTrackPart() instanceof Straight)) {
            throw new IllegalStateException("No previous track part of type Straight present");
        }

        Straight previous = (Straight) getPreviousTrackPart();
        double straightEndX = previous.getX_end()[0];
        double straightEndY = previous.getY_end()[0];

        this.radius = Math.sqrt(Math.pow((straightEndX - c_x[0]), 2) + Math.pow((straightEndY - c_y[0]), 2));
    }

    @Override
    public void calculateTrackPartLength() {
        double angle_rad = Math.abs(ang_end[0] - ang_start[0]);
        this.trackPartLenghth = radius * angle_rad;
    }

    public TrackSegment buildCurveSegment() {
        TrackSegment trackSegment = new TrackSegment();
        trackSegment.setLength(trackPartLenghth);
        trackSegment.setMaxSpeed(calculateMaxSpeed());
        trackSegment.setAccelerationFactor(CURVE_ACCELERATION_FACTOR);
        trackSegment.setSector_ind(sector_ind);
        trackSegment.setType(TYPE);
        return trackSegment;
    }

    private double calculateMaxSpeed() {
        return radius * MAX_SPEED_FOR_UNIT_CIRCLE;
    }
}
