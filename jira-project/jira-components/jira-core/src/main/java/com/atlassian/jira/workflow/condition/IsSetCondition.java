/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Condition which checks whether a transientVars variable is set to a certain value.
 * For instance, if the 'submitbutton' field is set to 'jira.button.submit':
 *
 *   <condition type="class">
 *       <arg name="class.name">com.atlassian.jira.workflow.condition.IsSetCondition</arg>
 *       <arg name="name">submitbutton</arg>
 *       <arg name="value">jira.button.submit</arg>
 *   </condition>
 *
 * See {@link com.atlassian.jira.web.action.issue.CreateIssueDetails#getAuxiliarySubmitButtonValue()} for details on this example.
 *
 */
public class IsSetCondition implements Condition
{
    private static final Logger log = Logger.getLogger(DisallowIfInStepCondition.class);

    public boolean passesCondition(Map transientVars, Map args, PropertySet ps)
    {
        try
        {
            String name = (String) args.get("name");
            if (name == null) throw new IllegalArgumentException("Must specify a 'name' arg specifying name of the variable to check");
            String requiredValue = (String) args.get("value");
            if (requiredValue == null) throw new IllegalArgumentException(("Must specify 'value' arg specifying value that 'key' should evaluate to"));
            String actualValue = (String) transientVars.get(name);
            return requiredValue.equals(actualValue);
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
            return false;
        }
    }
}
