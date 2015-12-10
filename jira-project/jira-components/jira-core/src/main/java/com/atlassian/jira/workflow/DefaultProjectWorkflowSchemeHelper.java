package com.atlassian.jira.workflow;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.4
 */
public class DefaultProjectWorkflowSchemeHelper implements ProjectWorkflowSchemeHelper
{
    private final ProjectService projectService;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final JiraAuthenticationContext authenticationContext;

    public DefaultProjectWorkflowSchemeHelper(ProjectService projectService, WorkflowSchemeManager workflowSchemeManager,
            JiraAuthenticationContext authenticationContext)
    {
        this.projectService = projectService;
        this.workflowSchemeManager = workflowSchemeManager;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public List<Project> getProjectsForScheme(Long schemeId)
    {
        final List<Project> projects = new ArrayList<Project>();
        for (final Project project : getAllProjects())
        {
            Long id = getSchemeIdForProject(project);
            if (id == null)
            {
                if (schemeId == null)
                {
                    projects.add(project);
                }
            }
            else
            {
                if (id.equals(schemeId))
                {
                    projects.add(project);
                }
            }
        }
        return projects;
    }

    @Override
    public List<Project> getProjectsForWorkflow(String workflowName)
    {
        Multimap<String, Project> workflow = getProjectsForWorkflow(Collections.singleton(workflowName));
        return Lists.newArrayList(workflow.get(workflowName));
    }

    @Override
    public Multimap<String, Project> getProjectsForWorkflow(Set<String> workflows)
    {
        //We need to ensure that the projects are kept in insertion order so they are ordered correctly.
        final SetMultimap<String, Project> result = LinkedHashMultimap.create();
        for (final Project project : getAllProjects())
        {
            final Map<String, String> workflowMap = workflowSchemeManager.getWorkflowMap(project);
            String defaultWorkflow = workflowMap.get(null);
            if (defaultWorkflow == null)
            {
                defaultWorkflow = JiraWorkflow.DEFAULT_WORKFLOW_NAME;
            }

            for (final IssueType type : project.getIssueTypes())
            {
                String workflow = workflowMap.get(type.getId());
                if (workflow == null)
                {
                    workflow = defaultWorkflow;
                }
                if (workflows.contains(workflow))
                {
                    result.put(workflow, project);
                }
            }
        }
        return result;
    }

    private List<Project> getAllProjects()
    {
        ServiceOutcome<List<Project>> projectsForAction = projectService.getAllProjectsForAction(authenticationContext.getLoggedInUser(), ProjectAction.EDIT_PROJECT_CONFIG);
        if (projectsForAction.isValid())
        {
            //Projects are already sorted apparently. So as long as we keep this order in our returned lists and maps
            //everything should remain sorted.
            return projectsForAction.getReturnedValue();
        }
        else
        {
            return Collections.emptyList();
        }
    }

    private Long getSchemeIdForProject(Project project)
    {
        try
        {
            GenericValue workflowScheme = workflowSchemeManager.getWorkflowScheme(project.getGenericValue());
            if (workflowScheme == null)
            {
                return null;
            }
            else
            {
                return workflowScheme.getLong("id");
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }
}
