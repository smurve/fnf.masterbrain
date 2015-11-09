package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.dispatch.Futures;
import akka.pattern.Patterns;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredPopulation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by mhan on 08.07.2015.
 */
class EvaluationActor<G> extends AbstractGAActor<G> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationActor.class);
    private int populationSize;
    private List<ScoredGenom<G>> scoredGenoms;
    private boolean finished = false;

    @Override
    public void onReceiveAfterInitialization(final Object o) throws Exception {
        if (o instanceof Population) {
            handlePopulation((Population<G>) o);
        } else if (o instanceof ScoredGenom) {
            handleScoredGenom((ScoredGenom<G>) o);
        } else {
            unhandled(o);
        }
    }

    private void handleScoredGenom(final ScoredGenom<G> o) {
        LOGGER.debug("ScoredGenom arrived");
        scoredGenoms.add(o);
        if (allEvaluated()) {

            ScoredPopulation<G> scoredPopulation = new ScoredPopulation<>(scoredGenoms);
            getConfiguration().getResultConsumer().tell(scoredPopulation.getBestGenom(), getSelf());
            getGaContext().getSelectionAndPairingActor().tell(scoredPopulation, getSelf());
            finished = true;
        }
    }

    private boolean allEvaluated() {
        return scoredGenoms.size() >= populationSize;
    }

    private void handlePopulation(final Population<G> population) throws InterruptedException {
        populationSize = population.size();
        scoredGenoms = new ArrayList<>(populationSize);
        for (G genom : population) {

            Future evaluate = Futures.future(() -> {
                Evaluation<G> evaluationFunction = getConfiguration().getEvaluationImpl();
                try {
                    return evaluationFunction.evaluate(genom, getConfiguration());
                } catch(Exception e) {
                    LOGGER.warn("Evaluation failed.", e);
                    throw e;
                }
            }, context().dispatcher());

            Patterns.pipe(evaluate, getContext().system().dispatcher()).to(getSelf());
        }


    }
}
