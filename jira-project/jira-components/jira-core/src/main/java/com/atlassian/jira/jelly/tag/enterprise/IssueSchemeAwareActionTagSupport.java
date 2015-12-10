/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.enterprise;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.enterprise.IssueSchemeAware;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.jelly.tag.UserAwareActionTagSupport;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public abstract class IssueSchemeAwareActionTagSupport extends UserAwareActionTagSupport implements IssueSchemeAware
{
    private final String[] requiredContextVariables;

    public IssueSchemeAwareActionTagSupport()
    {
        String[] temp = new String[super.getRequiredContextVariables().length + 1];
        System.arraycopy(super.getRequiredContextVariables(), 0, temp, 0, super.getRequiredContextVariables().length);
        temp[temp.length - 1] = JellyTagConstants.ISSUE_SCHEME_ID;
        requiredContextVariables = temp;
    }

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public boolean hasIssueScheme()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.ISSUE_SCHEME_ID);
    }

    public Long getIssueSchemeId()
    {
        if (hasIssueScheme())
            return (Long) getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_ID);
        else
            return null;
    }

    public GenericValue getIssueScheme()
    {
        try
        {
            return ManagerFactory.getIssueSecuritySchemeManager().getScheme(getIssueSchemeId());
        }
        catch (GenericEntityException e)
        {
            return null;
        }
    }
}
