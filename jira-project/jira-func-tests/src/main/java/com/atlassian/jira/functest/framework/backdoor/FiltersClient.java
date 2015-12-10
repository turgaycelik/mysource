package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

import java.util.ArrayList;
import java.util.List;

public class FiltersClient extends BackdoorControl<FiltersClient>
{
    public FiltersClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    @Override
    protected WebResource createResource()
    {
        return resourceRoot(rootPath).path("rest").path("func-test").path("1.0");
    }

    public String createFilter(String jql, String name)
    {
        return get(createResource().path("filter")
                .path("create")
                .queryParam("jql", jql)
                .queryParam("name", name)
        );
    }

    public String createFilter(String jql, String name, String owner, String permission)
    {
        return get(createResource().path("filter")
                .path("create")
                .queryParam("jql", jql)
                .queryParam("name", name)
                .queryParam("owner", owner)
                .queryParam("groupPermission", permission)
        );
    }

    public String createFilter(String jql, String name, boolean isFavourite)
    {
        return get(createResource().path("filter")
                .path("create")
                .queryParam("jql", jql)
                .queryParam("name", name)
                .queryParam("isFavourite", Boolean.toString(isFavourite))
        );
    }

    public String getFilterJql(long filter)
    {
        return get(createResource().path("filter")
                .path("jql")
                .queryParam("id", "" + filter));

    }

    public FilterInfo getFilter(long filter)
    {
        return createResource().path("filter")
               .queryParam("id", "" + filter)
                .get(FilterInfo.class);
    }

    public List<String> getColumnsForFilter(String id)
    {
        return createResource().path("filter").path("columns").queryParam("id", id).get(ArrayList.class);
    }
}
