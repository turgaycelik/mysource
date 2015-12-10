package com.atlassian.jira.bc.workflow;

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.atlassian.beehive.ClusterLockService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.opensymphony.workflow.loader.StepDescriptor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.security.Permissions.ADMINISTER;
import static com.atlassian.jira.util.ErrorCollection.Reason;

/**
 * Default workflow service implementation.  Provides some 'nice' error handling and delegates straight through
 * to the underlying {@link com.atlassian.jira.workflow.WorkflowManager}
 */
public class DefaultWorkflowService implements WorkflowService, Startable
{
    @VisibleForTesting
    static final String OVERWRITE_WORKFLOW_LOCK_NAME = DefaultWorkflowService.class.getName() + ".overwriteWorkflow";

    private static final Logger log = Logger.getLogger(DefaultWorkflowService.class);

    private final ClusterLockService clusterLockService;
    private final WorkflowManager workflowManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final WorkflowSchemeManager workflowSchemeManager;

    private Lock overwriteWorkflowLock;

    public DefaultWorkflowService(final WorkflowManager workflowManager, final JiraAuthenticationContext jiraAuthenticationContext,
            final PermissionManager permissionManager, final WorkflowSchemeManager workflowSchemeManager,
            final ClusterLockService clusterLockService)
    {
        this.clusterLockService = clusterLockService;
        this.workflowManager = workflowManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.workflowSchemeManager = workflowSchemeManager;
    }

    @Override
    public void start()
    {
        this.overwriteWorkflowLock = clusterLockService.getLockForName(OVERWRITE_WORKFLOW_LOCK_NAME);
    }

    public JiraWorkflow getDraftWorkflow(final JiraServiceContext jiraServiceContext, final String parentWorkflowName)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return null;
        }
        if (StringUtils.isEmpty(parentWorkflowName))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.parent"));
            return null;
        }
        final JiraWorkflow parentWorkflow = workflowManager.getWorkflow(parentWorkflowName);
        if (parentWorkflow == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.retrieve.no.parent"));
            return null;
        }
        return workflowManager.getDraftWorkflow(parentWorkflowName);
    }

    public JiraWorkflow createDraftWorkflow(final JiraServiceContext jiraServiceContext, final String parentWorkflowName)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return null;
        }
        if (StringUtils.isEmpty(parentWorkflowName))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.parent"));
            return null;
        }
        final JiraWorkflow parentWorkflow = workflowManager.getWorkflow(parentWorkflowName);
        if (parentWorkflow == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.parent"));
            return null;
        }
        if (!workflowManager.isActive(parentWorkflow))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.parent.not.active"));
            return null;
        }
        if (parentWorkflow.isSystemWorkflow())
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.not.editable"));
            return null;
        }

        try
        {
            return workflowManager.createDraftWorkflow(jiraServiceContext.getLoggedInApplicationUser(), parentWorkflowName);
        }
        catch (final IllegalStateException e)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getI18nBean().getText("admin.workflows.service.error.draft.exists.or.workflow.not.active"));
            return null;
        }
    }

    public boolean deleteDraftWorkflow(final JiraServiceContext jiraServiceContext, final String parentWorkflowName)
    {
        if (StringUtils.isEmpty(parentWorkflowName))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.delete.no.parent"));
            return false;
        }
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return false;
        }
        return workflowManager.deleteDraftWorkflow(parentWorkflowName);
    }

    @Override
    public ServiceOutcome<Void> deleteWorkflow(ApplicationUser deletingUser, String workflowName)
    {
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(deletingUser);
        JiraWorkflow workflow = getWorkflow(jiraServiceContext, workflowName);

        if (jiraServiceContext.getErrorCollection().hasAnyErrors())
        {
            return ServiceOutcomeImpl.from(jiraServiceContext.getErrorCollection(), null);
        }

        if (workflow == null)
        {
            return ServiceOutcomeImpl.error(getI18nBean().getText("admin.errors.workflow.with.name.does.not.exist", "'" + workflowName + "'"));
        }

        if (!workflow.isEditable())
        {
            return ServiceOutcomeImpl.error(getI18nBean().getText("admin.errors.workflow.cannot.be.deleted.as.it.is.not.editable"));
        }

        // Ensure that the workflow is not associated with any schemes
        Iterable<WorkflowScheme> workflowSchemes = workflowSchemeManager.getSchemesForWorkflowIncludingDrafts(workflow);
        if (!Iterables.isEmpty(workflowSchemes))
        {
            StringBuilder schemes = new StringBuilder();
            for (WorkflowScheme scheme : workflowSchemes)
            {
                if (scheme.isDraft())
                {
                    schemes.append(getI18nBean().getText("admin.workflows.service.workflow.draft.of", scheme.getName()));
                }
                else
                {
                    schemes.append('\'').append(scheme.getName()).append('\'');
                }
                schemes.append(", ");
            }
            schemes.delete(schemes.length() - 2, schemes.length() - 1);
            return ServiceOutcomeImpl.error(getI18nBean().getText("admin.errors.cannot.delete.this.workflow") + " " + schemes);
        }

        workflowManager.deleteWorkflow(workflow);

        return ServiceOutcomeImpl.ok(null);
    }

    public void overwriteActiveWorkflow(final JiraServiceContext jiraServiceContext, final String parentWorkflowName)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return;
        }
        //need to protect the validation and overwrite by an explicit lock.  The updateWorkflow method is protected
        //by the same lock.  This guarantees the workflow that is used to validate, won't change in between validation
        //and the overwrite itself.  This works since we only use the workflowName, and the overwrite workflow
        //looks up the latest version of the workflow from the underlying persistence layer.
        // Note that only the updateWorkflow() method is protected by this lock.  Deleting an draftWorkflow() is ok
        // as the worst thing that could happen is that the live workflow is overwritten by a draft that's been deleted
        // already.  We know that that draft was in a valid state however before it was deleted otherwise the
        // validateOverwriteWorkflow() wouldn't have passed.
        overwriteWorkflowLock.lock();
        try
        {
            validateOverwriteWorkflow(jiraServiceContext, parentWorkflowName);

            if (jiraServiceContext.getErrorCollection().hasAnyErrors())
            {
                return;
            }

            workflowManager.overwriteActiveWorkflow(jiraServiceContext.getLoggedInApplicationUser(), parentWorkflowName);
        }
        finally
        {
            overwriteWorkflowLock.unlock();
        }
    }

    public void validateOverwriteWorkflow(final JiraServiceContext jiraServiceContext, final String workflowName)
    {
        // Check that the user has the required permission
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return;
        }
        // Check that a workflow name was provided
        if (StringUtils.isEmpty(workflowName))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getI18nBean().getText("admin.workflows.service.error.overwrite.no.parent", workflowName));
            return;
        }
        // get hold of the Published workflow to be overwritten.
        final JiraWorkflow liveJiraWorkflow = workflowManager.getWorkflow(workflowName);
        if (liveJiraWorkflow == null)
        {
            // No actual workflow
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getI18nBean().getText("admin.workflows.service.error.overwrite.no.parent", workflowName));
            return;
        }
        if (!liveJiraWorkflow.isActive())
        {
            // No actual workflow
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getI18nBean().getText("admin.workflows.service.error.overwrite.inactive.parent", workflowName));
            return;
        }
        // get a hold of the Draft workflow that will be saved
        final JiraWorkflow draftJiraWorkflow = workflowManager.getDraftWorkflow(workflowName);
        if (draftJiraWorkflow == null)
        {
            // No draft workflow
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getI18nBean().getText("admin.workflows.service.error.overwrite.no.draft", workflowName));
            return;
        }
        // TODO: Should we deal with trying to save to default workflow ("jira") explicitly, or just rely on it not making a draft?
        // We have actually got hold of a live and draft workflow for the given name, check that the changes are valid:
        validateOverwriteWorkflow(liveJiraWorkflow, draftJiraWorkflow, jiraServiceContext);
    }

    public void updateWorkflow(final JiraServiceContext jiraServiceContext, final JiraWorkflow workflow)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            addError(jiraServiceContext, "admin.workflows.service.error.no.admin.permission", Reason.FORBIDDEN);
            return;
        }
        if ((workflow == null) || (workflow.getDescriptor() == null))
        {
            addError(jiraServiceContext, "admin.workflows.service.error.update.no.workflow", Reason.VALIDATION_FAILED);
            return;
        }
        if (!workflow.isEditable())
        {
            addError(jiraServiceContext, "admin.workflows.service.error.not.editable", Reason.VALIDATION_FAILED);
            return;
        }
        // This lock ensures that the overwriteWorkflow action above allows for an atomic validation and overwrite
        // of the workflow.  
        overwriteWorkflowLock.lock();
        try
        {
            workflowManager.updateWorkflow(jiraServiceContext.getLoggedInApplicationUser(), workflow);
        }
        finally
        {
            overwriteWorkflowLock.unlock();
        }
    }

    public void validateUpdateWorkflowNameAndDescription(final JiraServiceContext jiraServiceContext, final JiraWorkflow currentWorkflow, final String newWorkflowName)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return;
        }
        if ((currentWorkflow == null) || (currentWorkflow.getDescriptor() == null))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.update.no.workflow"));
            return;
        }

        if (!currentWorkflow.isEditable())
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getI18nBean().getText("admin.errors.workflow.cannot.be.edited.as.it.is.not.editable"));
            return;
        }

        //Draft workflows are not allowed to change name!
        if (currentWorkflow.isDraftWorkflow() && !newWorkflowName.equals(currentWorkflow.getName()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.edit.name.draft.workflow"));
            return;
        }

        if(!WorkflowUtil.isAcceptableName(newWorkflowName,"newWorkflowName",jiraServiceContext.getErrorCollection()))
        {
            return;
        }
        //If we're changeing name, check that no workflow already exists with the new name.
        if (!newWorkflowName.equals(currentWorkflow.getName()) && workflowManager.workflowExists(newWorkflowName))
        {
            jiraServiceContext.getErrorCollection().addError("newWorkflowName",
                getI18nBean().getText("admin.errors.a.workflow.with.this.name.already.exists"));
            return;
        }
    }

    public void updateWorkflowNameAndDescription(final JiraServiceContext jiraServiceContext, final JiraWorkflow currentWorkflow, final String newName, final String newDescription)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return;
        }
        workflowManager.updateWorkflowNameAndDescription(jiraServiceContext.getLoggedInApplicationUser(), currentWorkflow, newName, newDescription);
    }

    public JiraWorkflow getWorkflow(final JiraServiceContext jiraServiceContext, final String name)
    {
        if (StringUtils.isEmpty(name))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.null.name"));
            return null;
        }

        return workflowManager.getWorkflow(name);
    }

    public void validateCopyWorkflow(final JiraServiceContext jiraServiceContext, final String newWorkflowName)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return;
        }
        if (!WorkflowUtil.isAcceptableName(newWorkflowName,"newWorkflowName",jiraServiceContext.getErrorCollection()))
        {
            return;
        }

        try
        {
            if (workflowManager.workflowExists(newWorkflowName))
            {
                jiraServiceContext.getErrorCollection().addError("newWorkflowName",
                    getI18nBean().getText("admin.errors.a.workflow.with.this.name.already.exists"));
            }
        }
        catch (final WorkflowException e)
        {
            log.error("Error occurred while accessing workflow information.", e);
            jiraServiceContext.getErrorCollection().addErrorMessage(
                getI18nBean().getText("admin.errors.workflows.error.occurred.accessing.information"));
        }
    }

    public JiraWorkflow copyWorkflow(final JiraServiceContext jiraServiceContext, final String clonedWorkflowName, final String clonedWorkflowDescription, final JiraWorkflow workflowToClone)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return null;
        }
        return workflowManager.copyWorkflow(jiraServiceContext.getLoggedInApplicationUser(), clonedWorkflowName, clonedWorkflowDescription, workflowToClone);
    }

    public boolean isStepOnDraftWithNoTransitionsOnParentWorkflow(final JiraServiceContext jiraServiceContext, final JiraWorkflow workflow, final int stepId)
    {
        if (workflow.isDraftWorkflow())
        {
            //need to check the parent's step to see if it has any available actions.
            final JiraWorkflow parentWorkflow = getWorkflow(jiraServiceContext, workflow.getName());
            final StepDescriptor originalStep = parentWorkflow.getDescriptor().getStep(stepId);
            //if the step exists on the original workflow (may not, it could be a new step added to the draft,
            //in which case we don't care)
            if (originalStep != null)
            {
                final List<?> availableActions = originalStep.getActions();
                if ((availableActions == null) || availableActions.isEmpty())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void validateAddWorkflowTransitionToDraft(final JiraServiceContext jiraServiceContext, final JiraWorkflow newJiraWorkflow, final int stepId)
    {
        if (!hasAdminPermission(jiraServiceContext))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getI18nBean().getText("admin.workflows.service.error.no.admin.permission"));
            return;
        }
        if (isStepOnDraftWithNoTransitionsOnParentWorkflow(jiraServiceContext, newJiraWorkflow, stepId))
        {
            //original step didn't have any actions.  Lets check if the new step DOES have any actions.
            final StepDescriptor newStep = newJiraWorkflow.getDescriptor().getStep(stepId);
            final List<?> newActions = newStep.getActions();
            if ((newActions != null) && !newActions.isEmpty())
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(
                    getI18nBean().getText("admin.workflowtransitions.error.add.transition.draft.step.without.transition", newStep.getName()));
            }
        }
    }

    private void validateOverwriteWorkflow(final JiraWorkflow oldJiraWorkflow, final JiraWorkflow newJiraWorkflow, final JiraServiceContext jiraServiceContext)
    {
        // For each step in the original workflow, we want that step to exist in the new workflow with the same ID and
        // the same associated Issue Status.
        final List<GenericValue> linkedStatuses = oldJiraWorkflow.getLinkedStatuses();
        for (final GenericValue gvStatus : linkedStatuses)
        {
            // get the old and new StepDescriptor
            final StepDescriptor oldStepDescriptor = oldJiraWorkflow.getLinkedStep(gvStatus);
            final StepDescriptor newStepDescriptor = newJiraWorkflow.getLinkedStep(gvStatus);

            // Firstly we must have a step for the new status
            if (newStepDescriptor == null)
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(
                    getI18nBean().getText("admin.workflows.service.error.overwrite.missing.status", gvStatus.getString("name")));
                break;
            }
            // The step associated with this status must be the same step ID.
            // Otherwise os_currentstep table and possibly others will become invalid
            if (oldStepDescriptor.getId() != newStepDescriptor.getId())
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(
                    getI18nBean().getText("admin.workflows.service.error.overwrite.step.associated.with.wrong.status",
                        String.valueOf(oldStepDescriptor.getId()), gvStatus.getString("name")));
                break;
            }

            validateAddWorkflowTransitionToDraft(jiraServiceContext, newJiraWorkflow, oldStepDescriptor.getId());
            if (jiraServiceContext.getErrorCollection().hasAnyErrors())
            {
                break;
            }
        }
    }

    @VisibleForTesting
    I18nHelper getI18nBean()
    {
        return jiraAuthenticationContext.getI18nHelper();
    }

    @VisibleForTesting
    boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
    {
        return permissionManager.hasPermission(ADMINISTER, jiraServiceContext.getLoggedInUser());
    }

    private void addError(final JiraServiceContext jiraServiceContext, final String msgKey, final Reason reason)
    {
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        errorCollection.addErrorMessage(getI18nBean().getText(msgKey));
        errorCollection.addReason(reason);
    }
}
