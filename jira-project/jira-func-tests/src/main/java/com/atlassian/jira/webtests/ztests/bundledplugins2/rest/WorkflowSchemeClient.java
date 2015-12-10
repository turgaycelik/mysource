package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.RestApiClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.UserBean;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

/**
 * @since 5.2
 */
public class WorkflowSchemeClient extends RestApiClient<WorkflowSchemeClient>
{
    private final boolean draft;
    private final JIRAEnvironmentData data;

    public WorkflowSchemeClient(JIRAEnvironmentData environmentData)
    {
        this(environmentData, false);
    }

    public WorkflowSchemeClient(JIRAEnvironmentData environmentData, boolean draft)
    {
        super(environmentData);
        this.draft = draft;
        this.data = environmentData;
    }

    public WorkflowSchemeClient asDraft()
    {
        return new WorkflowSchemeClient(data, true);
    }

    public Response<WorkflowScheme> getWorkflowSchemeResponse(final long id)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createWorkflowSchemeResource(id).get(ClientResponse.class);
            }
        }, WorkflowScheme.class);
    }

    public WorkflowScheme getWorkflowScheme(long id)
    {
        return getWorkflowScheme(id, false);
    }

    public WorkflowScheme getWorkflowScheme(long id, boolean draftIfThere)
    {
        return createWorkflowSchemeResource(id)
                .queryParam("returnDraftIfExists", valueOf(draftIfThere))
                .get(WorkflowScheme.class);
    }

    protected WebResource createResource()
    {
        return super.createResource().path("workflowscheme");
    }

    private WebResource createWorkflowSchemeResource(long id)
    {
        WebResource resource = createResource().path(valueOf(id));
        if (draft)
        {
            resource = resource.path("draft");
        }
        return resource;
    }

    private WebResource createWorkflowSchemeWorkflowResource(long id, String workflowName)
    {
        return createWorkflowSchemeResource(id).path("workflow").queryParam("workflowName", workflowName);
    }

    private WebResource createWorkflowSchemeIssueTypeResource(long id, String issueType)
    {
        return createWorkflowSchemeResource(id).path("issuetype").path(issueType);
    }

    public WorkflowScheme createScheme(WorkflowScheme workflowScheme)
    {
        return createResource().type(MediaType.APPLICATION_JSON_TYPE).post(WorkflowScheme.class, workflowScheme);
    }

    public Response<WorkflowScheme> createSchemeResponse(final WorkflowScheme workflowScheme)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, workflowScheme);
            }
        }, WorkflowScheme.class);
    }

    public void deleteScheme(Long id)
    {
        createWorkflowSchemeResource(id).type(MediaType.APPLICATION_JSON_TYPE).delete();
    }

    public Response<Void> deleteSchemeResponse(final Long id)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createWorkflowSchemeResource(id).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
            }
        }, Void.class);
    }

    public Response<WorkflowScheme> updateWorkflowSchemeResponse(final WorkflowScheme workflowSchemeData)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createWorkflowSchemeResource(workflowSchemeData.getId())
                        .type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, workflowSchemeData);
            }
        }, WorkflowScheme.class);
    }

    public WorkflowScheme updateWorkflowScheme(final WorkflowScheme workflowSchemeData)
    {
        return createWorkflowSchemeResource(workflowSchemeData.getId())
                .type(MediaType.APPLICATION_JSON_TYPE).put(WorkflowScheme.class, workflowSchemeData);
    }

    public WorkflowScheme createDraft(long id)
    {
        return createWorkflowSchemeResource(id).path("createdraft").post(WorkflowScheme.class);
    }

    public Response<WorkflowScheme> createDraftResponse(final long id)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createWorkflowSchemeResource(id).path("createdraft").post(ClientResponse.class);
            }
        }, WorkflowScheme.class);
    }

    public List<WorkflowMapping> getWorkflowMappings(long id, boolean maybeDraft)
    {
        final WebResource workflow = createWorkflowSchemeResource(id)
                .path("workflow").queryParam("returnDraftIfExists", String.valueOf(maybeDraft));
        return workflow.get(WorkflowMapping.LIST);
    }

    public Response<List<WorkflowMapping>> getWorkflowMappingsResponse(final long id, final boolean maybeDraft)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                final WebResource workflow = createWorkflowSchemeResource(id)
                        .path("workflow").queryParam("returnDraftIfExists", String.valueOf(maybeDraft));
                return workflow.get(ClientResponse.class);
            }
        }, WorkflowMapping.LIST.getRawClass());
    }

    public WorkflowMapping getWorkflowMapping(long id, String workflowName, boolean maybeDraft)
    {
        WebResource workflow = createWorkflowSchemeWorkflowResource(id, workflowName)
            .queryParam("returnDraftIfExists", String.valueOf(maybeDraft));
        return workflow.get(WorkflowMapping.class);
    }

    public Response<WorkflowMapping> getWorkflowMappingResponse(final long id, final String workflowName, final boolean maybeDraft)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                WebResource workflow = createWorkflowSchemeWorkflowResource(id, workflowName)
                        .queryParam("returnDraftIfExists", String.valueOf(maybeDraft));
                return workflow.get(ClientResponse.class);
            }
        }, WorkflowMapping.class);
    }

    public WorkflowScheme deleteWorkflowMapping(long id, String workflowName, boolean maybeDraft)
    {
        final WebResource workflow = createWorkflowSchemeWorkflowResource(id, workflowName)
                .queryParam("updateDraftIfNeeded", String.valueOf(maybeDraft));
        return workflow.type(MediaType.APPLICATION_JSON_TYPE).delete(WorkflowScheme.class);
    }

    public Response<WorkflowScheme> deleteWorkflowMappingResponse(final long id, final String workflowName, final boolean maybeDraft)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                final WebResource workflow = createWorkflowSchemeWorkflowResource(id, workflowName)
                        .queryParam("updateDraftIfNeeded", String.valueOf(maybeDraft));
                return workflow.type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
            }
        }, WorkflowScheme.class);
    }

    public WorkflowScheme updateWorkflowMapping(long id, WorkflowMapping mapping)
    {
        return updateWorkflowMapping(id, mapping.getWorkflow(), mapping);
    }

    public WorkflowScheme updateWorkflowMapping(long id, String workflowName, WorkflowMapping mapping)
    {
        final WebResource workflow = createWorkflowSchemeWorkflowResource(id, workflowName);
        return workflow.type(MediaType.APPLICATION_JSON_TYPE).put(WorkflowScheme.class, mapping);
    }

    public Response<WorkflowScheme> updateWorkflowMappingResponse(final long id, final WorkflowMapping mapping)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                final WebResource workflow = createWorkflowSchemeWorkflowResource(id, mapping.getWorkflow());
                return workflow.type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, mapping);
            }
        }, WorkflowScheme.class);
    }

    public IssueTypeMappingBean getIssueTypeMapping(long id, String issueType, boolean maybeDraft)
    {
        final WebResource workflow = createWorkflowSchemeIssueTypeResource(id, issueType)
                .queryParam("returnDraftIfExists", String.valueOf(maybeDraft));
        return workflow.get(IssueTypeMappingBean.class);
    }

    public Response<IssueTypeMappingBean> getIssueTypeMappingResponse(final long id, final String issueType, final boolean maybeDraft)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                final WebResource workflow = createWorkflowSchemeIssueTypeResource(id, issueType)
                        .queryParam("returnDraftIfExists", String.valueOf(maybeDraft));
                return workflow.get(ClientResponse.class);
            }
        }, IssueTypeMappingBean.class);
    }

    public WorkflowScheme deleteIssueMapping(long id, String issueType, boolean maybeDraft)
    {
        final WebResource workflow = createWorkflowSchemeIssueTypeResource(id, issueType)
                .queryParam("updateDraftIfNeeded", String.valueOf(maybeDraft));
        return workflow.type(MediaType.APPLICATION_JSON_TYPE).delete(WorkflowScheme.class);
    }

    public Response<WorkflowScheme> deleteIssueMappingResponse(final long id, final String issueType, final boolean maybeDraft)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                final WebResource workflow = createWorkflowSchemeIssueTypeResource(id, issueType)
                        .queryParam("updateDraftIfNeeded", String.valueOf(maybeDraft));
                return workflow.type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
            }
        }, WorkflowScheme.class);
    }

    public WorkflowScheme updateIssueTypeMapping(long id, IssueTypeMappingBean mapping)
    {
        return updateIssueTypeMapping(id, mapping.getIssueType(), mapping);
    }

    public WorkflowScheme updateIssueTypeMapping(long id, String issueType, IssueTypeMappingBean mapping)
    {
        final WebResource workflow = createWorkflowSchemeIssueTypeResource(id, issueType);
        return workflow.type(MediaType.APPLICATION_JSON_TYPE).put(WorkflowScheme.class, mapping);
    }

    public Response<WorkflowScheme> updateIssueTypeMappingResponse(final long id, final IssueTypeMappingBean mapping)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                final WebResource workflow = createWorkflowSchemeIssueTypeResource(id, mapping.getIssueType());
                return workflow.type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, mapping);
            }
        }, WorkflowScheme.class);
    }

    public DefaultBean getDefault(long id, boolean draft)
    {
        return createDefaultResource(id).queryParam("returnDraftIfExists", String.valueOf(draft)).get(DefaultBean.class);
    }

    public Response<DefaultBean> getDefaultResponse(final long id, final boolean draft)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createDefaultResource(id)
                        .queryParam("returnDraftIfExists", String.valueOf(draft))
                        .get(ClientResponse.class);
            }
        }, DefaultBean.class);
    }

    public WorkflowScheme deleteDefault(long id, boolean updateDraftIfNeeded)
    {
        return createDefaultResource(id)
                .queryParam("updateDraftIfNeeded", String.valueOf(updateDraftIfNeeded))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .delete(WorkflowScheme.class);
    }

    public Response<WorkflowScheme> deleteDefaultResponse(final long id, final boolean updateDraftIfNeeded)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createDefaultResource(id)
                        .queryParam("updateDraftIfNeeded", String.valueOf(updateDraftIfNeeded))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .delete(ClientResponse.class);
            }
        }, WorkflowScheme.class);
    }

    public WorkflowScheme setDefault(long id, String workflowName, boolean updateDraft)
    {
        final DefaultBean requestEntity = new DefaultBean(workflowName);
        requestEntity.setUpdateDraftIfNeeded(updateDraft);

        return createDefaultResource(id)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(WorkflowScheme.class, requestEntity);
    }

    public Response<WorkflowScheme> setDefaultResponse(final long id, String workflowName, boolean updateDraft)
    {
        final DefaultBean requestEntity = new DefaultBean(workflowName);
        requestEntity.setUpdateDraftIfNeeded(updateDraft);

        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createDefaultResource(id)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .put(ClientResponse.class, requestEntity);
            }
        }, WorkflowScheme.class);
    }

    private WebResource createDefaultResource(long id)
    {
        return createWorkflowSchemeResource(id).path("default");
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class WorkflowScheme
    {
        @JsonProperty
        private Long id;

        @JsonProperty
        private String name;

        @JsonProperty
        private String description;

        @JsonProperty
        private String defaultWorkflow;

        @JsonProperty
        private Map<String, String> issueTypeMappings;

        @JsonProperty
        private Map<String, String> originalIssueTypeMappings;

        @JsonProperty
        private Map<String, IssueType> issueTypes;

        @JsonProperty
        private boolean draft;

        @JsonProperty
        private UserBean lastModifiedUser;

        @JsonProperty
        private String lastModified;

        @JsonProperty
        private URI self;

        @JsonProperty
        private Boolean updateDraftIfNeeded;

        @JsonProperty
        private String originalDefaultWorkflow;

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        public String getDefaultWorkflow()
        {
            return defaultWorkflow;
        }

        public void setDefaultWorkflow(String defaultWorkflow)
        {
            this.defaultWorkflow = defaultWorkflow;
        }

        public String getOriginalDefaultWorkflow()
        {
            return originalDefaultWorkflow;
        }

        public void setOriginalDefaultWorkflow(String originalDefaultWorkflow)
        {
            this.originalDefaultWorkflow = originalDefaultWorkflow;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public URI getSelf()
        {
            return self;
        }

        public void setSelf(URI self)
        {
            this.self = self;
        }

        public boolean isDraft()
        {
            return draft;
        }

        public void setDraft(boolean draft)
        {
            this.draft = draft;
        }

        public void setLastModifiedUser(UserBean lastModifiedUser)
        {
            this.lastModifiedUser = lastModifiedUser;
        }

        public UserBean getLastModifiedUser()
        {
            return lastModifiedUser;
        }

        public String getLastModified()
        {
            return lastModified;
        }

        public void setLastModified(String lastModified)
        {
            this.lastModified = lastModified;
        }

        public Boolean getUpdateDraftIfNeeded()
        {
            return updateDraftIfNeeded;
        }

        public void setUpdateDraftIfNeeded(Boolean updateDraftIfNeeded)
        {
            this.updateDraftIfNeeded = updateDraftIfNeeded;
        }

        public Map<String, String> getIssueTypeMappings()
        {
            return issueTypeMappings;
        }

        public Map<String, String> getOriginalIssueTypeMappings()
        {
            return originalIssueTypeMappings;
        }

        public void setIssueTypeMappings(Map<String, String> issueTypeMappings)
        {
            this.issueTypeMappings = issueTypeMappings;
        }

        public void setOriginalIssueTypeMappings(Map<String, String> originalIssueTypeMappings)
        {
            this.originalIssueTypeMappings = originalIssueTypeMappings;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class IssueTypeMappingBean
    {
        @JsonProperty
        private String issueType;

        @JsonProperty
        private String workflow;

        @JsonProperty
        private Boolean updateDraftIfNeeded;

        public IssueTypeMappingBean()
        {
        }

        public IssueTypeMappingBean(String issueType, String workflow)
        {
            this.issueType = issueType;
            this.workflow = workflow;
        }

        public String getIssueType()
        {
            return issueType;
        }

        public void setIssueType(String issueType)
        {
            this.issueType = issueType;
        }

        public String getWorkflow()
        {
            return workflow;
        }

        public void setWorkflow(String workflow)
        {
            this.workflow = workflow;
        }

        public Boolean getUpdateDraftIfNeeded()
        {
            return updateDraftIfNeeded;
        }

        public void setUpdateDraftIfNeeded(Boolean updateDraftIfNeeded)
        {
            this.updateDraftIfNeeded = updateDraftIfNeeded;
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class WorkflowMapping
    {
        public static final GenericType<List<WorkflowMapping>> LIST = new GenericType<List<WorkflowMapping>>(){};

        @JsonProperty
        private String workflow;

        @JsonProperty
        private Set<String> issueTypes;

        @JsonProperty
        private Boolean defaultMapping;

        @JsonProperty
        private Boolean updateDraftIfNeeded;

        public WorkflowMapping(String workflow)
        {
            this.workflow = workflow;
        }

        public WorkflowMapping()
        {
        }

        public Set<String> getIssueTypes()
        {
            return issueTypes;
        }

        public void setIssueTypes(Set<String> issueTypes)
        {
            this.issueTypes = issueTypes;
        }

        public String getWorkflow()
        {
            return workflow;
        }

        public void setWorkflow(String workflow)
        {
            this.workflow = workflow;
        }

        void addIssueType(String issueType)
        {
            issueTypes.add(issueType);
        }

        public Boolean isDefaultMapping()
        {
            return defaultMapping;
        }

        public void setDefaultMapping(Boolean defaultMapping)
        {
            this.defaultMapping = defaultMapping;
        }

        public Boolean getUpdateDraftIfNeeded()
        {
            return updateDraftIfNeeded;
        }

        public void setUpdateDraftIfNeeded(Boolean updateDraftIfNeeded)
        {
            this.updateDraftIfNeeded = updateDraftIfNeeded;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            WorkflowMapping that = (WorkflowMapping) o;

            if (defaultMapping != null ? !defaultMapping.equals(that.defaultMapping) : that.defaultMapping != null)
            { return false; }
            if (issueTypes != null ? !issueTypes.equals(that.issueTypes) : that.issueTypes != null) { return false; }
            if (workflow != null ? !workflow.equals(that.workflow) : that.workflow != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = workflow != null ? workflow.hashCode() : 0;
            result = 31 * result + (issueTypes != null ? issueTypes.hashCode() : 0);
            result = 31 * result + (defaultMapping != null ? defaultMapping.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class IssueType {
        @JsonProperty
        private URI self;

        @JsonProperty
        private URI iconUrl;

        @JsonProperty
        private String id;

        @JsonProperty
        private String description;

        @JsonProperty
        private String name;

        @JsonProperty
        private boolean subtask;
    }

    public static class DefaultBean
    {
        @JsonProperty
        private String workflow;

        @JsonProperty
        private Boolean updateDraftIfNeeded;

        public DefaultBean() {}

        public DefaultBean(String workflow)
        {
            this.workflow = workflow;
        }

        public Boolean getUpdateDraftIfNeeded()
        {
            return updateDraftIfNeeded;
        }

        public void setUpdateDraftIfNeeded(Boolean updateDraftIfNeeded)
        {
            this.updateDraftIfNeeded = updateDraftIfNeeded;
        }

        public String getWorkflow()
        {
            return workflow;
        }

        public void setWorkflow(String workflow)
        {
            this.workflow = workflow;
        }
    }
}
