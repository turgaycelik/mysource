package com.atlassian.jira.web.action.admin.issuefields.screens.enterprise;

import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditIssueTypeScreenScheme extends JiraWebActionSupport
{
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenSchemeManager fieldScreenSchemeManager;
    private Collection issueTypeScreenSchemes;
    private Long id;
    private String schemeName;
    private String schemeDescription;
    private IssueTypeScreenScheme issueTypeScreenScheme;
    private boolean edited;

    public EditIssueTypeScreenScheme(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, FieldScreenSchemeManager fieldScreenSchemeManager)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenSchemeManager = fieldScreenSchemeManager;
    }

    public String doDefault() throws Exception
    {
        validateId();

        if (!invalidInput())
        {
            setSchemeName(getIssueTypeScreenScheme().getName());
            setSchemeDescription(getIssueTypeScreenScheme().getDescription());
        }

        return INPUT;
    }

    protected void validateId()
    {
        if (getId() == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
        }
        else if (getIssueTypeScreenScheme() == null)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.invalid.id2"));
        }
    }

    protected void doValidation()
    {
        validateId();

        if (!invalidInput())
        {
            validateName(true);
        }
    }

    protected void validateName(boolean ignoreSameScheme)
    {
        if (!TextUtils.stringSet(getSchemeName()))
        {
            addError("schemeName", getText("admin.common.errors.validname"));
        }
        else
        {
            for (IssueTypeScreenScheme issueTypeScreenScheme : issueTypeScreenSchemeManager.getIssueTypeScreenSchemes())
            {
                if (((!ignoreSameScheme) || (ignoreSameScheme) && !getId().equals(issueTypeScreenScheme.getId())) && getSchemeName().equals(issueTypeScreenScheme.getName()))
                {
                    addError("schemeName", getText("admin.errors.screens.duplicate.screen.scheme.name"));
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getIssueTypeScreenScheme().setName(getSchemeName());
        getIssueTypeScreenScheme().setDescription(getSchemeDescription());
        getIssueTypeScreenScheme().store();
        return redirectToView();
    }

    public Collection getIssueTypeScreenSchemes()
    {
        if (issueTypeScreenSchemes == null)
        {
            issueTypeScreenSchemes = issueTypeScreenSchemeManager.getIssueTypeScreenSchemes();
        }

        return issueTypeScreenSchemes;
    }

    protected String redirectToView()
    {
        return getRedirect("ViewIssueTypeScreenSchemes.jspa");
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
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


    public IssueTypeScreenScheme getIssueTypeScreenScheme()
    {
        if (issueTypeScreenScheme == null)
        {
            issueTypeScreenScheme = issueTypeScreenSchemeManager.getIssueTypeScreenScheme(getId());
        }

        return issueTypeScreenScheme;
    }

    public String doViewCopyIssueTypeScreenScheme()
    {
        validateId();

        if (!invalidInput())
        {
            // Get a name and description for the new scheme
            setSchemeName(getText("common.words.copyof",getIssueTypeScreenScheme().getName()));
            setSchemeDescription(getIssueTypeScreenScheme().getDescription());
            return INPUT;
        }

        return getResult();
    }

    @RequiresXsrfCheck
    public String doCopyIssueTypeScreenScheme()
    {
        validateId();

        if (!invalidInput())
        {
            validateName(false);
            if (!invalidInput())
            {
                // Create a copy of the scheme
                IssueTypeScreenScheme issueTypeScreenScheme = new IssueTypeScreenSchemeImpl(issueTypeScreenSchemeManager, null);
                issueTypeScreenScheme.setName(getSchemeName());
                issueTypeScreenScheme.setDescription(getSchemeDescription());
                issueTypeScreenScheme.store();

                // Copy scheme entities
                for (IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity : getIssueTypeScreenScheme().getEntities())
                {
                    IssueTypeScreenSchemeEntity copyIssueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(issueTypeScreenSchemeManager, issueTypeScreenSchemeEntity, fieldScreenSchemeManager, getConstantsManager());
                    issueTypeScreenScheme.addEntity(copyIssueTypeScreenSchemeEntity);
                }

                return redirectToView();
            }
        }

        return getResult();
    }

    public boolean isEdited()
    {
        return edited;
    }

    public void setEdited(boolean edited)
    {
        this.edited = edited;
    }
}
