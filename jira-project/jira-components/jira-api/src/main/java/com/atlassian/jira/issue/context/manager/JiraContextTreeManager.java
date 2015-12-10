package com.atlassian.jira.issue.context.manager;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.project.ProjectManager;

public class JiraContextTreeManager
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ProjectManager projectManager;
    private final ConstantsManager constantsManager;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public JiraContextTreeManager(ProjectManager projectManager, ConstantsManager constantsManager)
    {
        this.projectManager = projectManager;
        this.constantsManager = constantsManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public JiraContextNode getRootNode()
    {
        return GlobalIssueContext.getInstance();
    }

    public void refresh()
    {
    }



    // ------------------------------------------------------------------------------------------ Private Helper Methods

    public static JiraContextNode getRootContext()
    {
        return GlobalIssueContext.getInstance();
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }
}
