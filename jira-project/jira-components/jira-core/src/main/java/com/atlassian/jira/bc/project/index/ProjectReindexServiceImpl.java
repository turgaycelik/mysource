package com.atlassian.jira.bc.project.index;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.index.ha.ReplicatedIndexManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IssueBatcherFactory;
import com.atlassian.jira.issue.index.IssueIndexer;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 *
 * @since v6.1
 */
public class ProjectReindexServiceImpl implements ProjectReindexService
{
    private final JiraAuthenticationContext authenticationContext;
    private final TaskManager taskManager;
    private IssueIndexer issueIndexer;
    private ReindexMessageManager reindexMessageManager;
    private ReplicatedIndexManager replicatedIndexManager;
    private final OfBizDelegator ofBizDelegator;
    private final IssueBatcherFactory issueBatcherFactory;
    private final SearchProvider searchProvider;
    private final IssueManager issueManager;
    private final EventPublisher eventPublisher;
    private final I18nHelper.BeanFactory i18nBeanFactory;

    public ProjectReindexServiceImpl(final JiraAuthenticationContext authenticationContext,
            final TaskManager taskManager, final IssueIndexer issueIndexer,
            final ReindexMessageManager reindexMessageManager, final ReplicatedIndexManager replicatedIndexManager,
            final OfBizDelegator ofBizDelegator, final IssueBatcherFactory issueBatcherFactory,
            final SearchProvider searchProvider, final IssueManager issueManager, final EventPublisher eventPublisher
            , final I18nHelper.BeanFactory i18nBeanFactory) 
    {
        this.authenticationContext = authenticationContext;
        this.taskManager = taskManager;
        this.issueIndexer = issueIndexer;
        this.reindexMessageManager = reindexMessageManager;
        this.replicatedIndexManager = replicatedIndexManager;
        this.ofBizDelegator = ofBizDelegator;
        this.issueBatcherFactory = issueBatcherFactory;
        this.searchProvider = searchProvider;
        this.issueManager = issueManager;
        this.eventPublisher = eventPublisher;
        this.i18nBeanFactory = i18nBeanFactory;
    }

    private static final Logger log = Logger.getLogger(ProjectReindexServiceImpl.class);

    @Override
    public String reindex(final Project project)
    {
        return reindex(project, true);
    }

    @Override
    public String reindex(final Project project, boolean updateReplicatedIndexStore)
    {
        final String taskName = getText("admin.indexing.project", project.getName());
        Callable<IndexCommandResult> indexCallable =
                new ReIndexProjectIndexerCommand(project, ofBizDelegator, issueIndexer,
                        taskManager, searchProvider, issueManager, eventPublisher, issueBatcherFactory, log, authenticationContext.getI18nHelper(), i18nBeanFactory);
        String progressUrl = taskManager.submitTask(indexCallable, taskName, new ProjectIndexTaskContext(project), true).getProgressURL();
        if (updateReplicatedIndexStore)
        {
            replicatedIndexManager.reindexProject(project);
        }
        return progressUrl;
    }

    @Override
    public boolean isReindexPossible(final Project project)
    {
        return taskManager.getLiveTask(new ProjectIndexTaskContext(project)) == null;
    }

    public String getText(String key, String parameter) {
        return getI18nHelper().getText(key, parameter);
    }

    private I18nHelper getI18nHelper()
    {
        return authenticationContext.getI18nHelper();
    }

}
