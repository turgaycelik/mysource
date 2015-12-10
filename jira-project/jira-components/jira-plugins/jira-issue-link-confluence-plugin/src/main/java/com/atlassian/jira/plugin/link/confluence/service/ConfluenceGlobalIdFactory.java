package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.link.confluence.ConfluenceGlobalId;

/**
 * Factory for encoding and decoding {@link ConfluenceGlobalId}s.
 *
 * @since v5.0
 */
public interface ConfluenceGlobalIdFactory
{
    ConfluenceGlobalId create(RemoteIssueLink link);
}
