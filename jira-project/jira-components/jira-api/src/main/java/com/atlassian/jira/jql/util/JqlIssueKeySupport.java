package com.atlassian.jira.jql.util;

import com.atlassian.jira.util.InjectableComponent;

/**
 * Provide JQL with some helper functions when dealing with Issue Keys.
 *
 * @since v4.0
 */
@InjectableComponent
public interface JqlIssueKeySupport
{
    /**
     * Determines is the passed issue key is valid for JIRA. It does *NOT* determine if the issue actually
     * exists within JIRA.
     *
     * @param issueKey the issue key to validate. Null will be considered an invalid key.
     * @return true if the passed key is valid or false otherwise.
     */
    boolean isValidIssueKey(String issueKey);

    /**
     * Return the numeric part of the issue key. Assumes that the passed key is valid. The method will return
     * -1 on a parsing error, however, this cannot be used to determine if a key is valid as some invalid keys
     * may parse correctly. The {@link #isValidIssueKey(String)} method can be used to determine if a key is invalid
     * or not.
     *
     * @param issueKey the issue key to parse. A null key will return -1.
     * @return the numeric part of the key. Will return -1 to indicate a parsing error.
     */
    long parseKeyNum(String issueKey);

    /**
     * Return the project key part of the issue key. Assumes that the passed key is valid. The method will return
     * null on a parsing error, however, this cannot be used to determine if a key is valid as some invalid keys
     * may parrse correctly. The {@link #isValidIssueKey(String)} method can be used to determine if a key is invalid
     * or not.
     *
     * @param issueKey the issue key to parse. A null key will return null.
     * @return the project key part of the issue key, or null on a parsing error.
     */
    String parseProjectKey(String issueKey);
}
