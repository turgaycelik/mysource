/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.validator;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;

import java.util.Map;

public class NotNullPermissionValidator extends PermissionValidator
{
    /**
     * Takes the name of a variables in the transient vars map, if it exists then calls validate
     */
    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException
    {
        String varKey = (String) args.get("vars.key");
        if (transientVars.containsKey(varKey))
        {
            super.validate(transientVars, args, ps);
        }
    }
}
