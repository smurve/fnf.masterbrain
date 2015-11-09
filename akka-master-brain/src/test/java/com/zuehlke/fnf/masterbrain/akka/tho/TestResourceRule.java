package com.zuehlke.fnf.masterbrain.akka.tho;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.InputStream;

/**
 * Created by tho on 10.08.2015.
 */
public class TestResourceRule extends TestWatcher {

    private Description description;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void starting(Description description) {
        this.description = description;
    }

    public <T> T readJsonFromTestPackage(String fileName, Class<T> target) {
        try {
            String dir = description.getTestClass().getPackage().getName().replace(".", "/");
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(dir + "/" + fileName)) {
                return mapper.readValue(in, target);
            }
        } catch (Exception e) {
            throw new RuntimeException("RULE FAILED", e);
        }
    }
}
