package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.ProjectFieldScreenHelper;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

@WebSudoRequired
public class ConfigureFieldScreen extends JiraWebActionSupport
{
    private final FieldScreenManager fieldScreenManager;
    private final ProjectFieldScreenHelper helper;
    private final JiraWebResourceManager webResourceManager;

    private Long id;
    private FieldScreen fieldScreen;
    private List<Project> projects;

    public ConfigureFieldScreen(final FieldScreenManager fieldScreenManager, final ProjectFieldScreenHelper helper,
            final JiraWebResourceManager webResourceManager)
    {
        this.fieldScreenManager = fieldScreenManager;
        this.helper = helper;
        this.webResourceManager = webResourceManager;
    }

    public String doDefault() throws Exception
    {
        return doExecute();
    }

    protected String doExecute() throws Exception
    {
        webResourceManager.requireResource("com.atlassian.jira.jira-project-config-plugin:screens-editor");

        if (id == null)
        {
            addErrorMessage(getText("admin.errors.id.cannot.be.null"));
        }
        else
        {
            fieldScreen = fieldScreenManager.getFieldScreen(id);
            if (fieldScreen == null)
            {
                addErrorMessage(getText("admin.issuefields.screens.configure.no.screen", String.valueOf(id)));
            }
        }
        return getResult();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public FieldScreen getFieldScreen()
    {
        return fieldScreen;
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            projects = helper.getProjectsForFieldScreen(getFieldScreen());
        }
        return projects;
    }
}