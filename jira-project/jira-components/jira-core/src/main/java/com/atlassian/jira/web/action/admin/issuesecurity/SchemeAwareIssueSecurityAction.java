/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.issue.security.IssueSecurityLevelScheme;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.AbstractSchemeAwareAction;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.SecurityTypeManager;

public class SchemeAwareIssueSecurityAction extends AbstractSchemeAwareAction
{
    private IssueSecurityLevelScheme issueSecurityLevelScheme;
    protected final IssueSecuritySchemeManager issueSecuritySchemeManager;
    protected final SecurityTypeManager issueSecurityTypeManager;

    public SchemeAwareIssueSecurityAction(IssueSecuritySchemeManager issueSecuritySchemeManager, SecurityTypeManager issueSecurityTypeManager)
    {
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityTypeManager = issueSecurityTypeManager;
    }

    public SchemeType getType(String id)
    {
        return issueSecurityTypeManager.getSchemeType(id);
    }

    public IssueSecuritySchemeManager getSchemeManager()
    {
        return issueSecuritySchemeManager;
    }

    public String getRedirectURL()
    {
        return null;
    }

    /**
     * Is this Level the default level for the scheme
     * @param levelId The id of the level to check if it is the default
     * @return true if the level is the default otherwise false
     */
    public boolean isDefault(Long levelId)
    {
        if (levelId != null)
        {
            if (getIssueSecurityLevelScheme() != null)
            {
                return levelId.equals(getIssueSecurityLevelScheme().getDefaultSecurityLevelId());
            }
        }

        return false;
    }

    public IssueSecurityLevelScheme getIssueSecurityLevelScheme()
    {
        if (issueSecurityLevelScheme == null)
        {
            issueSecurityLevelScheme = getSchemeManager().getIssueSecurityLevelScheme(getSchemeId());
        }
        return issueSecurityLevelScheme;
    }
}
