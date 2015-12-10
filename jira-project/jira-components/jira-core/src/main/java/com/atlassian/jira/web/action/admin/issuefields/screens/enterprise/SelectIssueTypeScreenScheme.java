package com.atlassian.jira.web.action.admin.issuefields.screens.enterprise;

import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class SelectIssueTypeScreenScheme extends JiraWebActionSupport
{
    private final ProjectManager projectManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;

    private Long projectId;
    private Long schemeId;
    private Collection issueTypeScreenSchemes;
 
    public SelectIssueTypeScreenScheme(ProjectManager projectManager, IssueTypeScreenSchemeManager issueTypeScreenSchemeManager)
    {
        this.projectManager = projectManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
    }

    public String doDefault() throws Exception
    {
        validateId();

        if (!invalidInput())
        {
            IssueTypeScreenScheme issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(getProject());
            if (issueTypeScreenScheme != null)
            {
                setSchemeId(issueTypeScreenScheme.getId());
            }
            else
            {
                setSchemeId(null);
            }
        }

        return INPUT;
    }

    protected void doValidation()
    {
        validateId();

        if (!invalidInput())
        {
            if (getSchemeId() != null && getFieldLayoutScheme() == null)
            {
                addError("schemeId", getText("admin.errors.screens.invalid.scheme.id"));
            }
        }
    }

    private void validateId()
    {
        if (getProjectId() == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
        }
        else if (getProject() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.invalid.id2"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getSchemeId() != null)
            issueTypeScreenSchemeManager.addSchemeAssociation(getProject(), getFieldLayoutScheme());

        return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/screens");
    }

    private IssueTypeScreenScheme getFieldLayoutScheme()
    {
        return issueTypeScreenSchemeManager.getIssueTypeScreenScheme(getSchemeId());
    }

    public GenericValue getProject()
    {
        return projectManager.getProject(getProjectId());
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public void setSchemeId(Long schemeId)
    {
        this.schemeId = schemeId;
    }

    public Collection getIssueTypeScreenSchemes()
    {
        if (issueTypeScreenSchemes == null)
        {
            issueTypeScreenSchemes = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes();
        }

        return issueTypeScreenSchemes;
    }
}
