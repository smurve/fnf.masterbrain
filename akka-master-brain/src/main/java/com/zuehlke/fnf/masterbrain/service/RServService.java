package com.zuehlke.fnf.masterbrain.service;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import com.zuehlke.fnf.masterbrain.util.FunctionWithThrowable;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.zuehlke.fnf.masterbrain.util.StreamingUtils.conThrowRuntime;
import static com.zuehlke.fnf.masterbrain.util.StreamingUtils.funcThrowRuntime;

/**
 * Created by mhan on 02.07.2015.
 */
@Component
public class RServService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RServService.class);
    private static final String CFG_PREFIX = "${masterbrain.rserve";
    private static final Joiner JOIN_NL = Joiner.on("\n");
    @Autowired
    private MasterBrainService masterBrain;
    private RConnection rConnection;
    private SensorDataRange lastTrackCommend;

    @Value(CFG_PREFIX + ".host}")
    private String host;
    @Value(CFG_PREFIX + ".port}")
    private int port;
    @Value(CFG_PREFIX + ".user}")
    private String user;
    @Value(CFG_PREFIX + ".password}")
    private String password;
    private String rCodeLocation = "classpath*:/R/remote/*.R";
    private FileSystem zipFs;
    @PostConstruct
    public void init() throws Exception {
        rConnection = new RConnection(host, port);
        rConnection.login(user, password);

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        //FileSystem zipfs = FileSystems.newFileSystem(uri, env);

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());
        Resource[] resources = resolver.getResources(rCodeLocation);
        Arrays.stream(resources).map(funcThrowRuntime(res -> {
            LOGGER.info("Reading R script={}", res.getURL());
            return res;
        })).map(funcThrowRuntime(res -> res.getURI()))
                .map(funcThrowRuntime(uri -> {
                    LOGGER.debug("URI={}", uri);
                    if ("jar".equals(uri.getScheme()) && zipFs == null) {
                        zipFs = FileSystems.newFileSystem(uri, env);
                    }
                    return uri;
                }))
                .map(uri -> Paths.get(uri))
                .map(funcThrowRuntime(Files::readAllLines))
                .map(JOIN_NL::join)
                .forEach(conThrowRuntime(this::initRscript));

        masterBrain.registerInboxForTrackBuilder((message) -> {
            LOGGER.debug("Got Message form Inbox: {}", message);
            if (message instanceof BuildTrack) {
                lastTrackCommend = ((BuildTrack) message).getData();
                publish(buildTrack(lastTrackCommend));
            }
        });

        masterBrain.registerInboxForLocalizator((message) -> {
            LOGGER.debug("Got Message form Inbox: {}", message);
            if (message instanceof Localize) {
                publish(localizeCar(((Localize) message).getData()));
            } else if ("FIX_TRACK".equals(message)) {
                LOGGER.warn("Rebuilding track.");
                publish(buildTrack(lastTrackCommend));
            }
        });
    }

    private void publish(LocationPs locations) {
        masterBrain.onLocationPs(locations);
    }

    private void publish(Track track) {
        masterBrain.onTrack(track);
    }


    private Track buildTrack(SensorDataRange sensorDataList) {
        try {
            String sensorDataAsJson = SensorDataMapper.toJSON(sensorDataList);
            String format = String.format("buildTrack('%s')", sensorDataAsJson);
            LOGGER.debug("Sending to rserv {}", sensorDataAsJson);

            return sendCommad(format, TrackMapper::fromRObject);
        } catch (Exception e) {
            LOGGER.error("Exception on building track with Rserv", e);
            throw Throwables.propagate(e);
        }
    }

    private LocationPs localizeCar(SensorDataRange sensorDataList) {
        try {
            long deltaToSystemtimeAtIncome = sensorDataList.getDelta().getDelta();
            long synchedTime = System.currentTimeMillis() - deltaToSystemtimeAtIncome;
            String sensorDataAsJson = SensorDataMapper.toJSON(sensorDataList);
            //LOGGER.info("delta: {}, synchedTime: {}, data: {}", deltaToSystemtimeAtIncome, synchedTime, sensorDataAsJson);
            String format = String.format("localize('%s', %d)", sensorDataAsJson, synchedTime);
            LOGGER.debug("Sending to rserv {}", format);
            return sendCommad(format, LocationMapper::fromRObject);
        } catch (Exception e) {
            LOGGER.error("Exception on localize car", e);
            throw Throwables.propagate(e);
        }
    }


    public void initRscript(String rscript) throws RserveException {
        rConnection.voidEval(rscript);
    }


    @PreDestroy
    public void destroy() {
        rConnection.close();
    }

    public synchronized <T> T sendCommad(String command, FunctionWithThrowable<REXP, T> consumeResult) throws Exception {
        Stopwatch started = Stopwatch.createStarted();
        REXP x = rConnection.eval(command);
        LOGGER.debug("Call to Rserv took: {}", started);
        return consumeResult.apply(x);
    }
}
