package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.config.ps.ConfigPropertySet;

/**
 * A check to make sure that attachments is correctly configured in JIRA XML data.
 *
 * @since v4.0
 */
final public class AttachmentDirectoryChecker implements ConfigurationCheck
{
    /**
     * Check to see if the attachment directory has been configured.
     */
    public static final String CHECKID_ATTACH_HOME = "attachhome";

    /**
     * Check to set the attachment path to a default of {@link #JIRA_FUNC_ATTACHMENT}.
     */
    public static final String CHECKID_ATTACH_DIR = "attachdirectory";

    /**
     * Property in JIRA that tells us if attachments are enabled.
     */
    private final static String PROP_ATTACH_ENABLED = "jira.option.allowattachments";

    /**
     * JIRA property path to the attachments.
     */
    private final static String PROP_ATTACH_PATH = "jira.path.attachments";

    /**
     * JIRA property that indicates whether or not the default attachments directory is being used.
     */
    private final static String PROP_ATTACH_DEFAULT_DIR = "jira.path.attachments.use.default.directory";

    /**
     * Default value for the attachments directory.
     */
    private static final String JIRA_FUNC_ATTACHMENT = "func_test_attachments";

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

        //We always do these checks. Attachments can be enabled through the tests, so it is better to have them set
        //correctly.

        //Check to see that we are using the default.
        final String directory = propertySet.getStringPropertyDefault(PROP_ATTACH_PATH, "<nothing>");
        if (options.checkEnabled(CHECKID_ATTACH_HOME) && !propertySet.getBooleanPropertyDefault(PROP_ATTACH_DEFAULT_DIR, false))
        {
            if (propertySet.getBooleanPropertyDefault(PROP_ATTACH_ENABLED, false))
            {
                //only make this an error if the attachment are enabled.
                builder.error("File has '" + directory + "' configured as its attachment path. It should be using its JIRA.HOME.", CHECKID_ATTACH_HOME);
            }
            else
            {
                builder.warning("File has '" + directory + "' configured as its attachment path. It should be using its JIRA.HOME.", CHECKID_ATTACH_HOME);
            }
        }

        if (options.checkEnabled(CHECKID_ATTACH_DIR) && propertySet.contains(PROP_ATTACH_PATH) && !JIRA_FUNC_ATTACHMENT.equals(directory))
        {
            builder.warning(String.format("File has '%s' configured as it attachment path. It should always be set to '%s' even when using JIRA.HOME.",
                    directory, JIRA_FUNC_ATTACHMENT), CHECKID_ATTACH_DIR);
        }
    }

    public void fixConfiguration(final JiraConfig config, final CheckOptions options)
    {
        final ConfigPropertySet propertySet = config.getApplicationProperties();

        //Make sure JIRA is using the default attachment directory.
        if (options.checkEnabled(CHECKID_ATTACH_HOME))
        {
            propertySet.setBooleanProperty(PROP_ATTACH_DEFAULT_DIR, true);
        }

        //Make sure the JIRA attachment directory is set.
        if (options.checkEnabled(CHECKID_ATTACH_DIR) && propertySet.contains(PROP_ATTACH_PATH))
        {
            propertySet.setStringProperty(PROP_ATTACH_PATH, JIRA_FUNC_ATTACHMENT);
        }
    }
}
