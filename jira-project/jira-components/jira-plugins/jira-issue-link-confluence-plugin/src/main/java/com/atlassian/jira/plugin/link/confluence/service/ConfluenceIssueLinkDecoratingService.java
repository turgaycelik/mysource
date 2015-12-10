package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.link.RemoteIssueLink;

import java.io.IOException;

/**
 * This service decorates {@link RemoteIssueLink}s to Confluence instances so that they have current information.
 *
 * @since v5.0
 */
public interface ConfluenceIssueLinkDecoratingService
{
    RemoteIssueLink decorate(RemoteIssueLink remoteIssueLink) throws CredentialsRequiredException, IOException, PermissionException;
}