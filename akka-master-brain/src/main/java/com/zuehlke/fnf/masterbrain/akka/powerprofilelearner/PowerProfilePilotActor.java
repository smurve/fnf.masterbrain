package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import com.google.common.collect.ImmutableList;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

/**
 * Created by tho on 23.07.2015.
 */
public class PowerProfilePilotActor extends UntypedActor {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PowerProfilePilotActor.class);

    private Track track;
    private ImmutableList<Double> powerProfile;
    private ActorRef learner;
    private ActorRef webPublisher;
    private int initialPower;

    public PowerProfilePilotActor() {
        initialPower = context().system().settings().config().getInt("masterbrain.pilot.initialPower");
        learner = getContext().actorOf(Props.create(LearnerActor.class), LearnerActor.class.getSimpleName());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        LOGGER.debug("Message={}", message);
        if (message instanceof Power) {
            handlePowerCommand((Power) message);
        } else if (message instanceof Locations) {
            handleLocations((Locations) message);
        } else if (message instanceof Track) {
            handleTrack((Track) message);
        } else if (message instanceof ResetCommand) {
            handleReset();
        } else if (message instanceof LearningFinished) {
            handleLearningFinished((LearningFinished) message);
        } else if (message instanceof GetPowerProfile) {
            handleGetPowerProfile();
        } else if (message instanceof RegisterWebPublisher) {
            webPublisher = getContext().sender();
        } else {
            unhandled(message);
        }
    }

    private void handleReset() {
        track = null;
        powerProfile = null;
    }

    private void handlePowerCommand(Power message) {
        LOGGER.info("I don't care about manual power commands!");
    }

    private void handleGetPowerProfile() {
        LOGGER.info("Somebody is asking for current powerProfile");
        getContext().sender().tell(TrackWithPowerProfile.from(track, powerProfile), getSelf());
    }

    private void handleLearningFinished(final LearningFinished message) {
        ImmutableList<Double> powerSettings = message.getPowerSettings();
        LOGGER.debug("Learning finished: {}, \nwith approximate race time: {}", powerSettings, message.getScore());
        powerProfile = powerSettings;
        if (track != null && powerSettings != null) {
            if (webPublisher != null) {
                webPublisher.tell(TrackWithPowerProfile.from(track, powerSettings), getSelf());
            } else {
                LOGGER.info("No webpublisher, yet");
            }
        }
    }

    private void handleTrack(Track message) {
        LOGGER.info("Got a new track");
        if (track == null) {
            track = message;
            track.compoundTrackParts();
            track.preparePhysics();
            learner.tell(track, getSelf());
        } else {
            LOGGER.info("Ignoring new track");
        }
    }

    private void handleLocations(Locations locations) {
        ActorRef sender = context().sender();
        LOGGER.debug("Got new locations!");
        Future future = Futures.future(() -> {
            Power pc = new Power();
            if (powerProfile != null) {
                int indexWithHighestPropability = locations.getIndexWithHighestPropability();
                int value = powerProfile.get(indexWithHighestPropability).intValue();
                //value = Math.min(value, 200);
                pc.setValue(value);
            } else {
                pc.setValue(initialPower);
            }
            return pc;
        }, getContext().system().dispatcher());
        Patterns.pipe(future, getContext().dispatcher()).to(sender, getSelf());
    }


}
