package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages;

import akka.actor.ActorRef;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;

/**
 * Created by mhan on 17.07.2015.
 */
public class Context {
    private final ActorRef gaActor;
    private final ActorRef evaluationActor;
    private final ActorRef terminationActor;
    private final ActorRef selectionAndPairingActor;
    private final Configuration configuration;

    public Context(final ActorRef gaActor, final ActorRef evaluationActor, final ActorRef selectionAndPairingActor, final ActorRef terminationActor, final
    Configuration
            configuration) {
        this.gaActor = gaActor;
        this.evaluationActor = evaluationActor;
        this.terminationActor = terminationActor;
        this.selectionAndPairingActor = selectionAndPairingActor;
        this.configuration = configuration;
    }

    public ActorRef getGaActor() {
        return gaActor;
    }

    public ActorRef getEvaluationActor() {
        return evaluationActor;
    }

    public ActorRef getTerminationActor() {
        return terminationActor;
    }

    public ActorRef getSelectionAndPairingActor() {
        return selectionAndPairingActor;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
