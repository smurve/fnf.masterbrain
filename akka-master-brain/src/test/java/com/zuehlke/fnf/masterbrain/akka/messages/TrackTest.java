package com.zuehlke.fnf.masterbrain.akka.messages;

import com.google.common.io.CharStreams;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by mhau on 09.07.2015.
 */
public class TrackTest {

    private static Track track;

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream is = TrackTest.class.getResourceAsStream("/testtrack.json");
        String trackString = CharStreams.toString(new InputStreamReader(is));
        track = TrackMapper.fromJSON(trackString);
        track.compoundTrackParts();
        track.preparePhysics();
    }

    @Test
    public void trackPartsCompounded() {
        List<Curve> curves = track.getCurves();
        List<Straight> straights = track.getStraights();

        curves.forEach(c -> {
            assertTrue(c.getNextTrackPart() != null);
            assertTrue(c.getPreviousTrackPart() != null);
        });

        straights.forEach(s -> {
            assertTrue(s.getNextTrackPart() != null);
            assertTrue(s.getPreviousTrackPart() != null);
        });
    }

    @Test
    public void trackLengthCalculated() {
        assertEquals(34.74, track.getTrackLength(), 0.1);
    }

    @Test
    public void trackSegmentation() {
        List<TrackSegment> segments = track.getSegments(14);
        assertEquals(14, segments.size());
    }
}