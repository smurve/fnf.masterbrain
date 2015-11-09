package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.carrera.relayapi.messages.RoundTimeMessage;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;
import com.zuehlke.fnf.masterbrain.MasterBrainProperties;
import com.zuehlke.fnf.masterbrain.akka.*;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import com.zuehlke.fnf.masterbrain.util.ConsumerWithThrowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.zuehlke.fnf.masterbrain.akka.Publisher.tell;

/**
 * Wires the actor system. Provides entry points for the Spring world to communicate with the actors. You can call the actors
 * directly or if the actors have to call you, you can register an inbox that calls you when messages have arrived.
 */
@Service
public class MasterBrainService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MasterBrainService.class);
    @Autowired
    private ActorSystem system;
    @Autowired
    private MasterBrainProperties settings;

    private ActorRef pilot;
    private ActorRef enricher;
    private ActorRef trackBuilder;
    private ActorRef localization;
    private ActorRef webPublisher;
    private ActorRef lapTimes;

    @PostConstruct
    public void init() throws Exception {
        trackBuilder = system.actorOf(Props.create(TrackBuilderActor.class, () -> new TrackBuilderActor()), TrackBuilderActor.NAME);
        localization = system.actorOf(Props.create(LocalizationActor.class, () -> new LocalizationActor()), LocalizationActor.NAME);
        enricher = system.actorOf(Props.create(SensorDataEnricherActor.class, () -> new SensorDataEnricherActor(trackBuilder, localization)),
                SensorDataEnricherActor.NAME);
        pilot = system.actorOf(Props.create(PilotControlActor.class, () -> new PilotControlActor(enricher)), PilotControlActor.NAME);
        lapTimes = system.actorOf(Props.create(LapTimeActor.class), LapTimeActor.NAME);
    }

    public void reset() {
        Arrays.asList(pilot, trackBuilder, localization, enricher, webPublisher).forEach((ref) -> {
            ref.tell(ResetCommand.message(), ActorRef.noSender());
        });
    }

    public void registerInboxForQueuePublisher(ConsumerWithThrowable<Object> fun) {
        registerInbox(fun, new RegisterQueuePublisher(), pilot);
    }

    public void registerInboxForWebPublisher(ConsumerWithThrowable<Object> fun) {
        Inbox inbox = registerInbox(fun, new RegisterWebPublisher(), pilot, enricher, trackBuilder, localization, lapTimes, localization);
        webPublisher = inbox.getRef();
    }

    public void registerInboxForTrackBuilder(ConsumerWithThrowable<Object> fun) {
        registerInbox(fun, new RegisterTrackBuilder(), trackBuilder);
    }

    public void registerInboxForLocalizator(ConsumerWithThrowable<Object> fun) {
        registerInbox(fun, new RegisterLocalizator(), localization);
    }

    private Inbox registerInbox(ConsumerWithThrowable<Object> fun, Object registerMessage, ActorRef... refs) {
        final FiniteDuration duration = Duration.create(20, TimeUnit.MILLISECONDS);
        Inbox inbox = Inbox.create(system);
        Arrays.stream(refs).forEach((ref) -> inbox.send(ref, registerMessage));

        settings.getExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Object obj = inbox.receive(duration);
                    if (obj != null) {
                        fun.consume(obj);
                    }
                } catch (TimeoutException e) {
                    // ignore
                } catch (Exception e) {
                    LOGGER.warn("Function failed.", e);
                }
                settings.getExecutorService().submit(this);
            }
        });
        return inbox;
    }

    public ActorRef getPilot() {
        return pilot;
    }

    public ActorRef getTrackBuilder() {
        return trackBuilder;
    }

    public ActorRef getLocalization() {
        return localization;
    }

    public ActorRef getWebPublisher() {
        return webPublisher;
    }

    public ActorRef getEnricher() {
        return enricher;
    }

    public ActorRef getLapTimes() {
        return lapTimes;
    }

    public ActorRef actorOf(Props props, String name) {
        return system.actorOf(props, name);
    }

    /**
     * Publishes a PenaltyMessage to all Actors of interest.
     *
     * @param penaltyMessage
     */
    public void onPenalty(PenaltyMessage penaltyMessage) {
        getPilot().tell(penaltyMessage, ActorRef.noSender());
    }

    /**
     * Publishes a SensorEvent to all Actors of interest.
     *
     * @param sensorEvent
     */
    public void onSensorEvent(SensorEvent sensorEvent) {
        SensorData sensorData = SensorData.fromEvent(sensorEvent);
        publish(sensorData, getEnricher());
    }

    public void onVelocity(VelocityMessage velocity) {
        // nothing, yet.
    }

    public void onRoundPassedMessage(RoundTimeMessage roundPassed) {
        publish(roundPassed, getLapTimes());
    }

    public void onLocationPs(LocationPs locations) {
        publish(locations, getLocalization(), getPilot());
    }

    private void publish(Object message, ActorRef... targets) {
        Arrays.stream(targets).forEach((actorRef) -> {
            tell(message, actorRef, ActorRef.noSender()).onMissingSender().ignore().andReturn();
        });
    }

    public void onTrack(Track track) {
        publish(track, getPilot(), getTrackBuilder(), getLocalization(), getWebPublisher());
    }
}
