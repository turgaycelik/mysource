package com.atlassian.jira.functest.framework.backdoor;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

public class FilterSubscriptionControl extends BackdoorControl<FilterSubscriptionControl>
{
    public FilterSubscriptionControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public void addSubscription(Long filterId, String groupName, String expr, Boolean emailOnEmpty)
    {
        WebResource resource = createResource().path("filterSubscription")
                .queryParam("filterId", filterId.toString())
                .queryParam("expr", expr)
                .queryParam("emailOnEmpty", emailOnEmpty.toString());
        if (groupName != null)
        {
            resource = resource.queryParam("groupName", groupName);
        }
        resource.post();
    }

    public Map get(Long id)
    {
        WebResource resource = createResource().path("filterSubscription")
                .path(id.toString());

        return resource.get(HashMap.class);
    }

    public String getCronForSubscription(Long id)
    {
        WebResource resource = createResource().path("filterSubscription")
                .path(id.toString()).path("cron");

        return resource.get(String.class);
    }
}
