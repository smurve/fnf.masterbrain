package com.zuehlke.fnf.masterbrain.util;

@FunctionalInterface
public interface ConsumerWithThrowable<T> {
    void consume(T t) throws Exception;
}