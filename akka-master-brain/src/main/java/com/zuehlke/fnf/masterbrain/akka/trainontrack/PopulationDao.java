package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created by tho on 13.08.2015.
 */
public class PopulationDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(PopulationDao.class);

    private File dataDir = new File("data/tot");
    private File populationFile = new File(dataDir, "population.json");
    private boolean enabled = true;

    public Population readExistingPopulation() {
        if (enabled && populationFile.exists()) {
            LOGGER.info("reading population from file. {}", populationFile);
            try (InputStream in = new FileInputStream(populationFile)) {

                List<TOTGenom> genoms = new ObjectMapper().readValue(in, new TypeReference<List<TOTGenom>>() {
                });
                return new Population(genoms);
            } catch (Exception e) {
                LOGGER.error("Population import failed. {}", e.getMessage());
            }
        }
        return null;
    }

    public void storePopulation(Population<TOTGenom> population) {
        if (enabled) {
            dataDir.mkdirs();
            LOGGER.info("writing population to file. {}", populationFile);
            try (OutputStream out = new FileOutputStream(populationFile)) {
                new ObjectMapper().writeValue(out, population.getPopulation());
            } catch (Exception e) {
                LOGGER.error("Population export failed. {}", e.getMessage());
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
