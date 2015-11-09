package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.PairingAndMutation;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by tho on 10.08.2015.
 */
public class TOTPairingAndMutation implements PairingAndMutation<TOTGenom> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TOTPairingAndMutation.class);
    private static final Random RANDOM = new Random();
    private TOTConfig config;

    @Override
    public Collection<TOTGenom> pairAndMutate(TOTGenom adam, TOTGenom eva, Configuration configuration) {
        config = (TOTConfig) configuration.getCustomProperties().get(TOTPilotActor.KEY_CONFIG);
        int populationSize = configuration.getPopulationSize();
        double[] childA = new double[adam.getValues().length];
        double[] childB = new double[adam.getValues().length];
        for (int i = 0; i < adam.getValues().length; i++) {
            if (RANDOM.nextBoolean()) {
                childA[i] = eva.getValues()[i];
                childB[i] = adam.getValues()[i];
            } else {
                childA[i] = adam.getValues()[i];
                childB[i] = eva.getValues()[i];
            }
        }
        double probability = 1d / populationSize;


        Track track = (Track) configuration.getCustomProperties().get(TOTPilotActor.KEY_TRACK);
        double[] wNRange = GRange.calcualteW2ToWnRange(track, 15);


        return Arrays.asList(mutate(childA, probability, wNRange), mutate(childB, probability, wNRange));
    }

    private TOTGenom mutate(double[] toMutate, double probability, double[] wNRange) {

        // additive mutation
        if (RANDOM.nextBoolean()) { //nextDouble() > (1 - probability)) {
            LOGGER.info("Mutating w0");
            List<Double> values = config.getW0mutationRange();
            GRange.generateWi(toMutate, 0, toMutate[0], values.get(0), values.get(1));
        }
        
        if (RANDOM.nextBoolean()) { //nextDouble() > (1 - probability)) {
            LOGGER.info("Mutating w1");
            List<Double> values = config.getW1mutationRange();
            GRange.generateWi(toMutate, 1, toMutate[1], values.get(0), values.get(1));
        }

        for (int i = 2; i < toMutate.length; i++) {
            if (RANDOM.nextBoolean()) { //nextDouble() > (1 - probability)) {
                LOGGER.info("Mutating w{}", i);
                GRange.generateWi(toMutate, i, toMutate[i], wNRange[0], wNRange[1]);
            }
        }

        return new TOTGenom(toMutate);
    }
}
