/*
 * Copyright (c) 2002-2004 All rights reserved.
 */
package com.atlassian.jira.util.concurrent;

import com.atlassian.util.concurrent.LazyReference;

/**
 * Thread-safe lock-less (see note) reference that is not constructed until required. This class is used to maintain a
 * reference to an object that is expensive to create and must be constructed once and once only. Therefore this
 * reference behaves as though the <code>final</code> keyword has been used (you cannot reset it once it has been
 * constructed).
 * <p>
 * When using this class you need to implement the {@link #create()} method to return the object this reference will
 * hold.
 * <p>
 * For instance:
 * <p>
 * <pre>
 * final ThreadsafeLazyLoadedReference ref = new ThreadsafeLazyLoadedReference()
 * {
 *     protected Object create() throws Exception
 *     {
 *         // Do some useful object construction here
 *         return new MyObject();
 *     }
 * };
 * </pre>
 * <p>
 * Then call to get a reference to the object:
 * <p>
 * <pre>
 *   MyObject myLazyLoadedObject = (MyObject) ref.get()
 * </pre>
 * <p>
 * Interruption policy is that if you want to be cancellable while waiting for another thread to create the value,
 * instead of calling {@link #get()} call {@link #getInterruptibly()}. If your {@link #create()} method throws an
 * {@link InterruptedException} however, it will be the causal exception inside the runtime
 * exception that {@link #get()} or {@link #getInterruptibly()} throws and your {@link #create()}
 * will not be called again.
 * <p>
 * @deprecated Switch to {@link LazyReference} instead.
 */
@Deprecated
public abstract class ThreadsafeLazyLoadedReference<V> extends LazyReference<V>
{}