/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import java.util.Collection;

import org.apache.commons.lang3.util.function.Collector;
import org.apache.commons.lang3.util.function.Predicate;
import org.apache.commons.lang3.util.function.Predicates;

/**
 * <p>
 * Helpers for {@code java.lang.Thread} and {@code java.lang.ThreadGroup}.
 * </p>
 * <p>
 * #ThreadSafe#
 * </p>
 *
 * @see java.lang.Thread
 * @see java.lang.ThreadGroup
 * @since 3.4
 * @version $Id$
 */
public class ThreadUtils {

	/**
     * Return the active thread with the specified id if it belong's to the specified thread group
     *
     * @param threadId The thread id
     * @param threadGroup The thread group
     * @return The thread which belongs to a specified thread group and the thread's id match the specified id.
     * {@code null} is returned if no such thread exists
     * @throws IllegalArgumentException if the specified id is zero or negative or the group is null
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Thread findThreadById(final long threadId, final ThreadGroup threadGroup) {
    	Collector<Thread> collector = new Collector<Thread>(new ThreadIdPredicate(threadId), 1); 	
    	visitThreads(threadGroup, true, collector);
    	return collector.getFirst();
    }

    /**
     * Return the active thread with the specified id if it belong's to a thread group with the specified group name
     *
     * @param threadId The thread id
     * @param threadGroupName The thread group name
     * @return The threads which belongs to a thread group with the specified group name and the thread's id match the specified id.
     * {@code null} is returned if no such thread exists
     * @throws IllegalArgumentException if the specified id is zero or negative or the group name is null
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Thread findThreadById(final long threadId, final String threadGroupName) {
    	// TODO: not super efficient, not sure why this method is desired
    	Collector<Thread> collector = new Collector<Thread>(new ThreadIdPredicate(threadId), 1); 	
    	for(ThreadGroup group : findThreadGroupsByName(threadGroupName)) {
        	if(!visitThreads(group, false, collector)) {
        		break;
        	}
    	}
    	return collector.getFirst();
    }

    /**
     * Return active threads with the specified name if they belong to a specified thread group
     *
     * @param threadName The thread name
     * @param threadGroupName The thread group
     * @return The threads which belongs to a thread group and the thread's name match the specified name,
     * An empty collection is returned if no such thread exists. The collection returned is always unmodifiable.
     * @throws IllegalArgumentException if the specified thread name or group is null
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Collection<Thread> findThreadsByName(final String threadName, final ThreadGroup threadGroup) {
    	Collector<Thread> collector = new Collector<Thread>(new ThreadNamePredicate(threadName)); 	
    	visitThreads(threadGroup, true, collector);
    	return collector.getResults();
    }

    /**
     * Return active threads with the specified name if they belong to a thread group with the specified group name
     *
     * @param threadName The thread name
     * @param threadGroupName The thread group name
     * @return The threads which belongs to a thread group with the specified group name and the thread's name match the specified name,
     * An empty collection is returned if no such thread exists. The collection returned is always unmodifiable.
     * @throws IllegalArgumentException if the specified thread name or group name is null
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Collection<Thread> findThreadsByName(final String threadName, final String threadGroupName) {
    	// TODO: not super efficient, not sure why this method is desired
    	Collector<Thread> collector = new Collector<Thread>(new ThreadNamePredicate(threadName)); 	
    	for(ThreadGroup group : findThreadGroupsByName(threadGroupName)) {
        	if(!visitThreads(group, false, collector)) {
        		break;
        	}
    	}
    	return collector.getResults();
    }

    /**
     * Return active thread groups with the specified group name
     *
     * @param threadGroupName The thread group name
     * @return the thread groups with the specified group name or an empty collection if no such thread group exists. The collection returned is always unmodifiable.
     * @throws IllegalArgumentException if group name is null
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Collection<ThreadGroup> findThreadGroupsByName(final String threadGroupName) {
    	Collector<ThreadGroup> collector = new Collector<ThreadGroup>(new ThreadGroupNamePredicate(threadGroupName)); 	
    	visitAllThreadGroups(collector);
    	return collector.getResults();
    }

    /**
     * Return all active thread groups including the system thread group (A thread group is active if it has been not destroyed)
     *
     * @return all thread groups including the system thread group. The collection returned is always unmodifiable.
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Collection<ThreadGroup> getAllThreadGroups() {
    	Collector<ThreadGroup> collector = new Collector<>(Predicates.<ThreadGroup>alwaysTrue()); 	
    	visitAllThreadGroups(collector);
    	return collector.getResults();
    }

    /**
     * Return the system thread group (sometimes also referred as "root thread group")
     *
     * @return the system thread group
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static ThreadGroup getSystemThreadGroup() {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        while(threadGroup.getParent() != null) {
            threadGroup = threadGroup.getParent();
        }
        return threadGroup;
    }

    /**
     * Return all active threads (A thread is active if it has been started and has not yet died)
     *
     * @return all active threads. The collection returned is always unmodifiable.
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Collection<Thread> getAllThreads() {
    	Collector<Thread> collector = new Collector<>(Predicates.<Thread>alwaysTrue()); 	
    	visitAllThreads(collector);
    	return collector.getResults();
    }

    /**
     * Return active threads with the specified name
     *
     * @param threadName The thread name
     * @return The threads with the specified name or an empty collection if no such thread exists. The collection returned is always unmodifiable.
     * @throws IllegalArgumentException if the specified name is null
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Collection<Thread> findThreadsByName(final String threadName) {
    	Collector<Thread> collector = new Collector<>(new ThreadNamePredicate(threadName)); 	
    	visitAllThreads(collector);
    	return collector.getResults();
    }

    /**
     * Return the active thread with the specified id
     *
     * @param threadId The thread id
     * @return The thread with the specified id or {@code null} if no such thread exists
     * @throws IllegalArgumentException if the specified id is zero or negative
     * @throws  SecurityException
     *          if the current thread cannot access the system thread group
     *
     * @throws  SecurityException  if the current thread cannot modify
     *          thread groups from this thread's thread group up to the system thread group
     */
    public static Thread findThreadById(final long threadId) {
       	Collector<Thread> collector = new Collector<>(new ThreadIdPredicate(threadId), 1); 	
    	visitAllThreads(collector);
    	return collector.getFirst();
     }

    /**
     * <p>
     * ThreadUtils instances should NOT be constructed in standard programming. Instead, the class should be used as
     * {@code ThreadUtils.getAllThreads()}
     * </p>
     * <p>
     * This constructor is public to permit tools that require a JavaBean instance to operate
     * </p>
     */
    public ThreadUtils() {
        super();
    }

    /**
     * Visit all active ThreadGroups starting from the system ThreadGroup. 
     * Iteration over the ThreadGroups will halt once the predicate returns false.
     * 
     * @param predicate The predicate to invoked with active ThreadGroup(s)
     */
    public static void visitAllThreadGroups(Predicate<ThreadGroup> predicate) {
    	ThreadGroup systemGroup = getSystemThreadGroup();
    	visitThreadGroups(systemGroup, true, predicate);
    }

    /**
     * Visit active ThreadGroup.
     * Iteration over the ThreadGroups will halt once the predicate returns false.
     * 
     * @param group The ThreadGroup to iterate over.
     * @param recurse If true, visit sub-groups of the given ThreadGroup; if false, just visit given ThreadGroup.
     * @param predicate The predicate to invoked with active ThreadGroup(s).
     * @return true, if all predicate always returned true
     */
    public static boolean visitThreadGroups(ThreadGroup group, boolean recurse, Predicate<ThreadGroup> predicate) {
        if (group == null) {
            throw new NullPointerException("The threadGroup must not be null");
        }
        if (predicate == null) {
            throw new NullPointerException("The predicate must not be null");
        }

    	int count = group.activeGroupCount();
    	ThreadGroup[] threadGroups;
    	do {
    		threadGroups = new ThreadGroup[count + (count>>1) + 1];
	    	count = group.enumerate(threadGroups, recurse);
    	}
    	while(count == threadGroups.length);
    	
    	for(int i = 0; i<count; ++i) {
    		if(!predicate.test(threadGroups[i])) {
    			return false;
    		}   		
    	}
    	return true;
    }

    /**
     * Visit all active threads starting from the system ThreadGroup. 
     * Iteration over the threads will halt once the predicate returns false.
     * 
     * @param predicate The predicate to invoked with active Thread(s)
     */
    public static void visitAllThreads(Predicate<Thread> predicate) {
    	ThreadGroup systemGroup = getSystemThreadGroup();
    	visitThreads(systemGroup, true, predicate);
    }

    /**
     * Visit active threads. Iteration over the threads will halt once the predicate returns false.
     * 
     * @param group The ThreadGroup to iterate over.
     * @param recurse If true, visit sub-groups of the given ThreadGroup; if false, just visit Thread of the given ThreadGroup.
     * @param predicate The predicate to invoked with active Thread(s).
     * @return true, if all predicate always returned true
     */
    public static boolean visitThreads(ThreadGroup group, boolean recurse, Predicate<Thread> predicate) {
    	int count = group.activeCount();
    	Thread[] threads;
    	do {
	    	threads = new Thread[count + (count>>1) + 1];
	    	count = group.enumerate(threads, recurse);
    	}
    	while(count == threads.length);
    	
    	for(int i = 0; i<count; ++i) {
    		if(!predicate.test(threads[i])) {
    			return false;
    		}   		
    	}
    	return true;
    }
    
    public static class ThreadIdPredicate implements Predicate<Thread> {
        private final long threadId;

        public ThreadIdPredicate(final long threadId) {
        	if(threadId<=0) {
        		throw new IllegalArgumentException("threadId must be positive value");
        	}
            this.threadId = threadId;
        }

        @Override
        public boolean test(final Thread thread) {
            return thread.getId()==threadId;
        }
    }
    
    public static class ThreadNamePredicate implements Predicate<Thread> {
        private final String name;

        public ThreadNamePredicate(final String name) {
            if (name == null) {
                throw new NullPointerException("The name must not be null");
            }
            this.name = name;
        }

        @Override
        public boolean test(final Thread thread) {
            return thread.getName().equals(name);
        }
    }
    
    public static class ThreadGroupNamePredicate implements Predicate<ThreadGroup> {
        private final String name;

        public ThreadGroupNamePredicate(final String name) {
            if (name == null) {
                throw new NullPointerException("The name must not be null");
            }
            this.name = name;
        }

        @Override
        public boolean test(final ThreadGroup threadGroup) {
            return threadGroup.getName().equals(name);
        }
    }
}
