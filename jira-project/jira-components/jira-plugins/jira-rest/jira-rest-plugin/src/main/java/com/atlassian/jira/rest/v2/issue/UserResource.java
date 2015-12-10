package com.atlassian.jira.rest.v2.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.fugue.Either;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarPickerHelper;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.fields.ColumnService;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.search.AssigneeService;
import com.atlassian.jira.event.user.UserAvatarUpdatedEvent;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.exception.ServerErrorWebException;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.rest.v2.issue.users.UserPickerResourceHelper;
import com.atlassian.jira.rest.v2.search.ColumnOptions;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static javax.ws.rs.core.Response.noContent;


/**
 * @since 4.2
 */
@Path ("user")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@AnonymousAllowed
public class UserResource
{
    private static final int MAX_LENGTH = 255;

    private final UserService userService;
    private final UserUtil userUtil;
    private final PasswordPolicyManager passwordPolicyManager;
    private final I18nHelper i18n;
    private final EmailFormatter emailFormatter;
    private final JiraAuthenticationContext authContext;
    private final TimeZoneManager timeZoneManager;
    private final AvatarService avatarService;
    private final AvatarResourceHelper avatarResourceHelper;
    private final UserPropertyManager userPropertyManager;
    private final PermissionManager permissionManager;
    private final ProjectService projectService;
    private final IssueService issueService;
    private final ProjectManager projectManager;
    private final AvatarManager avatarManager;
    private final EventPublisher eventPublisher;
    private final AssigneeService assigneeService;
    private final IssueManager issueManager;
    private final UserPickerResourceHelper userPickerHelper;
    private final JiraBaseUrls jiraBaseUrls;
    private final ColumnService columnService;
    private final XsrfInvocationChecker xsrfChecker;

    public UserResource(final UserService userService, UserUtil userUtil, final PasswordPolicyManager passwordPolicyManager, I18nHelper i18n, EmailFormatter emailFormatter,
            JiraAuthenticationContext authContext, TimeZoneManager timeZoneManager,
            AvatarPickerHelper avatarPickerHelper, AvatarManager avatarManager, AvatarService avatarService,
            AttachmentHelper attachmentHelper, UserPropertyManager userPropertyManager, PermissionManager permissionManager,
            ProjectService projectService, IssueService issueService, ProjectManager projectManager,
            EventPublisher eventPublisher, AssigneeService assigneeService, IssueManager issueManager,
            UserPickerResourceHelper userPickerHelper, JiraBaseUrls jiraBaseUrls, ColumnService columnService,
            XsrfInvocationChecker xsrfChecker, final UserManager userManager)
    {
        this.userService = userService;
        this.passwordPolicyManager = passwordPolicyManager;
        this.userPropertyManager = userPropertyManager;
        this.permissionManager = permissionManager;
        this.projectService = projectService;
        this.issueService = issueService;
        this.projectManager = projectManager;
        this.avatarManager = avatarManager;
        this.eventPublisher = eventPublisher;
        this.assigneeService = assigneeService;
        this.issueManager = issueManager;
        this.userPickerHelper = userPickerHelper;
        this.jiraBaseUrls = jiraBaseUrls;
        this.columnService = columnService;
        this.avatarResourceHelper = new AvatarResourceHelper(authContext, avatarManager, avatarPickerHelper, attachmentHelper, userManager);
        this.userUtil = userUtil;
        this.i18n = i18n;
        this.emailFormatter = emailFormatter;
        this.authContext = authContext;
        this.timeZoneManager = timeZoneManager;
        this.avatarService = avatarService;
        this.xsrfChecker = xsrfChecker;
    }

    /**
     * Returns a user. This resource cannot be accessed anonymously.
     *
     * @param name the username
     * @param key user key
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     */
    @GET
    public Response getUser(@QueryParam ("username") final String name, @QueryParam("key") final String key)
    {
        if (authContext.getUser() == null)
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.authentication.no.user.logged.in")));
        }

        return Response.ok(buildUserBean(name, key)).cacheControl(never()).build();
    }

    /**
     * Returns a list of users that match the search string. This resource cannot be accessed anonymously.
     *
     *
     * @param username A query string used to search username, name or e-mail address
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param includeActive If true, then active users are included in the results (default true)
     * @param includeInactive If true, then inactive users are included in the results (default false)
     * @param uriInfo context used for creating urls in user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @return A list of user objects that match the username provided
     */
    @GET
    @Path ("search")
    public Response findUsers(@QueryParam("username") final String username, @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults,
            @QueryParam ("includeActive") Boolean includeActive, @QueryParam ("includeInactive") Boolean includeInactive,
            @Context UriInfo uriInfo)
    {
        final List<User> allResults = userPickerHelper.findUsers(username, includeActive, includeInactive);
        final List<User> page = userPickerHelper.limitUserSearch(startAt, maxResults, allResults, null);
        return Response.ok(makeUserBeans(page)).cacheControl(never()).build();
    }

    /**
     * Returns a list of users matching query with highlighting. This resource cannot be accessed anonymously.
     *
     *
     * @param query A string used to search username, Name or e-mail address
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserPickerResultsBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @return An object containing list of matched user objects, with html representing highlighting.
     */
    @GET
    @Path ("picker")
    public Response findUsersForPicker(@QueryParam ("query") final String query,
            @QueryParam ("maxResults") Integer maxResults,
            @QueryParam("showAvatar") final Boolean showAvatar,
            @QueryParam("exclude") final List<String> excludeUsers)
    {

        return Response.ok(userPickerHelper.findUsersAsBean(query, maxResults, showAvatar, excludeUsers)).cacheControl(never()).build();
    }



    /**
     * Returns a list of users that match the search string. This resource cannot be accessed anonymously.
     * Please note that this resource should be called with an issue key when a list of assignable users is retrieved
     * for editing.  For create only a project key should be supplied.  The list of assignable users may be incorrect
     * if it's called with the project key for editing.
     *
     * @param name the username
     * @param projectKey the key of the project we are finding assignable users for
     * @param issueKey the issue key for the issue being edited we need to find assignable users for.
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param uriInfo Context used for constructing user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if no project or issue key was provided
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     * @return a Response with the users matching the query
     */
    @GET
    @Path ("assignable/search")
    public Response findAssignableUsers(@QueryParam ("username") final String name, @QueryParam ("project") final String projectKey,
            @QueryParam ("issueKey") final String issueKey, @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults, @QueryParam ("actionDescriptorId") Integer actionDescriptorId, @Context UriInfo uriInfo)
    {
        ActionDescriptor actionDescriptor = null;
        if (actionDescriptorId != null)
        {
            actionDescriptor = getActionDescriptorById(issueKey, actionDescriptorId);
        }

        final List<User> usersWithPermission = findAssignableUsers(name, projectKey, issueKey, actionDescriptor);

        final List<User> page = userPickerHelper.limitUserSearch(startAt, maxResults, usersWithPermission, null);
        return Response.ok(makeUserBeans(page)).cacheControl(never()).build();
    }

    /**
     * Create user. By default created user will not be notified with email.
     * If password field is not set then password will be randomly generated.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returned if the user was created.
     *
     * @response.representation.201.example
     *      {@link UserWriteBean#DOC_EXAMPLE_CREATED}
     *
     * @request.representation.example
     *      {@link UserWriteBean#DOC_EXAMPLE_CREATE}
     *
     * @response.representation.400.doc
     *      Returned if the request is invalid.
     *
     * @response.representation.403.doc
     *      Returned if the caller user does not have permission to create the user.
     *
     * @response.representation.500.doc
     *      Returned if the user was not created because of other error.
     */
    @POST
    @WebSudoRequired
    @ExperimentalApi
    public Response createUser(final UserWriteBean userBean)
    {
        final ApplicationUser loggedUser = authContext.getUser();
        mustBeAdmin(loggedUser);

        UserService.CreateUserValidationResult validationResult = userService.validateCreateUserForAdmin(loggedUser.getDirectoryUser(),
                userBean.getName(),
                userBean.getPassword(),
                userBean.getPassword(),
                userBean.getEmailAddress(),
                userBean.getDisplayName()
        );
        if (!validationResult.isValid())
        {
            throw new BadRequestWebException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        try
        {
            if (userBean.getNotification() != null && Boolean.parseBoolean(userBean.getNotification()))
            {
                userService.createUserWithNotification(validationResult);
            }
            else
            {
                userService.createUserNoNotification(validationResult);
            }

            final UserBean responseUserBean = buildUserBean(userBean.getName(), null);
            return Response.status(Response.Status.CREATED).location(responseUserBean.getSelf()).entity(responseUserBean).cacheControl(never()).build();
        }
        catch (PermissionException e)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }
        catch (CreateException e)
        {
            throw new ServerErrorWebException(ErrorCollection.of(e.getLocalizedMessage()));
        }
    }


    /**
     * Modify user. The "value" fields present will override the existing value.
     * Fields skipped in request will not be changed.
     *
     * @param name the username
     * @param key user key
     * @return a user
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the user exists and the caller has permission to edit it.
     *
     * @request.representation.example
     *      {@link UserWriteBean#DOC_EXAMPLE_UPDATE}
     *
     * @response.representation.200.example
     *      {@link UserWriteBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if the request is invalid.
     *
     * @response.representation.403.doc
     *      Returned if the caller user does not have permission to edit the user.
     *
     * @response.representation.404.doc
     *      Returned if the caller does have permission to edit the user but the user does not exist.
     */
    @PUT
    @WebSudoRequired
    @ExperimentalApi
    public Response updateUser(@QueryParam ("username") final String name, @QueryParam("key") final String key, final UserWriteBean userBean)
    {
        final ApplicationUser loggedUser = authContext.getUser();
        mustBeAdmin(loggedUser);

        if (StringUtils.isBlank(userBean.getName()) &&
                StringUtils.isBlank(userBean.getEmailAddress()) &&
                StringUtils.isBlank(userBean.getDisplayName()))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("error.no.value.found.to.be.changed")));
        }

        if (StringUtils.length(userBean.getDisplayName()) > MAX_LENGTH)
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("error.display.name.too.long")));
        }

        if (StringUtils.length(userBean.getEmailAddress()) > MAX_LENGTH)
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("error.email.address.too.long")));
        }

        if (StringUtils.isNotBlank(userBean.getEmailAddress()) &&
                !TextUtils.verifyEmail(userBean.getEmailAddress()))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("error.invalid.email.address.format")));
        }

        ApplicationUser changedUser = getUserByUsernameOrKey(name, key);

        final ImmutableUser.Builder userBuilder = ImmutableUser.newUser(changedUser.getDirectoryUser());
        userBuilder.name(StringUtils.defaultIfBlank(userBean.getName(), changedUser.getName()));
        userBuilder.emailAddress(StringUtils.defaultIfBlank(userBean.getEmailAddress(), changedUser.getEmailAddress()));
        userBuilder.displayName(StringUtils.defaultIfBlank(userBean.getDisplayName(), changedUser.getDisplayName()));

        final UserService.UpdateUserValidationResult validationResult =
                userService.validateUpdateUser(new DelegatingApplicationUser(changedUser.getKey(), userBuilder.toUser()));

        if (!validationResult.isValid())
        {
            throw new BadRequestWebException(ErrorCollection.of(validationResult.getErrorCollection()));
        }
        userService.updateUser(validationResult);

        return Response.ok(buildUserBean(null, changedUser.getKey())).cacheControl(never()).build();
    }


    /**
     * Modify user password.
     *
     * @param name the username
     * @param key user key
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link PasswordBean#DOC_EXAMPLE}
     *
     * @response.representation.204.doc
     *      Returned if the user exists and the caller has permission to edit it.
     *
     * @response.representation.403.doc
     *      Returned if the caller does not have permission to change the user password.
     *
     * @response.representation.404.doc
     *      Returned if the caller does have permission to change user password but the user does not exist.
     */
    @PUT
    @WebSudoRequired
    @ExperimentalApi
    @Path ("password")
    public Response changeUserPassword(@QueryParam ("username") final String name, @QueryParam("key") final String key, final PasswordBean passwordBean)
    {
        final ApplicationUser loggedUser = authContext.getUser();
        mustBeAdmin(loggedUser);

        ApplicationUser changedUser = getUserByUsernameOrKey(name, key);

        nonSysAdminCannotModifySysAdmin(loggedUser, changedUser);

        final String password = passwordBean.getPassword();
        if (StringUtils.isBlank(password))
        {
            //TODO: i18
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("error.password.cannot.be.empty")));
        }

        final Collection<WebErrorMessage> messages = passwordPolicyManager.checkPolicy(changedUser, null, password);
        if (!messages.isEmpty())
        {
            //TODO: i18
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("changepassword.new.password.rejected")));
        }

        try
        {
            userUtil.changePassword(changedUser.getDirectoryUser(), password);
        }
        catch (UserNotFoundException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("error-no-entity")));
        }
        catch (InvalidCredentialException e)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }
        catch (OperationNotPermittedException e)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }
        catch (PermissionException e)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }

        return Response.noContent().cacheControl(never()).build();
    }

    /**
     * Removes user.
     *
     * @param name the username
     * @param key user key
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @response.representation.204.doc
     *       Returned if the user was deleted successfully.
     *
     * @response.representation.400.doc
     *      Returned if the request is invalid or some other server error occurred.
     *
     * @response.representation.403.doc
     *      Returned if the caller does not have permission to remove the user.
     *
     * @response.representation.404.doc
     *      Returned if the caller does have permission to remove user but the user does not exist.
     */
    @DELETE
    @WebSudoRequired
    @ExperimentalApi
    public Response removeUser(@QueryParam ("username") final String name, @QueryParam("key") final String key)
    {
        final ApplicationUser loggedUser = authContext.getUser();
        mustBeAdmin(loggedUser);

        ApplicationUser removedUser = getUserByUsernameOrKey(name, key);

        final UserService.DeleteUserValidationResult validationResult = userService.validateDeleteUser(loggedUser, removedUser);

        if (!validationResult.isValid())
        {
            throw new BadRequestWebException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        try
        {
            userService.removeUser(loggedUser, validationResult);
        }
        //TODO: fix after JRADEV-22334 (fixing JIRA API to not do such a stupid thing as throwing one generic exception)
        catch (Exception e)
        {
            throw new BadRequestWebException(ErrorCollection.of(validationResult.getErrorCollection()));
        }

        return Response.noContent().cacheControl(never()).build();
    }

    private void mustBeAdmin(final ApplicationUser user)
    {
        if (user == null)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }

        final boolean isGlobalAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, user);
        if (!isGlobalAdmin)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }
    }

    private void nonSysAdminCannotModifySysAdmin(final ApplicationUser loggedUser, final ApplicationUser changedUser)
    {
        boolean isSystemAdminLogged = permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, loggedUser);
        boolean isSystemAdminEdited = permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, changedUser);
        if (isSystemAdminEdited && !isSystemAdminLogged)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }
    }

    protected ActionDescriptor getActionDescriptorById(String issueKey, Integer actionDescriptorId)
    {
        WorkflowTransitionUtil workflowTransitionUtil = JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
        workflowTransitionUtil.setIssue(issueManager.getIssueObject(issueKey));
        workflowTransitionUtil.setAction(actionDescriptorId);

        return workflowTransitionUtil.getActionDescriptor();
    }

    private List<User> findAssignableUsers(String name, String projectKey, String issueKey, ActionDescriptor actionDescriptor)
    {
        Collection<User> users = null;
        final User loggedInUser = authContext.getLoggedInUser();
        if (StringUtils.isNotBlank(issueKey))
        {
            final IssueService.IssueResult issueResult = issueService.getIssue(loggedInUser, issueKey);

            if (!issueResult.isValid())
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(issueResult.getErrorCollection()));
            }
            if (!permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, issueResult.getIssue(), loggedInUser))
            {
                throw new NotAuthorisedWebException();
            }

            users = assigneeService.findAssignableUsers(name, issueResult.getIssue(), actionDescriptor);
        }
        else if (StringUtils.isNotBlank(projectKey))
        {
            //get the project without any permission checks.  This code path will most likely get executed when
            //creating issues.  The projectService only allows getting projects that a user can browse or administer.
            //When creating issues these permissions aren't necessary.
            final Project project = projectManager.getProjectObjByKey(projectKey);
            if (project == null)
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(authContext.getI18nHelper().getText("rest.must.provide.valid.project")));
            }
            if (!permissionManager.hasPermission(Permissions.ASSIGN_ISSUE, project, loggedInUser))
            {
                throw new NotAuthorisedWebException();
            }

            users = assigneeService.findAssignableUsers(name, project);
        }
        else
        {
            throwWebException(authContext.getI18nHelper().getText("rest.must.provide.project.or.issue"),
                    com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
        }

        return new ArrayList<User>(users);
    }

    /**
     * Returns a list of active users that match the search string. This resource cannot be accessed anonymously.
     * Given an issue key this resource will provide a list of users that match the search string and have
     * the browse issue permission for the issue provided.
     *
     * @param name the username filter, no users returned if left blank
     * @param issueKey the issue key for the issue being edited we need to find viewable users for.
     * @param projectKey the optional project key to search for users with if no issueKey is supplied.
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param uriInfo Context used for constructing user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.400.doc
     *     Returned if no project or issue key was provided
     *
     * @response.representation.404.doc
     *     Returned if the requested issue or project is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     * @return a Response with the users matching the query
     */
    @GET
    @Path ("viewissue/search")
    public Response findUsersWithBrowsePermission(@QueryParam ("username") final String name,
            @QueryParam ("issueKey") final String issueKey, @QueryParam ("projectKey") final String projectKey,
            @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults, @Context UriInfo uriInfo)
    {
        final Either<Project, Issue> issueOrProject = getIssueOrProject(issueKey, projectKey);
        final Iterable<User> usersWithPermission = findUsersWithPermission(ImmutableList.of(Permissions.BROWSE), name, issueOrProject, false);

        final List<User> page = userPickerHelper.limitUserSearch(startAt, maxResults, usersWithPermission, null);
        return Response.ok(makeUserBeans(page)).cacheControl(never()).build();
    }

    @VisibleForTesting
    Iterable<User> findUsersWithPermission(final Iterable<Integer> permissions, final String name, Either<Project, Issue> projectOrIssue, boolean allowEmptyQuery)
    {
        final Predicate<User> permissionPredicate = Either.merge(projectOrIssue.left().map(new Function<Project, Predicate<User>>()
        {
            @Override
            public Predicate<User> apply(Project project)
            {
                return createProjectPredicate(permissions, project);
            }
        }).right().map(new Function<Issue, Predicate<User>>()
        {
            @Override
            public Predicate<User> apply(Issue issue)
            {
                return createIssuePredicate(permissions, issue);
            }
        }));

        return Iterables.filter(userPickerHelper.findUsers(name, true, false, allowEmptyQuery), permissionPredicate);
    }

    @VisibleForTesting
    Either<Project, Issue> getIssueOrProject(String issueKey, String projectKey)
    {
        if (StringUtils.isNotBlank(issueKey))
        {
            final IssueService.IssueResult issueResult = issueService.getIssue(authContext.getLoggedInUser(), issueKey);

            if (!issueResult.isValid())
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(issueResult.getErrorCollection()));
            }
            return Either.right((Issue)issueResult.getIssue());
        }
        if (StringUtils.isNotBlank(projectKey))
        {
            //get the project without any permission checks.  This code path will most likely get executed when
            //creating issues.  The projectService only allows getting projects that a user can browse or administer.
            //When creating issues these permissions aren't necessary.
            final Project project = projectManager.getProjectObjByKey(projectKey);
            if (project == null)
            {
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(authContext.getI18nHelper().getText("rest.must.provide.valid.project")));
            }
            return Either.left(project);
        }

        throw createWebException(authContext.getI18nHelper().getText("rest.must.provide.project.or.issue"),
                com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED);
    }

    @VisibleForTesting
    Predicate<User> createProjectPredicate(final Iterable<Integer> permissions, final Project project)
    {
        return new Predicate<User>()
        {
            @Override
            public boolean apply(User user)
            {
                for (Integer permission : permissions)
                {
                    if (!permissionManager.hasPermission(permission, project, user, true)) { return false; }
                }
                return true;
            }
        };
    }

    @VisibleForTesting
    Predicate<User> createIssuePredicate(final Iterable<Integer> permissions, final Issue issue)
    {
        return new Predicate<User>()
        {
            @Override
            public boolean apply(User user)
            {
                for (Integer permission : permissions)
                {
                    if (!permissionManager.hasPermission(permission, issue, user)) return false;
                }
                return true;
            }
        };
    }


    /**
     * Returns a list of active users that match the search string and have all specified permissions for the project or issue.<br>
     * This resource can be accessed by users with ADMINISTER_PROJECT permission for the project or global ADMIN or SYSADMIN rights.
     *
     *
     * @param name the username filter, list includes all users if unspecified
     * @param permissions comma separated list of permissions for project or issue returned users must have, see
     * <a href="https://developer.atlassian.com/static/javadoc/jira/6.0/reference/com/atlassian/jira/security/Permissions.Permission.html">Permissions</a>
     * JavaDoc for the list of all possible permissions.
     * @param issueKey the issue key for the issue for which returned users have specified permissions.
     * @param projectKey the optional project key to search for users with if no issueKey is supplied.
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.400.doc
     *     Returned if no project or issue key was provided or when permissions list is empty or contains an invalid entry
     *
     * @response.representation.404.doc
     *     Returned if the requested issue or project is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     *
     * @response.representation.403.doc
     *     Returned if the current user does not have admin rights for the project.
     *
     * @return a Response with the users matching the query
     */
    @GET
    @Path ("permission/search")
    public Response findUsersWithAllPermissions(@QueryParam ("username") final String name, @QueryParam ("permissions") final String permissions,
            @QueryParam ("issueKey") final String issueKey, @QueryParam ("projectKey") final String projectKey,
            @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults)
    {
        if (StringUtils.isBlank(permissions))
        {
            throw new RESTException(Response.Status.BAD_REQUEST, authContext.getI18nHelper().getText("rest.missing.permission.string"));
        }

        final Either<Project, Issue> issueOrProject = getIssueOrProject(issueKey, projectKey);
        final Project project = issueOrProject.left().on(new Function<Issue, Project>()
        {
            @Override
            public Project apply(Issue issue)
            {
                return issue.getProjectObject();
            }
        });
        final ApplicationUser currentUser = authContext.getUser();
        if (!(permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, currentUser)
                || permissionManager.hasPermission(Permissions.ADMINISTER, currentUser)
                || permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, currentUser)))
        {
            throw new ForbiddenWebException();
        }


        final ImmutableList<Integer> permissionIds = parsePermissions(permissions);

        final Iterable<User> usersWithPermission = findUsersWithPermission(permissionIds, name, issueOrProject, true);

        final List<User> page = userPickerHelper.limitUserSearch(startAt, maxResults, usersWithPermission, null);
        return Response.ok(makeUserBeans(page)).cacheControl(never()).build();
    }

    @VisibleForTesting
    ImmutableList<Integer> parsePermissions(final String permissions)
    {
        final List<String> requestedPermissions = ImmutableList.copyOf(StringUtils.split(permissions, ","));
        return ImmutableList.copyOf(Iterables.transform(requestedPermissions, new Function<String, Integer>()
        {
            @Override
            public Integer apply(final String permission)
            {
                try
                {
                    return Permissions.Permission.valueOf(permission).getId();
                }
                catch (IllegalArgumentException e)
                {
                    throw new RESTException(Response.Status.BAD_REQUEST, authContext.getI18nHelper().getText("rest.invalid.permission.string", permission));
                }
            }

        }));
    }


    /**
     * Returns a list of users that match the search string and can be assigned issues for all the given projects.
     * This resource cannot be accessed anonymously.
     *
     * @param name the username
     * @param startAt the index of the first user to return (0-based)
     * @param maxResults the maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     * If you specify a value that is higher than this number, your search results will be truncated.
     * @param projectKeysStr the keys of the projects we are finding assignable users for, comma-separated
     * @param uriInfo Context used for constructing user objects
     *
     * @response.representation.200.qname
     *      List of users
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     */
    @GET
    @Path ("assignable/multiProjectSearch")
    public Response findBulkAssignableUsers(@QueryParam ("username") final String name,
            @QueryParam ("projectKeys") final String projectKeysStr, @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults, @Context UriInfo uriInfo)
    {
        // 1. Get projects for keys, aborting on any errors
        String[] projectKeys = projectKeysStr.split(",");
        List<Project> projects = new ArrayList<Project>(projectKeys.length);
        for (String projectKey : projectKeys)
        {
            final ProjectService.GetProjectResult projectResult =
                    projectService.getProjectByKeyForAction(authContext.getLoggedInUser(), projectKey, ProjectAction.VIEW_PROJECT);

            if (projectResult.getErrorCollection().hasAnyErrors())
            {
                final ErrorCollection errors = ErrorCollection.of(projectResult.getErrorCollection());
                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
            projects.add(projectResult.getProject());
        }

        // 2. Get users matching the search that are assignable for all projects
        List<User> users = null;
        for (final Project project : projects)
        {
            Collection<User> projectAssignableUsers = assigneeService.findAssignableUsers(name, project);

            if (users == null)
            {
                users = new ArrayList<User>(projectAssignableUsers);
            }
            else
            {
                users.retainAll(projectAssignableUsers);
            }
        }

        // 3. Bean them up, Scottie.
        final List<User> page = userPickerHelper.limitUserSearch(startAt, maxResults, users, null);
        return Response.ok(makeUserBeans(page)).cacheControl(never()).build();
    }

    /**
     * Returns all avatars which are visible for the currently logged in user.
     *
     * @since v5.0
     * @param name username
     *
     * @return all avatars for given user, which the logged in user has permission to see
     *
     * @response.representation.200.qname
     *      avatars
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a map containing a list of avatars for both custom an system avatars
     *
     * @response.representation.200.example
     *      {@link AvatarBean#DOC_EXAMPLE_LIST}
     *
     * @response.representation.404.doc
     *      Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *      Returned if the current user is not authenticated.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the list of avatars.
     */
    @GET
    @Path ("avatars")
    public Map<String, List<AvatarBean>> getAllAvatars(@QueryParam ("username") final String name)
    {
        final ApplicationUser user = getApplicationUser(name);
        Long selectedAvatarId = null;
        final Avatar selectedAvatar = avatarService.getAvatar(authContext.getUser(), user);
        if (selectedAvatar != null)
        {
            selectedAvatarId = selectedAvatar.getId();
        }
        return avatarResourceHelper.getAllAvatars(Avatar.Type.USER, user.getKey(), selectedAvatarId);
    }

    /**
     * Converts temporary avatar into a real avatar
     *
     * @since v5.0
     *
     * @param username username
     * @param croppingInstructions cropping instructions
     * @return created avatar
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EDIT_EXAMPLE}
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      Returns created avatar
     *
     * @response.representation.201.example
     *      {@link AvatarBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *      Returned if the cropping coordinates are invalid
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to pick avatar
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Path ("avatar")
    public Response createAvatarFromTemporary(@QueryParam ("username") final String username,
            final AvatarCroppingBean croppingInstructions)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        ApplicationUser user = getApplicationUser(username);
        return avatarResourceHelper.createAvatarFromTemporary(Avatar.Type.USER, user.getKey(), croppingInstructions);
    }


    @PUT
    @Path ("avatar")
    public Response updateProjectAvatar(final @QueryParam ("username") String username, final AvatarBean avatarBean)
    {
        final ApplicationUser userObject = getApplicationUser(username);
        final PropertySet propertySet = userPropertyManager.getPropertySet(userObject);
        String id = avatarBean.getId();
        Long avatarId;
        try
        {
            avatarId = id == null ? null : Long.valueOf(id);
        }
        catch (NumberFormatException e)
        {
            avatarId = null;
        }
        if (!avatarManager.hasPermissionToEdit(authContext.getUser(), userObject))
        {
            throw new NotAuthorisedWebException();
        }
        propertySet.setLong(AvatarManager.USER_AVATAR_ID_KEY, avatarId);

        eventPublisher.publish(new UserAvatarUpdatedEvent(userObject.getDirectoryUser(), avatarId));

        return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
    }


    /**
     * Creates temporary avatar. Creating a temporary avatar is part of a 3-step process in uploading a new
     * avatar for a user: upload, crop, confirm.
     *
     * <p>
     *     The following examples shows these three steps using curl.
     *     The cookies (session) need to be preserved between requests, hence the use of -b and -c.
     *     The id created in step 2 needs to be passed to step 3
     *     (you can simply pass the whole response of step 2 as the request of step 3).
     * </p>
     *
     * <pre>
     * curl -c cookiejar.txt -X POST -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -H "Content-Type: image/png" --data-binary @mynewavatar.png \
     *   'http://localhost:8090/jira/rest/api/2/user/avatar/temporary?username=admin&amp;filename=mynewavatar.png'
     *
     * curl -b cookiejar.txt -X POST -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -H "Content-Type: application/json" --data '{"cropperWidth": "65","cropperOffsetX": "10","cropperOffsetY": "16"}' \
     *   -o tmpid.json \
     *   http://localhost:8090/jira/rest/api/2/user/avatar?username=admin
     *
     * curl -b cookiejar.txt -X PUT -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -H "Content-Type: application/json" --data-binary @tmpid.json \
     *   http://localhost:8090/jira/rest/api/2/user/avatar?username=admin
     * </pre>
     *
     * @since v5.0
     *
     * @param username username
     * @param filename name of file being uploaded
     * @param size size of file
     * @param request servlet request
     * @return temporary avatar cropping instructions
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      application/json
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the request does not conain a valid XSRF token
     *
     * @response.representation.404.doc
     *      Returned if the currently authenticated user does not have EDIT PROJECT permission.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.WILDCARD)
    @Path ("avatar/temporary")
    public Response storeTemporaryAvatar(final @QueryParam ("username") String username,
            final @QueryParam ("filename") String filename,
            final @QueryParam ("size") Long size,
            final @Context HttpServletRequest request)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        ApplicationUser user = getApplicationUser(username);
        return avatarResourceHelper.storeTemporaryAvatar(Avatar.Type.USER, user.getKey(), filename, size, request);
    }


    /**
     * Creates temporary avatar using multipart. The response is sent back as JSON stored in a textarea. This is because
     * the client uses remote iframing to submit avatars using multipart. So we must send them a valid HTML page back from
     * which the client parses the JSON from.
     *
     * <p>
     * Creating a temporary avatar is part of a 3-step process in uploading a new
     * avatar for a user: upload, crop, confirm. This endpoint allows you to use a multipart upload
     * instead of sending the image directly as the request body.
     * </p>
     *
     * <p>You *must* use "avatar" as the name of the upload parameter:</p>
     *
     * <pre>
     * curl -c cookiejar.txt -X POST -u admin:admin -H "X-Atlassian-Token: no-check" \
     *   -F "avatar=@mynewavatar.png;type=image/png" \
     *   'http://localhost:8090/jira/rest/api/2/user/avatar/temporary?username=admin'
     * </pre>
     *
     * @since v5.0
     *
     * @param username Username
     * @param request servlet request
     *
     * @return temporary avatar cropping instructions
     *
     * @response.representation.201.qname
     *      avatar
     *
     * @response.representation.201.mediaType
     *      text/html
     *
     * @response.representation.201.doc
     *      temporary avatar cropping instructions embeded in HTML page. Error messages will also be embeded in the page.
     *
     * @response.representation.201.example
     *      {@link com.atlassian.jira.rest.v2.issue.AvatarCroppingBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if user does NOT exist
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while converting temporary avatar to real avatar
     */
    @POST
    @Consumes (MediaType.MULTIPART_FORM_DATA)
    @Path ("avatar/temporary")
    @Produces ({ MediaType.TEXT_HTML })
    public Response storeTemporaryAvatarUsingMultiPart(@QueryParam ("username") String username,
            final @MultipartFormParam ("avatar") FilePart filePart,
            final @Context HttpServletRequest request)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(ExecutingHttpRequest.get());
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            throw new XsrfCheckFailedException();
        }

        ApplicationUser user = getApplicationUser(username);
        return avatarResourceHelper.storeTemporaryAvatarUsingMultiPart(Avatar.Type.USER, user.getKey(), filePart, request);
    }

    /**
     * Deletes avatar
     *
     * @since v5.0
     *
     * @param username username
     * @param id database id for avatar
     * @return temporary avatar cropping instructions
     *
     * @response.representation.204.mediaType
     *      application/json
     *
     * @response.representation.204.doc
     *      Returned if the avatar is successfully deleted.
     *
     * @response.representation.403.doc
     *      Returned if the currently authenticated user does not have permission to delete the avatar.
     *
     * @response.representation.404.doc
     *      Returned if the avatar does not exist or the currently authenticated user does not have permission to
     *      delete it.
     */
    @DELETE
    @Path ("avatar/{id}")
    public Response deleteAvatar(final @QueryParam ("username") String username, final @PathParam ("id") Long id)
    {
        getApplicationUser(username);
        return avatarResourceHelper.deleteAvatar(id);
    }

    /**
     * Returns the default columns for the given user. Admin permission will be required to get columns for a user
     * other than the currently logged in user.
     *
     * @since v6.1
     *
     * @param username username
     * @return column configuration
     *
     * @response.representation.200.qname
     *      columns
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a list of columns for configured for the given user
     *
     * @response.representation.404.doc
     *      Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *      Returned if the current user is not permitted to request the columns for the given user.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the column configuration.
     */
    @GET
    @Path ("columns")
    public Response defaultColumns(final @QueryParam ("username") String username)
    {
        final ApplicationUser currentUser = authContext.getUser();
        ApplicationUser columnUser = (username == null) ? currentUser : getApplicationUser(username);

        final ServiceOutcome<ColumnLayout> outcome = columnService.getColumnLayout(currentUser, columnUser);
        if (outcome.isValid())
        {
            final List<ColumnLayoutItem> columnLayoutItems = outcome.getReturnedValue().getColumnLayoutItems();
            return Response.ok(ColumnOptions.toColumnOptions(columnLayoutItems)).cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Sets the default columns for the given user.  Admin permission will be required to get columns for a user
     * other than the currently logged in user.
     *
     * @since v6.1
     *
     * @param username username
     * @param fields list of column ids
     * @return javax.ws.rs.core.Response containing basic message and http return code
     *
     * @response.representation.200.doc
     *      Returned when the columns is saved successfully
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while retrieving the column configuration.
     */
    @PUT
    @Path ("columns")
    @Consumes (MediaType.WILDCARD)
    public Response setColumns(final @FormParam ("username") String username,
                               final @FormParam ("columns") List<String> fields)
    {
        final ApplicationUser currentUser = authContext.getUser();
        ApplicationUser columnUser = (username == null) ? currentUser : getApplicationUser(username);

        final ServiceResult outcome = columnService.setColumns(currentUser, columnUser, fields);
        if (outcome.isValid())
        {
            return Response.ok().cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    /**
     * Reset the default columns for the given user to the system default. Admin permission will be required to get
     * columns for a user other than the currently logged in user.
     *
     * @since v6.1
     *
     * @param username username
     * @return javax.ws.rs.core.Response containing basic message and http return code
     *
     * @response.representation.204.doc
     *      Returned when the columns are reset successfully
     *
     * @response.representation.401.doc
     *      Returned if the current user is not permitted to request the columns for the given user.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while resetting the column configuration.
     */
    @DELETE
    @Path ("columns")
    @Consumes (MediaType.WILDCARD)
    public Response resetColumns(final @QueryParam ("username") String username)
    {
        final ApplicationUser currentUser = authContext.getUser();
        ApplicationUser columnUser = (username == null) ? currentUser : getApplicationUser(username);

        final ServiceResult outcome = columnService.resetColumns(currentUser, columnUser);
        if (outcome.isValid())
        {
            return noContent().cacheControl(never()).build();
        }
        else
        {
            throw new RESTException(ErrorCollection.of(outcome.getErrorCollection()));
        }
    }

    private UserBean buildUserBean(String name, String key) {
        ApplicationUser user = getUserByUsernameOrKey(name, key);
        final UserBeanBuilder builder = new UserBeanBuilder(jiraBaseUrls).user(user)
                .groups(new ArrayList<String>(userUtil.getGroupNamesForUser(user.getUsername())))
                .loggedInUser(authContext.getUser())
                .emailFormatter(emailFormatter)
                .timeZone(timeZoneManager.getTimeZoneforUser(user.getDirectoryUser()))
                .avatarService(avatarService);

        return builder.buildFull();
    }

    private ApplicationUser getUserByUsernameOrKey(final String name, final String key)
    {
        final ApplicationUser user;

        if (name == null)
        {
            if (key == null)
            {
                // we need either the key or username, otherwise, it's an error...
                throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.or.key.param")));
            }
            // Get by key
            user = userUtil.getUserByKey(key);
        }
        else
        {
            if (key != null)
            {
                // You can't specify both the username or the key. Choose one.
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(i18n.getText("rest.user.error.too.many.params")));
            }
            // get by name
            user = userUtil.getUserByName(name);
        }

        if (user == null)
        {
            if (name != null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.user.error.not.found", name)));
            }
            else
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.user.error.not.found.with.key", key)));
            }
        }
        return user;
    }

    private ApplicationUser getApplicationUser(final String name)
    {
        if (name == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.param")));
        }

        final ApplicationUser user = userUtil.getUserByName(name);
        if (user == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.user.error.not.found", name)));
        }

        return user;
    }

    private List<UserBean> makeUserBeans(Collection<User> users)
    {
        List<UserBean> beans = new ArrayList<UserBean>();
        for (User user : users)
        {
            UserBeanBuilder builder = new UserBeanBuilder(jiraBaseUrls).user(user);
            builder.loggedInUser(authContext.getUser());
            builder.emailFormatter(emailFormatter);
            builder.timeZone(timeZoneManager.getTimeZoneforUser(user));
            beans.add(builder.buildMid());
        }
        return beans;
    }

    private void throwWebException(String message, com.atlassian.jira.util.ErrorCollection.Reason reason)
    {
        com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(message, reason);
        throwWebException(errorCollection);
    }

    private void throwWebException(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new RESTException(ErrorCollection.of(errorCollection));
    }

    private RESTException createWebException(String message, com.atlassian.jira.util.ErrorCollection.Reason reason)
    {
        final com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(message, reason);
        return new RESTException(ErrorCollection.of(errorCollection));
    }
}
