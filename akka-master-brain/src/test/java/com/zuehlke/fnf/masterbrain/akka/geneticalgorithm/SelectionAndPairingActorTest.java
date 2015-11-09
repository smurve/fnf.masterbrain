package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.JavaTestKit;
import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredPopulation;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.numberguess.NumberGuessingFitnessFunction;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.numberguess.NumberGuessingPairing;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Context;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.numberguess.NumberGuessingEvaluation;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 13.08.2015.
 */
public class SelectionAndPairingActorTest {

    @Rule
    public AkkaRule akka = AkkaRule.create();

    @Test
    public void testNewPopulationAfterSelectingIsBetter() {
        new JavaTestKit(akka.getSystem()) {{
            JavaTestKit terminationActor = akka.newProbe();
            JavaTestKit resultActor = akka.newProbe();

            Props props = Props.create(SelectionAndPairingActor.class);

            Configuration<Integer> configuration = new Configuration(6, null, NumberGuessingFitnessFunction.class, NumberGuessingPairing.class, null, new HashMap<>(), resultActor.getRef());
            Context context = new Context(null, null, null, terminationActor.getRef(), configuration);

            ActorRef subject = akka.actorOf(props);

            subject.tell(context, getRef());


            ScoredPopulation<Integer> scoredPopulation = new ScoredPopulation<>(Arrays.asList(
                    new ScoredGenom<>(100, 58),
                    new ScoredGenom<>(44, 2),
                    new ScoredGenom<>(19, 23),
                    new ScoredGenom<>(18, 24),
                    new ScoredGenom<>(41, 1),
                    new ScoredGenom<>(0, 42)
            ));
            Double sumScoreFirstGeneration = scoredPopulation.getPopulation().stream().mapToDouble((sg) -> sg.getScore()).sum();

            subject.tell(scoredPopulation, getRef());

            Population<Integer> newPopulation = terminationActor.expectMsgClass(Population.class);

            List<ScoredGenom<Integer>> collect = newPopulation.getPopulation().stream().map((i) -> new NumberGuessingEvaluation().evaluate(i, configuration)).collect(Collectors.toList());
            Double sumScoreSecondGeneration = collect.stream().mapToDouble((sg) -> sg.getScore()).sum();
            // because only the best survive and we don't do any mutation in this test, the second generation must be better
            assertThat(sumScoreSecondGeneration, is(lessThan(sumScoreFirstGeneration)));
        }};
    }
}
