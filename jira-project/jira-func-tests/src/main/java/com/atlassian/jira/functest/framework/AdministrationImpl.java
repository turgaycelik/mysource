package com.atlassian.jira.functest.framework;

import com.atlassian.jira.functest.framework.admin.AdminTabs;
import com.atlassian.jira.functest.framework.admin.AdminTabsImpl;
import com.atlassian.jira.functest.framework.admin.AdvancedApplicationProperties;
import com.atlassian.jira.functest.framework.admin.AdvancedApplicationPropertiesImpl;
import com.atlassian.jira.functest.framework.admin.Attachments;
import com.atlassian.jira.functest.framework.admin.AttachmentsImpl;
import com.atlassian.jira.functest.framework.admin.CustomFields;
import com.atlassian.jira.functest.framework.admin.CustomFieldsImpl;
import com.atlassian.jira.functest.framework.admin.DefaultMailServerAdministration;
import com.atlassian.jira.functest.framework.admin.FieldConfigurationSchemes;
import com.atlassian.jira.functest.framework.admin.FieldConfigurationSchemesImpl;
import com.atlassian.jira.functest.framework.admin.FieldConfigurations;
import com.atlassian.jira.functest.framework.admin.FieldConfigurationsImpl;
import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.admin.GeneralConfigurationImpl;
import com.atlassian.jira.functest.framework.admin.IssueLinking;
import com.atlassian.jira.functest.framework.admin.IssueLinkingImpl;
import com.atlassian.jira.functest.framework.admin.IssueSecuritySchemes;
import com.atlassian.jira.functest.framework.admin.IssueSecuritySchemesImpl;
import com.atlassian.jira.functest.framework.admin.MailServerAdministration;
import com.atlassian.jira.functest.framework.admin.NotificationSchemes;
import com.atlassian.jira.functest.framework.admin.NotificationSchemesImpl;
import com.atlassian.jira.functest.framework.admin.PermissionSchemes;
import com.atlassian.jira.functest.framework.admin.PermissionSchemesImpl;
import com.atlassian.jira.functest.framework.admin.Project;
import com.atlassian.jira.functest.framework.admin.ProjectImpl;
import com.atlassian.jira.functest.framework.admin.ProjectImport;
import com.atlassian.jira.functest.framework.admin.ProjectImportImpl;
import com.atlassian.jira.functest.framework.admin.Resolutions;
import com.atlassian.jira.functest.framework.admin.ResolutionsImpl;
import com.atlassian.jira.functest.framework.admin.Roles;
import com.atlassian.jira.functest.framework.admin.RolesImpl;
import com.atlassian.jira.functest.framework.admin.SendBulkMail;
import com.atlassian.jira.functest.framework.admin.Subtasks;
import com.atlassian.jira.functest.framework.admin.SubtasksImpl;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.admin.TimeTrackingImpl;
import com.atlassian.jira.functest.framework.admin.UsersAndGroups;
import com.atlassian.jira.functest.framework.admin.UsersAndGroupsImpl;
import com.atlassian.jira.functest.framework.admin.ViewFieldScreens;
import com.atlassian.jira.functest.framework.admin.ViewFieldScreensImpl;
import com.atlassian.jira.functest.framework.admin.ViewServices;
import com.atlassian.jira.functest.framework.admin.ViewWorkflows;
import com.atlassian.jira.functest.framework.admin.ViewWorkflowsImpl;
import com.atlassian.jira.functest.framework.admin.plugins.Plugins;
import com.atlassian.jira.functest.framework.admin.plugins.PluginsImpl;
import com.atlassian.jira.functest.framework.admin.user.shared.DefaultSharedDashboardsAdministration;
import com.atlassian.jira.functest.framework.admin.user.shared.DefaultSharedFiltersAdministration;
import com.atlassian.jira.functest.framework.admin.user.shared.SharedDashboardsAdministration;
import com.atlassian.jira.functest.framework.admin.user.shared.SharedFiltersAdministration;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.util.AsynchronousTasks;
import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.testkit.client.dump.FuncTestTimer;
import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.testkit.client.xmlbackup.XmlBackupCopier;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.base.Preconditions;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequestSource;
import com.meterware.httpunit.WebTable;
import com.sun.jersey.api.client.UniformInterfaceException;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.jira.functest.matcher.BuildNumberMatcher.hasBuildNumber;
import static com.atlassian.jira.webtests.LicenseKeys.V2_COMMERCIAL;
import static java.lang.String.valueOf;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertThat;

/**
 * Implementation of {@link com.atlassian.jira.functest.framework.Administration}
 *
 * @since v3.13
 */
public class AdministrationImpl extends AbstractFuncTestUtil implements Administration, FuncTestLogger
{
    /**
     * The name of the import directory.
     */
    private static final String IMPORT_DIR = "import";

    private static final Pattern PATTERN_BUILD_NUMBER = Pattern.compile("#(\\d+)");

    private final XmlBackupCopier xmlBackupCopier;
    private final Navigation navigation;
    private AsynchronousTasks asynchronousTasks;
    private final TextAssertions text;
    private final GeneralConfiguration generalConfiguration;
    private final Project project;
    private final UsersAndGroups usersAndGroups;
    private final Roles roles;
    private final CustomFields customFields;
    private final PermissionSchemes permissionSchemes;
    private final IssueSecuritySchemes issueSecuritySchemes;
    private final FieldConfigurations fieldConfigurations;
    private final FieldConfigurationSchemes fieldConfigurationSchemes;
    private final ResolutionsImpl resolutions;
    private final ViewServices viewServices;
    private final ProjectImport projectImport;
    private final Attachments attachments;
    private final Plugins plugins;
    private final ViewFieldScreensImpl viewFieldScreens;
    private final ViewWorkflows workflows;
    private final MailServerAdministration mailServerAdministration;
    private final SharedFiltersAdministration sharedFiltersAdministration;
    private final SharedDashboardsAdministration sharedDashboardsAdministration;
    private final NotificationSchemes notificationSchemes;
    private final SendBulkMail sendBulkMail;
    private final AdminTabs adminTabs;
    private final AdvancedApplicationProperties advancedApplicationProperties;
    private final Backdoor backdoor;

    private static Set<String> copiedFiles = Collections.synchronizedSet(new HashSet<String>());
    /**
     * Evil but necessary static field used for caching the JIRA_HOME during func test runs.
     */
    private static final ThreadLocal<String> JIRA_HOME_DIR = new ThreadLocal<String>();
    private static boolean backdoorIsPresent = true;
    private boolean safeMode = false;


    /**
     * Note: if you need to construct this for an old-style {@link com.atlassian.jira.webtests.JIRAWebTest}, you may
     * want to consider using {@link com.atlassian.jira.functest.framework.FuncTestHelperFactory} instead.
     *
     * @param tester the tester
     * @param environmentData the environment data
     * @param navigation the navigation
     * @param assertions the assertions
     * @see FuncTestHelperFactory#getAdministration()
     */
    public AdministrationImpl(final WebTester tester, final JIRAEnvironmentData environmentData,
            final Navigation navigation, final Assertions assertions)
    {
        super(tester, environmentData, 2);
        this.backdoor = new Backdoor(environmentData);
        this.xmlBackupCopier = new XmlBackupCopier(environmentData.getBaseUrl());
        this.navigation = navigation;
        this.asynchronousTasks = new AsynchronousTasks(tester, environmentData, childLogIndentLevel());
        this.text = assertions.getTextAssertions();
        this.generalConfiguration = new GeneralConfigurationImpl(tester, environmentData);
        this.project = new ProjectImpl(backdoor, tester, environmentData, navigation, assertions, asynchronousTasks);
        this.usersAndGroups = new UsersAndGroupsImpl(tester, navigation, assertions.getTextAssertions(), locators);
        this.roles = new RolesImpl(tester, environmentData, 3);
        this.customFields = new CustomFieldsImpl(tester, environmentData, navigation, getFuncTestHelperFactory().getForm());
        this.permissionSchemes = new PermissionSchemesImpl(tester, environmentData);
        this.issueSecuritySchemes = new IssueSecuritySchemesImpl(tester, environmentData);
        this.fieldConfigurations = new FieldConfigurationsImpl(tester, environmentData);
        this.fieldConfigurationSchemes = new FieldConfigurationSchemesImpl(tester, environmentData);
        this.resolutions = new ResolutionsImpl(tester, environmentData);
        this.viewServices = new ViewServices(tester, navigation);
        this.projectImport = new ProjectImportImpl(tester, environmentData, navigation, this);
        this.attachments = new AttachmentsImpl(tester, environmentData, navigation);
        this.viewFieldScreens = new ViewFieldScreensImpl(tester, environmentData, navigation);
        this.workflows = new ViewWorkflowsImpl(tester, environmentData, childLogIndentLevel(), navigation);
        this.plugins = new PluginsImpl(tester, environmentData, logIndentLevel, navigation, this, locators);
        this.mailServerAdministration = new DefaultMailServerAdministration(tester, navigation, locators);
        this.sharedFiltersAdministration = new DefaultSharedFiltersAdministration(tester, navigation, locators);
        this.sharedDashboardsAdministration = new DefaultSharedDashboardsAdministration(tester, navigation, locators);
        this.sendBulkMail = new DefaultSendBulkMail(navigation);
        this.notificationSchemes = new NotificationSchemesImpl(tester, environmentData, childLogIndentLevel());
        this.adminTabs = new AdminTabsImpl(tester, environmentData);
        this.advancedApplicationProperties = new AdvancedApplicationPropertiesImpl(tester, environmentData);
    }

    public void reIndex()
    {
        tester.gotoPage("secure/admin/jira/IndexAdmin.jspa");
        tester.submit("reindex");

        waitForIndexCompletion(1000, 100);
    }

    public void setProfiling(final boolean on)
    {
    }

    /**
     * Restores the jira instance to one with no issues. Some projects have been created
     */
    public void restoreBlankInstance()
    {
        restoreBlankInstanceWithLicense(V2_COMMERCIAL);
    }

    public void restoreBlankInstanceWithLicense(LicenseKeys.License license)
    {
        restoreDataWithLicense("blankprojects.xml", license.getLicenseString());
    }

    public void restoreNotSetupInstance()
    {
        final File file = new File(environmentData.getXMLDataLocation(), "TestEmpty.xml");
        copyFileToJiraImportDirectory(file);

        tester.gotoPage(getRestoreUrl());
        tester.setWorkingForm("restore-xml-data-backup");
        tester.setFormElement("license", V2_COMMERCIAL.getLicenseString());
        tester.setFormElement("filename", file.getName());
        tester.checkCheckbox("quickImport", "true");

        tester.submit();
        waitForRestore();

        //should go straight to the Setup screen after the import!
        tester.assertTextPresent("JIRA Setup");
    }

    private void copyFileToJiraImportDirectory(File file)
    {
        String filename = file.getName();

        if (!copiedFiles.contains(filename))
        {
            File jiraImportDirectory = new File(getJiraHomeDirectory(), IMPORT_DIR);
            try
            {
                FileUtils.copyFileToDirectory(file, jiraImportDirectory);
                copiedFiles.add(filename);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not copy file " + file.getAbsolutePath() + " to the import directory in jira home " + jiraImportDirectory, e);
            }
        }
    }

    public File replaceTokensInFile(final String originalXmlFileName, final Map<String, String> replacements)
            throws IOException
    {
        final String resource = environmentData.getXMLDataLocation().getAbsolutePath() + "/" + originalXmlFileName;
        String xml = IOUtils.toString(new FileReader(resource));
        xml = replaceTokens(xml, replacements);
        // write new data to temp file
        File newData = File.createTempFile(originalXmlFileName, ".xml");
        final FileWriter of = new FileWriter(newData);
        of.write(xml);
        of.close();

        return newData;
    }

    /**
     * Turn on safe mode - ignores dangermode.
     */
    @Override
    public void setSafeMode(final boolean safeModeIsOn)
    {
        this.safeMode = safeModeIsOn;
    }

    public void restoreDataWithReplacedTokens(final String originalXmlFileName, final Map<String, String> replacements) throws IOException
    {
        restoreDataWithReplacedTokens(originalXmlFileName, replacements, false);
    }

    @Override
    public void restoreDataWithReplacedTokens(String originalXmlFileName, Map<String, String> replacements, boolean useDefaultPaths) throws IOException
    {
        final String resource = environmentData.getXMLDataLocation().getAbsolutePath() + "/" + originalXmlFileName;
        String xml = IOUtils.toString(new FileReader(resource));
        xml = replaceTokens(xml, replacements);
        File newData = null;
        try
        {
            newData = replaceTokensInFile(originalXmlFileName, replacements);
            restoreData(newData.getParent(), newData.getName(), useDefaultPaths);
        }
        finally
        {
            if (newData.exists())
            {
                assert newData.delete();
            }
        }
    }

    public void restoreData(final String fileName)
    {
        if (backdoorIsPresent)
        {
            try
            {
                backdoor.restoreData(fileName);
                // Retain parity with restoreData behaviour
                navigation.login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
            }
            catch (UniformInterfaceException uie)
            {
                log("Backdoor was not present");
                log(uie);
                backdoorIsPresent = false;
                restoreData(fileName, false);
            }
        }
        else
        {
            restoreData(fileName, false);
        }
    }

    @Override
    public void restoreData(String fileName, boolean useDefaultPaths)
    {
        restoreData(environmentData.getXMLDataLocation().getAbsolutePath(), fileName, useDefaultPaths);
    }

    @Override
    public void restoreData(final String fileName, final OutgoingMailSettings outgoingMailSetting)
    {
        restoreData
                (
                        environmentData.getXMLDataLocation().getAbsolutePath(), fileName, true,
                        FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD, false,
                        V2_COMMERCIAL.getLicenseString(), outgoingMailSetting
                );
    }

    @Override
    public Link link()
    {
        return new DefaultLink(tester);
    }

    @Override
    public void restoreDataAndLogin(final String fileName, final String username)
    {
        restoreDataAndLogin(fileName, username, false);
    }

    @Override
    public void restoreDataAndLogin(final String fileName, final String username, boolean useDefaultPaths)
    {
        restoreData(environmentData.getXMLDataLocation().getAbsolutePath(), fileName, true, username, username, useDefaultPaths);
    }

    @Override
    public void restoreDataSlowOldWayAndLogin(String fileName, String username)
    {
        restoreDataSlowOldWayAndLogin(fileName, username, false);
    }

    @Override
    public void restoreDataSlowOldWayAndLogin(String fileName, String username, boolean useDefaultPath)
    {
        restoreData(environmentData.getXMLDataLocation().getAbsolutePath(), fileName, false, FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD, useDefaultPath);
    }

    public void restoreDataSlowOldWay(final String fileName)
    {
        restoreDataSlowOldWay(fileName, false);
    }

    @Override
    public void restoreDataSlowOldWay(String fileName, boolean useDefaultPaths)
    {
        restoreDataSlowOldWay(environmentData.getXMLDataLocation().getAbsolutePath(), fileName, useDefaultPaths);
    }

    @Override
    public void restoreDataWithPluginsReload(String fileName)
    {
        restoreDataWithPluginsReload(fileName, false);
    }

    @Override
    public void restoreDataWithPluginsReload(String fileName, boolean useDefaultPaths) {
        restoreDataSlowOldWay(environmentData.getXMLDataLocation().getAbsolutePath(), fileName, useDefaultPaths);
    }

    public void restoreData(final String path, final String fileName)
    {
        restoreData(path, fileName, false);
    }

    public void restoreData(final String path, final String fileName, boolean useDefaultPath)
    {
        restoreData(path, fileName, true, FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD, useDefaultPath);
    }

    public void restoreDataSlowOldWay(final String path, final String fileName)
    {
        restoreDataSlowOldWay(path, fileName, false);
    }

    public void restoreDataSlowOldWay(final String path, final String fileName, boolean useDefaultPath)
    {
        restoreData(path, fileName, false, FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD, useDefaultPath);
    }

    private void restoreData(final String path, final String fileName, final boolean clearCache,
                             final String username, final String password, final boolean useDefaultPaths)
    {
        restoreData(path, fileName, clearCache, username, password, useDefaultPaths, V2_COMMERCIAL.getLicenseString());
    }

    @Override
    public void restoreDataWithLicense(String fileName, String licenseKey, boolean useDefaultPaths)
    {
        restoreData(environmentData.getXMLDataLocation().getAbsolutePath(), fileName, true, FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD, useDefaultPaths, licenseKey);
    }

    public void restoreDataWithLicense(final String fileName, final String licenseKey)
    {
        restoreDataWithLicense(fileName, licenseKey, false);
    }

    private void restoreData(final String path, final String fileName, final boolean clearCache, final String username,
                             final String password, final boolean useDefaultPaths, final String licenseString)
    {
        navigation.gotoAdminSection("system_info");
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Restore");
        final String sourcePath = path + FS + fileName;
        final String jiraImportDir = getJiraHomeDirectory() + FS + IMPORT_DIR + FS + new File(fileName).getName();

        boolean baseUrlReplaced = xmlBackupCopier.copyXmlBackupTo(sourcePath, jiraImportDir);

        log("Restoring data '" + jiraImportDir + "'");
        tester.gotoPage(getRestoreUrl());
        tester.setWorkingForm("restore-xml-data-backup");
        tester.setFormElement("filename", fileName);
        tester.setFormElement("license", licenseString);
        if (useDefaultPaths)
        {
            reflectivelySetDefaultPaths(tester.getDialog().getForm());
        }
        if (clearCache)
        {
            tester.checkCheckbox("quickImport", "true");
        }
        tester.submit();
        waitForRestore();

        if (!isRestoreSuccessful())
        {
            //The following are assertions of possible error messages to display the cause of failure to import
            //instead of having to check HTML dump manually. Please add/modify new error messages not included already.
            assertCauseOfError("The xml data you are trying to import seems to be from a newer version of JIRA. This will not work.", jiraImportDir);
            assertCauseOfError("You must enter the location of an XML file.", jiraImportDir);
            assertCauseOfError("Could not find file at this location.", jiraImportDir);
            assertCauseOfError("Invalid license key specified.", jiraImportDir);
            assertCauseOfError("The current license is too old to install this version of JIRA", jiraImportDir);
            assertCauseOfError("Invalid license type for this version of JIRA. License should be of type Standard.", jiraImportDir);
            assertCauseOfError("Invalid license type for this version of JIRA. License should be of type Professional.", jiraImportDir);
            assertCauseOfError("Invalid license type for this version of JIRA. License should be of type Enterprise.", jiraImportDir);
            assertCauseOfError("You must specify an index for the restore process.", jiraImportDir);
            assertCauseOfError("Error parsing export file. Your export file is invalid.", jiraImportDir);
            assertCauseOfError("specified in the backup file is not valid", jiraImportDir);
            throw new AssertionError("Failed to restore JIRA data from [" + jiraImportDir + "]. See logs for details.");
        }

        navigation.disableWebSudo();
        navigation.login(username, password);

        final long howLong =  timer.end();
        log("Restored '" + fileName + "' in " + (howLong) + "ms");

        if (!baseUrlReplaced)
        {
            generalConfiguration.setBaseUrl(getEnvironmentData().getBaseUrl().toString());
        }
        tester.beginAt("/");
    }

    private String getRestoreUrl()
    {
        final String safeModeParam = safeMode ? "?safemode=true" : "";
        return "secure/admin/XmlRestore!default.jspa" + safeModeParam;
    }

    private void restoreData(final String path, final String fileName, final boolean clearCache, final String username,
            final String password, final boolean useDefaultPaths, final String licenseString, final OutgoingMailSettings outgoingMailSetting)
    {
        navigation.gotoAdminSection("system_info");
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Restore");
        final String sourcePath = path + FS + fileName;
        final String jiraImportDir = getJiraHomeDirectory() + FS + IMPORT_DIR + FS + new File(fileName).getName();

        boolean baseUrlReplaced = xmlBackupCopier.copyXmlBackupTo(sourcePath, jiraImportDir);

        log("Restoring data '" + jiraImportDir + "'");
        tester.gotoPage(getRestoreUrl());
        tester.setWorkingForm("restore-xml-data-backup");
        tester.setFormElement("filename", fileName);
        tester.setFormElement("license", licenseString);
        tester.setFormElement("outgoingEmail", outgoingMailSetting.asString());
        if (useDefaultPaths)
        {
            reflectivelySetDefaultPaths(tester.getDialog().getForm());
        }
        if (clearCache)
        {
            tester.checkCheckbox("quickImport", "true");
        }
        tester.submit();
        waitForRestore();

        if (!isRestoreSuccessful())
        {
            //The following are assertions of possible error messages to display the cause of failure to import
            //instead of having to check HTML dump manually. Please add/modify new error messages not included already.
            assertCauseOfError("The xml data you are trying to import seems to be from a newer version of JIRA. This will not work.", jiraImportDir);
            assertCauseOfError("You must enter the location of an XML file.", jiraImportDir);
            assertCauseOfError("Could not find file at this location.", jiraImportDir);
            assertCauseOfError("Invalid license key specified.", jiraImportDir);
            assertCauseOfError("The current license is too old to install this version of JIRA", jiraImportDir);
            assertCauseOfError("Invalid license type for this version of JIRA. License should be of type Standard.", jiraImportDir);
            assertCauseOfError("Invalid license type for this version of JIRA. License should be of type Professional.", jiraImportDir);
            assertCauseOfError("Invalid license type for this version of JIRA. License should be of type Enterprise.", jiraImportDir);
            assertCauseOfError("You must specify an index for the restore process.", jiraImportDir);
            assertCauseOfError("Error parsing export file. Your export file is invalid.", jiraImportDir);
            assertCauseOfError("specified in the backup file is not valid", jiraImportDir);
            throw new AssertionError("Failed to restore JIRA data from [" + jiraImportDir + "]. See logs for details.");
        }

        navigation.disableWebSudo();
        navigation.login(username, password);

        final long howLong =  timer.end();
        log("Restored '" + fileName + "' in " + (howLong) + "ms");

        if (!baseUrlReplaced)
        {
            generalConfiguration.setBaseUrl(getEnvironmentData().getBaseUrl().toString());
        }
        tester.beginAt("/");
    }

    public void restoreI18nData(final String fileName)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Restore");

        final String filePath = environmentData.getXMLDataLocation().getAbsolutePath() + "/" + fileName;

        File file = new File(filePath);
        copyFileToJiraImportDirectory(file);

        log("Restoring data '" + filePath + "'");
        tester.gotoPage(getRestoreUrl());
        tester.setWorkingForm("restore-xml-data-backup");
        tester.setFormElement("filename", file.getName());
        tester.setFormElement("license", V2_COMMERCIAL.getLicenseString());
        tester.checkCheckbox("quickImport", "true");
        tester.submit();
        waitForRestore();

        if (!isRestoreSuccessful())
        {
            Assert.fail("Your project failed to import successfully. See logs for details");
        }
        final long howLong = timer.end();

        navigation.disableWebSudo();
        navigation.login(FunctTestConstants.ADMIN_USERNAME, FunctTestConstants.ADMIN_PASSWORD);
        log("Restored '" + fileName + "' in " + (howLong) + "ms");
    }

    @Override
    public void restoreDataWithBuildNumber(String fileName, int expectedBuilderNumber)
    {
        // make sure the backup file has not been upgraded
        File backupFile = new File(getEnvironmentData().getXMLDataLocation(), fileName);
        FileInputStream backup = null;
        try
        {
            backup = new FileInputStream(backupFile);
            assertThat(backup, hasBuildNumber(expectedBuilderNumber));
        }
        catch (FileNotFoundException missingXmlBackup)
        {
            fail(String.format("The xml backup file: %s could not be found.", fileName));
        }
        finally
        {
            if (backup != null)
            {
                IOUtils.closeQuietly(backup);
            }
        }
        restoreData(fileName);
    }

    private void reflectivelySetDefaultPaths(final WebForm jiraForm)
    {
        final Class<WebForm> webFormClass = WebForm.class;
        final Class<WebRequestSource> webRequestSourceClass = WebRequestSource.class;
        reflectivlySetField(jiraForm, webFormClass, "_formParameters", null);
        reflectivlySetField(jiraForm, webFormClass, "_presetParameters", null);
        final String postUrl = (String) reflectivelyGetField(jiraForm, webRequestSourceClass, "_destination"); //the _destination field exists on the superclass of webForms
        //TODO: check the action url for other params before appending to it!
        reflectivlySetField(jiraForm, webRequestSourceClass, "_destination", postUrl + "?useDefaultPaths=true");
        final Class[] paramType = {String.class, String.class};
        final String[] params = {"useDefaultPaths", "true"};
        reflectivelyInvoke(jiraForm, webFormClass, "addPresetParameter", paramType, params);
        tester.setFormElement("useDefaultPaths", "true");
    }

    private void reflectivelyInvoke(final Object self, final Class<?> clazz, final String methodName, final Class[] paramType, final Object[] params)
    {
        Preconditions.checkNotNull(self, "cannot invoke %s on null object", methodName);
        Method method = null;
        try
        {
            method = clazz.getDeclaredMethod(methodName, paramType);
            method.setAccessible(true);
            method.invoke(self, params);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(String.format("Error getting method '%s(%s)' for %s : possibly a library update has changed this method", methodName, Arrays.toString(paramType), clazz.getName()), e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(String.format("Error invoking method '%s(%s)' for %s : exception raised during method invocation : %s", e.getCause().getMessage(), methodName, Arrays.toString(paramType), clazz.getName()), e.getCause());
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(String.format("Error invoking method '%s(%s)' for %s : possibly a security manager has prevented access to this method", methodName, Arrays.toString(paramType), clazz.getName()), e);
        }
        finally
        {
            if (method != null) {
                method.setAccessible(false);
            }
        }
    }

    private void reflectivlySetField(final Object self, Class<?> clazz, final String fieldName, final Object fieldValue)
    {
        Preconditions.checkNotNull(self, "cannot set field %s to %s on null object", fieldName, fieldValue);
        Field field = null;
        try
        {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(self, fieldValue);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(String.format("Error setting field '%s' to '%s' for %s : possibly a security manager has prevented access to this field", fieldName, fieldValue, clazz.getName()), e);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(String.format("Error setting field '%s' to '%s' for %s : possibly a library update has changed this field", fieldName, fieldValue, clazz.getName()), e);
        }
        finally
        {
            if (field != null)
            {
                field.setAccessible(false);
            }
        }
    }
    private Object reflectivelyGetField(final Object self, final Class<?> clazz, final String fieldName)
    {
        Preconditions.checkNotNull(self, "cannot get field %s on null object", fieldName);
        Field field = null;
        try
        {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(self);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(String.format("Error getting field '%s' for %s : possibly a security manager has prevented access to this field", fieldName, clazz.getName()), e);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(String.format("Error getting field '%s' for %s : possibly a library update has changed this field", fieldName, clazz.getName()), e);
        }
        finally
        {
            if (field != null)
            {
                field.setAccessible(false);
            }
        }
    }

    public File exportDataToFile(final String fileName)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Export");

        final String realFileName = FilenameUtils.getName(fileName);

        log("Backing up data to '" + realFileName + "'");
        tester.gotoPage("secure/admin/XmlBackup!default.jspa");
        tester.setWorkingForm("jiraform");
        tester.setFormElement("filename", realFileName);
        tester.submit();
        if (new IdLocator(tester, "replace_submit").exists())
        {
            tester.setWorkingForm("jiraform");
            tester.submit();
        }

        final String text = StringUtils.stripToNull(new IdLocator(tester, "backup-file").getText());
        if (text == null)
        {
            Assert.fail("The restore did not redirect to the result page.");
        }
        timer.end();

        final File file = new File(text);
        Assert.assertTrue("Backup returned '" + text + "' which is not an absolute file.", file.isAbsolute());
        return file;
    }

    public String getCurrentAttachmentPath()
    {
        navigation.gotoAdmin();
        tester.clickLink("attachments");
        // Get the table 'attachmentSettings'.
        final WebTable attachmentSettings = tester.getDialog().getWebTableBySummaryOrId("table-AttachmentSettings");
        // Check that  'Attachment Path' is in the third row where we expect it:
        if (attachmentSettings.getCellAsText(1, 0).contains("Attachment Path"))
        {
            String attachmentPath = attachmentSettings.getCellAsText(1, 1).trim();
            // Check if this is the "default" directory. Looks like "Default Directory [/home/mlassau/jira/jira_trunk/data/attachments]"
            if (attachmentPath.startsWith("Default Directory ["))
            {
                // Strip "Default Directory [" from the front, and the "]" from the end
                attachmentPath = attachmentPath.substring("Default Directory [".length(), attachmentPath.length() - 1);
            }
            return attachmentPath;
        }
        else
        {
            throw new RuntimeException("Error occured when trying to screen-scrape the attachment path. 'Attachment Path' not found where expected in the table.");
        }
    }

    public void activateSubTasks()
    {
        log("activating sub tasks");
        tester.gotoPage("/secure/admin/subtasks/ManageSubTasks.jspa");
        if (tester.getDialog().isLinkPresentWithText("Enable"))
        {
            tester.clickLinkWithText("Enable");
        }
        else
        {
            log("Subtasks already enabled");
        }
    }

    public void addSubTaskType(final String name)
    {
        activateSubTasks();
        tester.setFormElement("name", name);
        tester.submit("Add");
    }

    public GeneralConfiguration generalConfiguration()
    {
        return generalConfiguration;
    }

    @Override
    public Backdoor backdoor()
    {
        return backdoor;
    }

    public Project project()
    {
        return project;
    }

    public UsersAndGroups usersAndGroups()
    {
        return usersAndGroups;
    }

    public Roles roles()
    {
        return roles;
    }

    public CustomFields customFields()
    {
        return customFields;
    }

    public PermissionSchemes permissionSchemes()
    {
        return permissionSchemes;
    }

    public IssueSecuritySchemes issueSecuritySchemes()
    {
        return issueSecuritySchemes;
    }

    public FieldConfigurations fieldConfigurations()
    {
        return fieldConfigurations;
    }

    public FieldConfigurationSchemes fieldConfigurationSchemes()
    {
        return fieldConfigurationSchemes;
    }

    public ProjectImport projectImport()
    {
        return projectImport;
    }

    public Plugins plugins()
    {
        return plugins;
    }

    public void removeGlobalPermission(final GlobalPermissionKey permissionKey, final String group)
    {
        final String deleteLink = "del_" + permissionKey.getKey() + "_" + group;
        navigation.gotoAdminSection("global_permissions");
        if ((tester.getDialog().isLinkPresent(deleteLink)))
        {
            tester.clickLink(deleteLink);
            tester.submit("Delete");
        }
    }

    public void removeGlobalPermission(final int permission, final String group)
    {
        final GlobalPermissionKey permissionKey = GlobalPermissionKey.GLOBAL_PERMISSION_ID_TRANSLATION.get(permission);
        removeGlobalPermission(permissionKey, group);
    }

    public void addGlobalPermission(final GlobalPermissionKey permission, final String group)
    {
        final HtmlPage page = new HtmlPage(tester);
        final String addUrl = page.addXsrfToken("secure/admin/jira/GlobalPermissions.jspa?groupName=" + group + "&globalPermType=" + permission.getKey() + "&action=add");
        tester.gotoPage(addUrl);
    }

    public void addGlobalPermission(final int permission, final String group)
    {
        final GlobalPermissionKey permissionKey = GlobalPermissionKey.GLOBAL_PERMISSION_ID_TRANSLATION.get(permission);
        addGlobalPermission(permissionKey, group);
    }

    public void switchToLicense(final LicenseKeys.License license)
    {
        switchToLicense(license.getLicenseString(), license.getDescription());
    }

    public void switchToLicense(final String license, final String description)
    {
        navigation.gotoAdminSection("license_details");
        tester.setFormElement("license", license);
        tester.submit("Add");

        text.assertTextPresent(new XPathLocator(tester,"//table[@id='license_table']"), description);
    }

    public void switchToPersonalLicense()
    {
        switchToLicense(LicenseKeys.V2_PERSONAL.getLicenseString(), "JIRA " + getEdition() + ": Personal");
    }

    public void switchToStarterLicense()
    {
        switchToLicense(LicenseKeys.V2_STARTER.getLicenseString(), "JIRA " + getEdition() + ": Starter");
    }

    public String getJiraHomeDirectory()
    {
        String jiraHome = JIRA_HOME_DIR.get();
        if (jiraHome == null)
        {
            String jiraHomePath = null;
            try
            {
                navigation.gotoAdminSection("system_info");
                WebTable filePathTable = tester.getDialog().getResponse().getTableWithID("file_paths");
                if (filePathTable != null && filePathTable.getTableCellWithID("file_paths_jirahome") != null)
                {
                    jiraHomePath = filePathTable.getTableCellWithID("file_paths_jirahome").asText().trim();
                }

                if (jiraHomePath == null)
                {
                    throw new RuntimeException("Can't find JIRA.HOME. Do you have websudo enabled?");
                }
                else
                {
                    // try to get the canonical path
                    JIRA_HOME_DIR.set(jiraHome = new File(jiraHomePath).getCanonicalPath());
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        return jiraHome;
    }

    @Override
    public String getSystemTenantHomeDirectory()
    {
        navigation.gotoAdminSection("system_info");
        final WebTable filePathTable;
        try
        {
            filePathTable = tester.getDialog().getResponse().getTableWithID("file_paths");
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
        if (filePathTable != null && filePathTable.getTableCellWithID("file_paths_jirahome") != null)
        {
            return filePathTable.getTableCellWithID("file_paths_jirahome").asText().trim();
        }
        return null;
    }

    @Override
    public MailServerAdministration mailServers()
    {
        return mailServerAdministration;
    }

    @Override
    public SharedFiltersAdministration sharedFilters()
    {
        return sharedFiltersAdministration;
    }

    @Override
    public SharedDashboardsAdministration sharedDashboards()
    {
        return sharedDashboardsAdministration;
    }

    @Override
    public SendBulkMail sendBulkMail()
    {
        return sendBulkMail;
    }

    @Override
    public AdminTabs tabs()
    {
        return adminTabs;
    }

    @Override
    public AdvancedApplicationProperties advancedApplicationProperties()
    {
        return advancedApplicationProperties;
    }

    private void assertCauseOfError(final String errorMessage, final String filePath)
    {
        if (tester.getDialog().isTextInResponse(errorMessage))
        {
            throw new AssertionError("Failed to restore JIRA data. Cause: " + errorMessage + " File path: [" + filePath + "]");
        }
    }

    public Subtasks subtasks()
    {
        return new SubtasksImpl(tester, getEnvironmentData());
    }

    public IssueLinking issueLinking()
    {
        return new IssueLinkingImpl(tester, navigation, logger);
    }

    public TimeTracking timeTracking()
    {
        return new TimeTrackingImpl(tester, getEnvironmentData());
    }

    public Resolutions resolutions()
    {
        return resolutions;
    }

    public ViewServices services()
    {
        return viewServices;
    }

    public String getEdition()
    {
        return ENTERPRISE;
    }

    public long getBuildNumber()
    {
        final IdLocator idLocator = new IdLocator(tester, "footer-build-information");
        final String buildInfo = idLocator.getText();

        if (StringUtils.isBlank(buildInfo))
        {
            throw new RuntimeException("Unable to find build information in the footer.");
        }

        final Matcher matcher = PATTERN_BUILD_NUMBER.matcher(buildInfo);
        if (!matcher.find())
        {
            throw new RuntimeException("Unable to find build number from the footer.");
        }

        try
        {
            return Long.parseLong(matcher.group(1));
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException("Unable to find builder number from the footer.", e);
        }
    }

    private String replaceTokens(String source, final Map<String, String> replacements)
    {
        for (final String token : replacements.keySet())
        {
            final int index = source.indexOf(token);
            if (index < 0)
            {
                Assert.fail("Replacement token '" + token + "' not found");
            }
            source = source.replaceAll(token, Matcher.quoteReplacement(replacements.get(token)));
        }
        return source;
    }

    public void runJellyScript(final String script)
    {
        log("Running jelly script '" + script + "'.");
        navigation.gotoAdminSection("jelly_runner");
        tester.setFormElement("script", script);
        tester.submit("Run now");
    }

    public void enableAccessLogging()
    {
        log("enabling access logging");
        navigation.gotoAdminSection("logging_profiling");
        tester.clickLink("enable_http_access");
    }

    @Override
    public Attachments attachments()
    {
        return attachments;
    }

    @Override
    public ViewFieldScreens viewFieldScreens()
    {
        return viewFieldScreens;
    }

    @Override
    public Utilities utilities()
    {
        return new Util();
    }

    @Override
    public ViewWorkflows workflows()
    {
        return workflows;
    }

    @Override
    public NotificationSchemes notificationSchemes()
    {
        return notificationSchemes;
    }

    public static class DefaultLink implements Link
    {
        private final WebTester tester;

        private DefaultLink(final WebTester tester)
        {
            this.tester = tester;
        }

        @Override
        public boolean isPresent()
        {
            return tester.getDialog().isLinkPresent("admin_link") || tester.getDialog().isLinkPresent("admin_project_menu");
        }
    }

    private class Util implements Utilities
    {
        public void runServiceNow(final long serviceId)
        {
            navigation.gotoPage("ServiceExecutor.jspa");
            tester.setFormElement("serviceId", valueOf(serviceId));
            tester.submit();
            tester.assertTextNotPresent("No service with this id exists");
        }
    }

    private void waitForIndexCompletion(long sleepTime, int retryCount)
    {
        asynchronousTasks.waitForSuccessfulCompletion(sleepTime, retryCount, "Indexing");
    }

    private boolean isRestoreSuccessful()
    {
        return isOnRestore() && new XPathLocator(tester, "//a[@id=\"login\"]").exists();
    }

    private boolean isOnRestore()
    {
        return tester.getDialog().getResponse().getURL().toExternalForm().contains("ImportResult.jspa");
    }

    public void waitForRestore()
    {
        //wait for result page to come up
        String url = tester.getDialog().getResponse().getURL().toExternalForm();
        while(url.contains("importprogress"))
        {
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
            }
            final String subUrl = url.substring(getEnvironmentData().getBaseUrl().toString().length());
            tester.gotoPage(subUrl);
            url = tester.getDialog().getResponse().getURL().toExternalForm();
        }
    }
}
