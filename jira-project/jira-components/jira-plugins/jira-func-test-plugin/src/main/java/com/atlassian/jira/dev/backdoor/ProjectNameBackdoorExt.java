package com.atlassian.jira.dev.backdoor;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

/**
 * @since v6.3
 */
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
@Consumes ({ MediaType.APPLICATION_JSON })
@Path ("project/{projectId}/name")
public class ProjectNameBackdoorExt
{
    private final ProjectManager projectManager;

    public ProjectNameBackdoorExt(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    @GET
    public Response getProjectId(@PathParam ("projectId") Long projectId)
    {
        Project project = projectManager.getProjectObj(projectId);
        return Response.ok(project.getName()).build();
    }
}
