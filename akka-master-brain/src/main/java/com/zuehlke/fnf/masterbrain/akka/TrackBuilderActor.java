package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.google.common.collect.EvictingQueue;
import com.typesafe.config.Config;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zuehlke.fnf.masterbrain.akka.Publisher.tell;
import static com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper.readValue;

/**
 * Collects sensor data and sends it to the RServ backend to reconstruct the track.
 * If reconstruction was successful the actor stops sending data to the backend until a reset of the actor.
 */
public class TrackBuilderActor extends UntypedActor {

    public static final String NAME = "TrackBuilder";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(TrackBuilderActor.class);

    private final EvictingQueue<SensorData> buffer;

    private final int buildAfter;
    private ActorRef dumper;
    private Track track;
    private ActorRef rService;
    private ActorRef webPublisher;

    private int counter = 0;
    private TimeDelta delta;

    public TrackBuilderActor() {
        Config config = context().system().settings().config();
        this.buildAfter = readValue("masterbrain.trackBuilder.buildAfter", config::getInt, 500);
        int bufferSize = readValue("masterbrain.trackBuilder.bufferSize", config::getInt, 5000);

        buffer = EvictingQueue.create(bufferSize);

        boolean dump = readValue("masterbrain.trackBuilder.dump", config::getBoolean, false);
        if (dump) {
            dumper = getContext().actorOf(Props.create(DumpActor.class), DumpActor.NAME);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SensorData) {
            handleSensorData((SensorData) message);
        } else if (message instanceof SensorDataRange) {
            handleTrackBuilding((SensorDataRange) message);
        } else if (message instanceof ResetCommand) {
            handleReset();
        } else if (message instanceof RegisterTrackBuilder) {
            rService = getContext().sender();
        } else if (message instanceof RegisterWebPublisher) {
            webPublisher = getContext().sender();
        } else if (message instanceof Track) {
            handleTrack((Track) message);
        } else if (message instanceof GetTrackBuildingState) {
            handleTrackBuildingState();
        } else if (message instanceof GetTrack) {
            handleGetTrack();
        } else if (message instanceof TimeDelta) {
            delta = (TimeDelta)message;
        } else {
            unhandled(message);
        }
    }

    private void handleTrack(Track message) {
        if (message.getStatus().isError()) {
            LOGGER.warn("Track building failed. error={}", message.getStatus().getMessage());
            return;
        }
        track = message;
        tell(track, dumper, getSelf()).onMissingSender().ignore().andReturn();
    }

    private void handleGetTrack() {
        LOGGER.debug("Somebody is asking for current track");
        if (track != null) {
            getContext().sender().tell(track, getSelf());
        }
    }

    private void handleTrackBuildingState() {
        LOGGER.debug("Somebody ask for TrackBuildingState");
        getContext().sender().tell(TrackBuildingState.from(counter, buildAfter), getSelf());
    }

    private void handleReset() {
        LOGGER.info("reset");
        buffer.clear();
        track = null;
        counter = 0;
    }

    private void handleTrackBuilding(final SensorDataRange message) {
        LOGGER.debug("Should call RSERV now!");
        tell(BuildTrack.from(message), rService, getSelf()).onMissingSender().logInfo("No rService known, yet").andReturn();
        LOGGER.debug("Leaving TrackBuilder. Track building still running");
    }

    private void handleSensorData(SensorData message) {
        counter++;
        buffer.add(message);
        if (counter % 100 == 0) {
            LOGGER.debug("Received {} events: ", counter);
        }
        if (counter >= buildAfter) {
            buildTrackIfNeeded();
            counter = 0;
        }
        tell(TrackBuildingState.from(counter, buildAfter), webPublisher, getSelf()).onMissingSender().logDebug("No webPublisher").andReturn();
    }

    private void triggerTrackBuilding() {
        if (delta == null) {
            LOGGER.warn("No time delta, yet!");
        } else {
            SensorDataRange event = SensorDataRange.from(buffer, delta);
            getSelf().tell(event, getSelf());
            tell(event, dumper, getSelf()).onMissingSender().ignore().andReturn();
        }
    }

    private void buildTrackIfNeeded() {
        if (track == null || !track.getStatus().isOk()) {
            LOGGER.debug("No valid track, yet");
            triggerTrackBuilding();
        } else {
            LOGGER.debug("We already have a track, so, we don't do a recalcualtion");
        }
    }

}
