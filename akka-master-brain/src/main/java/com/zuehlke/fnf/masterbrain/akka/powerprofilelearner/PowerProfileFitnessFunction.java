package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.FitnessFunction;

/**
 * Created by tho on 14.08.2015.
 */
public class PowerProfileFitnessFunction implements FitnessFunction<PowerProfileGenom> {

    // use default impl.


    // This is the old fitness function by marcel that was originally located in SelectionAndPairingActor
//    @Override
//    public List<FitnessValued<PowerProfileGenom>> computeFitnessValues(ScoredPopulation<PowerProfileGenom> population) {
//        double sumOfScores = sumScores(population);
//        return population.getPopulation().stream().map(sg -> new FitnessValued<>(sg, sg.getScore()/sumOfScores)).collect(Collectors.toList());
//    }
//
//    private double sumScores(final ScoredPopulation<PowerProfileGenom> o) {
//        return o.getPopulation().stream().mapToDouble(ScoredGenom::getScore).sum();
//    }
}
