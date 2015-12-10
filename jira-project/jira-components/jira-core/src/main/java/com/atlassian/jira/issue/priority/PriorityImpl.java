/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.priority;

import com.atlassian.jira.issue.IssueConstantImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrl;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import org.ofbiz.core.entity.GenericValue;

public class PriorityImpl extends IssueConstantImpl implements Priority
{
    private final GenericValue genericValue;

    public PriorityImpl(GenericValue genericValue, TranslationManager translationManager,
            JiraAuthenticationContext authenticationContext, BaseUrl locator)
    {
        super(genericValue, translationManager, authenticationContext, locator);
        this.genericValue = genericValue;
    }

    public String getStatusColor()
    {
        return genericValue.getString("statusColor");
    }

    public void setStatusColor(final String statusColor)
    {
        genericValue.setString("statusColor", statusColor);
    }
}
