package com.atlassian.jira.web.action.admin.issuefields.screens.enterprise;

import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class ViewIssueTypeScreenSchemes extends JiraWebActionSupport
{
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private Collection issueTypeScreenSchemes;
    private Long id;
    private String schemeName;
    private String schemeDescription;
    private Long fieldScreenSchemeId;
    private boolean confirm;
    private IssueTypeScreenScheme issueTypeScreenScheme;
    private Collection fieldScreenSchemes;
    private Map projectsMap;

    public ViewIssueTypeScreenSchemes(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, FieldScreenSchemeManager fieldScreenSchemeManager)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
        this.projectsMap = new HashMap();
    }

    protected String doExecute() throws Exception
    {
        return getResult();
    }

    public Collection getIssueTypeScreenSchemes()
    {
        if (issueTypeScreenSchemes == null)
        {
            issueTypeScreenSchemes = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes();
        }

        return issueTypeScreenSchemes;
    }

    public Long getFieldScreenSchemeId()
    {
        return fieldScreenSchemeId;
    }

    public void setFieldScreenSchemeId(Long fieldScreenSchemeId)
    {
        this.fieldScreenSchemeId = fieldScreenSchemeId;
    }

    protected String redirectToView()
    {
        return getRedirect("ViewIssueTypeScreenSchemes.jspa");
    }

    public String getSchemeName()
    {
        return schemeName;
    }

    public void setSchemeName(String schemeName)
    {
        this.schemeName = schemeName;
    }

    public String getSchemeDescription()
    {
        return schemeDescription;
    }

    public void setSchemeDescription(String schemeDescription)
    {
        this.schemeDescription = schemeDescription;
    }

    public String doViewDeleteIssueTypeScreenScheme()
    {
        validateForDelete();

        if (!invalidInput())
        {
            return "confirm";
        }

        return getResult();
    }

    @RequiresXsrfCheck
    public String doDeleteIssueTypeScreenScheme()
    {
        validateForDelete();

        if (!invalidInput())
        {
            getIssueTypeScreenScheme().remove();
            return redirectToView();
        }

        return getResult();
    }

    private void validateForDelete()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
        }
        else if (getIssueTypeScreenScheme() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.invalid.id2"));
        }
        else if (getIssueTypeScreenScheme().isDefault())
        {
            addErrorMessage(getText("admin.errors.issuetypescreenschemes.cannot.delete.default"));
        }
        else if (getProjects(getIssueTypeScreenScheme()).size() > 0)
        {
            addErrorMessage(getText("admin.errors.issuetypescreenschemes.cannot.delete.used.by.project"));
        }
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme()
    {
        if (issueTypeScreenScheme == null)
        {
            issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(getId());
        }

        return issueTypeScreenScheme;
    }

    public Collection getFieldScreenSchemes()
    {
        if (fieldScreenSchemes == null)
        {
            fieldScreenSchemes = fieldScreenSchemeManager.getFieldScreenSchemes();
        }

        return fieldScreenSchemes;
    }

    public Collection getProjects(IssueTypeScreenScheme issueTypeScreenScheme)
    {
        if (!projectsMap.containsKey(issueTypeScreenScheme.getId()))
        {
            projectsMap.put(issueTypeScreenScheme.getId(), issueTypeScreenScheme.getProjects());
        }

        return (Collection) projectsMap.get(issueTypeScreenScheme.getId());
    }
}
