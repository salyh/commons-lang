package org.apache.commons.lang3.util.function;

public class Predicates {
	
	public static final Predicate<? super Object> ALWAYS_TRUE = new Predicate<Object>() {;
	    @Override
	    public boolean test(final Object ignored) {
	        return true;
	    }
	};

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> alwaysTrue() {
		return (Predicate<T>) Predicates.ALWAYS_TRUE;
	}
}