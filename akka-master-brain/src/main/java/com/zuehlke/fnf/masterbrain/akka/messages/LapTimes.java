package com.zuehlke.fnf.masterbrain.akka.messages;

import com.google.common.collect.EvictingQueue;

/**
 * Created by tho on 05.08.2015.
 */
public class LapTimes {
    private final LapTime[] lapTimes;

    private LapTimes(LapTime[] lapTimes) {
        this.lapTimes = lapTimes;
    }

    public static LapTimes from(EvictingQueue<LapTime> lapTimes) {
        LapTime[] lt = lapTimes.toArray(new LapTime[lapTimes.size()]);
        return new LapTimes(lt);
    }

    public LapTime[] getLapTimes() {
        return lapTimes;
    }
}
