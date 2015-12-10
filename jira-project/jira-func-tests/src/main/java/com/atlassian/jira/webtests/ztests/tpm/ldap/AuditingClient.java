package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * @since v6.2
 */
public class AuditingClient extends RestApiClient<AuditingClient>
{
    private final JIRAEnvironmentData environmentData;

    protected AuditingClient(final JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        this.environmentData = environmentData;
    }

    public ViewResponse getViewResponse()
    {
        return createResource().get(ViewResponse.class);
    }

    @Override
    protected WebResource createResource()
    {
        return resourceRoot(environmentData.getBaseUrl().toExternalForm()).path("rest").path("jira-auditing-plugin").path("1").path("view");
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class ViewResponse
    {
        @JsonProperty
         private List<RecordResponse> records;

        public List<RecordResponse> getRecords()
        {
            return records;
        }

        @JsonIgnoreProperties (ignoreUnknown = true)
        static public class RecordResponse
        {
            @JsonProperty
            private String summary;
            @JsonProperty
            private Long id;
            @JsonProperty
            private String created;
            @JsonProperty
            private AssociatedItemResponse objectItem;

            public String getSummary()
            {
                return summary;
            }

            public Long getId()
            {
                return id;
            }

            public String getCreated()
            {
                return created;
            }

            public AssociatedItemResponse getObjectItem()
            {
                return objectItem;
            }
        }

        public static class AssociatedItemResponse
        {
            @JsonProperty
            private String objectName;
            @JsonProperty
            private String objectType;

            public String getObjectName()
            {
                return objectName;
            }

            public String getObjectType()
            {
                return objectType;
            }
        }
    }
}
