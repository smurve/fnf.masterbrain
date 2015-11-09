package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.actor.PoisonPill;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.State;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Context;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Termination;
import scala.concurrent.Future;

/**
 * Created by mhan on 09.07.2015.
 */
class TerminationActor<G> extends AbstractGAActor<G> {
    private Termination<G> evaluationFunction;
    private Population<G> population;

    @Override
    public void onReceiveAfterInitialization(final Object o) throws Exception {
        if (o instanceof Population) {
            handlePopulation((Population<G>) o);
        } else if (o instanceof State) {
            handleState((State) o);
        } else if (o instanceof PoisonPill) {

        } else {
            unhandled(o);
        }
    }

    @Override
    protected void onInitializationMessage(final Context context) throws Exception {
        evaluationFunction = TypedActor.get(getContext().system()).typedActorOf(
                new TypedProps<Termination>(Termination.class, ((Class<Termination>) getConfiguration().getTerminationImpl())));
    }

    private void handleState(final State o) {
        switch (o) {
            case CONTINUE:
                getGaContext().getEvaluationActor().tell(population, getSelf());
                break;
            case TERMINATE:
                getConfiguration().getResultConsumer().tell(population, getSelf());
                getGaContext().getGaActor().tell(o, getSelf());
        }
    }

    private void handlePopulation(final Population<G> population) {
        this.population = population;
        Future pair = Futures.future(() -> {
            return evaluationFunction.isFinished(population, getConfiguration()) ? State.TERMINATE : State.CONTINUE;
        }, context().dispatcher());
        Patterns.pipe(pair, getContext().system().dispatcher()).to(getSelf());
    }
}
