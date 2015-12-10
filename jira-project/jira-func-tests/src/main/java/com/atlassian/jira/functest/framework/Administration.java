package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.admin.AdminTabs;
import com.atlassian.jira.functest.framework.admin.AdvancedApplicationProperties;
import com.atlassian.jira.functest.framework.admin.Attachments;
import com.atlassian.jira.functest.framework.admin.CustomFields;
import com.atlassian.jira.functest.framework.admin.FieldConfigurationSchemes;
import com.atlassian.jira.functest.framework.admin.FieldConfigurations;
import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.admin.IssueLinking;
import com.atlassian.jira.functest.framework.admin.IssueSecuritySchemes;
import com.atlassian.jira.functest.framework.admin.MailServerAdministration;
import com.atlassian.jira.functest.framework.admin.NotificationSchemes;
import com.atlassian.jira.functest.framework.admin.PermissionSchemes;
import com.atlassian.jira.functest.framework.admin.Project;
import com.atlassian.jira.functest.framework.admin.ProjectImport;
import com.atlassian.jira.functest.framework.admin.Resolutions;
import com.atlassian.jira.functest.framework.admin.Roles;
import com.atlassian.jira.functest.framework.admin.SendBulkMail;
import com.atlassian.jira.functest.framework.admin.Subtasks;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.admin.UsersAndGroups;
import com.atlassian.jira.functest.framework.admin.ViewFieldScreens;
import com.atlassian.jira.functest.framework.admin.ViewServices;
import com.atlassian.jira.functest.framework.admin.ViewWorkflows;
import com.atlassian.jira.functest.framework.admin.plugins.Plugins;
import com.atlassian.jira.functest.framework.admin.user.shared.SharedDashboardsAdministration;
import com.atlassian.jira.functest.framework.admin.user.shared.SharedFiltersAdministration;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.webtests.LicenseKeys;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Provides methods for carring out administration tasks in JIRA.
 *
 * @since v3.13
 */
public interface Administration
{
    String ENTERPRISE = "Enterprise";
    String PROFESSIONAL = "Professional";
    String STANDARD = "Standard";

    /**
     * Reindex JIRA, waiting for JIRA to complete the task, in the default index directory.
     */
    void reIndex();

    /**
     * Set JIRA's profiling on or off.
     *
     * @param on or off
     */
    void setProfiling(boolean on);

    /**
     * Restores the specified fileName as  JIRA data.
     * Does not make english assertions.
     *
     * @param fileName the fiel conatining the JIRA data
     */
    void restoreI18nData(String fileName);

    /**
     * Basically check the passed backup to ensure that it matches the passed build number and then restore
     * the data. An exception will be thrown if the backup does not match the passed build number.
     *
     * @param fileName the path of the backup.
     * @param expectedBuilderNumber the build number the data should have.
     */
    void restoreDataWithBuildNumber(String fileName, int expectedBuilderNumber);

    /**
     * Same as {@link #restoreData(String, boolean)}, but with useDefaultPaths set to false.
     */
    void restoreData(String fileName);

    /**
     * Restores the specified fileName as JIRA data.
     *
     * @param fileName        the XML file inside the standard backup file directory containing the JIRA data
     * @param useDefaultPaths if set to true, the xml backup's paths (e.g., index, attachment etc) will be ignored, and the
     *                        default for those used in stead
     */
    void restoreData(String fileName, boolean useDefaultPaths);

    /**
     * same as {@link #restoreDataAndLogin(String, String, boolean)} but with userDefaultPaths parameter set to false.
     */
    void restoreDataAndLogin(final String fileName, String username);

    /**
     * Restore the specified file and login to JIRA using the passed username.
     *
     * @param fileName        the name of the xml file to restore.
     * @param username        the username used to login to JIRA after the restore. The username and the password are assumed to
     *                        be the same.
     * @param useDefaultPaths if set to true, the xml backup's paths (e.g., index, attachment etc) will be ignored, and the
     *                        default for those used in stead
     */
    void restoreDataAndLogin(final String fileName, final String username, boolean useDefaultPaths);

    /**
     * Same as {@link #restoreDataSlowOldWayAndLogin(String, String, boolean)} but with useDefaultPath set to false.
     */
    void restoreDataSlowOldWayAndLogin(final String fileName, String username);

    /**
     * Restore the specified file  data NOT using the clear cache mechanism and login to JIRA using the passed username. This is
     * much slower than using the default methods and should only be used if there is a real reason for it. If you find yourself
     * using this method perhaps we should be fixing the cache clearing stuff instead.
     *
     * @param fileName       the name of the xml file to restore.
     * @param username       the username used to login to JIRA after the restore. The username and the password are assumed to be
     *                       the same.
     * @param useDefaultPath if set to true, the paths in the xml backup (e.g., index, and attachment) will be ignored, and their
     *                       default paths used instead.
     */
    void restoreDataSlowOldWayAndLogin(String fileName, String username, boolean useDefaultPath);


    /**
     * Same as {@link #restoreDataSlowOldWay(String, boolean)}, except with useDefaultPaths set to false.
     */
    void restoreDataSlowOldWay(String fileName);

    /**
     * Restores the specified fileName as JIRA data NOT using the clear cache mechanism. This is much slower than using the
     * default methods and should only be used if there is a real reason for it. If you find yourself using this method perhaps we
     * should be fixing the cache clearing stuff instead.
     *
     * @param fileName        the XML file inside the standard backup file directory containing the JIRA data
     * @param useDefaultPaths if set to true, the paths in the xml backup (e.g., index, and attachment) will be ignored, and their
     *                        default paths used instead.
     */
    void restoreDataSlowOldWay(String fileName, boolean useDefaultPaths);

    /**
     * Same as {@link #restoreDataWithPluginsReload(String, boolean)}, except with useDefaultPaths set to false.
     */
    void restoreDataWithPluginsReload(String fileName);

    /**
     * <p>Restores the specified fileName as JIRA data NOT using the clear cache mechanism. The plugins system is restarted.</p>
     * <p/>
     * <p>This restore uses the full Pico refresh the same as in Production. ie it does not do a "Quick Import".</p>
     * <p/>
     * <p>This is much slower than using the default methods and should only be used if there is a real reason for it.</p>
     *
     * @param fileName        the XML file inside the standard backup file directory containing the JIRA data
     * @param useDefaultPaths if set to true, the paths in the xml backup (e.g., index, and attachment) will be ignored, and their
     *                        default paths used instead.
     */
    void restoreDataWithPluginsReload(String fileName, boolean useDefaultPaths);

    /**
     * Same as {@link #restoreDataSlowOldWay(String, String, boolean)}, but with useDefaultPath set to false;
     */
    void restoreDataSlowOldWay(final String path, final String fileName);

    /**
     * Restores the XML file from the specified directory NOT using the clear cache mechanism. This is much slower than using the
     * default methods and should only be used if there is a real reason for it. If you find yourself using this method perhaps we
     * should be fixing the cache clearing stuff instead.
     *
     * @param path           the directory in which the XML file is located
     * @param fileName       the name of the XML file
     * @param useDefaultPath if set to true, the paths (e.g., attachment, index) in the xml backup will be ignored, and the
     *                       default used instead
     */
    void restoreDataSlowOldWay(final String path, final String fileName, boolean useDefaultPath);

    /**
     * Same as {@link #restoreDataWithReplacedTokens(String, java.util.Map, boolean)}, except with useDefaultPaths set to false.
     */
    void restoreDataWithReplacedTokens(String originalXmlFileName, Map<String, String> replacements) throws IOException;

    void setSafeMode(boolean safeModeIsOn);

    /**
     * Reads in an XML file and performs token replacements on it, writes the data to a temp file, then imports that into JIRA.
     * <p/>
     * Note: this only works when the FuncTest client is running on the same host as the JIRA server.
     * <p/>
     * For methods to create date strings to use as your tokens, check out the {@link com.atlassian.jira.functest.framework.util.date.DateUtil}
     * class
     *
     * @param originalXmlFileName the name of the XML file to read in; must be located in the standard XML file directory
     * @param replacements        a map of token replacements
     * @param useDefaultPaths     if set to true, the paths (e.g., attachment, index) in the xml backup will be ignored, and the
     *                            default used instead. If the token being replaced is part of the default paths, it is still
     *                            replaced prior to restore.
     * @throws IOException if there is a problem reading/writing the files
     */
    void restoreDataWithReplacedTokens(String originalXmlFileName, Map<String, String> replacements, boolean useDefaultPaths) throws IOException;

    /**
     * Reads in an XML file and performs token replacements on it, writes the data to a temp file which is the return value
     *
     * @param originalXmlFileName the name of the XML file to read in; must be located in the standard XML file directory
     * @param replacements a map of token replacements
     * @throws IOException if there is a problem reading/writing the files
     * @return a new temporary file that has its tokens replaced
     */
    File replaceTokensInFile(final String originalXmlFileName, final Map<String, String> replacements) throws IOException;

    /**
     * same as {@link #restoreDataWithLicense(String, String, boolean)}, except with useDefaultPaths set to false.
     */
    void restoreDataWithLicense(String fileName, String licenseKey);

    /**
     * Restores the specified fileName as JIRA data, using the specified License key on import.
     *
     * @param fileName        the file conatining the JIRA data
     * @param licenseKey      the license key to be installed
     * @param useDefaultPaths if set to true, the paths (e.g., attachment, index) in the xml backup will be ignored, and the
     *                        default used instead
     */
    void restoreDataWithLicense(String fileName, String licenseKey, boolean useDefaultPaths);

    /**
     * Exports the current running data to the specified absolute path
     *
     * @param fileName to backup to. This should just be a filename because JIRA always appends ${JIRA.HOME}/export
     *  to it before doing the export. Passing a directory may make it fail on windows.
     * @return export file. it will be absolute.
     */
    File exportDataToFile(String fileName);

    /**
     * Restores a JIRA to a well known blank instance
     */
    void restoreBlankInstance();

    /**
     * Restores a JIRA to a well known blank instance with the supplied licence.
     *
     * @param license the license to be installed
     */
    void restoreBlankInstanceWithLicense(LicenseKeys.License license);

    /**
     * Restores a backup file with JIRA in an not setup state.
     *
     * If you use this method, you will have to then set up JIRA from scratch!
     */
    void restoreNotSetupInstance();

    /**
     * Obtains the current attachment path configured for JIRA.
     *
     * <p> This method does not check whether Attachments are enabled or not.
     * If JIRA is configured to use the "default" attachment path, then this method still returns the ACTUAL path that is used.
     *
     * <p> The implementation navigates to the Admin Attachments Settings page and screenscrapes, so don't expect to remain on the same page as when you called the method.
     *
     * @return the current attachment path configured for JIRA.
     *
     */
    String getCurrentAttachmentPath();

    /**
     * @deprecated use {@link com.atlassian.jira.functest.framework.admin.Subtasks#enable()} instead.
     */
    @Deprecated
    void activateSubTasks();

    void addSubTaskType(String name);

    /**
     * Remove a group from a given Global Permission.
     * Ends up on the Global Permissions page.
     *
     * @param permission The permission to remove teh group from
     * @param group      the group to remove
     *
     * @deprecated Use {@link #removeGlobalPermission(com.atlassian.jira.permission.GlobalPermissionKey, String)} instead. Since v6.2.5.
     */
    void removeGlobalPermission(final int permission, final String group);

    /**
     * Remove a group from a given Global Permission.
     * Ends up on the Global Permissions page.
     *
     * @param permission The permission to remove the group from
     * @param group      the group to remove
     */
    void removeGlobalPermission(final GlobalPermissionKey permission, final String group);

    /**
     * Add a group to a Global permission
     * Ends up on the Global Permissions page.
     *
     * @param permission the permission to add the group to
     * @param group      The group to add
     *
     * @deprecated Use {@link #addGlobalPermission(com.atlassian.jira.permission.GlobalPermissionKey, String)} instead. Since v6.2.5.
     */
    void addGlobalPermission(final int permission, final String group);

    /**
     * Add a group to a Global permission
     * Ends up on the Global Permissions page.
     *
     * @param permission the permission to add the group to
     * @param group      The group to add
     */
    void addGlobalPermission(final GlobalPermissionKey permission, final String group);

    /**
     * Changes JIRA's lincense to a given license object
     *
     * @param license     license to switch to
     */
    void switchToLicense(final LicenseKeys.License license);

    /**
     * Changes JIRA's lincense to a given license and asserts that the description then appears on the view license
     *
     * @param license     license to switch to
     * @param description license description
     */
    void switchToLicense(String license, String description);

    /**
     * Switches the license to a personal license.
     */
    void switchToPersonalLicense();

    /**
     * Switches the license to a starter license.
     */
    void switchToStarterLicense();

    /**
     * Returns the JIRA home directory as reported in the System Info section
     *
     * @return the JIRA home directory as reported in the System Info section
     */
    String getJiraHomeDirectory();

    /**
     * Returns the current edition name.  This may be useful for licensing checks
     *
     * @return the current edition name
     * @see #ENTERPRISE
     * @see #PROFESSIONAL
     * @see #STANDARD
     */
    String getEdition();

    /**
     * Returns ths current build number. A {@link RuntimeException} will be thrown if we can't find the build number.
     *
     * @return JIRA's build number.
     */
    long getBuildNumber();

    /**
     * Allows you to perform generalConfiguration actions.
     *
     * @return generalConfiguration
     */
    GeneralConfiguration generalConfiguration();

    /**
     * Allows you to perform backdoor actions
     *
     * @return backdoor
     */
    Backdoor backdoor();

    /**
     * Allows you to perform project actions.
     *
     * @return project
     */
    Project project();

    /**
     * Allows you to perform user and group actions.
     *
     * @return usersAndGroups
     */
    UsersAndGroups usersAndGroups();

    /**
     * Allows you to perform role actions
     *
     * @return roles
     */
    Roles roles();

    /**
     * Allows you to perform custom field actions.
     *
     * @return customFields
     */
    CustomFields customFields();

    /**
     * Allows you to modify permission schemes.
     *
     * @return permissionSchemes
     */
    PermissionSchemes permissionSchemes();

    /**
     * Allows you to modify issue security schemes.
     *
     * @return issue security schemes of this administration
     */
    IssueSecuritySchemes issueSecuritySchemes();

    /**
     * Allows you to modify field configurations.
     *
     * @return fieldConfigurations
     */
    FieldConfigurations fieldConfigurations();

    /**
     * Allows you to modify field configuration schemes.
     *
     * @return fieldConfigurationSchemes
     */
    FieldConfigurationSchemes fieldConfigurationSchemes();

    /**
     * Allows you to perform project imports.
     *
     * @return projectImport
     */
    ProjectImport projectImport();

    /**
     * Utility function to allow you some basic plugins control
     * @return plugins
     */
    Plugins plugins();

    /**
     * Given a jelly script, this call will navigate to the admin section in JIRA and execute the script.
     *
     * @param script The script to execute.
     */
    void runJellyScript(String script);

    /**
     * enable http access logging
     */
    void enableAccessLogging();

    /**
     * @return attachments
     */
    Attachments attachments();

    /**
     * @return utilities
     */
    Utilities utilities();

    /**
     * @return grouping to manage administration of subtasks
     */
    Subtasks subtasks();

    /**
     * TGo to issue linking administration section.
     *
     * @return grouping to manage issue linking
     */
    IssueLinking issueLinking();

    /**
     * Get time tracking administration section util.
     *
     * @return time tracking configuration
     */
    TimeTracking timeTracking();

    /**
     * @return resolution configuration
     */
    Resolutions resolutions();

    /**
     * Returns the ViewServices object which allows you to run operations on the ViewServices page.
     * @return the ViewServices object which allows you to run operations on the ViewServices page.
     */
    ViewServices services();


    /**
     * Field screens config.
     *
     * @return field screens config
     */
    ViewFieldScreens viewFieldScreens();


    /**
     * Manage the 'Workflows' administration page
     *
     * @return {@link com.atlassian.jira.functest.framework.admin.ViewWorkflows} instance
     */
    ViewWorkflows workflows();

    /**
     * 'Notification Schemes' administration section
     *
     * @return {@link com.atlassian.jira.functest.framework.admin.NotificationSchemes} instance
     */
    NotificationSchemes notificationSchemes();

    /**
     * Get the system tenant home directory.  This does the same thing as getJiraHomeDirectory, except it doesn't cache
     * it in a thread local.  It assumes that the environment data for this administration object is for the system
     * tenant.  It should only be called when a tenant is provisioned, which should only be done on the first test to
     * that tenant.
     *
     * @return The system tenant home directory.
     */
    String getSystemTenantHomeDirectory();

    /**
     * Gets an instance of the Mail Server Administration page.
     * @return an instance of the Mail Server Administration page.
     */
    MailServerAdministration mailServers();

    /**
     * Gets an instance of the Shared Filters Administration page.
     * @return an instance of the Shared Filters Administration page.
     */
    SharedFiltersAdministration sharedFilters();

    /**
     * Gets an instance of the Send Bulk Mail Page.
     * @return an instance of the Send Bulk Mail Page.
     */
    SendBulkMail sendBulkMail();

    /**
     * Gets an instance of the AdminTabs Page Object.
     *
     * @return an instance of the AdminTabs Page Object.
     */
    AdminTabs tabs();

    /**
     * Gets an instance of the AdvancedApplicationProperties Page Object.
     *
     * @return an instance of the AdvancedApplicationProperties Page Object.
     */
    AdvancedApplicationProperties advancedApplicationProperties();

    /**
     * Waits for the data import progress to complete.
     */
    void waitForRestore();


    SharedDashboardsAdministration sharedDashboards();

    void restoreData(String fileName, OutgoingMailSettings outgoingMailSetting);

    /**
     * Retrieves the administration home link in the JIRA Header.
     *
     * @return the administration home link in the JIRA Header.
     */
    Link link();

    /**
     * Represents the administration home link in the JIRA Header.
     */
    public static interface Link
    {
        boolean isPresent();
    }

    /**
     * Sub interface that contains... utilities?:P
     */
    public interface Utilities {
        /**
         * Run the specified service immediately and wait until it is complete.
         *
         * @param serviceId for the required service
         */
        void runServiceNow(long serviceId);
    }

    public enum OutgoingMailSettings
    {
        ENABLE("true"), DISABLE("false");

        private final String value;

        OutgoingMailSettings(String value)
        {
            this.value = value;
        }

        public String asString()
        {
            return value;
        }
    }
}
