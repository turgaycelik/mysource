package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import java.util.List;
import javax.ws.rs.core.MultivaluedMap;

public class IssuesControl extends BackdoorControl<IssuesControl>
{
    public static final String LIST_VIEW_LAYOUT = "list-view";

    public IssuesControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public String getIssueKeyForSummary(String summary)
    {
        return get(createResource().path("issues/issueKeyForSummary")
                .queryParam("summary", summary));

    }

    public String getIssueIdByCurrentKey(String key)
    {
        return get(createResource().path("issues/issueIdByKey")
                .queryParam("key", key));

    }

    public String getExportOptions(String jql, String filterId, String modified)
    {
        MultivaluedMap params = new MultivaluedMapImpl();
        if (jql != null)
        {
            params.add("jql", jql);
        }
        if (filterId != null)
        {
            params.add("filterId", filterId);
        }
        if (modified != null)
        {
            params.add("modified", modified);
        }
        WebResource resource = resourceRoot(rootPath).path("rest/issueNav/1/issueNav/operations/views");
        return resource
                .header("X-Atlassian-Token", "nocheck")
                .post(String.class, params);
    }

    /**
     * Set a user's preferred search layout (detail or list view).
     *
     * @param layoutKey A layout key (see {@link #LIST_VIEW_LAYOUT}).
     * @param username The user whose preferred search layout is to be set.
     */
    public void setPreferredSearchLayout(String layoutKey, String username)
    {
        loginAs(username);
        WebResource resource = resourceRoot(rootPath)
                .path("rest/issueNav/latest/preferredSearchLayout");

        MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
        parameters.add("layoutKey", layoutKey);
        resource.header("X-Atlassian-Token", "nocheck").post(parameters);
    }

    public void touch(String key)
    {
        put(createResource().path("issues/touch").queryParam("key", key));
    }

    private void put(final WebResource webResource)
    {
        webResource.put();
    }

    public List<JsonNode> getHistoryMetadata(String issueKey) {
        WebResource resource = resourceRoot(rootPath).path("rest/api/2/issue/" + issueKey).queryParam("expand", "changelog");
        final JsonNode issue = resource.get(JsonNode.class);
        final ArrayNode histories = (ArrayNode) issue.get("changelog").get("histories");
        return ImmutableList.copyOf(Iterators.transform(histories.getElements(), new Function<JsonNode, JsonNode>()
        {
            @Override
            public JsonNode apply(JsonNode input)
            {
                return input.get("historyMetadata");
            }
        }));
    }
}
