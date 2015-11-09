package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by mhau on 08.07.2015.
 */
public abstract class TrackPart {

    @JsonIgnore
    protected double trackPartLenghth;
    @JsonIgnore
    private TrackPart nextTrackPart;
    @JsonIgnore
    private TrackPart previousTrackPart;

    public TrackPart getNextTrackPart() {
        return nextTrackPart;
    }

    void setNextTrackPart(TrackPart next) {
        nextTrackPart = next;
    }

    public TrackPart getPreviousTrackPart() {
        return previousTrackPart;
    }

    public void setPreviousTrackPart(TrackPart previousTrackPart) {
        this.previousTrackPart = previousTrackPart;
    }

    public double getTrackPartLenghth() {
        return trackPartLenghth;
    }

    public abstract void calculateTrackPartLength();
}
