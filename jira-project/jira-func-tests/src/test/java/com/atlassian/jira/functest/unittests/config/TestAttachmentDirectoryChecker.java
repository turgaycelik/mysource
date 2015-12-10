package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.AttachmentDirectoryChecker;
import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.XmlUtils;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import junit.framework.TestCase;

/**
 * Test for {@link com.atlassian.jira.functest.config.AttachmentDirectoryChecker}.
 *
 * @since v4.1
 */
public class TestAttachmentDirectoryChecker extends TestCase
{
    private static final String PROP_HOME = "jira.path.attachments.use.default.directory";
    private static final String PROP_PATH = "jira.path.attachments";
    private static final String PROP_ENABLED = "jira.option.allowattachments";

    private static final String VALUE_PATH = "func_test_attachments";

    private static final String CHECK_HOME = "attachhome";
    private static final String CHECK_DIR = "attachdirectory";

    public void testInvalidProperties() throws Exception
    {
        JiraConfig config = new JiraConfig(XmlUtils.createInvalidDocument(), null);
        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertTrue(result.getErrors().iterator().next().getMessage().matches(".*Unable to read entry:.*"));
    }

    public void testCheckAttachmentsChecksDisabled() throws Exception
    {
        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(new ConfigPropertySet());

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_DIR, CHECK_HOME));
        assertTrue(result.isGood());
   }

    public void testCheckAttachmentsGood() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, true);
        configPropertySet.setBooleanProperty(PROP_ENABLED, true);
        configPropertySet.setBooleanProperty(PROP_PATH, true);
        configPropertySet.setStringProperty(PROP_PATH, VALUE_PATH);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());
        assertTrue(result.isGood());
    }

    public void testCheckAttachmentsNotHome() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, false);
        configPropertySet.setBooleanProperty(PROP_ENABLED, true);
        configPropertySet.setStringProperty(PROP_PATH, VALUE_PATH);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());
        assertTrue(result.getWarnings().isEmpty());

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.error("File has 'func_test_attachments' configured as its attachment path. It should be using its JIRA.HOME.", CHECK_HOME);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckAttachmentsNotHomeAttachmentsDisabled() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, false);
        configPropertySet.setBooleanProperty(PROP_ENABLED, false);
        configPropertySet.setStringProperty(PROP_PATH, VALUE_PATH);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.warning("File has 'func_test_attachments' configured as its attachment path. It should be using its JIRA.HOME.", "attachhome");

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckAttachmentsNotHomeCheckDisabled() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, false);
        configPropertySet.setStringProperty(PROP_PATH, VALUE_PATH);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_HOME));
        assertTrue(result.isGood());
    }

    public void testCheckAttachmentsBadDir() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setStringProperty(PROP_PATH, "something");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_HOME));

        CheckResultBuilder builder = new CheckResultBuilder();
        builder.warning("File has 'something' configured as it attachment path. It should always be set to 'func_test_attachments' even when using JIRA.HOME.", CHECK_DIR);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckAttachmentsBadDirCheckDisabled() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, true);
        configPropertySet.setStringProperty(PROP_PATH, "randombad");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECK_DIR));

        assertTrue(result.isGood());
    }

    public void testCheckAttachmentsDirNotSet() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, true);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(result.isGood());
    }

    public void testFixAttachmentsGoodAlready() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, true);
        configPropertySet.setStringProperty(PROP_PATH, VALUE_PATH);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(configPropertySet.getBooleanProperty(PROP_HOME));
        assertEquals(VALUE_PATH, configPropertySet.getStringProperty(PROP_PATH));
    }

    public void testFixAttachmentsBadHome() throws Exception
    {
        ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, false);
        configPropertySet.setStringProperty(PROP_PATH, VALUE_PATH);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);
        configPropertySet = config.getApplicationProperties();

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(configPropertySet.getBooleanProperty(PROP_HOME));
        assertEquals(VALUE_PATH, configPropertySet.getStringProperty(PROP_PATH));
    }

    public void testFixAttachmentsBadHomeCheckDisabled() throws Exception
    {
        final ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setStringProperty(PROP_PATH, VALUE_PATH);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_HOME));

        assertNull(configPropertySet.getBooleanProperty(PROP_HOME));
        assertEquals(VALUE_PATH, configPropertySet.getStringProperty(PROP_PATH));
    }

    public void testFixAttachmentsBadPath() throws Exception
    {
        ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, false);
        configPropertySet.setBooleanProperty(PROP_ENABLED, true);
        configPropertySet.setStringProperty(PROP_PATH, "BadPath");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);
        configPropertySet = config.getApplicationProperties();

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_HOME));

        assertFalse(configPropertySet.getBooleanProperty(PROP_HOME));
        assertEquals(VALUE_PATH, configPropertySet.getStringProperty(PROP_PATH));
    }

    public void testFixAttachmentsBadPathCheckDisabled() throws Exception
    {
        ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, false);
        configPropertySet.setStringProperty(PROP_PATH, "random");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);
        configPropertySet = config.getApplicationProperties();

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_DIR));

        assertTrue(configPropertySet.getBooleanProperty(PROP_HOME));
        assertEquals("random", configPropertySet.getStringProperty(PROP_PATH));
    }

    public void testFixAttachmentsNoPath() throws Exception
    {
        ConfigPropertySet configPropertySet = new ConfigPropertySet();

        configPropertySet.setBooleanProperty(PROP_HOME, false);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(configPropertySet);
        configPropertySet = config.getApplicationProperties();

        final AttachmentDirectoryChecker checker = new AttachmentDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECK_DIR));

        assertTrue(configPropertySet.getBooleanProperty(PROP_HOME));
        assertFalse(configPropertySet.contains(PROP_PATH));
    }
}