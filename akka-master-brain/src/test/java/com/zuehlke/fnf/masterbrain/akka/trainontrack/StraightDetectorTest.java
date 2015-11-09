package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import static org.junit.Assert.*;

import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackPoints;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

public class StraightDetectorTest {

	@Test
	public void testBasicStraigth() {
		
		double[] gs = new double[]{0,0,0,10000,10000,10000};
		
		Track track = new Track();
		TrackPoints tp = TrackPoints.from(null, null, gs, new double[gs.length]);
		track.setTrackpoints(tp);
		
		StraightDetector sd = new StraightDetector(track);
		assertThat(sd.getNofStraightsAhead(0), is(3));
		assertThat(sd.getNofStraightsAhead(1), is(2));
		assertThat(sd.getNofStraightsAhead(2), is(1));
		assertThat(sd.getNofStraightsAhead(3), is(0));
		assertThat(sd.getNofStraightsAhead(4), is(0));
		assertThat(sd.getNofStraightsAhead(5), is(0));
	}
	
	@Test
	public void testBoundaryStraigth() {
		
		double[] gs = new double[]{0,0,10000,10000,10000,0};
		
		Track track = new Track();
		TrackPoints tp = TrackPoints.from(null, null, gs, new double[gs.length]);
		track.setTrackpoints(tp);
		
		StraightDetector sd = new StraightDetector(track);
		assertThat(sd.getNofStraightsAhead(0), is(2));
		assertThat(sd.getNofStraightsAhead(1), is(1));
		assertThat(sd.getNofStraightsAhead(2), is(0));
		assertThat(sd.getNofStraightsAhead(3), is(0));
		assertThat(sd.getNofStraightsAhead(4), is(0));
		assertThat(sd.getNofStraightsAhead(5), is(3));
	}
	
	@Test
	public void testNegativeGStraigth() {
		
		double[] gs = new double[]{0,0,0,-10000,-10000,10000};
		
		Track track = new Track();
		TrackPoints tp = TrackPoints.from(null, null, gs, new double[gs.length]);
		track.setTrackpoints(tp);
		
		StraightDetector sd = new StraightDetector(track);
		assertThat(sd.getNofStraightsAhead(0), is(3));
		assertThat(sd.getNofStraightsAhead(1), is(2));
		assertThat(sd.getNofStraightsAhead(2), is(1));
		assertThat(sd.getNofStraightsAhead(3), is(0));
		assertThat(sd.getNofStraightsAhead(4), is(0));
		assertThat(sd.getNofStraightsAhead(5), is(0));
	}

}
