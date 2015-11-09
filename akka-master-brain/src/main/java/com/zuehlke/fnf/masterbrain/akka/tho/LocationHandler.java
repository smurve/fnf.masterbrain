package com.zuehlke.fnf.masterbrain.akka.tho;

import com.typesafe.config.Config;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.zuehlke.fnf.masterbrain.akka.config.ConfigHelper.readValue;

/**
 * Decides what to do if we get a new localization. It looks at the segments that lie ahead of the current location.
 * By configuration you can define:
 * - how many segments to look at
 * - offset of these segments from the current location (remember, that we're always a little bit behind the actual position)
 * - maximum speed
 * - speed during braking
 * <p/>
 * Created by tho on 29.07.2015.
 */
public class LocationHandler {
    public static final String PROPERTY_LOOKAHEAD_SEGMENTS = "masterbrain.pilot.tho.lookaheadSegments";
    public static final String PROPERTY_MAX_POWER = "masterbrain.pilot.tho.maxPower";
    public static final String PROPERTY_BREAK_POWER = "masterbrain.pilot.tho.breakPower";
    public static final String PROPERTY_OFFSET = "masterbrain.pilot.tho.offset";
    public static final int DEFAULT_LOOKAHEAD_SEGMENTS = 20;
    public static final int DEFAULT_MAX_POWER = 255;
    public static final int DEFAULT_BREAK_POWER = 155;
    public static final int DEFAULT_OFFSET = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationHandler.class);
    private SegmentType[] segments;
    private Track track;
    private SafetyHandler safetyHandler;

    private int lastBreakSegmentIndex = -1;
    private long lastBreakTime;
    private int lookaheadSegments;
    private int maxPower;
    private int currentMaxPower;
    private int offset;
    private int breakPower;
    private int lastCurvesToCome;

    public LocationHandler(Config config) {
        lookaheadSegments = readValue(PROPERTY_LOOKAHEAD_SEGMENTS, config::getInt, DEFAULT_LOOKAHEAD_SEGMENTS);
        maxPower = readValue(PROPERTY_MAX_POWER, config::getInt, DEFAULT_MAX_POWER);
        currentMaxPower = (int) (maxPower * 0.7);
        breakPower = readValue(PROPERTY_BREAK_POWER, config::getInt, DEFAULT_BREAK_POWER);
        offset = readValue(PROPERTY_OFFSET, config::getInt, DEFAULT_OFFSET);
    }

    public void onNewLocation(Locations message, PilotFeedback feedback) {
        safetyHandler.checkAndContinue((o) -> handleLocation(message, feedback));
    }

    private void handleLocation(Locations message, PilotFeedback feedback) {
        initSegmentsIfNeeded(message, feedback);
        if (segments == null) {
            LOGGER.info("segments not ready, yet.");
            return;
        }

        int segmentIndex = message.getIndexWithHighestPropability();
        if (segmentIndex < 0) {
            LOGGER.info("segmentIndex={}", segmentIndex);
            return;
        }
        double p = message.getPs()[segmentIndex];
        safetyHandler.checkProbabiltyAndContinue(
                p,
                feedback,
                (f) -> lookahead(segmentIndex, false, f),
                (x) -> currentMaxPower = (int) (maxPower * 0.8)
        );
    }

    private void lookahead(int segmentIndex, boolean simulation, PilotFeedback feedback) {
        if (!simulation) {
            accelerateToMaxPower();
        }
        int[] lookaheadIndexes = extractSegmentIndexesOfInterest(segmentIndex);
        int curvesAhead = detectCurvesAndBreakPosition(lookaheadIndexes);
        int power = calculatePower(curvesAhead, simulation);
        feedback.firePower(Power.of(power));
    }

    private int calculatePower(int curvesToCome, boolean simulation) {
        int power = simulation ? maxPower : currentMaxPower;
        if (curvesToCome > 0) {

            if (lastCurvesToCome > 1 && curvesToCome < 5) {
                LOGGER.debug("curvesToCome={}", curvesToCome);
                power = simulation ? maxPower : currentMaxPower; //breakPower + 20;
                LOGGER.debug("curve ending... power={}", power);
            } else if (lastCurvesToCome > 1 && curvesToCome < 10) {
                power = (maxPower - breakPower) / 3 + breakPower;
                LOGGER.info("curve end is near. power={}", power);
            } else {
                power = breakPower;
            }
            lastCurvesToCome = curvesToCome;
        } else {
            lastCurvesToCome = 0;
        }
        return power;
    }

    private int detectCurvesAndBreakPosition(int[] lookaheadIndexes) {
        int curvesAhead = 0;
        int firstBreakSegment = -1;
        for (int i = 0; i < lookaheadIndexes.length; i++) {

            SegmentType s = segments[lookaheadIndexes[i]];
            if (SegmentType.curve.equals(s)) {
                curvesAhead++;
                if (firstBreakSegment < 0) {
                    lastBreakTime = System.currentTimeMillis();
                    firstBreakSegment = lookaheadIndexes[i] - 1;
                    //LOGGER.info("set lastBreakSegmentIndex={}", lastBreakSegmentIndex);
                }
            } else {
                //LOGGER.info("");
            }
        }
        if (curvesAhead > 0) {
            lastBreakSegmentIndex = firstBreakSegment;
        }
        return curvesAhead;
    }

    private int[] extractSegmentIndexesOfInterest(int segmentIndex) {
        int[] lookaheadIndexes = new int[lookaheadSegments];
        for (int i = 0; i < lookaheadSegments; i++) {
            int idx = offset + segmentIndex + i;
            idx = idx % segments.length;
            if (idx < -0) {
                idx = segments.length + idx;
            }
            lookaheadIndexes[i] = idx;
            LOGGER.debug("idx={}", idx);
        }
        return lookaheadIndexes;
    }

    private void accelerateToMaxPower() {
        if (currentMaxPower < maxPower) {
            currentMaxPower++;
        }
    }

    private void initSegmentsIfNeeded(Locations message, PilotFeedback feedback) {
        if (track == null) {
            LOGGER.info("No track, yet");
            return;
        }
        if (segments == null) {
            List<TrackSegment> trackSegments = track.getSegments(message.getPs().length);

            segments = trackSegments.stream().map(
                    (s) -> Curve.TYPE.equals(s.getType()) ? SegmentType.curve : SegmentType.straight
            ).collect(Collectors.toList()).toArray(new SegmentType[trackSegments.size()]);

            LOGGER.info("{} TrackSegments: {}", segments.length, Arrays.asList(segments));

            createPowerProfile(feedback);
        }
    }

    private void createPowerProfile(PilotFeedback feedback) {
        final List<Integer> simulation = new ArrayList<>();
        for (int i = 0; i < segments.length; i++) {
            lookahead(i, true, new PilotFeedback() {
                @Override
                public void firePower(Power power) {
                    simulation.add(power.getValue());
                }

                @Override
                public void fireInfo(Info info) {
                    // ignore
                }

                @Override
                public void firePowerProfile(int[] powerValues) {
                    // ignore
                }
            });
        }
        int[] simData = ArrayUtils.toPrimitive(simulation.toArray(new Integer[simulation.size()]));
        feedback.firePowerProfile(simData);
    }

    public void onNewTrack(Track message) {
        LOGGER.info("Got a new track");
        reset();
        track = message;

    }

    public void reset() {
        track = null;
        lastBreakSegmentIndex = -1;
        segments = null;
    }

    public void setSafetyHandler(SafetyHandler safetyHandler) {
        this.safetyHandler = safetyHandler;
    }


    /**
     * Fixes track segments that lie in front of the segment at which we were braking the last time.
     * It will reduce the current maximum power to prevent rapid speed changes in the current curve. Maximum power
     * will be restored after a few location updates.
     *
     * @param numToFix
     * @return
     */
    public List<SegmentType> fixSegmentsBeforeLastBreak(int numToFix, PilotFeedback feedback) {
        if (segments == null) {
            return Collections.emptyList();
        }
        LOGGER.info("lastBreakSegmentIndex={}", lastBreakSegmentIndex);
        currentMaxPower = (int) (maxPower * 0.7);
        if (System.currentTimeMillis() - lastBreakTime > 500) {
            LOGGER.info("Last break was too long ago. Won't fixing anything.");
            return Arrays.asList(segments);
        }
        if (lastBreakSegmentIndex < 0) {
            LOGGER.info("Never hit the breaks so far.");
            return Arrays.asList(segments);
        }

        LOGGER.info("Marking {} segments as curve at segment {}", numToFix, lastBreakSegmentIndex);
        for (int i = 0; i < numToFix; i++) {
            int idx = lastBreakSegmentIndex - i % segments.length;
            LOGGER.debug("Marking segment {} as curve.", idx);
            segments[idx] = SegmentType.curve;
        }
        createPowerProfile(feedback);
        return Arrays.asList(segments);
    }

    public Track getTrack() {
        return track;
    }
}