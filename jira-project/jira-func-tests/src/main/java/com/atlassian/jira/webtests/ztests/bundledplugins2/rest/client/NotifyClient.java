package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import static javax.ws.rs.core.MediaType.*;

public class NotifyClient extends RestApiClient<NotifyClient>
{
    public NotifyClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public Response postResponse(final String issueKey, final Notification notification)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return notifyForIssueWithKey(issueKey).type(APPLICATION_JSON_TYPE).post(ClientResponse.class, notification);
            }
        });
    }

    private WebResource notifyForIssueWithKey(String issueKey)
    {
        return createResource().path("issue").path(issueKey).path("notify");
    }
}
