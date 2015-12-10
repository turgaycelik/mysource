package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.RequiresTaskInformation;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.task.TimeBasedLogSink;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;

import java.util.concurrent.Callable;

/**
 * Class that allows WorkflowMigration to occur on a different thread.
 *
 * @since v3.13
 */

class WorkflowAsynchMigrator implements Callable<WorkflowMigrationResult>, ProvidesTaskProgress, RequiresTaskInformation<WorkflowMigrationResult>
{
    private static final int MAX_TIME_BETWEEN_EVENTS = 120000;

    private volatile TaskProgressSink taskProgressSink = null;
    private volatile TaskDescriptor<WorkflowMigrationResult> taskDescriptor = null;
    private final WorkflowSchemeMigrationHelper migrator;

    WorkflowAsynchMigrator(final WorkflowSchemeMigrationHelper migrator)
    {
        this.migrator = migrator;
    }

    public WorkflowMigrationResult call() throws Exception
    {
        return migrator.migrate(new TimeBasedLogSink(migrator.getLogger(), taskDescriptor.getDescription(), MAX_TIME_BETWEEN_EVENTS, taskProgressSink));
    }

    public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
    {
        this.taskProgressSink = taskProgressSink;
    }

    public void setTaskDescriptor(final TaskDescriptor<WorkflowMigrationResult> taskDescriptor)
    {
        this.taskDescriptor = taskDescriptor;
    }
}
