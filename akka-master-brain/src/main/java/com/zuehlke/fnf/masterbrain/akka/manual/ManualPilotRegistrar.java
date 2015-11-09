package com.zuehlke.fnf.masterbrain.akka.manual;

import com.zuehlke.fnf.masterbrain.akka.messages.PilotConfig;
import com.zuehlke.fnf.masterbrain.service.PilotRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Registers the ManualPilotActor with the registry.
 * Created by tho on 23.07.2015.
 */
@Service
public class ManualPilotRegistrar {

    @Autowired
    private PilotRegistry registry;

    public static PilotConfig createConfig() {
        return PilotConfig.create("00_Manual_Pilot", ManualPilotActor.class, "Allows manual control of the slot car", PilotConfig.ACCEPT_MANUAL_POWER_UPDATES);
    }

    @PostConstruct
    public void registerPilot() throws ClassNotFoundException {
        registry.registerPilotActor(createConfig());
    }
}
