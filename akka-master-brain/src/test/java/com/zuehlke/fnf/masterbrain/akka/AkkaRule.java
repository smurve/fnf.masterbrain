package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tho on 23.07.2015.
 */
public class AkkaRule extends ExternalResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaRule.class);
    private String config;
    private ActorSystem system;

    private AkkaRule(String config) {
        this.config = config;
    }

    public static AkkaRule createWithConfig(String configuration) {
        return new AkkaRule(configuration);
    }

    public static AkkaRule create() {
        return new AkkaRule(null);
    }

    @Override
    protected void before() throws Throwable {
        LOGGER.info("Creating akka system.");
        if (config == null) {
            system = ActorSystem.create();
        } else {
            system = ActorSystem.create("akka", ConfigFactory.parseString(config));
        }
    }

    @Override
    protected void after() {
        LOGGER.info("Killing akka system.");
        JavaTestKit.shutdownActorSystem(system);
    }

    public ActorSystem getSystem() {
        return system;
    }

    public Config getConfig() {
        return system.settings().config();
    }

    public JavaTestKit newProbe() {
        return new JavaTestKit(system);
    }

    public ActorRef actorOf(Props props, String name) {
        return system.actorOf(props, name);
    }

    public ActorRef actorOf(Props props) {
        return system.actorOf(props);
    }

    public void withConfig(String config) {
        try {
            after();
            this.config = config;
            before();
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
