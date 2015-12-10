package com.atlassian.jira.web.action.admin.issuesecurity;

import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.scheme.AbstractViewSchemes;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class ViewSchemes extends AbstractViewSchemes
{
    public SchemeManager getSchemeManager()
    {
        return ComponentAccessor.getComponent(IssueSecuritySchemeManager.class);
    }

    public String getRedirectURL()
    {
        return null;
    }

    public boolean isCanDelete(final GenericValue scheme)
    {
        try
        {
            if (scheme != null)
            {
                final List projects = getProjects(scheme);
                if (projects == null || projects.isEmpty())
                {
                    return true;
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Error while retrieving projects for scheme.", e);
        }

        return false;
    }
}
