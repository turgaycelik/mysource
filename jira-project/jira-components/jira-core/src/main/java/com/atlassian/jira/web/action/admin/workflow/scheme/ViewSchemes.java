package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.Lists;

import java.util.List;

@WebSudoRequired
public class ViewSchemes extends JiraWebActionSupport
{
    private final WorkflowSchemeManager workflowSchemeManager;
    private final ConstantsManager constantsManager;

    private List<AssignableWorkflowScheme> activeSchemes;
    private List<AssignableWorkflowScheme> inactiveSchemes;

    public ViewSchemes(final WorkflowSchemeManager workflowSchemeManager, ConstantsManager constantsManager)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.constantsManager = constantsManager;
    }

    @Override
    protected String doExecute() throws Exception
    {
        activeSchemes = Lists.newArrayList();
        inactiveSchemes = Lists.newArrayList();
        for (AssignableWorkflowScheme scheme : workflowSchemeManager.getAssignableSchemes())
        {
            if (workflowSchemeManager.isActive(scheme))
            {
                activeSchemes.add(scheme);
            }
            else
            {
                inactiveSchemes.add(scheme);
            }
        }

        return super.doExecute();
    }

    public boolean isHasScheme()
    {
        return !getActiveWorkflowSchemes().isEmpty() || !getInactiveWorkflowSchemes().isEmpty();
    }

    public List<AssignableWorkflowScheme> getActiveWorkflowSchemes()
    {
        return activeSchemes;
    }

    public List<AssignableWorkflowScheme> getInactiveWorkflowSchemes()
    {
        return inactiveSchemes;
    }

    public List<Project> getProjects(AssignableWorkflowScheme scheme)
    {
        return workflowSchemeManager.getProjectsUsing(scheme);
    }

    public DraftWorkflowScheme getDraftFor(AssignableWorkflowScheme scheme)
    {
        return workflowSchemeManager.getDraftForParent(scheme);
    }

    public IssueType getIssueType(String type)
    {
        return constantsManager.getIssueTypeObject(type);
    }
}
