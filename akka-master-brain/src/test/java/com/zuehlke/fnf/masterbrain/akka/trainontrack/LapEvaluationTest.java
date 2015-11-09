package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackPoints;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.messages.Status;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import scala.concurrent.Future;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.lt;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by tho on 11.08.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class LapEvaluationTest {

    @Mock
    private TOTPilotActor feedback;

    /**
     * Simple Track model of a round race track
     * __ooo
     * _o   o
     * O     o
     * o     o
     * o     o
     * _o   o
     * __ooo
     * <p/>
     * O is the start location
     * Race runs clockwise
     */
    private double[] xs = new double[]{1, 2, 3, 4, 5, 6, 7, 7, 7, 6, 5, 4, 3, 2, 1};
    private double[] ys = new double[]{3, 2, 1, 1, 1, 2, 3, 4, 5, 6, 7, 7, 7, 6, 5, 4};
    private double[] gs = new double[]{-50, 5000, 50, -50, 50, 5000, 50, -50, 50, 5000, 50, -50, 50, 5000, 50, -50};

    @Test
    public void testEvaluation() {

        Track track = new Track();
        TrackPoints trackPoints = new TrackPoints();
        trackPoints.setGs(gs);
        trackPoints.setXs(xs);
        trackPoints.setYs(ys);
        track.setTrackpoints(trackPoints);
        track.setStatus(Status.ok());
        FeatureExtraxtor featureExtraxtor = new FeatureExtraxtor(track, 2, 3);

        TOTGenom genom = new TOTGenom(new double[]{100, 100, 100, 100});
        LapEvaluation evaluation = new LapEvaluation(genom, 0, 255, 100, 0, 0, 10, featureExtraxtor, feedback);
        evaluation.setExitSafetyInCurve(false);
        Future<ScoredGenom<TOTGenom>> future = evaluation.getFuture();
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(1)).firePower(lt(256)); // switch to safety mode.

        // No lets 'drive' a complete lap
        evaluation.onLocations(createLocations(new double[]{1, 0, 0, 0, 0, 0, 0, 0}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(2)).firePower(lt(256));

        evaluation.onLocations(createLocations(new double[]{0, 1, 0, 0, 0, 0, 0, 0}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(3)).firePower(lt(256));

        evaluation.onLocations(createLocations(new double[]{0, 0, 1, 0, 0, 0, 0, 0}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(4)).firePower(lt(256));

        evaluation.onLocations(createLocations(new double[]{0, 0, 0, 1, 0, 0, 0, 0}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(5)).firePower(lt(256));

        evaluation.onLocations(createLocations(new double[]{0, 0, 0, 0, 1, 0, 0, 0}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(6)).firePower(lt(256));

        evaluation.onLocations(createLocations(new double[]{0, 0, 0, 0, 0, 1, 0, 0}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(7)).firePower(lt(256));

        evaluation.onLocations(createLocations(new double[]{0, 0, 0, 0, 0, 0, 1, 0}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(8)).firePower(lt(256));

        evaluation.onLocations(createLocations(new double[]{0, 0, 0, 0, 0, 0, 0, 1}), track);
        assertThat(future.isCompleted(), is(false));
        verify(feedback, times(9)).firePower(lt(256));

        // this will finish the lap
        evaluation.onLocations(createLocations(new double[]{1, 0, 0, 0, 0, 0, 0, 0}), track);
        assertThat(future.isCompleted(), is(true));
        verify(feedback, times(10)).firePower(anyInt());
    }

    private Locations createLocations(double[] ps) {
        Locations message = new Locations();
        message.setXs(xs);
        message.setYs(ys);
        message.setPs(ps);
        message.setStatus(Status.ok());
        return message;
    }


}

