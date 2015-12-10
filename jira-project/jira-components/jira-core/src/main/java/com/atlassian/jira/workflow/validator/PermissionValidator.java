/*
 * Copyright (c) 2002-2014
 * All rights reserved.
 */

package com.atlassian.jira.workflow.validator;

import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowUtil;

import com.google.common.annotations.VisibleForTesting;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.ValidatorDescriptor;

/**
 * An OSWorkflow validator that validates a given permission is correct. Sample usage:
 * <pre>
 * &lt;validator type="class"&gt;
 * &lt;arg name="class.name"&gt;com.atlassian.jira.workflow.validator.PermissionValidator&lt;/arg&gt;
 * &lt;arg name="permission"&gt;Create Issue&lt;/arg&gt;
 * &lt;/validator&gt;
 * </pre>
 */
public class PermissionValidator extends AbstractPermissionValidator implements Validator
{
    @SuppressWarnings ("unchecked")
    public static ValidatorDescriptor makeDescriptor(final String permission)
    {
        final ValidatorDescriptor permValidator = DescriptorFactory.getFactory().createValidatorDescriptor();
        permValidator.setType("class");
        permValidator.getArgs().put("class.name", PermissionValidator.class.getName());
        permValidator.getArgs().put("permission", permission);
        return permValidator;
    }

    @Override
    public void validate(final Map transientVars, final Map args, final PropertySet ps) throws InvalidInputException
    {
        final ApplicationUser user = getCaller(transientVars);
        hasUserPermission(args, transientVars, user);
    }

    @VisibleForTesting
    ApplicationUser getCaller(final Map transientVars) throws InvalidInputException
    {
        final String userKey = getCallerKey(transientVars);
        final ApplicationUser user = ComponentAccessor.getUserManager().getUserByKey(userKey);

        // Check if user was found
        if (userKey != null && user == null)
        {
            throw new InvalidInputException("You don't have the correct permissions - user (" + userKey + ") not found");
        }
        return user;
    }

    /**
     * Exposed for testing - to escape from testing internals of that bloody static WorkflowUtil.getCallerKey
     */
    @VisibleForTesting
    protected String getCallerKey(final Map transientVars)
    {
        return WorkflowUtil.getCallerKey(transientVars);
    }
}
