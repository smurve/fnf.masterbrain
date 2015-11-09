package com.zuehlke.fnf.masterbrain.akka.messages;

/**
 * Created by tho on 10.07.2015.
 */
public class TrackBuildingState {

    private int n;

    private int of;

    public static TrackBuildingState from(int n, int of) {
        TrackBuildingState state = new TrackBuildingState();
        state.setN(n);
        state.setOf(of);
        return state;
    }

    public int getOf() {
        return of;
    }

    public void setOf(int of) {
        this.of = of;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public boolean isDone() {
        return n >= of;
    }
}
