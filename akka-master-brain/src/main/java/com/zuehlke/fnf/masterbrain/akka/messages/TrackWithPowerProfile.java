package com.zuehlke.fnf.masterbrain.akka.messages;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhan on 12.07.2015.
 */
public class TrackWithPowerProfile {
    private final Track track;
    private final List<PowerSetting> powerSettings;

    private TrackWithPowerProfile(final Track track, final List<PowerSetting> powerSettings) {
        this.track = track;
        this.powerSettings = powerSettings;
    }

    public static TrackWithPowerProfile from(final Track track, final ImmutableList<Double> powerSettings) {
        ArrayList<PowerSetting> builder = new ArrayList<>();
        List<TrackSegment> segments = track.getSegments(powerSettings.size());
        for (int i = 0; i < powerSettings.size(); i++) {
            TrackSegment trackSegment = segments.get(i);
            Double powerSetting = powerSettings.get(i);
            builder.add(new PowerSetting(trackSegment.getLength(), powerSetting));
        }

        return new TrackWithPowerProfile(track, builder);
    }

    public List<PowerSetting> getPowerSettings() {
        return powerSettings;
    }

    public Track getTrack() {
        return track;
    }

    public static class PowerSetting {
        private final double length;
        private final double powerSetting;

        public PowerSetting(final double length, final double powerSetting) {
            this.length = length;
            this.powerSetting = powerSetting;
        }

        public double getLength() {
            return length;
        }

        public double getPowerSetting() {
            return powerSetting;
        }
    }
}
