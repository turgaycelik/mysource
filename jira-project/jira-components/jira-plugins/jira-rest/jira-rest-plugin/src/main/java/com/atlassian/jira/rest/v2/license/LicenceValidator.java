package com.atlassian.jira.rest.v2.license;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * A REST endpoint to provide simple validation services for a JIRA license.
 * Typically used by the setup phase of the JIRA application.  This will return an object
 * with a list of errors as key, value pairs.
 *
 * @since v5.2
 */
@Path ("licenseValidator")
@AnonymousAllowed
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class LicenceValidator
{
    private final I18nHelper i18nHelper;
    private final JiraLicenseService licenseService;

    public LicenceValidator(JiraLicenseService licenseService, I18nHelper i18nHelper)
    {
        this.i18nHelper = i18nHelper;
        this.licenseService = licenseService;
    }

    @POST
    public LicenseValidationResults validate(String license)
    {
        JiraLicenseService.ValidationResult validationResults = licenseService.validate(i18nHelper, license);
        return extractValidationDetails(validationResults);
    }

    private LicenseValidationResults extractValidationDetails(JiraLicenseService.ValidationResult validationResults)
    {
        LicenseValidationResults results = new LicenseValidationResults();
        results.setErrors(validationResults.getErrorCollection().getErrors());
        results.setLicenseString(validationResults.getLicenseString());
        return results;
    }
}
