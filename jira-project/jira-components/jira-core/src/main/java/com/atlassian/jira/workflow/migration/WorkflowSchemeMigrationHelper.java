package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowScheme;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 *
 * @since v5.2
 */
public interface WorkflowSchemeMigrationHelper<T extends WorkflowScheme>
{
    List<GenericValue> getTypesNeedingMigration();

    Collection<GenericValue> getStatusesNeedingMigration(GenericValue issueType);

    void addMapping(GenericValue issueType, GenericValue oldStatus, GenericValue newStatus);

    boolean isHaveIssuesToMigrate() throws GenericEntityException;

    boolean doQuickMigrate() throws GenericEntityException;

    TaskDescriptor<WorkflowMigrationResult> migrateAsync() throws RejectedExecutionException;

    Logger getLogger();

    // Returns a collection of errors associated with issues in the workflow migration
    WorkflowMigrationResult migrate(TaskProgressSink sink) throws GenericEntityException, WorkflowException;

}
