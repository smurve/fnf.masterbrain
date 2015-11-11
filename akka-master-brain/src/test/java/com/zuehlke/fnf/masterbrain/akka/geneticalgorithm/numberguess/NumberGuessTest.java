package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.numberguess;

import akka.actor.ActorRef;
import akka.testkit.JavaTestKit;
import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.Configuration;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.GAActor;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 14.08.2015.
 */
public class NumberGuessTest {

    @Rule
    public AkkaRule akka = AkkaRule.create();

    @Test
    public void test() throws InterruptedException {
        new JavaTestKit(akka.getSystem()) {{

            JavaTestKit resultActor = akka.newProbe();
            Configuration<Integer> configuration = new Configuration(100, NumberGuessingEvaluation.class, NumberGuessingFitnessFunction.class, NumberGuessingPairing.class, NumberGuessingTermination.class, new HashMap<>(), resultActor.getRef(), getSystem().dispatcher());

            ActorRef geneticAlgorithm = akka.actorOf(GAActor.props(configuration));

            Population<Integer> initialPopulation = new Population<>(Arrays.asList(0, 100, 20, 88, 90, 70, 10, 25, 66, 77));
            geneticAlgorithm.tell(initialPopulation, ActorRef.noSender());


            Object[] result = resultActor.receiveN(11);
            Arrays.stream(result).forEach(System.out::println);
            ScoredGenom<Integer> finalScore = (ScoredGenom<Integer>) result[9];
            assertThat(finalScore.getScore(), is(lessThanOrEqualTo(2d)));
        }};
    }

}
