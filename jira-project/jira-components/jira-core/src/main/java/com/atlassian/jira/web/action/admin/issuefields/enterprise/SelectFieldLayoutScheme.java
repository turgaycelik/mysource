package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@SuppressWarnings ("UnusedDeclaration")
@WebSudoRequired
public class SelectFieldLayoutScheme extends JiraWebActionSupport
{
    private final ProjectManager projectManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final ReindexMessageManager reindexMessageManager;
    private final FieldLayoutSchemeHelper fieldLayoutSchemeHelper;

    private Long projectId;
    private Long schemeId;
    private List fieldLayoutSchemes;

    public SelectFieldLayoutScheme(ProjectManager projectManager, FieldLayoutManager fieldLayoutManager, final ReindexMessageManager reindexMessageManager, final FieldLayoutSchemeHelper fieldLayoutSchemeHelper)
    {
        this.projectManager = projectManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.reindexMessageManager = notNull("reindexMessageManager", reindexMessageManager);
        this.fieldLayoutSchemeHelper = notNull("fieldLayoutSchemeHelper", fieldLayoutSchemeHelper);
    }

    public String doDefault()
    {
        validateId();

        if (!invalidInput())
        {
            FieldConfigurationScheme fieldConfigurationScheme = fieldLayoutManager.getFieldConfigurationScheme(getProject());
            if (fieldConfigurationScheme != null)
            {
                setSchemeId(fieldConfigurationScheme.getId());
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
            if (getSchemeId() != null && getFieldConfigurationScheme() == null)
            {
                addError("schemeId", getText("admin.errors.fieldlayout.invalid.scheme.id"));
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
        FieldConfigurationScheme currentFieldConfigurationScheme = fieldLayoutManager.getFieldConfigurationScheme(getProject());
        Long currentFieldConfigurationSchemeId = currentFieldConfigurationScheme == null ? null : currentFieldConfigurationScheme.getId();
        Long newFieldConfigurationSchemeId = getSchemeId();

        // determine if we need to display a reindex message based on the scheme change
        if (fieldLayoutSchemeHelper.doesChangingFieldLayoutSchemeForProjectRequireMessage(getLoggedInUser(), getProjectId(), currentFieldConfigurationSchemeId, newFieldConfigurationSchemeId))
        {
            reindexMessageManager.pushMessage(getLoggedInUser(), "admin.notifications.task.field.configuration");
        }

        if (currentFieldConfigurationScheme != null)
        {
            fieldLayoutManager.removeSchemeAssociation(getProject(), currentFieldConfigurationSchemeId);
        }

        if (newFieldConfigurationSchemeId != null)
        {
            fieldLayoutManager.addSchemeAssociation(getProject(), newFieldConfigurationSchemeId);
        }

        return getRedirect("/plugins/servlet/project-config/" + getProject().getKey() + "/fields");
    }

    private FieldConfigurationScheme getFieldConfigurationScheme()
    {
        return fieldLayoutManager.getFieldConfigurationScheme(getSchemeId());
    }

    public Project getProject()
    {
        return projectManager.getProjectObj(getProjectId());
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

    public Collection getFieldLayoutSchemes()
    {
        if (fieldLayoutSchemes == null)
        {
            fieldLayoutSchemes = fieldLayoutManager.getFieldLayoutSchemes();
        }

        return fieldLayoutSchemes;
    }

}
