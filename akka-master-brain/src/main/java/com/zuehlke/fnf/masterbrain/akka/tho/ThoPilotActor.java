package com.zuehlke.fnf.masterbrain.akka.tho;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import com.zuehlke.fnf.masterbrain.akka.utils.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zuehlke.fnf.masterbrain.akka.Publisher.tell;

/**
 * A simple pilot which looks at the track segments ahead. The pilot relies on the Track and Locations provided
 * by other actors. It only known straight and curve segments. On straight segment it will power up to maxSpeed and
 * it will slow down when a curve comes in sight. It's able to learn from penalties by modifying it's knowledge about
 * the track by switching the segment type from straight to curve to expand the curve.
 * <p/>
 * Created by tho on 23.07.2015.
 */
public class ThoPilotActor extends UntypedActor implements PilotFeedback {

    static final String MESSAGE_CHECK_UPDATES = "CHECK_UPDATES";
    private static final Logger LOGGER = LoggerFactory.getLogger(ThoPilotActor.class);
    private int initialPower = -1;

    private SafetyHandler safetyHandler;
    private LocationHandler locationHandler;
    private LocationHandler trackHandler;
    private ActorRef pilotController;
    private StopWatch stopWatch = new StopWatch();

    public ThoPilotActor() {
        Config config = context().system().settings().config();
        initialPower = ConfigHelper.readValue("masterbrain.pilot.initialPower", config::getInt, 150);
        safetyHandler = new SafetyHandler(config);
        locationHandler = new LocationHandler(config);
        locationHandler.setSafetyHandler(safetyHandler);
        trackHandler = locationHandler;
        handleReset();
        scheduleUpdateCheck();
    }

    private void scheduleUpdateCheck() {
        long millis = 50;
        FiniteDuration interval = Duration.create(millis, TimeUnit.MILLISECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), MESSAGE_CHECK_UPDATES, context().system().dispatcher(), null);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        LOGGER.debug("Message={}", message);
        if (message instanceof Power) {
            handlePowerCommand((Power) message);
        } else if (message instanceof Locations) {
            locationHandler.onNewLocation((Locations) message, this);
            stopWatch.onLocation((Locations) message, lapTime -> tell(new Info("THO", "race", String.format("lap time=%s", lapTime)), pilotController, getSelf()).onMissingSender().ignore().andReturn());
        } else if (MESSAGE_CHECK_UPDATES.equals(message)) {
            safetyHandler.checkSafety(this);
        } else if (message instanceof PenaltyMessage) {
            handlePenalty((PenaltyMessage) message);
        } else if (message instanceof Track) {
            trackHandler.onNewTrack((Track) message);
            stopWatch.setTrack((Track) message);
        } else if (message instanceof ResetCommand) {
            handleReset();
        } else if (message instanceof ActorRef) {
            pilotController = context().sender();
        } else {
            unhandled(message);
        }
    }

    private void handlePenalty(PenaltyMessage message) {
        safetyHandler.handlePenalty(message, locationHandler::fixSegmentsBeforeLastBreak, this);
    }

    private void publishToPilotController(Object message) {
        tell(message, pilotController, getSelf()).onMissingSender().logInfo("No pilotController, yet.").andReturn();
    }

    private void handleReset() {
        firePower(Power.of(initialPower));
        safetyHandler.reset();
        locationHandler.reset();
        trackHandler.reset();
    }

    private void handlePowerCommand(Power power) {
        LOGGER.debug("Don't care about power commands from outside)");
    }

    @Override
    public void firePower(Power newPower) {
        publishToPilotController(newPower);
    }

    @Override
    public void fireInfo(Info info) {
        publishToPilotController(info);
    }

    @Override
    public void firePowerProfile(int[] powerValues) {
        List<Double> collect = Arrays.stream(powerValues).mapToDouble(Double::valueOf).boxed().collect(Collectors.toList());
        ImmutableList<Double> profile = new ImmutableList.Builder<Double>().addAll(collect).build();
        TrackWithPowerProfile p = TrackWithPowerProfile.from(locationHandler.getTrack(), profile);
        tell(p, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }
}
