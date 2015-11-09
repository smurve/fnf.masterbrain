package com.zuehlke.fnf.masterbrain.akka.manual;

import com.zuehlke.fnf.masterbrain.akka.messages.Locations;
import com.zuehlke.fnf.masterbrain.akka.trainontrack.SpeedCalculator;
import com.zuehlke.fnf.masterbrain.akka.trainontrack.TOTGenom;
import com.zuehlke.fnf.masterbrain.akka.trainontrack.FeatureExtraxtor;

/**
 * Created by tho on 02.10.2015.
 */
public class FixedSpeedCalculator extends SpeedCalculator {

    private int fixedSpeed;

    public FixedSpeedCalculator(FeatureExtraxtor featureExtraxtor, int fixedSpeed) {
        super(featureExtraxtor, new TOTGenom(new double[]{0d, 1d, 2d, 3d, 4d, 5d}), 255, 0);
        this.fixedSpeed = fixedSpeed;
    }

    @Override
    public int calculateSpeed(Locations locations) {
        return fixedSpeed;
    }
}
