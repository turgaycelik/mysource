/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

public class UpdateNotNullIssueFieldFunction extends UpdateIssueFieldFunction
{
    /**
     * Takes a name (vars.key) of a transient var in the args map
     * If the modified fields of the issue contains that the field id stored in "vars.key" set the field value as the field.value and call the
     * super method to update the issue field function.
     */
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String varKey = (String) args.get("vars.key");

        if (issue != null && issue.getModifiedFields().containsKey(varKey))
        {
            args.put("field.value", transientVars.get(varKey));
            super.execute(transientVars, args, ps);
        }
    }
}
