package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.functest.framework.util.IssueTableClient;
import com.atlassian.jira.functest.framework.util.SearchersClient;
import com.atlassian.jira.testkit.client.AdvancedSettingsControl;
import com.atlassian.jira.testkit.client.AttachmentsControl;
import com.atlassian.jira.testkit.client.CustomFieldsControl;
import com.atlassian.jira.testkit.client.DarkFeaturesControl;
import com.atlassian.jira.testkit.client.DashboardControl;
import com.atlassian.jira.testkit.client.DataImportControl;
import com.atlassian.jira.testkit.client.GeneralConfigurationControl;
import com.atlassian.jira.testkit.client.I18nControl;
import com.atlassian.jira.testkit.client.IssueLinkingControl;
import com.atlassian.jira.testkit.client.IssueTypeControl;
import com.atlassian.jira.testkit.client.MailServersControl;
import com.atlassian.jira.testkit.client.PermissionSchemesControl;
import com.atlassian.jira.testkit.client.PermissionsControl;
import com.atlassian.jira.testkit.client.ScreensControl;
import com.atlassian.jira.testkit.client.SearchRequestControl;
import com.atlassian.jira.testkit.client.ServicesControl;
import com.atlassian.jira.testkit.client.SubtaskControl;
import com.atlassian.jira.testkit.client.SystemPropertiesControl;
import com.atlassian.jira.testkit.client.UsersAndGroupsControl;
import com.atlassian.jira.testkit.client.WebSudoControl;
import com.atlassian.jira.testkit.client.WorkflowSchemesControl;
import com.atlassian.jira.testkit.client.restclient.ComponentClient;
import com.atlassian.jira.testkit.client.restclient.ProjectRoleClient;
import com.atlassian.jira.testkit.client.restclient.SearchClient;
import com.atlassian.jira.testkit.client.restclient.VersionClient;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

/**
 * @since v5.2
 */
public class Backdoor
{
    private final com.atlassian.jira.testkit.client.Backdoor testkit;
    private final ApplicationPropertiesControl applicationPropertiesControl;
    private final UserProfileControl userProfileControl;
    private final TestRunnerControl testRunnerControl;
    private final EntityEngineControl entityEngineControl;
    private final FilterSubscriptionControl filterSubscriptionControl;
    private final IssueTypeScreenSchemesControl issueTypeScreenSchemes;
    private final BarrierControl barrierControl;
    private final PermissionsControl permissionsControl;
    private final FieldConfigurationControlExt fieldConfigurationControl;
    private final ScreensControlExt screensControl;
    private final ManagedConfigurationControl managedConfigurationControl;
    private final IndexingControl indexingControl;
    private final ProjectControlExt projectControl;
    private final IssuesControl issueNavControl;
    private final FiltersClient filtersControl;
    private final IssueTableClient issueTableClient;
    private final SearchersClient searchersClient;
    private final WorkflowsControlExt workflowsControlExt;
    private final PluginsControlExt pluginsControl;
    private final ColumnControl columnControl;
    private final VersionClient versionClient;
    private final ComponentClient componentClient;
    private final PluginIndexConfigurationControl pluginIndexConfigurationControl;
    private final PermissionSchemesControlExt permissionSchemesControl;
    private final LicenseRoleControl licenseRoleControl;
    private final LicenseControl licenseControl;

    public Backdoor(JIRAEnvironmentData environmentData)
    {
        testkit = new com.atlassian.jira.testkit.client.Backdoor(environmentData);
        applicationPropertiesControl = new ApplicationPropertiesControl(environmentData);
        entityEngineControl = new EntityEngineControl(environmentData);
        userProfileControl = new UserProfileControl(environmentData);
        testRunnerControl = new TestRunnerControl(environmentData);
        filterSubscriptionControl = new FilterSubscriptionControl(environmentData);
        issueTypeScreenSchemes = new IssueTypeScreenSchemesControl(environmentData);
        barrierControl = new BarrierControl(environmentData);
        permissionsControl = new PermissionsControl(environmentData);
        fieldConfigurationControl = new FieldConfigurationControlExt(environmentData);
        screensControl = new ScreensControlExt(environmentData);
        this.managedConfigurationControl = new ManagedConfigurationControl(environmentData);
        indexingControl = new IndexingControl(environmentData);
        projectControl = new ProjectControlExt(environmentData);
        issueNavControl = new IssuesControl(environmentData);
        filtersControl = new FiltersClient(environmentData);
        issueTableClient = new IssueTableClient(environmentData);
        searchersClient = new SearchersClient(environmentData);
        workflowsControlExt = new WorkflowsControlExt(environmentData);
        pluginsControl = new PluginsControlExt(environmentData);
        columnControl = new ColumnControl(environmentData);
        componentClient = new ComponentClient(environmentData);
        versionClient = new VersionClient(environmentData);
        pluginIndexConfigurationControl = new PluginIndexConfigurationControl(environmentData);
        permissionSchemesControl = new PermissionSchemesControlExt(environmentData);
        licenseRoleControl = new LicenseRoleControl(environmentData);
        licenseControl = new LicenseControl(environmentData);
    }

    public com.atlassian.jira.testkit.client.Backdoor getTestkit()
    {
        return testkit;
    }

    /**
     * @deprecated use {@link #restoreDataFromResource(String, String)} instead
     * @param fileName xml file name
     */
    @Deprecated
    public void restoreData(String fileName)
    {
        testkit.restoreData(fileName, LicenseKeys.V2_COMMERCIAL.getLicenseString());
    }

    public void restoreBlankInstance()
    {
        testkit.restoreBlankInstance(LicenseKeys.V2_COMMERCIAL.getLicenseString());
    }

    public void restoreBlankInstance(String license)
    {
        testkit.restoreBlankInstance(license);
    }

    public void restoreDataFromResource(String resource)
    {
        testkit.restoreDataFromResource(resource, LicenseKeys.V2_COMMERCIAL.getLicenseString());
    }

    @Deprecated
    public void restoreData(String xmlFileName, String license)
    {
        testkit.restoreData(xmlFileName, license);
    }

    public void restoreDataFromResource(String resourcePath, String license)
    {
        testkit.restoreDataFromResource(resourcePath, license);
    }

    public IssuesControl issueNavControl()
    {
        return issueNavControl;
    }

    public AttachmentsControl attachments() { return testkit.attachments(); }

    public ScreensControlExt screens() {return screensControl;}

    public UsersAndGroupsControl usersAndGroups() {return testkit.usersAndGroups();}

    public com.atlassian.jira.testkit.client.IssuesControl issues() {
        return testkit.issues();
    }

    public I18nControl i18n() {return testkit.i18n();}

    public DarkFeaturesControl darkFeatures() {return testkit.darkFeatures();}

    public PluginsControlExt plugins() {return pluginsControl;}

    public PermissionsControl permissions() {return permissionsControl;}

    public FiltersClient filters() {return filtersControl;}

    public ApplicationPropertiesControl applicationProperties() {return applicationPropertiesControl;}

    public EntityEngineControl entityEngine() { return entityEngineControl; }

    public SystemPropertiesControl systemProperties() {return testkit.systemProperties();}

    public ProjectControlExt project() {return projectControl;}

    public PermissionSchemesControl permissionSchemes() {return permissionSchemesControl;}

    public IssueTypeScreenSchemesControl issueTypeScreenSchemes() {return issueTypeScreenSchemes;}

    public ScreensControl screensControl() {return testkit.screensControl();}

    public MailServersControl mailServers() {return testkit.mailServers();}

    public SearchRequestControl searchRequests() {return testkit.searchRequests();}

    public UserProfileControl userProfile() {return userProfileControl;}

    public ServicesControl services() {return testkit.services();}

    public DataImportControl dataImport() {return testkit.dataImport();}

    public TestRunnerControl testRunner() {return testRunnerControl;}

    public FieldConfigurationControlExt fieldConfiguration() {return fieldConfigurationControl;}

    public IssueTypeControl issueType() {return testkit.issueType();}

    public SubtaskControl subtask() {return testkit.subtask();}

    public WebSudoControl websudo() {return testkit.websudo();}

    public DashboardControl dashboard() {return testkit.dashboard();}

    public BarrierControl barrier() {return barrierControl;}

    public IndexingControl indexing() {return indexingControl;}

    public GeneralConfigurationControl generalConfiguration() {return testkit.generalConfiguration();}

    public AdvancedSettingsControl advancedSettings() {return testkit.advancedSettings();}

    public CustomFieldsControl customFields() {return testkit.customFields();}

    public IssueLinkingControl issueLinking() {return testkit.issueLinking();}

    public WorkflowsControlExt workflow() {return workflowsControlExt;}

    public SearchClient search() {return testkit.search();}

    public WorkflowSchemesControl workflowSchemes() {return testkit.workflowSchemes();}

    public ProjectRoleClient projectRole() { return testkit.projectRole(); }

    public FilterSubscriptionControl filterSubscriptions() { return filterSubscriptionControl; }

    public ManagedConfigurationControl managedConfiguration() { return managedConfigurationControl; }

    public IssueTableClient issueTableClient() { return issueTableClient; }

    public SearchersClient searchersClient() { return searchersClient; }

    public ColumnControl columnControl() { return columnControl; }

    public VersionClient versions() { return versionClient; }

    public ComponentClient components() { return componentClient; }

    public PluginIndexConfigurationControl getPluginIndexConfigurationControl()
    {
        return pluginIndexConfigurationControl;
    }

    public LicenseRoleControl licenseRoles()
    {
        return licenseRoleControl;
    }

    public LicenseControl license()
    {
        return licenseControl;
    }
}
