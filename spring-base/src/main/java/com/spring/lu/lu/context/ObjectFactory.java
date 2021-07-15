package com.spring.lu.lu.context;

@FunctionalInterface
public interface ObjectFactory<T> {
    T getObject();
}
