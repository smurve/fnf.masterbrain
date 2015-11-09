package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.numberguess;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.PairingAndMutation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * Created by tho on 13.08.2015.
 */
public class NumberGuessingPairing implements PairingAndMutation<Integer> {
    private Random random = new Random(1);

    @Override
    public Collection<Integer> pairAndMutate(Integer adam, Integer eva, Configuration configuration) {
        int childA = adam;
        int childB = eva;
        if (random.nextBoolean()) {
            childA = (adam + eva) / 2;
        }
        if (random.nextBoolean()) {
            childA = childA + random.nextInt(4) - 2;
        }
        if (random.nextBoolean()) {
            childB = childB + random.nextInt(4) - 2;
        }

        return Arrays.asList(cap(childA), cap(childB));
    }

    private int cap(int x) {
        return Math.max(Math.min(100, x), 0);
    }
}
