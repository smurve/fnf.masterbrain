package com.zuehlke.fnf.masterbrain.akka.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mhan on 06.07.2015.
 */
public class SensorDataMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJSON(SensorDataRange buildTrack) throws JsonProcessingException {
        return mapper.writeValueAsString(buildTrack);
    }

    public static SensorDataRange fromJSON(InputStream in) throws IOException {
        return mapper.readValue(in, SensorDataRange.class);
    }
}
