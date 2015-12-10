package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;

/**
 * Controls managed configuration
 *
 * @since v5.2
 */
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON})
@Consumes ({MediaType.APPLICATION_JSON})
@Path ("managedconfiguration")
public class ManagedConfigurationBackdoor
{
    private final ManagedConfigurationItemService managedConfigurationItemService;
    private final CustomFieldManager customFieldManager;
    private final WorkflowService workflowService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WorkflowSchemeManager workflowSchemeManager;

    public ManagedConfigurationBackdoor(ManagedConfigurationItemService managedConfigurationItemService, CustomFieldManager customFieldManager, WorkflowService workflowService, JiraAuthenticationContext jiraAuthenticationContext, WorkflowSchemeManager workflowSchemeManager)
    {
        this.managedConfigurationItemService = managedConfigurationItemService;
        this.customFieldManager = customFieldManager;
        this.workflowService = workflowService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.workflowSchemeManager = workflowSchemeManager;
    }

    @Path("customfields")
    @GET
    public Response getManagedCustomFields()
    {
        Collection<ManagedConfigurationItem> managedConfigurationItems = managedConfigurationItemService.getManagedConfigurationItems(ManagedConfigurationItemType.CUSTOM_FIELD);
        List<ManagedItem> result = CollectionUtil.transform(managedConfigurationItems, TRANSFORMER);
        return Response.ok(result).cacheControl(never()).build();
    }

    @Path("customfields/{id}")
    @GET
    public Response getManagedCustomField(final @PathParam("id") String itemId)
    {
        CustomField customField = customFieldManager.getCustomFieldObject(itemId);
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedCustomField(customField);
        return Response.ok(ManagedItem.from(item)).cacheControl(never()).build();
    }

    @Path("customfields/{id}")
    @POST
    public Response postManageCustomField(final @PathParam("id") String itemId, RegisterItemHolder registerItemHolder)
    {
        CustomField customField = customFieldManager.getCustomFieldObject(itemId);
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedCustomField(customField);

        // update item
        item = item.newBuilder().setManaged(registerItemHolder.isManaged).setConfigurationItemAccessLevel(registerItemHolder.isLocked ? ConfigurationItemAccessLevel.LOCKED : ConfigurationItemAccessLevel.ADMIN).build();
        ServiceOutcome<ManagedConfigurationItem> outcome = managedConfigurationItemService.updateManagedConfigurationItem(item);
        if (outcome.isValid())
        {
            return Response.ok(ManagedItem.from(item)).cacheControl(never()).build();
        }
        else
        {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not update managed item").build());
        }
    }

    @Path("workflows")
    @GET
    public Response getManagedWorkflows()
    {
        Collection<ManagedConfigurationItem> managedConfigurationItems = managedConfigurationItemService.getManagedConfigurationItems(ManagedConfigurationItemType.WORKFLOW);
        List<ManagedItem> result = CollectionUtil.transform(managedConfigurationItems, TRANSFORMER);
        return Response.ok(result).cacheControl(never()).build();
    }

    @Path("workflows/{name}")
    @GET
    public Response getManagedWorkflow(final @PathParam("name") String workflowName)
    {
        JiraWorkflow workflow = workflowService.getWorkflow(new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser()), workflowName);
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedWorkflow(workflow);
        return Response.ok(ManagedItem.from(item)).cacheControl(never()).build();
    }

    @Path("workflows/{name}")
    @POST
    public Response postManageWorkflow(final @PathParam("name") String workflowName, RegisterItemHolder registerItemHolder)
    {
        JiraWorkflow workflow = workflowService.getWorkflow(new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser()), workflowName);
        ManagedConfigurationItem item = managedConfigurationItemService.getManagedWorkflow(workflow);

        // update item
        item = item.newBuilder().setManaged(registerItemHolder.isManaged).setConfigurationItemAccessLevel(registerItemHolder.isLocked ? ConfigurationItemAccessLevel.LOCKED : ConfigurationItemAccessLevel.ADMIN).build();
        ServiceOutcome<ManagedConfigurationItem> outcome = managedConfigurationItemService.updateManagedConfigurationItem(item);
        if (outcome.isValid())
        {
            return Response.ok(ManagedItem.from(item)).cacheControl(never()).build();
        }
        else
        {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not update managed item").build());
        }
    }

    @Path("workflowschemes")
    @GET
    public Response getManagedWorkflowSchemes()
    {
        Collection<ManagedConfigurationItem> managedConfigurationItems = managedConfigurationItemService.getManagedConfigurationItems(ManagedConfigurationItemType.WORKFLOW);
        List<ManagedItem> result = CollectionUtil.transform(managedConfigurationItems, TRANSFORMER);
        return Response.ok(result).cacheControl(never()).build();
    }

    @Path("workflowschemes/{name}")
    @GET
    public Response getManagedWorkflowScheme(final @PathParam("name") String workflowSchemeName)
    {
        AssignableWorkflowScheme workflowScheme = workflowSchemeManager.getWorkflowSchemeObj(workflowSchemeName);
        if (workflowScheme == null)
        {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Could not find managed workflow scheme with name " + workflowSchemeName).build());
        }

        ManagedConfigurationItem item = managedConfigurationItemService.getManagedWorkflowScheme(workflowScheme);
        return Response.ok(ManagedItem.from(item)).cacheControl(never()).build();
    }

    @Path("workflowschemes/{name}")
    @POST
    public Response postManageWorkflowScheme(final @PathParam("name") String workflowSchemeName, RegisterItemHolder registerItemHolder)
    {
        AssignableWorkflowScheme workflowScheme = workflowSchemeManager.getWorkflowSchemeObj(workflowSchemeName);
        if (workflowScheme == null)
        {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity("Could not find managed workflow scheme with name " + workflowSchemeName).build());
        }

        ManagedConfigurationItem item = managedConfigurationItemService.getManagedWorkflowScheme(workflowScheme);

        // update item
        item = item.newBuilder().setManaged(registerItemHolder.isManaged).setConfigurationItemAccessLevel(registerItemHolder.isLocked ? ConfigurationItemAccessLevel.LOCKED : ConfigurationItemAccessLevel.ADMIN).build();
        ServiceOutcome<ManagedConfigurationItem> outcome = managedConfigurationItemService.updateManagedConfigurationItem(item);
        if (outcome.isValid())
        {
            return Response.ok(ManagedItem.from(item)).cacheControl(never()).build();
        }
        else
        {
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Could not update managed item").build());
        }
    }

    @JsonAutoDetect
    private static class ManagedItem
    {
        @JsonProperty
        private String itemId;

        @JsonProperty
        private String itemType;

        @JsonProperty
        private boolean isManaged;

        @JsonProperty
        private boolean isLocked;

        private ManagedItem(String itemId, String itemType, boolean managed, boolean locked)
        {
            this.itemId = itemId;
            this.itemType = itemType;
            isManaged = managed;
            isLocked = locked;
        }

        static ManagedItem from(ManagedConfigurationItem item)
        {
            return new ManagedItem(item.getItemId(), item.getItemType().toString(), item.isManaged(), item.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED);
        }
    }

    private static class RegisterItemHolder
    {
        public boolean isManaged;
        public boolean isLocked;
    }

    private static final Function<ManagedConfigurationItem,ManagedItem> TRANSFORMER = new Function<ManagedConfigurationItem, ManagedItem>()
    {
        @Override
        public ManagedItem get(ManagedConfigurationItem input)
        {
            return ManagedItem.from(input);
        }
    };
}
