package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Control for ManagedConfigurationBackdoor
 *
 * @since v5.2
 */
public class ManagedConfigurationControl extends BackdoorControl<ManagedConfigurationControl>
{
    public ManagedConfigurationControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public List<ManagedEntity> getManagedCustomFields()
    {
        return get(createCustomFieldsResource(), ManagedEntity.LIST);
    }

    public ManagedEntity getManagedCustomField(String customFieldId)
    {
        return get(createCustomFieldsResource().path(customFieldId), ManagedEntity.class);
    }

    public ManagedEntity postManagedCustomField(String customFieldId, boolean isManaged, boolean isLocked)
    {
        RegisterEntityHolder registerEntityHolder = new RegisterEntityHolder(isManaged, isLocked);
        return post(createCustomFieldsResource().path(customFieldId), registerEntityHolder, ManagedEntity.class);
    }

    public List<ManagedEntity> getManagedWorkflows()
    {
        return get(createWorkflowsResource(), ManagedEntity.LIST);
    }

    public ManagedEntity getManagedWorkflow(String workflowName)
    {
        return get(createWorkflowsResource().path(workflowName), ManagedEntity.class);
    }

    public ManagedEntity postManagedWorkflow(String workflowName, boolean isManaged, boolean isLocked)
    {
        RegisterEntityHolder registerEntityHolder = new RegisterEntityHolder(isManaged, isLocked);
        return post(createWorkflowsResource().path(workflowName), registerEntityHolder, ManagedEntity.class);
    }

    public List<ManagedEntity> getManagedWorkflowSchemes()
    {
        return get(createWorkflowSchemesResource(), ManagedEntity.LIST);
    }

    public ManagedEntity getManagedWorkflowScheme(String workflowSchemeName)
    {
        return get(createWorkflowSchemesResource().path(workflowSchemeName), ManagedEntity.class);
    }

    public ManagedEntity postManagedWorkflowScheme(String workflowSchemeName, boolean isManaged, boolean isLocked)
    {
        RegisterEntityHolder registerEntityHolder = new RegisterEntityHolder(isManaged, isLocked);
        return post(createWorkflowSchemesResource().path(workflowSchemeName), registerEntityHolder, ManagedEntity.class);
    }

    private <T> T get(WebResource resource, Class<T> returnType)
    {
        return resource.get(returnType);
    }

    private <T> T get(WebResource resource, GenericType<T> returnType)
    {
        return resource.get(returnType);
    }

    private <T> T post(WebResource resource, Object object, Class<T> returnType)
    {
        return resource.type(MediaType.APPLICATION_JSON_TYPE).post(returnType, object);
    }

    private WebResource createCustomFieldsResource()
    {
        return createManagedConfigurationResource().path("customfields");
    }

    private WebResource createWorkflowsResource()
    {
        return createManagedConfigurationResource().path("workflows");
    }

    private WebResource createWorkflowSchemesResource()
    {
        return createManagedConfigurationResource().path("workflowschemes");
    }

    private WebResource createManagedConfigurationResource()
    {
        return createResource().path("managedconfiguration");
    }

    public static class ManagedEntity
    {
        private static final GenericType<List<ManagedEntity>> LIST = new GenericType<List<ManagedEntity>>(){};

        private String itemId;
        private String itemType;
        private boolean isManaged;
        private boolean isLocked;

        public ManagedEntity()
        {
        }

        public ManagedEntity(String itemId, String itemType, boolean isManaged, boolean isLocked)
        {
            this.itemId = itemId;
            this.itemType = itemType;
            this.isManaged = isManaged;
            this.isLocked = isLocked;
        }

        public String getItemId()
        {
            return itemId;
        }

        public String getItemType()
        {
            return itemType;
        }

        public boolean isManaged()
        {
            return isManaged;
        }

        public boolean isLocked()
        {
            return isLocked;
        }

        public void setItemId(String itemId)
        {
            this.itemId = itemId;
        }

        public void setItemType(String itemType)
        {
            this.itemType = itemType;
        }

        public void setIsManaged(boolean isManaged)
        {
            this.isManaged = isManaged;
        }

        public void setIsLocked(boolean isLocked)
        {
            this.isLocked = isLocked;
        }
    }

    private static class RegisterEntityHolder
    {
        public boolean isManaged;
        public boolean isLocked;

        private RegisterEntityHolder(boolean managed, boolean locked)
        {
            isManaged = managed;
            isLocked = locked;
        }
    }
}
