package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Track;

import static java.lang.Math.abs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StraightDetector {

	private static final Logger LOGGER = LoggerFactory.getLogger(StraightDetector.class);
	private static final double MIN_G = 1000;
	private int[] straightsAhead;
	
	public StraightDetector(Track track) {
		double[] gs = track.getTrackPoints().getGs();
		straightsAhead = new int[gs.length];
		
		int trailingStraights = 0;
		while(abs(gs[gs.length - trailingStraights - 1]) <= MIN_G) {
			trailingStraights++;
			if(trailingStraights >= gs.length) {
				LOGGER.warn("No curves detected for GA Features");
				return;
			}
		}
		
		int straightCount = 0;
		for(int i = gs.length - trailingStraights - 1; i >= 0; i--) {
			if(abs(gs[i]) > MIN_G) {
				straightCount = 0;				
			} else {
				straightCount++;
			}
			straightsAhead[i] = straightCount;
		}
		
		for(int i = gs.length - 1; i > gs.length - trailingStraights - 1; i--) {
			straightCount++;
			straightsAhead[i] = straightCount;
		}
	}
	
	public int getNofStraightsAhead(int index) {
		return(straightsAhead[index]);
	}

}
