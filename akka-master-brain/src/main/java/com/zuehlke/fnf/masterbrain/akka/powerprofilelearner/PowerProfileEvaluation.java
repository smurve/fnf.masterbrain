package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import akka.dispatch.Futures;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import com.zuehlke.fnf.masterbrain.simulator.Simulator;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import scala.concurrent.Future;

/**
 * Created by mhan on 10.07.2015.
 */
public class PowerProfileEvaluation implements Evaluation<PowerProfileGenom> {
    @Override
    public Future<ScoredGenom<PowerProfileGenom>> evaluate(final PowerProfileGenom element, final Configuration configuration) {
        return Futures.future(() -> {
            Simulator simulator = (Simulator) configuration.getCustomProperties().get("simulator");
            double score = simulator.simulate(element.getPowerSettings()).getTrackTime();
            return new ScoredGenom<>(element, score);
        }, configuration.getExecutionContextExecutor());

    }
}
