package com.zuehlke.fnf.masterbrain.akka.messages;

public class Locations {

    private double[] xs;
    private double[] ys;
    private double[] ps;
    private Status status;

    public static Locations from(double[] xs, double[] ys, double[] ps) {
        Locations loc = new Locations();
        loc.setXs(xs);
        loc.setYs(ys);
        loc.setPs(ps);
        return loc;
    }

    public static Locations from(Track track, LocationPs message) {
        double[] xs = track == null ? new double[0] : track.getTrackPoints().getXs();
        double[] ys = track == null ? new double[0] : track.getTrackPoints().getYs();
        double[] ps = message == null ? new double[0] : message.getPs();
        Status status = message == null ? null : message.getStatus();
        Locations locs = from(xs, ys, ps);
        locs.setStatus(status);
        return locs;
    }

    public double[] getPs() {
        return ps;
    }

    public void setPs(double[] ps) {
        this.ps = ps;
    }

    public double[] getYs() {
        return ys;
    }

    public void setYs(double[] ys) {
        this.ys = ys;
    }

    public double[] getXs() {
        return xs;
    }

    public void setXs(double[] xs) {
        this.xs = xs;
    }

    public int getIndexWithHighestPropability() {
        int maxIndex = -1;
        double maxNumber = -1;
        if (ps != null) {
            for (int i = 0; i < ps.length; i++) {
                double newnumber = ps[i];
                if (newnumber > maxNumber) {
                    maxIndex = i;
                    maxNumber = newnumber;
                }
            }
        }
        return maxIndex;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
