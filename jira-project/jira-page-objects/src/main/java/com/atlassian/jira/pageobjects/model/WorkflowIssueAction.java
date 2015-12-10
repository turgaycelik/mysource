package com.atlassian.jira.pageobjects.model;

import com.atlassian.jira.util.dbc.Assertions;

import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Represents built-in and custom JIRA workflow transitions that are executed as actions in the UI. 
 *
 * For standard issue actions, use {@link com.atlassian.jira.pageobjects.model.DefaultIssueActions}
 *
 * @since v4.3
 */
public class WorkflowIssueAction implements IssueOperation
{
    public static WorkflowIssueAction CLOSE_ISSUE = new WorkflowIssueAction(2L, "Close Issue");

    private static final String ID_TEMPLATE = "action_id_%d";
    private static final String CSS_CLASS = "issueaction-workflow-transition";

    private final long workflowActionId;
    private final String id;
    private final String uiName;

    public WorkflowIssueAction(long workflowActionId, String uiName)
    {
        this.workflowActionId = greaterThan("workflowActionId", workflowActionId, 0);
        this.id = String.format(ID_TEMPLATE, workflowActionId);
        this.uiName = Assertions.notNull("uiName", uiName);
        
    }

    public long workflowActionId()
    {
        return workflowActionId;
    }

    @Override
    public String id()
    {
        return id;
    }

    @Override
    public String uiName()
    {
        return uiName;
    }

    @Override
    public String cssClass()
    {
        return CSS_CLASS;
    }

    @Override
    public boolean hasShortcut()
    {
        return false;
    }

    @Override
    public CharSequence shortcut()
    {
        throw new IllegalStateException("Workflow transitions do not have shortcuts");
    }

    @Override
    public String toString()
    {
        return asString("WorkflowIssueAction[workflowActionId=",workflowActionId,",id=",id,",uiName=",uiName,"]");
    }
}
