package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.dispatch.Futures;
import akka.pattern.Patterns;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredPopulation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.FitnessFunction;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.PairingAndMutation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.FitnessValued;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import java.util.*;

/**
 * Created by mhan on 08.07.2015.
 */
class SelectionAndPairingActor<G> extends AbstractGAActor<G> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelectionAndPairingActor.class);
    private static final Random RANDOM = new Random();
    private int populationSize;
    private List<G> nextGeneration;

    public SelectionAndPairingActor() {
    }

    @Override
    public void onReceiveAfterInitialization(final Object o) throws Exception {
        if (o instanceof ScoredPopulation) {
            handleScoredPopulation((ScoredPopulation<G>) o);
        } else if (o instanceof Collection) {
            handleNewSibbling((Collection<G>) o);
        } else {
            unhandled(o);
        }
    }

    private void handleNewSibbling(final Collection<G> sibbling) {
        nextGeneration.addAll(sibbling);
        if (nextGenerationComplete()) {
            getGaContext().getTerminationActor().tell(new Population<>(nextGeneration), getSelf());
        }
    }

    private boolean nextGenerationComplete() {
        LOGGER.debug("Current nextGeneration size={}", nextGeneration.size());
        return nextGeneration.size() >= populationSize;
    }

    private void handleScoredPopulation(final ScoredPopulation<G> population) {
        populationSize = getConfiguration().getPopulationSize();
        FitnessFunction<G> fitnessFunction = getConfiguration().getFitnessFunctionImpl();

        List<FitnessValued<G>> fitnessValues = fitnessFunction.computeFitnessValues(population);
        Collections.sort(fitnessValues, (o1, o2) -> o2.compareTo(o1));

        PairingAndMutation<G> evaluationFunction = getConfiguration().getPairingImpl();

        Set<Double> scoresUsedForPairing = new HashSet<>();
        nextGeneration = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize / 2; i++) {

            ScoredGenom<G> getNextGuy =  getNextGuy(fitnessValues);
            scoresUsedForPairing.add(getNextGuy.getScore());
            ScoredGenom<G> getNextGuy2 = getNextGuy(fitnessValues);
            scoresUsedForPairing.add(getNextGuy2.getScore());

            Future pair = Futures.future(() -> {
                try {
                    return evaluationFunction.pairAndMutate(getNextGuy.getGenom(), getNextGuy2.getGenom(), getConfiguration());
                } catch(Exception e) {
                    LOGGER.error("pair failed.", e);
                    return null;
                }
            }, context().dispatcher());
            Patterns.pipe(pair, getContext().system().dispatcher()).to(getSelf());
        }
        LOGGER.debug("Scores used for pairing: {}", scoresUsedForPairing);
    }

    private ScoredGenom<G> getNextGuy(final List<FitnessValued<G>> fitnessValues) {
        double random = RANDOM.nextDouble();
        double sum = 0;
        for (FitnessValued<G> fv : fitnessValues) {
            sum += fv.getFitnessValue();
            if (sum > random) {
                return fv.getScoredGenom();
            }
        }
        LOGGER.info("No fitness value found that has cumsum > {}", random);
        return fitnessValues.get(fitnessValues.size()-1).getScoredGenom();

// tho: don't understand this impl
//        int i = Collections.binarySearch(fitnessValues, new FitnessValued<>(random));
//        if (i >= 0) {
//            return fitnessValues.get(i).getScoredGenom();
//        } else {
//            int realIndex = -i;
//            return realIndex >= fitnessValues.size() ? fitnessValues.get(fitnessValues.size() - 1).getScoredGenom() : fitnessValues.get(realIndex).getScoredGenom();
//        }
    }



}
