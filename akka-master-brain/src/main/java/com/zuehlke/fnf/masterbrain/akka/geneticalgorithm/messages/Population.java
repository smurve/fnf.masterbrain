package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.List;

/**
 * Created by mhan on 08.07.2015.
 */
public class Population<T> implements Iterable<T> {
    private ImmutableList<T> population;

    public Population() {

    }
    public Population(final List<T> population) {
        this.population = ImmutableList.copyOf(population);
    }

    public ImmutableList<T> getPopulation() {
        return population;
    }


    @Override
    public Iterator<T> iterator() {
        return population.iterator();
    }

    public int size() {
        return population.size();
    }

    @Override
    public String toString() {
        return "Population{" +
                "population=" + population +
                '}';
    }
}
