package com.zuehlke.fnf.masterbrain.akka.messages;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by mhan on 09.07.2015.
 */
public class LocationMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationMapper.class);

    public static LocationPs fromRObject(REXP o) throws REXPMismatchException {
        LocationPs toReturn = new LocationPs();

        HashMap<HashMap, String> o1 = (HashMap<HashMap, String>) o.asNativeJavaObject();
        o1.forEach((m, s) -> {
            if ("location".equals(s)) {
                readLocation(m, toReturn);
            } else if ("status".equals(s)) {
                StatusMapper.readStatus(m, (status) -> toReturn.setStatus(status));
            }
        });


        return toReturn;
    }

    private static void readLocation(HashMap<Object, String> o, LocationPs toReturn) {
        o.forEach((m, s) -> {
            if ("data".equals(s)) {
                double[] ps = (double[]) m;
                if (ps == null || ps.length == 0) {
                    LOGGER.error("Localization failed. Empty ps");
                }
                toReturn.setPs(ps);
            }
        });

    }
}
