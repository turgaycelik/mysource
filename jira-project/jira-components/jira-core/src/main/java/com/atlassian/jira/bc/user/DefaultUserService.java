package com.atlassian.jira.bc.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.event.user.UserProfileUpdatedEvent;
import com.atlassian.jira.event.user.UserRenamedEvent;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.plugin.user.PasswordPolicyManager;
import com.atlassian.jira.plugin.user.PreDeleteUserErrorsManager;
import com.atlassian.jira.plugin.user.WebErrorMessage;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserDeleteVeto;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link com.atlassian.jira.bc.user.UserService} interface. Contains metohods to create/delete users hiding UserUtil internals.
 *
 * @since v4.0
 */
public class DefaultUserService implements UserService
{
    private static final int MAX_FIELD_LENGTH = 255;
    private static final char[] INVALID_USERNAME_CHARS = { '<', '>', '&' };

    private final UserUtil userUtil;
    private final UserDeleteVeto userDeleteVeto;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraContactHelper jiraContactHelper;
    private final I18nHelper.BeanFactory i18nFactory;
    private final EventPublisher eventPublisher;
    private final PreDeleteUserErrorsManager preDeleteUserErrorsManager;
    private final PasswordPolicyManager passwordPolicyManager;

    public DefaultUserService(final UserUtil userUtil, final UserDeleteVeto userDeleteVeto, final PermissionManager permissionManager,
            final UserManager userManager, final JiraContactHelper jiraContactHelper,
            final I18nHelper.BeanFactory i18nFactory, final JiraAuthenticationContext jiraAuthenticationContext,
            final EventPublisher eventPublisher, final PreDeleteUserErrorsManager preDeleteUserErrorsManager,
            final PasswordPolicyManager passwordPolicyManager)
    {
        this.userUtil = userUtil;
        this.userDeleteVeto = userDeleteVeto;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.jiraContactHelper = jiraContactHelper;
        this.i18nFactory = i18nFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.eventPublisher = eventPublisher;
        this.preDeleteUserErrorsManager = preDeleteUserErrorsManager;
        this.passwordPolicyManager = passwordPolicyManager;
    }

    @Override
    public CreateUserValidationResult validateCreateUserForSignup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForSignupOrSetup(loggedInUser, username, password, confirmPassword, email, fullname, true);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForSetup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForSignupOrSetup(loggedInUser, username, password, confirmPassword, email, fullname, false);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForSignupOrSetup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForSignupOrSetup(loggedInUser, username, password, confirmPassword, email, fullname, true);
    }

    private CreateUserValidationResult validateCreateUserForSignupOrSetup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname, boolean checkForWritableDirectory)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (checkForWritableDirectory && !userManager.hasWritableDirectory())
        {
            String link = getContactAdminLink(i18nBean);
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only.contact.admin", link));
            return new CreateUserValidationResult(errors);
        }

        List<WebErrorMessage> passwordErrors = ImmutableList.of();
        errors.addErrorCollection(validateCreateUser(i18nBean, username, password, confirmPassword, email, fullname, null));
        //for setup or public signups the password is a required field.
        if (StringUtils.isEmpty(password))
        {
            errors.addError(FieldName.PASSWORD, i18nBean.getText("signup.error.password.required"));
        }
        else
        {
            passwordErrors = buildPasswordErrors(errors, ApplicationUsers.from(loggedInUser), username, fullname, email, password);
        }

        if (errors.hasAnyErrors())
        {
            return new CreateUserValidationResult(errors, passwordErrors);
        }
        return new CreateUserValidationResult(username, password, email, fullname);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdminPasswordRequired(final User loggedInUser,
            final String username, final String password, final String confirmPassword, final String email,
            final String fullname)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.user.no.permission.to.create"));
            return new CreateUserValidationResult(errors);
        }

        if (!userManager.hasWritableDirectory())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only"));
            return new CreateUserValidationResult(errors);
        }

        List<WebErrorMessage> passwordErrors = ImmutableList.of();
        errors.addErrorCollection(validateCreateUser(i18nBean, username, password, confirmPassword, email, fullname, null));
        //for setup or public signups the password is a required field.
        if (StringUtils.isEmpty(password))
        {
            errors.addError(FieldName.PASSWORD, i18nBean.getText("signup.error.password.required"));
        }
        else
        {
            passwordErrors = buildPasswordErrors(errors, ApplicationUsers.from(loggedInUser), username, fullname, email, password);
        }

        if (errors.hasAnyErrors())
        {
            return new CreateUserValidationResult(errors, passwordErrors);
        }
        return new CreateUserValidationResult(username, password, email, fullname, null);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdmin(final User loggedInUser,
            final String username, final String password, final String confirmPassword, final String email,
            final String fullname)
    {
        return validateCreateUserForAdmin(loggedInUser, username, password, confirmPassword, email, fullname, null);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdmin(User loggedInUser, String username, String password, String confirmPassword, String email, String fullname, @Nullable Long directoryId)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.user.no.permission.to.create"));
            return new CreateUserValidationResult(errors);
        }

        if (!userManager.hasWritableDirectory())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only"));
            return new CreateUserValidationResult(errors);
        }

        errors.addErrorCollection(validateCreateUser(i18nBean, username, password, confirmPassword, email, fullname, directoryId));

        if (directoryId == null)
        {
            // We will add to first writable directory - validate that there is one.
            if (userManager.getWritableDirectories().size() == 0)
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only"));
            }
        }
        else
        {
            Directory directory = userManager.getDirectory(directoryId);
            if (directory == null)
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.no.such.directory", directoryId));
            }
            else
            {
                if (!directory.getAllowedOperations().contains(OperationType.CREATE_USER))
                {
                    errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.read.only.directory", directory.getName()));
                }
            }
        }

        List<WebErrorMessage> passwordErrors = ImmutableList.of();
        if (password != null)
        {
            passwordErrors = buildPasswordErrors(errors, username, fullname, email, password);
        }

        if (errors.hasAnyErrors())
        {
            return new CreateUserValidationResult(errors, passwordErrors);
        }
        return new CreateUserValidationResult(username, password, email, fullname, directoryId);
    }

    @Override
    public CreateUsernameValidationResult validateCreateUsername(final User loggedInUser, final String username)
    {
        return validateCreateUsername(loggedInUser, username, null);
    }

    @Override
    public CreateUsernameValidationResult validateCreateUsername(final User loggedInUser, final String username, final Long directoryId)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        validateUsername(username, directoryId, i18nBean, errors);

        return new CreateUsernameValidationResult(username, directoryId, errors);
    }

    private ErrorCollection validateCreateUser(I18nHelper i18nBean, final String username, final String password,
            final String confirmPassword, final String email, final String fullname, @Nullable final Long directoryId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();

        //validate the user params
        if (StringUtils.isEmpty(email))
        {
            errors.addError(FieldName.EMAIL, i18nBean.getText("signup.error.email.required"));
        }
        else if (email.length() > MAX_FIELD_LENGTH)
        {
            errors.addError(FieldName.EMAIL, i18nBean.getText("signup.error.email.greater.than.max.chars"));
        }
        else if (!TextUtils.verifyEmail(email))
        {
            errors.addError(FieldName.EMAIL, i18nBean.getText("signup.error.email.valid"));
        }

        if (StringUtils.isEmpty(fullname))
        {
            errors.addError(FieldName.FULLNAME, i18nBean.getText("signup.error.fullname.required"));
        }
        else if (fullname.length() > MAX_FIELD_LENGTH)
        {
            errors.addError(FieldName.FULLNAME, i18nBean.getText("signup.error.full.name.greater.than.max.chars"));
        }

        validateUsername(username, directoryId, i18nBean, errors);

        // If a password has been specified then we need to check they are the same
        // else there is no password specified then check to see if we need one.
        if (StringUtils.isNotEmpty(confirmPassword) || StringUtils.isNotEmpty(password))
        {
            if (password == null || !password.equals(confirmPassword))
            {
                errors.addError(FieldName.CONFIRM_PASSWORD, i18nBean.getText("signup.error.password.mustmatch"));
            }
        }
        return errors;
    }

    private void validateUsername(final String username, final Long directoryId, final I18nHelper i18nBean, final ErrorCollection errors)
    {
        if (StringUtils.isEmpty(username))
        {
            errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.required"));
        }
        else if (username.length() > MAX_FIELD_LENGTH)
        {
            errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.greater.than.max.chars"));
        }
        else
        {
            if (StringUtils.containsAny(username, INVALID_USERNAME_CHARS))
            {
                errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.invalid.chars"));
            }

            if (!errors.getErrors().containsKey(FieldName.NAME))
            {
                if (directoryId == null)
                {
                    // Check if the username exists in any directory
                    if (userUtil.userExists(username))
                    {
                        errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.exists"));
                    }
                }
                else
                {
                    // Check if the username exists in the given directory - we allow duplicates in other directories
                    if (userManager.findUserInDirectory(username, directoryId) != null)
                    {
                        errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.exists"));
                    }
                }
            }
        }
    }

    @Override
    public User createUserNoNotification(final CreateUserValidationResult result)
            throws PermissionException, CreateException
    {
        Assertions.notNull("You can not create a user with a null validation result.", result);
        Assertions.stateTrue("You can not create a user with an invalid validation result.", result.isValid());

        final String username = result.getUsername();
        final String password = result.getPassword();
        final String email = result.getEmail();
        final String fullname = result.getFullname();
        final Long directoryId = result.getDirectoryId();

        return userUtil.createUserNoNotification(username, password, email, fullname, directoryId);
    }

    @Override
    public User createUserFromSignup(final CreateUserValidationResult result)
            throws PermissionException, CreateException
    {
        return createUserWithNotification(result, UserEventType.USER_SIGNUP);
    }

    @Override
    public User createUserWithNotification(final CreateUserValidationResult result)
            throws PermissionException, CreateException
    {
        return createUserWithNotification(result, UserEventType.USER_CREATED);
    }

    private User createUserWithNotification(final CreateUserValidationResult result, int eventType)
            throws PermissionException, CreateException
    {
        Assertions.notNull("You can not create a user, validation result", result);
        Assertions.stateTrue("You can not create a user with an invalid validation result.", result.isValid());

        final String username = result.getUsername();
        final String password = result.getPassword();
        final String email = result.getEmail();
        final String fullname = result.getFullname();
        final Long directoryId = result.getDirectoryId();

        return userUtil.createUserWithNotification(username, password, email, fullname, directoryId, eventType);
    }

    @Override
    public UpdateUserValidationResult validateUpdateUser(User user)
    {
        return validateUpdateUser(ApplicationUsers.from(user));
    }

    @Override
    public UpdateUserValidationResult validateUpdateUser(ApplicationUser user)
    {
        final ApplicationUser loggedInUser = jiraAuthenticationContext.getUser();
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!isAdministrator(loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.update.no.permission"));
            return new UpdateUserValidationResult(errors);
        }
        // Check the user actually exists
        ApplicationUser userToUpdate = userManager.getUserByKey(user.getKey());
        if (userToUpdate == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.user.does.not.exist"));
            return new UpdateUserValidationResult(errors);
        }
        // Is the directory writable?
        if (!userManager.canUpdateUser(userToUpdate))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.edit.user.directory.read.only"));
            return new UpdateUserValidationResult(errors);
        }
        // Is a standard admin trying to update a SysAdmin?
        if (!isSysAdmin(loggedInUser) && isSysAdmin(userToUpdate))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.must.be.sysadmin.to.edit.sysadmin"));
            return new UpdateUserValidationResult(errors);
        }

        // Special checks for deactivate
        if (!user.isActive())
        {
            final Collection<ProjectComponent> components = userUtil.getComponentsUserLeads(userToUpdate);
            if (components.size() > 0)
            {
                String projectList = getDisplayableProjectList(getProjectsFor(components));
                // Show this error against the field, because we cannot post HTML to error messages, so we put up an
                // explicit error message with link to components. See EditUser.java and editprofile.jsp
                errors.addError("active", i18nBean.getText("admin.errors.users.cannot.deactivate.due.to.component.lead", projectList));
            }

            Collection<Project> projects = userUtil.getProjectsLeadBy(userToUpdate);
            if (projects.size() > 0)
            {
                String projectList = getDisplayableProjectList(projects);
                // Show this error against the field, because we cannot post HTML to error messages, so we put up an
                // explicit error message with link to components. See EditUser.java and editprofile.jsp
                errors.addError("active", i18nBean.getText("admin.errors.users.cannot.deactivate.due.to.project.lead", projectList));
            }

            if (loggedInUser.getName().equalsIgnoreCase(user.getUsername()))
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.deactivate.currently.logged.in"));
            }
        }

        // Trying to rename?
        if (!IdentifierUtils.equalsInLowerCase(userToUpdate.getUsername(), user.getUsername()))
        {
            if (userManager.canRenameUser(userToUpdate))
            {
                // We pass null DirectoryID because we want to check if this username exists in _any_ directory.
                validateUsername(user.getUsername(), null, i18nBean, errors);
            }
            else
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.rename.due.to.configuration"));
            }

        }

        if (errors.hasAnyErrors())
        {
            return new UpdateUserValidationResult(errors);
        }
        else
        {
            return new UpdateUserValidationResult(user);
        }
    }

    private Collection<Project> getProjectsFor(Collection<ProjectComponent> components)
    {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        HashSet<Project> projects = new HashSet<Project>(components.size());
        for (ProjectComponent component : components)
        {
            projects.add(projectManager.getProjectObj(component.getProjectId()));
        }
        return projects;
    }

    private String getDisplayableProjectList(Collection<Project> projects)
    {
        final Collection<String> projectKeys = Collections2.transform(projects, new Function<Project, String>()
        {
            @Override
            public String apply(Project from)
            {
                return from.getKey();
            }
        });
        return StringUtils.join(projectKeys, ", ");
    }

    @Override
    public void updateUser(UpdateUserValidationResult updateUserValidationResult)
    {
        if (updateUserValidationResult.isValid())
        {
            // Keep the old user around to see what changed
            final ApplicationUser oldUser = userManager.getUserByKey(updateUserValidationResult.getApplicationUser().getKey());
            userManager.updateUser(updateUserValidationResult.getApplicationUser());
            // Send event
            if (IdentifierUtils.equalsInLowerCase(oldUser.getUsername(), updateUserValidationResult.getApplicationUser().getUsername()))
            {
                eventPublisher.publish(new UserProfileUpdatedEvent(updateUserValidationResult.getApplicationUser(), jiraAuthenticationContext.getUser()));
            }
            else
            {
                // The username changed: send a more specific event
                eventPublisher.publish(new UserRenamedEvent(updateUserValidationResult.getApplicationUser(), jiraAuthenticationContext.getUser(), oldUser.getUsername()));
            }
        }
        else
        {
            throw new IllegalStateException("Invalid UpdateUserValidationResult");
        }
    }

    @Override
    public DeleteUserValidationResult validateDeleteUser(final User loggedInUser, final String username)
    {
        return validateDeleteUser(ApplicationUsers.from(loggedInUser), username);
    }

    @Override
    public DeleteUserValidationResult validateDeleteUser(final ApplicationUser loggedInUser, final String username)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (username == null || username.length() == 0)
        {
            errors.addError("username", i18nBean.getText("admin.errors.users.cannot.delete.due.to.invalid.username"));
            return new DeleteUserValidationResult(errors);
        }

        return validateDeleteUser(loggedInUser, userManager.getUserByName(username));
    }

    @Override
    public DeleteUserValidationResult validateDeleteUser(final ApplicationUser loggedInUser, final ApplicationUser userForDelete)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.delete.no.permission"));
            return new DeleteUserValidationResult(errors);
        }

        if (!userManager.isUserExisting(userForDelete))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.user.does.not.exist"));
            return new DeleteUserValidationResult(errors);
        }

        if (userForDelete.equals(loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.currently.logged.in"));
            return new DeleteUserValidationResult(errors);
        }

        if (!userManager.canUpdateUser(userForDelete))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.user.read.only"));
            return new DeleteUserValidationResult(errors);
        }

        if (!isSysAdmin(loggedInUser) && isSysAdmin(userForDelete))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.sysadmin"));
            return new DeleteUserValidationResult(errors);
        }

        try
        {
            if (!userManager.getUserState(userForDelete).isInMultipleDirectories())
            {
                validateDeleteUserReferences(loggedInUser, userForDelete, i18nBean, errors);
            }
        }
        catch (Exception e)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.exception.occured.validating") + " " + e);
        }

        if (errors.hasAnyErrors())
        {
            return new DeleteUserValidationResult(errors);
        }

        return new DeleteUserValidationResult(userForDelete);
    }



    private void validateDeleteUserReferences(ApplicationUser loggedInUser, ApplicationUser userForDelete,
            I18nHelper i18nBean, ErrorCollection errors) throws SearchException
    {
        final String username = userForDelete.getUsername();

        final long numberOfReportedIssues = userUtil.getNumberOfReportedIssuesIgnoreSecurity(loggedInUser, userForDelete);
        if (numberOfReportedIssues > 0)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.reported.issues", "'" + username + "'", "" + numberOfReportedIssues));
        }

        final long numberOfAssignedIssues = userUtil.getNumberOfAssignedIssuesIgnoreSecurity(loggedInUser, userForDelete);
        if (numberOfAssignedIssues > 0)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.assigned.issues", "'" + username + "'", "" + numberOfAssignedIssues));
        }

        final long numberOfComments = userDeleteVeto.getCommentCountByAuthor(userForDelete);
        if (numberOfComments > 0)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.commented.issues", "'" + username + "'", "" + numberOfComments));
        }

        final long numberOfProjectsUserLeads = userUtil.getProjectsLeadBy(userForDelete).size();
        if (numberOfProjectsUserLeads > 0)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.project.lead", "'" + username + "'", "" + numberOfProjectsUserLeads));
        }

        ImmutableList<WebErrorMessage> lst = preDeleteUserErrorsManager.getWarnings(userForDelete.getDirectoryUser());
        for (WebErrorMessage errorMessage : lst)
        {
            errors.addErrorMessage(errorMessage.getDescription());
        }
    }

    private boolean isAdministrator(@Nullable ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

    private boolean isSysAdmin(@Nullable ApplicationUser user)
    {
        return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }



    @Override
    public void removeUser(final User loggedInUser, final DeleteUserValidationResult result)
    {
        removeUser(ApplicationUsers.from(loggedInUser), result);
    }

    @Override
    public void removeUser(final ApplicationUser loggedInUser, final DeleteUserValidationResult result)
    {
        Assertions.notNull("You can not remove a user with a null validation result.", result);
        Assertions.stateTrue("You can not remove a user with an invalid validation result.", result.isValid());

        final ApplicationUser userForDelete = result.getApplicationUser();
        userUtil.removeUser(loggedInUser, userForDelete);
    }

    I18nHelper getI18nBean(final User user)
    {
        return i18nFactory.getInstance(user);
    }

    I18nHelper getI18nBean(final ApplicationUser user)
    {
        return i18nFactory.getInstance(user);
    }

    private String getContactAdminLink(I18nHelper i18n)
    {
        return jiraContactHelper.getAdministratorContactMessage(i18n);
    }

    private List<WebErrorMessage> buildPasswordErrors(ErrorCollection errors, ApplicationUser loggedInUser, String username,
            String fullname, String email, String password)
    {
        final ApplicationUser originalUser = jiraAuthenticationContext.getUser();
        jiraAuthenticationContext.setLoggedInUser(loggedInUser);
        try
        {
            return buildPasswordErrors(errors, username, fullname, email, password);
        }
        finally
        {
            jiraAuthenticationContext.setLoggedInUser(originalUser);
        }
    }

    private List<WebErrorMessage> buildPasswordErrors(ErrorCollection errors, String username, String fullname, String email, String password)
    {
        final Collection<WebErrorMessage> messages = passwordPolicyManager.checkPolicy(username, fullname, email, password);
        if (messages.isEmpty())
        {
            return ImmutableList.of();
        }
        errors.addError(FieldName.PASSWORD, jiraAuthenticationContext.getI18nHelper().getText("signup.error.password.rejected"));
        return ImmutableList.copyOf(messages);
    }
}
