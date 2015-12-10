package com.atlassian.jira.web.action.admin;

import java.util.Map;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;

public class MockObjectConfiguration implements ObjectConfiguration
{
    Map fieldKeyToNameMap;

    public MockObjectConfiguration(Map fieldKeyToNameMap)
    {
        this.fieldKeyToNameMap = fieldKeyToNameMap;
    }

    public String getDescription(Map params)
    {
        return null;
    }

    public boolean allFieldsHidden()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isI18NValues(String string)
    {
        return true;
    }

    public String getFieldDefault(String key) throws ObjectConfigurationException
    {
        return null;
    }

    public String getFieldDescription(String key) throws ObjectConfigurationException
    {
        return null;
    }

    public String[] getFieldKeys()
    {
        return (String[]) fieldKeyToNameMap.keySet().toArray(new String[fieldKeyToNameMap.size()]);
    }

    public String getFieldName(String key) throws ObjectConfigurationException
    {
        return (String) fieldKeyToNameMap.get(key);
    }

    public int getFieldType(String key) throws ObjectConfigurationException
    {
        return 0;
    }

    public Map getFieldValues(String key) throws ObjectConfigurationException
    {
        return null;
    }

    public Map getFieldValuesHtmlEncoded(String key) throws ObjectConfigurationException
    {
        return null;
    }

    public void init(Map params)
    {
    }

    public String[] getEnabledFieldKeys()
    {
        return new String[0];
    }

    public boolean isEnabled(String key)
    {
        return false;
    }
}
