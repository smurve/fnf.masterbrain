package com.zuehlke.fnf.masterbrain.akka.config;

import java.util.function.Function;

/**
 * Created by tho on 29.07.2015.
 */
public class ConfigHelper {

    public static <T> T readValue(String path, Function<String, T> configProvider, T defaultValue) {
        try {
            T value = configProvider.apply(path);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
