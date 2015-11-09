package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.google.common.collect.EvictingQueue;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static com.zuehlke.fnf.masterbrain.akka.Publisher.tell;
import static com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper.readValue;

/**
 * This actor collects the latest n @SensorData and sends them to the RServ backend from time to time:
 */
public class LocalizationActor extends UntypedActor {

    public static final String NAME = "Localization";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LocalizationActor.class);
    public static final String INFO_TYPE = "localization";
    private static final String MESSAGE_CHECK_LOCALIZATION = "MESSAGE_CHECK_LOCALIZATION";

    private ActorRef rServ;
    private ActorRef webPublisher;
    private ActorRef dumpActor;

    private EvictingQueue<SensorData> buffer;

    private long counter;
    private long counterOnLastLocalization;

    private Track track;
    private boolean missingDataMode;
    private boolean missingLocationMode;
    private long lastLocationMillis;
    private long maxLastLocationMillis = 1000;
    private TimeDelta delta;

    public LocalizationActor() {
        initBuffer();
        scheduleLocalizationTrigger();
        scheduleLocalizationCheck();
        initDumpActor();
    }

    private void initDumpActor() {
        boolean dump = readValue("masterbrain.localization.dump", context().system().settings().config()::getBoolean, false);
        if (dump) {
            dumpActor = context().actorOf(Props.create(DumpActor.class), DumpActor.NAME);
        }
    }

    private void initBuffer() {
        int bufferSize = readValue("masterbrain.localization.bufferSize", context().system().settings().config()::getInt, 50);
        buffer = EvictingQueue.create(bufferSize);
    }

    private void scheduleLocalizationTrigger() {
        long millis = readValue("masterbrain.localization.interval", context().system().settings().config()::getLong, 20L);
        FiniteDuration interval = Duration.create(millis, TimeUnit.MILLISECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), new Localize(), context().system().dispatcher(), null);
    }

    private void scheduleLocalizationCheck() {
        long millis = readValue("masterbrain.localization.localizationCheckInterval", context().system().settings().config()::getLong, 500L);
        FiniteDuration interval = Duration.create(millis, TimeUnit.MILLISECONDS);
        context().system().scheduler().schedule(interval, interval, getSelf(), MESSAGE_CHECK_LOCALIZATION, context().system().dispatcher(), null);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SensorData) {
            handleSensorData((SensorData) message);
        } else if (message instanceof Localize) {
            handleLocalization();
        } else if (message instanceof LocationPs) {
            handleLocationPs((LocationPs)message);
        } else if (message instanceof Track) {
            handleTrack((Track) message);
        } else if (message instanceof ResetCommand) {
            handleReset();
        } else if (message instanceof RegisterLocalizator) {
            rServ = getContext().sender();
        } else if (message instanceof RegisterWebPublisher) {
            webPublisher = getContext().sender();
        } else if (message instanceof TimeDelta) {
            delta = (TimeDelta)message;
        } else if (MESSAGE_CHECK_LOCALIZATION.equals(message)) {
            checkLocalization();
        } else {
            unhandled(message);
        }
    }

    /**
     * Checks if localization is still working. If no it triggers the recalculation that should fix it in R.
     */
    private void checkLocalization() {
        if (track == null || rServ == null || (System.currentTimeMillis() - lastLocationMillis) < maxLastLocationMillis) {
            return;
        }

        LOGGER.warn("Localization broken.");
        //enterMissingLocalizationMode();
    }



    private void handleLocationPs(LocationPs message) {
        if (!message.getStatus().isOk()) {
            publishToWebPublisher(new Info(INFO_TYPE, "data", message.getStatus().getMessage()));
            return;
        }
        if (Double.isNaN(message.getPs()[0])) {
            LOGGER.info("NaN values", message.getStatus(), message.getPs());
        } else {
            lastLocationMillis = System.currentTimeMillis();
            exitMissingLocationMode();
        }
    }

    private void enterMissingLocalizationMode() {
        if (!missingLocationMode) {
            LOGGER.warn("enter missingLocationMode");
            missingLocationMode = !missingLocationMode;
            publishToWebPublisher(new Info(INFO_TYPE, "location", String.format("No localization for %s ms", maxLastLocationMillis)));
            tell("FIX_TRACK", rServ, getSelf()).onMissingSender().logWarn("No rServ.").andReturn();
        }
    }

    private void exitMissingLocationMode() {
        if (missingLocationMode) {
            missingLocationMode = !missingLocationMode;
            publishToWebPublisher(new Info(INFO_TYPE, "location", "ok"));
            lastLocationMillis = System.currentTimeMillis();
        }
    }

    private void handleTrack(Track message) {
        if (message.getStatus().isOk()) {
            track = message;
            publishToWebPublisher(new Info(INFO_TYPE, "track", "ok"));
            exitMissingLocationMode();
        } else {
            LOGGER.info("Discarding new track due to status. {}", message.getStatus());
            publishToWebPublisher(new Info(INFO_TYPE, "track", "Track quality is bad."));
        }
    }

    private void handleReset() {
        LOGGER.info("reset");
        buffer.clear();
        track = null;
    }

    private void handleLocalization() {
        LOGGER.debug("Should localize now!");
        if (track != null && !missingLocationMode) {
            LOGGER.debug("We have a track");
            if (rServ != null) { // && !buffer.isEmpty()) {
                long elements = counter - counterOnLastLocalization;
                counterOnLastLocalization = counter;
                if (elements < 1) {
                    LOGGER.debug("new data elements={}", elements);
                    enterMissingDataMode();
                    //return;
                } else {
                    exitMissingDataMode();
                }
                if (delta == null) {
                    LOGGER.info("No time delta, yet!");
                } else {
                    SensorDataRange data = SensorDataRange.from(buffer, delta);
                    dump(data);
                    publishToBackend(data);
                }
                buffer.clear();
            } else {
                LOGGER.info("No rServ, yet");
            }
        } else if (track != null && missingLocationMode) {
            LOGGER.info("we are in missing localization mode");
        } else {
            LOGGER.debug("No track, yet");
        }
        LOGGER.debug("Leaving Localization. Localization still running");
    }

    private void dump(SensorDataRange data) {
        tell(data, dumpActor, getSelf()).onMissingSender().ignore().andReturn();
    }

    private void publishToBackend(SensorDataRange data) {
        tell(Localize.from(data), rServ, getSelf()).onMissingSender().ignore().andReturn();
    }

    private void exitMissingDataMode() {
        if (missingDataMode) {
            missingDataMode = false;
            publishToWebPublisher(new Info(INFO_TYPE, "data", "ok"));
        }
    }

    private void enterMissingDataMode() {
        if (!missingDataMode) {
            missingDataMode = true;
            publishToWebPublisher(new Info(INFO_TYPE, "data", "No new data"));
        }
    }

    private void publishToWebPublisher(Object message) {
        tell(message, webPublisher, getSelf()).onMissingSender().ignore().andReturn();
    }

    private void handleSensorData(SensorData message) {
        counter++;
        buffer.add(message);
    }

}
