package com.atlassian.jira.workflow.migration;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.task.StatefulTaskProgressSink;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResultForMultipleProjects;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult.SUCCESS;
import static com.google.common.collect.Collections2.transform;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 *
 * @since v5.2
 */
public class DraftWorkflowSchemeMigrationHelper extends AbstractWorkflowSchemeMigrationHelper<DraftWorkflowScheme>
{
    private final List<Project> projects;
    private final Collection<Long> projectIds;
    private final Project triggerProject;

    DraftWorkflowSchemeMigrationHelper(Project triggerProject, List<Project> projects, DraftWorkflowScheme targetScheme, WorkflowManager workflowManager,
            OfBizDelegator delegator, SchemeManager schemeManager, I18nHelper i18nHelper, User user, ConstantsManager constantsManager,
            TaskManager taskManager, IssueIndexManager issueIndexManager, WorkflowSchemeManager workflowSchemeManager, EventPublisher eventPublisher)
            throws WorkflowException, GenericEntityException
    {
        super(targetScheme, workflowManager, delegator, schemeManager, i18nHelper, user, constantsManager,
                taskManager, issueIndexManager, workflowSchemeManager, eventPublisher);

        this.triggerProject = triggerProject;
        this.projects = projects;
        this.projectIds = transform(projects, new Function<Project, Long>()
        {
            @Override
            public Long apply(Project project)
            {
                return project.getId();
            }
        });

        calculateInputRequired();
    }

    private class DefaultProjectMigrator<T extends WorkflowScheme> extends ProjectMigrator<T>
    {
        DefaultProjectMigrator(Project project, T targetScheme, OnCompleteCallback<T> onCompleteCallback)
        {
            super(project, targetScheme, workflowManager, schemeManager, delegator,
                    user, constantsManager, i18nHelper,  issueIndexManager, onCompleteCallback);
        }
    }

    @Override
    JiraWorkflow getExistingWorkflow(String issueTypeId) throws WorkflowException
    {
        return getExistingWorkflowByProjectId(issueTypeId, projects.iterator().next().getId());
    }

    @Override
    void quickMigrate() throws GenericEntityException
    {
        workflowSchemeManager.replaceSchemeWithDraft(targetScheme);

        // Clear the active workflow name cache
        workflowSchemeManager.clearWorkflowCache();
        copyAndDeleteDraftsForInactiveWorkflowsIn(targetScheme.getParentScheme());
    }

    @Override
    EntityCondition getProjectClause(String projectField)
    {
        return new EntityExpr(projectField, EntityOperator.IN, projectIds);
    }

    @Override
    public WorkflowMigrationResult migrate(TaskProgressSink sink) throws GenericEntityException, WorkflowException
    {
        if (sink == null)
        {
            sink = TaskProgressSink.NULL_SINK;
        }

        StatefulTaskProgressSink migrationSink = new StatefulTaskProgressSink(0, 100, sink);

        migrationSink.makeProgress(1, null, i18nHelper.getText("admin.selectworkflowscheme.progress.created.target"));

        Iterator<StatefulTaskProgressSink> sinks = StatefulTaskProgressSink.createPercentageSinksForRange(2, 98, projects.size(), sink).iterator();
        List<WorkflowMigrationResult> projectMigrationResults = Lists.newArrayListWithCapacity(projects.size());
        boolean atLeastOneWasSuccessful = false;
        AssignableWorkflowScheme intermediateScheme = null;

        if (projects.size() > 1)
        {
            intermediateScheme = prepareIntermediateScheme();
            for (Project project : projects)
            {
                ProjectMigrator<?> projectMigrator = new DefaultProjectMigrator<AssignableWorkflowScheme>(project, intermediateScheme, new ProjectMigrator.OnCompleteCallback<AssignableWorkflowScheme>()
                {
                    @Override
                    public void onComplete(SchemeManager schemeManager, Project project, AssignableWorkflowScheme workflowScheme, StatefulTaskProgressSink migrationSink)
                            throws GenericEntityException
                    {
                        schemeManager.removeSchemesFromProject(project);
                        schemeManager.addSchemeToProject(project, schemeManager.getSchemeObject(workflowScheme.getId()));
                    }
                });
                if (migrate(sinks, projectMigrationResults, projectMigrator))
                {
                    atLeastOneWasSuccessful = true;
                }
            }
        }
        else
        {
            ProjectMigrator<?> projectMigrator = new DefaultProjectMigrator<DraftWorkflowScheme>(Iterables.getOnlyElement(projects), targetScheme, new ProjectMigrator.OnCompleteCallback<DraftWorkflowScheme>()
                {
                    @Override
                    public void onComplete(SchemeManager schemeManager, Project project, DraftWorkflowScheme workflowScheme, StatefulTaskProgressSink migrationSink)
                            throws GenericEntityException
                    {
                        taskContext.markSafeToDelete();
                        workflowSchemeManager.replaceSchemeWithDraft(workflowScheme);
                    }
            });
            atLeastOneWasSuccessful = migrate(sinks, projectMigrationResults, projectMigrator);
        }

        WorkflowMigrationResult result = new WorkflowMigrationResultForMultipleProjects(projectMigrationResults);

        migrationSink.makeProgress(99, null, i18nHelper.getText("admin.selectworkflowscheme.progress.cleanup"));

        if (SUCCESS == result.getResult() && result.getNumberOfFailedIssues() == 0 && intermediateScheme != null)
        {
            cleanUpSchemes(intermediateScheme);
        }

        if (atLeastOneWasSuccessful)
        {
            eventPublisher.publish(new WorkflowSchemeMigrationCompletedEvent(targetScheme));
        }

        // Clear the active workflow name cache
        workflowSchemeManager.clearWorkflowCache();
        copyAndDeleteDraftsForInactiveWorkflowsIn(targetScheme.getParentScheme());

        migrationSink.makeProgress(100, null, i18nHelper.getText("admin.selectworkflowscheme.progress.complete"));

        return result;
    }

    private boolean migrate(Iterator<StatefulTaskProgressSink> sinks, List<WorkflowMigrationResult> projectMigrationResults, ProjectMigrator<?> projectMigrator)
            throws GenericEntityException
    {
        WorkflowMigrationResult projectMigrationResult = projectMigrator.migrate(sinks.next(), typesNeedingMigration, workflowMigrationMapping);
        projectMigrationResults.add(projectMigrationResult);
        return SUCCESS == projectMigrationResult.getResult();
    }

    private AssignableWorkflowScheme prepareIntermediateScheme()
    {
        String newDescription = getIntermediateSchemeDescription();
        return workflowSchemeManager.copyDraft(targetScheme, user, newDescription);
    }

    private String getIntermediateSchemeDescription()
    {
        StringBuilder sb = new StringBuilder();

        if (isNotBlank(targetScheme.getDescription()))
        {
            sb.append(targetScheme.getDescription()).append(" ");
        }

        sb.append(i18nHelper.getText("admin.selectworkflowscheme.migration.draft.auto.generated", targetScheme.getName()));

        return sb.toString();
    }

    private void cleanUpSchemes(AssignableWorkflowScheme intermediateScheme)
    {
        AssignableWorkflowScheme originalScheme = workflowSchemeManager.getParentForDraft(targetScheme.getId());

        if (!workflowSchemeManager.isActive(originalScheme))
        {
            workflowSchemeManager.deleteWorkflowScheme(originalScheme);

            intermediateScheme = intermediateScheme.builder()
                    .setName(originalScheme.getName())
                    .setDescription(originalScheme.getDescription())
                    .build();
            workflowSchemeManager.updateWorkflowScheme(intermediateScheme);
        }
    }

    @Override
    protected String getMigrateAsyncTaskDesc()
    {
        return i18nHelper.getText("admin.selectworkflows.publish.draft.task.desc", schemeName);
    }

    @Override
    EnterpriseWorkflowTaskContext createEnterpriseWorkflowTaskContext()
    {
        return new EnterpriseWorkflowTaskContext(triggerProject, projects, schemeId, true);
    }
}
