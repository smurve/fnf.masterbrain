package com.zuehlke.fnf.masterbrain.akka.tho;

import com.zuehlke.fnf.masterbrain.akka.messages.PilotConfig;
import com.zuehlke.fnf.masterbrain.service.PilotRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Registers the ThoPilotActor with the pilot registration.
 * <p/>
 * Created by tho on 23.07.2015.
 */
@Service
public class ThoPilotRegistrar {

    @Autowired
    private PilotRegistry registry;

    public static PilotConfig createConfig() {
        return PilotConfig.create("THO - Thomas", ThoPilotActor.class, "Lookahead implementation by tho");
    }

    @PostConstruct
    public void registerPilot() throws ClassNotFoundException {
        registry.registerPilotActor(createConfig());
    }
}
