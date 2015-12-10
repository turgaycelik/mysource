package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This check will update an issues status with the value in the workflow entry table if null.
 */
public class WorkflowIssueStatusNull extends CheckImpl
{
    private final WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();

    private final ProjectManager projectManager = ComponentAccessor.getProjectManager();

    public WorkflowIssueStatusNull(final OfBizDelegator ofBizDelegator, final int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.workflow.issue.status.desc");
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

        final HashMap<String, List<Long>> statusKeysMap = new HashMap<String, List<Long>>();
        final HashMap<String, Status> statusCache = new HashMap<String, Status>();

        try
        {
            // get all the issueSteps with status of null
            final List<GenericValue> issueSteps = ofBizDelegator.findByAnd("IssueWorkflowStepView", FieldMap.build("status", null));

            for (final GenericValue genericValue : issueSteps)
            {
                final String issueKey = JiraKeyUtils.fastFormatIssueKey(
                        projectManager.getProjectObj(genericValue.getLong("project")).getKey(), genericValue.getLong("issuenum"));

                try
                {
                    final Status status = getStatusFromWorkflow(genericValue, statusCache);
                    final String statusFromWorkflow = status.getId();
                    final Long issueId = genericValue.getLong("issueid");
                    List<Long> issueIds = statusKeysMap.get(statusFromWorkflow);
                    if (issueIds == null)
                    {
                        issueIds = new ArrayList<Long>();
                        statusKeysMap.put(statusFromWorkflow, issueIds);
                    }
                    issueIds.add(issueId);
                    if (correct)
                    {
                        results.add(new CheckAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.workflow.issue.status.message", issueKey,
                                status.getName()), "JRA-7428"));
                    }
                    else
                    {
                        // If we are just previewing then simply record the message
                        results.add(new CheckAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.workflow.issue.status.preview", issueKey,
                                status.getName()), "JRA-7428"));
                    }
                }
                catch (final IllegalStateException ise)
                {
                    results.add(new CheckAmendment(Amendment.UNFIXABLE_ERROR, getI18NBean().getText("admin.integrity.check.workflow.issue.status.unfixable", issueKey, ise.getMessage()), "JRA-7428"));
                }
            }

            if (correct)
            {
                // iterate through the map and perform a bulk update for each set of issues and status
                for (final Object o : statusKeysMap.keySet())
                {
                    final String statusId = (String) o;
                    final List<Long> issueIds = statusKeysMap.get(statusId);
                    ofBizDelegator.bulkUpdateByPrimaryKey("Issue", FieldMap.build("status", statusId), issueIds);
                }
            }
        }
        catch (final Exception e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }

        return results;
    }

    private Status getStatusFromWorkflow(final GenericValue genericValue, final HashMap<String, Status> statusCache) throws WorkflowException, IllegalStateException
    {
        final String issueType = genericValue.getString("type");
        final Long projectId = genericValue.getLong("project");

        final String cacheKey = projectId + ":" + issueType + ":" + genericValue.getInteger("stepId");

        Status status = statusCache.get(cacheKey);
        if(status == null)
        {
            final JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueType);
            if(workflow == null)
                throw new IllegalStateException("Workflow for project id " + projectId + " and issue type id " + issueType + " is not defined");

            final Integer stepId = genericValue.getInteger("stepId");
            if (stepId == null)
                throw new IllegalStateException("Issue has no status, and status cannot be derived as the workflow step for this issue is missing.");

            final StepDescriptor step = workflow.getDescriptor().getStep(stepId);
            if(step == null)
                throw new IllegalStateException("Can not resolve a step with id: " + stepId + " from workflow " + workflow.getName());

            status = workflow.getLinkedStatusObject(step);
            if(status == null)
                throw new IllegalStateException("Can not resolve a linked status for workflow step " + step.getName());

            statusCache.put(cacheKey, status);
        }
        return status;
    }
}
