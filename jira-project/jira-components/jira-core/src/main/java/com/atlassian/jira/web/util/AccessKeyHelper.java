package com.atlassian.jira.web.util;

/**
 * Helper interface for access keys for web browsers.
 *
 * @since v4.0
 */
public interface AccessKeyHelper
{
    /**
     * Screens the given access key against known browser shortcuts, so that the access key can be selectively omitted.
     *
     * @param accessKey the access key to screen
     * @return true if the access key does not clash with any browser shortcut we know about; false otherwise
     */
    boolean isAccessKeySafe(String accessKey);
}
