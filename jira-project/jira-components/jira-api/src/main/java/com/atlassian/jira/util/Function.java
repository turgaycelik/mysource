package com.atlassian.jira.util;

/**
 * A Function that resolves Descriptors (of type D) to a Resource (of type R).
 * <p>
 * Semantically, this could be a Factory, Generator, Builder, Closure,
 * Transformer, Resolver or something else entirely. No particular guarantees
 * are implied by this interface apart from idempotence (see below).
 * Specifically, implementations may or may not return or accept null and can
 * optionally block until elements are available or throw
 * {@link RuntimeException runtime exceptions} if the input is not acceptable
 * for some reason. Any clients that require a firmer contract should subclass
 * this interface and document their requirements.
 * <p>
 * It is expected that for any two calls to {@link #get(Object)} D the returned
 * resource will be semantically the same, ie. that the call is effectively
 * idempotent. Any implementation that violates this should document the fact.
 * It is not necessary that the resolved object is the same instance or even
 * implements {@link #equals(Object)} and {@link #hashCode()} however.
 * <p>
 * As this interface requires idempotence implementations should be reentrant
 * and thread-safe.
 * 
 * @param <D> the descriptor type.
 * @param <R> the resource type it resolves to.
 */
public interface Function<D, R> extends com.atlassian.util.concurrent.Function<D, R>
{
    /**
     * Resolves an output <R> where an input <D> is given.
     * 
     * @param input an object of type D.
     * @return the output of type R.
     */
    R get(D input);
}
