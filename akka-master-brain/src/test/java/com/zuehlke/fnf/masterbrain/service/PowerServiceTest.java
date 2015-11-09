package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActor;
import com.zuehlke.fnf.masterbrain.akka.messages.GetPower;
import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.messages.Power;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by tho on 23.07.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class PowerServiceTest {

    @Rule
    public AkkaRule akka = AkkaRule.create();
    @Mock
    private MasterBrainService masterBrainService;
    @InjectMocks
    private PowerService service;

    @Test
    public void testGetCurrentPower() {

        JavaTestKit pilot = akka.newProbe();

        when(masterBrainService.getPilot()).thenReturn(pilot.getRef());

        new JavaTestKit(akka.getSystem()) {{
            // Mock behaviour of pilot
            pilot.setAutoPilot(new TestActor.AutoPilot() {
                public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                    if (msg instanceof GetPower) {
                        sender.tell(Power.of(42), ActorRef.noSender());
                    }
                    return noAutoPilot();
                }
            });

            // Now ask for the Power
            Power actualPower = service.getCurrentPower();
            assertThat(actualPower, notNullValue());
            assertThat(actualPower.getValue(), is(42));
        }};
    }

    @Test
    public void testGetCurrentPowerWhenPilotDoesNotAnswer() {

        JavaTestKit pilot = akka.newProbe();

        when(masterBrainService.getPilot()).thenReturn(pilot.getRef());

        new JavaTestKit(akka.getSystem()) {{
            // Now ask for the Power
            Power actualPower = service.getCurrentPower();
            assertThat(actualPower, nullValue());
        }};
    }
}
