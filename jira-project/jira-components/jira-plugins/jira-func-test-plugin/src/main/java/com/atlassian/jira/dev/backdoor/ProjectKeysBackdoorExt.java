package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
@Path ("project/{projectId}/keys")
public class ProjectKeysBackdoorExt
{
    private final ProjectManager projectManager;
    private final ProjectKeyStore projectKeyStore;

    public ProjectKeysBackdoorExt(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
        this.projectKeyStore = ComponentAccessor.getComponent(ProjectKeyStore.class);
    }

    @PUT
    public Response addProjectKey(@PathParam ("projectId") Long projectId, String projectKey)
    {
        projectKeyStore.addProjectKey(projectId, projectKey);
        projectManager.refresh();
        return Response.ok().build();
    }

    @GET
    public Response getProjectKeys(@PathParam ("projectId") Long projectId)
    {
        final Multimap<Long, String> allKeys = Multimaps.invertFrom(Multimaps.forMap(projectKeyStore.getAllProjectKeys()), HashMultimap.<Long, String>create());

        return Response.ok(Lists.newArrayList(allKeys.get(projectId))).build();
    }

}
