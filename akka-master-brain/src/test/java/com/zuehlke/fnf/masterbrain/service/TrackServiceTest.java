package com.zuehlke.fnf.masterbrain.service;

import akka.actor.ActorRef;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActor;
import com.google.common.collect.ImmutableList;
import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.messages.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

/**
 * Created by tho on 23.07.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class TrackServiceTest {

    @Mock
    private MasterBrainService masterBrainService;

    @InjectMocks
    private TrackService service;

    @Rule
    public AkkaRule akka = AkkaRule.create();

    @Test
    public void testGetCurrentTrackWhenTrackIsAvailable() {

        JavaTestKit trackBuilder = akka.newProbe();

        when(masterBrainService.getTrackBuilder()).thenReturn(trackBuilder.getRef());

        new JavaTestKit(akka.getSystem()) {{
            Track expectedTrack = new Track();

            // Mock behaviour of trackBuilder
            trackBuilder.setAutoPilot(new TestActor.AutoPilot() {
                public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                    if (msg instanceof GetTrack) {
                        sender.tell(expectedTrack, ActorRef.noSender());
                    }
                    return noAutoPilot();
                }
            });

            // Now as for the Track
            Track actualTrack = service.getCurrentTrack();
            assertThat(actualTrack, notNullValue());
            assertThat(actualTrack, is(expectedTrack));
        }};
    }

    @Test
    public void testGetCurrentTrackWhenTrackBuilderDoesNotAnswer() {

        JavaTestKit trackBuilder = akka.newProbe();

        when(masterBrainService.getTrackBuilder()).thenReturn(trackBuilder.getRef());

        new JavaTestKit(akka.getSystem()) {{
            // Now as for the Track
            Track actualTrack = service.getCurrentTrack();
            assertThat(actualTrack, nullValue());
        }};
    }

    @Test
    public void testGetCurrentPowerProfileWhenPowerProfileIsAvailable() {

        JavaTestKit pilot = akka.newProbe();

        when(masterBrainService.getPilot()).thenReturn(pilot.getRef());

        new JavaTestKit(akka.getSystem()) {{
            Track track = new Track();
            track.setCurves(Arrays.asList(new Curve()));
            track.setStraights(Arrays.asList(new Straight(){{ setSector_ind(new int[1]);}}));
            track.setTrackpoints(new TrackPoints());
            ImmutableList<Double> expectedPowerProfile = ImmutableList.of(1d, 2d, 3d);

            // Mock behaviour of pilot
            pilot.setAutoPilot(new TestActor.AutoPilot() {
                public TestActor.AutoPilot run(ActorRef sender, Object msg) {
                    if (msg instanceof GetPowerProfile) {
                        sender.tell(TrackWithPowerProfile.from(track, expectedPowerProfile), ActorRef.noSender());
                    }
                    return noAutoPilot();
                }
            });

            // Now ask for the Track
            List<TrackWithPowerProfile.PowerSetting> actualPowerSetting = service.getCurrentPowerProfile();
            assertThat(actualPowerSetting, notNullValue());
            assertThat(actualPowerSetting.size(), is(3));
            assertThat(actualPowerSetting.get(0).getPowerSetting(), is(1d));
            assertThat(actualPowerSetting.get(1).getPowerSetting(), is(2d));
            assertThat(actualPowerSetting.get(2).getPowerSetting(), is(3d));
        }};
    }

    @Test
    public void testGetCurrentPowerProfileWhenTrackBuilderDoesNotAnswer() {

        JavaTestKit pilot = akka.newProbe();

        when(masterBrainService.getPilot()).thenReturn(pilot.getRef());

        new JavaTestKit(akka.getSystem()) {{
            // Now ask for the Track
            List<TrackWithPowerProfile.PowerSetting> actualPowerSetting = service.getCurrentPowerProfile();
            assertThat(actualPowerSetting, nullValue());
        }};
    }
}

