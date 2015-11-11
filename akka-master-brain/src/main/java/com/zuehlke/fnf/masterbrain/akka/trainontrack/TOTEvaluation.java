package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.spi.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.Promise;
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
    public Future<ScoredGenom<TOTGenom>> evaluate(TOTGenom genom, Configuration configuration) {
        LOGGER.debug("{}: genom: {}", instance, genom);
        final Promise<ScoredGenom<TOTGenom>> promise = Futures.promise();

        boolean useCache = (Boolean) configuration.getCustomProperties().get(TOTPilotActor.KEY_USE_CACHE);
        scoreDao.setEnabled(useCache);
        ScoredGenom<TOTGenom> existing = scoreDao.loadExistingScore(genom, configuration);
        if (existing != null) {
            promise.success(existing);
        }

        if (!promise.isCompleted()) {
            ActorRef learner = (ActorRef) configuration.getCustomProperties().get(TOTPilotActor.KEY_LEARNER_REF);
            Timeout startTimeout = new Timeout(Duration.create(10, TimeUnit.MINUTES));
            Future<Object> future = Patterns.ask(learner, StartPermission.ask, startTimeout);
            future.andThen(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable throwable, Object o) throws Throwable {
                    if (throwable == null) {
                        StartPermission permission = (StartPermission) o;
                        if (StartPermission.terminate.equals(permission)) {
                            LOGGER.debug("{}: Terminating", instance);
                            promise.success(new ScoredGenom<>(genom, Double.MAX_VALUE));
                            return;
                        }


                        LOGGER.debug("{}: start evaluation", instance);

                        Timeout evaluationTimeout = new Timeout(Duration.create(30, TimeUnit.SECONDS));
                        Future<Object> future = Patterns.ask(learner, genom, evaluationTimeout);
                        future.andThen(new OnComplete<Object>() {
                            @Override
                            public void onComplete(Throwable throwable, Object o) throws Throwable {
                                if (throwable == null) {
                                    ScoredGenom result = (ScoredGenom) o;
                                    LOGGER.debug("{}: got a result. {}", instance, result);
                                    scoreDao.storeScore(result);
                                    promise.success(result);
                                } else {
                                    LOGGER.info("Evaulation failed.", throwable);
                                }
                            }
                        }, configuration.getExecutionContextExecutor());

                        return;
                    } else {
                        LOGGER.warn("{}: Evaluation timeout.", instance);
                        learner.tell(StartPermission.terminate, ActorRef.noSender());
                        promise.success(new ScoredGenom<>(genom, Double.MAX_VALUE));
                    }
                }
            }, configuration.getExecutionContextExecutor());
        }
        return promise.future();
    }


}
