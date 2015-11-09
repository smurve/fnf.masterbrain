package com.zuehlke.fnf.masterbrain.service;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.zuehlke.fnf.masterbrain.akka.messages.GetLapTimes;
import com.zuehlke.fnf.masterbrain.akka.messages.LapTime;
import com.zuehlke.fnf.masterbrain.akka.messages.LapTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tho on 05.08.2015.
 */
@Service
public class LapTimeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LapTimeService.class);
    @Autowired
    private MasterBrainService masterBrain;

    public List<LapTime> getLapTimes() {
        Timeout timeout = new Timeout(Duration.create(1, TimeUnit.SECONDS));
        LOGGER.info("Asking aktor for current lap times.");
        Future<Object> future = Patterns.ask(masterBrain.getLapTimes(), new GetLapTimes(), timeout);
        LOGGER.info("Future received. Waiting for answer");
        LapTime[] result = new LapTime[0];
        try {
            LapTimes lapTimes = (LapTimes) Await.result(future, timeout.duration());
            result = lapTimes.getLapTimes();
        } catch (Exception e) {
            LOGGER.warn("Did not receive any result on time.");
        }
        return Arrays.asList(result);
    }
}
