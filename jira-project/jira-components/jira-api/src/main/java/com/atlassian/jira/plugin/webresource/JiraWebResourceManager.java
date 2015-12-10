package com.atlassian.jira.plugin.webresource;

import com.atlassian.annotations.PublicApi;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Map;

/**
 * Add some additional methods to the stock WebResourceManager that we can use in JIRA.
 *
 * @since v5.0
 */
@PublicApi
public interface JiraWebResourceManager extends WebResourceManager
{
    /**
     * Adds key-value String pairs to a map to be rendered later.
     *
     * @param key a unique key to store the value against
     * @param value an HTML-safe string
     *
     * @return true if metadata added to map successfully
     * @since v5.0
     */
    boolean putMetadata(String key, String value);

    /**
     * Returns the map of key-value pairs added via {@link #putMetadata(String, String)}.
     * Should return an empty map and log a warning if called more than once in a request.
     *
     * @return the map of key-value pairs added via {@link #putMetadata(String, String)}.
     * @since v5.0
     */
    Map<String, String> getMetadata();
}
