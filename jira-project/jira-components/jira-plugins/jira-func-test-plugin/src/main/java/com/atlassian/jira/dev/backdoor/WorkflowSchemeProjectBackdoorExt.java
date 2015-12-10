package com.atlassian.jira.dev.backdoor;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.testkit.plugin.workflows.WorkflowSchemeDataFactory;
import com.atlassian.jira.testkit.plugin.workflows.WorkflowSchemeProjectBackdoor;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.AssignableWorkflowSchemeMigrationHelper;
import com.atlassian.jira.workflow.migration.MigrationHelperFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.concurrent.ExecutionException;

import static com.atlassian.jira.testkit.plugin.util.CacheControl.never;

/**
 * Extended WorkflowSchemeProjectBackdoor.
 *
 * @since v5.2
 */
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
@Consumes ({ MediaType.APPLICATION_JSON })
@Path ("project/{projectKey}/workflowscheme")
public class WorkflowSchemeProjectBackdoorExt extends WorkflowSchemeProjectBackdoor
{
    private final WorkflowSchemeManager workflowSchemeManager;
    private final MigrationHelperFactory migrationHelperFactory;
    private final TaskManager taskManager;

    public WorkflowSchemeProjectBackdoorExt(WorkflowSchemeManager workflowSchemeManager,
            MigrationHelperFactory migrationHelperFactory,
            WorkflowSchemeDataFactory dataFactory,
            ProjectManager projectManager,
            TaskManager taskManager)
    {
        super(workflowSchemeManager, dataFactory, projectManager);
        this.workflowSchemeManager = workflowSchemeManager;
        this.migrationHelperFactory = migrationHelperFactory;
        this.taskManager = taskManager;
    }

    @GET
    @Path ("project/{projectKey}/workflowscheme/foo")
    public String asdf(@PathParam ("projectKey") String projectKey)
    {
        return "ok";
    }

    @DELETE
    public Response setDefault(@PathParam ("projectKey") String projectKey)
    {
        Project project = getProject(projectKey);
        if (project == null)
        {
            throw new WebApplicationException(404);
        }

        return migrateScheme(workflowSchemeManager.getDefaultWorkflowScheme(), project);
    }

    @POST
    public Response change(@PathParam ("projectKey") String projectKey, long id)
    {
        Project project = getProject(projectKey);
        if (project == null) { throw new WebApplicationException(404); }

        final AssignableWorkflowScheme scheme = workflowSchemeManager.getWorkflowSchemeObj(id);
        if (scheme == null)
        {
            throw new WebApplicationException(404);
        }

        return migrateScheme(scheme, project);
    }

    private Response migrateScheme(AssignableWorkflowScheme scheme, Project project)
    {
        try
        {
            final AssignableWorkflowSchemeMigrationHelper migrationHelper = migrationHelperFactory.createMigrationHelper(project, scheme);
            if (!migrationHelper.doQuickMigrate())
            {
                final TaskDescriptor<WorkflowMigrationResult> async = migrationHelper.migrateAsync();
                taskManager.waitUntilTaskCompletes(async.getTaskId());
                TaskDescriptor<WorkflowMigrationResult> completedMigration = taskManager.getTask(async.getTaskId());

                final WorkflowMigrationResult result = completedMigration.getResult();
                if (result.getResult() == WorkflowMigrationResult.SUCCESS && result.getNumberOfFailedIssues() == 0)
                {
                    return ok(scheme);
                }
                else
                {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(String.format("%d issues failed to migrate.", result.getNumberOfFailedIssues()))
                            .cacheControl(never()).build();
                }
            }
            else
            {
                return ok(scheme);
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e.getCause());
        }
    }
}
