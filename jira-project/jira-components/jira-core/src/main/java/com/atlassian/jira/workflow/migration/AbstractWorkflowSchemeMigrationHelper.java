package com.atlassian.jira.workflow.migration;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;

/**
 *
 * @since v5.2
 */
public abstract class AbstractWorkflowSchemeMigrationHelper<T extends WorkflowScheme> implements WorkflowSchemeMigrationHelper<T>
{
    private static final Logger log = Logger.getLogger(AbstractWorkflowSchemeMigrationHelper.class);

    final T targetScheme;
    final WorkflowManager workflowManager;
    protected final SchemeManager schemeManager;
    protected final List<GenericValue> typesNeedingMigration;
    protected final Map<GenericValue, Collection<GenericValue>> statusesNeedingMigration;
    protected final WorkflowMigrationMapping workflowMigrationMapping;
    protected final OfBizDelegator delegator;
    protected final User user;
    protected final ConstantsManager constantsManager;
    protected final I18nHelper i18nHelper;
    final Long schemeId;
    protected final String schemeName;
    protected final TaskManager taskManager;
    protected final IssueIndexManager issueIndexManager;
    final WorkflowSchemeManager workflowSchemeManager;
    protected final EventPublisher eventPublisher;
    EnterpriseWorkflowTaskContext taskContext;

    AbstractWorkflowSchemeMigrationHelper(T targetScheme, WorkflowManager workflowManager, OfBizDelegator delegator,
            SchemeManager schemeManager, I18nHelper i18nHelper, User user, ConstantsManager constantsManager, TaskManager taskManager, IssueIndexManager issueIndexManager,
            WorkflowSchemeManager workflowSchemeManager, EventPublisher eventPublisher)
    {
        this.targetScheme = targetScheme;
        this.workflowManager = workflowManager;
        this.delegator = delegator;
        this.schemeManager = schemeManager;
        this.i18nHelper = i18nHelper;
        this.user = user;
        this.taskManager = taskManager;
        this.issueIndexManager = issueIndexManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.eventPublisher = eventPublisher;
        this.workflowMigrationMapping = new WorkflowMigrationMapping();
        this.constantsManager = constantsManager;
        this.typesNeedingMigration = new ArrayList<GenericValue>();
        this.statusesNeedingMigration = new HashMap<GenericValue, Collection<GenericValue>>();

        if (targetScheme == null)
        {
            schemeId = null;
            schemeName = i18nHelper.getText("admin.common.words.default");
        }
        else
        {
            schemeId =  targetScheme.getId();
            schemeName = targetScheme.getName();
        }
    }

    void calculateInputRequired() throws WorkflowException, GenericEntityException
    {
        List<GenericValue> issueTypes = constantsManager.getAllIssueTypes();

        for (GenericValue issueType : issueTypes)
        {
            String issueTypeId = issueType.getString("id");
            JiraWorkflow existingWorkflow = getExistingWorkflow(issueTypeId);
            JiraWorkflow targetWorkflow = getTargetWorkflow(issueTypeId);

            boolean needMigration = false;
            // Check if the source workflow is the same as the destination one
            if (existingWorkflow.equals(targetWorkflow))
            {
                // If the workflows are the same, then we need to find out if we have any issues on the 'wrong' workflow for this issue type
                Collection<Long> issueIdsOnWrongWorkflow = getIssueIdsOnWrongWorkflow(issueTypeId, existingWorkflow.getName());
                if (issueIdsOnWrongWorkflow != null && !issueIdsOnWrongWorkflow.isEmpty())
                {
                    // If we do have issues on the wrong workflow then we need to migrate these issues
                    needMigration = true;
                    // Record the issue ids of issues that are on wrong workflow so that we know that they
                    // need migration even though the workflow they are supposed to be on is the same as the
                    // destination workflow
                    addIssueIdsOnWrongWorkflow(issueIdsOnWrongWorkflow);
                }
            }
            else
            {
                // If not, we definitely need to migrate issues for this issue type
                needMigration = true;
            }

            // We need to ask the user to provide a mapping for statuses for the given issue type if:
            // 1. The source and destination workflows are different
            // 2. We have issues on the 'wrong' workflow to what they should be.
            if (needMigration)
            {
                Collection<GenericValue> existingStatuses = new HashSet<GenericValue>(existingWorkflow.getLinkedStatuses());

                // Get a collection of statuses for which we have issues of type issueType
                // This is needed in case we have inconsistent data, where we have issues in statuses
                // that do not exist in workflow the issues should be on
                Collection<GenericValue> actualExistingStatuses = getUniqueStatusesForIssueType(issueTypeId);
                existingStatuses.addAll(actualExistingStatuses);

                List<GenericValue> targetStatuses = targetWorkflow.getLinkedStatuses();
                existingStatuses.removeAll(targetStatuses);

                if (existingStatuses.size() > 0)
                {
                    typesNeedingMigration.add(issueType);
                    statusesNeedingMigration.put(issueType, existingStatuses);
                }

                // find out intersection of statuses and add mappings
                Collection<GenericValue> intersection = new HashSet<GenericValue>(existingWorkflow.getLinkedStatuses());
                intersection.addAll(actualExistingStatuses);

                intersection.retainAll(targetStatuses);

                for (GenericValue status : intersection)
                {
                    addMapping(issueType, status, status);
                }
            }
        }
    }

    /**
     * Retrieves issue ids of any issues that are on the 'wrong' workflow for the given issue type. The issues could
     * be on a wrong issue type due to a previously failed workflow migration for the project.
     *
     * @param issueTypeId          the issue type the issue's of which should be checked
     * @param expectedWorkflowName the name of the workflow we expect the issues to be on
     * @return a {@link Collection} of issue ids ({@link Long}s) of any issues with given issueTypeId that are not using
     *         workflow with expectedWorkflowName. Remember, the name of a workflow is it unique identifier. If no issues are found
     *         an empty collection is returned.
     * @throws GenericEntityException if there is problem querying the database.
     */
    private Collection<Long> getIssueIdsOnWrongWorkflow(String issueTypeId, String expectedWorkflowName)
            throws GenericEntityException
    {
        if (issueTypeId == null)
        {
            throw new IllegalArgumentException("Issue Type id should not be null.");
        }

        List<Long> issueIds = new ArrayList<Long>();
        OfBizListIterator listIterator = null;

        try
        {
            EntityCondition projectIssueTypeClause = getProjectIssueTypeClause(issueTypeId, "issueType", "issueProject");
            EntityCondition workflowClause = new EntityExpr("workflowName", EntityOperator.NOT_EQUAL, expectedWorkflowName);

            EntityCondition condition = new EntityExpr(projectIssueTypeClause, EntityOperator.AND, workflowClause);

            listIterator = delegator.findListIteratorByCondition("IssueWorkflowEntryView", condition, null, asList("issueId"), null, null);
            GenericValue issueIdGV = listIterator.next();
            while (issueIdGV != null)
            {
                issueIds.add(issueIdGV.getLong("issueId"));
                // See if we have another record
                issueIdGV = listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }

        return Collections.unmodifiableList(issueIds);
    }

    private Collection<GenericValue> getUniqueStatusesForIssueType(String issueTypeId) throws GenericEntityException
    {
        if (issueTypeId == null)
        {
            throw new NullPointerException("Issue Type should not be null.");
        }

        EntityCondition projectIssueTypeClause = getProjectIssueTypeClause(issueTypeId, "type", "project");

        return getUniqueStatuses(projectIssueTypeClause);
    }

    private EntityCondition getProjectIssueTypeClause(String issueTypeId, String issueTypeField, String projectField)
    {
        EntityCondition issueTypeClause = new EntityFieldMap(ImmutableMap.of(issueTypeField, issueTypeId), EntityOperator.AND);
        EntityCondition projectClause = getProjectClause(projectField);

        return new EntityExpr(issueTypeClause, EntityOperator.AND, projectClause);
    }

    abstract EntityCondition getProjectClause(String projectField);


    /**
     * AbstractWorkflowMigrationHelper
     * Retrieves a collection of unique status GenericValues for which issues exist given a EntityCondition that
     * will be used as the SQL WHERE clause against the Issue table.
     */
    private Collection<GenericValue> getUniqueStatuses(EntityCondition condition) throws GenericEntityException
    {
        Collection<GenericValue> foundStatuses = new ArrayList<GenericValue>();

        OfBizListIterator listIterator = null;

        try
        {
            // SELECT DISTINCT status FROM jiraissue WHERE project = projectId AND type = issueTypeId
            EntityFindOptions findOptions = new EntityFindOptions();
            findOptions.setDistinct(true);
            listIterator = delegator.findListIteratorByCondition("Issue", condition, null, asList(IssueFieldConstants.STATUS), null, findOptions);
            GenericValue statusIdGV = listIterator.next();
            while (statusIdGV != null)
            {
                GenericValue statusGV = constantsManager.getStatus(statusIdGV.getString("status"));
                // If the issue status does not exist or is null - do not include the status here
                // The IssueVerifier should catch it down the line and not allow the issues to be migrated to new worfklow
                if (statusGV != null)
                {
                    foundStatuses.add(statusGV);
                }
                else
                {
                    // Print out a message here. It might help solve a few support cases.
                    getLogger().debug("Found issue with status id '" + statusIdGV.getString("status") + "'. The status for this id does not exist.");
                }

                // See if we have another status
                statusIdGV = listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }

        return foundStatuses;
    }

    private void addIssueIdsOnWrongWorkflow(Collection<Long> issueIdsOnWrongWorkflow)
    {
        if (issueIdsOnWrongWorkflow != null)
        {
            this.workflowMigrationMapping.addIssueIdsOnWorongWorkflow(issueIdsOnWrongWorkflow);
        }
    }

    JiraWorkflow getExistingWorkflowByProjectId(String issueTypeId, long projectId) throws WorkflowException
    {
        JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueTypeId);
        if (workflow == null)
        {
            throw new WorkflowException("Could not find workflow associated with project '" + projectId + "', issuetype " + issueTypeId);
        }
        return workflow;
    }

    /** Gets non-null workflow for an issue type in the current project. */
    abstract JiraWorkflow getExistingWorkflow(String issueTypeId) throws WorkflowException;

    /** Gets non-null target workflow for an issue type in the current project. */
    JiraWorkflow getTargetWorkflow(String issueTypeId) throws WorkflowException
    {
        String workflowName = targetScheme.getActualWorkflow(issueTypeId);
        JiraWorkflow workflow = workflowManager.getWorkflow(workflowName);
        if (workflow == null)
        {
            throw new WorkflowException("Could not find workflow associated with issuetype " + issueTypeId);
        }
        return workflow;
    }

    @Override
    public List<GenericValue> getTypesNeedingMigration()
    {
        return typesNeedingMigration;
    }

    @Override
    public Collection<GenericValue> getStatusesNeedingMigration(GenericValue issueType)
    {
        return statusesNeedingMigration.get(issueType);
    }

    @Override
    public void addMapping(GenericValue issueType, GenericValue oldStatus, GenericValue newStatus)
    {
        workflowMigrationMapping.addMapping(issueType, oldStatus, newStatus);
    }

    @Override
    public Logger getLogger()
    {
        return log;
    }

    @Override
    public boolean doQuickMigrate() throws GenericEntityException
    {
        if (isHaveIssuesToMigrate())
        {
            return false;
        }
        else
        {
            quickMigrate();
            return true;
        }
    }

    abstract void quickMigrate() throws GenericEntityException;

    @Override
    public boolean isHaveIssuesToMigrate() throws GenericEntityException
    {
        OfBizListIterator issueIterator = null;
        try
        {
            issueIterator = delegator.findListIteratorByCondition("Issue", getProjectClause("project"));
            return (issueIterator.next() != null);
        }
        finally
        {
            if (issueIterator != null)
            {
                issueIterator.close();
            }
        }
    }

    @Override
    public TaskDescriptor<WorkflowMigrationResult> migrateAsync() throws RejectedExecutionException
    {
        final String taskDesc = getMigrateAsyncTaskDesc();
        taskContext = createEnterpriseWorkflowTaskContext();

        return taskManager.submitTask(new WorkflowAsynchMigrator(this), taskDesc, taskContext);
    }

    abstract String getMigrateAsyncTaskDesc();

    abstract EnterpriseWorkflowTaskContext createEnterpriseWorkflowTaskContext();

    protected void copyAndDeleteDraftsForInactiveWorkflowsIn(WorkflowScheme workflowScheme)
    {
        Collection<String> oldWorkflowNames = workflowScheme.getMappings().values();
        workflowManager.copyAndDeleteDraftsForInactiveWorkflowsIn(user, transform(oldWorkflowNames, new Function<String, JiraWorkflow>()
        {
            @Override
            public JiraWorkflow apply(String input)
            {
                return workflowManager.getWorkflow(input);
            }
        }));
    }
}
