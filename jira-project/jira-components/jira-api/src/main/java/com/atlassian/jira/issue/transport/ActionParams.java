package com.atlassian.jira.issue.transport;


import java.util.Map;

/**
 * An interface that is off the same format as the Map returned by Action.getParameters.
 * All keys are strings and all values are String arrays.
 */
public interface ActionParams extends FieldParams
{
    @Override
    Map<String, String[]> getKeysAndValues();

    String[] getAllValues();
    String[] getValuesForNullKey();
    String[] getValuesForKey(String key);

    String getFirstValueForNullKey();
    String getFirstValueForKey(String key);

    void put(String id, String[] values);
}
