package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.zuehlke.fnf.masterbrain.akka.messages.GetPower;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

@Service
public class PowerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerService.class);
    private MasterBrainService masterBrain;

    @Autowired
    public PowerService(MasterBrainService masterBrain) {
        this.masterBrain = masterBrain;
    }

    public Power getCurrentPower() {
        Timeout timeout = new Timeout(Duration.create(1, TimeUnit.SECONDS));
        LOGGER.info("Asking Pilot for current power.");
        Future<Object> future = Patterns.ask(masterBrain.getPilot(), new GetPower(), timeout);
        LOGGER.info("Future received. Waiting for answer");
        Power result;
        try {
            result = (Power) Await.result(future, timeout.duration());
        } catch (Exception e) {
            LOGGER.warn("Did not receive any result on time.");
            return null;
        }
        LOGGER.info("Answer received.");
        return result;
    }

    public void setPower(int power) {
        masterBrain.getPilot().tell(Power.of(power), ActorRef.noSender());
    }

}
