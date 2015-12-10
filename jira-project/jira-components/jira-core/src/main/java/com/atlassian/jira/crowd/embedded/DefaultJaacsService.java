package com.atlassian.jira.crowd.embedded;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.ObjectNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationManagerException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.RemoteAddress;
import com.atlassian.ip.IPMatcher;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.ValidationFailureException;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultJaacsService implements JaacsService
{
    private static final Logger log = Logger.getLogger(DefaultJaacsService.class);

    private final ApplicationManager applicationManager;
    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory i18nFactory;

    public DefaultJaacsService(ApplicationManager applicationManager, PermissionManager permissionManager, I18nHelper.BeanFactory i18nFactory)
    {
        this.applicationManager = applicationManager;
        this.permissionManager = permissionManager;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public Set<RemoteAddress> getRemoteAddresses(final JiraServiceContext jiraServiceContext, long applicationId)
    {
        Application app = null;
        try
        {
            app = applicationManager.findById(applicationId);
        }
        catch (ObjectNotFoundException e)
        {
            return Collections.emptySet();
        }
        return app.getRemoteAddresses();
    }

    @Override
    public boolean validateAddRemoteAddress(final JiraServiceContext jiraServiceContext, String remoteAddress, long applicationId)
    {
        validateJiraServiceContext(jiraServiceContext);

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.jaacs.application.admin.required"));
            return false;
        }
        if (StringUtils.isEmpty(remoteAddress))
        {
            errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.remote.address.empty"));
            return false;
        }
        // Check that it is a valid IP address
        if (!IPMatcher.isValidPatternOrHost(remoteAddress))
        {
            errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.remote.address.invalid.ip", remoteAddress));
            return false;
        }
        // Check for duplicates
        Application app = null;
        try
        {
            app = applicationManager.findById(applicationId);
        }
        catch (ObjectNotFoundException e)
        {
            errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.application.invalid"));
            return false;
        }
        Set<RemoteAddress> remoteAddresses = app.getRemoteAddresses();
        if (remoteAddresses.contains(new RemoteAddress(remoteAddress)))
        {
            errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.remote.address.duplicate"));
            return false;
        }

        return true;
    }

    @Override
    public boolean addRemoteAddress(final JiraServiceContext jiraServiceContext, String remoteAddress, long applicationId)
    {
        final I18nHelper i18n = jiraServiceContext.getI18nBean();
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        if (validateAddRemoteAddress(jiraServiceContext, remoteAddress, applicationId))
        {
            Application app = null;
            try
            {
                app = applicationManager.findById(applicationId);
            }
            catch (ObjectNotFoundException e)
            {
                errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.application.invalid"));
                return false;
            }
            try
            {
                applicationManager.addRemoteAddress(app, new RemoteAddress(remoteAddress));
            }
            catch (ObjectNotFoundException e)
            {
                errorCollection.addErrorMessage(i18n.getText("admin.jaacs.application.remote.add.address.error", e));
                log.error("Exception trying to add remote address: " + e, e);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean validateDeleteApplication(JiraServiceContext jiraServiceContext, long applicationId)
    {
        validateJiraServiceContext(jiraServiceContext);

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.jaacs.application.admin.required"));
            return false;
        }
        // Check address exists
        Application app;
        try
        {
            app = applicationManager.findById(applicationId);
        }
        catch (ObjectNotFoundException e)
        {
            errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.application.invalid"));
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteApplication(JiraServiceContext jiraServiceContext, long applicationId)
    {
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();
        if (validateDeleteApplication(jiraServiceContext, applicationId))
        {
            Application app;
            try
            {
                app = applicationManager.findById(applicationId);
            }
            catch (ObjectNotFoundException e)
            {
                errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.application.invalid"));
                return false;
            }
            try
            {
                applicationManager.remove(app);
            }
            catch (ApplicationManagerException e)
            {
                errorCollection.addErrorMessage(i18n.getText("admin.jaacs.application.remote.remove.address.error", e));
                log.error("Exception trying to remove application: " + e, e);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean validateResetPassword(final JiraServiceContext jiraServiceContext, String password, long applicationId)
    {
        validateJiraServiceContext(jiraServiceContext);

        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getLoggedInUser()))
        {
            errorCollection.addErrorMessage(i18n.getText("admin.jaacs.application.admin.required"));
            return false;
        }
        if (StringUtils.isEmpty(password))
        {
            errorCollection.addError("credential", i18n.getText("admin.jaacs.application.password.empty"));
            return false;
        }
        return true;
    }

    @Override
    public boolean resetPassword(final JiraServiceContext jiraServiceContext, String password, long applicationId)
    {
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final I18nHelper i18n = jiraServiceContext.getI18nBean();
        if (validateResetPassword(jiraServiceContext, password, applicationId))
        {
            Application app;
            try
            {
                app = applicationManager.findById(applicationId);
            }
            catch (ObjectNotFoundException e)
            {
                errorCollection.addError("remoteAddresses", i18n.getText("admin.jaacs.application.application.invalid"));
                return false;
            }
            try
            {
                applicationManager.updateCredential(app, new PasswordCredential(password));
            }
            catch (ObjectNotFoundException e)
            {
                errorCollection.addError("credential", i18n.getText("admin.jaacs.application.remote.set.password.error", e));
                log.error("Exception trying to set application password: " + e, e);
            }
            catch (ApplicationManagerException e)
            {
                errorCollection.addErrorMessage(i18n.getText("admin.jaacs.application.remote.set.password.error", e));
                log.error("Exception trying to set application password: " + e, e);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<Application> findAll(User remoteUser)
    {
        ensureIsAdmin(remoteUser);
        List<Application> allApplications = applicationManager.findAll();
        Collection<Application> nonPermanentApplications = Collections2.filter(allApplications, new ExcludePermanent());
        return Lists.newArrayList(nonPermanentApplications);
    }

    @Override
    public ApplicationImpl findById(User remoteUser, Long applicationId)
    {
        ensureIsAdmin(remoteUser);
        try
        {
            Application app = applicationManager.findById(applicationId);

            // never return "permanent" apps. they should not be seen by clients
            return app.isPermanent() ? null : ApplicationImpl.newInstance(app);
        }
        catch (ObjectNotFoundException e)
        {
            return null;
        }
    }

    @Override
    public void create(User remoteUser, Application application)
    {
        ensureIsAdmin(remoteUser);
        try
        {
            validateApplication(remoteUser, application);
            applicationManager.add(application);
        }
        catch (InvalidCredentialException e)
        {
            throw new ValidationFailureException(i18n(remoteUser).getText("admin.jaacs.application.must.be.admin"), e);
        }
        catch (DataAccessException e)
        {
            throw new ValidationFailureException(i18n(remoteUser).getText("admin.jaacs.application.create.failed", application.getName()), e);
        }
    }

    @Override
    public void update(User remoteUser, Application updatedApplication)
    {
        ensureIsAdmin(remoteUser);
        try
        {
            validateApplication(remoteUser, updatedApplication);
            applicationManager.update(updatedApplication);
        }
        catch (ObjectNotFoundException e)
        {
            throw new ValidationFailureException(i18n(remoteUser).getText("admin.jaacs.application.not.found", updatedApplication.getId()), e);
        }
        catch (ApplicationManagerException e)
        {
            throw new ValidationFailureException(i18n(remoteUser).getText("admin.jaacs.application.update.failed", displayName(remoteUser, updatedApplication.getId())), e);
        }
        catch (DataAccessException e)
        {
            throw new ValidationFailureException(i18n(remoteUser).getText("admin.jaacs.application.update.failed", displayName(remoteUser, updatedApplication.getId())), e);
        }
    }

    protected void validateJiraServiceContext(final JiraServiceContext jiraServiceContext)
    {
        if (jiraServiceContext == null)
        {
            throw new IllegalArgumentException("The JiraServiceContext must not be null.");
        }
        if (jiraServiceContext.getErrorCollection() == null)
        {
            throw new IllegalArgumentException("The error collection must not be null.");
        }
    }

    /**
     * If the remote user is not an administrator, throws a ValidationFailureException with the appropriate error.
     *
     * @param remoteUser a User representing the user on whose behalf to perform the call
     * @throws com.atlassian.jira.util.ValidationFailureException if the user is not an admin
     */
    protected void ensureIsAdmin(User remoteUser) throws ValidationFailureException
    {
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, remoteUser))
        {
            throw new ValidationFailureException(i18n(remoteUser).getText("admin.jaacs.application.admin.required"));
        }
    }

    /**
     * Validates the application's name, password, and remote addresses fields.
     *
     * @param user a User on whose behalf
     * @param application the Application to validate
     * @throws ValidationFailureException if any of the field values is not valid
     */
    protected void validateApplication(User user, Application application) throws ValidationFailureException
    {
        ErrorCollection errors = new SimpleErrorCollection();
        if (isBlank(application.getName()))
        {
            errors.addError("name", i18n(user).getText("admin.jaacs.application.remote.application.name.required"));
        }

        if (application.getCredential() == null || isBlank(application.getCredential().getCredential()))
        {
            errors.addError("credential", i18n(user).getText("admin.jaacs.application.password.empty"));
        }

        for (RemoteAddress remoteAddress : application.getRemoteAddresses())
        {
            if (!IPMatcher.isValidPatternOrHost(remoteAddress.getAddress()))
            {
                errors.addError("remoteAddresses", i18n(user).getText("admin.jaacs.application.remote.address.invalid.ip", remoteAddress.getAddress()));
                break;
            }
        }

        if (errors.hasAnyErrors())
        {
            throw new ValidationFailureException(errors);
        }
    }

    /**
     * Attempts to read the the Application with the given id from the database, and returns its display name. If there
     * is a problem reading the application from the database, this method returns the application's id.
     *
     * @param remoteUser the User on whose behalf this service is operating
     * @param applicationId a Long containing an application id
     * @return a String containing the application's display name, or its id
     */
    protected String displayName(User remoteUser, Long applicationId)
    {
        try
        {
            return findById(remoteUser, applicationId).getName();
        }
        catch (RuntimeException e)
        {
            log.warn("Could not read application with id: %d" + applicationId, e);
            return String.valueOf(applicationId);
        }
    }

    /**
     * Returns an I18nHelper for the given user.
     *
     * @param user a User
     * @return an I18nHelper
     */
    protected I18nHelper i18n(User user)
    {
        return i18nFactory.getInstance(user);
    }

    /**
     * Excludes the permanent application, which is the built-in "crowd-embedded" application.
     */
    static class ExcludePermanent implements Predicate<Application>
    {
        @Override
        public boolean apply(Application application)
        {
            return !application.isPermanent();
        }
    }
}
