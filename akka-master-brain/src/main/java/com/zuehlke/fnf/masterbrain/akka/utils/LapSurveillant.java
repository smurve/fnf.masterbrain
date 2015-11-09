package com.zuehlke.fnf.masterbrain.akka.utils;

import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tho on 14.08.2015.
 */
public class LapSurveillant {
    private static final Logger LOGGER = LoggerFactory.getLogger(LapSurveillant.class);
    private static final double MIN_TOUCHED_POINTS_RATIO = 0.05;
    private final int trackLength;
    private final int startIndex;
    private final int numOfSegments;
    private final int segmentLength;


    private int[] track;
    private int minTouchedPoints;
    private int touchedPoints;
    private int[] segments;

    private int lastSegment;

    public LapSurveillant(int trackLength, int startIndex, int numOfSegments) {
        this.trackLength = trackLength;
        this.startIndex = startIndex;
        this.numOfSegments = numOfSegments;
        this.segmentLength = trackLength / numOfSegments;
        track = new int[trackLength];
        minTouchedPoints = (int)(track.length * MIN_TOUCHED_POINTS_RATIO);
        touchedPoints = 0;
        segments = new int[numOfSegments];
        LOGGER.debug("segment thresholds=[{}, {}]", getThresholdSegmentIndexStartOfTrack(), getThresholdSegmentIndexEndOfTrack());
    }

    public LapSurveillant(Track track, Locations message) {
        this(message.getPs().length, message.getIndexWithHighestPropability(), (int) Math.max(2, Math.round(track.getTrackLength() * 0.2)));
    }

    public boolean isLapFinished(int location) {
        if (location < 0) {
            return false;
        }
        int index = (location - startIndex + trackLength) % trackLength;
        LOGGER.debug("Location {} is index {}", location, index);
        if(track[index] == 0) {
        	touchedPoints++;
        }
        track[index]++;

        int currentSegment = Math.min(index / segmentLength, numOfSegments - 1);
        if (lastSegment == currentSegment) {
            LOGGER.debug("Still in same segment");
        } else {
            LOGGER.debug("Segment jump");
			if (segments[currentSegment] == 0) {
                LOGGER.debug("Entering segment={}", currentSegment);
                if (lastSegment < currentSegment) {
                    LOGGER.debug("We're moving forward.");
                    segments[currentSegment]++;
                } else {
                    if (currentSegment <= getThresholdSegmentIndexStartOfTrack() && lastSegment >= getThresholdSegmentIndexEndOfTrack()) {
                        if(touchedPoints > minTouchedPoints) {
                            LOGGER.info("Arrived at start segment without being in that segment before. Lap finished.");
                            return true;
                        } else {
                            LOGGER.info("Arrived at start segment but did not touch enough track points ({} from required {}). Lap not finished.", touchedPoints, minTouchedPoints);
                        	return false;
                        }
                    } else {
                        LOGGER.debug("Unwanted back jump. Ignoring this");
                        return false;
                    }
                }
            } else {
                LOGGER.info("Been here before... segment={}", currentSegment);
                if (currentSegment <= getThresholdSegmentIndexStartOfTrack()) {
                	if(touchedPoints > minTouchedPoints) {
                		LOGGER.info("Arrived at start segment. Lap finished.");
                		return true;
                    } else {
                		LOGGER.info("Arrived at start segment but did not touch enough track points ({} from required {}). Lap not finished.", touchedPoints, minTouchedPoints);
                    	return false;
                    }
                }
            }
        }
        lastSegment = currentSegment;

        //LOGGER.debug("Track is now: {}", Arrays.toString(track));
        //LOGGER.debug("Semgnets are now: {}", Arrays.toString(segments));


        return false;
    }

    private int getThresholdSegmentIndexEndOfTrack() {
        return segments.length - Math.max(1, numOfSegments / 6);
    }

    private int getThresholdSegmentIndexStartOfTrack() {
        return numOfSegments / 6;
    }
    
}
