package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.HttpServletVariables;

import com.google.common.collect.Lists;
import webwork.action.ActionContext;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

public class SetupLicense extends AbstractSetupAction
{
    private static final String NEW_ACCOUNT = "newAccount";
    private static final String LOGIN = "login";
    private static final String EXISTING_LICENSE = "existingLicense";
    private static final String LICENSE_VALIDATION_RESULTS = "json";

    private final JiraLicenseUpdaterService licenseService;
    private final JiraSystemRestarter jiraSystemRestarter;
    private final JiraWebResourceManager webResourceManager;
    private final SetupSharedVariables sharedVariables;

    /**
     * Properties for when finally submitting.
     */
    private String licenseString;
    private String pluginLicenseString;
    private String firstName;
    private String lastName;
    private String email;
    private JiraLicenseService.ValidationResult validationResult;
    private String selectedBundle;

    /**
     * The license validation response.
     */
    private String licenseValidationJson;

    /**
     * The license to validate.
     */
    private String licenseToValidate;

    /**
     * Constructor.
     * @param fileFactory
     * @param licenseService
     * @param jiraSystemRestarter
     * @param servletVariables
     */
    public SetupLicense(FileFactory fileFactory, JiraLicenseUpdaterService licenseService, JiraSystemRestarter jiraSystemRestarter, JiraWebResourceManager webResourceManager, final HttpServletVariables servletVariables)
    {
        super(fileFactory);
        this.licenseService = licenseService;
        this.jiraSystemRestarter = jiraSystemRestarter;
        this.webResourceManager = webResourceManager;
        sharedVariables = new SetupSharedVariables(servletVariables, getApplicationProperties());
    }

    /**
     * A Simple action to allow ajax calls to verify a license, returing a json representation.
     * @return The json view of the validation.
     */
    public String doValidateLicense()
    {
        JiraLicenseService.ValidationResult validationResults = licenseService.validate(this, licenseToValidate);
        licenseValidationJson = new LicenseValidationResults(validationResults).toJson();
        if (validationResults.getErrorCollection().hasAnyErrors())
        {
            ActionContext.getContext().getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        return LICENSE_VALIDATION_RESULTS;
    }

    public void doValidation()
    {
        validationResult = licenseService.validate(this, licenseString);
        final ErrorCollection errorCollection = validationResult.getErrorCollection();
        if (errorCollection.hasAnyErrors())
        {
            addErrorCollection(errorCollection);
        }
        super.doValidation();
    }

    protected String doExecute()
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        boolean licensePreviouslySet = licenseService.getLicense().isLicenseSet();

        licenseService.setLicense(validationResult);

        // Avoid restarting if license previously set,
        // as some plugins that are brought up in the full container may contain servlet filters that break for mid-request reload
        // Example: JWDSendRedirectFilter
        if (!licensePreviouslySet)
        {
            jiraSystemRestarter.ariseSirJIRA();
        }

        request.getSession().setAttribute(SetupLicenseSessionStorage.SESSION_KEY, new SetupLicenseSessionStorage(firstName, lastName, email));
        sharedVariables.setBundleLicenseKey(pluginLicenseString);

        return getRedirect("SetupAdminAccount!default.jspa");
    }

    @Override
    public String doDefault() throws Exception {
        final String selectedBundle = sharedVariables.getSelectedBundle();
        webResourceManager.putMetadata(SetupSharedVariables.SETUP_CHOOSEN_BUNDLE, selectedBundle);
        return super.doDefault();
    }

    public void setLicenseToValidate(String license)
    {
        this.licenseToValidate = license.replace(' ', '+');
    }

    public String getLicenseToValidate()
    {
        return licenseToValidate;
    }

    public String getLicenseValidationResults()
    {
        return licenseValidationJson;
    }

    public void setSetupLicenseKey(String licenseString)
    {
        this.licenseString = licenseString;
    }

    public void setSetupPluginLicenseKey(String pluginLicenseString)
    {
        this.pluginLicenseString = pluginLicenseString;
    }

    public void setSetupEmail(String email)
    {
        this.email = email;
    }

    public void setSetupFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public void setSetupLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getSelectedBundle() {
        return selectedBundle;
    }

    public void setSelectedBundle(String selectedBundle) {
        this.selectedBundle = selectedBundle;
    }

    public List<SetupOptions> getLicenseSetupOptions()
    {
        return Lists.newArrayList(
                new SetupOptions(NEW_ACCOUNT, getText("setupLicense.type.register")),
                new SetupOptions(LOGIN, getText("setupLicense.type.login")),
                new SetupOptions(EXISTING_LICENSE, getText("setup.cross.selling.license.option.existing.license"))
        );
    }

    /**
     * A simple class used to provide key/value pairs for setup options.
     * Key is used to enumerate the list of options and value is used for display.
     */
    private class SetupOptions
    {
        private final String key;
        private final String value;

        private SetupOptions(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey()
        {
            return key;
        }

        public String getValue()
        {
            return value;
        }
    }
}
