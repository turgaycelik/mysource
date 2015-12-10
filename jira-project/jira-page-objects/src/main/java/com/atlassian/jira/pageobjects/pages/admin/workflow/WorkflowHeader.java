package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.pageobjects.PageBinder;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

/**
 * @since v5.1
 */
public interface WorkflowHeader
{
    String getWorkflowName();

    String getWorkflowDescription();

    boolean isSystem();

    ProjectSharedBy sharedBy();

    List<String> getSharedProjects();

    boolean canEditNameOrDescription();

    EditWorkflowNameAndDescriptionDialog editNameOrDescription();

    boolean canCreateOrEditDraft();

    boolean canDiscard();

    boolean canViewDraft();

    DiscardDraftDialog openDiscardDialog();

    boolean canPublish();

    PublishDialog openPublishDialog();

    boolean isDraft();

    boolean hasLinkToLiveWorkflow();

    ViewWorkflowSteps gotoLiveWorkflow();

    boolean isActive();

    boolean isInactive();

    List<String> getInfoMessages();

    List<String> getWarningMessages();

    WorkflowMode<?> getCurrentMode();

    <T extends WorkflowHeader> T createDraftInMode(WorkflowMode<T> mode);

    <T extends WorkflowHeader> T createDraft(WorkflowMode<T> mode);

    <T extends WorkflowHeader> T setCurrentEditMode(WorkflowMode<T> mode);

    void setCurrentViewMode(WorkflowMode<?> mode);

    public static class WorkflowMode<T extends WorkflowHeader>
    {
        public static final WorkflowMode<ViewWorkflowSteps> TEXT = new WorkflowMode<ViewWorkflowSteps>(ViewWorkflowSteps.class, "text");
        public static final WorkflowMode<WorkflowDesignerPage> DIAGRAM = new WorkflowMode<WorkflowDesignerPage>(WorkflowDesignerPage.class, "diagram");
        public static final Set<WorkflowHeaderDelegate.WorkflowMode<?>> VALUES = ImmutableSet.<WorkflowHeaderDelegate.WorkflowMode<?>>of(TEXT, DIAGRAM);

        private final Class<T> next;
        private final String name;

        public WorkflowMode(Class<T> next, String name)
        {
            this.next = next;
            this.name = name;
        }

        public T bindAfterCreateDraft(PageBinder binder, String workflowMode)
        {
            return binder.bind(next, workflowMode, true);
        }

        public T bind(PageBinder binder, String workflowMode, boolean draft)
        {
            return binder.bind(next, workflowMode, draft);
        }

        public static WorkflowMode fromString(String mode)
        {
            for (WorkflowMode workflowMode : VALUES)
            {
                if (workflowMode.name.equalsIgnoreCase(mode))
                {
                    return workflowMode;
                }
            }
            return null;
        }

        public Class<T> getNext()
        {
            return next;
        }

        public String getName()
        {
            return name;
        }
    }
}
