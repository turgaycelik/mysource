package com.atlassian.jira.event;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when JIRA should clear and reinit all of its caches.
 *
 * @since v4.1
 */
public final class ClearCacheEvent
{
    public static final ClearCacheEvent INSTANCE = new ClearCacheEvent();

    private Map<String, ? extends Object> properties;

    private ClearCacheEvent()
    {
        this(Collections.<String, Object>emptyMap());
    }

    public ClearCacheEvent(Map<String, ? extends Object> properties) {

        this.properties = properties;
    }

    public Object getProperty(String key)
    {
        return properties.get(key);
    }
}
