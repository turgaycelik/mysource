package com.atlassian.jira.plugin.webfragment;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.plugin.web.ContextProvider;

import java.util.Map;

/**
 * Implementing {@link ContextProvider}s will be wrapped with {@link CacheableContextProviderDecorator} on creation
 * in {@link JiraWebFragmentHelper}, and have {@link #getContextMap(java.util.Map)} invocation results cached
 * in the request.
 *
 * @since v4.4
 */
@PublicSpi
public interface CacheableContextProvider extends ContextProvider
{
    /**
     * Return a key that is unique for the scope that this context should be scoped.  E.g. If the context is unique per
     * issue, per user return "JRA-11234:nmenere"
     *
     * @param context the context passed into the getContextMap method.
     * @return a key that is unique for the scope that this context
     */
    public abstract String getUniqueContextKey(Map<String, Object> context);

}
