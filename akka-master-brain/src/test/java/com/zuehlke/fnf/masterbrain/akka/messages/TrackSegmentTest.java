package com.zuehlke.fnf.masterbrain.akka.messages;

import org.junit.*;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by mhan on 11.07.2015.
 */
public class TrackSegmentTest {

    @Test
    public void testDriveForBadSegment() throws IOException {
        TrackSegment trackSegment = new TrackSegment();
        trackSegment.setSector_ind(4);
        trackSegment.setV0(166.78755);
        trackSegment.setLength(376.99112 / 120);
        trackSegment.setAccelerationFactor(Curve.CURVE_ACCELERATION_FACTOR);
        trackSegment.drive(53.47004);
        assertTrue(trackSegment.getTransitTime() > 0);
    }
}