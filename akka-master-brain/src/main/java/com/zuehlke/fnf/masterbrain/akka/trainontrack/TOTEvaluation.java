package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by tho on 10.08.2015.
 */
public class TOTEvaluation implements Evaluation<TOTGenom> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TOTEvaluation.class);
    private static int counter;
    private int instance;
    private ScoreDao scoreDao = new ScoreDao();


    public TOTEvaluation() {
        instance = counter++;
    }

    @Override
    public ScoredGenom<TOTGenom> evaluate(TOTGenom genom, Configuration configuration) {
        LOGGER.debug("{}: genom: {}", instance, genom);
        boolean useCache = (Boolean) configuration.getCustomProperties().get(TOTPilotActor.KEY_USE_CACHE);
        scoreDao.setEnabled(useCache);
        ScoredGenom<TOTGenom> existing = scoreDao.loadExistingScore(genom, configuration);
        if (existing != null) {
            return existing;
        }

        ActorRef learner = (ActorRef) configuration.getCustomProperties().get(TOTPilotActor.KEY_LEARNER_REF);
        try {

            Timeout startTimeout = new Timeout(Duration.create(7, TimeUnit.DAYS));
            StartPermission permission = (StartPermission) Await.result(Patterns.ask(learner, StartPermission.ask, startTimeout), startTimeout.duration());
            if (StartPermission.terminate.equals(permission)) {
                LOGGER.debug("{}: Terminating", instance);
                return new ScoredGenom<>(genom, Double.MAX_VALUE);
            }
            LOGGER.debug("{}: start evaluation", instance);

            Timeout evaluationTimeout = new Timeout(Duration.create(30, TimeUnit.SECONDS));
            Future<Object> future = Patterns.ask(learner, genom, evaluationTimeout);
            ScoredGenom result = (ScoredGenom) Await.result(future, evaluationTimeout.duration());
            LOGGER.debug("{}: got a result. {}", instance, result);
            scoreDao.storeScore(result);
            return result;
        } catch (Exception e) {
            LOGGER.warn("{}: Evaluation timeout.", instance);
            learner.tell(StartPermission.terminate, ActorRef.noSender());
            return new ScoredGenom<>(genom, Double.MAX_VALUE);
        }
    }


}
