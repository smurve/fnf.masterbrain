package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Entry point for sensor events from the race track. Responsibilities
 * - enhance sensor data with current power
 * - distribute sensor data to actors of interest
 */
public class SensorDataEnricherActor extends UntypedActor {

    public static final String NAME = "SensorDataEnricher";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SensorDataEnricherActor.class);


    private final ActorRef trackBuilder;
    private final ActorRef localization;
    private ActorRef webPublisher;
    private Power currentPower;
    private DeltaCalculator deltaCalculator = new DeltaCalculator();

    public SensorDataEnricherActor(ActorRef trackBuilder, ActorRef localization) {
        this.trackBuilder = trackBuilder;
        this.localization = localization;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SensorData) {
            handleSensorData((SensorData) message);
        } else if (message instanceof ResetCommand) {
            handleReset();
        } else if (message instanceof RegisterWebPublisher) {
            webPublisher = getContext().sender();
        } else if (message instanceof Power) {
            currentPower = (Power) message;
        } else {
            unhandled(message);
        }
    }

    private void handleSensorData(SensorData data) {
        if (currentPower != null) {
            data.setForce(currentPower.getValue());
            deltaCalculator.handle(data, (newDelta) -> {
                TimeDelta msg = new TimeDelta(newDelta);
                Publisher.tell(msg, localization, getSelf()).onMissingSender().ignore().andReturn();
                Publisher.tell(msg, trackBuilder, getSelf()).onMissingSender().ignore().andReturn();
            });
            Publisher.tell(data, webPublisher, getSelf()).onMissingSender().ignore().andReturn();
            Publisher.tell(data, localization, getSelf()).onMissingSender().ignore().andReturn();
            Publisher.tell(data, trackBuilder, getSelf()).onMissingSender().ignore().andReturn();
        }
    }

    private void handleReset() {
        LOGGER.info("reset");
        currentPower = null;
        deltaCalculator = new DeltaCalculator();
    }

    static class DeltaCalculator {
        private static final Logger LOGGER = LoggerFactory.getLogger(DeltaCalculator.class);

        private Long currentDelta;
        private List<SensorData> data = new ArrayList();

        public void handle(SensorData sensorData, Consumer<Long> onNewDelta) {
            LOGGER.debug("{}", sensorData);
            //LOGGER.info("handle {}", data.size());
            if (currentDelta != null) {
                LOGGER.debug("We already have a delta.");
                return;
            }
            //if (sensorData.getDeltaToSystemtimeAtIncome() > 0) {
                data.add(sensorData);
            //} else {
            //    LOGGER.info("delta == 0");
            //}
            if (data.size() > 200) {
                double sum = 0;
                for (SensorData sensorEvent : data) {
                    sum += sensorEvent.getDeltaToSystemtimeAtIncome();
                }
                currentDelta = (long) (sum / (double)data.size());
                LOGGER.info("new delta={}", currentDelta);
                onNewDelta.accept(currentDelta);
                data.clear();
            }
        }
    }
}
