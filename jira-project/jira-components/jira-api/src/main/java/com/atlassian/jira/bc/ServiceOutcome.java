package com.atlassian.jira.bc;

import com.atlassian.annotations.PublicApi;

/**
 * A service result that also has an value.
 *
 * @since v4.2
 */
@PublicApi
public interface ServiceOutcome<T> extends ServiceResult
{
    /**
     * Returns the value that was returned by the service, or null.
     *
     * @return the value returned by the service, or null
     */
    T getReturnedValue();

    /**
     * Returns the value that was returned by the service, or null.
     *
     * @return the value returned by the service, or null
     * @since 6.2
     */
    T get();
}
