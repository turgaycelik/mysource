package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.security.auth.trustedapps.Application;
import com.atlassian.security.auth.trustedapps.ApplicationRetriever;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.google.common.base.Functions;

import static com.google.common.collect.Iterables.transform;

/**
 * Implements the {@link com.atlassian.security.auth.trustedapps.TrustedApplicationsManager}
 *
 * @since v3.12
 */
public class SeraphTrustedApplicationsManager implements TrustedApplicationsManager, TrustedApplicationsConfigurationManager
{
    private final TrustedApplicationManager manager;
    private final CurrentApplicationFactory applicationFactory;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public SeraphTrustedApplicationsManager(final TrustedApplicationManager manager, final CurrentApplicationFactory applicationFactory, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        Null.not("trustedApplicationManager", manager);
        Null.not("applicationFactory", applicationFactory);
        Null.not("jiraAuthenticationContext", jiraAuthenticationContext);

        this.manager = manager;
        this.applicationFactory = applicationFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public TrustedApplication getTrustedApplication(final String id)
    {
        final TrustedApplicationInfo app = manager.get(id);
        if (app != null  && app.isValidKey())
        {
            return app;
        }
        final CurrentApplication currentApplication = getCurrentApplication();
        if (currentApplication.getID().equals(id) && (currentApplication instanceof TrustedApplication))
        {
            return (TrustedApplication) currentApplication;
        }
        return null;
    }

    public CurrentApplication getCurrentApplication()
    {
        return applicationFactory.getCurrentApplication();
    }

    /**
     * @return all configured Trusted Applications.
     */
    @Override
    public Iterable<TrustedApplication> getTrustedApplications()
    {
        return transform(manager.getAll(), Functions.<TrustedApplication> identity());
    }

    /**
     * Adds the specified Trusted Application.
     *
     * @param conditions the conditions that incoming requests must meet in order to be accepted.
     * @return the newly created Trusted Application.
     */
    @Override
    public TrustedApplication addTrustedApplication(Application app, RequestConditions conditions)
    {
        final TrustedApplicationInfo existingApplicationInfo = manager.get(app.getID());
        final TrustedApplicationInfo storedApplicationInfo;
        if (existingApplicationInfo != null)
        {
            storedApplicationInfo = new TrustedApplicationBuilder()
                .set(existingApplicationInfo)
                .set(app)
                .set(conditions)
                .toInfo();
        }
        else
        {
            // For legacy reasons, TrustedApplications must have a non-null name (and remember that "" == null in Oracle)
            storedApplicationInfo = new TrustedApplicationBuilder()
                .set(app)
                .setName(app.getID())
                .set(conditions)
                .toInfo();
        }
        manager.store(jiraAuthenticationContext.getLoggedInUser(), storedApplicationInfo);
        return storedApplicationInfo;
    }

    /**
     * Removes the specified Trusted Application.
     *
     * @param applicationID the ID of the trusted application.
     * @return {@code true} if the Trusted Application with the specified ID was found and removed, {@code false} if the
     *         specified ID was not found.
     */
    @Override
    public boolean deleteApplication(String applicationID)
    {
        return manager.delete(jiraAuthenticationContext.getLoggedInUser(), applicationID);
    }

    /**
     * Retrieve the application certificate from some other application, over HTTP. Will look for the certificate at
     * <code>${baseUrl}/admin/appTrustCertificate</code>.
     *
     * @param baseUrl the base URL of the application to be queried
     * @return the retrieved application certificate
     * @throws com.atlassian.security.auth.trustedapps.ApplicationRetriever.RetrievalException if there are problems
     * with the certificate retrieved from the remote server or the server cannot be contacted
     * @throws RuntimeException if there are problems retrieving the certificate from the remote server
     */
    @Override
    public Application getApplicationCertificate(String baseUrl) throws ApplicationRetriever.RetrievalException
    {
        return KeyFactory.getEncryptionProvider().getApplicationCertificate(baseUrl);
    }
}
