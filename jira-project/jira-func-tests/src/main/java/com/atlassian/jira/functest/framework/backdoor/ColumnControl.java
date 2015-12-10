package com.atlassian.jira.functest.framework.backdoor;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Use this backdoor to configure columns for users and/or filters for func/WD tests
 *
 * This includes getting the currently selected columns for users or filters and set those columns as well given a list.
 *
 * @since v6.1
 */
public class ColumnControl extends BackdoorControl<ColumnControl>
{
    public ColumnControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<ColumnItem> getLoggedInUserColumns()
    {
        return getUserColumns(null);
    }

    public boolean setLoggedInUserColumns(List<String> columnIds)
    {
        return setUserColumns(null, columnIds);
    }

    public boolean addLoggedInUserColumns(List<String> columnIds)
    {
        return addUserColumns(null, columnIds);
    }

    public boolean restoreLoggedInUserColumns()
    {
        return restoreUserColumns(null);
    }


    public List<ColumnItem> getUserColumns(String username)
    {
        WebResource resource = createResource().path("user/columns");
        if (username != null)
        {
            resource = resource.queryParam("username", username);
        }
        return resource.get(new GenericType<List<ColumnItem>>(){});
    }

    public boolean setUserColumns(String username, List<String> columnIds)
    {
        WebResource resource = createResource().path("user/columns");
        if (username != null)
        {
            resource = resource.queryParam("username", username);
        }

        MultivaluedMap formData = new MultivaluedMapImpl();
        for (String id : columnIds)
        {
            formData.add("columns", id);
        }

        ClientResponse result = resource.put(ClientResponse.class, formData);
        try
        {
            return result.getClientResponseStatus().getStatusCode() == 200;
        }
        finally
        {
            result.close();
        }
    }

    public boolean addUserColumns(String username, List<String> columnIds)
    {
        return setUserColumns(username, addColumns(getUserColumns(username), columnIds));
    }

    public boolean restoreUserColumns(String username)
    {
        WebResource resource = createResource().path("user/columns");
        if (username != null)
        {
            resource = resource.queryParam("username", username);
        }

        ClientResponse result = resource.delete(ClientResponse.class);
        try
        {
            return result.getClientResponseStatus().getStatusCode() == 200;
        }
        finally
        {
            result.close();
        }
    }


    public List<ColumnItem> getFilterColumns(String filterId)
    {
        WebResource resource = createResource().path("filter/" + filterId + "/columns");
        return resource.get(new GenericType<List<ColumnItem>>(){});
    }

    public boolean setFilterColumns(String filterId, List<String> columnIds)
    {
        WebResource resource = createResource().path("filter/" + filterId + "/columns");

        MultivaluedMap formData = new MultivaluedMapImpl();
        for (String id : columnIds)
        {
            formData.add("columns", id);
        }

        ClientResponse result = resource.put(ClientResponse.class, formData);
        try
        {
            return result.getClientResponseStatus().getStatusCode() == 200;
        }
        finally
        {
            result.close();
        }
    }

    public List<ColumnItem> getSystemDefaultColumns()
    {
        WebResource resource = createResource().path("settings/columns");
        return resource.get(new GenericType<List<ColumnItem>>(){});
    }

    public boolean setSystemDefaultColumns(List<String> columnIds)
    {
        WebResource resource = createResource().path("settings/columns");

        MultivaluedMap formData = new MultivaluedMapImpl();
        for (String id : columnIds)
        {
            formData.add("columns", id);
        }

        ClientResponse result = resource.put(ClientResponse.class, formData);
        try
        {
            return result.getClientResponseStatus().getStatusCode() == 200;
        }
        finally
        {
            result.close();
        }
    }

    private List<String> addColumns(List<ColumnItem> originalColumns, List<String> newColumns)
    {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        for (ColumnItem originalColumn : originalColumns)
        {
            builder.add(originalColumn.value);
        }
        builder.addAll(newColumns);

        return builder.build();
    }


    @Override
    protected WebResource createResource()
    {
        return resourceRoot(rootPath).path("rest").path("api").path("2");
    }

    /**
     * Transfer object for a single column.
     */
    @XmlRootElement
    public static class ColumnItem
    {
        @XmlElement
        public String label;
        @XmlElement
        public String value;

        public ColumnItem(String value, String label)
        {
            this.label = label;
            this.value = value;
        }

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        ColumnItem()
        {}
    }
}
