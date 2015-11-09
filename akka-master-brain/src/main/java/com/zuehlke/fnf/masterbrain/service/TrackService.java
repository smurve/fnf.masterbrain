package com.zuehlke.fnf.masterbrain.service;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.zuehlke.fnf.masterbrain.akka.messages.GetTrack;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.GetPowerProfile;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackWithPowerProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mhan on 08.07.2015.
 */
@Service
public class TrackService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackService.class);

    @Autowired
    private MasterBrainService masterBrain;

    public Track getCurrentTrack() {
        Timeout timeout = new Timeout(Duration.create(500, TimeUnit.MILLISECONDS));
        LOGGER.info("Asking TrackBuilder for current track.");
        Future<Object> future = Patterns.ask(masterBrain.getTrackBuilder(), new GetTrack(), timeout);
        LOGGER.info("Future received. Waiting for answer");
        try {
            Track track = (Track) Await.result(future, timeout.duration());
            LOGGER.info("Track received");
            return track;
        } catch (Exception e) {
            LOGGER.warn("Did not receive any result on time.");
            return null;
        }
    }

    public List<TrackWithPowerProfile.PowerSetting> getCurrentPowerProfile() {
        Timeout timeout = new Timeout(Duration.create(500, TimeUnit.MILLISECONDS));
        LOGGER.info("Asking Pilot for current powerProfile.");
        Future<Object> future = Patterns.ask(masterBrain.getPilot(), new GetPowerProfile(), timeout);
        LOGGER.info("Future received. Waiting for answer");
        TrackWithPowerProfile result = null;
        try {
            result = (TrackWithPowerProfile) Await.result(future, timeout.duration());
        } catch (Exception e) {
            LOGGER.info("Did not receive any result on time.");
            return null;
        }
        LOGGER.info("Answer received.");
        return result.getPowerSettings();
    }
}