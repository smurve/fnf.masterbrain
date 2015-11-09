package com.zuehlke.fnf.masterbrain.akka.messages;

/**
 * Created by tho on 13.07.2015.
 */
public class LocationPs {

    private double[] ps;

    private Status status;

    public double[] getPs() {
        return ps;
    }

    public void setPs(double[] ps) {
        this.ps = ps;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
