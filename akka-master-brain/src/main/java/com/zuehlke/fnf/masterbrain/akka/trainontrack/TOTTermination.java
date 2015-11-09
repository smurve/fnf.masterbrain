package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Termination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tho on 10.08.2015.
 */
public class TOTTermination implements Termination<TOTGenom> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TOTTermination.class);
    private PopulationDao dao = new PopulationDao();

    @Override
    public boolean isFinished(Population<TOTGenom> populationEvaluation, Configuration configuration) {
        boolean useCache = (Boolean) configuration.getCustomProperties().get(TOTPilotActor.KEY_USE_CACHE);
        dao.setEnabled(useCache);
        dao.storePopulation(populationEvaluation);
        LOGGER.info("isFinished");
        return false;
    }
}
