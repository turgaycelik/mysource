package com.atlassian.jira.web.action.admin.workflow.scheme;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.EnterpriseWorkflowTaskContext;
import com.atlassian.jira.workflow.migration.WorkflowSchemeMigrationTaskAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;

/**
 * @since v3.13
 */
@WebSudoRequired
public class SelectProjectWorkflowSchemeStep3 extends SelectProjectWorkflowScheme
{
    private final static String ABORTED_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.aborted.defaultworkflow";
    private static final String FAILURE_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.withfailure.defaultworkflow";

    private final Map<Long, String> failedIssueIds;

    private Long taskId;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(final Long taskId)
    {
        this.taskId = taskId;
    }

    public SelectProjectWorkflowSchemeStep3(WorkflowSchemeMigrationTaskAccessor taskAccessor, final TaskManager taskManager,
            WorkflowSchemeManager workflowSchemeManager, final TaskDescriptorBean.Factory taskDescriptorFactory)
    {
        super(taskAccessor, taskManager, workflowSchemeManager, taskDescriptorFactory);
        failedIssueIds = new HashMap<Long, String>();
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();
        final Long taskId = getTaskId();
        TaskDescriptor<?> taskDescriptor = null;
        if (taskId != null)
        {
            taskDescriptor = getTaskManager().getTask(taskId);
        }
        if (taskDescriptor == null)
        {
            addErrorMessage(getText("common.tasks.task.not.found"));
        }
        else
        {
            final TaskContext context = taskDescriptor.getTaskContext();
            if (!(context instanceof EnterpriseWorkflowTaskContext))
            {
                addErrorMessage(getText("common.tasks.wrong.task.context", EnterpriseWorkflowTaskContext.class.getName(),
                    context.getClass().getName()));
            }
            else
            {
                initTaskDescriptorBean((TaskDescriptor<WorkflowMigrationResult>) taskDescriptor);
            }
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (!hasPermission())
        {
            return "securitybreach";
        }
        else if (!isDone())
        {
            // let the view display the progress
            return SUCCESS;
        }
        else
        {
            // ok its finished running but how succesfully
            WorkflowMigrationResult migrationResult = null;
            migrationResult = (WorkflowMigrationResult) getCurrentTask().getResult();
            if (migrationResult == null)
            {
                return ERROR;
            }

            final ErrorCollection errorCollection = migrationResult.getErrorCollection();
            if ((errorCollection != null) && errorCollection.hasAnyErrors())
            {
                // Do not complete the migration - redirect to workflow migration error screen displaying errors encountered.
                addErrorCollection(errorCollection);
                return "workflowmigrationerror";
            }

            if (WorkflowMigrationResult.SUCCESS == migrationResult.getResult())
            {
                // Check if there are any failed issues.
                if (migrationResult.getNumberOfFailedIssues() > 0)
                {
                    // Save the failed issue ids and keys
                    failedIssueIds.putAll(migrationResult.getFailedIssues());
                    // Report failed issues
                    return "workflowmigrationwithfailure";
                }
                else
                {
                    return SUCCESS;
                }
            }
            else
            {
                // We have a failure
                // Save the failed issue ids and keys
                failedIssueIds.putAll(migrationResult.getFailedIssues());
                // Report the problem to the user
                return "workflowmigrationaborted";
            }
        }

    }

    @Override
    public TaskDescriptorBean getCurrentTask()
    {
        return super.getCurrentTask(false);
    }

    public String getDestinationURL()
    {
        return getRedirectURL();
    }

    public boolean isDone()
    {
        final TaskDescriptorBean currentTask = getCurrentTask();
        return currentTask != null && currentTask.isFinished();
    }

    public Map<Long, String> getFailedIssueIds()
    {
        return failedIssueIds;
    }

    public static String getAbortedMigrationMessageKey()
    {
        return ABORTED_MIGRATION_MESSAGE_KEY;
    }

    public static String getFailureMigrationMessageKey()
    {
        return FAILURE_MIGRATION_MESSAGE_KEY;
    }
}
