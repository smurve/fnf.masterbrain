package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import com.zuehlke.fnf.masterbrain.akka.messages.SensorDataMapper;
import com.zuehlke.fnf.masterbrain.akka.messages.SensorDataRange;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads sensor data from a dump file and sends it to the track builder.
 * <p/>
 * Created by tho on 13.07.2015.
 */
@Service
public class TrackLoadingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackLoadingService.class);
    @Autowired
    private MasterBrainService masterBrain;

    public String load(String track) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("data/" + track + ".json")) {
            SensorDataRange data = SensorDataMapper.fromJSON(in);
            masterBrain.getTrackBuilder().tell(data, ActorRef.noSender());
        } catch (IOException e) {
            return "FAILED";
        }
        return "OK";
    }

    public List<String> getTracks() {
        try {
            org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath:data/*.json");
            List<String> files = Arrays.stream(resources)
                    .map(resource -> StringUtils.substringBeforeLast(resource.getFilename(), ".json"))
                    .sorted(Comparator.<String>naturalOrder())
                    .collect(Collectors.toList());
            return files;
        } catch (IOException e) {
            LOGGER.error("Couldn't lookup tracks.", e);
        }
        return Collections.emptyList();
    }
}
