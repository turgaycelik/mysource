package com.atlassian.sal.jira.license;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.license.JiraLicenseManager;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.license.LicenseHandler;

import java.util.Locale;

/**
 * Jira implementation of license handler
 */
public class JiraLicenseHandler implements LicenseHandler
{
    private JiraLicenseService jiraLicenseService;
    private JiraLicenseManager jiraLicenseManager = null;
    private I18nHelper.BeanFactory i18nBeanFactory;

    public JiraLicenseHandler(JiraLicenseService jiraLicenseService, JiraLicenseManager jiraLicenseManager, I18nHelper.BeanFactory i18nBeanFactory)
    {
        this.jiraLicenseService = jiraLicenseService;
        this.jiraLicenseManager = jiraLicenseManager;
        this.i18nBeanFactory = i18nBeanFactory;
    }
    /**
     * Sets the license, going through the regular validation steps as if you used the web UI
     *
     * @param license The license string
     */
    public void setLicense(String license)
    {
        JiraLicenseService.ValidationResult validationResult = jiraLicenseService.validate(i18nBeanFactory.getInstance(Locale.getDefault()), license);
        if (validationResult.getErrorCollection().hasAnyErrors())
        {
            throw new IllegalArgumentException("Specified license was invalid.");
        }
        jiraLicenseManager.setLicense(license);
    }


    /**
     * Gets the server ID of the currently running application.  The server ID format is four quadruples of
     * alphanumeric characters, each separated by a dash (<tt>-</tt>).
     *
     * @return the server ID
     *
     * @since 2.7
     */
    @Override
    public String getServerId()
    {
        return jiraLicenseService.getServerId();
    }

    /**
     * Gets the Support Entitlement Number (SEN) for the currently running application.
     *
     * @return the Support Entitlement Number, or {@code null} if there is no current support entitlement.
     *
     * @since 2.7
     */
    @Override
    public String getSupportEntitlementNumber()
    {
        LicenseDetails license = jiraLicenseService.getLicense();
        return license != null ? license.getSupportEntitlementNumber() : null;
    }
}
