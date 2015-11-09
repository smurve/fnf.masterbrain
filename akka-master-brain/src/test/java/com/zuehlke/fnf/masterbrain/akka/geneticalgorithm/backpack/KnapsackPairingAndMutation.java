package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack;

import com.google.common.collect.ImmutableList;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.PairingAndMutation;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collection;
import java.util.Random;

import static com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack.Functions.toBiginteger;

/**
 * Created by mhan on 09.07.2015.
 */
public class KnapsackPairingAndMutation implements PairingAndMutation<BigInteger> {
    private static final Random RANDOM = new Random();

    @Override
    public Collection<BigInteger> pairAndMutate(final BigInteger adam, final BigInteger eva, Configuration config) {
        // Uniform Crossover

        BitSet childA = new BitSet();
        BitSet childB = new BitSet();


        for (Item item : Item.values()) {
            boolean adamSet = adam.testBit(item.ordinal());
            boolean evaSet = eva.testBit(item.ordinal());
            if (adamSet ^ evaSet && RANDOM.nextBoolean()) {
                adamSet = !adamSet;
                evaSet = !evaSet;
            }

            childA.set(item.ordinal(), adamSet);
            childB.set(item.ordinal(), evaSet);
        }

        double properbility = 1d / Item.values().length;
        return ImmutableList.of(toBiginteger.apply(mutate(childA, properbility)), toBiginteger.apply(mutate(childB, properbility)));
    }

    private BitSet mutate(final BitSet toMutate, double properbility) {
        for (Item item : Item.values()) {
            if (RANDOM.nextDouble() > (1 - properbility)) {
                toMutate.flip(item.ordinal());
            }
        }
        return toMutate;
    }
}
