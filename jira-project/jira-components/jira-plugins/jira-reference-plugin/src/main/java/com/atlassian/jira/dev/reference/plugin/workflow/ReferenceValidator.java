/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.dev.reference.plugin.workflow;

import com.atlassian.jira.workflow.validator.AbstractPermissionValidator;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;

import java.util.Map;

/**
 * <p>
 * A reference validator that will return <code>true</code>, or <code>false</code> basing on its configuration.
 *
 * @since 4.3
 */
public class ReferenceValidator extends AbstractPermissionValidator implements Validator
{
    public void validate(Map transientVars, Map args, PropertySet ps) throws InvalidInputException
    {
        Object resultObject = args.get(ReferenceWorkflowModuleFactory.RESULT_PARAM);
        if (resultObject == null)
        {
            throw new IllegalStateException("Args <" + args + "> do not include the required param <"
                    + ReferenceWorkflowModuleFactory.RESULT_PARAM + ">");
        }
        if (!Boolean.TRUE.equals(Boolean.valueOf((String) resultObject)))
        {
            throw new InvalidInputException("Somebody configured me to fail:)");  
        }
    }
}
