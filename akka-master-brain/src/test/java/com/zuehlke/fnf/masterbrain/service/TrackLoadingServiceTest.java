package com.zuehlke.fnf.masterbrain.service;

import akka.testkit.JavaTestKit;
import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import com.zuehlke.fnf.masterbrain.akka.messages.SensorDataRange;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by tho on 23.07.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class TrackLoadingServiceTest {

    @Rule
    public AkkaRule akka = AkkaRule.create();
    @Mock
    private MasterBrainService masterBrainService;
    @InjectMocks
    private TrackLoadingService service;

    @Test
    public void testLoadValidTrack() {
        JavaTestKit trackBuilder = akka.newProbe();
        when(masterBrainService.getTrackBuilder()).thenReturn(trackBuilder.getRef());

        new JavaTestKit(akka.getSystem()) {{
            String result = service.load("hollywood");
            assertThat(result, is("OK"));
            trackBuilder.expectMsgClass(SensorDataRange.class);
        }};
    }

    @Test
    public void testLoadUnknownTrack() {
        JavaTestKit trackBuilder = akka.newProbe();
        when(masterBrainService.getTrackBuilder()).thenReturn(trackBuilder.getRef());


        new JavaTestKit(akka.getSystem()) {{
            String result = service.load("foo");
            assertThat(result, is("FAILED"));
            trackBuilder.expectNoMsg(duration("200 milliseconds"));
        }};
    }
}
