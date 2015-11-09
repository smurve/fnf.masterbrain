package com.zuehlke.fnf.masterbrain.akka.tho;

import com.zuehlke.fnf.masterbrain.akka.messages.Info;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;

/**
 * Created by tho on 07.08.2015.
 */
public interface PilotFeedback {
    void firePower(Power of);

    void fireInfo(Info info);

    void firePowerProfile(int[] powerValues);
}
