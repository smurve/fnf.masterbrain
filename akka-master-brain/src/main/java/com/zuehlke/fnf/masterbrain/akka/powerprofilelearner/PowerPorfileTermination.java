package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Termination;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mhan on 10.07.2015.
 */
public class PowerPorfileTermination implements Termination<PowerProfileGenom> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerPorfileTermination.class);
    private int currentIteration = 0;

    @Override
    public boolean isFinished(final Population<PowerProfileGenom> population, Configuration config) {
        Integer maxIterations = (Integer) config.getCustomProperties().get(LearnerActor.KEY_MAX_ITERATIONS);
        LOGGER.trace("{}/{} iterations in GA finished", currentIteration, maxIterations);
        return currentIteration++ >= maxIterations;
    }

//    private boolean isCanceled(final Configuration config) {
//        Future<Object> ask = Patterns.ask((ActorRef) config.getCustomProperties().get(LearnerActor.KEY_LEARNER_REF), getIsCanceled(), Timeout.apply
//                (200, TimeUnit.MILLISECONDS));
//
//        try {
//            Boolean isCanceled = (Boolean) ask.result(Duration.apply(8000, TimeUnit.MILLISECONDS), null);
//            if (isCanceled) {
//                return true;
//            }
//        } catch (Exception e) {
//            LOGGER.warn("Error while asking for cancelation", e);
//            return true;
//        }
//        return false;
//    }
}
