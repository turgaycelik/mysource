package com.atlassian.jira.rest.v2.admin;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.v2.admin.WorkflowTransitionResource.WorkflowMode;

/**
 * REST resource for retrieving workflows.
 *
 * @since v5.2
 */
@Path ("workflow")
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
public class WorkflowsResource
{
    private final WorkflowManager workflowManager;
    private final DateTimeFormatter dateTimeFormatter;
    private final UserManager userManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final WorkflowTransitionResource.Factory transitionFactory;

    public WorkflowsResource(WorkflowManager workflowManager, DateTimeFormatter dateTimeFormatter,
            UserManager userManager, JiraAuthenticationContext jiraAuthenticationContext,
            PermissionManager permissionManager,
            WorkflowTransitionResource.Factory transitionFactory)
    {
        this.workflowManager = workflowManager;
        this.dateTimeFormatter = dateTimeFormatter;
        this.userManager = userManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.transitionFactory = transitionFactory;
    }

    /**
     * Returns all workflows.
     *
     * @return all workflows.
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned if the currently authenticated user has administration permission. Contains a
     *      full representation of every workflow.
     *
     * @response.representation.401.doc
     *      Returned if the currently authenticated user does not have administration permission.
     */
    @GET
    public Response getAllWorkflows(@QueryParam("workflowName") String workflowName)
    {
        verifyUserHasAdminPermission();

        Object entity = workflowName == null ? actuallyGetAllWorkflows() : jiraWorkflowToWorkflowBean(workflowManager.getWorkflow(workflowName));

        return Response.ok(entity).cacheControl(never()).build();
    }

    /**
     * Return a sub-resource to handle transitions.
     *
     * @param workflowName the name of the workflow to use.
     * @param workflowMode the type of workflow to use. Can either be "live" or "draft".
     *
     * @return A {@link WorkflowTransitionResource} to handle the transition.
     */
    @Path("transitions")
    public WorkflowTransitionResource getWorkflowTransitions(@QueryParam("workflowName") String workflowName,
            @QueryParam("workflowMode") String workflowMode)
    {
        verifyUserHasAdminPermission();

        return transitionFactory.getResource(workflowName,
                WorkflowMode.getMode(workflowMode));
    }

    private void verifyUserHasAdminPermission()
    {
        ApplicationUser user = jiraAuthenticationContext.getUser();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            throw new NotAuthorisedWebException();
        }
    }

    private Iterable<WorkflowBean> actuallyGetAllWorkflows()
    {
        return Lists.newArrayList(Collections2.transform(workflowManager.getWorkflows(), new Function<JiraWorkflow, WorkflowBean>()
        {
            @Override
            public WorkflowBean apply(JiraWorkflow workflow)
            {
                return jiraWorkflowToWorkflowBean(workflow);
            }
        }));
    }

    private WorkflowBean jiraWorkflowToWorkflowBean(JiraWorkflow jiraWorkflow)
    {
        String name = jiraWorkflow.getName();
        String description = jiraWorkflow.getDescription();
        int steps = jiraWorkflow.getDescriptor().getSteps().size();
        boolean isDefault = jiraWorkflow.isDefault();

        String updateDate = jiraWorkflow.getUpdatedDate() != null ? dateTimeFormatter.forLoggedInUser().format(jiraWorkflow.getUpdatedDate()) : null;

        ApplicationUser updateAuthor = jiraWorkflow.getUpdateAuthor();
        String updateAuthorName = userManager.isUserExisting(updateAuthor)?updateAuthor.getDisplayName():null;

        return new WorkflowBean(name, description, updateDate, updateAuthorName, steps, isDefault);
    }
}
