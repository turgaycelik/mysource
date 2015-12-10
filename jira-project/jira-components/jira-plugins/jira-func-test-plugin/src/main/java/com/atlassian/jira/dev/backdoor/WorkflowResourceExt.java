package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;
import static com.google.common.collect.Iterables.transform;

/**
 * @since v5.2
 */
@AnonymousAllowed
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
@Path ("workflow")
public class WorkflowResourceExt
{
    private static final Function<JiraWorkflow, Object> TO_BEAN = new Function<JiraWorkflow, Object>()
    {
        @Override
        public Object apply(JiraWorkflow input)
        {
            return new AssignableWorkflow(input);
        }
    };


    private final WorkflowManager workflowManager;

    public WorkflowResourceExt(WorkflowManager workflowManager) {this.workflowManager = workflowManager;}

    @POST
    public Response createWorkflow(String name)
    {
        JiraWorkflow workflow = new ConfigurableJiraWorkflow(name, workflowManager);
        workflowManager.createWorkflow("admin", workflow);

        return okWorkflow(workflow);
    }

    @POST
    @Path ("{name}")
    public Response changeDescription(@PathParam("name") String name, String description)
    {
        final JiraWorkflow workflow = workflowManager.getWorkflowClone(name);
        workflowManager.updateWorkflowNameAndDescription("admin", workflow, name, description);
        return ok("");
    }

    @Path ("createdraft")
    @POST
    public Response createWorkflowDraft(String parentName)
    {
        final JiraWorkflow workflow = workflowManager.getWorkflow(parentName);
        if (parentName == null)
        {
            return fourOh4();
        }

        workflowManager.createDraftWorkflow("admin", parentName);

        return okWorkflow(workflow);
    }

    @GET
    public Response getAll(@QueryParam("name") String name)
    {
        if (StringUtils.isBlank(name))
        {
            return Response.ok(transform(workflowManager.getWorkflows(), TO_BEAN)).cacheControl(never()).build();
        }
        else
        {
            final JiraWorkflow workflow = workflowManager.getWorkflow(name);
            if (name == null)
            {
                return fourOh4();
            }
            return okWorkflow(workflow);
        }
    }

    @DELETE
    public Response deleteWorkflow(@QueryParam("name") String name)
    {
        final JiraWorkflow workflow = workflowManager.getWorkflow(name);
        workflowManager.deleteWorkflow(workflow);
        return ok("");
    }

    private Response okWorkflow(JiraWorkflow workflow)
    {
        return ok(new AssignableWorkflow(workflow));
    }

    private Response ok(Object entity)
    {
        return Response.ok(entity).cacheControl(never()).build();
    }

    private Response fourOh4() {return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();}


    @JsonAutoDetect
    public static class AssignableWorkflow
    {
        private String name;
        private String description;
        private boolean hasDraft;

        public AssignableWorkflow()
        {
        }

        public AssignableWorkflow(JiraWorkflow workflow)
        {
            name = workflow.getName();
            description = workflow.getDescription();
            hasDraft = workflow.hasDraftWorkflow();
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
