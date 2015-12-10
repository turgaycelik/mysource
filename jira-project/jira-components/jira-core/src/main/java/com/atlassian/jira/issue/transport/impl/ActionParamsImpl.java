package com.atlassian.jira.issue.transport.impl;

import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.util.JiraCollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActionParamsImpl implements ActionParams
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    protected Map<String, String[]> params;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ActionParamsImpl()
    {
        params = new HashMap<String, String[]>();
    }

    public ActionParamsImpl(Map<String, String[]> params)
    {
        this.params = new HashMap<String, String[]>(params);
    }

    // --------------------------------------------------------------------------------------------- FieldParams Methods
    public Set<String> getAllKeys()
    {
        return params.keySet();
    }

    public Map<String, String[]> getKeysAndValues()
    {
        return new HashMap<String, String[]>(params);
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
    public String[] getAllValues()
    {
        List<String> allValues = new ArrayList<String>();
        for (final Object o : params.values())
        {
            final String[] array = (String[]) o;
            allValues.addAll(Arrays.asList(array));
        }

        return JiraCollectionUtils.stringCollectionToStringArray(allValues);
    }

    public String[] getValuesForNullKey()
    {
        return getValuesForKey(null);
    }

    public String[] getValuesForKey(String key)
    {
        return params.get(key);
    }

    // -------------------------------------------------------------------------------------------- StringParams methods
    public String getFirstValueForNullKey()
    {
        return getFirstValueForKey(null);
    }

    public String getFirstValueForKey(String key)
    {
        String[] c = getValuesForKey(key);

        if (c != null && c.length > 0)
        {
            return c[0];
        }
        else
        {
            return null;
        }
    }

    public void put(String id, String[] values)
    {
        params.put(id,  values);
    }
}
