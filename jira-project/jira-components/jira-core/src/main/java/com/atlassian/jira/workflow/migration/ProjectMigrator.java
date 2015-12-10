package com.atlassian.jira.workflow.migration;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueVerifier;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.task.StatefulTaskProgressSink;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationSuccess;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationTerminated;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.util.profiling.UtilTimerStack;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 *
 * @since v5.2
 */
class ProjectMigrator<T extends WorkflowScheme>
{
    private static final Logger log = Logger.getLogger(ProjectMigrator.class);

    private final Project project;
    private final String projectName;
    private final Long projectId;

    private final T targetScheme;

    private final WorkflowManager workflowManager;
    private final SchemeManager schemeManager;
    private final OfBizDelegator delegator;
    private final User user;
    private final ConstantsManager constantsManager;
    private final I18nHelper i18nHelper;
    private final IssueIndexManager issueIndexManager;

    private final OnCompleteCallback<T> onCompleteCallback;

    ProjectMigrator(Project project, T targetScheme, WorkflowManager workflowManager, SchemeManager schemeManager, OfBizDelegator delegator,
            User user, ConstantsManager constantsManager, I18nHelper i18nHelper,  IssueIndexManager issueIndexManager, OnCompleteCallback<T> onCompleteCallback)
    {
        this.project = project;
        this.targetScheme = targetScheme;
        this.workflowManager = workflowManager;
        this.schemeManager = schemeManager;
        this.delegator = delegator;
        this.user = user;
        this.constantsManager = constantsManager;
        this.i18nHelper = i18nHelper;
        this.issueIndexManager = issueIndexManager;
        this.onCompleteCallback = onCompleteCallback;

        this.projectName = project.getName();
        this.projectId = project.getId();
    }

    // Returns a collection of errors associated with issues in the workflow migration
    public WorkflowMigrationResult migrate(TaskProgressSink sink, List<GenericValue> typesNeedingMigration,
            WorkflowMigrationMapping workflowMigrationMapping) throws GenericEntityException, WorkflowException
    {
        UtilTimerStack.push("EnterpriseWorkflowMigrationHelper.migrate");
        log.info("Started workflow migration for project '" + projectName + "'.");

        if (sink == null)
        {
            sink = TaskProgressSink.NULL_SINK;
        }

        //The progress is divied up:
        //0 - 5: Find Issues
        //6 - 16: Verify Issues.
        //17 - 22: Find issues again.
        //23 - 98: Migrate issues.
        //99: Associate scheme with project.
        //100: Finished

        StatefulTaskProgressSink migrationSink = new StatefulTaskProgressSink(0, 100, sink);
        String currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.verification");

        log.info("Verifying issues can be moved to another workflow for project '" + projectName + "'.");
        migrationSink.makeProgress(0, currentSubTask, getI18nHelper().getText("admin.selectworkflowscheme.progress.find.affected.issues", projectName));

        try
        {
            UtilTimerStack.push("Verifying Issues can be moved to another workflow");

            IssueVerifier issueVerifier = new IssueVerifier();
            SimpleErrorCollection errorCollection = new SimpleErrorCollection();

            Collection<Long> issueIds;
            try
            {
                // Collect issue ids for issues to verify before migration
                issueIds = getIssueIds(projectId);
            }
            catch (Exception e)
            {
                log.error("Error occurred while validating issues for workflow migration on project '" + projectName + "'.", e);
                // This should not really occur - but lets handle it the best we can
                errorCollection.addErrorMessage("Error occurred while retrieving issues for verifying for workflow migration: " + e.getMessage());
                // Pop the stack before returning
                UtilTimerStack.pop("Verifying Issues can be moved to another workflow");

                return new WorkflowMigrationTerminated(errorCollection);
            }

            migrationSink.makeProgressIncrement(5, currentSubTask,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.found.affected.issues", projectName));

            try
            {
                final int numberOfIssues = issueIds.size();

                TaskProgressSink issueSink = migrationSink.createStepSinkView(6, 10, numberOfIssues);

                // Loop through issue ids, retrieve one issue at a time and verify it
                int issueCounter = 1;
                for (Long issueId : issueIds)
                {
                    issueSink.makeProgress(issueCounter, currentSubTask,
                            getI18nHelper().getText("admin.selectworkflowscheme.progress.verify.issue", String.valueOf(issueCounter), String.valueOf(numberOfIssues), projectName));


                    GenericValue issueGV = retrieveIssue(issueId);
                    if (issueGV != null)
                    {
                        // Do not check the current workflow integrity. Even if issues are in 'bad' workflow state we should migrate them to new
                        // workflow and fix any problems.
                        ErrorCollection possibleErrors = issueVerifier.verifyForMigration(issueGV, typesNeedingMigration, workflowMigrationMapping, false);
                        errorCollection.addErrorCollection(possibleErrors);
                    }
                    else
                    {
                        log.debug("Issue with id '" + issueId + "' not found.");
                    }
                    issueCounter++;
                }

                if (errorCollection.hasAnyErrors())
                {
                    log.info("Enterprise workflow migration failed with invalid issues for project '" + projectName + "'.");
                    return new WorkflowMigrationTerminated(errorCollection);
                }
            }
            catch (Exception e)
            {
                log.error("Error occurred while verifying issues for workflow migration on project '" + projectName + "'.", e);
                errorCollection.addErrorMessage(getI18nHelper().getText("admin.errors.workflows.error.occurred.verifying.issues", e.getMessage()));
                return new WorkflowMigrationTerminated(errorCollection);
            }
            finally
            {
                UtilTimerStack.pop("Verifying Issues can be moved to another workflow");
            }
            currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.migration");
            migrationSink.makeProgress(17, currentSubTask, getI18nHelper().getText("admin.selectworkflowscheme.progress.find.affected.issues", projectName));

            UtilTimerStack.push("Refinding issues for new workflow");
            try
            {
                // Collect issue ids for issues to migrate. Need to pull out the list again so that if any issues
                // were created in the mean time we find them.
                issueIds = getIssueIds(projectId);
            }
            catch (Exception e)
            {
                log.error("Error occurred while retrieving issues for workflow migration of project '" + projectName + "'.", e);
                // This should not really occur - but lets handle it the best we can
                errorCollection.addErrorMessage("Error occurred while retrieving issues for workflow migration. " + e.getMessage());
                // Pop the stack before returning

                return new WorkflowMigrationTerminated(errorCollection);
            }
            finally
            {
                UtilTimerStack.pop("Refinding issues for new workflow");
            }

            migrationSink.makeProgressIncrement(5, currentSubTask,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.found.affected.issues", projectName));

            WorkflowMigrationResult result = migrateIssues(issueIds, migrationSink, typesNeedingMigration, workflowMigrationMapping);

            currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.association");

            log.info("Assigning workflow scheme to project '" + projectName + "'.");
            migrationSink.makeProgress(99, currentSubTask,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.assign.workflow", getTargetSchemeName(), projectName));

            if (result.getResult() == WorkflowMigrationResult.SUCCESS)
            {
                complete(schemeManager, project, targetScheme, migrationSink);
            }

            log.info("Workflow migration complete for project '" + projectName + "'.");

            migrationSink.makeProgress(100, null,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.complete.in.project", projectName));

            return result;
        }
        finally
        {
            UtilTimerStack.pop("EnterpriseWorkflowMigrationHelper.migrate");
        }
    }

    private WorkflowMigrationResult migrateIssues(Collection issueIds, StatefulTaskProgressSink percentageSink,
            List<GenericValue> typesNeedingMigration, WorkflowMigrationMapping workflowMigrationMapping)
            throws GenericEntityException
    {
        UtilTimerStack.push("Moving issues to new workflow");
        // Contains issue id to issue key mapping of issues that have failed the migration
        Map<Long, String> failedIssues = new HashMap<Long, String>();

        log.info("Migrating issues in project '" + projectName + "' to new workflow.");

        try
        {
            final int numberOfIssues = issueIds.size();
            long issueCounter = 1;
            TaskProgressSink issueSink = percentageSink.createStepSinkView(23, 75, numberOfIssues);

            final String currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.migration");

            // Retrieve one issue at a time and migrate it to new workflow
            for (Iterator iterator = issueIds.iterator(); iterator.hasNext(); issueCounter++)
            {
                issueSink.makeProgress(issueCounter, currentSubTask,
                        getI18nHelper().getText("admin.selectworkflowscheme.progress.migrate.issue", String.valueOf(issueCounter), String.valueOf(numberOfIssues), projectName));

                Long issueId = (Long) iterator.next();
                GenericValue issueGV = retrieveIssue(issueId);

                if (issueGV != null)
                {
                    try
                    {
                        GenericValue currentIssueType = getConstantsManager().getIssueType(issueGV.getString("type"));

                        // Get details for changelog
                        JiraWorkflow originalWorkflow = workflowManager.getWorkflow(issueGV);

                        // Note that if we tried to migrate earlier and something went wrong, this status and wfId may not exist in originalWorkflow!
                        GenericValue originalStatus = getConstantsManager().getStatus(issueGV.getString("status"));
                        String originalWfIdString = issueGV.getLong("workflowId").toString();
                        GenericValue targetStatus;

                        // Mappings exist only for types that require migration
                        // Other types retain their current status
                        if (typesNeedingMigration.contains(currentIssueType))
                        {
                            // For each issue look up the target status using current issue type and CURRENT status in the mapping
                            targetStatus = workflowMigrationMapping.getTargetStatus(issueGV);
                        }
                        else
                        {
                            targetStatus = getConstantsManager().getStatus(issueGV.getString("status"));
                        }

                        // Go for it
                        String issueTypeId = issueGV.getString("type");
                        JiraWorkflow targetWorkflow = getTargetWorkflow(issueTypeId);

                        // Migrate the issue to new workflow if:
                        // 1. If the issue is one of the 'bad' issues - issue which is on the workflow which it should not
                        // be on. Issues get into this state most often due to a previous failed workflow migration.
                        // 2. The source workflow is different to the destination workflow
                        boolean isIssueOnWrongWorkflow = workflowMigrationMapping.isIssueOnWrongWorkflow(issueGV.getLong("id"));
                        if (isIssueOnWrongWorkflow || !targetWorkflow.equals(getExistingWorkflow(issueTypeId)))
                        {
                            // Start transaction before transitioning an issue
                            Transaction txn = Txn.begin();
                            try
                            {
                                // Disable indexing so that no indexing occurrs inside a transaction
                                // We will reindex the issue once the migration of the issue finishes
                                issueIndexManager.hold();

                                final boolean needsIndex = workflowManager.migrateIssueToWorkflowNoReindex(issueGV, targetWorkflow, targetStatus);
                                createChangeLog(issueGV, originalWfIdString, originalStatus, originalWorkflow, targetWorkflow, targetStatus);
                                // Commit changes for the issue
                                txn.commit();

                                //We only need to do a reindex if the WF migration changed the issue. The issue is
                                //always technically changed as the wfId is updated, however, we don't index this value
                                //and as such we don't have to do a re-index. We also always add change items to the issue,
                                //but again we don't index these types of change items.
                                if (needsIndex)
                                {
                                    reindexIssue(issueGV);
                                }
                            }
                            catch (Exception e)
                            {
                                // Roll back changes for the issue and throw an Exception
                                txn.rollback();
                                // Rethrow the exception for further handling.
                                throw e;
                            }
                            finally
                            {
                                issueIndexManager.release();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("Error occurred while migrating issue to a new workflow for project '" + projectName + "'.", e);

                        // Record that an issue failed and see if we should proceed
                        failedIssues.put(issueId, issueGV.getString("key"));

                        // Test if we have failed too many times
                        if (failedIssues.size() >= 10)
                        {
                            log.info("Enterprise workflow migration cancelled due to number of errors during issues migration for project '" + projectName + "'.");

                            // If too many issues have failed then return
                            return new WorkflowMigrationTerminated(failedIssues);
                        }
                    }
                }
                else
                {
                    log.debug("Issue with id '" + issueId + "' not found.");
                }
            }
        }
        finally
        {
            UtilTimerStack.pop("Moving issues to new workflow");
        }
        return new WorkflowMigrationSuccess(failedIssues);
    }

    private JiraWorkflow getExistingWorkflow(String issueTypeId) throws WorkflowException
    {
        JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueTypeId);
        if (workflow == null)
        {
            throw new WorkflowException("Could not find workflow associated with project '" + projectId + "', issuetype " + issueTypeId);
        }
        return workflow;
    }

    /** Gets non-null target workflow for an issue type in the current project. */
    private JiraWorkflow getTargetWorkflow(String issueTypeId) throws WorkflowException
    {
        String workflowName = targetScheme.getActualWorkflow(issueTypeId);
        JiraWorkflow workflow = workflowManager.getWorkflow(workflowName);
        if (workflow == null)
        {
            throw new WorkflowException("Could not find workflow associated with issuetype " + issueTypeId);
        }
        return workflow;
    }

    private void reindexIssue(GenericValue issueGV)
    {
        String issueKey = issueGV.getString("key");
        try
        {
            // Reindex issue
            UtilTimerStack.push("Reindexing issue: " + issueKey);
            ComponentAccessor.getIssueIndexManager().reIndex(issueGV);
            UtilTimerStack.pop("Reindexing issue: " + issueKey);
        }
        catch (IndexException e)
        {
            log.error("Error occurred while reindexing issue: " + issueKey, e);
        }
    }

    // Create change log for changes made to issue during migration
    private void createChangeLog(GenericValue issue, String originalWfIdString, GenericValue originalStatus, JiraWorkflow originalWorkflow, JiraWorkflow targetWorkflow, GenericValue targetStatus)
    {
        String newwfIdString = issue.getLong("workflowId").toString();
        List<ChangeItemBean> changeItems = new ArrayList<ChangeItemBean>();

        boolean createChangeLog = false;

        // Record the changes
        if (!originalWfIdString.equals(newwfIdString))
        {
            changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow", originalWfIdString, originalWorkflow.getName(), newwfIdString, targetWorkflow.getName()));
            createChangeLog = true;
        }

        if (!originalStatus.getString("id").equals(targetStatus.getString("id")))
        {
            changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "status", originalStatus.getString("id"), originalStatus.getString("name"), targetStatus.getString("id"), targetStatus.getString("name")));
            createChangeLog = true;
        }

        try
        {
            // Only create the change log if there have been changes
            if (createChangeLog)
            {
                ChangeLogUtils.createChangeGroup(getUser(), issue, issue, changeItems, true);
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred creating change log: " + e, e);
        }
    }

    void complete(SchemeManager schemeManager, Project project,
            T workflowScheme, StatefulTaskProgressSink migrationSink) throws GenericEntityException
    {
        onCompleteCallback.onComplete(schemeManager, project, workflowScheme, migrationSink);
    }

    private String getTargetSchemeName()
    {
        if (targetScheme == null)
        {
            return getI18nHelper().getText("admin.common.words.default");
        }
        else
        {
            return targetScheme.getName();
        }
    }

    User getUser()
    {
        return user;
    }

    I18nHelper getI18nHelper()
    {
        return i18nHelper;
    }

    private Collection<Long> getIssueIds(Long projectId) throws GenericEntityException
    {
        log.debug("Returning all issues associated with project.");

        // JRA-6987 - do not retrieve all issues at once - use ofbiz iterator to iterate over each issue id
        OfBizListIterator issueIterator = null;

        Collection<Long> issueIds = new ArrayList<Long>();

        try
        {
            final EntityFieldMap cond = new EntityFieldMap(ImmutableMap.of("project", projectId), EntityOperator.AND);
            issueIterator = delegator.findListIteratorByCondition("Issue", cond, null, asList("id"), null, null);
            GenericValue issueIdGV = issueIterator.next();
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            while (issueIdGV != null)
            {
                // record the issue id
                issueIds.add(issueIdGV.getLong("id"));
                // See if we have another issue
                issueIdGV = issueIterator.next();
            }
        }
        finally
        {
            if (issueIterator != null)
            {
                issueIterator.close();
            }
        }

        return issueIds;
    }

    private GenericValue retrieveIssue(Long issueId) throws GenericEntityException
    {
        return delegator.findById("Issue", issueId);
    }

    private ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }

    public interface OnCompleteCallback<T extends WorkflowScheme>
    {
        void onComplete(SchemeManager schemeManager, Project project, T workflowScheme, StatefulTaskProgressSink migrationSink)
                throws GenericEntityException;
    }
}
