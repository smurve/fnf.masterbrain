package com.zuehlke.fnf.masterbrain.akka.manual;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import com.zuehlke.fnf.masterbrain.akka.Publisher;
import com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import com.zuehlke.fnf.masterbrain.akka.utils.StopWatch;
import com.zuehlke.fnf.masterbrain.akka.trainontrack.FeatureExtraxtor;
import com.zuehlke.fnf.masterbrain.akka.trainontrack.SanityCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is a pilot implementation that drives the car with a constant speed manually set through
 * the REST API. Therefore, there's no real algorithm implemented here.
 * <p/>
 * Created by tho on 23.07.2015.
 */
public class ManualPilotActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManualPilotActor.class);

    private Power power;
    private int initialPower;
    private StopWatch stopWatch = new StopWatch();
    private ActorRef pilotController;
    private FeatureExtraxtor featureExtractor;

    public ManualPilotActor() {
        Config config = context().system().settings().config();
        initialPower = ConfigHelper.readValue("masterbrain.pilot.initialPower", config::getInt, 150);
        handleReset();
        schedulePowerProfilePublishing();
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
        } else if (message instanceof ActorRef) {
            pilotController = context().sender();
        } else if ("POWER_PROFILE".equals(message)) {
            publishPowerProfile();
        } else {
            unhandled(message);
        }
    }



    private void handleTrack(final Track message) throws InterruptedException {
        LOGGER.info("Got a new track.");
        stopWatch.setTrack(message);
        featureExtractor = new FeatureExtraxtor(message, 0, 0);
    }

    private void handleReset() {
        power = Power.of(initialPower);
    }

    private void handleLocations(Locations locations) {
        LOGGER.debug("Don't care about current Location. Sending 'update' to pilotController");
        Publisher.tell(power, pilotController, getSelf()).onMissingSender().ignore().andReturn();
        stopWatch.onLocation(locations, lapTime -> Publisher.tell(new Info("MPA", "race", String.format("lap time=%s", lapTime)), pilotController, getSelf()).onMissingSender().ignore().andReturn());
    }

    private void handlePowerCommand(Power power) {
        this.power = power;
        LOGGER.debug("Got a new Power command.");
        Publisher.tell(power, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }

    public void firePowerProfile(int[] powerValues) {
        List<Double> collect = Arrays.stream(powerValues).mapToDouble(Double::valueOf).boxed().collect(Collectors.toList());
        ImmutableList<Double> profile = new ImmutableList.Builder<Double>().addAll(collect).build();
        TrackWithPowerProfile p = TrackWithPowerProfile.from(featureExtractor.getTrack(), profile);
        Publisher.tell(p, pilotController, getSelf()).onMissingSender().ignore().andReturn();
    }

    private void publishPowerProfile() {
        if (featureExtractor != null) {
            FixedSpeedCalculator speedCalculator = new FixedSpeedCalculator(featureExtractor, power.getValue());
            new SanityCheck(Integer.MAX_VALUE, speedCalculator.simulateLap()).execute(this::firePowerProfile, (s, aDouble) -> LOGGER.info("SHOULD NEVER HAPPEN"));
        }
    }

    private void schedulePowerProfilePublishing() {
        FiniteDuration interval = Duration.create(1, TimeUnit.SECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), "POWER_PROFILE", context().system().dispatcher(), null);
    }
}
