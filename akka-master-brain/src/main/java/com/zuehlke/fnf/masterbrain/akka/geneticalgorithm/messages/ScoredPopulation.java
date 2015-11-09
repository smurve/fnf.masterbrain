package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mhan on 08.07.2015.
 */
public class ScoredPopulation<T> implements Iterable<ScoredGenom<T>> {
    private final ImmutableList<ScoredGenom<T>> population;

    public ScoredPopulation(final List<ScoredGenom<T>> population) {
        this.population = ImmutableList.copyOf(population.stream().sorted(
                // sort descending
                (o1, o2) -> o1.compareTo(o2) * -1
        ).iterator());
    }

    public ImmutableList<ScoredGenom<T>> getPopulation() {
        return population;
    }


    @Override
    public Iterator<ScoredGenom<T>> iterator() {
        return population.iterator();
    }

    public int size() {
        return population.size();
    }

    public ScoredGenom<T> getBestGenom() {
        return population.get(population.size() - 1);
    }
}
