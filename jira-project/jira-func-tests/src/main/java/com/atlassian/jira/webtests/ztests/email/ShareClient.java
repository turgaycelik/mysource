package com.atlassian.jira.webtests.ztests.email;

import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nullable;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * A REST client used to talk to the share e-mail rest resource.
 *
 * @since v5.0
 */
public class ShareClient extends RestApiClient
{
    static class ShareBean
    {
        public Set<String> usernames;
        public Set<String> emails;
        public String message;
        public String jql;

        public ShareBean(Set<String> usernames, Set<String> emails, String message, String jql)
        {
            this.usernames = usernames;
            this.emails = emails;
            this.message = message;
            this.jql = jql;
        }
    }

    private final JIRAEnvironmentData environmentData;

    public ShareClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        this.environmentData = environmentData;
    }

    ShareClient(JIRAEnvironmentData environmentData, String version)
    {
        super(environmentData, version);
        this.environmentData = environmentData;
    }

    /**
     * Shares an issue with the given key with the users and email addresses specified.
     *
     *
     * @param issueKey the issue key of the issue to share
     * @param usernames JIRA users to share with
     * @param emails email addresses to share with
     * @param message A message to send with the share email
     * @return a Response
     */
    public Response shareIssue(final String issueKey, final Set<String> usernames,
            @Nullable final Set<String> emails, final String message)
    {
        ShareBean bean = new ShareBean(usernames, emails, message, null);
        return postToShareResource("issue/" + issueKey, bean);
    }

    /**
     * Shares a saved search (filter) with the given id with the users and email addresses specified.
     *
     *
     * @param searchRequestId the id of the saved SearchRequest to share
     * @param usernames JIRA users to share with
     * @param emails email addresses to share with
     * @param message A message to send with the share email
     * @return a Response
     */
    public Response shareSavedSearch(final String searchRequestId, final Set<String> usernames,
            @Nullable final Set<String> emails, final String message)
    {
        ShareBean bean = new ShareBean(usernames, emails, message, null);
        return postToShareResource("filter/" + searchRequestId, bean);
    }

    /**
     * Shares a search query with the users and email addresses specified.
     *
     *
     * @param jql the jql query to share
     * @param usernames JIRA users to share with
     * @param emails email addresses to share with
     * @param message A message to send with the share email
     * @return a Response
     */
    public Response shareSearchQuery(final String jql, final Set<String> usernames,
            @Nullable final Set<String> emails, final String message)
    {
        ShareBean bean = new ShareBean(usernames, emails, message, jql);
        return postToShareResource("search", bean);
    }

    private Response postToShareResource(final String path, final ShareBean shareBean)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                WebResource shareResource = getShareResource().path(path);
                return shareResource.type(APPLICATION_JSON_TYPE).post(ClientResponse.class, shareBean);
            }
        });
    }

    private WebResource getShareResource()
    {
        return resourceRoot(environmentData.getBaseUrl().toExternalForm()).path("rest").path("share").path("1.0");
    }
}
