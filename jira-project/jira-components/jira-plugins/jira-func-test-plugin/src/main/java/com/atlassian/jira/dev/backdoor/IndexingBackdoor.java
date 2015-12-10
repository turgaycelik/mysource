package com.atlassian.jira.dev.backdoor;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.index.ProjectIndexTaskContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ForegroundIndexTaskContext;
import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import com.atlassian.jira.web.action.admin.index.ReIndexAsyncIndexerCommand;
import com.atlassian.jira.web.action.admin.index.ReIndexBackgroundIndexerCommand;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeComparator;
import org.joda.time.DateTimeFieldType;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.ServletActionContext;

/**
 * Backdoor for starting/stopping/querying indexing.
 *
 * @since v5.2
 */
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
@Path ("/indexing")
public class IndexingBackdoor
{
    private static final Logger log = Logger.getLogger(IndexingBackdoor.class);

    private final IssueIndexer issueIndexer;
    private final TaskManager taskManager;
    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IssueIndexManager issueIndexManager;
    private final ProjectManager projectManager;
    private final SearchService searchService;
    private final IssueManager issueManager;
    private final UserManager userManager;
    private final OfBizDelegator genericDelegator;

    public IndexingBackdoor(IssueIndexer issueIndexer, TaskManager taskManager, I18nHelper.BeanFactory i18nFactory, JiraAuthenticationContext jiraAuthenticationContext, IssueIndexManager issueIndexManager, ProjectManager projectManager, SearchService searchService, IssueManager issueManager, UserManager userManager)
    {
        this.issueIndexer = issueIndexer;
        this.taskManager = taskManager;
        this.i18nFactory = i18nFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.issueIndexManager = issueIndexManager;
        this.projectManager = projectManager;
        this.searchService = searchService;
        this.issueManager = issueManager;
        this.userManager = userManager;
        this.genericDelegator = ComponentAccessor.getOfBizDelegator();
    }

    @POST
    @Path ("background")
    public void triggerBackgroundIndexing(@QueryParam("comments") boolean reindexComments, @QueryParam("changeHistory") boolean reindexChangeHistory)
    {
        submitIndexingTask(new ReIndexBackgroundIndexerCommand(indexManager(), reindexComments, reindexChangeHistory, log, i18n(), i18nFactory));
    }

    @POST
    @Path ("stoptheworld")
    public void triggerStopTheWorldIndexing()
    {
        submitIndexingTask(new ReIndexAsyncIndexerCommand(getJohnsonEventContaner(), indexManager(), log, i18n(), i18nFactory));
    }

    @POST
    @Path ("deleteIndex")
    public void deleteIndex()
    {
        issueIndexer.deleteIndexes();
    }

    @GET
    @Path ("deindex")
    public boolean deindex(@QueryParam ("key") String key)
    {
        Issue issue = issueManager.getIssueObject(key);
        if (issue != null)
        {
            issueIndexer.deindexIssues(new IssueObjectIssuesIterable(Collections.singletonList(issue)), Contexts.nullContext());
        }
        return true;
    }

    @GET
    @Path ("indexDummyIssue")
    public boolean indexDummyIssue(@QueryParam ("id") String id, @QueryParam ("issueType") String issueType, @QueryParam ("projectId") String projectId, @QueryParam ("key") String key, @QueryParam ("summary") String summary, @QueryParam ("description") String description)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", Long.valueOf(id));
        map.put("project", Long.valueOf(projectId));
        map.put("type", issueType);
        map.put("key", key);
        map.put("summary", summary);
        map.put("description", description);
        map.put("created", new Timestamp(System.currentTimeMillis()));
        GenericValue gv = genericDelegator.makeValue("Issue", map);
        Index.Result result = issueIndexer.reindexIssues(new IssueObjectIssuesIterable(Collections.singletonList(IssueImpl.getIssueObject(gv))), Contexts.nullContext(), false, false, false);
        result.await();
        return true;
    }

    @POST
    @Path("reindexAll")
    public Response reindexAll()
    {
        try
        {
            issueIndexManager.reIndexAll();
            return Response.ok().build();
        }
        catch (IndexException e)
        {
            return Response.serverError().build();
        }
    }

    @GET
    public boolean isIndexingRunning()
    {
        TaskDescriptor<IndexCommandResult> task = getIndexingTask();

        return task != null && !task.isFinished();
    }

    @GET
    @Path("started")
    public boolean isIndexingStarted()
    {
        TaskDescriptor<IndexCommandResult> task = getIndexingTask();

        return task != null && task.getStartedTimestamp() != null;
    }

    @GET
    @Path("project")
    public boolean isIndexingProject(@QueryParam ("projectId") Long projectId)
    {
        TaskDescriptor<IndexCommandResult> task = getIndexingTaskForProject(projectId);

        return task != null && !task.isFinished();
    }


    @GET
    @Path("project/started")
    public boolean isIndexingProjectStarted(@QueryParam ("projectId") Long projectId)
    {
        TaskDescriptor<IndexCommandResult> task = getIndexingTaskForProject(projectId);

        return task != null && task.getStartedTimestamp() != null;
    }

    @GET
    @Path("consistent")
    public boolean isIndexConsistent()
    {
        return issueIndexManager.isIndexConsistent();
    }

    @GET
    @Path ("consistent/updated")
    public boolean isIndexUpdatedFieldConsistent() throws SearchException
    {
        final List<Issue> issues = searchService.search(userManager.getUser("admin"),
                JqlQueryBuilder.newBuilder().where().buildQuery(), PagerFilter.getUnlimitedFilter()).getIssues();

        for (Issue issueFromIndex : issues)
        {
            if (!areEqualToTheSecond(issueFromIndex.getUpdated(), issueManager.getIssueObject(issueFromIndex.getId()).getUpdated()))
            {
                return false;
            }
        }
        return true;
    }

    private boolean areEqualToTheSecond(Timestamp updatedFromIdnex, Timestamp updatedFromDB) {
        return DateTimeComparator.getInstance(DateTimeFieldType.secondOfMinute()).compare(updatedFromIdnex, updatedFromDB) == 0;
    }

    private TaskDescriptor<IndexCommandResult> getIndexingTask()
    {
        return taskManager.getLiveTask(new IndexTaskContext());
    }


    private TaskDescriptor<IndexCommandResult> getIndexingTaskForProject(final Long projectId)
    {
        return taskManager.getLiveTask(new ProjectIndexTaskContext(projectManager.getProjectObj(projectId)));
    }

    private TaskDescriptor<?> submitIndexingTask(Callable<? extends Serializable> cmd)
    {
        return taskManager.submitTask(cmd, i18n().getText("admin.indexing.jira.indexing"), new ForegroundIndexTaskContext());
    }

    private IndexLifecycleManager indexManager()
    {
        return ComponentAccessor.getIssueIndexManager();
    }

    private I18nHelper i18n()
    {
        return i18nFactory.getInstance(jiraAuthenticationContext.getLoggedInUser());
    }

    private JohnsonEventContainer getJohnsonEventContaner()
    {
        ServletContext ctx = ServletActionContext.getServletContext();
        if (ctx != null)
        {
            return JohnsonEventContainer.get(ctx);
        }

        return null;
    }
}
