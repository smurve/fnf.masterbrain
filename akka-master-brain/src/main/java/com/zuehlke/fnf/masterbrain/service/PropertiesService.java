package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.zuehlke.fnf.masterbrain.akka.messages.PilotProperties;
import com.zuehlke.fnf.masterbrain.akka.messages.GetPilotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by tho on 15.09.2015.
 */
@Service
public class PropertiesService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesService.class);
    @Autowired
    private MasterBrainService masterBrain;

    public void setProperties(PilotProperties properties) {
        masterBrain.getPilot().tell(properties, ActorRef.noSender());
    }

    public PilotProperties getPilotProperties() {

        Timeout timeout = new Timeout(Duration.create(1, TimeUnit.SECONDS));
        LOGGER.info("Asking Pilot for current properties.");
        Future<Object> future = Patterns.ask(masterBrain.getPilot(), new GetPilotProperties(), timeout);
        LOGGER.info("Future received. Waiting for answer");
        PilotProperties result;
        try {
            result = (PilotProperties) Await.result(future, timeout.duration());
        } catch (Exception e) {
            LOGGER.warn("Did not receive any result on time.");
            return null;
        }
        LOGGER.info("Answer received.");
        return result;
    }
}
