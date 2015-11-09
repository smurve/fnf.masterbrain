package com.zuehlke.fnf.masterbrain.akka.trainontrack;

import java.util.Arrays;

/**
 * Created by tho on 11.08.2015.
 */
public class TOTGenom {

    private double[] values;


    public TOTGenom() {
        values = new double[0];
    }

    public TOTGenom(double[] values) {
        this.values = values;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "values=" + Arrays.toString(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TOTGenom lotGenom = (TOTGenom) o;

        return Arrays.equals(values, lotGenom.values);

    }

    @Override
    public int hashCode() {
        return values != null ? Arrays.hashCode(values) : 0;
    }
}
