package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Context;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by mhan on 09.07.2015.
 */
public class GAActor<G> extends AbstractGAActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(GAActor.class);

    public GAActor(final Configuration<G> configuration) throws Exception {
        ActorRef evaluationActor = getContext().actorOf(Props.create(EvaluationActor.class));
        ActorRef termination = getContext().actorOf(Props.create(TerminationActor.class));
        ActorRef selectionAndPairngActor = getContext().actorOf(Props.create(SelectionAndPairingActor.class));

        Context msg = new Context(getSelf(), evaluationActor, selectionAndPairngActor, termination, configuration);

        // Don't send the Context asynchronously or it might be overtaken by the Population message. Usually we must not send messages directly
        // to onReceive() but this belongs to the constructor initialization!!!
        //getSelf().tell(msg, getSelf());
        onReceive(msg);

        evaluationActor.tell(msg, getSelf());
        termination.tell(msg, getSelf());
        selectionAndPairngActor.tell(msg, getSelf());
    }

    public static <G> Props props(final Configuration<G> config) {
        return Props.create(GAActor.class,
                () -> new GAActor<>(config));
    }

    @Override
    public void onReceiveAfterInitialization(final Object o) throws Exception {
        if (o instanceof Population) {
            getGaContext().getEvaluationActor().tell(o, getSelf());
        } else if (o instanceof State) {
            if (State.TERMINATE.equals(o)) {
                terminateAll();
            }
        } else {
            unhandled(o);
        }
    }

    private void terminateAll() {
        LOGGER.info("Terminating all GA Actors.");
        terminate(getGaContext().getEvaluationActor());
        terminate(getGaContext().getSelectionAndPairingActor());
        terminate(getGaContext().getTerminationActor());
        terminate(getGaContext().getGaActor());
    }

    private void terminate(final ActorRef actor) {
        LOGGER.info("Stopping actor={}", actor);
        Future stopped = Patterns.gracefulStop(actor, FiniteDuration.create(5, TimeUnit.SECONDS));
        stopped.andThen(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable throwable, Object o) throws Throwable {
                if (throwable == null) {
                    LOGGER.info("Stopped. {}", actor);
                } else {
                    LOGGER.error("Graceful stop of actor failed. {}. {}", actor, throwable.getMessage());
                }
            }
        }, context().dispatcher());
    }
}
