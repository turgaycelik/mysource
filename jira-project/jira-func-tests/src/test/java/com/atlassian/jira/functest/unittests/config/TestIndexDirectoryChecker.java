package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.CheckOptionsUtils;
import com.atlassian.jira.functest.config.CheckResultBuilder;
import com.atlassian.jira.functest.config.ConfigurationCheck;
import com.atlassian.jira.functest.config.IndexDirectoryChecker;
import com.atlassian.jira.functest.config.JiraConfig;
import com.atlassian.jira.functest.config.XmlUtils;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import junit.framework.TestCase;

/**
 * Test for {@link com.atlassian.jira.functest.config.IndexDirectoryChecker}.
 *
 * @since v4.1
 */
public class TestIndexDirectoryChecker extends TestCase
{
    private static final String CHECKID_INDEX_HOME = "indexhome";
    private static final String CHECKID_INDEX_ENABLED = "indexenabled";
    private static final String CHECKID_INDEX_DIRECTORY = "indexdirectory";

    private final static String PROP_INDEX_OPTION = "jira.option.indexing";
    private final static String PROP_INDEX_PATH = "jira.path.index";
    private final static String PROP_INDEX_DEFAULT_DIR = "jira.path.index.use.default.directory";

    private static final String FUNC_TEST_INDEX = "func_test_index";


    public void testInvalidProperties() throws Exception
    {
        JiraConfig config = new JiraConfig(XmlUtils.createInvalidDocument(), null);
        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getWarnings().isEmpty());
        assertTrue(result.getErrors().iterator().next().getMessage().matches(".*Unable to read entry:.*"));
    }

    public void testCheckGoodConfiguration() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingDisabled() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, false);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        builder.warning("File does not have indexing enabled.", CHECKID_INDEX_ENABLED);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingNotSet() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        builder.warning("File does not have indexing enabled.", CHECKID_INDEX_ENABLED);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingDisabledCheckDisabled() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, false);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_ENABLED));

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingUsingHome() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, false);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        builder.error("File has 'func_test_index' configured as its index path. It should be using its JIRA.HOME.", CHECKID_INDEX_HOME);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingUsingHomeNotSet() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        builder.error("File has 'func_test_index' configured as its index path. It should be using its JIRA.HOME.", CHECKID_INDEX_HOME);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingUsingHomeCheckDisabled() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, false);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_HOME));

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingDir() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, "radomdir");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_HOME));

        builder.warning("File has 'radomdir' configured as its index path. It should be using 'func_test_index' even with JIRA.HOME configured.", CHECKID_INDEX_DIRECTORY);

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingDirCheckDisabled() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, "radomdir");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_DIRECTORY));

        assertEquals(builder.buildResult(), result);
    }

    public void testCheckIndexingNoDir() throws Exception
    {
        CheckResultBuilder builder = new CheckResultBuilder();

        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        final ConfigurationCheck.Result result = checker.checkConfiguration(config, CheckOptionsUtils.allOptions());

        assertEquals(builder.buildResult(), result);
    }

    public void testFixGood() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));
    }

    public void testFixIndexingDisabled() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, false);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        ap = config.getApplicationProperties();

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));

    }

    public void testFixIndexingNotSet() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, false);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        ap = config.getApplicationProperties();

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));
    }

    public void testFixIndexingCheckDisabled() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, false);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_ENABLED));

        assertFalse(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));

        ap.removeProperty(PROP_INDEX_OPTION);
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_ENABLED));

        assertNull(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));
    }

    public void testFixHomeDir() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, false);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        ap = config.getApplicationProperties();

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));

        //Check when it is not set.
        ap.removeProperty(PROP_INDEX_DEFAULT_DIR);
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));
    }

    public void testFixHomeDirCheckDisabled() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, false);
        ap.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_HOME));

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertFalse(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));

        ap.removeProperty(PROP_INDEX_DEFAULT_DIR);
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_HOME));

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertNull(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));
    }

    public void testFixDefaultDir() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        ap.setStringProperty(PROP_INDEX_PATH, "random");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);
        ap = config.getApplicationProperties();

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals(FUNC_TEST_INDEX, ap.getStringProperty(PROP_INDEX_PATH));

        //Check when it is not set.
        ap.removeProperty(PROP_INDEX_PATH);
        checker.fixConfiguration(config, CheckOptionsUtils.allOptions());

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertNull(ap.getStringProperty(PROP_INDEX_PATH));
    }

    public void testFixDefaultDirCheckDisabled() throws Exception
    {
        ConfigPropertySet ap = new ConfigPropertySet();
        ap.setBooleanProperty(PROP_INDEX_OPTION, true);
        ap.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, false);
        ap.setStringProperty(PROP_INDEX_PATH, "random");

        JiraConfig config = new JiraConfig();
        config.setApplicationProperties(ap);
        ap = config.getApplicationProperties();

        final IndexDirectoryChecker checker = new IndexDirectoryChecker();
        checker.fixConfiguration(config, CheckOptionsUtils.disabled(CHECKID_INDEX_DIRECTORY));

        assertTrue(ap.getBooleanProperty(PROP_INDEX_OPTION));
        assertTrue(ap.getBooleanProperty(PROP_INDEX_DEFAULT_DIR));
        assertEquals("random", ap.getStringProperty(PROP_INDEX_PATH));

     }
}
