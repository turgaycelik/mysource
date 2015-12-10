package com.atlassian.jira.mention;

/**
 * Given string content this class will return a set of usernames that were found in the content prefixed with
 *
 * @since 5.0
 */
public interface MentionFinder
{
    /**
     * Get all usernames mentioned in the content
     *
     * @param content the content
     * @return the usernames that were mentioned
     */
    Iterable<String> getMentionedUsernames(String content);
}
