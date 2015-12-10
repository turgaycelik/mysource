package com.atlassian.jira.web.action.util;

import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.DowngradeUtilsImpl;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

import javax.servlet.ServletContext;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;

/**
 * An error handler to be used both by {@link com.atlassian.jira.web.action.setup.SetupImport} and
 * {@link ImportAll} for some consistent error handling.
 *
 * @since v4.4
 */
public class DefaultImportResultHandler implements ImportResultHandler
{
    private final JiraLicenseService jiraLicenseService;
    private final LicenseJohnsonEventRaiser licenseJohnsonEventRaiser;
    private final ExternalLinkUtil externalLinkUtil;
    private final BuildUtilsInfo buildUtilsInfo;

    public DefaultImportResultHandler(final JiraLicenseService jiraLicenseService,
            final LicenseJohnsonEventRaiser licenseJohnsonEventRaiser, final ExternalLinkUtil externalLinkUtil,
            final BuildUtilsInfo buildUtilsInfo)
    {
        this.jiraLicenseService = jiraLicenseService;
        this.licenseJohnsonEventRaiser = licenseJohnsonEventRaiser;
        this.externalLinkUtil = externalLinkUtil;
        this.buildUtilsInfo = buildUtilsInfo;
    }

    public boolean handleErrorResult(ServletContext context, DataImportService.ImportResult lastResult, I18nHelper i18n, ErrorCollection errorCollection)
    {
        switch (lastResult.getImportError())
        {
            case UPGRADE_EXCEPTION:
                final JohnsonEventContainer eventCont = JohnsonEventContainer.get(context);
                final Event errorEvent = new Event(EventType.get("upgrade"), "An error occurred performing JIRA upgrade task", lastResult.getSpecificErrorMessage(), EventLevel.get(EventLevel.ERROR));
                if (eventCont != null)
                {
                    // Use licenseJohnsonEventRaiser to check for specific license errors; if it's not a Johnson problem report it normally.
                    final LicenseDetails licenseDetails = jiraLicenseService.getLicense();
                    if (!licenseJohnsonEventRaiser.checkLicenseIsTooOldForBuild(context, licenseDetails))
                    {
                        eventCont.addEvent(errorEvent);
                    }
                }
                return true;
            case V1_LICENSE_EXCEPTION:
                errorCollection.addErrorMessage(getLicenseErrorMessage(i18n, lastResult.getSpecificErrorMessage()));
                return false;
            case CUSTOM_PATH_EXCEPTION:
                errorCollection.addErrorMessage(i18n.getText("admin.errors.custom.path", "<a id=\"reimport\" href=\"#\">", "</a>"));
                return false;
            case DOWNGRADE_FROM_ONDEMAND:
                errorCollection.addErrorMessage(i18n.getText("admin.errors.import.downgrade.error", lastResult.getSpecificErrorMessage(), "<a id='acknowledgeDowngradeError' href='#'>", "</a>"));
                return false;
            default:
                return !lastResult.getErrorCollection().hasAnyErrors() && checkLicenseIsInvalidOrTooOldForBuild(context);
        }
    }

    private boolean checkLicenseIsInvalidOrTooOldForBuild(final ServletContext context)
    {
        final LicenseDetails licenseDetails = jiraLicenseService.getLicense();
        return licenseJohnsonEventRaiser.checkLicenseIsTooOldForBuild(context, licenseDetails) ;
    }

    private String getLicenseErrorMessage(final I18nHelper i18n, String licenseString)
    {
        JiraLicenseService.ValidationResult validationResult = jiraLicenseService.validate(i18n, licenseString);
        final NumberFormat nf = NumberFormat.getNumberInstance();
        final String upgradeLink = MessageFormat.format("<a target=\"_blank\" href=\"{0}\">", externalLinkUtil.getProperty("external.link.jira.upgrade.lic", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", jiraLicenseService.getServerId(), String.valueOf(validationResult.getTotalUserCount()), String.valueOf(validationResult.getActiveUserCount()))));
        final String evaluationLink = MessageFormat.format("<a target=\"_blank\" href=\"{0}\">", externalLinkUtil.getProperty("external.link.jira.license.view", Arrays.<String>asList(buildUtilsInfo.getVersion(), buildUtilsInfo.getCurrentBuildNumber(), "enterprise", jiraLicenseService.getServerId())));

        return i18n.getText("setup.error.invalidlicensekey.wrong.license.version.my.atlassian.link", upgradeLink, "</a>", evaluationLink, "</a>") +
                "<p>" +
                i18n.getText("setup.error.invalidlicensekey.wrong.license.version.how.many.users", nf.format(validationResult.getTotalUserCount()), nf.format(validationResult.getActiveUserCount())) +
                "<p>" +
                i18n.getText("setup.error.invalidlicensekey.whatisactive", "<i>", "</i>");
    }

}
