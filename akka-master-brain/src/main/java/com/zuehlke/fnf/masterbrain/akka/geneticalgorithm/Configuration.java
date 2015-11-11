package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.actor.ActorRef;
import com.google.common.base.Throwables;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.FitnessFunction;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.PairingAndMutation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Termination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContextExecutor;

import java.util.Map;

/**
 * Created by mhan on 08.07.2015.
 */
public class Configuration<G> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private final int populationSize;
    private final Class<? extends PairingAndMutation<G>> pairingImpl;
    private final Class<? extends Termination<G>> terminationImpl;
    private final Class<? extends Evaluation<G>> evaluationImpl;
    private final Class<? extends FitnessFunction<G>> fitnessFunctionImpl;

    private final Map<Object, Object> customProperties;
    private final ActorRef resultConsumer;
    private ExecutionContextExecutor executionContextExecutor;

    public Configuration(final int populationSize, final Class<? extends Evaluation<G>> evaluation, final Class<? extends FitnessFunction<G>> fitnessFunction, final Class<? extends PairingAndMutation<G>> pairing, final
    Class<? extends Termination<G>> termination, Map<Object, Object> customProperties, final ActorRef resultConsumer, ExecutionContextExecutor executionContextExecutor) {
        this.evaluationImpl = evaluation;
        this.pairingImpl = pairing;
        this.fitnessFunctionImpl = fitnessFunction;
        this.populationSize = populationSize;
        this.terminationImpl = termination;
        this.customProperties = customProperties;
        this.resultConsumer = resultConsumer;
        this.executionContextExecutor = executionContextExecutor;
    }

    public ExecutionContextExecutor getExecutionContextExecutor() {
        return executionContextExecutor;
    }

    public Evaluation<G> getEvaluationImpl() {
        return createInstanceOf(evaluationImpl);
    }

    public PairingAndMutation<G> getPairingImpl() {
        return createInstanceOf(pairingImpl);
    }

    public FitnessFunction<G> getFitnessFunctionImpl() {
        return createInstanceOf(fitnessFunctionImpl);
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public Class<? extends Termination> getTerminationImpl() {
        return terminationImpl;
    }

    public Map<Object, Object> getCustomProperties() {
        return customProperties;
    }


    private <T> T createInstanceOf(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            LOGGER.error("error creating instanceof: {}", clazz);
            throw Throwables.propagate(e);
        }
    }

    public ActorRef getResultConsumer() {
        return resultConsumer;
    }
}
