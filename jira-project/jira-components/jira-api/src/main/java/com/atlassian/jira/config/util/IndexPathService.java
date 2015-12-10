package com.atlassian.jira.config.util;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;

/**
 * Service that provides access to JIRA's Lucene indexing paths.
 * Only System Administrators have sufficient privileges to see these as they are all host-OS file paths.
 *
 * @since v4.0
 * @see com.atlassian.jira.config.util.IndexPathManager
 */
@PublicApi
public interface IndexPathService
{
    /**
     * Returns the root path of JIRA's indexes.
     * @param serviceContext The JiraServiceContext
     * @return the root path of JIRA's indexes.
     */
    String getIndexRootPath(JiraServiceContext serviceContext);

    /**
     * Returns the path of JIRA's issue indexes.
     * @param serviceContext The JiraServiceContext
     * @return the path of JIRA's issue indexes.
     */
    String getIssueIndexPath(JiraServiceContext serviceContext);

    /**
     * Returns the path of JIRA's comment indexes.
     * @param serviceContext The JiraServiceContext
     * @return the path of JIRA's comment indexes.
     */
    String getCommentIndexPath(JiraServiceContext serviceContext);

    /**
     * Returns the root path of JIRA's plugin indexes.
     * <p> NOTE: Each Plugin should create a new directory under this path
     *
     * @param serviceContext The JiraServiceContext
     * @return the root path of JIRA's plugin indexes.
     */
    String getPluginIndexRootPath(JiraServiceContext serviceContext);

    /**
     * Returns the path of JIRA's shared entity indexes.
     * @param serviceContext The JiraServiceContext
     * @return the path of JIRA's shared entity indexes.
     */
    String getSharedEntityIndexPath(JiraServiceContext serviceContext);

    /**
     * Specify an explicit (custom) index root path.
     *
     * @param serviceContext The JiraServiceContext
     * @param indexPath the path to use
     */
    void setIndexRootPath(JiraServiceContext serviceContext, String indexPath);

    /**
     * Specify that the default location within JiraHome should be used to store indexes.
     * @param serviceContext The JiraServiceContext
     */
    void setUseDefaultDirectory(JiraServiceContext serviceContext);
}