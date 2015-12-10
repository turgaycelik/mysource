package com.atlassian.jira.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @deprecated Use {@link com.atlassian.jira.workflow.IssueWorkflowManager} instead. Since v5.0.
 */
public class IssueUtilsBean
{
    private static final Logger log = Logger.getLogger(IssueUtilsBean.class);

    private final WorkflowManager workflowManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueWorkflowManager issueWorkflowManager;

    public IssueUtilsBean(final WorkflowManager workflowManager,
            final JiraAuthenticationContext authenticationContext, final IssueWorkflowManager issueWorkflowManager)
    {
        this.workflowManager = workflowManager;
        this.authenticationContext = authenticationContext;
        this.issueWorkflowManager = issueWorkflowManager;
    }

    /**
     * Gets available actions for given issue and current user.
     * <p>
     * <b>Remember to set current user in context when using this method.</b>
     * @deprecated Use {@link com.atlassian.jira.workflow.IssueWorkflowManager#getAvailableActions(Issue, com.atlassian.jira.user.ApplicationUser)} instead. Since v5.0.
     */
    public Map<Integer, ActionDescriptor> loadAvailableActions(final Issue issueObject)
    {
        final Map<Integer, ActionDescriptor> availableActions = new LinkedHashMap<Integer, ActionDescriptor>();
        for (final ActionDescriptor actionDescriptor : issueWorkflowManager.getAvailableActions(issueObject))
        {
            availableActions.put(actionDescriptor.getId(), actionDescriptor);
        }
        return availableActions;
    }

    /**
     * Is this a valid action for the issue in its current state (for current user).
     * <p>
     * <b>Remember to set current user in context when using this method.</b>
     * @param issue  the issue
     * @param action the id of the action we want to transition
     * @return true if it is ok to use this transition
     *
     * @deprecated Use {@link com.atlassian.jira.workflow.IssueWorkflowManager#isValidAction(Issue, int)} instead. Since v5.0.
     */
    public boolean isValidAction(final Issue issue, final int action)
    {
        return loadAvailableActions(issue).containsKey(action);
    }

    /**
     * @deprecated This should have been private. Use {@link WorkflowManager#makeWorkflow(com.atlassian.crowd.embedded.api.User)} instead. Since v5.0.
     */
    public Workflow getWorkflow()
    {
        return workflowManager.makeWorkflow(authenticationContext.getUser());
    }

    @Deprecated
    public GenericValue setPriority(final GenericValue issue, final User remoteUser, final String priority) throws Exception
    {
        return IssueUtils.setPriority(issue, remoteUser, priority);
    }
}
