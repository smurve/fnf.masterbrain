package com.zuehlke.fnf.masterbrain.akka.messages;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Created by mhan on 17.07.2015.
 */
public class TrackPointsTest {

    @Test
    public void testGetNumberOfPointsForSectorIndex() throws Exception {
        TrackPoints from = TrackPoints.from(null, null, null, d(1, 1, 2, 2, 3, 3, 3, 4));
        assertEquals(2, from.getNumberOfPointsForSectorIndex(1));
        assertEquals(2, from.getNumberOfPointsForSectorIndex(2));
        assertEquals(3, from.getNumberOfPointsForSectorIndex(3));
        assertEquals(1, from.getNumberOfPointsForSectorIndex(4));
    }

    public double[] d(double... ds) {
        return ds;
    }
}