package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.sal.api.net.ResponseException;

/**
 * A Service for querying Confluence instances to get page info.
 *
 * @since v5.0
 */
public interface ConfluencePageService
{
    /**
     * Get the pageId matching the given pageUrl.
     *
     * @param applicationLink the link to the Confluence instance
     * @param pageUrl the url of the page
     * @return the id of the page
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    RemoteResponse<String> getPageId(ApplicationLink applicationLink, String pageUrl) throws CredentialsRequiredException, ResponseException;
}
