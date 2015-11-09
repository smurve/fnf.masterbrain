package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackPoints;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 13.08.2015.
 */
public class PopulationSamplerTest {

    @Rule
    public AkkaRule akka = AkkaRule.createWithConfig("masterbrain{" +
            " pilot.tot {" +
            "  populationSize=1000," +
            "  cache=false," +
            "  w0range=[140,170]," +
            "  w1range=[0.2,0.4]," +
            "  w2min=-0.003," +
            "  w3min=0.01," +
            " }" +
            "}");

    @Test
    public void testCreatePopulation() {
        PopulationSampler sampler = new PopulationSampler(new TOTConfig(akka.getConfig()));
        Track track = new Track();
        TrackPoints trackPoints = new TrackPoints();
        trackPoints.setGs(new double[]{-1, -2, -3, -4, -5, 5, 4, 3, 2, 1});
        track.setTrackpoints(trackPoints);
        Population<TOTGenom> population = sampler.createPopulation(track);

        assertThat(population.size(), is(1000));
        population.getPopulation().stream().forEach(
                (genom) -> {
                    // These asserts depend directly on the values used in the PopulationSampler
                    // when calling GRange.generateWi() for the single genoms

                    // w0, the constant
                    assertThat(genom.getValues()[0], is(lessThanOrEqualTo(170d)));
                    assertThat(genom.getValues()[0], is(greaterThanOrEqualTo(140d)));

                    // w1-w4, the w for the gs. Yo man!
                    assertThat(genom.getValues()[1], is(lessThanOrEqualTo(0.4)));
                    assertThat(genom.getValues()[1], is(greaterThanOrEqualTo(0.2)));
                    assertThat(genom.getValues()[2], is(lessThanOrEqualTo(5d)));
                    assertThat(genom.getValues()[2], is(greaterThanOrEqualTo(-5d)));
                    assertThat(genom.getValues()[3], is(lessThanOrEqualTo(5d)));
                    assertThat(genom.getValues()[3], is(greaterThanOrEqualTo(-5d)));
                }
        );
    }
}
