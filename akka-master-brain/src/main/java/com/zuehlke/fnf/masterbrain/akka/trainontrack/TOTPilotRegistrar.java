package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import com.zuehlke.fnf.masterbrain.service.PilotRegistry;
import com.zuehlke.fnf.masterbrain.akka.messages.PilotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by tho on 10.08.2015.
 */
@Service
public class TOTPilotRegistrar {

    @Autowired
    private PilotRegistry registry;

    private static PilotConfig getLearnerConfig() {
        return PilotConfig.create("Train On Track (Learner)",
                TOTPilotActor.class,
                "Genetic Algorithm based learner that trains on the real track. It looks ahead and calculates the speed based on three segments using a simple neuronal network.");
    }

    private static PilotConfig getAutoPilotConfig() {
        return PilotConfig.create("Train On Track (Auto Pilot)",
                TOTAutopilotActor.class,
                "Drives based on the result of the Train On Track Learner");
    }

    @PostConstruct
    public void registerPilot() {
        registry.registerPilotActor(getLearnerConfig());
        registry.registerPilotActor(getAutoPilotConfig());
    }
}