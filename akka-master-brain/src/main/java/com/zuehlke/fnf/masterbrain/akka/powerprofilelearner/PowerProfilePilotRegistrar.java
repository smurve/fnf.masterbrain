package com.zuehlke.fnf.masterbrain.akka.powerprofilelearner;

import com.zuehlke.fnf.masterbrain.service.PilotRegistry;
import com.zuehlke.fnf.masterbrain.akka.messages.PilotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by tho on 23.07.2015.
 */
@Service
public class PowerProfilePilotRegistrar {

    @Autowired
    private PilotRegistry registry;

    public static PilotConfig getConfig() {
        return PilotConfig.create("Power Profile Pilot", PowerProfilePilotActor.class, "Genetic Algorithm based learner that calculates a power profile for every location on the track");
    }

    @PostConstruct
    public void registerPilot() {
        registry.registerPilotActor(getConfig());
    }
}
