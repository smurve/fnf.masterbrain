package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackPoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 12.08.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpeedCalculatorTest {

    @Test
    public void testSimulateLap() {
        Track track = new Track();
        TrackPoints trackPoints = new TrackPoints();
        trackPoints.setGs(new double[]{0, 0, 10000, 0, 0, 0});
        track.setTrackpoints(trackPoints);
        FeatureExtraxtor featureExtraxtor = new FeatureExtraxtor(track, 1, 1);
        TOTGenom genom = new TOTGenom(new double[]{0, 1, 0.0001, 0.0001});
        SpeedCalculator calculator = new SpeedCalculator(featureExtraxtor, genom, 255, 0);
        int[] simulation = calculator.simulateLap();
        assertThat(simulation, is(new int[]{4, 3, 2, 2, 1, 5}));
    }
}
