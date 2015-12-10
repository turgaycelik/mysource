package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.project.index.ProjectReindexService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @since v6.1
 */
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
@Consumes ({ MediaType.APPLICATION_JSON })
@Path ("project/{projectId}/key")
public class ProjectKeyBackdoorExt
{
    private final ProjectService projectService;
    private final ProjectManager projectManager;
    private final ProjectReindexService projectReindexService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectKeyBackdoorExt(ProjectService projectService, ProjectManager projectManager,
                                 ProjectReindexService projectReindexService,
                                 JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.projectService = projectService;
        this.projectManager = projectManager;
        this.projectReindexService = projectReindexService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @PUT
    public Response editProjectKey(@PathParam ("projectId") Long projectId, String newProjectKey)
    {
        Project project = projectManager.getProjectObj(projectId);
        projectService.updateProject(
                new ProjectService.UpdateProjectValidationResult(new SimpleErrorCollection(),
                        project.getName(), newProjectKey, project.getDescription(),
                        project.getLeadUserKey(), project.getUrl(),
                        project.getAssigneeType(), project.getAvatar().getId(), project, true,
                        jiraAuthenticationContext.getUser()
                ));
        projectManager.refresh();
        projectReindexService.reindex(project);
        return Response.ok().build();
    }
}
