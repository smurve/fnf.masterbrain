package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import com.zuehlke.fnf.masterbrain.akka.messages.GetPilot;
import com.zuehlke.fnf.masterbrain.akka.messages.Pilot;
import com.zuehlke.fnf.masterbrain.akka.messages.PilotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by tho on 23.07.2015.
 */
@Service
public class PilotRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PilotRegistry.class);

    @Autowired
    private MasterBrainService masterBrainService;

    private Map<String, PilotConfig> pilots = new HashMap<>();

    public void registerPilotActor(PilotConfig pilotConfig) {
        LOGGER.info("Registering new pilot={}", pilotConfig);
        pilots.put(pilotConfig.getName(), pilotConfig);
    }

    public List<PilotConfig> getPilots() {
        return new ArrayList<>(pilots.values());
    }

    public void activatePilot(String pilotName) throws ClassNotFoundException {
        PilotConfig pilotConfig = pilots.get(pilotName);
        if (pilotConfig == null) {
            throw new IllegalArgumentException("Unknown pilot");
        }

        PilotConfig current = getCurrenPilot();
        if (current != null && current.getActorClass().equals(pilotConfig.getActorClass())) {
            LOGGER.info("Pilot {} is already loaded.", pilotName);
            return;
        }

        Class actorClass = Class.forName(pilotConfig.getActorClass());
        ActorRef actor = masterBrainService.actorOf(Props.create(actorClass), actorClass.getSimpleName());
        Pilot pilot = Pilot.of(actor);
        pilot.setConfig(pilotConfig);
        masterBrainService.getPilot().tell(pilot, ActorRef.noSender());
    }

    public PilotConfig getCurrenPilot() {
        akka.util.Timeout timeout = new akka.util.Timeout(Duration.create(500, TimeUnit.MILLISECONDS));
        Future<Object> future = Patterns.ask(masterBrainService.getPilot(), new GetPilot(), timeout);
        LOGGER.info("Future received. Waiting for answer");
        PilotConfig result;
        try {
            Pilot pilot = (Pilot) Await.result(future, timeout.duration());
            result = pilot.getConfig();
        } catch (Exception e) {
            LOGGER.warn("Did not receive any result on time.");
            return null;
        }
        LOGGER.info("Answer received.");
        return result;
    }
}
