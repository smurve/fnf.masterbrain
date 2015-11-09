package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.EvictingQueue;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tho on 05.08.2015.
 */
public class LapTimeActor extends UntypedActor {

    public static final String NAME = "LapTime";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LapTimeActor.class);

    private ActorRef webPublisher;

    private EvictingQueue<LapTime> buffer = EvictingQueue.create(500);
    private LapTime last;

    @Override
    public void onReceive(Object message) throws Exception {
        LOGGER.debug("Message={}", message);
        if (message instanceof RoundTimeMessage) {
            handleRoundPassedMessage((RoundTimeMessage) message);
        } else if (message instanceof GetLapTimes) {
            handleGetLapTimes();
        } else if (message instanceof RegisterWebPublisher) {
            handleRegisterWebPublisher();
        } else if (message instanceof ResetCommand) {
            handleReset();
        }
    }

    private void handleGetLapTimes() {
        context().sender().tell(LapTimes.from(buffer), getSelf());
    }

    private void handleReset() {
        buffer.clear();
        last = null;
    }

    private void handleRoundPassedMessage(RoundTimeMessage message) {
        LapTime current = LapTime.from(last, message);
        buffer.add(current);
        last = current;
        if (current.getDuration() > 0) {
            if (webPublisher != null) {
                webPublisher.tell(current, getSelf());
            }
        }
    }

    private void handleRegisterWebPublisher() {
        LOGGER.info("Got a new WebPublisher");
        webPublisher = getContext().sender();
    }
}
