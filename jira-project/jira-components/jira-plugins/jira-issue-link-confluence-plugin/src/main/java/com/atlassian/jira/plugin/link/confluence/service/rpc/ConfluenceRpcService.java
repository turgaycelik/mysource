package com.atlassian.jira.plugin.link.confluence.service.rpc;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.confluence.ConfluencePage;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSearchResult;
import com.atlassian.jira.plugin.link.confluence.ConfluenceSpace;
import com.atlassian.sal.api.net.ResponseException;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Helper class for making XMLRPC calls to Confluence servers.
 *
 * @since v5.0
 */
public interface ConfluenceRpcService
{
    /**
     * Gets the page with the given id, on the Confluence server given by the application link.
     *
     * @param applicationLink the application link of the Confluence server
     * @param pageId the page id
     * @return a {@link RemoteResponse} containing a {@link ConfluencePage} and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    public RemoteResponse<ConfluencePage> getPage(ApplicationLink applicationLink, String pageId) throws CredentialsRequiredException, ResponseException;

    /**
     * Get the list of spaces for the Confluence instance matching the given ApplicationLink.
     *
     * @param applicationLink the link to the Confluence instance
     * @return a {@link RemoteResponse} containing a List of {@link ConfluenceSpace}s and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    RemoteResponse<List<ConfluenceSpace>> getSpaces(ApplicationLink applicationLink) throws CredentialsRequiredException, ResponseException;

    /**
     * Search the Confluence instance matching the given ApplicationLink.
     *
     * @param applicationLink the link to the Confluence instance
     * @param query the search term
     * @param maxResults the maximum number of results to return
     * @param spaceKey the space to search within, or if null, search all spaces
     * @return a {@link RemoteResponse} containing a List of {@link ConfluenceSearchResult}s and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    RemoteResponse<List<ConfluenceSearchResult>> search(ApplicationLink applicationLink, String query, int maxResults, @Nullable String spaceKey) throws CredentialsRequiredException, ResponseException;
}
