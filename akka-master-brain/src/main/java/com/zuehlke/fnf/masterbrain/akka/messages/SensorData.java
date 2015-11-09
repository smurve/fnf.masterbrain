package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class SensorData {

    @JsonIgnore
    private final long deltaToSystemtimeAtIncome;
    private long t;
    private int[] g;
    private int force = -1;

    public SensorData() {
        this.deltaToSystemtimeAtIncome = 0;
    }

    public SensorData(long t, int... g) {
        this.t = t;
        this.g = g;
        this.deltaToSystemtimeAtIncome = System.currentTimeMillis() - t;
    }

    public static SensorData fromEvent(SensorEvent event) {
        return new SensorData(event.getTimeStamp(), event.getG()[2]);
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public int[] getG() {
        return g;
    }

    public void setG(final int[] g) {
        this.g = g;
    }

    public int getForce() {
        return force;
    }

    public void setForce(int force) {
        this.force = force;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public long getDeltaToSystemtimeAtIncome() {
        return deltaToSystemtimeAtIncome;
    }
}
