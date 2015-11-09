package com.zuehlke.fnf.masterbrain.akka;

import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Helper that handles null ActorRefs. You can decide how to react if ActorRef is null.
 * Created by tho on 10.08.2015.
 */
public class Publisher {

    private static final Logger LOG = LoggerFactory.getLogger(Publisher.class);
    private final Object message;
    private final ActorRef sender;
    private ActorRef target;
    private String logMessage;
    private BiConsumer<String, Object> logFunction;

    public Publisher(Object message, ActorRef target, ActorRef sender) {
        this.message = message;
        this.target = target;
        this.sender = sender;
    }

    public static Publisher tell(Object message, ActorRef target, ActorRef sender) {
        return new Publisher(message, target, sender);
    }

    public LogBehaviour onMissingSender() {
        return new LogBehaviour();
    }

    public class LogBehaviour {
        public FailBehaviour ignore() {
            logFunction = (msg, obj) -> {
            };
            return new FailBehaviour();
        }

        public FailBehaviour logDebug(String logMessage) {
            return log(logMessage, LOG::debug);
        }

        public FailBehaviour logInfo(String logMessage) {
            return log(logMessage, LOG::info);

        }

        public FailBehaviour logWarn(String logMessage) {
            return log(logMessage, LOG::warn);

        }

        public FailBehaviour logError(String logMessage) {
            return log(logMessage, LOG::error);

        }

        private FailBehaviour log(String logMessage, BiConsumer<String, Object> logFunction) {
            Publisher.this.logMessage = logMessage;
            Publisher.this.logFunction = logFunction;
            return new FailBehaviour();
        }
    }

    public class FailBehaviour {
        public void andReturn() {
            executeAndLog();
        }

        public void andFail() {
            executeAndLog();
            throw new RuntimeException(logMessage);
        }

        private void executeAndLog() {
            if (target == null) {
                LogMsg logMsg = logMessage == null ? new LogMsg(sender, "No ActorRef") : new LogMsg(sender, logMessage);
                logFunction.accept("{}", logMsg);
            } else {
                target.tell(message, sender);
            }

        }
    }

    private class LogMsg {

        private final ActorRef sender;
        private final String logMessage;

        public LogMsg(ActorRef sender, String logMessage) {
            this.sender = sender;
            this.logMessage = logMessage;
        }

        @Override
        public String toString() {
            return String.format("sender=%s, logMessage=%s", sender, logMessage);
        }
    }
}
