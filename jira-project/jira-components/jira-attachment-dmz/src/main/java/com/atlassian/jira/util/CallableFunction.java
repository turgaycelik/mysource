package com.atlassian.jira.util;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import com.atlassian.util.concurrent.ExceptionPolicy;

import com.google.common.base.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps function execution in a Callable with a ExceptionPolicy.
 *
 * @param <I> The input type of the function
 * @param <O> Return type of the function
 * @since v6.3
 */
public final class CallableFunction<I, O>
{
    private static final Logger log = LoggerFactory.getLogger(CallableFunction.class);

    private final Function<I, O> function;
    private final ExceptionPolicy exceptionPolicy;

    public CallableFunction(@Nonnull final Function<I, O> function,
            @Nonnull final ExceptionPolicy exceptionPolicy)
    {
        this.function = Preconditions.checkNotNull(function);
        this.exceptionPolicy = Preconditions.checkNotNull(exceptionPolicy);
    }

    public Callable<O> apply(final I input)
    {
        return new InputProcessorCallable(input);
    }

    /**
     * Performs a single input processing operation.
     */
    private class InputProcessorCallable implements Callable<O>
    {
        private final I input;

        public InputProcessorCallable(I input) {
            this.input = input;
        }

        @Override public O call()
        {
            if (log.isDebugEnabled())
            {
                log.debug("Processing input: {}", input);
            }

            try
            {
                return function.get(input);
            }
            catch (final RuntimeException e)
            {
                return exceptionPolicy.<O>handler().apply(new com.google.common.base.Supplier<O>()
                {
                    @Override
                    public O get()
                    {
                        throw e;
                    }
                }).get();
            }
        }
    }
}
