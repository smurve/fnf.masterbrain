package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhan on 06.07.2015.
 */
public class Straight extends TrackPart {
    @JsonIgnore
    public static final int STRAIGHT_ACCELERATION_FACTOR = 170;
    public static final String TYPE = "straight";

    private double[] x_start;
    private double[] x_end;

    private double[] y_start;
    private double[] y_end;

    private double[] x_dir;
    private double[] y_dir;
    private int[] sector_ind;

    public double[] getX_start() {
        return x_start;
    }

    public void setX_start(final double[] x_start) {
        this.x_start = x_start;
    }

    public double[] getX_end() {
        return x_end;
    }

    public void setX_end(final double[] x_end) {
        this.x_end = x_end;
    }

    public double[] getY_start() {
        return y_start;
    }

    public void setY_start(final double[] y_start) {
        this.y_start = y_start;
    }

    public double[] getY_end() {
        return y_end;
    }

    public void setY_end(final double[] y_end) {
        this.y_end = y_end;
    }

    public double[] getX_dir() {
        return x_dir;
    }

    public void setX_dir(final double[] x_dir) {
        this.x_dir = x_dir;
    }

    public double[] getY_dir() {
        return y_dir;
    }

    public void setY_dir(final double[] y_dir) {
        this.y_dir = y_dir;
    }

    public int[] getSector_ind() {
        return sector_ind;
    }

    public void setSector_ind(final int[] sector_ind) {
        this.sector_ind = sector_ind;
    }

    @Override
    public void calculateTrackPartLength() {
        trackPartLenghth = Math.sqrt(Math.pow((x_start[0] - x_end[0]), 2) +
                Math.pow((y_start[0] - y_end[0]), 2));
    }

    public List<TrackSegment> buildStraightSegments(int numberOfSegments) {
        List<TrackSegment> segments = new ArrayList<>();
        for (int i = 0; i < numberOfSegments; i++) {
            TrackSegment segment = new TrackSegment();
            segment.setLength(trackPartLenghth / numberOfSegments);
            segment.setMaxSpeed(Double.MAX_VALUE);
            segment.setAccelerationFactor(STRAIGHT_ACCELERATION_FACTOR);
            segment.setSector_ind(sector_ind[0]);
            segment.setType(TYPE);
            segments.add(segment);
        }
        return segments;
    }
}
