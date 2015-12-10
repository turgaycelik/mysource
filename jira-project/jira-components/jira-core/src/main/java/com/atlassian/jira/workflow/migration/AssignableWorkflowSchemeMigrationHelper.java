package com.atlassian.jira.workflow.migration;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.task.StatefulTaskProgressSink;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;

public class AssignableWorkflowSchemeMigrationHelper extends AbstractWorkflowSchemeMigrationHelper<AssignableWorkflowScheme>
{
    private final Project project;
    private final String projectName;
    private final Long projectId;
    private final ProjectMigrator<AssignableWorkflowScheme> projectMigrator;

    AssignableWorkflowSchemeMigrationHelper(Project project, AssignableWorkflowScheme targetScheme, WorkflowManager workflowManager,
            OfBizDelegator delegator, SchemeManager schemeManager, I18nHelper i18nHelper, User user, ConstantsManager constantsManager,
            TaskManager taskManager, IssueIndexManager issueIndexManager, WorkflowSchemeManager workflowSchemeManager, EventPublisher eventPublisher)
            throws WorkflowException, GenericEntityException
    {
        super(targetScheme, workflowManager, delegator, schemeManager, i18nHelper, user, constantsManager, taskManager,
                issueIndexManager, workflowSchemeManager, eventPublisher);

        this.project = project;
        this.projectName = project.getName();
        this.projectId = project.getId();

        projectMigrator = new ProjectMigrator<AssignableWorkflowScheme>(project, targetScheme, workflowManager, schemeManager,
                delegator, user, constantsManager, i18nHelper, issueIndexManager, new ProjectMigrator.OnCompleteCallback<AssignableWorkflowScheme>()
        {
            @Override
            public void onComplete(SchemeManager schemeManager, Project project, AssignableWorkflowScheme workflowScheme, StatefulTaskProgressSink migrationSink)
                    throws GenericEntityException
            {
                assignSchemeToTemplate(migrationSink);
            }
        });

        calculateInputRequired();
    }

    @Override
    JiraWorkflow getExistingWorkflow(String issueTypeId) throws WorkflowException
    {
        return getExistingWorkflowByProjectId(issueTypeId, projectId);
    }

    @Override
    EntityCondition getProjectClause(String projectField)
    {
        return new EntityFieldMap(ImmutableMap.of(projectField, projectId), EntityOperator.AND);
    }


    // Returns a collection of errors associated with issues in the workflow migration
    @Override
    public WorkflowMigrationResult migrate(TaskProgressSink sink) throws GenericEntityException, WorkflowException
    {
        return projectMigrator.migrate(sink, typesNeedingMigration, workflowMigrationMapping);
    }

    @VisibleForTesting
    void assignSchemeToTemplate(StatefulTaskProgressSink migrationSink) throws GenericEntityException
    {
        WorkflowScheme sourceScheme = workflowSchemeManager.getWorkflowSchemeObj(project);
        assignSchemeToProjectTemplate(migrationSink);
        workflowSchemeManager.clearWorkflowCache();
        copyAndDeleteDraftsForInactiveWorkflowsIn(sourceScheme);
        eventPublisher.publish(new WorkflowSchemeMigrationCompletedEvent(targetScheme));
    }

    @Override
    void quickMigrate() throws GenericEntityException
    {
        projectMigrator.complete(schemeManager, project, targetScheme, null);
    }

    @Override
    String getMigrateAsyncTaskDesc()
    {
        return i18nHelper.getText("admin.selectworkflows.task.desc", projectName, schemeName);
    }

    @Override
    EnterpriseWorkflowTaskContext createEnterpriseWorkflowTaskContext()
    {
        return new EnterpriseWorkflowTaskContext(project, schemeId, false);
    }

    @VisibleForTesting
    void assignSchemeToProjectTemplate(StatefulTaskProgressSink migrationSink) throws GenericEntityException
    {
        // need to find the set of workflows that may need their draft workflows to be copied and deleted, since
        // the parent workflow may no longer be active.
        workflowSchemeManager.cleanUpSchemeDraft(project, user);
        schemeManager.removeSchemesFromProject(project);

        // Check if associating with none - the default workflow
        if (targetScheme != null && targetScheme.getId() != null)
        {
            Scheme scheme = workflowSchemeManager.getSchemeObject(targetScheme.getId());
            schemeManager.addSchemeToProject(project, scheme);
        }
    }

    public void associateProjectAndWorkflowScheme() throws GenericEntityException
    {
        projectMigrator.complete(schemeManager, project, targetScheme, null);
    }
}
