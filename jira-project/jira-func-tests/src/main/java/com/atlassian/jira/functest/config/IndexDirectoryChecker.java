package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.config.ps.ConfigPropertySet;

/**
 * A check to make sure that indexing is correctly configured in JIRA XML data.
 *
 * @since v4.0
 */
public class IndexDirectoryChecker implements ConfigurationCheck
{
    /**
     * The check ID to ensure that we are using JIRA.HOME for indexing.
     */
    public static final String CHECKID_INDEX_HOME = "indexhome";

    /**
     * The check to ensure that indexing is enabled.
     */
    public static final String CHECKID_INDEX_ENABLED = "indexenabled";

    /**
     * The check that will set the index directory to {@link #FUNC_TEST_INDEX}.
     */
    public static final String CHECKID_INDEX_DIRECTORY = "indexdirectory";

    /**
     * JIRA option to see if indexing is enabled..
     */
    private final static String PROP_INDEX_OPTION = "jira.option.indexing";

    /**
     * JIRA option to set the path to the index.
     */
    private final static String PROP_INDEX_PATH = "jira.path.index";

    /**
     * JIRA option that tells it to use the default directory.
     */
    private final static String PROP_INDEX_DEFAULT_DIR = "jira.path.index.use.default.directory";

    /**
     * The default directory for func test indexes.
     */
    private static final String FUNC_TEST_INDEX = "func_test_index";

    public Result checkConfiguration(final JiraConfig config, final CheckOptions options)
    {
        final CheckResultBuilder builder = new CheckResultBuilder();
        try
        {
            processConfig(config, options, builder);
        }
        catch (Exception e)
        {
            return builder.error(e.getMessage()).buildResult();
        }
        return builder.buildResult();
    }

    private void processConfig(final JiraConfig config, final CheckOptions options, final CheckResultBuilder builder)
    {
        final ConfigPropertySet propertySet = config.getApplicationProperties();
        if (options.checkEnabled(CHECKID_INDEX_ENABLED) && !propertySet.getBooleanPropertyDefault(PROP_INDEX_OPTION, false))
        {
            builder.warning("File does not have indexing enabled.", CHECKID_INDEX_ENABLED);
        }

        if (options.checkEnabled(CHECKID_INDEX_HOME) && !propertySet.getBooleanPropertyDefault(PROP_INDEX_DEFAULT_DIR, false))
        {
            final String directory = propertySet.getStringPropertyDefault(PROP_INDEX_PATH, "<nothing>");
            builder.error("File has '" + directory + "' configured as its index path. It should be using its JIRA.HOME.", CHECKID_INDEX_HOME);
        }

        if (options.checkEnabled(CHECKID_INDEX_DIRECTORY) && propertySet.contains(PROP_INDEX_PATH))
        {
            final String directory = propertySet.getStringPropertyDefault(PROP_INDEX_PATH, "<nothing>");
            if (!FUNC_TEST_INDEX.equals(directory))
            {
                builder.warning("File has '" + directory + "' configured as its index path. It should be using '"
                        + FUNC_TEST_INDEX + "' even with JIRA.HOME configured.", CHECKID_INDEX_DIRECTORY);
            }
        }
    }

    public void fixConfiguration(final JiraConfig config, final CheckOptions options)
    {
        final ConfigPropertySet propertySet = config.getApplicationProperties();

        if (options.checkEnabled(CHECKID_INDEX_ENABLED))
        {
            propertySet.setBooleanProperty(PROP_INDEX_OPTION, true);
        }

        if (options.checkEnabled(CHECKID_INDEX_HOME))
        {
            propertySet.setBooleanProperty(PROP_INDEX_DEFAULT_DIR, true);
        }

        if (options.checkEnabled(CHECKID_INDEX_DIRECTORY))
        {
            String index = propertySet.getStringProperty(PROP_INDEX_PATH);
            if (index != null)
            {
                propertySet.setStringProperty(PROP_INDEX_PATH, FUNC_TEST_INDEX);
            }
        }
    }

}
