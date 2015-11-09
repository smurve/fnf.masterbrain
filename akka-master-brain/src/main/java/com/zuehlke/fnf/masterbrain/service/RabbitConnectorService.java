package com.zuehlke.fnf.masterbrain.service;

import com.zuehlke.carrera.api.PilotApi;
import com.zuehlke.carrera.api.PilotApiImpl;
import com.zuehlke.carrera.api.channel.PilotToRelayChannelNames;
import com.zuehlke.carrera.api.client.rabbit.RabbitClient;
import com.zuehlke.carrera.api.seralize.JacksonSerializer;
import com.zuehlke.carrera.relayapi.messages.PilotLifeSign;
import com.zuehlke.carrera.relayapi.messages.PowerControl;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by tho on 20.09.2015.
 */
@Service
@EnableScheduling
public class RabbitConnectorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitConnectorService.class);
    @Autowired
    private MasterBrainService masterBrainService;
    @Value("${masterbrain.rabbitMq.host}")
    private String host;
    @Value("${masterbrain.rabbitMq.port}")
    private int port; // currently not supported by the client lib
    @Value("${masterbrain.rabbitMq.username}")
    private String username;
    @Value("${masterbrain.rabbitMq.accessCode}")
    private String accessCode;
    private PilotApi pilotApi;

    @PostConstruct
    public void init() throws Exception {
        LOGGER.info("Connecting to RabbitMq. host={}, port={}, username={}", host, port, username);

        pilotApi = new PilotApiImpl(new RabbitClient(), new PilotToRelayChannelNames(username/*settings.getName()*/), new
                JacksonSerializer());
        pilotApi.onPenalty(masterBrainService::onPenalty);
        pilotApi.onRaceStart(this::handleUnknown);
        pilotApi.onRaceStop(this::handleUnknown);
        pilotApi.onRoundPassed(masterBrainService::onRoundPassedMessage);
        pilotApi.onSensor(masterBrainService::onSensorEvent);
        pilotApi.onVelocity(this::handleUnknown);
        pilotApi.connect(host);

        LOGGER.info("Initializing rabbit MQ producer");
        masterBrainService.registerInboxForQueuePublisher((message) -> {
            LOGGER.debug("Got Message form Inbox: {}", message);
            if (message instanceof Power) {
                pilotApi.powerControl(new PowerControl(((Power) message).getValue(), username, accessCode, System.currentTimeMillis()));
            }
        });
    }

    @Scheduled(fixedRate = 2000)
    public void ensureConnection() {
        LOGGER.debug("ensureConnection");
        pilotApi.connect(host);
    }

    @Scheduled(fixedRate = 1000)
    public void announce() {
        LOGGER.debug("announce");
        PilotLifeSign lifeSign = new PilotLifeSign(username,
                accessCode, null, System.currentTimeMillis());
        pilotApi.announce(lifeSign);

    }

    private void handleUnknown(Object msg) {
        LOGGER.debug("unhandled message from relay. msg={}", msg);
    }
}
