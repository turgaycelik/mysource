package com.atlassian.jira.rest.v2.admin.workflowscheme;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.util.ErrorCollection.Reason;
import static com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR;
import static com.atlassian.jira.util.ErrorCollection.Reason.getWorstReason;

/**
 * @since v5.2
 */
@Path ("workflowscheme")
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
@WebSudoRequired
public class WorkflowSchemeResource
{
    private final AssignableRestWorkflowScheme.Factory factory;
    private final WorkflowManager workflowManager;
    private final IssueTypeManager issueTypeManager;
    private final JiraAuthenticationContext authenticationContext;

    public WorkflowSchemeResource(AssignableRestWorkflowScheme.Factory factory,
            WorkflowManager workflowManager, IssueTypeManager issueTypeManager,
            JiraAuthenticationContext authenticationContext)
    {
        this.factory = factory;
        this.workflowManager = workflowManager;
        this.issueTypeManager = issueTypeManager;
        this.authenticationContext = authenticationContext;
    }

    /**
     * Returns the requested workflow scheme to the caller.
     *
     * @param id the id of the scheme.
     * @param returnDraftIfExists when true indicates that a scheme's draft, if it exists, should be queried instead of
     *  the scheme itself.
     * @return the requested workflow scheme.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the scheme exists and the caller has permission to see it.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the requested scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}")
    public Response getById(@PathParam ("id") long id,
            @QueryParam("returnDraftIfExists") @DefaultValue("false") boolean returnDraftIfExists)
    {
        return withScheme(id, draftMaybe(returnDraftIfExists, new Function<RestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(RestWorkflowScheme input)
            {
                return responseOk(input.asBean());
            }
        }));
    }

    /**
     * Create a new workflow scheme.
     *
     * The body contains a representation of the new scheme. Values not passed are assumed to be set to their defaults.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#CREATE_EXAMPLE}
     *
     * @response.representation.201.doc
     *      The newly created scheme.
     *
     * @response.representation.201.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @POST
    public Response createScheme(WorkflowSchemeBean bean)
    {
        ServiceOutcome<AssignableRestWorkflowScheme> outcome = factory.create(bean);
        if (outcome.isValid())
        {
            return responseCreated(outcome.getReturnedValue().asBean());
        }
        else
        {
            return responseError(outcome);
        }
    }

    /**
     * Delete the passed workflow scheme.
     *
     * @param id the id of the scheme.
     *
     * @response.representation.204.doc
     *      If the scheme was deleted.
     *
     * @response.representation.404.doc
     *      Returned if the requested scheme does not exist.
     * @response.representation.400.doc
     *      Returned if the requested scheme is active (i.e. being used by JIRA).
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}")
    public Response deleteScheme(@PathParam("id") long id)
    {
        ServiceOutcome<AssignableRestWorkflowScheme> outcome = factory.getById(id);
        if (!outcome.isValid())
        {
            return responseError(outcome);
        }
        else
        {
            return response(outcome.getReturnedValue().delete());
        }
    }

    /**
     * Update the passed workflow scheme.
     *
     * The body of the request is a representation of the workflow scheme. Values not passed are assumed to indicate
     * no change for that field.
     *
     * The passed representation can have its updateDraftIfNeeded flag set to true to indicate that the draft
     * should be created and/or updated when the actual scheme cannot be edited (e.g. when the scheme is being used by
     * a project). Values not appearing the body will not be touched.
     *
     * @param id the id of the scheme.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#UPDATE_EXAMPLE}
     *
     * @response.representation.200.doc
     *      The updated scheme.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the requested scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path("{id}")
    public Response update(@PathParam ("id") long id,
            final WorkflowSchemeBean bean)
    {
        return withScheme(id, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                return input.update(bean);
            }
        }));
    }

    /**
     * Returns the requested draft workflow scheme to the caller.
     *
     * @param id the id of the parent scheme.
     * @return the requested draft workflow scheme.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the scheme exists and the caller has permission to see it.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DRAFT_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the requested draft scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}/draft")
    public Response getDraftById(@PathParam ("id") long id)
    {
        return withDraft(id, new Function<DraftRestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(DraftRestWorkflowScheme input)
            {
                return responseOk(input.asBean());
            }
        });
    }

    /**
     * Delete the passed draft workflow scheme.
     *
     * @param id the id of the parent scheme.
     *
     * @response.representation.204.doc
     *      If the scheme was deleted.
     *
     * @response.representation.404.doc
     *      Returned if the requested draft scheme or parent scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}/draft")
    public Response deleteDraftById(@PathParam ("id") long id)
    {
        return withDraft(id, new Function<DraftRestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(DraftRestWorkflowScheme input)
            {
                return response(input.delete());
            }
        });
    }

    /**
     * Create a draft for the passed scheme. The draft will be a copy of the state of the parent.
     *
     * @param id the id of the parent scheme.
     *
     * @response.representation.201.doc
     *      The newly created scheme.
     *
     * @response.representation.201.example
     *      {@link WorkflowSchemeDocumentation#DRAFT_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @POST
    @Path("{id}/createdraft")
    public Response createDraftForParent(@PathParam ("id") long id)
    {
        ServiceOutcome<AssignableRestWorkflowScheme> outcome = factory.getById(id);
        if (!outcome.isValid())
        {
            return responseError(outcome);
        }

        ServiceOutcome<DraftRestWorkflowScheme> draftOutcome = outcome.getReturnedValue().createDraftScheme();
        if (!draftOutcome.isValid())
        {
            return responseError(draftOutcome);
        }
        else
        {
            return responseCreated(draftOutcome.getReturnedValue().asBean());
        }
    }

    /**
     * Update a draft workflow scheme. The draft will created if necessary.
     *
     * The body is a representation of the workflow scheme. Values not passed are assumed to indicate no change for that field.
     *
     * @param id the id of the parent scheme.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#UPDATE_EXAMPLE}
     *
     * @response.representation.200.doc
     *      The updated/created scheme.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DRAFT_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the requested scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path ("{id}/draft")
    public Response updateDraft(@PathParam ("id") final long id, final WorkflowSchemeBean bean)
    {
        return withScheme(id, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                return input.updateDraft(bean);
            }
        }));
    }

    /**
     * Returns the workflow mappings or requested mapping to the caller for the passed scheme.
     *
     * @param id the id of the scheme.
     * @param returnDraftIfExists when true indicates that a scheme's draft, if it exists, should be queried instead of
     *  the scheme itself.
     * @param workflowName the workflow mapping to return. Null can be passed to return all mappings. Must be a valid workflow name.
     * @return the requested workflow scheme.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#WF_MAPPING_BEAN}
     *      {@link WorkflowSchemeDocumentation#WF_MAPPING_LIST}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or workflow does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}/workflow")
    public Response getWorkflow(@PathParam("id") long id,
            @QueryParam("workflowName") final String workflowName,
            @QueryParam("returnDraftIfExists") @DefaultValue("false") final boolean returnDraftIfExists)
    {
        return withScheme(id, checkWorkflow(workflowName, true, draftMaybe(returnDraftIfExists, new Function<RestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(RestWorkflowScheme input)
            {
                return responseOk(workflowName == null ? input.asWorkflowBeans() : input.asWorkflowBean(workflowName));
            }
        })));
    }

    /**
     * Returns the draft workflow mappings or requested mapping to the caller.
     *
     * @param id the id of the parent scheme.
     * @param workflowName the workflow mapping to return. Null can be passed to return all mappings. Must be a valid workflow name.
     * @return the requested workflow scheme.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#WF_MAPPING_BEAN}
     *      {@link WorkflowSchemeDocumentation#WF_MAPPING_LIST}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or workflow does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}/draft/workflow")
    public Response getDraftWorkflow(@PathParam("id") long id,
            final @QueryParam("workflowName") String workflowName)
    {
        return withDraft(id, checkWorkflow(workflowName, true, new Function<RestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(RestWorkflowScheme input)
            {
                return responseOk(workflowName == null ? input.asWorkflowBeans() : input.asWorkflowBean(workflowName));

            }
        }));
    }

    /**
     * Delete the passed workflow from the workflow scheme.
     *
     * @param id the id of the scheme.
     * @param workflowName the name of the workflow to delete.
     * @param updateDraftIfNeeded flag to indicate if a draft should be created if necessary to delete the workflow
     *   from the scheme.
     *
     * @response.representation.200.doc
     *      The scheme with the workflow deleted.
     *
     * @response.representation.404.doc
     *      Returned if the requested scheme or workflow does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}/workflow")
    public Response deleteWorkflowMapping(@PathParam("id") long id,
            @QueryParam("workflowName") final String workflowName,
            @QueryParam("updateDraftIfNeeded") final boolean updateDraftIfNeeded)
    {

        return withScheme(id, checkWorkflow(workflowName, false, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                return input.deleteWorkflow(workflowName, updateDraftIfNeeded);
            }
        })));
    }

    /**
     * Delete the passed workflow from the draft workflow scheme.
     *
     * @param id the id of the parent scheme.
     * @param workflowName the name of the workflow to delete.
     *
     * @response.representation.200.doc
     *      The scheme with the workflow deleted.
     *
     * @response.representation.404.doc
     *      Returned if the requested scheme or workflow does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}/draft/workflow")
    public Response deleteDraftWorkflowMapping(@PathParam("id") long id,
            @QueryParam("workflowName") final String workflowName)
    {
        return withDraft(id, checkWorkflow(workflowName, false, buildResponse(new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.deleteWorkflow(workflowName);
            }
        })));
    }

    /**
     * Update the scheme to include the passed mapping.
     *
     * The body is a representation of the workflow mapping.
     * Values not passed are assumed to indicate no change for that field.
     *
     * The passed representation can have its updateDraftIfNeeded flag set to true to indicate that the draft
     * should be created/updated when the actual scheme cannot be edited.
     *
     * @param id the id of the scheme.
     * @param workflowName the name of the workflow mapping to update.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#WF_MAPPING_UPDATE}
     *
     * @response.representation.200.doc
     *      The updated scheme.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path("{id}/workflow")
    public Response updateWorkflowMapping(@PathParam("id") long id,
            @QueryParam("workflowName") final String workflowName,
            final WorkflowMappingBean updateBean)
    {
        return withScheme(id, checkWorkflow(workflowName, false, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                updateBean.setWorkflow(workflowName);
                return input.updateWorkflowMappings(updateBean);
            }
        })));
    }

    /**
     * Update the draft scheme to include the passed mapping.
     *
     * The body is a representation of the workflow mapping.
     * Values not passed are assumed to indicate no change for that field.
     *
     * @param id the id of the parent scheme.
     * @param workflowName the name of the workflow mapping to update.
     *
     * @request.representation.mediaType
     *      application/json
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#WF_MAPPING_UPDATE}
     *
     * @response.representation.200.doc
     *      The updated scheme.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path("{id}/draft/workflow")
    public Response updateDraftWorkflowMapping(@PathParam("id") long id,
            @QueryParam("workflowName") final String workflowName,
            final WorkflowMappingBean updateBean)
    {
        return withDraft(id, checkWorkflow(workflowName, false, buildResponse(new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                updateBean.setWorkflow(workflowName);
                return input.updateWorkflowMappings(updateBean);
            }
        })));
    }

    /**
     * Returns the issue type mapping for the passed workflow scheme.
     *
     * @param id the id of the scheme.
     * @param returnDraftIfExists when true indicates that a scheme's draft, if it exists, should be queried instead of
     *  the scheme itself.
     * @param issueType the issue type to query.
     * @return the requested mapping.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#IT_MAPPING_BEAN}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or issue type does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}/issuetype/{issueType}")
    public Response getIssueType(@PathParam("id") long id,
            @PathParam("issueType") final String issueType,
            @QueryParam("returnDraftIfExists") @DefaultValue("false") boolean returnDraftIfExists)
    {
        return withScheme(id, checkIssueType(issueType, draftMaybe(returnDraftIfExists, new Function<RestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(RestWorkflowScheme input)
            {
                return responseOk(input.asIssueTypeBean(issueType));
            }
        })));
    }

    /**
     * Returns the issue type mapping for the passed draft workflow scheme.
     *
     * @param id the id of the parent scheme.
     * @param issueType the issue type to query.
     * @return the requested mapping.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#IT_MAPPING_BEAN}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or issue type does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}/draft/issuetype/{issueType}")
    public Response getDraftIssueType(@PathParam("id") long id, @PathParam("issueType") final String issueType)
    {
        return withDraft(id, checkIssueType(issueType, new Function<DraftRestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(DraftRestWorkflowScheme input)
            {
                return responseOk(input.asIssueTypeBean(issueType));
            }
        }));
    }

    /**
     * Remove the specified issue type mapping from the scheme.
     *
     * @param id the id of the scheme.
     * @param issueType the issue type to remove.
     * @param updateDraftIfNeeded when true will create and return a draft when the workflow scheme cannot be edited
     * (e.g. when it is being used by a project).
     *
     * @return the updated scheme or an error.
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or issue type does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}/issuetype/{issueType}")
    public Response deleteIssueType(@PathParam("id") long id,
            @PathParam("issueType") final String issueType,
            @QueryParam("updateDraftIfNeeded") final boolean updateDraftIfNeeded)
    {
        return withScheme(id, checkIssueType(issueType, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                return input.removeIssueType(issueType, updateDraftIfNeeded);
            }
        })));
    }

    /**
     * Remove the specified issue type mapping from the draft scheme.
     *
     * @param id the parent of the draft scheme.
     * @param issueType the issue type to remove.
     *
     * @return the updated scheme or an error.
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DRAFT_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or issue type does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}/draft/issuetype/{issueType}")
    public Response deleteDraftIssueType(@PathParam("id") long id, @PathParam("issueType") final String issueType)
    {
        return withDraft(id, checkIssueType(issueType, buildResponse(new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.removeIssueType(issueType);
            }
        })));
    }

    /**
     * Set the issue type mapping for the passed scheme.
     *
     * The passed representation can have its updateDraftIfNeeded flag set to true to indicate that
     * the draft should be created/updated when the actual scheme cannot be edited.
     *
     * @param id the id of the scheme.
     * @param issueType the issue type being set.
     * @param mappingBean the new mapping for the issue type.
     *
     * @return updated scheme or an error.
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#IT_MAPPING_UPDATE}
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or issue type does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path("{id}/issuetype/{issueType}")
    public Response setIssueType(@PathParam("id") long id,
            final @PathParam("issueType") String issueType,
            final IssueTypeMappingBean mappingBean)
    {
        return withScheme(id, checkIssueType(issueType, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                mappingBean.setIssueType(issueType);
                return input.updateIssueTypeMappings(mappingBean);
            }
        })));
    }

    /**
     * Set the issue type mapping for the passed draft scheme.
     *
     * The passed representation can have its updateDraftIfNeeded flag set to true to indicate that
     * the draft should be created/updated when the actual scheme cannot be edited.
     *
     * @param id the id of the parent scheme.
     * @param issueType the issue type being set.
     * @param mappingBean the new mapping for the issue type.
     *
     * @return updated scheme or an error.
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#IT_MAPPING_UPDATE}
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DRAFT_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if either the requested scheme or issue type does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path("{id}/draft/issuetype/{issueType}")
    public Response setDraftIssueType(@PathParam("id") long id,
            @PathParam("issueType") final String issueType,
            final IssueTypeMappingBean mappingBean)
    {
        return withDraft(id, checkIssueType(issueType, buildResponse(new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                mappingBean.setIssueType(issueType);
                return input.updateIssueTypeMappings(mappingBean);
            }
        })));
    }

    /**
     * Return the default workflow from the passed workflow scheme.
     *
     * @param id the id of the scheme.
     * @param returnDraftIfExists when true indicates that a scheme's draft, if it exists, should be queried instead of
     *  the scheme itself.
     * @return the default workflow or an error.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DEF_BEAN}
     *
     * @response.representation.404.doc
     *      Returned when the workflow scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}/default")
    public Response getDefault(@PathParam("id") long id,
            @QueryParam("returnDraftIfExists") @DefaultValue("false") boolean returnDraftIfExists)
    {
        return withScheme(id, draftMaybe(returnDraftIfExists, new Function<RestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(RestWorkflowScheme input)
            {
                return responseOk(input.asDefaultBean());
            }
        }));
    }

    /**
     * Return the default workflow from the passed draft workflow scheme to the caller.
     *
     * @param id the id of the parent scheme.
     * @return the default workflow or an error.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DEF_BEAN}
     *
     * @response.representation.404.doc
     *      Returned when the workflow scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @GET
    @Path("{id}/draft/default")
    public Response getDraftDefault(@PathParam("id") long id)
    {
        return withDraft(id, new Function<DraftRestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(DraftRestWorkflowScheme input)
            {
                return responseOk(input.asDefaultBean());
            }
        });
    }

    /**
     * Remove the default workflow from the passed workflow scheme.
     *
     * @param id the id of the scheme.
     * @param updateDraftIfNeeded when true will create and return a draft when the workflow scheme cannot be edited
     * (e.g. when it is being used by a project).
     * @return the updated scheme or an error.

     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}/default")
    public Response deleteDefault(@PathParam("id") long id,
            final @QueryParam("updateDraftIfNeeded") boolean updateDraftIfNeeded)
    {
        return withScheme(id, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                return input.removeDefault(updateDraftIfNeeded);
            }
        }));
    }

    /**
     * Remove the default workflow from the passed draft workflow scheme.
     *
     * @param id the id of the parent scheme.
     * @return the updated scheme or an error.

     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DRAFT_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @DELETE
    @Path("{id}/draft/default")
    public Response deleteDraftDefault(@PathParam("id") long id)
    {
        return withDraft(id, buildResponse(new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.removeDefault();
            }
        }));
    }

    /**
     * Set the default workflow for the passed workflow scheme.
     *
     * The passed representation can have its
     * updateDraftIfNeeded flag set to true to indicate that the draft should be created/updated when the actual scheme
     * cannot be edited.
     *
     * @param id the id of the scheme.
     * @param bean the new default.
     * @return the updated scheme or an error.
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#DEF_UPDATE}
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path("{id}/default")
    public Response updateDefault(@PathParam("id") long id, final DefaultBean bean)
    {
        return withScheme(id, buildResponse(new Function<AssignableRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(AssignableRestWorkflowScheme input)
            {
                return input.updateDefault(bean);
            }
        }));
    }

    /**
     * Set the default workflow for the passed draft workflow scheme.
     *
     * @param id the id of the parent scheme.
     * @param bean the new default.
     * @return the updated scheme or an error.
     *
     * @request.representation.example
     *      {@link WorkflowSchemeDocumentation#DEF_UPDATE}
     *
     * @response.representation.200.doc
     *      Returned on success.
     *
     * @response.representation.200.example
     *      {@link WorkflowSchemeDocumentation#DRAFT_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the scheme does not exist.
     *
     * @response.representation.401.doc
     *      Returned if there is no user or if the user has not entered a websudo
     *      session.
     */
    @PUT
    @Path("{id}/draft/default")
    public Response updateDraftDefault(@PathParam("id") long id, final DefaultBean bean)
    {
        return withDraft(id, buildResponse(new Function<DraftRestWorkflowScheme, ServiceOutcome<? extends RestWorkflowScheme>>()
        {
            @Override
            public ServiceOutcome<? extends RestWorkflowScheme> apply(DraftRestWorkflowScheme input)
            {
                return input.updateDefault(bean);
            }
        }));
    }

    private static Response response(ServiceOutcome<?> outcome)
    {
        if (outcome.isValid())
        {
            if (outcome.getReturnedValue() == null)
            {
                return responseNoContent();
            }
            else
            {
                return responseOk(outcome.getReturnedValue());
            }
        }
        else
        {
            return responseError(outcome);
        }
    }

    private static Response responseNoContent()
    {
        return Response.noContent()
                .cacheControl(never()).build();
    }

    private static Response responseOk(Object entity)
    {
        return Response.ok(entity)
                .cacheControl(never()).build();
    }

    private static Response responseCreated(WorkflowSchemeBean entity)
    {
        return Response.created(entity.getSelf()).entity(entity)
                .cacheControl(never()).build();
    }

    private static Response responseError(ServiceOutcome<?> outcome)
    {
        final ErrorCollection errorCollection = outcome.getErrorCollection();
        Reason reason = getWorstReason(errorCollection.getReasons());
        if (reason == null)
        {
            reason = SERVER_ERROR;
        }

        return Response.status(reason.getHttpStatusCode())
                .entity(com.atlassian.jira.rest.api.util.ErrorCollection.of(errorCollection))
                .cacheControl(never())
                .build();
    }

    private Response responseError(Response.Status reason, String key, Object...args)
    {
        String msg = authenticationContext.getI18nHelper().getText(key, args);

        return Response.status(reason)
                .entity(com.atlassian.jira.rest.api.util.ErrorCollection.of(msg))
                .cacheControl(never())
                .build();
    }

    private Response withScheme(long id, Function<? super AssignableRestWorkflowScheme, Response> function)
    {
        ServiceOutcome<AssignableRestWorkflowScheme> outcome = factory.getById(id);
        if (!outcome.isValid())
        {
            return responseError(outcome);
        }

        return function.apply(outcome.getReturnedValue());
    }

    private Response withDraft(long parentId, Function<? super DraftRestWorkflowScheme, Response> function)
    {
        ServiceOutcome<AssignableRestWorkflowScheme> outcome = factory.getById(parentId);
        if (!outcome.isValid())
        {
            return responseError(outcome);
        }

        ServiceOutcome<DraftRestWorkflowScheme> draftOutcome = outcome.getReturnedValue().getDraftScheme();
        if (!draftOutcome.isValid())
        {
            return responseError(draftOutcome);
        }
        return function.apply(draftOutcome.getReturnedValue());
    }

    private <I> Function<I, Response> checkIssueType(final String issueType, final Function<I, Response> function)
    {
        return new Function<I, Response>()
        {
            @Override
            public Response apply(@Nullable I input)
            {
                if (issueType == null || issueTypeManager.getIssueType(issueType) == null)
                {
                    return responseError(Response.Status.NOT_FOUND, "rest.error.workflowscheme.issuetype.does.not.exist", issueType);
                }
                return function.apply(input);
            }
        };
    }

    private <I> Function<I, Response> checkWorkflow(final String workflow, final boolean allowNull, final Function<I, Response> function)
    {
        return new Function<I, Response>()
        {
            @Override
            public Response apply(@Nullable I input)
            {
                if (workflow == null)
                {
                    if (!allowNull)
                    {
                        return responseError(Response.Status.NOT_FOUND, "rest.error.workflowscheme.workflow.not.passed", workflow);
                    }
                }
                else if (workflowManager.getWorkflow(workflow) == null)
                {
                    return responseError(Response.Status.NOT_FOUND, "rest.error.workflowscheme.workflow.does.not.exist", workflow);
                }
                return function.apply(input);
            }
        };
    }

    private <I> Function<I, Response> buildResponse(final Function<I, ServiceOutcome<? extends RestWorkflowScheme>> function)
    {
        return new Function<I, Response>()
        {
            @Override
            public Response apply(@Nullable I input)
            {
                final ServiceOutcome<? extends RestWorkflowScheme> outcome = function.apply(input);
                if (outcome.isValid())
                {
                    return responseOk(outcome.getReturnedValue().asBean());
                }
                else
                {
                    return responseError(outcome);
                }
            }
        };
    }
    private Function<AssignableRestWorkflowScheme, Response> draftMaybe(final boolean draft, final Function<RestWorkflowScheme, Response> function)
    {
        return new Function<AssignableRestWorkflowScheme, Response>()
        {
            @Override
            public Response apply(AssignableRestWorkflowScheme input)
            {
                if (draft)
                {
                    return function.apply(input.getDraftMaybe());
                }
                else
                {
                    return function.apply(input);
                }
            }
        };
    }
}
