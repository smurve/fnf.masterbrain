package com.zuehlke.fnf.masterbrain.simulator;

import com.google.common.io.CharStreams;
import com.zuehlke.fnf.masterbrain.akka.messages.Track;
import com.zuehlke.fnf.masterbrain.akka.messages.TrackMapper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by mhau on 09.07.2015.
 */
public class SimulatorTest {

    private static Track track;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorTest.class);

    @BeforeClass
    public static void setUp() throws Exception {
        InputStream is = SimulatorTest.class.getResourceAsStream("/testtrack.json");
        String trackString = CharStreams.toString(new InputStreamReader(is));
        track = TrackMapper.fromJSON(trackString);
        track.compoundTrackParts();
        track.preparePhysics();
    }

    @Test
    public void powerConstant100() {
        Simulator sim = new Simulator(track, 188);
        SimulationResult result = sim.simulate(Collections.nCopies(9, 100.0));
        assertEquals(23.0, result.getTrackTime(), 0.5);
    }

    @Test
    public void powerConstant120() {
        Simulator sim = new Simulator(track, 220);
        SimulationResult result = sim.simulate(Collections.nCopies(9, 120.0));
        assertEquals(18.7, result.getTrackTime(), 0.5);
    }

    @Test
    public void powerConstant140() {
        Simulator sim = new Simulator(track, 247);
        SimulationResult result = sim.simulate(Collections.nCopies(9, 140.0));
        assertEquals(17.5, result.getTrackTime(), 0.5);
    }

    @Test
    public void powerConstant160() {
        Simulator sim = new Simulator(track, 257);
        SimulationResult result = sim.simulate(Collections.nCopies(9, 160.0));
        assertEquals(20.2, result.getTrackTime(), 0.5);
    }

    @Test
    public void randomPowers() {
        Simulator sim = new Simulator(track, 257);

        Random random = new Random();
        List<Double> powerSettings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            powerSettings.add(random.nextDouble() * 255);
        }
        SimulationResult result = sim.simulate(powerSettings);
        assertTrue(result.getTrackTime() > 0);
    }

    @Test
         public void geneticAlgorithmThinksThesePowerSettingsFail() {
        Simulator sim = new Simulator(track, 190);
        List<Double> powerSettings = Arrays.asList(118.36216983385314, 152.60815802316628, 75.84059562778816, 153.26316670481447, 93.79657051057906, 215.13974516885625, 10.923613732340085, 210.91540157515266, 33.58833885706694, 58.689735294196026, 113.85519555273149, 112.16199982212622, 140.84493199986957, 235.7722222827585, 68.660778031391, 20.54771658294497, 65.05562853984476, 38.126297657843494, 43.014474577038065, 247.23375293176838, 95.09546233066547, 33.55672918453367, 124.2049507566131, 51.80400844532075, 227.79402603394445, 149.64168187146373, 6.138480810765382, 220.73232715235477, 49.20800024420399, 246.06879998549502, 239.2399732489681, 200.21507525268657, 248.97817970300184, 150.05206460521234, 239.60249603025204, 184.50903201715187, 127.00073953412848, 3.5220149162292502, 223.4418634846849, 167.63812071464014, 189.8173050903995, 138.5127127586132, 251.64109968178295, 133.79470049396744, 212.15590336979935, 32.392180025147155, 62.92463349714369, 14.626996590550489, 246.31235456574055, 234.03083508969584, 239.67920759127537, 147.37621324950553, 244.85491539218975, 248.7688226286826, 133.31226405795223, 179.97227211039845, 155.46262853469585, 237.86842277340898, 231.27807248747962, 195.18851027695274);

        SimulationResult result1 = sim.simulate(powerSettings);
        double trackTime1 = result1.getTrackTime();
        assertTrue(trackTime1 > 0);
        LOGGER.info("result time 1: " + trackTime1);

        SimulationResult result2 = sim.simulate(powerSettings);
        double trackTime2 = result2.getTrackTime();
        assertTrue(trackTime2 > 0);
        LOGGER.info("result time 2: " + trackTime2);

        assertEquals(trackTime1, trackTime2, 0.01);
    }

    @Test
    public void geneticAlgorithmThinksThesePowerSettingsFailSmall() {
        Simulator sim = new Simulator(track, 190);
        // -161.73310434137903
        List<Double> powerSettings = Arrays.asList(84.76066335580374, 125.54620711601267, 214.06391545565793, 97.99887309925593, 59.23699836427073, 100.29591966242145, 215.45054472723263, 69.30962500935911, 46.66892440355864, 96.28814170102841, 64.50839720203007, 53.750279173238134, 195.90458141524232, 168.9318746735683, 39.33341947614751, 233.6706963955955, 203.1926013279065, 17.005182921546826, 98.51233167271731, 99.0714052869601, 57.82988510826591, 146.14736185946694, 147.8079563564784, 171.59660028518567, 240.76774050672017, 244.9596497657617, 251.7185093316476, 240.67471799951647, 248.18839336053122, 254.3013404917486);
        SimulationResult result1 = sim.simulate(powerSettings);
        double trackTime1 = result1.getTrackTime();
        assertTrue(trackTime1 > 0);
        LOGGER.info("result time 1: " + trackTime1);

        SimulationResult result2 = sim.simulate(powerSettings);
        double trackTime2 = result2.getTrackTime();
        assertTrue(trackTime2 > 0);
        LOGGER.info("result time 2: " + trackTime2);

        assertEquals(trackTime1, trackTime2, 0.01);
    }

}