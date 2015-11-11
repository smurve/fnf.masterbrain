package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import com.typesafe.config.Config;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import com.zuehlke.fnf.masterbrain.akka.manual.ManualPilotActor;
import com.zuehlke.fnf.masterbrain.akka.manual.ManualPilotRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static com.zuehlke.fnf.masterbrain.akka.Publisher.tell;
import static com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper.readValue;

/**
 * This Actor controls the actual pilot implementation. The actual Pilot is exchangeable at runtime. This actor is
 * responsible for:
 * - publishing Power messages to the outside word (the race track or web)
 * - reply to GetPower requests
 * - creation of  the acutal pilot
 * - initialization of the  pilot (when Track becomes available)
 * - republishing of the actual Power at a fixed rate
 */
public class PilotControlActor extends UntypedActor {

    public static final String NAME = "PilotControlActor";
    public static final String PUSH_POWER_TO_QUEUE = "PUSH_POWER_TO_QUEUE";
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PilotControlActor.class);
    private ActorRef sensorDataEnricher;
    private ActorRef queuePublisher;
    private ActorRef webPublisher;


    private Pilot pilot;

    private int currentPower = 0;
    private int initialPower;
    private Track track;
    private PilotProperties currentPilotProperties;

    public PilotControlActor(ActorRef sensorDataEnricher) {
        this.sensorDataEnricher = sensorDataEnricher;
        Config config = context().system().settings().config();
        initialPower = readValue("masterbrain.pilot.initialPower", config::getInt, 150);
        setupDefaultPilot();
        resetCar();
        schedulePowerTrigger();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        LOGGER.debug("Message={}", message);
        if (message instanceof Power) {
            handlePowerCommand((Power) message);
        } else if (message instanceof LocationPs) {
            handleLocationPs((LocationPs) message);
        } else if (message instanceof Info) {
            handleInfo((Info) message);
        } else if (message instanceof PenaltyMessage) {
            handlePenalty((PenaltyMessage) message);
        } else if (message instanceof ResetCommand) {
            handleReset();
        } else if (message instanceof GetPower) {
            handleGetPower();
        } else if (message instanceof Track) {
            handleTrack((Track) message);
        } else if (message instanceof RegisterQueuePublisher) {
            handleRegiserQueuePublisher();
        } else if (message instanceof RegisterWebPublisher) {
            handleRegisterWebPublisher();
        } else if (message instanceof TrackWithPowerProfile) {
            tell(message, webPublisher, getSelf()).onMissingSender().ignore().andReturn();
        } else if (message instanceof Pilot) {
            handlePilot((Pilot) message);
        } else if (message instanceof GetPilot) {
            handleGetPilot();
        } else if (message instanceof PilotProperties) {
            handlePilotProperties((PilotProperties) message);
        } else if (message instanceof GetPilotProperties) {
            handleGetPilotProperties();
        } else if (PUSH_POWER_TO_QUEUE.equals(message)) {
            getSelf().tell(Power.of(currentPower), getSelf());
        } else {
            forwardToPilot(message);
        }
    }

    private void handleGetPilotProperties() {
        getSender().tell(currentPilotProperties, getSelf());
    }

    private void handlePilotProperties(PilotProperties message) {
        if (isFromMyselfOrThePilot()) {
            currentPilotProperties = message;
            tell(message, webPublisher, getSelf()).onMissingSender().ignore().andReturn();
        } else {
            tell(message, pilot.getActorRef(), getSelf()).onMissingSender().logInfo("No Pilot set. Can't send PilotProperties command to pilot.").andReturn();
        }

    }

    private void handlePenalty(PenaltyMessage message) {
        int diffSpeed = (int) (message.getActualSpeed() - message.getSpeedLimit());
        String info = String.format("diff=%s, actualSpeed=%s, speedLimit=%s", diffSpeed, Math.round(message.getActualSpeed()), message.getSpeedLimit());
        handleInfo(new Info("penalty", "speed", info));
        forwardToPilot(message);
    }

    private void handleInfo(Info message) {
        tell(message, webPublisher, getSelf()).onMissingSender().ignore().andReturn();
    }

    /**
     * This triggers the republishing of the current power value. This is needed in case we
     * got a penalty and the race controller stopped our car and we have to start it again.
     */
    private void schedulePowerTrigger() {
        Config config = context().system().settings().config();
        long millis = readValue("masterbrain.pilot.republishInterval", config::getLong, 100L);
        FiniteDuration interval = Duration.create(millis, TimeUnit.MILLISECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), PUSH_POWER_TO_QUEUE, context().system().dispatcher(), null);
    }

    private void handleRegiserQueuePublisher() {
        LOGGER.info("Got a new QueuePublisher {}", context().sender());
        queuePublisher = getContext().sender();
    }

    private void handleGetPilot() {
        LOGGER.info("{} is asking for pilot {}.", context().sender(), pilot.getActorRef());
        tell(pilot, context().sender(), getSelf()).onMissingSender().ignore().andReturn();
    }

    private void handleRegisterWebPublisher() {
        LOGGER.info("Got a new WebPublisher");
        webPublisher = getContext().sender();
        if (pilot != null) {
            LOGGER.info("Telling pilot about new WebPublisher");
            pilot.getActorRef().tell(new RegisterWebPublisher(), webPublisher);
        }
    }

    private void handleTrack(Track track) {
        if (!track.getStatus().isOk()) {
            LOGGER.info("Discarding new track due to status. {}", track.getStatus());
            return;
        }
        LOGGER.info("Got a new track");
        this.track = track;
        forwardToPilot(track);
    }

    private void handleLocationPs(LocationPs message) {
        Future<Locations> future = Futures.future(() -> {
            Locations locations = Locations.from(track, message);
            return locations;
        }, getContext().dispatcher());
        if (pilot != null) {
            LOGGER.debug("Piping Locations to pilot {}", pilot);
            Patterns.pipe(future, getContext().dispatcher()).to(pilot.getActorRef(), getSelf());
        }
        if (webPublisher != null) {
            LOGGER.debug("Piping Locations to webpublisher");
            Patterns.pipe(future, getContext().dispatcher()).to(webPublisher, getSelf());
        }
    }

    private void forwardToPilot(Object message) {
        if (pilot != null) {
            pilot.getActorRef().tell(message, getSelf());
        } else {
            LOGGER.info("No pilot. Dropping message of type={}", message.getClass());
        }
    }


    private void handlePilot(Pilot message) {
        LOGGER.info("Got a new pilot. Changing from pilot={} to pilot={}", pilot == null ? null : pilot.getActorRef(), message.getActorRef());
        resetCar();
        if (message.getActorRef() == null) {
            LOGGER.error("Pilot has no actorRef assigned!");
            return;
        }
        if (pilot != null) {
            LOGGER.info("Killing old pilot {}", pilot.getActorRef());
            Future<Boolean> stopped = Patterns.gracefulStop(pilot.getActorRef(), Duration.create(5, TimeUnit.SECONDS));
            stopped.andThen(new OnComplete<Boolean>() {
                public void onComplete(Throwable failure, Boolean result) {
                    if (failure == null) {
                        LOGGER.info("Pilot stopped.");
                    } else {
                        LOGGER.error("Graceful stop of pilot actor failed.", failure);
                    }
                    handleInfo(new Info("safety", "off", null));
                }
            }, context().dispatcher());
        }

        // reset properties
        currentPilotProperties = new PilotProperties();
        tell(currentPilotProperties, webPublisher, getSelf()).onMissingSender().ignore().andReturn();
        // init new pilot
        pilot = message;
        LOGGER.info("Registrating self on new pilot");
        pilot.getActorRef().tell(getSelf(), getSelf());
        if (track != null) {
            LOGGER.info("Sending track to new pilot");
            pilot.getActorRef().tell(track, getSelf());
        }
        if (webPublisher != null) {
            LOGGER.info("Sending webPublisher to new pilot");
            pilot.getActorRef().tell(new RegisterWebPublisher(), webPublisher);
        }

    }

    private void handleGetPower() {
        LOGGER.debug("Somebody is asking for current power");
        Power power = new Power();
        power.setValue(currentPower);
        getContext().sender().tell(power, getSelf());
    }

    private void handleReset() {
        LOGGER.info("reset");
        track = null;
        resetCar();
    }

    private void handlePowerCommand(Power message) {
        if (isFromMyselfOrThePilot()) {
            LOGGER.debug("updating power from {} to {}", currentPower,
                    message.getValue());
            currentPower = message.getValue();
            tell(message, sensorDataEnricher, getSelf()).onMissingSender().ignore().andReturn();
            tell(message, queuePublisher, getSelf()).onMissingSender().ignore().andReturn();
            tell(message, webPublisher, getSelf()).onMissingSender().ignore().andReturn();
        } else {
            if (pilot != null) {
                //LOGGER.info("Routing Power command to pilot {}", pilot.getActorRef());
                pilot.getActorRef().tell(message, getSelf());
            } else {
                LOGGER.info("No Pilot set. Can't send Power command to pilot. Sender was={}", context().sender());
            }
        }
    }

    private boolean isFromMyselfOrThePilot() {
        return pilot != null && context().sender().equals(pilot.getActorRef()) || context().sender().equals(getSelf());
    }

    private void setupDefaultPilot() {
        PilotConfig config = ManualPilotRegistrar.createConfig();
        ActorRef defaultPilot = context().actorOf(Props.create(ManualPilotActor.class), config.getName());
        Pilot p = Pilot.of(defaultPilot);
        p.setConfig(config);
        getSelf().tell(p, getSelf());
    }


    private void resetCar() {
        LOGGER.info("Stopping car.");
        getSelf().tell(Power.of(0), getSelf());
        LOGGER.info("Starting car with moderate speed");
        context().system().scheduler().scheduleOnce(Duration.create(1, TimeUnit.SECONDS), getSelf(), Power.of(initialPower), context().system().dispatcher(), getSelf());

    }
}