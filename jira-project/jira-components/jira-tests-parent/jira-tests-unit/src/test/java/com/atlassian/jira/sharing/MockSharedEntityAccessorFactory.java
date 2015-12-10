package com.atlassian.jira.sharing;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.SharedEntityAccessor.Factory;

/**
 * Simple implementation of {@link SharedEntityAccessor.Factory} for unit tests.
 *
 * @since v3.13
 */
public class MockSharedEntityAccessorFactory implements Factory
{
    private final Map /*<String, SharedEntityAccessor>*/ accessors = new HashMap();

    public SharedEntityAccessor getSharedEntityAccessor(final TypeDescriptor type)
    {
        return getSharedEntityAccessor(type.getName());
    }

    public SharedEntityAccessor getSharedEntityAccessor(final String type)
    {
        return (SharedEntityAccessor) accessors.get(type);
    }

    public void addAccessor(final String type, final SharedEntityAccessor accessor)
    {
        accessors.put(type, accessor);
    }

    public void addAccessor(final TypeDescriptor type, final SharedEntityAccessor accessor)
    {
        addAccessor(type.getName(), accessor);
    }
}
