/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.ProjectFieldLayoutSchemeHelper;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.action.admin.issuefields.enterprise.FieldLayoutSchemeHelper;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

@WebSudoRequired
public class ViewIssueFields extends AbstractConfigureFieldLayout
{
    private final ProjectFieldLayoutSchemeHelper helper;
    private EditableDefaultFieldLayout editableDefaultFieldLayout;
    private List<Project> projects;

    public ViewIssueFields(FieldScreenManager fieldScreenManager, RendererManager rendererManager,
            final ReindexMessageManager reindexMessageManager, final FieldManager fieldManager, final FieldLayoutSchemeHelper fieldLayoutSchemeHelper,
            HackyFieldRendererRegistry hackyFieldRendererRegistry, final ProjectFieldLayoutSchemeHelper helper,
            final ManagedConfigurationItemService managedConfigurationItemService)
    {
        super(fieldScreenManager, rendererManager, reindexMessageManager, fieldLayoutSchemeHelper, fieldManager, hackyFieldRendererRegistry, managedConfigurationItemService);
        this.helper = helper;
    }

    public EditableFieldLayout getFieldLayout()
    {
        if (editableDefaultFieldLayout == null)
        {
            try
            {
                editableDefaultFieldLayout = getFieldLayoutManager().getEditableDefaultFieldLayout();
            }
            catch (DataAccessException e)
            {
                log.error("Error while retrieving field layout.", e);
                addErrorMessage(getText("view.issue.error.retrieving.field.layout"));
            }
        }
        return editableDefaultFieldLayout;
    }

    protected String getFieldRedirect() throws Exception
    {
        return getRedirect("ViewIssueFields.jspa");
    }

    protected void store()
    {
        try
        {
            getFieldLayoutManager().storeEditableDefaultFieldLayout((EditableDefaultFieldLayout) getFieldLayout());
        }
        catch (DataAccessException e)
        {
            addErrorMessage(getText("admin.errors.fieldlayout.error.storing"));
        }
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final FieldLayout fieldLayout = getFieldLayoutManager().getFieldLayout();
            projects = helper.getProjectsForFieldLayout(fieldLayout);
        }
        return projects;
    }

}
