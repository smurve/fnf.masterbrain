package com.zuehlke.fnf.masterbrain.akka.geneticalgorithm;

import akka.actor.*;
import akka.japi.Pair;
import akka.testkit.TestKit;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.backpack.*;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.Population;
import com.zuehlke.fnf.masterbrain.akka.geneticalgorithm.messages.ScoredGenom;
import org.junit.*;
import scala.concurrent.duration.Duration;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by mhan on 09.07.2015.
 */
public class GeneticAlgorithmTest extends TestKit {
    private static final Random RANDOM = new Random();

    public GeneticAlgorithmTest() {
        super(ActorSystem.create("GeneticAlgorithm"));
    }

    @Test
    public void testBackPack() throws InterruptedException {


        Inbox inbox = Inbox.create(system());
        ActorRef callback = system().actorOf(Props.create(ResultActor.class));

        Configuration<BigInteger> config = new Configuration<>(28, KnapsackEvaluation.class, KnapsackFitnessFunction.class, KnapsackPairingAndMutation.class, KnapsackTermination
                .class, null, callback, system().dispatcher());
        ActorRef geneticAlgorithm = system().actorOf(GAActor.props(config));
        geneticAlgorithm.tell(createInitialPopulation(), ActorRef.noSender());

        // result
        inbox.send(callback, Item.A);

        Object receive = inbox.receive(Duration.create(4, TimeUnit.SECONDS));
        System.out.println("WAAAIT" +receive);
    }

    private Population<BigInteger> createInitialPopulation() {
        List<BigInteger> newGenoms = new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            newGenoms.add(randomKnapsack());
        }
        return new Population<>(newGenoms);
    }

    private BigInteger randomKnapsack() {
        BitSet bitSet = new BitSet();
        for (Item item : Item.values()) {
            bitSet.set(item.ordinal(), RANDOM.nextBoolean());
        }
        byte[] bytes = bitSet.toByteArray();
        if (bytes.length == 0) {
            return BigInteger.valueOf(2 >> Item.values().length - 1);
        }
        return Functions.toBiginteger.apply(bitSet);
    }

    public static class ResultActor extends UntypedActor {

        private ScoredGenom highestGuy = null;
        private ActorRef sender;

        @Override
        public void onReceive(final Object o) throws Exception {
            if (o instanceof Item) {
                sender = getSender();
            }
            if (o instanceof ScoredGenom) {
                ScoredGenom genom = (ScoredGenom) o;
                if (highestGuy == null || highestGuy.compareTo(genom) < 0) {
                    highestGuy = genom;
                    System.out.println(highestGuy);
                }
            } else if (o instanceof Population) {
                System.out.println(o);
                sender.tell(new Pair<>(o, highestGuy), getSelf());
            }
        }
    }
}