package com.zuehlke.fnf.masterbrain.util;

import com.google.common.base.Throwables;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by mhan on 02.07.2015.
 */
public class StreamingUtils {

    public static <F, T> Function<F, T> funcThrowRuntime(FunctionWithThrowable<F, T> wrapedFunction) {
        return val -> {
            try {
                return wrapedFunction.apply(val);
            } catch (Throwable e) {
                throw Throwables.propagate(e);
            }
        };
    }

    public static <T> Consumer<T> conThrowRuntime(ConsumerWithThrowable<T> wrapedFunction) {
        return val -> {
            try {
                wrapedFunction.consume(val);
            } catch (Throwable e) {
                throw Throwables.propagate(e);
            }
        };
    }

}
