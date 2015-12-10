package com.atlassian.jira.gadgets.system;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.system.SystemInfoUtils;
import com.atlassian.jira.util.system.check.SystemEnvironmentChecklist;
import com.atlassian.jira.web.util.ExternalLinkUtil;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static com.atlassian.jira.util.collect.CollectionUtil.first;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.status;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.substringAfter;

/**
 * REST endpoint for the admin-gadget.
 *
 * @since v4.0
 */
@Path ("/admin")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class AdminGadgetResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;
    private final SystemInfoUtils systemInfoUtils;
    private final GlobalPermissionManager globalPermissionManager;
    private final UserUtil userUtil;
    private final ExternalLinkUtil externalLinkUtil;
    private final JiraLicenseService jiraLicenseService;
    private final AdminTaskManager adminTaskManager;
    private final FeatureManager featureManager;
    private final PluginAccessor pluginAccessor;
    private final JiraLicenseService licenseService;

    public AdminGadgetResource(
            final JiraAuthenticationContext authenticationContext,
            final PermissionManager permissionManager,
            final ApplicationProperties applicationProperties,
            final SystemInfoUtils systemInfoUtils,
            final GlobalPermissionManager globalPermissionManager,
            final UserUtil userUtil,
            final JiraLicenseService jiraLicenseService,
            final AdminTaskManager adminTaskManager,
            final FeatureManager featureManager,
            final PluginAccessor pluginAccessor)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.systemInfoUtils = systemInfoUtils;
        this.globalPermissionManager = globalPermissionManager;
        this.userUtil = userUtil;
        this.adminTaskManager = adminTaskManager;
        this.pluginAccessor = pluginAccessor;
        this.jiraLicenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.externalLinkUtil = ExternalLinkUtilImpl.getInstance();
        this.featureManager = featureManager;
        this.licenseService = jiraLicenseService;
    }

    @GET
    public Response getData()
    {
        List<String> list = getWarningMessages();
        return Response.ok(createAdminProperties(list)).cacheControl(NO_CACHE).build();
    }

    @PUT
    @Path("task/done")
    public Response setTaskDone(@QueryParam ("name") final String name)
    {
        adminTaskManager.setTaskMarkedAsCompleted(authenticationContext.getLoggedInUser(), name, true);
        return status(NO_CONTENT).build();
    }

    @PUT
    @Path("task/undone")
    public Response setTaskUndone(@QueryParam ("name") final String name)
    {
        adminTaskManager.setTaskMarkedAsCompleted(authenticationContext.getLoggedInUser(), name, false);
        return status(NO_CONTENT).build();
    }

    @PUT
    @Path("tasklist/done")
    public Response setTaskListDone(@QueryParam ("name") final String name)
    {
        adminTaskManager.setTaskListDimissed(authenticationContext.getLoggedInUser(), name, true);
        return status(NO_CONTENT).build();
    }

    @PUT
    @Path("tasklist/undone")
    public Response setTaskListUndone(@QueryParam ("name") final String name)
    {
        adminTaskManager.setTaskListDimissed(authenticationContext.getLoggedInUser(), name, false);
        return status(NO_CONTENT).build();
    }

    private Object createAdminProperties(final List<String> list)
    {
        boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
        if (isAdmin)
        {
            return new AdminProperties(this, list, featureManager);
        }
        else
        {
            return new NonAdminProperties();
        }
    }

    protected String sen()
    {
        return substringAfter(licenseService.getLicense().getSupportEntitlementNumber(), "SEN-");
    }

    protected List<String> getWarningMessages()
    {
        return SystemEnvironmentChecklist.getWarningMessages(authenticationContext.getLocale(), true);
    }

    protected boolean userIsLicenseHolder()
    {
        LicenseDetails license = licenseService.getLicense();
        if (license == null || license.getContacts().size() < 1)
        {
            return false;
        }
        String licenseeEmail = first(license.getContacts()).getEmail();
        if (isBlank(licenseeEmail))
        {
            return false;
        }
        ApplicationUser currentUser = authenticationContext.getUser();
        if (currentUser == null || isBlank(currentUser.getEmailAddress()))
        {
            return false;
        }
        return currentUser.getEmailAddress().equals(licenseeEmail);
    }


    @XmlRootElement
    public static class NonAdminProperties
    {
        @XmlElement
        boolean isAdmin;

        public NonAdminProperties()
        {
            isAdmin = false;
        }
    }

    @XmlRootElement
    public static class AdminProperties
    {
        public static final String ADD_PRODUCT_URL = "https://my.atlassian.com/ondemand/configure/%s?modifyAction=activate&productKey=%s";
        @XmlElement
        boolean isAdmin;

        @XmlElement
        boolean notExternalUserManagement;

        @XmlElement
        boolean hasZeroUserLicense;

        @XmlElement
        boolean hasExceededUserLimit;

        @XmlElement
        boolean hasReachedUserLimit;

        @XmlElement
        boolean isLicenseHolder;

        @XmlElement
        String dbConfigDocsUrl;

        @XmlElement
        boolean isSystemAdministrator;

        @XmlElement
        boolean isUsingHsql;

        @XmlElement
        String licenseStatusMessage;

        @XmlElement
        List<String> warningMessages;

        @XmlElement
        boolean nearExpiry;

        @XmlElement
        String licenseTypeNiceName;

        @XmlElement
        String partnerName;

        @XmlElement
        String licenseExpiryStatusMessage;

        @XmlElement
        String externalLinkMyAccount;

        @XmlElement
        String externalLinkPersonalSite;

        @XmlElement
        AdminTaskManager.AdminTaskLists tasks;

        @XmlElement
        boolean isOnDemand;

        @XmlElement
        String browseDocsUrl;

        @XmlElement
        String defineWorkflowsDocsUrl;

        @XmlElement
        String customizeFieldsDocsUrl;

        @XmlElement
        String customizeScreensDocsUrl;

        @XmlElement
        String manageUsersDocsUrl;

        @XmlElement
        String timeTrackingDocsUrl;

        @XmlElement
        String migrationDocsUrl;

        @XmlElement
        String addBonfireToODUrl;

        @XmlElement
        String addGreenhopperToODUrl;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private AdminProperties()
        {
        }

        private AdminProperties(AdminGadgetResource resource, List<String> warningMessages, FeatureManager featureManager)
        {
            this.warningMessages = warningMessages;
            this.isOnDemand = featureManager.isOnDemand();
            LicenseDetails licenseDetails = resource.jiraLicenseService.getLicense();
            notExternalUserManagement = !resource.applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
            isAdmin = resource.permissionManager.hasPermission(Permissions.ADMINISTER, resource.authenticationContext.getLoggedInUser());
            hasZeroUserLicense = licenseDetails.getMaximumNumberOfUsers() == 0;
            hasExceededUserLimit = resource.userUtil.hasExceededUserLimit();
            hasReachedUserLimit = !resource.userUtil.canActivateNumberOfUsers(1);
            isSystemAdministrator = isSystemAdministrator(resource.authenticationContext, resource);
            licenseStatusMessage = licenseDetails.getLicenseStatusMessage(resource.authenticationContext.getLoggedInUser(), "<br/><br/>");
            nearExpiry = licenseDetails.isLicenseAlmostExpired();
            licenseTypeNiceName = licenseDetails.getDescription();
            partnerName = licenseDetails.getPartnerName();
            isUsingHsql = resource.systemInfoUtils.getDatabaseType().equalsIgnoreCase("hsql");
            licenseExpiryStatusMessage = licenseDetails.getLicenseExpiryStatusMessage(resource.authenticationContext.getLoggedInUser());
            externalLinkMyAccount = resource.externalLinkUtil.getProperty("external.link.atlassian.my.account");
            externalLinkPersonalSite = resource.externalLinkUtil.getProperty("external.link.jira.personal.site");
            tasks = resource.adminTaskManager.getAdminTaskLists(resource.authenticationContext.getLoggedInUser());
            dbConfigDocsUrl = getHelpUrl("dbconfig.generic");
            browseDocsUrl = getHelpUrl("gadget.admin.documentation.browse");
            defineWorkflowsDocsUrl = getHelpUrl("gadget.admin.documentation.workflows");
            customizeFieldsDocsUrl = getHelpUrl("gadget.admin.documentation.fields");
            customizeScreensDocsUrl = getHelpUrl("gadget.admin.documentation.screens");
            manageUsersDocsUrl = getHelpUrl("gadget.admin.documentation.manage.users");
            timeTrackingDocsUrl = getHelpUrl("gadget.admin.documentation.time.tracking");
            migrationDocsUrl = getHelpUrl("gadget.admin.documentation.migration");
            addBonfireToODUrl = String.format(ADD_PRODUCT_URL, resource.sen(), "bonfire.jira.ondemand");
            addGreenhopperToODUrl = String.format(ADD_PRODUCT_URL, resource.sen(), "greenhopper.jira.ondemand");
            isLicenseHolder = resource.userIsLicenseHolder();
        }

        private String getHelpUrl(String helpKey) {
            return HelpUtil.getInstance().getHelpPath(helpKey, isOnDemand).getUrl();
        }

        private boolean isSystemAdministrator(JiraAuthenticationContext authenticationContext, AdminGadgetResource adminGadgetResource)
        {
            User user = authenticationContext.getLoggedInUser();
            return ((user != null) && adminGadgetResource.globalPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user));
        }

    }
}
