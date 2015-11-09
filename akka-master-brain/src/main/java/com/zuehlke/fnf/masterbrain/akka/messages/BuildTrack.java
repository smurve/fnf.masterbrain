package com.zuehlke.fnf.masterbrain.akka.messages;

/**
 * Created by tho on 09.07.2015.
 */
public class BuildTrack {

    private SensorDataRange data;

    public static BuildTrack from(SensorDataRange data) {
        BuildTrack o = new BuildTrack();
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
