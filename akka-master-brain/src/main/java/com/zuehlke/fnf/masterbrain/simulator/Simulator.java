package com.zuehlke.fnf.masterbrain.simulator;

import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhau on 09.07.2015.
 */
public class Simulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Simulator.class);

    private final Track track;
    private final double initSpeed;

    public Simulator(Track track, double initSpeed) {
        this.track = track;
        this.initSpeed = initSpeed;
    }

    public Simulator(Track track) {
        this(track, 0);
    }

    public SimulationResult simulate(List<Double> a) {
        List<Double> powerSettings = new ArrayList<>(a);
        List<TrackSegment> trackSegments = track.getSegments(powerSettings.size());
        trackSegments.get(0).setV0(initSpeed);

        for (int i = 0; i < trackSegments.size(); i++) {
            TrackSegment segment = trackSegments.get(i);
            double powerSetting = powerSettings.get(i);
            segment.drive(powerSetting);

            if (i != trackSegments.size() - 1) {
                trackSegments.get(i + 1).setV0(segment.getV1());
            }
        }

        SimulationResult result = new SimulationResult();
        result.setTrackTime(trackSegments.stream().mapToDouble(TrackSegment::getTransitTime).sum());
        result.setTrackLength(trackSegments.stream().mapToDouble(TrackSegment::getLength).sum());

        if (result.getTrackTime() < 0) {
            LOGGER.error("received flux-compensation problem, the world may be in a harzard state. score: {}, track-length: {} \n Intput: {}", result
                    .getTrackTime(), result.getTrackLength(), powerSettings);
            throw new IllegalStateException();
        }
        return result;
    }
}
