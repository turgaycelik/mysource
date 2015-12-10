package com.atlassian.jira.util;

import java.util.Collection;

public class JiraCollectionUtils
{
    public static String[] stringCollectionToStringArray(Collection<String> allValues)
    {
        String[] returnValue = new String[allValues.size()];
        int i = 0;
        for (final String s : allValues)
        {
            returnValue[i] = s;
            i++;
        }
        return returnValue;
    }
}
