package com.zuehlke.fnf.masterbrain.akka.messages;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * Created by tho on 13.07.2015.
 */
public class SensorDataMapperTest {

    @Test
    public void testFromJson() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("data/hollywood.json")) {
            SensorDataRange data = SensorDataMapper.fromJSON(in);
            assertNotNull(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
