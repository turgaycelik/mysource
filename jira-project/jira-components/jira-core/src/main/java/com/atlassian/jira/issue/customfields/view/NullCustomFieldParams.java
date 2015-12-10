package com.atlassian.jira.issue.customfields.view;

import com.atlassian.jira.issue.fields.CustomField;
import org.apache.commons.collections.Transformer;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class NullCustomFieldParams implements CustomFieldParams
{
    public void put(final String key, final Collection<String> value)
    {}

    public void remove(final String key)
    {}

    public Set<String> getAllKeys()
    {
        return null;
    }

    public void transformStringsToObjects()
    {}

    public void setCustomField(final CustomField customField)
    {}

    public CustomField getCustomField()
    {
        return null;
    }

    public Collection<String> getValuesForKey(final String key)
    {
        return null;
    }

    public Collection getValuesForNullKey()
    {
        return null;
    }

    public Collection getAllValues()
    {
        return null;
    }

    public String getQueryString()
    {
        return null;
    }

    public boolean isEmpty()
    {
        return true;
    }

    public boolean contains(final String key, final String value)
    {
        return false;
    }

    public void transformObjectsToStrings()
    {}

    public Object getFirstValueForNullKey()
    {
        return null;
    }

    public Map getKeysAndValues()
    {
        return null;
    }

    public boolean containsKey(final String key)
    {
        return false;
    }

    public Object getFirstValueForKey(final String key)
    {
        return null;
    }

    public void transform(final Transformer transformer)
    {}
}
