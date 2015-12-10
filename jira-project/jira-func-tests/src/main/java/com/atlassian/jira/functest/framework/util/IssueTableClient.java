package com.atlassian.jira.functest.framework.util;

import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IssueTableClient extends RestApiClient<IssueTableClient>
{
    private final JIRAEnvironmentData environmentData;

    /**
     * Temp class to add default constructor to get response working TODO: add default constructor to
     * IssueTableServiceOutcome and remove this
     */
    @XmlRootElement
    public static class ClientIssueTableServiceOutcome
    {
        @XmlElement
        public IssueTable issueTable;

        @XmlElement
        public List<String> warnings;

        public ClientIssueTableServiceOutcome()
        {
        }

        public ClientIssueTableServiceOutcome(IssueTable issueTable, Collection<String> warnings)
        {
            this.issueTable = issueTable;
            this.warnings = new ArrayList<String>(warnings);
        }

        public IssueTable getIssueTable()
        {
            return issueTable;
        }

        /**
         * @return All warnings that were generated while processing the request, e.g. "The value 'foo' does not exist
         *         for the field 'reporter'."
         */
        public Collection<String> getWarnings()
        {
            return warnings;
        }
    }

    public IssueTableClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        this.environmentData = environmentData;
    }

    public ClientIssueTableServiceOutcome getIssueTable(final Long filterId)
    {
        return getIssueTable(filterId, null, null, null, true);
    }

    public ClientIssueTableServiceOutcome getIssueTable(final String jql)
    {
        return getIssueTable(null, jql, null, null, true);
    }

    public ClientIssueTableServiceOutcome setSessionSearch(final String jql)
    {
        return getIssueTable(null, jql, null, null, true);
    }

    public ClientIssueTableServiceOutcome getIssueTable(final Long filterId, final String jql, final String num, final Integer startIndex, final Boolean useUserColumns)
    {
        return createResource()
                .header("X-Atlassian-Token", "nocheck")
                .post(ClientIssueTableServiceOutcome.class, getPostData(filterId, jql, num, startIndex, useUserColumns));
    }


    public Response getResponse(final Long filterId, final String jql, final String num, final Integer startIndex, final Boolean useUserColumns)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource()
                        .header("X-Atlassian-Token", "nocheck")
                        .post(ClientResponse.class, getPostData(filterId, jql, num, startIndex, useUserColumns));
            }
        }, String.class);
    }

    public Response getResponse(final Long filterId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource()
                        .header("X-Atlassian-Token", "nocheck")
                        .post(ClientResponse.class, getPostData(filterId, null, null, null, true));
            }
        }, String.class);
    }


    private MultivaluedMap getPostData(final Long filterId, final String jql, final String num, final Integer startIndex, final Boolean useUserColumns)
    {
        String columnConfig = null;
        if (null != useUserColumns)
        {
            columnConfig = useUserColumns.booleanValue()?"user":"filter";
        }
        return getPostData(filterId, jql, num, startIndex, columnConfig);
    }

    private MultivaluedMap getPostData(final Long filterId, final String jql, final String num, final Integer startIndex, final String columnConfig)
    {
        MultivaluedMap formData = new MultivaluedMapImpl();

        if (null != filterId)
        {
            formData.add("filterId", filterId.toString());
        }
        if (null != jql)
        {
            formData.add("jql", jql);
        }
        if (null != num)
        {
            formData.add("num", num);
        }
        if (null != startIndex)
        {
            formData.add("startIndex", String.valueOf(startIndex));
        }
        if (null != columnConfig)
        {
            formData.add("columnConfig", columnConfig);
        }

        return formData;
    }


    protected WebResource createResource()
    {
        return resourceRoot(environmentData.getBaseUrl().toExternalForm()).path("rest/issueNav/latest/issueTable");
    }
}