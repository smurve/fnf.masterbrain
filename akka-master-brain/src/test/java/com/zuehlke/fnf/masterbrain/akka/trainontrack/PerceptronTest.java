package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import org.junit.Test;

import java.util.Random;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
/**
 * Created by tho on 11.08.2015.
 */
public class PerceptronTest {

    @Test
    public void testPredictZero() {
        Perceptron perceptron = new Perceptron(50, new double[]{0,0,0});
        double actual = perceptron.predict(new double[]{1000, 2000, 3000});
        assertThat(actual, is(50d));
    }

    @Test
    public void testPredict() {
        Perceptron perceptron = new Perceptron(50, new double[]{-0.1,0.1,0.1});
        double actual = perceptron.predict(new double[]{1000, 2000, 3000});
        assertThat(actual, is(450d));
    }

    @Test
    public void test() {

        Random r = new Random();

        double[] features = new double[]{3825.373524687478, 3694.0020766797115, 3585.8156028368794};

        double min = -1;
        double max = 1;
        double[] genom = new double[3];

        for (int x = 0; x < 100; x++) {
            for (int i = 0; i < genom.length; i++) {
                genom[i] = (r.nextDouble() * (max - min)) + min;
            }

            Perceptron perceptron = new Perceptron(50, genom);
            double speed = perceptron.predict(features);
            if (speed > 0 && speed < 256) {
                System.out.println(speed);
            }
        }

    }
}
