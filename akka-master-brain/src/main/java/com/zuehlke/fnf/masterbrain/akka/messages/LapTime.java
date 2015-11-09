package com.zuehlke.fnf.masterbrain.akka.messages;

import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;

/**
 * Created by tho on 05.08.2015.
 */
public class LapTime {


    private final long start;
    private final long end;
    private final long duration;
    private final long differenceToLast;
    private int lap;

    public LapTime(int lap, long start, long end, long duration, long differenceToLast) {
        this.lap = lap;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.differenceToLast = differenceToLast;
    }

    public static LapTime from(LapTime last, RoundTimeMessage current) {
        int lap = 1;
        long start = 0;
        long end = current.getTimestamp();
        long duration = current.getRoundDuration();
        long differenceToLast = 0;
        if (last != null) {
            lap = last.getLap() + 1;
            start = last.getEnd();
            differenceToLast = duration - last.getDuration();
        }
        return new LapTime(lap, start, end, duration, differenceToLast);
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getDuration() {
        return duration;
    }

    public int getLap() {
        return lap;
    }

    public long getDifferenceToLast() {
        return differenceToLast;
    }
}
