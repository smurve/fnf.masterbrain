package com.zuehlke.fnf.masterbrain.akka.messages;

import akka.actor.ActorRef;

/**
 * Created by tho on 23.07.2015.
 */
public class Pilot {

    private ActorRef actorRef;

    private PilotConfig config;

    public Pilot(ActorRef pilot, PilotConfig config) {
        this.actorRef = pilot;
        this.config = config;
    }

    public static Pilot of(ActorRef pilotRef) {
        return new Pilot(pilotRef, null);
    }

    public static Pilot of(PilotConfig config) {
        return new Pilot(null, config);
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public void setActorRef(ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    public PilotConfig getConfig() {
        return config;
    }

    public void setConfig(PilotConfig config) {
        this.config = config;
    }
}
