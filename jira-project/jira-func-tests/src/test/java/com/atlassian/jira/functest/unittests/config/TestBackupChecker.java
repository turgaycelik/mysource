package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.BackupChecker;
import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.XmlUtils;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.service.ConfigService;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * test {@link com.atlassian.jira.functest.config.BackupChecker}.
 *
 * @since v4.1
 */
public class TestBackupChecker extends TestCase
{
    private static final String CHECKID_SERVICE = "backupservice";
    private static final String CHECKID_SERVICE_HOME = "backupservicehome";
    private static final String CHECKID_SERVICE_DIRECTORY = "backupservicedirectory";
    private static final String CHECKID_GLOBAL = "backupglobaldirectory";

    private static final String PROP_GLOBALPATH = "jira.path.backup";
    private static final String PROP_USE_DEFAULT_DIRECTORY = "USE_DEFAULT_DIRECTORY";
    private static final String PROP_DIR_NAME = "DIR_NAME";

    private static final String DEAFULT_BACKUP_PATH = "func_test_backup";


    public void testInvalidProperties() throws Exception
    {
        JiraConfig config = new JiraConfig(XmlUtils.createInvalidDocument(), null);
        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.allOptions());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertTrue(result.getErrors().iterator().next().getMessage().matches(".*Unable to read entry:.*"));
    }

    public void testCheckBackupServiceNoServices() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigService service = new ConfigService(6L, 7L, "DontMatch", "blarg", new ConfigPropertySet());

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.allOptions());
        assertGoodResult(result);
    }

    public void testCheckBackupServiceExportService() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigService service = new ConfigService(6L, 7L, "ExportService", "blarg", new ConfigPropertySet());

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.error("Backup service 'blarg' exists.", CHECKID_SERVICE);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupServiceBadHomeAndDir() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigPropertySet serviceConfig = new ConfigPropertySet();
        serviceConfig.setStringProperty(PROP_DIR_NAME, "somethingrandom");
        final ConfigService service = new ConfigService(6L, 7L, "ExportService", "blarg", serviceConfig);

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE));

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.error("Backup service configured to output to directory 'somethingrandom' it should be using JIRA.HOME.", CHECKID_SERVICE_HOME);
        builder.warning("Backup service configured to output to 'somethingrandom'. It should always be set to 'func_test_backup' even when using JIRA.HOME.", CHECKID_SERVICE_DIRECTORY);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupServiceBadHomeAndDirDisabled() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigPropertySet serviceConfig = new ConfigPropertySet();
        final ConfigService service = new ConfigService(6L, 7L, "ExportService", "blarg", serviceConfig);
        serviceConfig.setStringProperty(PROP_DIR_NAME, "somethingrandom");

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config,
                CheckOptionsUtils.disabled(CHECKID_SERVICE, CHECKID_SERVICE_DIRECTORY, CHECKID_SERVICE_HOME));

        CheckResultBuilder builder = new CheckResultBuilder();
        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupServiceGoodHomeNoDir() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();

        final ConfigPropertySet serviceConfig = new ConfigPropertySet();
        serviceConfig.setStringProperty(PROP_USE_DEFAULT_DIRECTORY, "true");
        final ConfigService service = new ConfigService(6L, 7L, "ExportService", "blarg", serviceConfig);

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE));

        CheckResultBuilder builder = new CheckResultBuilder();
        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupServiceGoodHomeBadDir() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();

        final ConfigPropertySet serviceConfig = new ConfigPropertySet();
        serviceConfig.setStringProperty(PROP_USE_DEFAULT_DIRECTORY, "true");
        serviceConfig.setStringProperty(PROP_DIR_NAME, "somethingrandom");
        final ConfigService service = new ConfigService(6L, 7L, "ExportService", "blarg", serviceConfig);

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE));

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.warning("Backup service configured to output to 'somethingrandom'. It should always be set to 'func_test_backup' even when using JIRA.HOME.", CHECKID_SERVICE_DIRECTORY);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupServiceGoodDir() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();

        final ConfigPropertySet serviceConfig = new ConfigPropertySet();
        serviceConfig.setStringProperty(PROP_DIR_NAME, "func_test_backup");
        final ConfigService service = new ConfigService(6L, 7L, "ExportService", "blarg", serviceConfig);

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE));

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.error("Backup service configured to output to directory 'func_test_backup' it should be using JIRA.HOME.", CHECKID_SERVICE_HOME);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupServiceGoodHomeAndDir() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();

        final ConfigPropertySet serviceProperty = new ConfigPropertySet();
        serviceProperty.setStringProperty(PROP_DIR_NAME, DEAFULT_BACKUP_PATH);
        serviceProperty.setStringProperty(PROP_USE_DEFAULT_DIRECTORY, "true");
        final ConfigService service = new ConfigService(6L, 7L, "ExportService", "blarg", serviceProperty);

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.singletonList(service));
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE));

        CheckResultBuilder builder = new CheckResultBuilder();
        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupGlobalBackupSet() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        ap.setStringProperty(PROP_GLOBALPATH, "random");

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.<ConfigService>emptyList());
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.warning("Global backup path set to 'random'.", CHECKID_GLOBAL);
        assertEquals(builder.buildResult(), result);
    }

    public void testCheckBackupGlobalBackupSetCheckDisabled() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        ap.setStringProperty(PROP_GLOBALPATH, "random");

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.<ConfigService>emptyList());
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        final ConfigurationCheck.Result result = backupChecker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE_HOME));

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.warning("Global backup path set to 'random'.", CHECKID_GLOBAL);
        assertEquals(builder.buildResult(), result);
    }

    public void testFixNothing() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigService service = new ConfigService(6L, 7L, "sasadasd.ExportSevice", "blarg", new ConfigPropertySet());
        final List<ConfigService> services = new ArrayList<ConfigService>();
        services.add(service);

        final JiraConfig config = new JiraConfig();
        config.setServices(services);
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        backupChecker.fixConfiguration(config, CheckOptionsUtils.allOptions());
        assertEquals(1, config.getServices().size());
        assertSame(service, config.getServices().get(0));
    }

    public void testFixService() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigService service = new ConfigService(6L, 7L, "sasadasd.ExportService", "blarg", new ConfigPropertySet());
        final List<ConfigService> services = new ArrayList<ConfigService>();
        services.add(service);

        final JiraConfig config = new JiraConfig();
        config.setServices(services);
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        backupChecker.fixConfiguration(config, CheckOptionsUtils.allOptions());
        assertTrue(config.getServices().isEmpty());
    }

    public void testFixServiceWithDefault() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigPropertySet servicePs = new ConfigPropertySet();
        final ConfigService service = new ConfigService(6L, 7L, "sasadasd.ExportService", "blarg", servicePs);
        final List<ConfigService> services = new ArrayList<ConfigService>();
        services.add(service);

        final JiraConfig config = new JiraConfig();
        config.setServices(services);
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        backupChecker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE));
        assertEquals(1, services.size());
        assertEquals(Boolean.TRUE.toString(), service.getPropertySet().getStringProperty(PROP_USE_DEFAULT_DIRECTORY));
        assertNull(service.getPropertySet().getStringProperty(PROP_DIR_NAME));
    }

    public void testFixServiceWithDefaultAndDir() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        final ConfigPropertySet servicePs = new ConfigPropertySet();
        servicePs.setStringProperty(PROP_DIR_NAME, "random");

        final ConfigService service = new ConfigService(6L, 7L, "sasadasd.ExportService", "blarg", servicePs);
        final List<ConfigService> services = new ArrayList<ConfigService>();
        services.add(service);

        final JiraConfig config = new JiraConfig();
        config.setServices(services);
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        backupChecker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE, CHECKID_SERVICE_HOME));
        assertEquals(1, services.size());
        assertEquals(service.getPropertySet().getStringProperty(PROP_DIR_NAME), "func_test_backup");
        assertNull(service.getPropertySet().getStringProperty(PROP_USE_DEFAULT_DIRECTORY));
    }

    public void testFixGlobalBackupPath() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();
        ap.setStringProperty(PROP_GLOBALPATH, "random");

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.<ConfigService>emptyList());
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        backupChecker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE_HOME));

        assertNull(ap.getStringProperty(PROP_GLOBALPATH));
    }

    public void testFixGlobalBackupPathNullPath() throws Exception
    {
        final ConfigPropertySet ap = new ConfigPropertySet();

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.<ConfigService>emptyList());
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        backupChecker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE_HOME));

        assertNull(ap.getStringProperty(PROP_GLOBALPATH));
    }

    public void testFixGlobalBackupPathDisabled() throws Exception
    {
        //We should not change the path when the test is disabled.
        final ConfigPropertySet ap = new ConfigPropertySet();
        ap.setStringProperty(PROP_GLOBALPATH, "random");

        final JiraConfig config = new JiraConfig();
        config.setServices(Collections.<ConfigService>emptyList());
        config.setApplicationProperties(ap);

        final BackupChecker backupChecker = new BackupChecker();
        backupChecker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_SERVICE_HOME, CHECKID_GLOBAL));

        assertEquals("random", ap.getStringProperty(PROP_GLOBALPATH));
    }

    private void assertGoodResult(ConfigurationCheck.Result result)
    {
        assertTrue("Expected good result but found errors", result.getErrors().isEmpty());
        assertTrue("Expected good result but found warnings", result.getWarnings().isEmpty());
    }
}
