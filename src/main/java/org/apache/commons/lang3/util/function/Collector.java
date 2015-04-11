package org.apache.commons.lang3.util.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Collect visited objects.
 * @param <T> The type of collected objects.
 */
public class Collector<T> implements Predicate<T> {
	final private List<T> collected;    	     	
	final private Predicate<T> predicate;
	final int maxToCollect;
	
	/**
	 * Create an unlimited collector.
	 * 
	 * @param predicate The predicate that determines which objects are to be collected.
	 */
	public Collector(Predicate<T> predicate) {
		this(predicate, Integer.MAX_VALUE);
	}

	/**
	 * Create a limited size collector.
	 *  
	 * @param predicate The predicate that determines which objects are to be collected.
	 * @param maxToCollect The maximum count of objects to collect.
	 */
	public Collector(Predicate<T> predicate, int maxToCollect) {
		this.predicate = predicate;
		this.maxToCollect = maxToCollect;
		this.collected = maxToCollect!=Integer.MAX_VALUE ?new ArrayList<T>(maxToCollect) :new ArrayList<T>();
	}    	

	/**
	 * Invoke predicate to determine if object should be collected.
	 * 
	 * @param t The object that could be collected.
	 * @return true if collected count is less than maximum count.
	 */
	@Override
    public boolean test(T t) {
		if(predicate.test(t)) {
			collected.add(t);
		}
		return collected.size()<maxToCollect;
	}
	
	/**
	 * Get the collected objects.
	 * @return The objects which the predicate selected.
	 */
	public Collection<T> getResults() {
		return collected;
	}

	/**
	 * Get the first collected value.
	 * @return The first value collected, or null.
	 */
	public T getFirst() {
		return collected.size()>0 ?collected.get(0) :null;
	}
}