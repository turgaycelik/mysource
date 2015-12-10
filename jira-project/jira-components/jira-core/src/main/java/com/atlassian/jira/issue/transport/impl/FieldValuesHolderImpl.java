package com.atlassian.jira.issue.transport.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.ObjectUtils;

import org.apache.commons.lang.StringEscapeUtils;

public class FieldValuesHolderImpl extends HashMap<String, Object> implements FieldValuesHolder
{
    public FieldValuesHolderImpl()
    {
    }

    public FieldValuesHolderImpl(int initialCapacity)
    {
        super(initialCapacity);
    }

    public FieldValuesHolderImpl(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor);
    }

    public FieldValuesHolderImpl(Map<String, ?> map)
    {
        this.putAll(map);
    }

    @Override
    public Object put(String key, Object value)
    {
        if (value instanceof Object[])
        {
            final List list = EasyList.buildNonNull((Object[])value);
            if (list != null && !list.isEmpty())
            {
                return super.put(key, list);
            }
        }
        else if (value instanceof Collection)
        {
            final List list = EasyList.buildNonNull((Collection)value);
            if (list != null && !list.isEmpty())
            {
                return super.put(key, list);
            }
        }
        else if (value instanceof CustomFieldParams)
        {
            CustomFieldParams params = (CustomFieldParams) value;
            if (!params.isEmpty())
            {
                return super.put(key, params);
            }
        }
        else if (ObjectUtils.isNotEmpty(value))
        {
            return super.put(key, value);
        }

        return null;
    }

    @Override
    public void putAll(@Nonnull final Map<? extends String, ?> map)
    {
        for (Map.Entry<? extends String, ?> entry : map.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * A public non-interface method to allow for returning HTML escaped strings. This should only be used as a short hand
     * by view templates.
     *
     * @param key the key
     * @return The object for the key. If a string, a string with with HTML characters escpaed.
     */
    public Object getEscaped(Object key)
    {
        Object o = get(key);
        if (o instanceof String)
        {
            return StringEscapeUtils.escapeHtml((String)o);
        }
        else
        {
            return o;
        }
    }
}
