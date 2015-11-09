package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mhan on 06.07.2015.
 */
public class Track {
    private Status status;
    private List<Straight> straights;
    private List<Curve> curves;
    private TrackPoints trackPoints;

    @JsonIgnore
    private double trackLength;

    public List<Straight> getStraights() {
        return straights;
    }

    public void setStraights(final List<Straight> straights) {
        this.straights = straights;
    }

    public List<Curve> getCurves() {
        return curves;
    }

    public void setCurves(final List<Curve> curves) {
        this.curves = curves;
    }

    public double getTrackLength() {
        return trackLength;
    }

    public void compoundTrackParts() {
        if (straights.size() != curves.size() + 1) {
            throw new IllegalStateException(String.format("can not handle track with %d straights and %d curves",
                    straights.size(), curves.size()));
        }
        connectStraightsWithNextCurve();
        connectCurvesWithNextStraight();
        connectFirstWithLastStraight();
    }

    private void connectFirstWithLastStraight() {
        Straight lastStraight = straights.get(straights.size() - 1);
        Straight firstStraight = straights.get(0);
        lastStraight.setNextTrackPart(firstStraight);
        firstStraight.setPreviousTrackPart(lastStraight);
    }

    private void connectCurvesWithNextStraight() {
        for (int i = 0; i < curves.size(); i++) {
            Curve c = curves.get(i);
            Straight nextStraight = straights.get(i + 1);
            c.setNextTrackPart(nextStraight);
            nextStraight.setPreviousTrackPart(c);
        }
    }

    private void connectStraightsWithNextCurve() {
        for (int i = 0; i < straights.size() - 1; i++) {
            Straight s = straights.get(i);
            Curve nextCurve = curves.get(i);
            s.setNextTrackPart(nextCurve);
            nextCurve.setPreviousTrackPart(s);
            nextCurve.setSector_ind(s.getSector_ind()[0] + 1);
        }
    }

    public void preparePhysics() {
        curves.forEach(Curve::calculateRadius);
        curves.forEach(Curve::calculateTrackPartLength);
        straights.forEach(Straight::calculateTrackPartLength);
        trackLength = straights.stream().mapToDouble(TrackPart::getTrackPartLenghth).sum() +
                curves.stream().mapToDouble(TrackPart::getTrackPartLenghth).sum();
    }

    public List<TrackSegment> getSegments(int numberOfSegments) {
        int trackParts = straights.size() + curves.size();
        if (numberOfSegments < trackParts) {
            throw new IllegalArgumentException("Number of segments should be at least " + trackParts);
        }
        final List<TrackSegment> segments = new ArrayList<>();

        List<Curve> shallow_curves = new ArrayList<>(curves);
        List<Straight> shallow_straights = new ArrayList<>(straights);

        shallow_curves.forEach(c -> {
            segments.add(c.buildCurveSegment());
        });

        if (numberOfSegments > trackParts) {
            List<TrackSegment> straightSegments = buildMultipleSegmentsPerStraight(numberOfSegments, shallow_curves, shallow_straights);
            segments.addAll(straightSegments);
        } else {
            shallow_straights.forEach(s -> {
                segments.addAll(s.buildStraightSegments(1));
            });
        }
        segments.sort((s1, s2) -> s1.getSector_ind() - s2.getSector_ind());
        return segments;
    }

    private List<TrackSegment> buildMultipleSegmentsPerStraight(int numberOfSegments, List<Curve> shallow_curves, List<Straight> shallow_straights) {
        shallow_straights.sort((s1, s2) -> Double.compare(s2.trackPartLenghth, s1.trackPartLenghth));
        Map<Straight, AtomicInteger> partsPerStraight = new HashMap<>();
        for (int i = 0; i < numberOfSegments - shallow_curves.size(); i++) {
            int index = i % shallow_straights.size();
            Straight s = shallow_straights.get(index);
            if (partsPerStraight.containsKey(s)) {
                partsPerStraight.get(s).incrementAndGet();
            } else {
                partsPerStraight.put(s, new AtomicInteger(1));
            }
        }
        final List<TrackSegment> straightSegments = new ArrayList<>();
        partsPerStraight.entrySet().forEach(e -> straightSegments.addAll(e.getKey().buildStraightSegments(e.getValue().get())));
        return straightSegments;
    }

    public TrackPoints getTrackPoints() {
        return trackPoints;
    }

    public void setTrackpoints(TrackPoints trackpoints) {
        this.trackPoints = trackpoints;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
