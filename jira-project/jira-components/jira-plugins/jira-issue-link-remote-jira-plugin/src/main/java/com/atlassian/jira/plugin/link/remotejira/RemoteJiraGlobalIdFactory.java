package com.atlassian.jira.plugin.link.remotejira;

/**
 * Factory for encoding and decoding {@link RemoteJiraGlobalId}s.
 *
 * @since v5.0
 */
public interface RemoteJiraGlobalIdFactory
{
    RemoteJiraGlobalId decode(String globalId);
}
