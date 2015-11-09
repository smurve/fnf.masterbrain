package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by tho on 11.08.2015.
 */
public class Perceptron {

    private final double w0;
    private RealVector genom;

    public Perceptron(double w0, double[] genom) {
        this.w0 = w0;
        this.genom = new ArrayRealVector(genom);
    }

    public double predict(double[] features) {
        RealVector featureVector = new ArrayRealVector(features);
        double s = genom.dotProduct(featureVector);
        return w0 + s;
    }
}
