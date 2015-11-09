package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import com.google.common.collect.ImmutableList;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.PairingAndMutation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by mhan on 10.07.2015.
 */
public class PowerProfilePairingAndMutation implements PairingAndMutation<PowerProfileGenom> {
    private static final Random RANDOM = new Random();

    @Override
    public Collection<PowerProfileGenom> pairAndMutate(final PowerProfileGenom adam, final PowerProfileGenom eva, final Configuration configuration) {
        // Uniform Crossover

        List<Double> childA = new ArrayList<>();
        List<Double> childB = new ArrayList<>();

        int populationSize = adam.getPowerSettings().size();

        for (int i = 0; i < adam.getPowerSettings().size(); i++) {
            Double adamValue = adam.getPowerSettings().get(i);
            Double evaValue = eva.getPowerSettings().get(i);

            if (RANDOM.nextBoolean()) {
                Double backup = adamValue;
                adamValue = evaValue;
                evaValue = backup;
            }
            childA.add(adamValue);
            childB.add(evaValue);
        }


        double probability = 1d / populationSize;
        return ImmutableList.of(mutate(childA, probability), mutate(childB, probability));
    }

    private PowerProfileGenom mutate(final List<Double> toMutate, double properbility) {

        for (int i = 0; i < toMutate.size(); i++) {
            if (RANDOM.nextDouble() > (1 - properbility)) {
                toMutate.set(i, RANDOM.nextDouble() * LearnerActor.MAX_POWER);
            }
        }

        return new PowerProfileGenom(toMutate);
    }
}
