package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bulkedit.operation.BulkEditTaskContext;
import com.atlassian.jira.bulkedit.operation.BulkOperationException;
import com.atlassian.jira.bulkedit.operation.ProgressAwareBulkOperation;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.FixedSized;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class BulkOperationProgress extends AbstractBulkOperationAction
{
    private static final int TRANISTION_ERRORS_LIMIT_DEFAULT = 100;

    private final TaskManager taskManager;
    private final TaskDescriptorBean.Factory taskBeanFactory;

    private Long taskId;
    private TaskDescriptorBean<BulkEditCommandResult> ourTask;
    private TaskDescriptor<BulkEditCommandResult> currentTaskDescriptor;

    public BulkOperationProgress(final TaskManager taskManager, final TaskDescriptorBean.Factory taskBeanFactory,
                                 SearchService searchService, final BulkEditBeanSessionHelper bulkEditBeanSessionHelper)
    {
        super(searchService, bulkEditBeanSessionHelper);
        this.taskManager = taskManager;
        this.taskBeanFactory = taskBeanFactory;
    }

    /**
     * Provides progress details of the task specified in the bulk edit bean (in the session).
     */
    public String doProgress() throws ExecutionException, InterruptedException
    {
        BulkEditBean bulkEditBean = getBulkEditBean();
        if (bulkEditBean == null)
        {
            addErrorMessage(getText("bulk.bean.doesntexist.error"));
            return ERROR;
        }
        String errorOutcome = ERROR + "-" + bulkEditBean.getOperationName();

        if (taskId == null)
        {
            addErrorMessage(getText("common.tasks.no.task.id"));
            return errorOutcome;
        }
        currentTaskDescriptor = taskManager.getTask(taskId);
        if (currentTaskDescriptor == null)
        {
            addErrorMessage(getText("common.tasks.no.task.found"));
            return errorOutcome;
        }
        final TaskContext context = currentTaskDescriptor.getTaskContext();
        if (!(context instanceof BulkEditTaskContext))
        {
            addErrorMessage(getText("common.tasks.wrong.task.context", BulkEditTaskContext.class.getName(),
                    context.getClass().getName()));
            return errorOutcome;
        }

        ourTask = taskBeanFactory.create(currentTaskDescriptor);
        if (currentTaskDescriptor.isFinished() && !currentTaskDescriptor.isCancelled())
        {
            final BulkEditCommandResult result = currentTaskDescriptor.getResult();
            if (!result.isSuccessful())
            {
                addErrorCollection(result.getErrorCollection());
            }
        }
        return "progress";
    }

    /**
     * Finishes the task progress view. When the task is complete, user can acknowledge and move to the next stage.
     */
    public String doFinish() throws Exception
    {
        BulkEditBean bulkEditBean = getBulkEditBean();
        if (bulkEditBean == null)
        {
            addErrorMessage(getText("bulk.bean.doesntexist.error"));
            return ERROR;
        }

        return finishWizard();
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(final Long taskId)
    {
        this.taskId = taskId;
    }

    public TaskDescriptorBean<BulkEditCommandResult> getCurrentTask()
    {
        if (ourTask == null)
        {
            final TaskDescriptor<BulkEditCommandResult> taskDescriptor = taskManager.getTask(taskId);
            if (taskDescriptor != null)
            {
                ourTask = taskBeanFactory.create(taskDescriptor);
            }
        }
        return ourTask;
    }

    public TaskDescriptorBean<?> getOurTask()
    {
        return ourTask;
    }

    protected String finishWizard() throws Exception
    {
        // Bulk operation progress doesn't know about redirect url, simply getting that from bulk edit bean.
        String redirectUrl = getRootBulkEditBean().getRedirectUrl();
        clearBulkEditBean();
        return getRedirect(redirectUrl);
    }

    public boolean isThereAnyTransitionError()
    {
        return !getTransitionErrors().isEmpty();
    }

    public boolean isTransitionErrorsLimited()
    {
        return getBulkEditBean().isTranisitionErrorsLimited(getTransitionErrorsCount());
    }

    public Map<String, Collection<String>> getTransitionErrors()
    {
        return getBulkEditBean().getTransitionErrors(getTransitionErrorsCount());
    }

    private int getTransitionErrorsCount()
    {
        try
        {
            return Integer.parseInt(
                    getApplicationProperties().getDefaultBackedString(APKeys.JIRA_BULK_EDIT_LIMIT_TRANSITION_ERRORS));
        }
        catch (NumberFormatException nfe)
        {
            // nop
        }
        return TRANISTION_ERRORS_LIMIT_DEFAULT;
    }

    public static class BulkEditCallable implements Callable<BulkEditCommandResult>, ProvidesTaskProgress
    {
        private TaskProgressSink taskProgressSink;
        private final Logger log;
        private final I18nHelper i18nHelper;
        private final ProgressAwareBulkOperation bulkOperation;
        private final BulkEditBean bulkEditBean;
        private final ApplicationUser user;

        public BulkEditCallable(final Logger log, final I18nHelper i18nHelper,
                                final ProgressAwareBulkOperation bulkOperation,
                                final BulkEditBean bulkEditBean, final ApplicationUser user)
        {
            this.log = log;
            this.i18nHelper = i18nHelper;
            this.bulkOperation = bulkOperation;
            this.bulkEditBean = bulkEditBean;
            this.user = user;
        }

        public BulkEditCommandResult call() throws Exception
        {
            log.debug(String.format("Bulk-operation on %d issuess", bulkEditBean.getSelectedIssues().size()));
            final long startTime = System.currentTimeMillis();

            try
            {
                int numberOfOperations = bulkOperation.getNumberOfTasks(bulkEditBean);
                Context context = Contexts.percentageReporter(new FixedSized(numberOfOperations), taskProgressSink,
                        i18nHelper, log, "Bulk-operation is {0}% complete. Index of current item: {1}",
                        "bulk.operation.progress.percent.complete", null);
                context.setName("bulk operation");
                bulkOperation.perform(bulkEditBean, user, context);
                // In case the operation does less work than we expected (due to bugs/errors/etc), we complete the progress manually.
                for (int i = 0; i < context.getNumberOfTasksToCompletion(); i++)
                {
                    context.start(null).complete();
                }
                return new BulkEditCommandResult(System.currentTimeMillis() - startTime, new SimpleErrorCollection());
            }
            catch (BulkOperationException e)
            {
                log.warn("Error while performing bulk operation", e);
                SimpleErrorCollection errorCollection = new SimpleErrorCollection();
                errorCollection.addErrorMessage(i18nHelper.getText("bulk.operation.perform.error"));
                return new BulkEditCommandResult(System.currentTimeMillis() - startTime, errorCollection);
            }
            finally
            {
                log.debug("Bulk-editing finished");
            }
        }

        public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
        {
            this.taskProgressSink = taskProgressSink;
        }
    }
}
