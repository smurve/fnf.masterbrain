package com.zuehlke.fnf.masterbrain.akka.messages;

import com.google.common.collect.ForwardingQueue;

public class SensorDataRange {

    private SensorData[] sensorEvents;
    private TimeDelta delta;

    public SensorDataRange() {

    }

    public SensorDataRange(SensorData[] data, TimeDelta delta) {
        this.sensorEvents = data;
        this.delta = delta;
    }

    public static SensorDataRange from(ForwardingQueue<SensorData> queue, TimeDelta delta) {
        return new SensorDataRange(queue.toArray(new SensorData[0]), delta);
    }

    public SensorData[] getSensorEvents() {
        return sensorEvents;
    }

    public TimeDelta getDelta() {
        return delta;
    }
}
