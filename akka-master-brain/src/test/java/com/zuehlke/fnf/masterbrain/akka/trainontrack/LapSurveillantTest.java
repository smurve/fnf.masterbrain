package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.utils.LapSurveillant;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 14.08.2015.
 */
public class LapSurveillantTest {

    @Test
    public void testWithoutOffset() {
        LapSurveillant lapSurveillant = new LapSurveillant(20, 0, 10);

        for (int i = 0; i < 20; i++) {
            assertThat(lapSurveillant.isLapFinished(i), is(false));
        }
        assertThat(lapSurveillant.isLapFinished(0), is(true));
    }

    @Test
    public void testWithOffset() {
        LapSurveillant lapSurveillant = new LapSurveillant(20, 5, 10);

        for (int i = 5; i < 25; i++) {
            assertThat(lapSurveillant.isLapFinished((i % 25)), is(false));
        }
        assertThat(lapSurveillant.isLapFinished(5), is(true));
    }

    @Test
    public void testWithMassiveJump() {
        LapSurveillant lapSurveillant = new LapSurveillant(20, 0, 10);

        for (int i = 2; i < 10; i++) {
            assertThat(lapSurveillant.isLapFinished(i), is(false));
        }
        assertThat(lapSurveillant.isLapFinished(0), is(false));
        assertThat(lapSurveillant.isLapFinished(19), is(false));
        assertThat(lapSurveillant.isLapFinished(0), is(true));
    }
}
