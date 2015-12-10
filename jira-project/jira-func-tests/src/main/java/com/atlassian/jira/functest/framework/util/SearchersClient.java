package com.atlassian.jira.functest.framework.util;


import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

public class SearchersClient extends RestApiClient<SearchersClient>
{
    private final JIRAEnvironmentData environmentData;

    public SearchersClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        this.environmentData = environmentData;
    }

    public List<Searcher> allSearchers(String jqlContext)
    {
        return asList(getSearchers(jqlContext).searchers);
    }

    private List<Searcher> asList(Searchers searchers)
    {
        List<Searcher> set = Lists.newArrayList();
        for (FilteredSearcherGroup group : searchers.getGroups())
        {
            for (Searcher searcher : group.getSearchers())
            {
                set.add(searcher);
            }
        }
        return set;
    }

    public SearchResults getSearchersBasic(final String ... params)
    {
        return basic(params).get(SearchResults.class);
    }

    public SearchResults getSearchers(final String jqlContext)
    {
        return searchers(jqlContext).get(SearchResults.class);
    }

    public Response getSearchersResponse(final String jqlContext)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return searchers(jqlContext).get(ClientResponse.class);
            }
        }, String.class);
    }

    private WebResource basic(final String ... params)
    {
        WebResource resource = createResource().path("QueryComponent!Default.jspa").queryParam("decorator", "none");
        for (int i = 0; i < params.length; i += 2)
        {
            resource = resource.queryParam(params[i], params[i + 1]);
        }
        return resource;
    }

    private WebResource searchers(final String jqlContext)
    {
        WebResource resource = createResource().path("secure/QueryComponent!Jql.jspa").queryParam("decorator", "none");
        if (null != jqlContext)
        {
            resource = resource.queryParam("jql", jqlContext);
        }
        return resource;
    }

    protected WebResource createResource()
    {
        return resourceRoot(environmentData.getBaseUrl().toExternalForm());
    }
}