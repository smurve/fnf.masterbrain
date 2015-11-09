package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuehlke.fnf.masterbrain.akka.messages.SensorDataRange;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Actor that dumps the provided SensorDataRange into a file.
 */
public class DumpActor extends UntypedActor {

    public static final String NAME = "Dump";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DumpActor.class);

    private ObjectMapper mapper = new ObjectMapper();

    private File dumpDir = new File("data/dump");
    public DumpActor() {
        dumpDir.mkdirs();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof SensorDataRange) {
            handleSensorDataRange((SensorDataRange) message);
        } else if (message instanceof Track) {
            handleTrack((Track) message);
        } else {
            unhandled(message);
        }
    }

    private void handleSensorDataRange(SensorDataRange message) {
        Future<String> future = Futures.future(() -> {
            dump(message);
            return "dump successful";
        }, getContext().dispatcher());

        future.onSuccess(new OnSuccess<String>() {
            @Override
            public final void onSuccess(String result) {
                LOGGER.debug(result);
            }
        }, getContext().dispatcher());
        LOGGER.debug("Leaving dumper. Dump still running");
    }

    private void handleTrack(Track message) {
        Future<String> future = Futures.future(() -> {
            dump(message);
            return "dump successful";
        }, getContext().dispatcher());

        future.onSuccess(new OnSuccess<String>() {
            @Override
            public final void onSuccess(String result) {
                LOGGER.debug(result);
            }
        }, getContext().dispatcher());
        LOGGER.debug("Leaving dumper. Dump still running");
    }

    private void dump(SensorDataRange sensorDataList) {
        long deltaToSystemtimeAtIncome = sensorDataList.getDelta().getDelta();
        long synchedTime = System.currentTimeMillis() - deltaToSystemtimeAtIncome;

        File file = new File(dumpDir, "sensorData-"
                + new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date())
                + "_" + synchedTime + ".json");
        LOGGER.debug("DUMPING TO FILE={}", file.getAbsolutePath());

        try (OutputStream out = new FileOutputStream(file)) {
            mapper.writeValue(out, sensorDataList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void dump(Track track) {
        File file = new File(dumpDir, "track-"
                + new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date())
                + "_" + System.nanoTime() + ".json");
        LOGGER.debug("DUMPING TO FILE={}", file.getAbsolutePath());

        try (OutputStream out = new FileOutputStream(file)) {
            mapper.writeValue(out, track);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
