package com.zuehlke.fnf.masterbrain.akka.config;

import com.zuehlke.fnf.masterbrain.akka.AkkaRule;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by tho on 29.07.2015.
 */
public class ConfigHelperTest {

    @Rule
    public AkkaRule akka = AkkaRule.create();

    @Test
    public void testDefaultIntValue() {
        int actual = ConfigHelper.readValue("foo.bar", akka.getConfig()::getInt, 42);
        assertThat(actual, is(42));
    }

    @Test
    public void testIntValue() {
        akka.withConfig("foo{" +
                "    bar=23" +
                "}");
        int actual = ConfigHelper.readValue("foo.bar", akka.getConfig()::getInt, 42);
        assertThat(actual, is(23));
    }

    @Test
    public void testStringValue() {
        akka.withConfig("foo{" +
                "    bar=test" +
                "}");
        String actual = ConfigHelper.readValue("foo.bar", akka.getConfig()::getString, "fail");
        assertThat(actual, is("test"));
    }


}
