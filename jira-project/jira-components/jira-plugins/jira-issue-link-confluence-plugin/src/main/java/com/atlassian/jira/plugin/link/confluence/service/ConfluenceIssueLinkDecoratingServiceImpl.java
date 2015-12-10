package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.confluence.ConfluenceGlobalId;
import com.atlassian.jira.plugin.link.confluence.ConfluencePage;
import com.atlassian.jira.plugin.link.confluence.service.rpc.ConfluenceRpcService;
import com.atlassian.jira.util.BaseUrlSwapper;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.sal.api.net.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * The default implementation of {@link ConfluenceIssueLinkDecoratingService}.
 *
 * @since v5.0
 */
@Component
public class ConfluenceIssueLinkDecoratingServiceImpl implements ConfluenceIssueLinkDecoratingService
{
    private static final String FAVICON_PATH = "/images/icons/favicon.png";
    private static final String CONFLUENCE_SRC = "src";

    private final ConfluenceRpcService confluenceRpcService;
    private final ConfluenceGlobalIdFactory confluenceGlobalIdFactory;

    @Autowired
    public ConfluenceIssueLinkDecoratingServiceImpl(
            final ConfluenceRpcService confluenceRpcService,
            final ConfluenceGlobalIdFactory confluenceGlobalIdFactory)
    {
        this.confluenceRpcService = confluenceRpcService;
        this.confluenceGlobalIdFactory = confluenceGlobalIdFactory;
    }

    @Override
    public RemoteIssueLink decorate(final RemoteIssueLink remoteIssueLink)
            throws CredentialsRequiredException, IOException, PermissionException
    {
        if (!RemoteIssueLink.APPLICATION_TYPE_CONFLUENCE.equals(remoteIssueLink.getApplicationType()))
        {
            throw new IllegalArgumentException("Remote link is not to Confluence");
        }

        final ConfluenceGlobalId globalId = confluenceGlobalIdFactory.create(remoteIssueLink);
        try
        {
            final RemoteResponse<ConfluencePage> response = confluenceRpcService.getPage(globalId.getApplicationLink(), globalId.getPageId());
            return handleResponse(response, remoteIssueLink, globalId.getApplicationLink());
        }
        catch (final ResponseException e)
        {
            throw new IOException("Failed to load Confluence Page from remote server", e);
        }
    }

    private static RemoteIssueLink handleResponse(final RemoteResponse<ConfluencePage> response, final RemoteIssueLink remoteIssueLink, final ApplicationLink appLink)
            throws ResponseException, CredentialsRequiredException, PermissionException
    {
        if (!response.isSuccessful())
        {
            switch (response.getStatusCode())
            {
                case 401: // Unauthorised
                {
                    throw new CredentialsRequiredException(appLink.createAuthenticatedRequestFactory(), "Token expired");
                }
                case 403: // Forbidden
                {
                    throw new ResponseException("Confluence remote API is disabled");
                }
                default:
                {
                    checkErrorMessages(response);
                    throw new ResponseException("Status Code: " + response.getStatusCode() +
                            ", Status Text: " + response.getStatusText() +
                            ", Errors: " + response.getErrors().getErrors() +
                            ", Error Messages: " + response.getErrors().getErrorMessages());
                }
            }
        }

        final String iconUrl = new UrlBuilder(appLink.getDisplayUrl().toASCIIString())
                .addPathUnsafe(FAVICON_PATH)
                .asUrlString();

        final ConfluencePage confluencePage = response.getEntity();
        return new RemoteIssueLinkBuilder(remoteIssueLink)
                .url(buildPageUrlWithSrc(BaseUrlSwapper.swapRpcUrlToDisplayUrl(confluencePage.getUrl(), appLink), "jira"))
                .title(confluencePage.getTitle())
                .iconUrl(iconUrl)
                .applicationName(appLink.getName())
                .build();
    }

    private static void checkErrorMessages(final RemoteResponse<?> response)
            throws PermissionException
    {
        // Parse the error messages and throw an appropriate exception if we need to
        // This is not ideal, but the Confluence Remote API does not give us much choice!
        if (response.containsErrorWithText("NotPermittedException"))
        {
            throw new PermissionException();
        }
    }

    private static String buildPageUrlWithSrc(String pageUrl, String param)
    {
        return new UrlBuilder(pageUrl).addParameter(CONFLUENCE_SRC, param).asUrlString();
    }
}
