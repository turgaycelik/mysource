package com.atlassian.jira.appconsistency.integrity.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.util.profiling.UtilTimerStack;

import com.google.common.collect.ImmutableList;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

// Ensure all issues have a valid workflow state.

public class WorkflowCurrentStepCheck extends CheckImpl
{
    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;

    public WorkflowCurrentStepCheck(final OfBizDelegator ofBizDelegator, final int id)
    {
        this(ofBizDelegator, id, ComponentAccessor.getConstantsManager(), ComponentAccessor.getWorkflowManager());
    }

    public WorkflowCurrentStepCheck(final OfBizDelegator ofBizDelegator, final int id, final ConstantsManager constantsManager, final WorkflowManager workflowManager)
    {
        super(ofBizDelegator, id);
        this.workflowManager = workflowManager;
        this.constantsManager = constantsManager;
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.workflow.current.step.desc");
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }

    private List<CheckAmendment> doCheck(final boolean correct) throws IntegrityException
    {
        final List<CheckAmendment> results = new ArrayList<CheckAmendment>();

        final String name = "WorkFlowCurrentStepCheck.doCheck";
        UtilTimerStack.push(name);

        try
        {
            final Collection<Long> issueIds = getIssueIds();

            for (final Object issueId1 : issueIds)
            {
                final Long issueId = (Long) issueId1;
                final GenericValue issue = ofBizDelegator.findById("Issue", issueId);

                if (issue != null)
                {
                    // This can be null
                    // Ref: https://jira.atlassian.com/browse/JRA-4714
                    // Handled after status is got, assuming status can't be null
                    final Long workflowEntryId = issue.getLong("workflowId");

                    final String issueKey = issue.getString("key");

                    final Status status = getStatus(issue.getString("status"));
                    // Integrity checker should come back later and fail this
                    if (status != null)
                    {
                        //Decided to handle this as a manual step as there is no other way to determine the right entry
                        // in the os_wfentry table which is for this issue (or do we need to abandon and create a new one?)
                        if (workflowEntryId == null) {
                                results.add(new CheckAmendment(Amendment.UNFIXABLE_ERROR,
                                        getI18NBean().getText("admin.integrity.check.workflow.current.workflowid.unfixable", issueKey, status.getName()), "JRA-4714"));
                            continue;
                        }
                        // Retrieve all currentsteps associated with this workflowentry - there should be only ONE
                        final List<GenericValue> currentSteps = ofBizDelegator.findByAnd("OSCurrentStep", FieldMap.build("entryId", workflowEntryId));

                        final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
                        final StepDescriptor stepInWorkflow = workflow.getLinkedStep(status);

                        // This can be null
                        // Ref: https://jira.atlassian.com/browse/JRA-4714?focusedCommentId=217851&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-217851
                        if (stepInWorkflow != null)
                        {
                            final int stepInWorkflowId = stepInWorkflow.getId();

                            switch (currentSteps.size())
                            {
                                // No corresponding step - create one if needed
                                case 0:
                                {
                                    // JRA-6721 - an issue may be associated with a 'dead-end' status - i.e. a status
                                    // without any outgoing transitions. In this case, the workflow entry associated with the
                                    // issue will not have an associated current step
                                    if (!stepInWorkflow.getActions().isEmpty())
                                    {
                                        createStep(correct, workflowEntryId, stepInWorkflowId, issue, results);
                                    }
                                    break;
                                }

                                // Only one current step exists - validate the stepId
                                case 1:
                                    validateStep(correct, currentSteps, stepInWorkflowId, issueKey, workflowEntryId, results);
                                    break;

                                // Multiple current steps exist for this workflow entry
                                // In this case we use the last updated currentstep (ensuring stepId is correct), and delete the others
                                default:
                                    deleteSteps(correct, currentSteps, stepInWorkflowId, issueKey, workflowEntryId, results);
                                    break;
                            }
                        }
                        else
                        {
                            results.add(new CheckAmendment(Amendment.UNFIXABLE_ERROR, getI18NBean().getText("admin.integrity.check.workflow.current.step.unfixable", issueKey, status.getName()), "JRA-8326"));
                        }
                    }
                    else if (issue.getString("status") != null)
                    {
                        results.add(new CheckAmendment(Amendment.UNFIXABLE_ERROR,
                                getI18NBean().getText("admin.integrity.check.issue.status.unfixable", issueKey, issue.getString("status")), "JRA-4714"));
                    }
                    else
                    {
                        // Don't bother, null status issues will be caught by WorkflowIssueStatusNull
                        // ofBizDelegator.findByAnd("IssueWorkflowStepView", EasyMap.build("status", null))
                    }
                }
            }
        }
        catch (final Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }
        finally
        {
            UtilTimerStack.pop(name);
        }

        return results;
    }

    // Ensure last currentstep has correct stepId value
    // Delete the other currentsteps
    private void deleteSteps(final boolean correct, final List<GenericValue> currentSteps, final int stepInWorkflowId, final String issueKey, final Long workflowEntryId, final List<CheckAmendment> results) throws GenericEntityException
    {
        String message;
        if (correct)
        {
            GenericValue step = currentSteps.get(0);

            // Check if first currentstep has correct stepId
            if (step.getInteger("stepId") != stepInWorkflowId)
            {
                step.set("stepId", stepInWorkflowId);
                step.store();

                // Record the message
                message = getI18NBean().getText("admin.integrity.check.workflow.current.step.delete.message1", issueKey, workflowEntryId.toString());
                results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
            }

            for (int j = 1; j < currentSteps.size(); j++)
            {
                step = currentSteps.get(j);
                step.remove();
            }

            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.delete.message2", issueKey, workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
        else
        {
            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.delete.preview", issueKey, workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
    }

    //Validate that the exisiting step has the correct stepId
    private void validateStep(final boolean correct, final List<GenericValue> currentSteps, final int stepInWorkflowId, final String issueKey, final Long workflowEntryId, final List<CheckAmendment> results) throws GenericEntityException
    {
        final GenericValue step = currentSteps.get(0);
        final String message;

        if (correct)
        {
            // Check if first currentstep has correct stepId
            if (step.getInteger("stepId") == null || step.getInteger("stepId") != stepInWorkflowId)
            {
                step.set("stepId", stepInWorkflowId);
                step.store();

                // Record the message
                message = getI18NBean().getText("admin.integrity.check.workflow.current.step.validate.message", issueKey, workflowEntryId.toString());
                results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
            }
        }
        else
        {
            if (step.getInteger("stepId") == null || step.getInteger("stepId") != stepInWorkflowId)
            {
                // Record the message
                message = getI18NBean().getText("admin.integrity.check.workflow.current.step.validate.preview", issueKey, workflowEntryId.toString());
                results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
            }
        }
    }

    // Create a step
    private void createStep(final boolean correct, final Long workflowEntryId, final int stepInWorkflowId, final GenericValue issue, final List<CheckAmendment> results) throws StoreException
    {
        final String message;
        if (correct)
        {
            final WorkflowStore store = workflowManager.getStore();
            store.createCurrentStep(workflowEntryId, stepInWorkflowId, null, issue.getTimestamp("created"), null, issue.getString("status"), null);

            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.correct.message", issue.getString("key"), workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
        else
        {
            // Record the message
            message = getI18NBean().getText("admin.integrity.check.workflow.current.step.correct.preview", issue.getString("key"), workflowEntryId.toString());
            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4539"));
        }
    }

    private Status getStatus(final String status)
    {
        return constantsManager.getStatusObject(status);
    }

    public Collection<Long> getIssueIds()
    {
        final Collection<Long> issueIds = new ArrayList<Long>();

        OfBizListIterator listIterator = null;

        try
        {
            // Retrieve all issues
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            listIterator = ofBizDelegator.findListIteratorByCondition("Issue", null, null, ImmutableList.of("id"), null, null);
            GenericValue issueIdGV = listIterator.next();
            while (issueIdGV != null)
            {
                issueIds.add(issueIdGV.getLong("id"));
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

        return issueIds;
    }
}
