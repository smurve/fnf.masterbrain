package com.zuehlke.fnf.masterbrain.akka.messages;

import com.google.common.collect.HashMultiset;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * Created by tho on 13.07.2015.
 */
public class TrackPoints {


    private HashMultiset<Integer> sektorToNumberMapping;
    private double[] ys;
    private double[] xs;
    private double[] gs;
    private double[] index;

    public static TrackPoints from(double[] xs, double[] ys, double[] gs, final double[] index) {
        TrackPoints tp = new TrackPoints();
        tp.setIndices(index);
        tp.setXs(xs);
        tp.setYs(ys);
        tp.setGs(gs);
        tp.setSektorToNumberMapping(DoubleStream.of(index).mapToInt(d -> (int) d).boxed().collect(Collectors.toCollection
                (HashMultiset::create)));
        return tp;
    }

    public double[] getXs() {
        return xs;
    }

    public void setXs(double[] xs) {
        this.xs = xs;
    }

    public double[] getYs() {
        return ys;
    }

    public void setYs(double[] ys) {
        this.ys = ys;
    }

    public double[] getIndex() {
        return index;
    }

    public void setIndices(final double[] index) {
        this.index = index;
    }

    public double[] getGs() {
        return gs;
    }

    public void setGs(double[] gs) {
        this.gs = gs;
    }

    public int size() {
        return index.length;
    }

    public int getNumberOfPointsForSectorIndex(final int sector_ind) {
        return sektorToNumberMapping.count(sector_ind);
    }

    public void setSektorToNumberMapping(final HashMultiset<Integer> sektorToNumberMapping) {
        this.sektorToNumberMapping = sektorToNumberMapping;
    }
}
