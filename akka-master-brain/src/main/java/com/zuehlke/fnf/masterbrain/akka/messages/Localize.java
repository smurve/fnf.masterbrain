package com.zuehlke.fnf.masterbrain.akka.messages;

public class Localize {

    private SensorDataRange data;

    public static Localize from(SensorDataRange data) {
        Localize o = new Localize();
        o.setData(data);
        return o;
    }

    public SensorDataRange getData() {
        return data;
    }

    public void setData(SensorDataRange data) {
        this.data = data;
    }
}
