/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.migration.WorkflowMigrationMapping;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IssueVerifier
{
    private static final Logger log = Logger.getLogger(IssueVerifier.class);

    public ErrorCollection verifyIssue(final Issue issue, final Map workflowMigrationMapping, final boolean checkWorkflowIntegrity)
    {
        return verifyIssue(issue.getGenericValue(), workflowMigrationMapping, checkWorkflowIntegrity);
    }

    /**
     * This is a very basic integrity/verification test used to indicate if a workflow migration
     * for a specific issue is possible.
     * <p/>
     * It is not possible to test all steps in the migration process due to database access and
     * creation of new elements (e.g. workflow entries, etc).
     * <p/>
     * This test ensures that a specific issue is in a suitable state (e.g. workflow, status, etc).
     * <p/>
     * This is the professional version of the checker.
     *
     * @return an error collection detailing errors associated with the issue.
     */
    public ErrorCollection verifyIssue(final GenericValue issue, final Map<String, String> workflowMigrationMapping, final boolean checkWorkflowIntegrity)
    {
        final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        final ErrorCollection errorCollection = new SimpleErrorCollection();

        try
        {
            checkIssueDetails(issue, errorCollection, constantsManager, checkWorkflowIntegrity);

            // Check target status
            final String statusId = issue.getString("status");
            final String targetStatusId = workflowMigrationMapping.get(statusId);
            final GenericValue targetStatus = constantsManager.getStatus(targetStatusId);

            if (targetStatus == null)
            {
                errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.unable.to.determine.target.status", issue.getString("key")));
                log.error("Unable to determine the target status for issue '" + issue.getString("key") + "'.");
            }
        }
        catch (final Exception e)
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.encountered.error.processing.issue", issue.getString("key")));
            log.error("Encountered an error processing the issue '" + issue.getString("key") + "'.", e);
        }
        return errorCollection;
    }

    public ErrorCollection verifyForMigration(GenericValue issue, List<GenericValue> typesNeedingMigration, WorkflowMigrationMapping workflowMigrationMapping, boolean checkWorkflowIntegrity)
    {
        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        ErrorCollection errorCollection = new SimpleErrorCollection();

        try
        {
            checkIssueDetails(issue, errorCollection, constantsManager, checkWorkflowIntegrity);

            GenericValue currentIssueType = constantsManager.getIssueType(issue.getString("type"));

            // Check target status
            GenericValue targetStatus = null;

            // Mappings exist only for types that require migration
            // Other types retain their current status
            if (typesNeedingMigration.contains(currentIssueType))
            {
                // For each issue look up the target status using current issue type and CURRENT status in the mapping
                targetStatus = workflowMigrationMapping.getTargetStatus(issue);
            }
            else
            {
                targetStatus = constantsManager.getStatus(issue.getString("status"));
            }

            if (targetStatus == null)
            {
                errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.unable.to.determine.target.status","'" + issue.getString("key") + "'"));
                log.error("Unable to determine the target status for issue '" + issue.getString("key") + "'.");
            }
        }
        catch (Exception e)
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.encountered.error.processing.issue","'" + issue.getString("key") + "'"));
            log.error("Encountered an error processing the issue '" + issue.getString("key") + "'.", e);
        }
        return errorCollection;
    }

    protected void checkIssueDetails(final GenericValue issue, final ErrorCollection errorCollection, final ConstantsManager constantsManager, final boolean checkWorkflowIntegrity) throws WorkflowException
    {
        Collection linkedStatuses = null;

        final JiraWorkflow originalWorkflow = getWorkflowManager().getWorkflow(issue);

        // Ensure that original workflow is defined
        if (originalWorkflow == null)
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.unable.to.determine.workflow", "'" + issue.getString("key") + "'"));
            log.error("Unable to determine the current workflow for issue '" + issue.getString("key") + "'.");
        }

        // Ensure issue has a workflow entry defined
        final Long originalWfIdString = issue.getLong("workflowId");
        if (originalWfIdString == null)
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.unable.to.determine.workflow.entry",
                "'" + issue.getString("key") + "'"));
            log.error("Unable to determine the current workflow entry for issue '" + issue.getString("key") + "'.");
        }

        final String issueTypeId = issue.getString("type");
        if ((issueTypeId == null) || (constantsManager.getIssueType(issue.getString("type")) == null))
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.unable.to.determine.current.issue.type",
                "'" + issue.getString("key") + "'"));
            log.error("Unable to determine the current issue type for issue '" + issue.getString("key") + "'.");
        }
        // Ensure issue type is defined
        else if (!constantsManager.getAllIssueTypes().contains(constantsManager.getIssueType(issue.getString("type"))))
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.invalid.issue.type", "'" + issue.getString("key") + "'"));
            log.error("Issue '" + issue.getString("key") + "'  does not have a valid issue type.");
        }

        // Ensure original status is valid
        final GenericValue originalStatus = ComponentAccessor.getConstantsManager().getStatus(issue.getString("status"));
        if (originalStatus == null)
        {
            errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.unable.to.determine.status", "'" + issue.getString("key") + "'"));
            log.error("Unable to determine the current status for issue '" + issue.getString("key") + "'.");
        }

        // Only do the following checks if we are checking workflow integrity. When a new workflow is being activated in the
        // system (in JIRA Professional) or when a workflow scheme is being changed for a project (JIRA Enterprise) we do not want to
        // check workflow integrity, as we want to do our best to allow the workflow migration to proceed.
        if (checkWorkflowIntegrity)
        {
            if (originalStatus != null)
            {
                //Ensure status is a linked status within the associated workflow
                if (linkedStatuses == null)
                {
                    linkedStatuses = getWorkflowManager().getWorkflow(issue).getLinkedStatuses();
                }

                if (!linkedStatuses.contains(originalStatus))
                {
                    errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.issue.incorrect.association",
                        "'" + issue.getString("key") + "'", "'" + originalStatus.getString("name") + "'"));
                    log.error("The issue '" + issue.getString("key") + "' is incorrectly associated with the status '" + originalStatus.getString("name") + "'.");
                }
            }

            // Ensure issue has a current step defined - only if the current step has linked transitions from it - i.e. not a final/dead-end step.
            try
            {
                final WorkflowStore store = getWorkflowManager().getStore();
                // Retrieve the stepDescriptors associated with the workflow
                final Collection stepDescriptors = originalWorkflow.getDescriptor().getSteps();

                for (final Object stepDescriptor1 : stepDescriptors)
                {
                    final StepDescriptor stepDescriptor = (StepDescriptor) stepDescriptor1;
                    final Map stepAttributes = stepDescriptor.getMetaAttributes();

                    // Retrieve the status associated with this step
                    final GenericValue statusGV = ComponentAccessor.getConstantsManager().getStatus(
                            (String) stepAttributes.get("jira.status.id"));

                    // Retrieve the actions available for this step if it is linked to the current status of the issue we are looking at
                    if (originalStatus.equals(statusGV))
                    {
                        final Collection actions = stepDescriptor.getActions();

                        // Only check the currentsteps associated with the store if there are actions available
                        // If there are no actions - this is a final/dead-end step - with no further transitions possible.
                        if (!actions.isEmpty())
                        {
                            final Collection workflowSteps = store.findCurrentSteps(originalWfIdString.longValue());

                            if (workflowSteps.isEmpty())
                            {
                                errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.unable.to.determine.current.step",
                                        "'" + issue.getString("key") + "'"));
                                log.error("Unable to determine the current step for issue for issue '" + issue.getString("key") + "'.");
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            catch (final StoreException se)
            {
                errorCollection.addErrorMessage(getI18nBean().getText("admin.errors.error.accessing.workflow.store",
                    "'" + issue.getString("key") + "'"));
                log.error("Error accessing workflow store to determine the current step for issue '" + issue.getString("key") + "'.");
            }
        }
    }

    protected I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    private WorkflowManager getWorkflowManager()
    {
        return ComponentAccessor.getWorkflowManager();
    }
}
