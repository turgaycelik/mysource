package com.atlassian.jira.util;

import org.apache.commons.lang.StringUtils;

public class JiraArrayUtils
{
    public static String[] add(String[] array, String obj)
    {
        if (array != null)
        {
            String[] newArray = new String[array.length + 1];
            for (int i = 0; i < array.length; i++)
            {
                newArray[i] = array[i];
            }
            newArray[array.length] = obj;

            return newArray;
        }
        else if (obj != null)
        {
            return new String[] {obj};
        }
        else
        {
            return null;
        }
    }

    public static boolean isContainsOneBlank(String[] array)
    {
        return array != null && array.length == 1 && StringUtils.isBlank(array[0]);
    }
}
