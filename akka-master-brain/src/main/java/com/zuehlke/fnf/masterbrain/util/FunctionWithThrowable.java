package com.zuehlke.fnf.masterbrain.util;

@FunctionalInterface
public interface FunctionWithThrowable<T, R> {
    R apply(T t) throws Exception;
}
