package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

// Ensure all issues have a valid workflow state.
public class WorkflowStateCheck extends CheckImpl<CheckAmendment>
{
    public WorkflowStateCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.workflow.state.check.desc");
    }

    public List<CheckAmendment> preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List<CheckAmendment> correct() throws IntegrityException
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

    private List<CheckAmendment> doCheck(boolean correct) throws IntegrityException
    {
        List<CheckAmendment> results = new ArrayList<CheckAmendment>();
        String message;
        try
        {
            for (final GenericValue gvIssue : getIssuesWithWorkflowId())
            {
                Long workflowId = gvIssue.getLong("workflowId");

                List<GenericValue> workflowEntries = ofBizDelegator.findByAnd("OSWorkflowEntry", FieldMap.build("id", workflowId));

                for (GenericValue workflowEntry : workflowEntries)
                {
                    // Ensure the 'state' column is not null or WorkflowEntry.CREATED
                    if (workflowEntry.getInteger("state") == null || "0".equals(workflowEntry.getInteger("state").toString()))
                    {
                        if (correct)
                        {
                            // Fix the problem
                            workflowEntry.set("state", new Integer(WorkflowEntry.ACTIVATED));
                            // Persist the changes
                            workflowEntry.store();

                            message = getI18NBean().getText("admin.integrity.check.workflow.state.check.message", gvIssue.getString("key"), workflowEntry.getLong("id").toString());
                            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4241"));
                        }
                        else
                        {
                            // If we are just previewing then simply record the message
                            message = getI18NBean().getText("admin.integrity.check.workflow.state.check.preview", gvIssue.getString("key"), workflowEntry.getLong("id").toString());
                            results.add(new CheckAmendment(Amendment.ERROR, message, "JRA-4241"));
                        }
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            throw new IntegrityException("Error occurred while performing check.", e);
        }

        return results;
    }

    /**
     * Returns all issues with a limited amount of data just to provide WorkflowId and be able to build the issue key.
     * @return all issues with a limited amount of data just to provide WorkflowId and be able to build the issue key.
     */
    private List<GenericValue> getIssuesWithWorkflowId()
    {
        final List<GenericValue> issues = new LinkedList<GenericValue>();

        OfBizListIterator listIterator = null;
        try
        {
            listIterator = ofBizDelegator.findListIteratorByCondition("Issue", null, null,
                    Arrays.asList("workflowId", IssueFieldConstants.PROJECT, IssueFieldConstants.ISSUE_NUMBER), null, null);
            GenericValue issue = listIterator.next();
            // Retrieve all issues
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            while (issue != null)
            {
                issues.add(issue);
                issue = listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                // Close the iterator
                listIterator.close();
            }

        }

        return issues;
    }
}