/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.Condition;

import java.util.Map;

public class IssueAssignedCondition implements Condition
{
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        return TextUtils.stringSet((String) transientVars.get("assignee"));
    }
}
