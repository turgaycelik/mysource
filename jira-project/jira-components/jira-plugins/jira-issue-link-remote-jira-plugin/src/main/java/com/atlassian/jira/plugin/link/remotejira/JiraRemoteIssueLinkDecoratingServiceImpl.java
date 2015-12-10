package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BaseUrlSwapper;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * The default implementation of {@link JiraRemoteIssueLinkDecoratingService}.
 *
 * @since v5.0
 */
@Component
@ExportAsService
public class JiraRemoteIssueLinkDecoratingServiceImpl implements JiraRemoteIssueLinkDecoratingService
{
    private final RemoteJiraRestService remoteJiraRestService;
    private final RemoteJiraGlobalIdFactoryImpl remoteJiraGlobalIdFactory;
    private final JiraAuthenticationContext authContext;

    @Autowired
    public JiraRemoteIssueLinkDecoratingServiceImpl(
            final RemoteJiraRestService remoteJiraRestService,
            final RemoteJiraGlobalIdFactoryImpl remoteJiraGlobalIdFactory,
            @ComponentImport final JiraAuthenticationContext authContext)
    {
        this.remoteJiraRestService = remoteJiraRestService;
        this.remoteJiraGlobalIdFactory = remoteJiraGlobalIdFactory;
        this.authContext = authContext;
    }

    @Override
    public RemoteIssueLink decorate(final RemoteIssueLink remoteIssueLink)
            throws CredentialsRequiredException, IOException, PermissionException
    {
        if (!RemoteIssueLink.APPLICATION_TYPE_JIRA.equals(remoteIssueLink.getApplicationType()))
        {
            throw new IllegalArgumentException("Remote link is not to JIRA");
        }

        final RemoteJiraGlobalId remoteJiraGlobalId = remoteJiraGlobalIdFactory.decode(remoteIssueLink.getGlobalId());
        final ApplicationLink jiraAppLink = remoteJiraGlobalId.getApplicationLink();
        final String remoteIssueID = String.valueOf(remoteJiraGlobalId.getRemoteIssueId());
        try
        {
            final RemoteResponse<RemoteJiraIssue> response = remoteJiraRestService.getIssue(jiraAppLink, remoteIssueID, RemoteJiraRestService.RestVersion.VERSION_2);
            return handleJiraResponse(response, remoteIssueLink, jiraAppLink);
        }
        catch (final ResponseException exception)
        {
            throw new IOException("Failed to load JIRA issue from remote server", exception);
        }
        // TODO: Retrieval of non-AppLinked issues was here.  See r163408.
    }

    private RemoteIssueLink handleJiraResponse(final RemoteResponse<RemoteJiraIssue> response, final RemoteIssueLink remoteIssueLink, ApplicationLink jiraAppLink)
            throws ResponseException, CredentialsRequiredException, PermissionException
    {
        if (!response.isSuccessful())
        {
            switch (response.getStatusCode())
            {
                case 401:  // Unauthorised
                {
                    if (authContext.getLoggedInUser() != null)
                    {
                        // We have a logged in user, their OAuth token must have expired
                        throw new CredentialsRequiredException(jiraAppLink.createAuthenticatedRequestFactory(), "Token expired");
                    }
                    else
                    {
                        // No permission has an anonymous user, they will need to login if they want to see more
                        throw new PermissionException();
                    }
                }
                case 403:  // Forbidden
                    throw new PermissionException();
                default:
                    throw new ResponseException(response.getStatusText());
            }
        }

        final RemoteJiraIssue remoteJiraIssue = response.getEntity();
        return new RemoteIssueLink(
                remoteIssueLink.getId(),
                remoteIssueLink.getIssueId(),
                remoteIssueLink.getGlobalId(),
                remoteJiraIssue.getKey(),
                remoteJiraIssue.getSummary(),
                remoteJiraIssue.getBrowseUrl(),
                BaseUrlSwapper.swapRpcUrlToDisplayUrl(remoteJiraIssue.getIconUrl(), jiraAppLink),
                remoteJiraIssue.getIconTitle(),
                remoteIssueLink.getRelationship(),
                remoteJiraIssue.isResolved(),
                BaseUrlSwapper.swapRpcUrlToDisplayUrl(remoteJiraIssue.getStatusIconUrl(), jiraAppLink),
                remoteJiraIssue.getStatusIconTitle(),
                BaseUrlSwapper.swapRpcUrlToDisplayUrl(remoteIssueLink.getStatusIconLink(), jiraAppLink),
                remoteIssueLink.getApplicationType(),
                jiraAppLink.getName(),
                remoteJiraIssue.getStatusName(),
                remoteJiraIssue.getStatusDescription(),
                remoteJiraIssue.getStatusCategoryKey(),
                remoteJiraIssue.getStatusCategoryColorName());
    }
}
