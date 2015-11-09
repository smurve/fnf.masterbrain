package com.zuehlke.fnf.masterbrain.akka.tho;

import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackSegment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.zuehlke.fnf.masterbrain.akka.tho.SegmentType.curve;
import static com.zuehlke.fnf.masterbrain.akka.tho.SegmentType.straight;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by tho on 29.07.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationHandlerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationHandlerTest.class);
    @Rule
    public AkkaRule akka = AkkaRule.createWithConfig("masterbrain{" +
            " pilot{" +
            "  tho{" +
            "    lookaheadSegments=2," +
            "    offset=1," +
            "    breakPower=42," +
            "    maxPower=142," +
            "  }" +
            " }" +
            "}");
    @Mock
    private Track track;
    @Mock
    private Locations locations;
    @Mock
    private PilotFeedback feedback;

    @Test
    public void testLocationBeforeCurve() throws Exception {
        LocationHandler handler = new LocationHandler(akka.getConfig());
        handler.setSafetyHandler(new SafetyHandler(akka.getConfig()));

        List<TrackSegment> trackSegments = createTrackSegments(
                straight,
                straight,
                curve,
                straight,
                straight,
                curve);
        int locationIndex = 0;
        double[] ps = new double[trackSegments.size()];
        ps[locationIndex] = 1;
        when(locations.getPs()).thenReturn(ps);
        when(track.getSegments(anyInt())).thenReturn(trackSegments);
        // We're located at the first segment (straight)
        when(locations.getIndexWithHighestPropability()).thenReturn(locationIndex);
        // Segment indexes of interrest are now 1 (straight) and 2 (curve)

        handler.onNewTrack(track);
        handler.onNewLocation(locations, feedback);

        verify(feedback, times(1)).firePower(Matchers.eq(Power.of(42)));
    }

    @Test
    public void testLocationEndOfCurve() throws Exception {
        LocationHandler handler = new LocationHandler(akka.getConfig());
        handler.setSafetyHandler(new SafetyHandler(akka.getConfig()));

        List<TrackSegment> trackSegments = createTrackSegments(
                straight,
                straight,
                curve,
                curve,
                curve,
                straight,
                straight,
                curve);
        double[] ps = new double[trackSegments.size()];
        ps[1] = 1;
        ps[2] = 1;
        when(locations.getPs()).thenReturn(ps);
        when(track.getSegments(anyInt())).thenReturn(trackSegments);
        // We're located at the first curve segment
        when(locations.getIndexWithHighestPropability()).thenReturn(1).thenReturn(2);
        // Segment indexes of interrest are now 3 (straight) and 4 (straight)

        handler.onNewTrack(track);
        handler.onNewLocation(locations, feedback);
        verify(feedback, times(1)).firePower(eq(Power.of(42)));
        // no end of curve is coming. must accelerate
        handler.onNewLocation(locations, feedback);
        // 101 because handler is still in acceleration mode from value calculated at construction
        // up to 142.
        verify(feedback, times(1)).firePower(eq(Power.of(101)));
    }

    @Test
    public void testSegmentRolloverAtEndOfTrack() throws Exception {
        LocationHandler handler = new LocationHandler(akka.getConfig());
        handler.setSafetyHandler(new SafetyHandler(akka.getConfig()));

        List<TrackSegment> trackSegments = createTrackSegments(
                straight,
                straight,
                curve,
                straight,
                straight,
                curve);
        int locationIndex = 5;
        double[] ps = new double[trackSegments.size()];
        ps[locationIndex] = 1;
        when(locations.getPs()).thenReturn(ps);
        when(track.getSegments(anyInt())).thenReturn(trackSegments);
        // We're located at the last segment (curve)
        when(locations.getIndexWithHighestPropability()).thenReturn(locationIndex);
        // segments of interest are now 1 (straight) and 2 (straight)

        handler.onNewTrack(track);
        handler.onNewLocation(locations, feedback);

        verify(feedback, times(1)).firePower(eq(Power.of(100)));
    }

    @Test
    public void testFixSegments() throws Exception {
        akka.withConfig("masterbrain{" +
                " pilot{" +
                "  tho{" +
                "    lookaheadSegments=2," +
                "    offset=2," +
                "    breakPower=42," +
                "    maxPower=142," +
                "  }" +
                " }" +
                "}");
        LocationHandler handler = new LocationHandler(akka.getConfig());
        handler.setSafetyHandler(new SafetyHandler(akka.getConfig()));

        List<TrackSegment> trackSegments = createTrackSegments(
                straight,
                straight,
                curve,
                straight,
                straight,
                curve);
        int locationIndex = 0;
        double[] ps = new double[trackSegments.size()];
        ps[locationIndex] = 1;
        when(locations.getPs()).thenReturn(ps);
        when(track.getSegments(anyInt())).thenReturn(trackSegments);
        // We're located at the first segment
        when(locations.getIndexWithHighestPropability()).thenReturn(locationIndex);
        // With the offset of 2 we'll look at segments curve (2) and straight (3)

        handler.onNewTrack(track);
        handler.onNewLocation(locations, feedback);
        verify(feedback, times(1)).firePower(eq(Power.of(42)));

        List<SegmentType> actualSegments = handler.fixSegmentsBeforeLastBreak(1, feedback);
        LOGGER.info("actualSegments={}", actualSegments);
        List<SegmentType> expectedSegments = Arrays.asList(
                straight,
                curve,
                curve,
                straight,
                straight,
                curve
        );
        assertTrue(expectedSegments.equals(actualSegments));
    }

    private List<TrackSegment> createTrackSegments(SegmentType... types) {
        return Arrays.stream(types).map(type -> createTrackSegment(type)).collect(Collectors.toList());
    }

    private TrackSegment createTrackSegment(SegmentType type) {
        TrackSegment ts = new TrackSegment();
        ts.setType(type.toString());
        return ts;
    }
}
