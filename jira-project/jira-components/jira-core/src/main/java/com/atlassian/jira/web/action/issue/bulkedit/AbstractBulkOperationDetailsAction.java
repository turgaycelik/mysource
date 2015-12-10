package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkEditTaskContext;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;

import java.util.concurrent.Callable;

public abstract class AbstractBulkOperationDetailsAction extends AbstractBulkOperationAction
{
    private final TaskManager taskManager;
    private final I18nHelper i18nHelper;

    public AbstractBulkOperationDetailsAction(final SearchService searchService,
                                              final BulkEditBeanSessionHelper bulkEditBeanSessionHelper,
                                              final TaskManager taskManager,
                                              final I18nHelper i18nHelper)
    {
        super(searchService, bulkEditBeanSessionHelper);
        this.taskManager = taskManager;
        this.i18nHelper = i18nHelper;
    }

    public abstract String doDetails() throws Exception;

    public abstract String doDetailsValidation() throws Exception;

    public abstract String doPerform() throws Exception;

    protected String submitBulkOperationTask(BulkEditBean bulkEditBean, ProgressAwareBulkOperation operation,
                                             String taskName) throws Exception
    {
        String username = getLoggedInApplicationUser().getUsername();
        String operationName = operation.getOperationName();
        BulkEditTaskContext context = new BulkEditTaskContext(username, operationName);
        final TaskDescriptor<BulkEditCommandResult> taskDescriptor = taskManager.getLiveTask(context);
        if (taskDescriptor != null)
        {
            log.debug("An existing task found for " + context + ", not starting another one.");
            return getRedirect(taskDescriptor.getProgressURL());
        }
        log.debug("Submitting a new task with task manager: " + context);
        Callable<BulkEditCommandResult> bulkeditCallable = new BulkOperationProgress.BulkEditCallable(log, i18nHelper,
                operation, bulkEditBean, getLoggedInApplicationUser());
        String progressUrl = taskManager.submitTask(bulkeditCallable, taskName, context).getProgressURL();
        return watchProgress(progressUrl);
    }
}
