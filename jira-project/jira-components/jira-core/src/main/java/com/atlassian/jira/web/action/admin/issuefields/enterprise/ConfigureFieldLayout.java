/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.ProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.action.admin.issuefields.AbstractConfigureFieldLayout;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

@WebSudoRequired
public class ConfigureFieldLayout extends AbstractConfigureFieldLayout
{
    private EditableFieldLayout editableSchemeFieldLayout = null;
    private FieldLayout schemeFieldLayout = null;
    private ProjectFieldLayoutSchemeHelper helper;
    private List<Project> projects;

    public ConfigureFieldLayout(FieldScreenManager fieldScreenManager, RendererManager rendererManager,
            final ReindexMessageManager reindexMessageManager, final FieldManager fieldManager, final FieldLayoutSchemeHelper fieldLayoutSchemeHelper,
            final HackyFieldRendererRegistry hackyFieldRendererRegistry, final ProjectFieldLayoutSchemeHelper helper,
            final ManagedConfigurationItemService managedConfigurationItemService)
    {
        super(fieldScreenManager, rendererManager, reindexMessageManager, fieldLayoutSchemeHelper, fieldManager, hackyFieldRendererRegistry, managedConfigurationItemService);
        this.helper = helper;
    }

    protected void doValidation()
    {
        // Check the scheme with specified id exists
        if (getId() != null)
        {
            if (getFieldLayout() == null)
            {
                addErrorMessage(getText("admin.errors.fieldlayout.invalid.field.config.id","'" + getId() + "'"));
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.fieldlayout.no.layout.specified"));
        }
    }

    public EditableFieldLayout getFieldLayout()
    {
        if (editableSchemeFieldLayout == null)
        {
            editableSchemeFieldLayout = getFieldLayoutManager().getEditableFieldLayout(getId());
        }
        return editableSchemeFieldLayout;
    }

    protected String getFieldRedirect() throws Exception
    {
        return getRedirect("ConfigureFieldLayout!default.jspa?id=" + getId());
    }

    protected void store()
    {
        try
        {
            getFieldLayoutManager().storeEditableFieldLayout(getFieldLayout());
        }
        catch (DataAccessException ex)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.error.storing"));
        }
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final FieldLayout fieldLayout = getNonEditableFieldLayout();
            projects = helper.getProjectsForFieldLayout(fieldLayout);
        }
        return projects;
    }

    private FieldLayout getNonEditableFieldLayout()
    {
        if(schemeFieldLayout == null)
        {
            schemeFieldLayout = getFieldLayoutManager().getFieldLayout(getId());
        }
        return schemeFieldLayout;
    }

}
