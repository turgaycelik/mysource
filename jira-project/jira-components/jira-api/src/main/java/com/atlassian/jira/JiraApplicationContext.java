package com.atlassian.jira;

/**
 * High level component that represents the single instance of JIRA.
 *
 * @since v3.13
 */
public interface JiraApplicationContext
{
    /**
     * Generates a fingerprint which should make reasonable attempts at uniqueness across real instances of JIRA.
     * Its content should not betray any sensitive data about the instance. Must not return null.
     * @return a some hash or other magic String that can be used to distinguish instances of JIRA.
     */
    String getFingerPrint();
}
