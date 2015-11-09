package com.zuehlke.fnf.masterbrain.akka.messages;

/**
 * Created by tho on 04.09.2015.
 */
public class TimeDelta {
    private long delta;

    public TimeDelta(long newDelta) {

        this.delta = newDelta;
    }

    public long getDelta() {
        return delta;
    }
}
