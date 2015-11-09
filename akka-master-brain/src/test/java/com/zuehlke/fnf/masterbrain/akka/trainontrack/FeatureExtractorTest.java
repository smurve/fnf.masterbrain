package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackPoints;
import com.zuehlke.fnf.masterbrain.akka.messages.Status;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 11.08.2015.
 */
public class FeatureExtractorTest {

    @Test
    public void testExtract() {
        Track track = new Track();
        TrackPoints trackpoints = new TrackPoints();
        trackpoints.setGs(new double[]{1,2,3,4,5,2000,7,8,9,10});
        track.setTrackpoints(trackpoints);


        Locations locations = new Locations();
        locations.setPs(new double[]{0,0,1,0,0,0,0,0,0,0});
        locations.setStatus(Status.ok());

        FeatureExtraxtor extraxtor = new FeatureExtraxtor(track, 3, 2);
        double[] actual = extraxtor.extract(locations);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.length, is(3));
        assertThat(actual[0], is(9d));
        assertThat(actual[1], is(10d));
        assertThat(actual[2], is(1000d));
    }

    @Test
    public void testExtractAbsValues() {
        Track track = new Track();
        TrackPoints trackpoints = new TrackPoints();
        trackpoints.setGs(new double[]{-1,2,-3,-4,-5,-6,7,-8000,9,-10});
        track.setTrackpoints(trackpoints);


        Locations locations = new Locations();
        locations.setPs(new double[]{0,0,1,0,0,0,0,0,0,0});
        locations.setStatus(Status.ok());

        FeatureExtraxtor extraxtor = new FeatureExtraxtor(track, 3, 3);
        double[] actual = extraxtor.extract(locations);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.length, is(3));
        assertThat(actual[0], is(1d));
        assertThat(actual[1], is(8000d));
        assertThat(actual[2], is(7d));
    }

    @Test
    public void testExtractAtEndOfTrack() {
        Track track = new Track();
        TrackPoints trackpoints = new TrackPoints();
        trackpoints.setGs(new double[]{1,2000,3,4,5,6,7,8,9,10});
        track.setTrackpoints(trackpoints);


        Locations locations = new Locations();
        locations.setPs(new double[]{0,0,0,0,0,0,0,0,1,0});
        locations.setStatus(Status.ok());

        FeatureExtraxtor extraxtor = new FeatureExtraxtor(track, 3, 2);
        double[] actual = extraxtor.extract(locations);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.length, is(3));
        assertThat(actual[0], is(9d));
        assertThat(actual[1], is(6d));
        assertThat(actual[2], is(1000d));
    }
}
