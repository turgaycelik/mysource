package com.atlassian.jira.rest.v2.issue;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.group.GroupService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.exception.ServerErrorWebException;
import com.atlassian.jira.rest.util.ResponseUtils;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.google.common.collect.Collections2.filter;

/**
 * @since v6.0
 */
@Path ("group")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class GroupResource
{

    static final int MAX_EXPANDED_USERS_COUNT = 50;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authContext;
    private final I18nHelper i18n;
    private final GroupManager groupManager;
    private final GroupService groupService;
    private final JiraBaseUrls jiraBaseUrls;
    private final CrowdService crowdService;
    private final EmailFormatter emailFormatter;

    private final Predicate<User> UserIsActivePredicate = new Predicate<User>()
    {
        @Override
        public boolean apply(@Nullable User user)
        {
            return (user != null) && user.isActive();
        }
    };

    public GroupResource(PermissionManager permissionManager, JiraAuthenticationContext authContext, I18nHelper i18n,
            GroupManager groupManager, final GroupService groupService, JiraBaseUrls jiraBaseUrls, final CrowdService crowdService, final EmailFormatter emailFormatter)
    {
        this.permissionManager = permissionManager;
        this.authContext = authContext;
        this.i18n = i18n;
        this.groupManager = groupManager;
        this.groupService = groupService;
        this.jiraBaseUrls = jiraBaseUrls;
        this.crowdService = crowdService;
        this.emailFormatter = emailFormatter;
    }

    /**
     * Returns REST representation for the requested group. Allows to get list of active users belonging to the
     * specified group and its subgroups if "users" expand option is provided. You can page through users list by using
     * indexes in expand param. For example to get users from index 10 to index 15 use "users[10:15]" expand value. This
     * will return 6 users (if there are at least 16 users in this group). Indexes are 0-based and inclusive.
     *
     * @param groupName A name of requested group.
     * @param expand List of fields to expand. Currently only available expand is "users".
     * @return REST representation of a group
     * @since 6.0
     *
     * @response.representation.200.qname group
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns full representation of a JIRA group in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.GroupBean#DOC_EXAMPLE_WITH_USERS}
     *
     * @response.representation.400.doc
     *     Returned if user requested an empty group name.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @response.representation.403.doc
     *     Returned if the current user does not have administrator permissions.
     *
     * @response.representation.404.doc
     *     Returned if the requested group was not found.
     */
    @GET
    public Response getGroup(@QueryParam ("groupname") final String groupName, @QueryParam ("expand") StringList expand)
    {
        final ApplicationUser remoteUser = authContext.getUser();
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser)
                || permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, remoteUser)))
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("rest.authorization.admin.required")));
        }

        validateGroupName(groupName);

        final Group group = groupManager.getGroup(groupName.trim());
        if (group == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.group.error.not.found", groupName)));
        }

        final GroupBean groupBean = buildGroupBean(group);
        return Response.ok(groupBean).build();
    }

    /**
     * Creates a group by given group parameter
     *
     * Returns REST representation for the requested group.
     *
     * @param groupBean a group to add
     * @return REST representation of a group
     * @since 6.1
     *
     * @response.representation.201.qname group
     *
     * @response.representation.201.mediaType application/json
     *
     * @response.representation.201.doc
     *      Returns full representation of a JIRA group in JSON format.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.GroupBean#DOC_EXAMPLE_WITH_USERS}
     *
     * @response.representation.400.doc
     *     Returned if user requested an empty group name or group already exists
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @response.representation.403.doc
     *     Returned if the current user does not have administrator permissions.
     *
     * @response.representation.500.doc
     *     Returned if the operation is not permitted or error occurs while creating the group.
     */
    @POST
    @WebSudoRequired
    @ExperimentalApi
    public Response createGroup(final AddGroupBean groupBean)
    {
        final String groupName = groupBean.getName();
        final Response response = doGroupUpdate(new GroupUpdateCommand(groupName)
        {
            @Override
            public Response execute() throws OperationNotPermittedException, InvalidGroupException
            {
                validateGroupName(groupName);

                if (crowdService.getGroup(groupName) != null)
                {
                    // Group exists
                    throw new BadRequestWebException(ErrorCollection.of(i18n.getText("groupbrowser.error.group.exists")));
                }

                final ImmutableGroup immutableGroup = new ImmutableGroup(groupName);
                crowdService.addGroup(immutableGroup);

                final GroupBean responseGroupBean = buildGroupBean(immutableGroup);
                return Response.status(Response.Status.CREATED)
                        .location(responseGroupBean.getSelf()).entity(responseGroupBean)
                        .cacheControl(never()).build();
            }
        });

        return response;
    }

    /**
     * Deletes a group by given group parameter.
     *
     * Returns no content
     *
     * @param groupName a group to delete
     * @param swapGroup a group to transfer visibility restrictions of the group that is being deleted
     * @return no content
     * @since 6.1
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returned if the group was deleted.
     *
     * @response.representation.400.doc
     *     Returned if user requested an group that does not exist.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @response.representation.403.doc
     *     Returned if the current user does not have administrator permissions.
     *
     * @response.representation.404.doc
     *     Returned if the requested group was not found.
     *
     * @response.representation.500.doc
     *      Returned if the operation is not permitted or error occurs while deleting the group.
     */
    @DELETE
    @WebSudoRequired
    @ExperimentalApi
    public Response removeGroup(@QueryParam ("groupname") final String groupName, @QueryParam ("swapGroup") final String swapGroup)
    {
        final Response response = doGroupUpdate(new GroupUpdateCommand(groupName)
        {
            @Override
            public Response execute() throws OperationNotPermittedException
            {
                ensureGroupExists(groupName);

                final JiraServiceContextImpl validateContext = new JiraServiceContextImpl(authContext.getUser());
                if (!groupService.validateDelete(validateContext, groupName, swapGroup))
                {
                    return ResponseUtils.throwEx(validateContext.getErrorCollection());
                }

                final JiraServiceContextImpl deleteContext = new JiraServiceContextImpl(authContext.getUser());
                if (!groupService.delete(deleteContext, groupName, swapGroup))
                {
                    return ResponseUtils.throwEx(deleteContext.getErrorCollection());
                }

                return Response.ok().cacheControl(never()).build();
            }
        });
        return response;
    }

    /**
     * Adds given user to a group.
     *
     * Returns the current state of the group.
     *
     * @param groupName A name of requested group.
     * @param userBean User to add to a group
     * @return REST representation of a group
     * @since 6.1
     *
     * @response.representation.201.qname group
     *
     * @response.representation.201.mediaType application/json
     *
     * @response.representation.201.doc
     *      Returns full representation of a JIRA group in JSON format.
     *
     * @response.representation.400.doc
     *     Returned if user requested an empty group name or the user already belongs to the group.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @response.representation.403.doc
     *     Returned if the current user does not have administrator permissions.
     *
     * @response.representation.404.doc
     *     Returned if the requested group was not found or requested user was not found.
     *
     * @response.representation.500.doc
     *     Returned if the operation is not permitted or error occurs while adding user the group.
     */
    @POST
    @WebSudoRequired
    @ExperimentalApi
    @Path ("user")
    public Response addUserToGroup(@QueryParam ("groupname") final String groupName, final UpdateUserToGroupBean userBean)
    {
        final Response response = doGroupUpdate(new GroupUpdateCommand(groupName)
        {
            @Override
            public Response execute() throws OperationNotPermittedException
            {
                ensureGroupExists(groupName);
                final String username = userBean.getName();

                final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authContext.getUser());
                if (!groupService.validateAddUserToGroup(serviceContext, ImmutableList.of(groupName), username))
                {
                    return ResponseUtils.throwEx(serviceContext.getErrorCollection());
                }

                final User crowdUser = getUser(username);

                final Group immutableGroup = new ImmutableGroup(groupName);
                final User immutableUser = ImmutableUser.newUser(crowdUser).toUser();
                if (crowdService.addUserToGroup(immutableUser, immutableGroup))
                {
                    final GroupBean responseGroupBean = buildGroupBean(immutableGroup);
                    return Response.status(Response.Status.CREATED)
                            .location(responseGroupBean.getSelf()).entity(responseGroupBean)
                            .cacheControl(never()).build();
                }
                else
                {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity(i18n.getText("rest.group.user.already.exists.in.group", username, groupName))
                            .cacheControl(never()).build();
                }
            }
        });

        return response;
    }

    /**
     * Removes given user from a group.
     *
     * Returns no content
     *
     * @param groupName A name of requested group.
     * @param username User to remove from a group
     * @return REST representation of a group
     * @since 6.1
     *
     * @response.representation.200.qname group
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      If the user was removed from the group.
     *
     * @response.representation.400.doc
     *     Returned if user requested an empty group name
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @response.representation.403.doc
     *     Returned if the current user does not have administrator permissions.
     *
     * @response.representation.404.doc
     *     Returned if the requested group was not found or the requested user wan not found
     *
     * @response.representation.500.doc
     *      Returned if the operation is not permitted or error occurs while removing user from the group.
     */
    @WebSudoRequired
    @ExperimentalApi
    @DELETE
    @Path ("user")
    public Response removeUserFromGroup(@QueryParam ("groupname") final String groupName, @QueryParam ("username") final String username)
    {
        final Response response = doGroupUpdate(new GroupUpdateCommand(groupName)
        {
            @Override
            public Response execute() throws OperationNotPermittedException
            {
                ensureGroupExists(groupName);

                // Get user before group op. validation to get 404 code if user does not exist
                final User crowdUser = getUser(username);

                final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authContext.getUser());
                if (!groupService.validateRemoveUserFromGroups(serviceContext, ImmutableList.of(groupName), username))
                {
                    return ResponseUtils.throwEx(serviceContext.getErrorCollection());
                }


                final ImmutableGroup immutableGroup = new ImmutableGroup(groupName);
                final User immutableUser = ImmutableUser.newUser(crowdUser).toUser();
                crowdService.removeUserFromGroup(immutableUser, immutableGroup);
                return Response.ok().cacheControl(never()).build();
            }
        });

        return response;
    }

    private Response doGroupUpdate(final GroupUpdateCommand command)
    {

        ensureCanManageGroups();
        final String groupName = command.getGroupName();

        try
        {
            return command.execute();
        }
        catch (OperationNotPermittedException e)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("generic.error",  e.getLocalizedMessage())));
        }
        catch (InvalidGroupException e)
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("generic.error",  e.getLocalizedMessage())));
        }
        catch (OperationFailedException e)
        {
            throw new ServerErrorWebException(ErrorCollection.of(i18n.getText("generic.error", e.getLocalizedMessage())));
        }
    }

    private void ensureGroupExists(final String groupName)
    {
        validateGroupName(groupName);
        final Group crowdGroup = crowdService.getGroup(groupName);
        if (crowdGroup == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.group.error.not.found", groupName)));
        }
    }

    private User getUser(final String username)
    {
        User crowdUser = null;
        if (username != null)
        {
            crowdUser = crowdService.getUser(username);
            if (crowdUser == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.errors.user.does.not.exist", username)));
            }
        }
        return crowdUser;
    }

    private void ensureCanManageGroups()
    {
        final ApplicationUser remoteUser = authContext.getUser();
        if (!(permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser)
                || permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, remoteUser)))
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("rest.authorization.admin.required")));
        }
    }

    private void validateGroupName(final String groupName)
    {
        if (StringUtils.isEmpty(groupName))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("rest.group.error.empty")));
        }
    }

    private GroupBean buildGroupBean(final Group group)
    {
        final ImmutableList<User> usersInGroup = ImmutableList.copyOf(filter(groupManager.getUsersInGroup(group), UserIsActivePredicate));
        final UserJsonBeanListWrapper userJsonBeanListWrapper = new UserJsonBeanListWrapper(jiraBaseUrls, usersInGroup, MAX_EXPANDED_USERS_COUNT, authContext.getUser(), emailFormatter);
        return new GroupBeanBuilder(jiraBaseUrls, group.getName()).users(userJsonBeanListWrapper).build();
    }

    private static class GroupUpdateCommand
    {
        private final String groupName;

        private GroupUpdateCommand(final String groupName) {
            this.groupName = groupName;
        }

        private String getGroupName()
        {
            return groupName;
        }

        Response execute() throws OperationNotPermittedException, InvalidGroupException
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
