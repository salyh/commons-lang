package org.apache.commons.lang3.util.function;


/**
 * Predicate compatible with Java8
 */
public interface Predicate<T> extends java.util.function.Predicate<T> {

	@Override
    boolean test(T t);
}