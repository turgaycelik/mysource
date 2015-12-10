/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.config.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.PathUtils;

import java.io.File;

/**
 * Responsible for determining the current location of JIRA indexes.
 */
@PublicApi
public interface IndexPathManager
{
    public static final String INDEXES_DIR = PathUtils.joinPaths(JiraHome.CACHES, "indexes");
    
    static final class Directory
    {
        public static final String ISSUES_SUBDIR = "issues";
        public static final String COMMENTS_SUBDIR = "comments";
        public static final String PLUGINS_SUBDIR = "plugins";
        public static final String ENTITIES_SUBDIR = "entities";
        public static final String CHANGE_HISTORY_SUBDIR = "changes";
    }

    /**
     * Returns the root path of JIRA's indexes.
     * If JIRA is configured to "Use the Default Index Directory", then the absolute path of that default directory is returned.
     *
     * @return the root path of JIRA's indexes
     */
    String getIndexRootPath();

    /**
     * This returns the root index directory that JIRA will use by default, if it is configured to do so.
     * This directory is a sub-directory of the configured jira-home and is defined in the Atlassian home directory
     * specification to live under [jira-home]/caches/indexes/.
     *
     * @return the default root index path
     */
    String getDefaultIndexRootPath();

    /**
     * Returns the path of JIRA's issue indexes.
     * @return the path of JIRA's issue indexes
     */
    String getIssueIndexPath();

    /**
     * Returns the path of JIRA's comment indexes.
     * @return the path of JIRA's comment indexes
     */
    String getCommentIndexPath();

    /**
     * Returns the path of JIRA's change history indexes.
     * @return the path of JIRA's change history indexes
     */
    String getChangeHistoryIndexPath();

    /**
     * Returns the root path of JIRA's plugin indexes.
     * <p>
     * NOTE: Each Plugin should create a new directory under this path
     * @return the root path of JIRA's plugin indexes
     */
    String getPluginIndexRootPath();

    /**
     * Returns the path of JIRA's shared entity indexes.
     * @return the path of JIRA's shared entity indexes
     */
    String getSharedEntityIndexPath();

    /**
     * Specify an explicit index root path.
     * @param indexPath the path to use
     */
    void setIndexRootPath(String indexPath);

    /**
     * Specify that the default location within JIRA Home should be used to store indexes.
     */
    void setUseDefaultDirectory();

    /**
     * Whether we are using the default index directory (within JIRA Home).
     * @return true if we are using the default index directory (within JIRA Home).
     */
    boolean getUseDefaultDirectory();

    public static enum Mode
    {
        DEFAULT, CUSTOM, DISABLED
    }
    Mode getMode();

    /**
     * Implementation of {@link IndexPathManager} that uses the {@link ApplicationProperties} to get the current paths.
     * Nothing outside of this class should ever refer to the JIRA_PATH_INDEX ApplicationProperty. That property
     * needs to be interpreted properly when using JIRA Home.
     */
    public class PropertiesAdaptor implements IndexPathManager
    {
        private final ApplicationProperties applicationProperties;
        private final JiraHome jiraHome;

        public PropertiesAdaptor(final ApplicationProperties applicationProperties, final JiraHome jiraHome)
        {
            this.applicationProperties = applicationProperties;
            this.jiraHome = jiraHome;
        }

        public String getIndexRootPath()
        {
            if(applicationProperties.getOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY))
            {
                return getDefaultIndexRootPath();
            }
            else
            {
                return applicationProperties.getString(APKeys.JIRA_PATH_INDEX);
            }
        }

        public String getDefaultIndexRootPath()
        {
            // http://extranet.atlassian.com/display/DEV/Product+home+directories+specification
            return new File(jiraHome.getLocalHomePath(), INDEXES_DIR).getAbsolutePath();
        }

        public void setIndexRootPath(final String rootPath)
        {
            applicationProperties.setString(APKeys.JIRA_PATH_INDEX, rootPath);
            applicationProperties.setOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY,  Boolean.FALSE);
        }

        public void setUseDefaultDirectory()
        {
            applicationProperties.setOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY,  Boolean.TRUE);
        }

        public boolean getUseDefaultDirectory()
        {
            return applicationProperties.getOption(APKeys.JIRA_PATH_INDEX_USE_DEFAULT_DIRECTORY);
        }

        public Mode getMode()
        {
            if (getUseDefaultDirectory())
            {
                return Mode.DEFAULT;
            }
            else
            {
                return Mode.CUSTOM;
            }
        }

        public String getIssueIndexPath()
        {
            return PathUtils.joinPaths(getIndexRootPath(), Directory.ISSUES_SUBDIR);
        }

        public String getCommentIndexPath()
        {
            return PathUtils.joinPaths(getIndexRootPath(), Directory.COMMENTS_SUBDIR);
        }

        public String getChangeHistoryIndexPath()
        {
            return PathUtils.joinPaths(getIndexRootPath(), Directory.CHANGE_HISTORY_SUBDIR);
        }

        public String getSharedEntityIndexPath()
        {
            return PathUtils.joinPaths(getIndexRootPath(), Directory.ENTITIES_SUBDIR);
        }

        public String getPluginIndexRootPath()
        {
            return PathUtils.joinPaths(getIndexRootPath(), Directory.PLUGINS_SUBDIR);
        }
    }
}