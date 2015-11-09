package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredPopulation;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tho on 14.08.2015.
 */
public interface FitnessFunction<G> extends Serializable {
    default List<FitnessValued<G>> computeFitnessValues(final ScoredPopulation<G> population) {
        double bestValue = population.getBestGenom().getScore();
        List<FitnessValued<G>> fitnessValues = population.getPopulation().stream().map(scoredGenom -> new FitnessValued<>(scoredGenom, (1d / (scoredGenom.getScore() - 0.97 * bestValue)))).collect(Collectors.toList());
        double sum = fitnessValues.stream().mapToDouble((fv) -> fv.getFitnessValue()).sum();
        List<FitnessValued<G>> result = fitnessValues.stream().map((fv) -> new FitnessValued<G>(fv.getScoredGenom(), fv.getFitnessValue() / sum)).collect(Collectors.toList());
        return result;
    }

}
