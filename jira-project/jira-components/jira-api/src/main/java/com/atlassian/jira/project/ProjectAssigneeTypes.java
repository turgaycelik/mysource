/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.Map;

/**
 * Utility class for dealing with project assignee types.
 *
 * @since 5.1.6
 */
@PublicApi
public class ProjectAssigneeTypes extends AssigneeTypes
{
    private static final String NO_DEFAULT_MESSAGE = "Please select a Default Assignee";

    public static boolean isValidType(Long defaultAssigneeType)
    {
        return (defaultAssigneeType != null) && ((defaultAssigneeType == PROJECT_LEAD) || (defaultAssigneeType == UNASSIGNED && isAllowUnassigned()));
    }

    public static boolean isProjectLead(Long defaultAssigneeType)
    {
        if (defaultAssigneeType == null)
        {
            return !isAllowUnassigned();
        }
        else
        {
            return defaultAssigneeType == PROJECT_LEAD;
        }
    }

    public static boolean isUnassigned(Long defaultAssigneeType)
    {
        if (defaultAssigneeType == null)
        {
            return isAllowUnassigned();
        }
        else
        {
            return defaultAssigneeType == UNASSIGNED;
        }
    }

    public static Map<String, String> getAssigneeTypes()
    {
        if (isAllowUnassigned())
        {
            return MapBuilder.build(String.valueOf(UNASSIGNED), PRETTY_UNASSIGNED, String.valueOf(PROJECT_LEAD), PRETTY_PROJECT_LEAD);
        }
        else
        {
            return MapBuilder.build(String.valueOf(PROJECT_LEAD), PRETTY_PROJECT_LEAD);
        }
    }

    public static String getPrettyAssigneeType(Long defaultAssigneeType)
    {
        if (isProjectLead(defaultAssigneeType))
        {
            return PRETTY_PROJECT_LEAD;
        }
        else if (isUnassigned(defaultAssigneeType))
        {
            return PRETTY_UNASSIGNED;
        }
        else
        {
            return NO_DEFAULT_MESSAGE;
        }
    }
}
