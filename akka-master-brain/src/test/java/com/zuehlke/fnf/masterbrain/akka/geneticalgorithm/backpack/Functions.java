package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.function.Function;

/**
 * Created by mhan on 09.07.2015.
 */
public class Functions {
    public static Function<BitSet, BigInteger> toBiginteger = new Function<BitSet, BigInteger>() {
        @Override
        public BigInteger apply(final BitSet bitSet) {
            long bitInteger = 0;
            for (int i = 0; i < 32; i++) {
                if (bitSet.get(i)) {
                    bitInteger |= (1 << i);
                }
            }
            return BigInteger.valueOf(bitInteger);
        }
    };
}
