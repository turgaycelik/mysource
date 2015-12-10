package com.atlassian.jira.functest.framework.backdoor;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.atlassian.jira.testkit.client.WorkflowsControl;
import com.atlassian.jira.testkit.client.restclient.Response;

import com.google.common.collect.Maps;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.jira.functest.framework.FunctTestConstants.FUNC_TEST_PLUGIN_REST_PATH;

/**
 * @since v5.2
 */
public class WorkflowsControlExt extends WorkflowsControl
{
    public WorkflowsControlExt(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<Workflow> getWorkflowsDetailed()
    {
        return createExtResource().get(Workflow.LIST);
    }

    public Workflow getWorkflowDetailed(final String name)
    {
        final Response<Workflow> response = toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createExtResource().queryParam("name", name).get(ClientResponse.class);
            }
        }, Workflow.class);

        return response.body;
    }

    public Workflow createWorkflow(String name)
    {
        return createExtResource().entity(name).post(Workflow.class);
    }

    public Workflow createDraftOfWorkflow(String parentName)
    {
        return createExtResource().path("createdraft").entity(parentName).post(Workflow.class);
    }

    public void setDescription(String name, String description)
    {
        createExtResource().path(name).entity(description).post();
    }

    public void deleteWorkflow(final String name)
    {
        createExtResource().queryParam("name", name).delete();
    }

    private WebResource createExtResource()
    {
        // for workflow scheme-related backdoor, use the func-test-plugin
        return createResourceForPath(FUNC_TEST_PLUGIN_REST_PATH).path("workflow");
    }

    private WebResource createRestAPIResource()
    {
        // for workflow scheme-related backdoor, use the func-test-plugin
        return createResourceForPath(BackdoorControl.API_REST_PATH, BackdoorControl.API_REST_VERSION).path("workflow");
    }

    public void setTransitionProperty(String workflowName, boolean draft, long transitionID, String key, Object value)
    {
        createRestAPIResource()
                .path("transitions")
                .path(String.valueOf(transitionID))
                .path("properties")
                .queryParam("workflowName", workflowName)
                .queryParam("key", key)
                .queryParam("workflowMode", draft ? "draft" : "live")
                .entity(new PropertyBean(key, value.toString()))
                .put();
    }

    public Map<String, String> getProperties(String workflowName, boolean draft, long transitionId)
    {
        final List<PropertyBean> propertyBeans = createRestAPIResource()
                .path("transitions")
                .path(String.valueOf(transitionId))
                .path("properties")
                .queryParam("workflowName", workflowName)
                .queryParam("workflowMode", draft ? "draft" : "live")
                .get(PropertyBean.LIST);

        Map<String, String> result = Maps.newHashMap();
        for (PropertyBean property : propertyBeans)
        {
            result.put(property.getKey(), property.getValue());
        }
        return result;
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class PropertyBean
    {
        private static final GenericType<List<PropertyBean>> LIST = new GenericType<List<PropertyBean>>(){};

        private final String key;
        private final String value;

        @JsonCreator
        public PropertyBean(@JsonProperty ("key") String key, @JsonProperty("value") String value)
        {
            this.key = StringUtils.stripToNull(key);
            this.value = StringUtils.stripToEmpty(value);
        }

        @JsonProperty
        public String getKey()
        {
            return key;
        }

        @JsonProperty
        public String getValue()
        {
            return value;
        }

        @JsonProperty
        public String getId()
        {
            return getKey();
        }
    }

    @JsonAutoDetect
    public static class Workflow
    {
        private static final GenericType<List<Workflow>> LIST = new GenericType<List<Workflow>>(){};

        private String name;
        private String description;
        private boolean hasDraft;

        public Workflow()
        {
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

        public boolean isHasDraft()
        {
            return hasDraft;
        }

        public void setHasDraft(boolean hasDraft)
        {
            this.hasDraft = hasDraft;
        }
    }
}
