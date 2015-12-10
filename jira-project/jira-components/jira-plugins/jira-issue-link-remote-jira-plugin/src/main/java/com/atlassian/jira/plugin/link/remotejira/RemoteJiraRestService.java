package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.sal.api.net.ResponseException;

import java.util.Map;

/**
 * Helper class for making REST calls to JIRA servers.
 *
 * @since v5.0
 */
public interface RemoteJiraRestService
{
    public enum RestVersion
    {
        VERSION_2_0alpha1("2.0alpha1"),
        VERSION_2("2"),
        VERSION_1("1.0");

        private final String s;

        RestVersion(final String s)
        {
            this.s = s;
        }

        @Override
        public String toString()
        {
            return s;
        }
    }

    /**
     * Gets the issue with the given key, on the JIRA server given by the application link.
     *
     * @param applicationLink the application link of the JIRA server
     * @param issueIdOrKey the issue ID or key
     * @param restVersion the version of the REST API to invoke
     * @return an {@link RemoteResponse} containing a {@link RemoteJiraIssue} and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    public RemoteResponse<RemoteJiraIssue> getIssue(ApplicationLink applicationLink, String issueIdOrKey, RestVersion restVersion) throws CredentialsRequiredException, ResponseException;

    /**
     * Gets the issue with the given key, on the JIRA server given by the application link.
     *
     * @param baseUri the base URI of the JIRA instance
     * @param issueIdOrKey the issue ID or key
     * @param restVersion the version of the REST API to invoke
     * @return an {@link RemoteResponse} containing a {@link RemoteJiraIssue} and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    public RemoteResponse<RemoteJiraIssue> getIssue(String baseUri, String issueIdOrKey, RestVersion restVersion) throws CredentialsRequiredException, ResponseException;

    /**
     * Creates a remote issue link between the given remote issue and the given local issue.
     *
     * @param applicationLink the application link of the remote JIRA server
     * @param remoteIssueKey the issue key of the remote issue
     * @param issue the local issue
     * @param relationship the relationship for the link
     * @param restVersion the version of the REST API to invoke
     * @return an {@link RemoteResponse} containing a {@link JSONObject} and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    public RemoteResponse<JSONObject> createRemoteIssueLink(ApplicationLink applicationLink, String remoteIssueKey, Issue issue, String relationship, RestVersion restVersion) throws CredentialsRequiredException, ResponseException;

    /**
     * Request the given REST resource on the given JIRA server with the given params.
     *
     * @param applicationLink the application link of the JIRA server
     * @param resourcePath the path of the REST resource, e.g. issue
     * @param params the parameters to add to the request
     * @param restVersion the version of the REST API to invoke
     * @return an {@link RemoteResponse} containing the {@link String} response and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    public RemoteResponse<String> requestResource(ApplicationLink applicationLink, String resourcePath, Map<String, String> params, RestVersion restVersion) throws CredentialsRequiredException, ResponseException;

    /**
     * Request the given URL on the given JIRA server with the given params. The URL does not need to be a REST resource.
     *
     * @param applicationLink the application link of the JIRA server
     * @param url the relative URL to request, from the base URL of the applicationLink, e.g. secure/Dashboard.jspa
     * @param params the parameters to add to the request
     * @return an {@link RemoteResponse} containing the {@link String} response and properties of the HTTP response
     * @throws CredentialsRequiredException thrown if authentication is required
     * @throws ResponseException thrown if the response cannot be retrieved
     */
    public RemoteResponse<String> requestURL(ApplicationLink applicationLink, String url, Map<String, String> params) throws CredentialsRequiredException, ResponseException;
}
