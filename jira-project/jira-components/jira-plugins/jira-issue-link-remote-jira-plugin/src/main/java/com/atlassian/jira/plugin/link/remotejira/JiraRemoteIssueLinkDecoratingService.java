package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.link.RemoteIssueLink;

import java.io.IOException;

/**
 * This service decorates {@link com.atlassian.jira.issue.link.RemoteIssueLink}s to remote JIRA instances so that they have current information,
 * including priority and status.
 *
 * @since v5.0
 */
public interface JiraRemoteIssueLinkDecoratingService
{
    RemoteIssueLink decorate(RemoteIssueLink remoteIssueLink)
            throws CredentialsRequiredException, IOException, PermissionException;
}