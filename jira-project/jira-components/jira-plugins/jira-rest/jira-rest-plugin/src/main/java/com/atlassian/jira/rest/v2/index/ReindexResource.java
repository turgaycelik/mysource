package com.atlassian.jira.rest.v2.index;

import com.atlassian.jira.config.BackgroundIndexTaskContext;
import com.atlassian.jira.config.ForegroundIndexTaskContext;
import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.jira.web.action.admin.index.ActivateAsyncIndexerCommand;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import com.atlassian.jira.web.action.admin.index.ReIndexBackgroundIndexerCommand;
import com.atlassian.johnson.JohnsonEventContainer;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * REST resource for starting/stopping/querying indexing.
 *
 * @since v6.1.4
 */
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("reindex")
public class ReindexResource
{
    private static final Logger log = Logger.getLogger(ReindexResource.class);

    private final TaskManager taskManager;
    private final TaskDescriptorHelper taskDescriptorHelper;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IndexLifecycleManager indexLifecycleManager;
    private final PermissionManager permissionManager;
    private final URI location;
    private final I18nHelper.BeanFactory i18nBeanFactory;

    public ReindexResource(final IndexLifecycleManager indexLifecycleManager, final TaskManager taskManager,
            final JiraAuthenticationContext jiraAuthenticationContext,
            final PermissionManager permissionManager, final JiraBaseUrls jiraBaseUrls, final I18nHelper.BeanFactory i18nBeanFactory)
    {
        this.taskManager = taskManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.i18nBeanFactory = i18nBeanFactory;
        this.taskDescriptorHelper = new TaskDescriptorHelper(taskManager);
        this.indexLifecycleManager = indexLifecycleManager;
        this.permissionManager = permissionManager;
        try
        {
            this.location = new URI(jiraBaseUrls.restApi2BaseUrl() + "reindex/" );
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Kicks off a reindex.  Need Admin permissions to perform this reindex.
     *
     * @param type Case insensitive String indicating type of reindex.  If omitted, then defaults to BACKGROUND_PREFERRED
     * @param indexComments Indicates that comments should also be reindexed. Not relevant for foreground reindex, where comments are always reindexed.
     * @param indexChangeHistory Indicates that changeHistory should also be reindexed. Not relevant for foreground reindex, where changeHistory is always reindexed.
     * @return  Response that provides a redirect to the GET.
     *
     * @response.representation.202.mediaType application/json
     *
     * @response.representation.202.doc
     *      Returns a representation of the progress of the re-index operation.
     *
     * @response.representation.202.example
     *      {@link ReindexBean#DOC_EXAMPLE}
     *
     */
    @POST
    public Response reindex(@QueryParam ("type") final String type,
            @QueryParam ("indexComments") @DefaultValue ("false") final boolean indexComments,
            @QueryParam ("indexChangeHistory") @DefaultValue ("false") final boolean indexChangeHistory)
    {
        TaskDescriptor<IndexCommandResult> task;
        ReindexBean reindexBean;
        final ReindexBean.Type requestedIndexType = ReindexBean.fromString(type);
        final ApplicationUser user = jiraAuthenticationContext.getUser();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            throw new NotAuthorisedWebException();
        }
        try
        {
            task = taskDescriptorHelper.getActiveIndexTask();
            if (task != null)
            {
                return Response.status(Response.Status.CONFLICT).
                        entity(ReindexBean.fromTaskDescriptor(task)).location(location).build();
            }
            TaskDescriptor<IndexCommandResult> indexingTask;
            if (requestedIndexType.equals(ReindexBean.Type.FOREGROUND))
            {
                indexingTask =  triggerForegroundIndexing();
            }
            else
            {
                final boolean canBeBackgroundIndexed = indexLifecycleManager.isIndexConsistent();
                if (canBeBackgroundIndexed)
                {
                    indexingTask = triggerBackgroundIndexing(indexComments, indexChangeHistory);
                }
                else
                {
                    if (requestedIndexType.equals(ReindexBean.Type.BACKGROUND_PREFFERED))
                    {
                        indexingTask = triggerForegroundIndexing();
                    }
                    else
                    {
                        return Response.status(Response.Status.CONFLICT).
                                entity(i18n().getText("admin.indexing.strategy.background.unsafe")).
                                cacheControl(never()).
                                build();
                    }
                }
            }
            reindexBean = ReindexBean.fromTaskDescriptor(indexingTask);
            return Response.status(Response.Status.ACCEPTED).
                    location(location).
                    entity(reindexBean).
                    header("Retry-After", 10L).
                    cacheControl(never()).
                    build();
        }
        catch (Exception e)
        {
            return Response.serverError().build();
        }
    }

    /**
     * Returns information on the system reindexes.  If a reindex is currently taking place then information about this reindex is returned.
     * If there is no active index task, then returns information about the latest reindex task run, otherwise returns a 404
     * indicating that no reindex has taken place.
     *
     * @param taskId the id of an indexing task you wish to obtain details on.  If omitted, then defaults to the standard behaviour and
     * returns information on the active reindex task, or the last task to run if no reindex is taking place. .  If there is no
     * reindexing task with that id then a 404 is returned.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a representation of the progress of the re-index operation.
     *
     * @response.representation.200.example
     *      {@link ReindexBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if there is no re-indexing task found
     *
     * @since v6.1.4
     */
    @GET
    public Response getReindexInfo(@QueryParam("taskId") final long taskId)
    {
        try
        {
            TaskDescriptor<IndexCommandResult> task = taskDescriptorHelper.getIndexTask(taskId);

            if (task == null)
            {
                if (taskId > 0)
                {
                    return build404Response();
                }
                task = taskDescriptorHelper.getLastindexTask();
                if (task == null)
                {
                    return build404Response();
                }
            }
            if (task.isFinished())
            {
                return buildTaskCompletedResponse(task);
            }
            else
            {
                return buildTaskRunningResponse(task);
            }
        }
        catch (Exception e)
        {
            return Response.serverError().entity(e.getMessage()).cacheControl(never()).build();
        }
    }

    private Response buildTaskRunningResponse(final TaskDescriptor<IndexCommandResult> task)
            throws ExecutionException, InterruptedException
    {
        final ReindexBean bean = ReindexBean.fromTaskDescriptor(task);
        final long currentProgress = bean.getCurrentProgress();

        final long retryAfter =  currentProgress > 0 ?
                 ((100 - currentProgress)/currentProgress) * task.getElapsedRunTime()/1000 + 1 :
                 10;
        return Response.status(Response.Status.SEE_OTHER).location(location).
                 entity(bean).header("Retry-After", retryAfter).cacheControl(never()).build();
    }

    private Response buildTaskCompletedResponse(final TaskDescriptor<IndexCommandResult> task)
            throws ExecutionException, InterruptedException
    {
        final IndexCommandResult result = task.getResult();
        if (result.isSuccessful())
        {
            return Response.ok(ReindexBean.fromTaskDescriptor(task)).lastModified(task.getFinishedTimestamp()).build();
        }
        else
        {
            return Response.serverError().entity(result.getErrorCollection()).cacheControl(never()).build();
        }
    }

    private Response build404Response()
    {
        return Response.status(Response.Status.NOT_FOUND).entity(i18n().getText("admin.indexing.no.task.found")).build();
    }

    private TaskDescriptor<IndexCommandResult> triggerBackgroundIndexing(final boolean comments, final boolean changeHistory)
    {
        return submitIndexingTask(new ReIndexBackgroundIndexerCommand(indexLifecycleManager, log, i18n(), i18nBeanFactory),
                new BackgroundIndexTaskContext(), true);
    }

    private TaskDescriptor<IndexCommandResult> triggerForegroundIndexing()
    {
        return submitIndexingTask(new ActivateAsyncIndexerCommand(true, getJohnsonEventContainer(), indexLifecycleManager, log,
                i18n(), i18nBeanFactory),
                new ForegroundIndexTaskContext(), false);
    }

    private I18nHelper i18n()
    {
        return jiraAuthenticationContext.getI18nHelper();
    }

    private TaskDescriptor<IndexCommandResult> submitIndexingTask(final Callable<IndexCommandResult> cmd, final IndexTaskContext indexTaskContext, boolean cancellable)
    {
        return taskManager.submitTask(cmd, i18n().getText("admin.indexing.jira.indexing"), indexTaskContext, cancellable);
    }


    private JohnsonEventContainer getJohnsonEventContainer()
    {
        final ServletContext ctx = ServletContextProvider.getServletContext();
        if (ctx != null)
        {
            return JohnsonEventContainer.get(ctx);
        }
        return null;
    }
}
