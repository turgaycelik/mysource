package com.atlassian.jira.workflow.function.issue;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.spi.WorkflowEntry;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Map;

/**
 * Abstract base class for all JIRA workflow {@link FunctionProvider}s (eg. post-functions).
 *
 * For JIRA FunctionProviders implementing the method:
 * <pre>
 *     public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException;
 * </pre>
 * <ul>
 * <li>The 'transientVars' parameter will be populated with values specific to only this invocation, eg. the current user and issue.
 * <li>The 'args' parameter will be populated with static configuration values set when the function was first added.
 * Contents are partly source from the plugin factory class, via {@link com.atlassian.jira.plugin.workflow.WorkflowPluginFactory#getDescriptorParams(java.util.Map)}
 * </ul>
 * @see com.atlassian.jira.plugin.workflow.WorkflowPluginFactory
 */
@PublicSpi
public abstract class AbstractJiraFunctionProvider extends WorkflowFunctionUtils implements FunctionProvider
{
    /**
     * This method retrieves the (potentially modified) issue object that is being transitioned through workflow.
     *
     * @param transientVars
     * @return the issue object representing the issue the functions shoudl modify
     * @throws com.atlassian.jira.exception.DataAccessException If for some reason the issue doesn't exist.
     */
    protected MutableIssue getIssue(Map transientVars) throws DataAccessException
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
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
