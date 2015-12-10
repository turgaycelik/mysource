package com.atlassian.jira.rest.v2.admin;


import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowPropertyEditor;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.workflow.loader.ActionDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
public abstract class WorkflowTransitionResource
{
    private static final String PROPERTIES_PATH = "{id}/properties";

    //Only in this class.
    private WorkflowTransitionResource()
    {
    }

    /**
     * Return the property or properties associated with a transition.
     *
     * @param workflowName the name of the workflow to use.
     * @param workflowMode the type of workflow to use. Can either be "live" or "draft".
     * @param transitionId the ID of the transition within the workflow.
     * @param includeReservedKeys some keys under the "jira." prefix are editable, some are not. Set this to true
     *     in order to include the non-editable keys in the response.
     * @param key the name of the property key to query. Can be left off the query to return all properties.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link PropertyBeanDoco#EXAMPLE_LIST}
     *
     * @response.representation.200.example
     *      {@link PropertyBeanDoco#EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the user does not have admin permission
     */
    @GET
    @Path (PROPERTIES_PATH)
    abstract public Response getProperties(@PathParam("id") long transitionId,
            @QueryParam("includeReservedKeys") boolean includeReservedKeys,
            @QueryParam("key") String key,
            @QueryParam("workflowName") String workflowName,
            @QueryParam("workflowMode") String workflowMode);

    /**
     * Add a new property to a transition. Trying to add a property that already
     * exists will fail.
     *
     * @param workflowName the name of the workflow to use.
     * @param workflowMode the type of workflow to use. Can either be "live" or "draft".
     * @param transitionId the ID of the transition within the workflow.
     * @param key the name of the property to add.
     *
     * @request.representation.doc The new property to create. If the "key" is passed it must match the value in the
     *   "key" query parameter of the request.
     *
     * @request.representation.example
     *      {@link PropertyBeanDoco#CREATE_EXAMPLE}

     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link PropertyBeanDoco#EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the user does not have admin permission
     *
     * @response.representation.400.doc
     *      Returned if the request is invalid.
     */
    @POST
    @WebSudoRequired
    @Path (PROPERTIES_PATH)
    public abstract Response createProperty(@PathParam ("id") long transitionId,
            @QueryParam ("key") String key,
            @QueryParam("workflowName") String workflowName,
            @QueryParam("workflowMode") String workflowMode,
            PropertyBean body);

    /**
     * Update/add new property to a transition. Trying to update a property that does
     * not exist will result in a new property being added.
     *
     * @param workflowName the name of the workflow to use.
     * @param workflowMode the type of workflow to use. Can either be "live" or "draft".
     * @param transitionId the ID of the transition within the workflow.
     * @param key the name of the property to add.
     *
     * @request.representation.doc The new property to create. If the "key" is passed it must match the value in the
     *   "key" query parameter of the request.
     *
     * @request.representation.example
     *      {@link PropertyBeanDoco#CREATE_EXAMPLE}

     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link PropertyBeanDoco#EXAMPLE}
     *
     * @response.representation.403.doc
     *      Returned if the user does not have admin permission
     *
     * @response.representation.400.doc
     *      Returned if the request is invalid.
     *
     * @response.representation.304.doc
     *      Returned if no changes were actually made by the request (e.g. updating a property to value it already holds).
     */
    @PUT
    @WebSudoRequired
    @Path (PROPERTIES_PATH)
    public abstract Response updateProperty(@PathParam ("id") long transitionId,
            @QueryParam ("key") String key,
            @QueryParam("workflowName") String workflowName,
            @QueryParam("workflowMode") String workflowMode,
            PropertyBean body);

    /**
     * Delete a property from the passed transition on the passed workflow. It is not an error to delete a property that
     * does not exist.
     *
     * @param workflowName the name of the workflow to use.
     * @param workflowMode the type of workflow to use. Can either be "live" or "draft".
     * @param transitionId the ID of the transition within the workflow.
     * @param key the name of the property to add.
     *
     * @request.representation.doc The new property to create. If the "key" is passed it must match the value in the
     *   "key" query parameter of the request.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.403.doc
     *      Returned if the user does not have admin permission
     *
     * @response.representation.400.doc
     *      Returned if the request is invalid.
     *
     * @response.representation.304.doc
     *      Returned if no changes were actually made by the request (e.g. trying to delete a property that does not exist).
     */
    @DELETE
    @WebSudoRequired
    @Path (PROPERTIES_PATH)
    public abstract Response deleteProperty(@PathParam ("id") long transitionId,
            @QueryParam ("key") String key,
            @QueryParam("workflowName") String workflowName,
            @QueryParam("workflowMode") String workflowMode);

    @Component
    public static class Factory
    {
        private static final String WORKFLOW_NAME = "workflowName";
        private static final String WORKFLOW_MODE = "workflowMode";

        private final WorkflowManager workflowManager;
        private final JiraAuthenticationContext ctx;
        private final WorkflowService workflowService;
        private final WorkflowPropertyEditor.WorkflowPropertyEditorFactory factory;

        @Autowired
        public Factory(final WorkflowManager workflowManager, final JiraAuthenticationContext ctx,
                final WorkflowService workflowService, final WorkflowPropertyEditor.WorkflowPropertyEditorFactory factory)
        {
            this.workflowManager = workflowManager;
            this.ctx = ctx;
            this.factory = factory;
            this.workflowService = workflowService;
        }

        WorkflowTransitionResource getResource(String workflowName, WorkflowMode mode)
        {
            final JiraServiceContext context = new JiraServiceContextImpl(ctx.getUser());
            final JiraWorkflow workflow;
            if (mode == null)
            {
                return new ConstantResponseResource(Responses.badRequest(WORKFLOW_MODE,
                        ctx.getI18nHelper().getText("admin.errors.workflows.invalid.workflow.mode")));
            }
            else if (mode == WorkflowMode.LIVE)
            {
                workflow = workflowService.getWorkflow(context, workflowName);
            }
            else
            {
                workflow = workflowService.getDraftWorkflow(context, workflowName);
            }

            if (workflow == null)
            {
                if (context.getErrorCollection().hasAnyErrors())
                {
                    return new ConstantResponseResource(Responses.forCollection(context.getErrorCollection(),
                            ErrorCollection.Reason.NOT_FOUND));
                }
                else
                {
                    return new ConstantResponseResource(Responses.notFound(WORKFLOW_NAME,
                            ctx.getI18nHelper().getText("admin.errors.workflow.with.name.does.not.exist",
                                    workflowName)));
                }
            }
            else
            {
                return new TransitionResource(workflowManager, ctx, factory, workflow);
            }
        }
    }

    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    public static class TransitionResource extends WorkflowTransitionResource
    {
        private static final String PROPERTY_KEY = "key";
        private static final String TRANSITION_ID = "transitionId";

        private final WorkflowManager workflowManager;
        private final JiraAuthenticationContext ctx;
        private final WorkflowPropertyEditor.WorkflowPropertyEditorFactory factory;
        private final JiraWorkflow workflow;

        private static enum Operation
        {
            CREATE, UPDATE, DELETE
        }

        TransitionResource(final WorkflowManager workflowManager, final JiraAuthenticationContext ctx,
                final WorkflowPropertyEditor.WorkflowPropertyEditorFactory factory, final JiraWorkflow workflow)
        {
            this.workflowManager = workflowManager;
            this.ctx = ctx;
            this.factory = factory;
            this.workflow = workflow;
        }

        @Override
        @GET
        @Path (PROPERTIES_PATH)
        public Response getProperties(@PathParam("id") long transitionId,
                @QueryParam("includeReservedKeys") boolean includeReservedKeys,
                @QueryParam("key") String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode)
        {
            Map<Long, ActionDescriptor> actionsMap = getActionsMap(workflow);
            ActionDescriptor actionDescriptor = actionsMap.get(transitionId);
            if (actionDescriptor == null)
            {
                return Responses.notFound(TRANSITION_ID,
                        ctx.getI18nHelper().getText("admin.errors.workflows.transition.does.not.exist",
                                String.valueOf(transitionId)));
            }

            key = StringUtils.stripToNull(key);
            Map<String, String> metaAttributes = getMetaAttributes(actionDescriptor);
            if (key == null)
            {
                final List<PropertyBean> items = Lists.newArrayList();
                for (Map.Entry<String, String> s : metaAttributes.entrySet())
                {
                    if (includeReservedKeys || !WorkflowUtil.isReservedKey(s.getKey()))
                    {
                        items.add(new PropertyBean(s.getKey(), s.getValue()));
                    }
                }
                return Response.ok(items).cacheControl(never()).build();
            }
            else
            {
                if (metaAttributes.containsKey(key))
                {
                    return Response.ok(new PropertyBean(key, metaAttributes.get(key))).cacheControl(never()).build();
                }
                else
                {
                    String displayKey = key;
                    if (StringUtils.isBlank(displayKey))
                    {
                        displayKey = "<blank>";
                    }
                    return Responses.notFound(PROPERTY_KEY,
                            ctx.getI18nHelper().getText("admin.errors.workflows.attribute.key.does.not.exist", displayKey));
                }
            }
        }

        @Override
        @POST
        @WebSudoRequired
        @Path (PROPERTIES_PATH)
        public Response createProperty(@PathParam ("id") final long transitionId,
                @QueryParam ("key") String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode, final PropertyBean body)
        {
            return createEditOrDeleteProperty(transitionId, key, body, Operation.CREATE);
        }

        @Override
        @PUT
        @WebSudoRequired
        @Path (PROPERTIES_PATH)
        public Response updateProperty(@PathParam("id") final long transitionId,
                @QueryParam("key") String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode, PropertyBean body)
        {
            return createEditOrDeleteProperty(transitionId, key, body, Operation.UPDATE);
        }

        @Override
        @DELETE
        @WebSudoRequired
        @Path (PROPERTIES_PATH)
        public Response deleteProperty(@PathParam ("id") final long transitionId,
                @QueryParam ("key") String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode)
        {
            return createEditOrDeleteProperty(transitionId, key, null, Operation.DELETE);
        }

        private Response createEditOrDeleteProperty(final long transitionId, String key, final PropertyBean body,
                final Operation operation)
        {
            final JiraWorkflow workflow = getWorkflowForEdit();
            final Map<Long, ActionDescriptor> actionsMap = getActionsMap(workflow);
            final ActionDescriptor actionDescriptor = actionsMap.get(transitionId);
            if (actionDescriptor == null)
            {
                return Responses.notFound(TRANSITION_ID,
                        ctx.getI18nHelper().getText("admin.errors.workflows.transition.does.not.exist",
                                String.valueOf(transitionId)));
            }
            if (operation == Operation.UPDATE || operation == Operation.CREATE)
            {
                if (body.getKey() != null && !body.getKey().equals(key))
                {
                    return Responses.badRequest(PROPERTY_KEY,
                            ctx.getI18nHelper().getText("admin.errors.workflows.attribute.key.mismatch",
                                    key, body.getKey()));
                }

                final WorkflowPropertyEditor editor = factory.transitionPropertyEditor(workflow, actionDescriptor);
                final ServiceOutcome<WorkflowPropertyEditor.Result> outcome;
                if (operation == Operation.UPDATE)
                {
                    outcome = editor.updateProperty(key, body.value);
                }
                else
                {
                    outcome = editor.addProperty(key, body.value);
                }

                if (outcome.isValid())
                {
                    final WorkflowPropertyEditor.Result result = outcome.getReturnedValue();
                    if (!result.isModified())
                    {
                        return Responses.notModified();
                    }
                    else
                    {
                        return Responses.ok(new PropertyBean(result.name(), result.value()));
                    }
                }
                else
                {
                    return Responses.forCollection(outcome.getErrorCollection());
                }
            }
            else if (operation == Operation.DELETE)
            {
                final WorkflowPropertyEditor editor = factory.transitionPropertyEditor(workflow, actionDescriptor);
                final ServiceOutcome<WorkflowPropertyEditor.Result> outcome = editor.deleteProperty(key);
                if (outcome.isValid())
                {
                    final WorkflowPropertyEditor.Result result = outcome.getReturnedValue();
                    if (!result.isModified())
                    {
                        return Responses.notModified();
                    }
                    else
                    {
                        return Responses.ok();
                    }
                }
                else
                {
                    return Responses.forCollection(outcome.getErrorCollection());
                }
            }
            else
            {
                throw new IllegalStateException("Cannot handle operation '" + operation + "'.");
            }
        }

        private JiraWorkflow getWorkflowForEdit()
        {
            if (workflow.isDraftWorkflow())
            {
                return workflow;
            }
            else
            {
                return workflowManager.getWorkflowClone(workflow.getName());
            }
        }

        private Map<String, String> getMetaAttributes(ActionDescriptor actionDescriptor)
        {
            @SuppressWarnings ("unchecked")
            final Map<String, String> metaAttributes = actionDescriptor.getMetaAttributes();
            return metaAttributes == null ? Collections.<String, String>emptyMap() : metaAttributes;
        }

        private Map<Long, ActionDescriptor> getActionsMap(JiraWorkflow workflow)
        {
            return Maps.uniqueIndex(workflow.getAllActions(), new Function<ActionDescriptor, Long>()
            {
                @Override
                public Long apply(final ActionDescriptor actionDescriptor)
                {
                    return (long) actionDescriptor.getId();
                }
            });
        }
    }

    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    public static class ConstantResponseResource extends WorkflowTransitionResource
    {
        private final Response response;

        ConstantResponseResource(Response response)
        {
            this.response = response;
        }

        @Override
        @GET
        @Path (PROPERTIES_PATH)
        public Response getProperties(@PathParam("id") long transitionId,
                @QueryParam("includeReservedKeys") boolean includeReservedKeys,
                @QueryParam("key") String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode)
        {
            return response;
        }

        @Override
        @POST
        @WebSudoRequired
        @Path (PROPERTIES_PATH)
        public Response createProperty(@PathParam ("id") final long transitionId,
                @QueryParam ("key") final String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode,
                final PropertyBean body)
        {
            return response;
        }

        @Override
        @PUT
        @WebSudoRequired
        @Path (PROPERTIES_PATH)
        public Response updateProperty(@PathParam ("id") final long transitionId,
                @QueryParam ("key") final String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode,
                final PropertyBean body)
        {
            return response;
        }

        @Override
        @DELETE
        @WebSudoRequired
        @Path (PROPERTIES_PATH)
        public Response deleteProperty(@PathParam ("id") final long transitionId,
                @QueryParam ("key") final String key,
                @QueryParam("workflowName") String workflowName,
                @QueryParam("workflowMode") String workflowMode)
        {
            return response;
        }
    }

    public enum WorkflowMode
    {
        DRAFT, LIVE;

        public static WorkflowMode getMode(String name)
        {
            if (name == null)
            {
                return LIVE;
            }
            for (WorkflowMode mode : WorkflowMode.values())
            {
                if (mode.name().equalsIgnoreCase(name))
                {
                    return mode;
                }
            }
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropertyBean
    {
        private final String key;
        private final String value;

        @JsonCreator
        public PropertyBean(@JsonProperty("key") String key, @JsonProperty("value") String value)
        {
            this.key = key;
            this.value = value;
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

    private static final class Responses
    {
        private Responses() {}

        private static Response badRequest(String key, String message)
        {
            return forMessage(key, message, Response.Status.BAD_REQUEST);
        }

        private static Response notFound(String key, String message)
        {
            return forMessage(key, message, Response.Status.NOT_FOUND);
        }

        private static Response notModified()
        {
            return build(Response.notModified());
        }

        private static Response forCollection(ErrorCollection collection)
        {
            return forCollection(collection, ErrorCollection.Reason.SERVER_ERROR);
        }

        private static Response forCollection(ErrorCollection collection, ErrorCollection.Reason defaultReason)
        {
            if (!collection.hasAnyErrors())
            {
                throw new IllegalArgumentException("collection has no errors.");
            }
            if (defaultReason == null)
            {
                throw new IllegalArgumentException("defaultReason is null");
            }

            ErrorCollection.Reason worstReason = ErrorCollection.Reason.getWorstReason(collection.getReasons());
            if (worstReason == null)
            {
                worstReason = defaultReason;
            }
            return build(Response.status(worstReason.getHttpStatusCode())
                    .entity(com.atlassian.jira.rest.api.util.ErrorCollection.of(collection)));
        }

        private static Response forMessage(String key, String message, Response.Status status)
        {
            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            errorCollection.addError(key, message);

            return build(Response.status(status)
                    .entity(com.atlassian.jira.rest.api.util.ErrorCollection.of(errorCollection)));
        }

        private static Response ok(final Object bean)
        {
            return build(Response.ok(bean));
        }

        private static Response ok()
        {
            return build(Response.ok());
        }

        private static Response build(Response.ResponseBuilder builder)
        {
            return builder.cacheControl(never()).build();
        }
    }
}
