package com.zuehlke.fnf.masterbrain.akka.messages;

/**
 * TODO: currently located in simulator and brain project. Extract in some common package
 *
 * @author tho
 */
public class Power {

    private int value;

    public static Power of(int value) {
        Power power = new Power();
        power.setValue(value);
        return power;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Power[value=%s]", value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Power power = (Power) o;

        return value == power.value;

    }

    @Override
    public int hashCode() {
        return value;
    }
}
