package com.atlassian.jira.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since v5.2
 */
public interface BaseUrl
{
    /**
     * @return The base URL for this instance, also known as the context path.  If running in the context of a web request, this will return a url relative
     *         to the server root (ie "/jira/").  If running via email, it will return an absolute URL (eg. "http://example.com/jira/").
     */
    @Nonnull
    String getBaseUrl();

    /**
     * @return The canonical base URL for this instance.  It will return an absolute URL (eg. "http://example.com/jira/").
     */
    @Nonnull
    String getCanonicalBaseUrl();

    /**
     * Run the passed function in an environment where JIRA's configured {@code baseURL} is always used. This basically
     * makes the passed function ignore any smart {@code baseURL} that can be generated from the request associated
     * with the calling thread.
     *
     * @param input input to pass to the function.
     * @param runnable the function to execute.
     * @return the result of the function.
     * @since 6.3.1
     */
    @Nullable
    <I, O> O runWithStaticBaseUrl(@Nullable I input, @Nonnull com.google.common.base.Function<I, O> runnable);
}
