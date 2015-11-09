package com.zuehlke.fnf.masterbrain.akka.messages;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by tho on 06.08.2015.
 */
public class StatusMapper {

    public static void readStatus(HashMap<String[], String> m, Consumer<Status> consumer) {
        String code = null;
        String message = null;
        for (String[] key : m.keySet()) {
            String name = m.get(key);
            if ("code".equals(name)) {
                code = key[0];
            } else if ("message".equals(name)) {
                message = key[0];
            }
        }

        consumer.accept(Status.from(code, message));
    }
}
