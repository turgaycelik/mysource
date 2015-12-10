package com.atlassian.jira.config.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.PathUtils;

/**
 * Responsible for determining the current location of JIRA attachments.
 * @since 4.0
 */
@PublicApi
public interface AttachmentPathManager
{
    // http://extranet.atlassian.com/display/DEV/Product+home+directories+specification
    public static final String ATTACHMENTS_DIR = PathUtils.joinPaths(JiraHome.DATA, "attachments");
    
    /**
     * Returns the attachment path that will be used by default if JIRA is so configured.
     * This will live as a subdirectory of jira-home.
     *
     * @return the attachment path that will be used by default if JIRA is so configured.
     */
    String getDefaultAttachmentPath();

    /**
     * Returns the path used to store attachments
     * @return the path used to store attachments
     */
    String getAttachmentPath();

    /**
     * Set a custom location to store attachments
     * @param indexPath path to store attachments
     */
    void setCustomAttachmentPath(String indexPath);

    /**
     * Use the default location within JIRA Home to store attachments
     */
    void setUseDefaultDirectory();

    /**
     * Returns true if attachments are stored within JIRA Home
     * @return true if attachments are stored within JIRA Home
     */
    boolean getUseDefaultDirectory();

    /**
     * Turns "Allow Attachments" to off.
     */
    void disableAttachments();

    public static enum Mode
    {
        DEFAULT, CUSTOM, DISABLED
    }

    Mode getMode();

    /**
     * Implementation of {@link AttachmentPathManager} that uses the {@link com.atlassian.jira.config.properties.ApplicationProperties} to get the current paths.
     * Nothing outside of this class should ever refer to the JIRA_PATH_ATTACHMENTS ApplicationProperty. That property
     * needs to be interpreted properly when using JIRA Home.
     */
    public class PropertiesAdaptor implements AttachmentPathManager
    {
        private final ApplicationProperties applicationProperties;
        private final JiraHome jiraHome;

        public PropertiesAdaptor(final ApplicationProperties applicationProperties, final JiraHome jiraHome)
        {
            this.applicationProperties = applicationProperties;
            this.jiraHome = jiraHome;
        }

        public String getDefaultAttachmentPath()
        {
            return PathUtils.appendFileSeparator(jiraHome.getHomePath()) + ATTACHMENTS_DIR;
        }

        public String getAttachmentPath()
        {
            switch (getMode())
            {
                case DISABLED:
                    // TODO: this is a temporary fix to get Project Import Func Tests to run.
                    // This is how the "old behaviour" worked, but it is not really correct, and can still return null anyway...
                    // Need to fix TestProjectImportUsersDoNotExistPage, and check all places that are calling here.
                    return applicationProperties.getString(APKeys.JIRA_PATH_ATTACHMENTS);
                case DEFAULT:
                    return getDefaultAttachmentPath();
                case CUSTOM:
                    return applicationProperties.getString(APKeys.JIRA_PATH_ATTACHMENTS);
                default:
                    throw new IllegalArgumentException("Unknown attachment mode " + getMode().name());
            }
        }

        public void setCustomAttachmentPath(final String path)
        {
            applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, path);
            applicationProperties.setOption(APKeys.JIRA_PATH_ATTACHMENTS_USE_DEFAULT_DIRECTORY,  Boolean.FALSE);
            applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        }

        public void setUseDefaultDirectory()
        {
            applicationProperties.setString(APKeys.JIRA_PATH_ATTACHMENTS, getDefaultAttachmentPath());
            applicationProperties.setOption(APKeys.JIRA_PATH_ATTACHMENTS_USE_DEFAULT_DIRECTORY,  Boolean.TRUE);
            applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        }

        public boolean getUseDefaultDirectory()
        {
            return getMode() == Mode.DEFAULT;
        }

        public void disableAttachments()
        {
            applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, false);
        }

        public Mode getMode()
        {
            if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS))
            {
                return Mode.DISABLED;
            }
            else
            {
                if (applicationProperties.getOption(APKeys.JIRA_PATH_ATTACHMENTS_USE_DEFAULT_DIRECTORY))
                {
                    return Mode.DEFAULT;
                }
                else
                {
                    return Mode.CUSTOM;
                }
            }
        }
    }
}
