package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @since v6.1
 */
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
@Consumes ({ MediaType.APPLICATION_JSON })
@Path ("project/{projectKey}")
public class ProjectBackdoorExt
{
    private final ProjectManager projectManager;

    public ProjectBackdoorExt(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    @GET
    @Path ("id")
    public Response getProjectId(@PathParam ("projectKey") String key)
    {
        Project project = projectManager.getProjectObjByKey(key);
        return Response.ok(project.getId()).build();
    }

    @GET
    @Path ("category/name")
    public Response getProjectCategoryName(@PathParam ("projectKey") String key)
    {
        Project project = projectManager.getProjectObjByKey(key);
        if (project == null)
        {
            return Response.status(Response.Status.NOT_FOUND).entity("project with specified key cannot be found").build();
        }
        ProjectCategory category = project.getProjectCategoryObject();
        if (category == null)
        {
            return Response.status(Response.Status.NOT_FOUND).entity("project specified does not have a category").build();
        }
        return Response.ok(category.getName()).build();
    }
}
