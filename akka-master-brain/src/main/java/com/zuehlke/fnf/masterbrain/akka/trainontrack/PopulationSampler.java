package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by tho on 11.08.2015.
 */
public class PopulationSampler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PopulationSampler.class);
    private Random random = new Random();

    private PopulationDao dao;
    private Population population;
    private TOTConfig config;

    public PopulationSampler(TOTConfig config) {
        this.config = config;
        dao = new PopulationDao();
        dao.setEnabled(config.isUseCache());
    }

    public Population<TOTGenom> createPopulation(Track track) {
        if (population != null) {
            return population;
        }
        population = dao.readExistingPopulation();
        if (population == null) {
            population = createNewPopulation(track);
        }
        dao.storePopulation(population);
        return population;
    }


    private Population<TOTGenom> createNewPopulation(Track track) {
        LOGGER.info("creating population of {} for {} variables", config.getPopulationSize(), 3);
        LOGGER.info("w0range={}. w1range={}, w2init={}, w3init={}", config.getW0range(), config.getW1range(), config.getW2init(), config.getW3init());

        double[] wNRange = GRange.calcualteW2ToWnRange(track, 20);
        double minWn = wNRange[0];
        double maxWn = wNRange[1];

        Set<TOTGenom> genoms = new HashSet<>();
        while (genoms.size() < config.getPopulationSize()) {
            double[] values = new double[4];

            GRange.generateWi(values, 0, config.getW0range().get(0), config.getW0range().get(1));
            GRange.generateWi(values, 1, config.getW1range().get(0), config.getW1range().get(1));
            GRange.generateWi(values, 2, config.getW2init(), minWn, maxWn);
            GRange.generateWi(values, 3, config.getW3init(), minWn, maxWn);

            genoms.add(new TOTGenom(values));
        }
        Population<TOTGenom> pop = new Population<>(new ArrayList<>(genoms));
        return pop;
    }


}
