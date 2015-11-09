package com.zuehlke.fnf.masterbrain.akka.utils;

import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by tho on 08.09.2015.
 */
public class StopWatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopWatch.class);

    private LapSurveillant lapSurveillant;
    private long lapStartNanos;
    private Track track;

    public void setTrack(Track track) {
        this.track = track;
    }

    public void onLocation(Locations locations, Consumer<Long> onComplete) {
        if (track == null) {
            LOGGER.info("No track, yet.");
            return;
        }
        int highestProbIndex = locations.getIndexWithHighestPropability();
        if (highestProbIndex >= 0) {
            if (lapSurveillant == null || lapSurveillant.isLapFinished(highestProbIndex)) {
                if (lapStartNanos > 0) {
                    long lapTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lapStartNanos);
                    LOGGER.debug("Lap complete. time={} ms", lapTime);
                    onComplete.accept(lapTime);
                }

                lapSurveillant = new LapSurveillant(track, locations);
                lapStartNanos = System.nanoTime();
            }
        }
    }
}
