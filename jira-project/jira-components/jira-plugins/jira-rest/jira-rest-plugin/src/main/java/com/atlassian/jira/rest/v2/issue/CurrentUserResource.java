package com.atlassian.jira.rest.v2.issue;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.event.user.UserProfileUpdatedEvent;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;


/**
 * Currently logged user resource
 * @since 6.1
 */
@Path ("myself")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@ExperimentalApi
public class CurrentUserResource
{
    private static final int MAX_LENGTH = 255;

    private final UserService userService;
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final PasswordPolicyManager passwordPolicyManager;
    private final EventPublisher eventPublisher;
    private final I18nHelper i18n;
    private final EmailFormatter emailFormatter;
    private final JiraAuthenticationContext authContext;
    private final TimeZoneManager timeZoneManager;
    private final AvatarService avatarService;
    private final JiraBaseUrls jiraBaseUrls;

    public CurrentUserResource(final UserService userService, final UserUtil userUtil, final UserManager userManager, final PasswordPolicyManager passwordPolicyManager, final EventPublisher eventPublisher, final I18nHelper i18n, final EmailFormatter emailFormatter,
            final JiraAuthenticationContext authContext, final TimeZoneManager timeZoneManager,
            final AvatarService avatarService, final JiraBaseUrls jiraBaseUrls)
    {
        this.userService = userService;
        this.userManager = userManager;
        this.passwordPolicyManager = passwordPolicyManager;
        this.eventPublisher = eventPublisher;
        this.jiraBaseUrls = jiraBaseUrls;
        this.userUtil = userUtil;
        this.i18n = i18n;
        this.emailFormatter = emailFormatter;
        this.authContext = authContext;
        this.timeZoneManager = timeZoneManager;
        this.avatarService = avatarService;
    }

    /**
     * Returns currently logged user. This resource cannot be accessed anonymously.
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
     *      {@link UserBean#DOC_EXAMPLE}
     *
     * @response.representation.403.doc
     *     Returned if the current user is not authenticated.

     * @response.representation.404.doc
     *      Returned if the the user could not be found.
     */
    @GET
    public Response getUser()
    {
        final ApplicationUser currentUser = authContext.getUser();
        if (currentUser == null)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }

        return createUserResponse(currentUser);
    }

    /**
     * Modify currently logged user. The "value" fields present will override the existing value.
     * Fields skipped in request will not be changed. Only email and display name can be change that way.
     *
     * @return a user
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.doc
     *      Returned if the user exists and the caller has permission to edit it.
     *
     * @request.representation.example
     *      {@link UserWriteBean#DOC_EXAMPLE_UPDATE_MYSELF}
     *
     * @response.representation.200.example
     *      {@link UserWriteBean#DOC_EXAMPLE_UPDATED_MYSELF}
     *
     *
     * @response.representation.400.doc
     *      Returned if the request is invalid.
     *
     * @response.representation.403.doc
     *      Returned if the current user is not authenticated.
     *
     * @response.representation.404.doc
     *      Returned if the the user could not be found.
     */
    @PUT
    public Response updateUser(final UserWriteBean userBean)
    {
        final ApplicationUser currentUser = authContext.getUser();
        if (currentUser == null)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }

        if (StringUtils.isBlank(userBean.getEmailAddress()) &&
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

        // Is the directory writable?
        if (!userManager.canUpdateUser(currentUser))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("error.cannot.edit.user.directory.read.only")));
        }

        final ImmutableUser.Builder userBuilder = ImmutableUser.newUser(currentUser.getDirectoryUser());
        userBuilder.emailAddress(StringUtils.defaultIfBlank(userBean.getEmailAddress(), currentUser.getEmailAddress()));
        userBuilder.displayName(StringUtils.defaultIfBlank(userBean.getDisplayName(), currentUser.getDisplayName()));

        userManager.updateUser(userBuilder.toUser());

        final String key = currentUser.getKey();
        final ApplicationUser changedUser = userUtil.getUserByKey(key);
        if (changedUser == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("error-no-entity")));
        }

        eventPublisher.publish(new UserProfileUpdatedEvent(changedUser.getDirectoryUser(), changedUser.getDirectoryUser()));
        return createUserResponse(changedUser);
    }


    /**
     * Modify caller password.
     *
     * @request.representation.mediaType
     *      application/json
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
    @Path ("password")
    public Response changeMyPassword(final PasswordBean passwordBean)
    {
        final ApplicationUser currentUser = authContext.getUser();
        if (currentUser == null)
        {
            throw new ForbiddenWebException(ErrorCollection.of(i18n.getText("error.no-permission")));
        }

        final String password = passwordBean.getPassword();
        if (StringUtils.isBlank(password))
        {
            //TODO: i18
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("error.password.cannot.be.empty")));
        }

        final Collection<WebErrorMessage> messages = passwordPolicyManager.checkPolicy(currentUser, null, password);
        if (!messages.isEmpty())
        {
            //TODO: i18
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("changepassword.new.password.rejected")));
        }

        try
        {
            userUtil.changePassword(currentUser.getDirectoryUser(), password);
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

    private Response createUserResponse(final ApplicationUser currentUser)
    {
        final UserBeanBuilder builder = new UserBeanBuilder(jiraBaseUrls).user(currentUser)
                .groups(ImmutableList.copyOf(userUtil.getGroupNamesForUser(currentUser.getUsername())))
                .loggedInUser(currentUser)
                .emailFormatter(emailFormatter)
                .timeZone(timeZoneManager.getTimeZoneforUser(currentUser.getDirectoryUser()))
                .avatarService(avatarService);

        return Response.ok(builder.buildFull()).cacheControl(never()).build();
    }
}
