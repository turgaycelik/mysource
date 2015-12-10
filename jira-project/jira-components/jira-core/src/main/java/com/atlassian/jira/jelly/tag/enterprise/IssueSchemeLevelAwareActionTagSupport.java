/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.enterprise;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.jelly.enterprise.IssueSchemeLevelAware;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public abstract class IssueSchemeLevelAwareActionTagSupport extends IssueSchemeAwareActionTagSupport implements IssueSchemeLevelAware
{
    private final String[] requiredContextVariables;

    public IssueSchemeLevelAwareActionTagSupport()
    {
        String[] temp = new String[super.getRequiredContextVariables().length + 1];
        System.arraycopy(super.getRequiredContextVariables(), 0, temp, 0, super.getRequiredContextVariables().length);
        temp[temp.length - 1] = JellyTagConstants.ISSUE_SCHEME_LEVEL_ID;
        requiredContextVariables = temp;
    }

    public String[] getRequiredContextVariables()
    {
        return requiredContextVariables;
    }

    public boolean hasIssueSchemeLevel()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID);
    }

    public Long getIssueSchemeLevelId()
    {
        final String issueSchemeLeveIdStr = (String) getContext().getVariable(JellyTagConstants.ISSUE_SCHEME_LEVEL_ID);
        try
        {
            return new Long(issueSchemeLeveIdStr);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public GenericValue getIssueSchemeLevel()
    {
        try
        {
            return ManagerFactory.getIssueSecurityLevelManager().getIssueSecurityLevel(getIssueSchemeLevelId());
        }
        catch (GenericEntityException e)
        {
            return null;
        }
    }
}
