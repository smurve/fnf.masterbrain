package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Math.abs;

/**
 * Created by tho on 10.08.2015.
 */
public class FeatureExtraxtor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureExtraxtor.class);
    private final int curveLookAhead;
    private final int curveLookBack;
    private Track track;
    private StraightDetector straightDetector;
    private int numberOfSegments;

    public FeatureExtraxtor(Track track, int curveLookAhead, int curveLookBack) {
        this.track = track;
        straightDetector = new StraightDetector(track);
        this.curveLookAhead = curveLookAhead;
        this.curveLookBack = curveLookBack;
        numberOfSegments = track.getTrackPoints().getGs().length;
    }

    double[] extract(Locations locations) {
        if (!locations.getStatus().isOk()) {
            return null;
        }
        int locationIndex = locations.getIndexWithHighestPropability();
        if (locationIndex < 0) {
            return null;
        }
        
        int nofLocations = locations.getPs().length;
        locationIndex = (locationIndex + 4 + nofLocations) % nofLocations;
        
        double[] result = new double[3];
        
        result[0] = getStraightAheadFeature(locationIndex);
        result[1] = getCurveAheadFeature(locationIndex);
        result[2] = getCurveBackFeature(locationIndex);
        
        LOGGER.debug("Features are: {}", result);
        return result;
    }

	private double getStraightAheadFeature(int locationIndex) {
		return straightDetector.getNofStraightsAhead(locationIndex);
	}
    
    private double getCurveAheadFeature(int locationIndex) {
    	double maxGValue = 0;
    	for(int i = 1; i <= curveLookAhead; i++) {
    		int variableIndex = (locationIndex + i) % numberOfSegments;
    		double variableG = track.getTrackPoints().getGs()[variableIndex];
    		if(abs(variableG) > maxGValue) {
    			maxGValue = abs(variableG);
    		}
    	}
    	return maxGValue;
	}

	private double getCurveBackFeature(int locationIndex) {
		double maxGValue = 0;
    	for(int i = 0; i <= curveLookBack; i++) {
    		int variableIndex = (numberOfSegments + locationIndex - i) % numberOfSegments;
    		double backScale = (double)(curveLookBack - i) / curveLookBack;
    		double variableG = track.getTrackPoints().getGs()[variableIndex] * backScale;
    		if(abs(variableG) > maxGValue) {
    			maxGValue = abs(variableG);
    		}
    	}
    	return maxGValue;
	}
    
	public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
        straightDetector = new StraightDetector(track);
    }
}
