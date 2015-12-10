package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorComparator;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @since v4.4
 */
@Path ("project/{projectIdOrKey}/role")
@AnonymousAllowed
@Consumes ( { MediaType.APPLICATION_JSON })
@Produces ( { MediaType.APPLICATION_JSON })
public class ProjectRoleResource
{
    ProjectRoleService projectRoleService;
    AvatarService avatarService;
    ProjectFinder projectFinder;
    JiraAuthenticationContext authContext;
    UriInfo uriInfo;
    UserManager userManager;
    ProjectRoleBeanFactory projectRoleBeanFactory;

    @SuppressWarnings ( { "UnusedDeclaration" })
    private ProjectRoleResource() {}

    @SuppressWarnings ( { "UnusedDeclaration" })
    public ProjectRoleResource(final ProjectRoleService projectRoleService, final AvatarService avatarService, final ProjectFinder projectFinder, final JiraAuthenticationContext authContext, final UriInfo uriInfo, final UserManager userManager, final ProjectRoleBeanFactory projectRoleBeanFactory)
    {
        this.projectRoleService = projectRoleService;
        this.avatarService = avatarService;
        this.projectFinder = projectFinder;
        this.authContext = authContext;
        this.uriInfo = uriInfo;
        this.userManager = userManager;
        this.projectRoleBeanFactory = projectRoleBeanFactory;
    }

    /**
     * Contains a list of roles in this project with links to full details.
     *
     * @param projectIdOrKey the project id or project key
     * @return list of roles and URIs to full details
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project exists and the user has permission to view it. A maps of roles to URIs containing
     *      full details for that role.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectRoleResource#GET_ROLES_DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the project is not found, or the calling user does not have permission to view it.
     */
    @GET
    public Response getProjectRoles(@PathParam ("projectIdOrKey") final String projectIdOrKey)
    {
        final Project project = getProjectByIdOrKey(projectIdOrKey);
        final Map<String, URI> roles = new HashMap<String, URI>();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final Collection<ProjectRole> projectRoles = projectRoleService.getProjectRoles(errorCollection);
        if (!errorCollection.hasAnyErrors())
        {
            for (ProjectRole projectRole : projectRoles)
            {
                final URI uri = projectRoleBeanFactory.projectRole(project, projectRole).self;
                roles.put(projectRole.getName(), uri);
            }
        }

        return Response.ok(roles).build();
    }

    public static Map<String, String> GET_ROLES_DOC_EXAMPLE;
    static {
        GET_ROLES_DOC_EXAMPLE = MapBuilder.<String, String>newBuilder()
                .add("Developers", Examples.restURI("project", "MKY", "role", "10000").toString())
                .add("Users", Examples.restURI("project", "MKY", "role", "10001").toString())
                .add("Administrators", Examples.restURI("project", "MKY", "role", "10002").toString())
                .toMap();
    }

    /**
     * Details on a given project role.
     *
     * @param projectIdOrKey the project id or project key
     * @param id the project role id
     * @return full details on the role and its actors. Actors are sorted by their display name.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the project and role exists and the user has permission to view it. Contains the role name,
     *      description, and project actors.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectRoleBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the project or role is not found, or the calling user does not have permission to view it.
     */
    @GET
    @Path ("{id}")
    public Response getProjectRole(@PathParam ("projectIdOrKey") final String projectIdOrKey, @PathParam ("id") final Long id)
    {
        final Project project = getProjectByIdOrKey(projectIdOrKey);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final ProjectRole projectRole = projectRoleService.getProjectRole(id, errorCollection);
        checkForErrors(errorCollection);

        final SimpleErrorCollection projectActorsErrorCollection = new SimpleErrorCollection();
        final ProjectRoleActors projectRoleActors = projectRoleService.getProjectRoleActors(projectRole, project, projectActorsErrorCollection);
        checkForErrors(projectActorsErrorCollection);

        final ProjectRoleBean projectRoleBean = projectRoleBeanFactory.projectRole(project, projectRole, projectRoleActors, authContext.getLoggedInUser());

        return Response.ok(projectRoleBean).build();
    }

    /**
     * Updates a project role to contain the sent actors.
     *
     * @param projectIdOrKey the project id or project key
     * @param id the project role id
     * @param actors the actors to set for the role
     * @return full details on the role and its actors after modification
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @request.representation.example
     *      { "user" : ["admin"] } or
     *{ "group" : ["jira-developers"] }
     *
     * @response.representation.200.doc
     *      Returned if the project and role exists and the user has permission to view it. Contains the role name,
     *      description, and project actors.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectRoleBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the actor could not be added to the project role
     */
    @PUT
    @Path ("{id}")
    public Response setActors(@PathParam ("projectIdOrKey") final String projectIdOrKey, @PathParam ("id") final Long id, ProjectRoleActorsUpdateBean actors)
    {
        final Project project = getProjectByIdOrKey(projectIdOrKey);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final ProjectRole projectRole = projectRoleService.getProjectRole(id, errorCollection);
        checkForErrors(errorCollection);

        final Map<String, String[]> simpleActors = actors.getCategorisedActors();
        final String[] usernames = simpleActors.get(UserRoleActorFactory.TYPE);
        final String[] groupnames = simpleActors.get(GroupRoleActorFactory.TYPE);

        if (usernames == null && groupnames == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final Map<String, Set<String>> newRoleActors = Maps.newHashMap();
        if (usernames != null)
        {
            newRoleActors.put(UserRoleActorFactory.TYPE, userkeysFromUsernames(usernames));
        }

        if (groupnames != null)
        {
            newRoleActors.put(GroupRoleActorFactory.TYPE, Sets.newHashSet(Arrays.asList(groupnames)));
        }
        final SimpleErrorCollection setActorErrorCollection = new SimpleErrorCollection();
        projectRoleService.setActorsForProjectRole(newRoleActors, projectRole, project, setActorErrorCollection);
        checkForErrors(setActorErrorCollection);

        return getProjectRole(projectIdOrKey, id);
    }

    private HashSet<String> userkeysFromUsernames(String[] usernames)
    {
        final HashSet<String> userKeys = new HashSet<String>();
        for (String username : usernames)
        {
            final ApplicationUser applicationUser = userManager.getUserByName(username);
            if (applicationUser != null)
            {
                userKeys.add(applicationUser.getKey());
            }
        }
        return userKeys;
    }

    /**
     * Add an actor to a project role.
     *
     * @param projectIdOrKey the project id or project key
     * @param id the project role id
     * @param actors the actors to add to the role
     * @return full details on the role and its actors after modification
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @request.representation.example
     *      { "user" : ["admin"] }  or
     *{ "group" : ["jira-developers"] }
     *
     * @response.representation.200.doc
     *      Returned if the project and role exists and the user has permission to view it. Contains the role name,
     *      description, and project actors.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.project.ProjectRoleBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the actor could not be added to the project role
     */
    @POST
    @Path ("{id}")
    public Response addActorUsers(@PathParam ("projectIdOrKey") final String projectIdOrKey, @PathParam ("id") final Long id,
            ActorsMap actors)
    {
        final Project project = getProjectByIdOrKey(projectIdOrKey);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final ProjectRole projectRole = projectRoleService.getProjectRole(id, errorCollection);
        checkForErrors(errorCollection);

        final String[] usernames = actors.get("user");
        final String[] groupnames = actors.get("group");

        if (usernames == null && groupnames == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (usernames != null)
        {
            final SimpleErrorCollection addActorErrorCollection = new SimpleErrorCollection();
            projectRoleService.addActorsToProjectRole(Arrays.asList(usernames), projectRole, project, UserRoleActorFactory.TYPE, addActorErrorCollection);
            checkForErrors(addActorErrorCollection);
        }

        if (groupnames != null)
        {
            final SimpleErrorCollection addActorErrorCollection = new SimpleErrorCollection();
            projectRoleService.addActorsToProjectRole(Arrays.asList(groupnames), projectRole, project, GroupRoleActorFactory.TYPE, addActorErrorCollection);
            checkForErrors(addActorErrorCollection);
        }

        return getProjectRole(projectIdOrKey, id);
    }

    /**
     * Remove actors from a project role.
     *
     * @param projectIdOrKey the project id or project key
     * @param id the project role id
     * @param username the username to remove from the project role
     * @param groupname the groupname to remove from the project role
     * @return no content on success
     *
     * @response.representation.204.doc
     *      Returned if the actor was successfully removed from the project role.
     *
     * @response.representation.404.doc
     *      Returned if the project or role is not found, the calling user does not have permission to view it, or does
     *      not have permission to modify the actors in the project role.
     */
    @DELETE
    @Path ("{id}")
    public Response deleteActor(@PathParam ("projectIdOrKey") final String projectIdOrKey, @PathParam ("id") final Long id, @QueryParam ("user") @Nullable final String username, @QueryParam ("group") @Nullable final String groupname)
    {
        final Project project = getProjectByIdOrKey(projectIdOrKey);

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final ProjectRole projectRole = projectRoleService.getProjectRole(id, errorCollection);
        checkForErrors(errorCollection);

        if (username == null && groupname == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (username != null)
        {
            final SimpleErrorCollection addActorErrorCollection = new SimpleErrorCollection();
            projectRoleService.removeActorsFromProjectRole(CollectionBuilder.list(username), projectRole, project, UserRoleActorFactory.TYPE, addActorErrorCollection);
            checkForErrors(addActorErrorCollection);
        }

        if (groupname != null)
        {
            final SimpleErrorCollection addActorErrorCollection = new SimpleErrorCollection();
            projectRoleService.removeActorsFromProjectRole(CollectionBuilder.list(groupname), projectRole, project, GroupRoleActorFactory.TYPE, addActorErrorCollection);
            checkForErrors(addActorErrorCollection);
        }

        return Response.noContent().build();
    }

    private Project getProjectByIdOrKey(String projectIdOrKey)
    {
        final ProjectService.GetProjectResult result = projectFinder.getGetProjectForActionByIdOrKey(authContext.getUser(),
                projectIdOrKey, ProjectAction.EDIT_PROJECT_CONFIG);

        if (result.getErrorCollection().hasAnyErrors())
        {
            final ErrorCollection errors = ErrorCollection.of(result.getErrorCollection());

            if (result.getErrorCollection().getReasons().contains(com.atlassian.jira.util.ErrorCollection.Reason.FORBIDDEN))
            {
                throw new NotAuthorisedWebException(errors);
            }
            else
            {
                throw new NotFoundWebException(errors);
            }
        }

        return result.getProject();
    }

    private void checkForErrors(SimpleErrorCollection errorCollection)
    {
        if (errorCollection.hasAnyErrors())
        {
            throw new NotFoundWebException(ErrorCollection.of(errorCollection));
        }
    }

    //JRADEV-12212: We have this concrete class so get around warnings generated by Jersy. A concrete class gives
    //Jersy access to the generic types.
    public static class ActorsMap extends HashMap<String, String[]>
    {
    }
}
