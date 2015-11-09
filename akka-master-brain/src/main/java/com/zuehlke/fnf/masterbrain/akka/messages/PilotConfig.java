package com.zuehlke.fnf.masterbrain.akka.messages;

/**
 * Created by tho on 23.07.2015.
 */
public class PilotConfig {

    public static final boolean ACCEPT_MANUAL_POWER_UPDATES = true;

    private String name;
    private String actorClass;
    private String description;
    private boolean acceptManualPowerUpdates;

    private PilotConfig(String name, String actorClass, String description, boolean acceptManualPowerUpdates) {
        this.name = name;
        this.actorClass = actorClass;
        this.description = description;
        this.acceptManualPowerUpdates = acceptManualPowerUpdates;
    }

    public static PilotConfig create(String name, Class actorClass, String description) {
        return new PilotConfig(name, actorClass.getName(), description, false);
    }

    public static PilotConfig create(String name, Class actorClass, String description, boolean acceptManualPowerUpdates) {
        return new PilotConfig(name, actorClass.getName(), description, acceptManualPowerUpdates);
    }

    public static PilotConfig create(String name, String actorClass, String description, boolean acceptManualPowerUpdates) {
        return new PilotConfig(name, actorClass, description, acceptManualPowerUpdates);
    }

    public String getActorClass() {
        return actorClass;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public boolean isAcceptManualPowerUpdates() {
        return acceptManualPowerUpdates;
    }

    @Override
    public String toString() {
        return "PilotEntry{" +
                "name='" + name + '\'' +
                ", actorClass='" + actorClass + '\'' +
                ", description='" + description + '\'' +
                ", acceptManualPowerUpdates=" + acceptManualPowerUpdates +
                '}';
    }
}
