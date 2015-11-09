package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.actor.Status;
import akka.actor.UntypedActor;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mhan on 17.07.2015.
 */
public abstract class AbstractGAActor<G> extends UntypedActor {
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private Context gaContext;

    @Override
    public final void onReceive(final Object o) throws Exception {
        //LOGGER.info("dispatcher: {}", context().dispatcher());
        if (o instanceof Context) {
            this.gaContext = (Context) o;
            onInitializationMessage(this.gaContext);
        } else {
            if (gaContext == null) {
                LOGGER.error("Not initialized");
                getSender().tell(new Status.Failure(new IllegalStateException("Not initialized")), getSelf());
                return;
            }
            onReceiveAfterInitialization(o);
        }
    }

    protected void onInitializationMessage(Context context) throws Exception {

    }

    protected abstract void onReceiveAfterInitialization(final Object o) throws Exception;

    protected Context getGaContext() {
        return gaContext;
    }

    protected Configuration<G> getConfiguration() {
        return getGaContext().getConfiguration();
    }
}
