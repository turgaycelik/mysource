package com.atlassian.jira.issue.customfields.option;

import org.apache.log4j.Logger;

/**
 * Date: 8/09/2004
 * Time: 10:15:12
 */
public class OptionUtils
{
    private static final Logger log = Logger.getLogger(OptionUtils.class);


    public static Long safeParseLong(String key)
    {
        Long parentOptionId = null;
        try
        {
            if (key != null)
                parentOptionId = new Long(key);
        }
        catch (NumberFormatException e)
        {
            log.debug("OptionParentId '" + key + "' is invalid null being returned", e);
        }
        return parentOptionId;
    }

    public static String safeToString(Long num)
    {
        return num == null ? null : num.toString();
    }
}
