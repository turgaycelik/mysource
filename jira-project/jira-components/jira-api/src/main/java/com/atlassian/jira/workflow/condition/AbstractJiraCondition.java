package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Map;

/**
 * Abstract base class for all JIRA workflow {@link Condition}s.
 */
public abstract class AbstractJiraCondition extends WorkflowFunctionUtils implements Condition
{
    /**
     * This method retrieves the <b>original</b> (unmodified) issue object that will be examined for the condition check.
     * In order to avoid multiple calls to the database to retrieve this object - include the original issue
     * object in the map passed - transientVars - for example:
     *
     *  GenericValue origianlIssueGV = ComponentAccessor.getIssueManager().getIssue(issue.getId());
     *  fields.put(AbstractJiraCondition.ORIGNAL_ISSUE_KEY, IssueImpl.getIssueObject(origianlIssueGV));
     *
     * If this method is overwriten, the logic should accommodate the retrieval of the original issue object.
     * @param transientVars see {@link com.opensymphony.workflow.Condition#passesCondition(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet)}
     * @return the original issue object
     * @throws DataAccessException If for some reason the issue doesn't exist.
     */
    protected Issue getIssue(Map transientVars) throws DataAccessException
    {
        Issue issue = (Issue) transientVars.get(ORIGINAL_ISSUE_KEY);
        if (issue == null)
        {
            WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
            try
            {
                issue = ComponentAccessor.getIssueManager().getIssueObjectByWorkflow(entry.getId());
            }
            catch (GenericEntityException e)
            {
                throw new DataAccessException("Problem looking up issue with workflow entry id "+entry.getId());
            }
            if (issue == null) throw new DataAccessException("No issue found with workflow entry id "+entry.getId());
        }
        return issue;
    }
}
