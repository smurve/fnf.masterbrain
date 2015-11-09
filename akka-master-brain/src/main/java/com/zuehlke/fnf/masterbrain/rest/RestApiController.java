package com.zuehlke.fnf.masterbrain.rest;

import com.zuehlke.fnf.masterbrain.akka.messages.*;
import com.zuehlke.fnf.masterbrain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/")
public class RestApiController {

    private Logger log = LoggerFactory.getLogger(RestApiController.class);

    @Autowired
    private MasterBrainService masterBrainService;
    @Autowired
    private TrackService trackService;
    @Autowired
    private PowerService powerService;
    @Autowired
    private TrackLoadingService trackLoadingService;
    @Autowired
    private PilotRegistry pilotRegistry;
    @Autowired
    private LapTimeService lapTimeService;
    @Autowired
    private PropertiesService propertiesService;

    @RequestMapping(value = "/ping", method = RequestMethod.GET, produces = "application/text")
    public String ping() {
        return "OK";
    }

    @RequestMapping(value = "/power", method = RequestMethod.GET, produces = "application/json")
    public Power getPower() {
        log.debug("getPower()");
        Power currentPower = powerService.getCurrentPower();
        if (currentPower == null) {
            throw new ResourceNotFoundException();
        }
        return currentPower;
    }

    @RequestMapping(value = "/power/{power}", method = RequestMethod.POST, produces = "application/json")
    public void setPower(@PathVariable int power) {
        log.debug("setPower({})", power);
        powerService.setPower(power);
    }

    @RequestMapping(value = "/track/powerprofile", method = RequestMethod.GET, produces = "application/json")
    public List<TrackWithPowerProfile.PowerSetting> getPowerProfile() {
        List<TrackWithPowerProfile.PowerSetting> profile = trackService.getCurrentPowerProfile();
        if (profile == null) {
            throw new ResourceNotFoundException();
        }
        return profile;
    }

    @RequestMapping(value = "/tracks/{track}", method = RequestMethod.POST, produces = "application/json")
    public String setTrack(@PathVariable String track) {
        log.debug("setTrack({})", track);
        if ("OK".equals(trackLoadingService.load(track))) {
            return "{ \"status\": \"Track loaded\" }";
        } else {
            throw new ResourceNotFoundException();
        }
    }

    @RequestMapping(value = "/tracks", method = RequestMethod.GET, produces = "application/json")
    public List<String> getTracks() {
        log.debug("getTracks()");
        return trackLoadingService.getTracks();
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST, produces = "application/json")
    public void reset() {
        log.debug("reset()");
        masterBrainService.reset();
    }

    @RequestMapping(value = "/track", method = RequestMethod.GET, produces = "application/json")
    public Track getCurrentTrack() {
        Track currentTrack = trackService.getCurrentTrack();
        if (currentTrack == null) {
            throw new ResourceNotFoundException();
        }
        return currentTrack;
    }

    @RequestMapping(value = "/pilots", method = RequestMethod.GET, produces = "application/json")
    public Collection<PilotConfig> getPilots() {
        List<PilotConfig> pilots = pilotRegistry.getPilots();
        Collections.sort(pilots, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        return pilots;
    }

    @RequestMapping(value = "/pilots/current", method = RequestMethod.GET, produces = "application/json")
    public PilotConfig getCurrentPilot() {
        PilotConfig config = pilotRegistry.getCurrenPilot();
        if (config == null) {
            throw new ResourceNotFoundException();
        }
        return config;
    }

    @RequestMapping(value = "/pilots", method = RequestMethod.PUT, produces = "application/json")
    public void setPilot(@RequestBody PilotRequest pilot) throws ClassNotFoundException {
        log.info("setPilot({}), {}", pilot, pilot.getPilot());
        pilotRegistry.activatePilot(pilot.getPilot());
    }

    @RequestMapping(value = "/pilots/current/properties", method = RequestMethod.PUT, produces = "application/json")
    public void setPilotProperties(@RequestBody PilotProperties properties) throws ClassNotFoundException {
        log.info("setPilotProperties({})", properties);
        propertiesService.setProperties(properties);

    }

    @RequestMapping(value = "/pilots/current/properties", method = RequestMethod.GET, produces = "application/json")
    public PilotProperties getPilotProperties() throws ClassNotFoundException {
        log.info("getPilotProperties({})");
        PilotProperties properties = propertiesService.getPilotProperties();
        if (properties == null) {
            throw new ResourceNotFoundException();
        }
        return properties;
    }

    @RequestMapping(value = "/laptimes", method = RequestMethod.GET, produces = "application/json")
    public List<LapTime> getLapTimes() {
        log.info("getLapTimes()");
        return lapTimeService.getLapTimes();
    }

    static class PilotRequest {
        private String pilot;

        public String getPilot() {
            return pilot;
        }

        public void setPilot(String pilot) {
            this.pilot = pilot;
        }
    }
}
