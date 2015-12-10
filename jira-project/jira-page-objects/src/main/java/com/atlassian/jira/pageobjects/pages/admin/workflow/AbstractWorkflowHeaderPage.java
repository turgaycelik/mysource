package com.atlassian.jira.pageobjects.pages.admin.workflow;

import java.util.List;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;

/**
 * @since v6.2
 */
abstract class AbstractWorkflowHeaderPage extends AbstractJiraPage implements WorkflowHeader
{
    private WorkflowHeaderDelegate header;

    public boolean canCreateOrEditDraft() {return getWorkflowHeader().canCreateOrEditDraft();}

    public boolean canDiscard() {return getWorkflowHeader().canDiscard();}

    public boolean canViewDraft()
    {
        return getWorkflowHeader().canViewDraft();
    }

    public boolean canEditNameOrDescription() {return getWorkflowHeader().canEditNameOrDescription();}

    public boolean canPublish() {return getWorkflowHeader().canPublish();}

    public <T extends WorkflowHeader> T createDraftInMode(WorkflowHeader.WorkflowMode<T> mode) {return getWorkflowHeader().createDraftInMode(mode);}

    public <T extends WorkflowHeader> T createDraft(WorkflowHeader.WorkflowMode<T> mode) {return getWorkflowHeader().createDraft(mode); }

    public <T extends WorkflowHeader> T setCurrentEditMode(WorkflowHeader.WorkflowMode<T> mode)
    {
        return header.setCurrentEditMode(mode);
    }

    public void setCurrentViewMode(WorkflowHeader.WorkflowMode<?> mode) {header.setCurrentViewMode(mode);}

    public DiscardDraftDialog openDiscardDialog() {return getWorkflowHeader().openDiscardDialog();}

    public EditWorkflowNameAndDescriptionDialog editNameOrDescription() {return getWorkflowHeader().editNameOrDescription();}

    public WorkflowHeader.WorkflowMode<?> getCurrentMode() {return getWorkflowHeader().getCurrentMode();}

    public List<String> getInfoMessages() {return getWorkflowHeader().getInfoMessages();}

    public List<String> getSharedProjects() {return getWorkflowHeader().getSharedProjects();}

    public List<String> getWarningMessages() {return getWorkflowHeader().getWarningMessages();}

    public String getWorkflowDescription() {return getWorkflowHeader().getWorkflowDescription();}

    public String getWorkflowName() {return getWorkflowHeader().getWorkflowName();}

    public ViewWorkflowSteps gotoLiveWorkflow() {return getWorkflowHeader().gotoLiveWorkflow();}

    public boolean hasLinkToLiveWorkflow() {return getWorkflowHeader().hasLinkToLiveWorkflow();}

    public boolean isActive() {return getWorkflowHeader().isActive();}

    public boolean isDraft() {return getWorkflowHeader().isDraft();}

    public boolean isInactive() {return getWorkflowHeader().isInactive();}

    public boolean isSystem() {return getWorkflowHeader().isSystem();}

    public PublishDialog openPublishDialog() {return getWorkflowHeader().openPublishDialog();}

    public ProjectSharedBy sharedBy() {return getWorkflowHeader().sharedBy();}

    WorkflowHeaderDelegate getWorkflowHeader()
    {
        if (header == null)
        {
            header = pageBinder.bind(WorkflowHeaderDelegate.class);
        }
        return header;
    }

    void init()
    {
        getWorkflowHeader().clearWorkflowDesignerState();
    }
}
