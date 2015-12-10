package com.atlassian.jira.util;

/**
 * A interface to resolve some input object into an output object.
 * <p/>
 * Semantically, this could be a Factory, Generator, Builder, Closure, Transformer, Decorator or something else
 * entirely. No guarantees are implied by this interface. Specifically, input and output objects may or may not be
 * nullable, runtime exceptions may or may not be thrown and the method may or may not block.
 *
 * @since v3.13
 */
public interface Resolver<I, O> extends Function<I, O>
{
    /**
     * Resolve an instance of the output type from an object of the input type.
     *
     * @param input the input object to resolve from.
     * @return the output object that has been resolved.
     */
    O get(I input);
}
