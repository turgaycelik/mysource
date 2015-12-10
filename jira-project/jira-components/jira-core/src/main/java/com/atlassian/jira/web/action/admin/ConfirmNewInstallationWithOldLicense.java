/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.BuildNumComparator;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.web.util.MetalResourcesManager;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.component.ComponentAccessor;


import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;

import webwork.action.ServletActionContext;

import static com.atlassian.jira.license.LicenseJohnsonEventRaiser.CLUSTERING_UNLICENSED;
import static com.atlassian.jira.license.LicenseJohnsonEventRaiser.LICENSE_TOO_OLD;
import static com.atlassian.jira.license.LicenseJohnsonEventRaiser.SUBSCRIPTION_EXPIRED;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Iterables.any;

/**
 * Displays the page to update the current JIRA license when it has been detected that the current license is
 * <em>&quot;too old&quot;</em>. <p/> <p>Security: This action is only accessible when a Johnson Event of type {@link
 * com.atlassian.jira.license.LicenseJohnsonEventRaiser#LICENSE_TOO_OLD} is present in the {@link JohnsonEventContainer}
 * </p> <p/> <p>Trigger: The link to display this action is displayed in the Johnson errors page (errors.jsp)</p>
 *
 * @see com.atlassian.jira.license.LicenseJohnsonEventRaiser
 * @see com.atlassian.jira.upgrade.UpgradeLauncher
 * @see JohnsonEventContainer
 */
public class ConfirmNewInstallationWithOldLicense extends JiraWebActionSupport
{
    private static final String CROWD_EMBEDDED_INTEGRATION_VERSION = "602";
    public static final String RADIO_OPTION_LICENSE = "license";
    public static final String RADIO_OPTION_EVALUATION = "evaluation";
    public static final String EXTERNAL_LINK_JIRA_LICENSE_VIEW_CLUSTERED = "external.link.jira.license.data.center.contact";
    public static final String EXTERNAL_LINK_JIRA_LICENSE_VIEW_TIMEBOMB = "external.link.jira.license.view.timebomb";
    public static final String CLUSTERED_TIMEBOMB_ANCHOR = "TimebombLicensesforTesting-TestingDataCentercompatibility";
    public static final String EXTERNAL_LINK_JIRA_LICENSE_VIEW = "external.link.jira.license.view";
    public static final String LICENSE_DESC_LINK_CONTENT = "licenseDescLinkContent";
    public static final String LICENSE_DESC_LINK_TIMEBOMB_CONTENT = "licenseDescLinkTimebombContent";

    private final JiraLicenseUpdaterService jiraLicenseService;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraSystemRestarter jiraSystemRestarter;
    private final CrowdService crowdService;
    private final PermissionManager permissionManager;
    private final ClusterManager clusterManager;
    private final JiraProperties jiraSystemProperties;

    private String userName;
    private String password;
    private String licenseString;
    private String radioOption;
    private boolean licenseUpdated = false;
    private boolean installationConfirmed = false;
    private LicenseDetails licenseDetails;
    private JiraLicenseService.ValidationResult validationResult;

    private boolean loginInvalid = false;
    private boolean radioOptionInvalid = false;

    public ConfirmNewInstallationWithOldLicense(final JiraLicenseUpdaterService jiraLicenseService,
            final BuildUtilsInfo buildUtilsInfo, final JiraSystemRestarter jiraSystemRestarter,
            CrowdService crowdService, PermissionManager permissionManager, JiraProperties jiraSystemProperties,
            ClusterManager clusterManager)
    {
        this.crowdService = crowdService;
        this.permissionManager = permissionManager;
        this.clusterManager = clusterManager;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraSystemRestarter = notNull("jiraSystemRestarter", jiraSystemRestarter);
        this.jiraSystemProperties = jiraSystemProperties;
    }

    /**
     * Whether this page should be displayed to the end user.
     *
     * @return true if the page should be displayed; otherwise, false.
     */
    private boolean shouldDisplay()
    {
        return isPresentInJohnsonEventContainer(EventType.get(LICENSE_TOO_OLD)) ||
                isPresentInJohnsonEventContainer(EventType.get(CLUSTERING_UNLICENSED)) ||
                isPresentInJohnsonEventContainer(EventType.get(SUBSCRIPTION_EXPIRED));
    }

    /**
     * Whether there is any event of the specified type in the current johnson event container.
     *
     * @param eventType The event type to look for.
     * @return true if there is any event of the specified type; otherwise, false.
     */
    private boolean isPresentInJohnsonEventContainer(final EventType eventType)
    {
        final class IsEventOfType implements Predicate<Event>
        {
            private EventType eventType;

            private IsEventOfType(final EventType eventType) {this.eventType = eventType;}

            @Override
            public boolean apply(@Nullable com.atlassian.johnson.event.Event event)
            {
                return event != null && (event.getKey().equals(eventType));
            }
        }

        return any(getJohnsonEventContainer().getEvents(), new IsEventOfType(eventType));
    }

    private boolean isEvaluationOptionDisplayable()
    {
        LicenseDetails licenseDetails = this.getLicenseDetails();
        if (licenseDetails.isDataCenter())
        {
            return false;
        }
        if (licenseDetails.isEnterpriseLicenseAgreement())
        {
            return false;
        }
        if (clusterManager.isClustered())
        {
            return false;
        }

        return true;
    }

    @Override
    public String doDefault() throws Exception
    {
        if (!shouldDisplay())
        {
            return "securitybreach";
        }

        return INPUT;
    }

    protected void doValidation()
    {
        if (!shouldDisplay())
        {
            return; // break out of here early if we are not allowed to access this page.
        }

        if (StringUtils.isBlank(radioOption))
        {
            log.warn("Neither the License nor the Install Confirmation have been supplied.");
            addErrorMessage(getText("admin.errors.no.license"));
            setRadioOptionInvalid(true);
            return;
        }

        if (getUserInfoAvailable())
        {
            //check that the user is an admin and that the password is correct
            User user = crowdService.getUser(userName);

            if (user == null)
            {
                addErrorMessage(getText("admin.errors.invalid.username.or.pasword"));
                setLoginInvalid(true);
                return;
            }

            try
            {
                crowdService.authenticate(userName, password);
            }
            catch (FailedAuthenticationException e)
            {
                addErrorMessage(getText("admin.errors.invalid.username.or.pasword"));
                setLoginInvalid(true);
                return;
            }

            if (!nonAdminUpgradeAllowed())
            {
                // Try to find an ApplicationUser for this directory user:
                String key = ComponentAccessor.getUserKeyService().getKeyForUsername(userName);
                final ApplicationUser applicationUser;
                if (key == null)
                {
                    // JRA-37644: assume this is pre-6.0 data before user keys existed
                    applicationUser = new DelegatingApplicationUser(userName, user);
                }
                else
                {
                    applicationUser = new DelegatingApplicationUser(key, user);
                }

                boolean hasAdminPermission = permissionManager.hasPermission(Permissions.ADMINISTER, applicationUser);

                if (!hasAdminPermission)
                {
                    addErrorMessage(getText("admin.errors.no.admin.permission"));
                    setLoginInvalid(true);
                    return;
                }
            }
        }

        if (radioOption.equals(RADIO_OPTION_LICENSE))
        {
            validationResult = jiraLicenseService.validate(this, licenseString);
            addErrorCollection(validationResult.getErrorCollection());
        }
    }

    protected String doExecute() throws Exception
    {
        if (!shouldDisplay())
        {
            return "securitybreach";
        }

        // Check if the license has been entered
        if (radioOption.equals(RADIO_OPTION_LICENSE))
        {
            licenseDetails = jiraLicenseService.setLicense(validationResult);
            if (!licenseDetails.isMaintenanceValidForBuildDate(buildUtilsInfo.getCurrentBuildDate()))
            {
                addError("license", getText("admin.errors.license.too.old"));
                return ERROR;
            }
            else
            {
                licenseUpdated = true;
            }
        }
        else if (radioOption.equals(RADIO_OPTION_EVALUATION)) // Check that the Installation under the Evaluation Terms has been confirmed
        {
            jiraLicenseService.confirmProceedUnderEvaluationTerms(userName);
            installationConfirmed = true;
        }
        else
        {
            throw new IllegalStateException("This will never happen!");
        }

        // rock JIRA's world!
        jiraSystemRestarter.ariseSirJIRA();

        // Remove the Old Licence Event
        JohnsonEventContainer cont = JohnsonEventContainer.get(ServletActionContext.getServletContext());
        for (final Object o : cont.getEvents())
        {
            Event event = (Event) o;
            if (event != null && isLicenseEvent(event))
            {
                cont.removeEvent(event);
            }
        }
        //JRADEV-22455 :- prevent anyone from clicking the link before restart - in 6.1 this link is removed
        cont.addEvent(new Event(EventType.get("restart"),
                getText("system.error.restart.for.changes"),
                EventLevel.get(EventLevel.FATAL)));

        return SUCCESS;
    }

    private boolean isLicenseEvent(Event event)
    {
        return anyMatch(event.getKey(), LICENSE_TOO_OLD, CLUSTERING_UNLICENSED, SUBSCRIPTION_EXPIRED);
    }

    private boolean anyMatch(EventType key, String... subscriptionExpired)
    {
        for (String event : subscriptionExpired)
        {
            if (key.equals(EventType.get(event)))
            {
                return true;
            }
        }
        return false;
    }

    JohnsonEventContainer getJohnsonEventContainer()
    {
        return JohnsonEventContainer.get(ServletActionContext.getServletContext());
    }

    public LicenseDetails getLicenseDetails()
    {
        if (licenseDetails == null)
        {
            licenseDetails = jiraLicenseService.getLicense();
        }
        return licenseDetails;
    }

    public Map<String, String> getLicenseStatusMessages()
    {
        LicenseDetails.LicenseStatusMessage licenseStatusMessage = getLicenseDetails().getLicenseStatusMessage(getI18nHelper());
        // null means "don't show"
        return licenseStatusMessage == null ? ImmutableMap.<String, String>of() :licenseStatusMessage.getAllMessages();
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setLicense(String licenseString)
    {
        this.licenseString = licenseString;
    }

    public void setRadioOption(String radioOption)
    {
        this.radioOption = radioOption;
    }

    public void setLoginInvalid(boolean loginInvalid)
    {
        this.loginInvalid = loginInvalid;
    }

    public void setRadioOptionInvalid(boolean radioOptionInvalid)
    {
        this.radioOptionInvalid = radioOptionInvalid;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getPassword()
    {
        return password;
    }

    public String getLicense()
    {
        return licenseString;
    }

    public String getRadioOption()
    {
        return radioOption;
    }

    public boolean getLoginInvalid()
    {
        return loginInvalid;
    }

    public boolean getRadioOptionInvalid()
    {
        return radioOptionInvalid;
    }

    private boolean nonAdminUpgradeAllowed()
    {
        return jiraSystemProperties.getBoolean(SystemPropertyKeys.UPGRADE_SYSTEM_PROPERTY);
    }

    public BuildUtilsInfo getBuildUtilsInfo()
    {
        return buildUtilsInfo;
    }

    public String getCurrentBuildDate()
    {
        return getOutlookDate().formatDMY(buildUtilsInfo.getCurrentBuildDate());
    }

    /**
     * During upgrades from 4.2 or earlier to 4.3 or later the user information is not available until after the upgrade
     * has run.
     *
     * @return True if user information is available and we can authenticate users.
     */
    public boolean getUserInfoAvailable()
    {
        BuildNumComparator comparator = new BuildNumComparator();
        // If the code version running is pre the crowd integration then just return true.
        if (comparator.compare(buildUtilsInfo.getCurrentBuildNumber(), CROWD_EMBEDDED_INTEGRATION_VERSION) < 0)
        {
            return true;
        }

        String currentDatabaseVersion = getApplicationProperties().getString(APKeys.JIRA_PATCHED_VERSION);
        return !Strings.isNullOrEmpty(currentDatabaseVersion) && comparator.compare(currentDatabaseVersion, CROWD_EMBEDDED_INTEGRATION_VERSION) > 0;
    }

    @ActionViewDataMappings ({ "input", "error" })
    public Map<String, Object> getDataMap()
    {
        String generalErrorMessage;

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("serverId", getServerId());
        data.put("errors", getErrors());
        data.put("radioOption", getRadioOption());
        data.put("radioOptionLicense", RADIO_OPTION_LICENSE);
        data.put("radioOptionEvaluation", RADIO_OPTION_EVALUATION);
        data.put("expired", getLicenseDetails().isExpired());
        data.put("evaluationOptionDisplayable", isEvaluationOptionDisplayable());
        data.put("clustered", clusterManager.isClustered());

        ExternalLinkUtil externalLinkUtil = ExternalLinkUtilImpl.getInstance();
        if (clusterManager.isClustered())
        {
            data.put(LICENSE_DESC_LINK_CONTENT, getAnchorTagForLink(externalLinkUtil,
                    EXTERNAL_LINK_JIRA_LICENSE_VIEW_CLUSTERED, null));
            data.put(LICENSE_DESC_LINK_TIMEBOMB_CONTENT, getAnchorTagForLink(externalLinkUtil,
                    EXTERNAL_LINK_JIRA_LICENSE_VIEW_TIMEBOMB, CLUSTERED_TIMEBOMB_ANCHOR));
        }
        else
        {
            // generating opening tag for link redirecting to generating evaluation license online
            String[] linkParams = new String[] {
                    getBuildUtilsInfo().getVersion(),
                    getBuildUtilsInfo().getCurrentBuildNumber(),
                    "Enterprise",
                    (String) data.get("serverId")
            };
            data.put(LICENSE_DESC_LINK_CONTENT, getAnchorTagForLink(externalLinkUtil, EXTERNAL_LINK_JIRA_LICENSE_VIEW, linkParams));
        }


        // dealing with general error messages
        generalErrorMessage = getErrorMessages().isEmpty() ? null : getErrorMessages().iterator().next();
        data.put("loginErrorMessageContent", getLoginInvalid() ? generalErrorMessage : null);
        setLoginErrorMessageTitle(generalErrorMessage, data);

        data.put("radioOptionErrorMessageContent", getRadioOptionInvalid() ? generalErrorMessage : null);

        // license status messages
        Map<String, String> licenseStatusMessages = getLicenseStatusMessages();
        if (licenseStatusMessages.get("admin.license.support.and.updates") != null)
        {
            data.put("licenseStatusMessage1Content", licenseStatusMessages.get("admin.license.support.and.updates"));
            data.put("licenseStatusMessage2Content", licenseStatusMessages.get("admin.license.renewal.target"));
        }
        else
        {
            data.put("licenseStatusMessage1Content", licenseStatusMessages.get("admin.license.evaluation"));
            data.put("licenseStatusMessage2Content", licenseStatusMessages.get("admin.license.evaluation.renew"));
        }

        // values of fields
        data.put("userNameValue", getUserName());
        data.put("licenseValue", getLicense());

        // for the fake decorator
        data.put("jiraTitle", getApplicationProperties().getDefaultBackedString(APKeys.JIRA_TITLE));
        data.put("jiraLogoUrl", getApplicationProperties().getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL));

        data.put("resourcesContent", MetalResourcesManager.getMetalResources(getHttpRequest().getContextPath()));

        return data;
    }

    @ActionViewDataMappings ({ "success" })
    public Map<String, Object> getDataMapSuccess()
    {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("licenseUpdated", licenseUpdated);
        data.put("installationConfirmed", installationConfirmed);

        // for the fake decorator
        data.put("jiraTitle", getApplicationProperties().getDefaultBackedString(APKeys.JIRA_TITLE));
        data.put("jiraLogoUrl", getApplicationProperties().getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL));

        data.put("resourcesContent", MetalResourcesManager.getMetalResources(getHttpRequest().getContextPath()));

        return data;
    }

    /**
     * Set the title to be used with the login error message.
     *
     * @param generalErrorMessage
     * @param data
     */
    private void setLoginErrorMessageTitle(String generalErrorMessage, Map<String, Object> data)
    {
        if (StringUtils.equals(generalErrorMessage, getText("admin.errors.no.admin.permission")))
        {
            data.put("loginErrorMessageTitle", getLoginInvalid() ?
                    getText("admin.errors.no.admin.permission.title") : null);
        }
    }

    private String getAnchorTagForLink(final ExternalLinkUtil util, final String link, final Object linkParams)
    {
        if (linkParams != null)
        {
            return "<a href=\"" + util.getProperty(link, linkParams) + "\">";
        }
        else
        {
            return "<a href=\"" + util.getProperty(link) + "\">";
        }
    }

}
