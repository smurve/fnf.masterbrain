package com.zuehlke.fnf.masterbrain.akka.tho;

import com.zuehlke.carrera.relayapi.messages.PenaltyMessage;
import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by tho on 29.07.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class SafetyHandlerTest {

    @Rule
    public AkkaRule akka = AkkaRule.createWithConfig("masterbrain{" +
            " pilot{" +
            "  tho{" +
            "    segmentsToFixOnPenalty=10," +
            "    powerDuringPenalty=120," +
            "    safetyCarDuration=500," +
            "    safetyModeAfterMillis=50," +
            "  }" +
            " }" +
            "}");
    @Mock
    private PilotFeedback feedback;

    @Mock
    private BiConsumer<Integer, PilotFeedback> fixSegments;

    @Mock
    private Consumer<Void> onSuccess;
    @Mock
    private Consumer<PilotFeedback> onSuccessWithFeedback;

    @Mock
    private Consumer<Void> cutMaxPower;

    @Test
    public void testThatHandlePenaltySetsTheConfiguredSpeed() {
        PenaltyMessage penalty = new PenaltyMessage();

        SafetyHandler handler = new SafetyHandler(akka.getConfig());
        handler.handlePenalty(penalty, fixSegments, feedback);

        verify(feedback, times(1)).firePower(eq(Power.of(120)));
    }


    @Test
    public void testThatHandlePenaltyFixesTheConfiguredNumberOfSegments() {
        PenaltyMessage penalty = new PenaltyMessage();

        SafetyHandler handler = new SafetyHandler(akka.getConfig());
        handler.handlePenalty(penalty, fixSegments, feedback);

        verify(fixSegments, times(1)).accept(eq(10), any(PilotFeedback.class));
    }

    @Test
    public void testCheckAndContinue() throws Exception {
        SafetyHandler handler = new SafetyHandler(akka.getConfig());
        handler.handlePenalty(new PenaltyMessage(), fixSegments, feedback);

        // we are in the penalty time in which we run in safety mode
        handler.checkAndContinue(onSuccess);
        verifyNoMoreInteractions(onSuccess);

        Thread.sleep(1000);
        // now safety mode is off
        handler.checkAndContinue(onSuccess);
        verify(onSuccess, times(1)).accept(null);
    }

    @Test
    public void testCheckSafetySlowDownIfLongTimeDiff() throws Exception {
        SafetyHandler handler = new SafetyHandler(akka.getConfig());
        handler.checkProbabiltyAndContinue(1.0, feedback, onSuccessWithFeedback, cutMaxPower);
        verifyNoMoreInteractions(feedback);
        handler.checkSafety(feedback);
        verifyNoMoreInteractions(feedback);
        Thread.sleep(60);
        handler.checkSafety(feedback);
        verify(feedback, times(1)).firePower(eq(Power.of(120)));
    }

    @Test
    public void testCheckProbabiltyAndContinueSlowsDownOnLowProbability() throws Exception {
        SafetyHandler handler = new SafetyHandler(akka.getConfig());
        handler.checkProbabiltyAndContinue(1.0, feedback, onSuccessWithFeedback, cutMaxPower);
        verifyNoMoreInteractions(feedback);
        handler.checkProbabiltyAndContinue(0.0, feedback, onSuccessWithFeedback, cutMaxPower);
        verify(feedback, times(1)).firePower(eq(Power.of(120)));
    }

}
