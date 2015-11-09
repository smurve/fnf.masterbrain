package com.zuehlke.fnf.masterbrain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tho on 09.07.2015.
 */
@Component
public class WebSocketPublisherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketPublisherService.class);
    private static final String CFG_PREFIX = "${masterbrain.webSocketPublisher";
    private static final String TOPIC_SENSOR_EVENT = "/topic/sensorEvents";
    private static final String TOPIC_CONTROL = "/topic/control";
    private static final String TOPIC_TRACK = "/topic/track";
    private static final String TOPIC_LOCATION = "/topic/location";
    private static final String TOPIC_LEARNING_FINISHED = "/topic/learning/finished";
    private static final String TOPIC_RESET = "/topic/reset";
    private static final String TOPIC_TRACK_BUILDING_STATE = "/topic/track/state";
    private static final String TOPIC_LAP_TIME = "/topic/laptime";
    private static final String TOPIC_INFO = "/topic/info";
    private static final String TOPIC_PILOT_PROPERTIES = "/topic/properties/pilot";

    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    private MasterBrainService masterBrain;
    @Value(CFG_PREFIX + ".locationProbabilityThreshold}")
    private double locationProbabilityThreshold;
    private DataTrottle sensorDataTrottle = new DataTrottle(100, TimeUnit.MILLISECONDS);
    private DataTrottle trackBuildingStateTrottle = new DataTrottle(2, TimeUnit.SECONDS);
    private DataTrottle locationsTrottle = new DataTrottle(100, TimeUnit.MILLISECONDS);
    private DataTrottle powerTrottle = new DataTrottle(100, TimeUnit.MILLISECONDS);

    @PostConstruct
    public void init() {
        masterBrain.registerInboxForWebPublisher((message) -> {
            LOGGER.debug("Got Message form Inbox: {}", message);
            sendMessage(message);
        });
    }

    private void sendMessage(Object message) throws Exception {
        try {
            if (message instanceof SensorData) {
                if (sensorDataTrottle.needsUpdate()) {
                    LOGGER.debug("Publishing: {}", message);
                    template.convertAndSend(TOPIC_SENSOR_EVENT, message);
                }
            } else if (message instanceof Power) {
                handlePower(message);
            } else if (message instanceof Track) {
                LOGGER.debug("Publishing Track: {}", TrackMapper.toJSON((Track) message));
                template.convertAndSend(TOPIC_TRACK, message);
            } else if (message instanceof Locations) {
                handleLocations((Locations) message);
            } else if (message instanceof ResetCommand) {
                template.convertAndSend(TOPIC_RESET, "reset");
            } else if (message instanceof TrackWithPowerProfile) {
                template.convertAndSend(TOPIC_LEARNING_FINISHED, message);
            } else if (message instanceof TrackBuildingState) {
                if (((TrackBuildingState) message).isDone() || trackBuildingStateTrottle.needsUpdate()) {
                    template.convertAndSend(TOPIC_TRACK_BUILDING_STATE, message);
                }
            } else if (message instanceof LapTime) {
                template.convertAndSend(TOPIC_LAP_TIME, message);
            } else if (message instanceof Info) {
                template.convertAndSend(TOPIC_INFO, message);
            } else if (message instanceof PilotProperties) {
                template.convertAndSend(TOPIC_PILOT_PROPERTIES, message);
            }
        } catch (Exception e) {
            LOGGER.warn("Sending STOMP over web socket failed.", e);
        }
    }

    private void handlePower(Object message) {
        if (powerTrottle.needsUpdate()) {
            LOGGER.debug("Publishing: {}", message);
            template.convertAndSend(TOPIC_CONTROL, message);
        }
    }

    private void handleLocations(Locations message) throws JsonProcessingException {
        if (locationsTrottle.needsUpdate()) {
            //LOGGER.info("Publishing Locations: {}", LocationsMapper.toJSON(message));
            double[] xs = message.getXs();
            double[] ys = message.getYs();
            double[] ps = message.getPs();

            List<Loc> locs = new ArrayList<>();
            if (message.getStatus().isOk()) {
                for (int i = 0; i < xs.length; i++) {
                    if (ps[i] > locationProbabilityThreshold) {
                        locs.add(Loc.from(xs[i], ys[i], ps[i], i));
                    }
                }
            }
            //LOGGER.info("publishing {} locations.", locs.size());
            template.convertAndSend(TOPIC_LOCATION, new LocationUpdate(locs, message.getStatus()));
            template.convertAndSend(TOPIC_LOCATION, LocIndex.from(message));
        }
    }

    /**
     * A Trottle to reduce flooding of the WebSocket connection to the browser.
     * <p/>
     * Define a time interval after which #needsUpdate() returns true once and then returns false until the next
     * interval has expired.
     */
    static class DataTrottle {
        private long lastSend = 0;
        private long interval;

        public DataTrottle(long amount, TimeUnit unit) {
            interval = unit.toMillis(amount);
        }

        public boolean needsUpdate() {
            long now = System.currentTimeMillis();
            if (now - lastSend > interval) {
                lastSend = now;
                return true;
            }
            return false;
        }
    }


    static class Loc {

        double x, y, p;
        int index;

        static Loc from(double x, double y, double p, int index) {
            Loc loc = new Loc();
            loc.x = x;
            loc.y = y;
            loc.p = p;
            loc.index = index;
            return loc;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getP() {
            return p;
        }

        public double getIndex() {
            return index;
        }
    }

    static class LocationUpdate {
        private List<Loc> locations;
        private Status status;


        LocationUpdate(List<Loc> locations, Status status) {
            this.locations = locations;
            this.status = status;
        }

        public List<Loc> getLocations() {
            return locations;
        }

        public Status getStatus() {
            return status;
        }
    }

    static class LocIndex {
        int locIndex;

        static LocIndex from(Locations locations) {
            LocIndex li = new LocIndex();
            li.locIndex = locations.getIndexWithHighestPropability();
            return li;
        }

        public int getLocIndex() {
            return locIndex;
        }
    }
}

