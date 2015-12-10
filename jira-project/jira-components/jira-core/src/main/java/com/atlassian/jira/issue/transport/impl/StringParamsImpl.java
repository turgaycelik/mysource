package com.atlassian.jira.issue.transport.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.StringParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class StringParamsImpl implements StringParams
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    protected Map<String, Collection<String>> params;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public StringParamsImpl()
    {
        params = new HashMap();
    }

    public StringParamsImpl(Map params)
    {
        validateMap(params);
        this.params = new HashMap(params);
    }

    public StringParamsImpl(ActionParams actionParams)
    {
        Map stringArrayMap = actionParams.getKeysAndValues();
        this.params = new HashMap(stringArrayMap.size());

        Set entries = stringArrayMap.entrySet();
        for (final Object entry1 : entries)
        {
            Map.Entry entry = (Map.Entry) entry1;
            String key = (String) entry.getKey();
            String[] value = (String[]) entry.getValue();

            params.put(key, EasyList.build(value));
        }
    }


    // --------------------------------------------------------------------------------------------- FieldParams Methods
    public Set getAllKeys()
    {
        return params.keySet();
    }

    public Map getKeysAndValues()
    {
        return new HashMap(params);
    }

    public boolean containsKey(String key)
    {
        return params.containsKey(key);
    }

    public boolean isEmpty()
    {
        return params.isEmpty();
    }

    // ---------------------------------------------------------------------------------------- CollectionParams methods
    public Collection getAllValues()
    {
        List allValues = new ArrayList();
        for (final Collection<String> strings : params.values())
        {
            List values = (List) strings;
            allValues.addAll(values);
        }
        return allValues;
    }

    public Collection getValuesForNullKey()
    {
        return getValuesForKey(null);
    }

    public Collection<String> getValuesForKey(@Nullable String key)
    {
        return params.get(key);
    }

    public void put(String key, Collection<String> value)
    {
         params.put(key, value);
    }

    // -------------------------------------------------------------------------------------------- StringParams methods
    public String getFirstValueForNullKey()
    {
        return getFirstValueForKey(null);
    }

    public String getFirstValueForKey(String key)
    {
        Collection c = getValuesForKey(key);

        if (c != null && !c.isEmpty())
        {
            return (String) c.iterator().next();
        }
        else
        {
            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    /**
     * Ensures that the map has List of Strings as its value
     * @param params
     */
    private void validateMap(Map params)
    {
        // @todo implement this
    }

}
