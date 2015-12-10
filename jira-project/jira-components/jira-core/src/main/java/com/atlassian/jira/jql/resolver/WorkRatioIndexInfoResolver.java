package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.worklog.WorkRatio;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Collections;
import java.util.List;

/**
 * Work Ratios are stored as integers, but are padded in a specific way
 *
 * @since v4.0
 */
public class WorkRatioIndexInfoResolver implements IndexInfoResolver<Object>
{
    public List<String> getIndexedValues(final String rawValue)
    {
        Assertions.notNull("rawValue", rawValue);
        return Collections.singletonList(convertToIndexValue(rawValue));
    }

    public List<String> getIndexedValues(final Long rawValue)
    {
        Assertions.notNull("rawValue", rawValue);
        return Collections.singletonList(convertToIndexValue(rawValue.toString()));
    }

    public String getIndexedValue(final Object indexedObject)
    {
        // there is no WorkRatio object - this should never be called.
        return null;
    }

    String convertToIndexValue(final String stringValue)
    {
        return WorkRatio.getPaddedWorkRatioString(stringValue);
    }
}
