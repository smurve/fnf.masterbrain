package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import com.zuehlke.fnf.masterbrain.simulator.Simulator;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;

/**
 * Created by mhan on 10.07.2015.
 */
public class PowerProfileEvaluation implements Evaluation<PowerProfileGenom> {
    @Override
    public ScoredGenom<PowerProfileGenom> evaluate(final PowerProfileGenom element, final Configuration configuration) {

        Simulator simulator = (Simulator) configuration.getCustomProperties().get("simulator");
        double score = simulator.simulate(element.getPowerSettings()).getTrackTime();
        return new ScoredGenom<>(element, score);
    }
}
