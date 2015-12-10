/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 6:11:58 PM
 */
package com.atlassian.jira.workflow;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

public class DefaultJiraWorkflow extends AbstractJiraWorkflow
{
    public static final int STOP_PROGRESS_ACTION_ID = 301;
    private final JiraAuthenticationContext ctx;

    public DefaultJiraWorkflow(final WorkflowDescriptor workflowDescriptor, final WorkflowManager workflowManager, JiraAuthenticationContext ctx)
    {
        super(workflowManager, workflowDescriptor);
        this.ctx = ctx;
    }

    public String getName()
    {
        return "jira";
    }

    @Override
    public String getDisplayName()
    {
        return ctx.getI18nHelper().getText("common.concepts.default.workflow");
    }

    public String getDescription()
    {
        return "The default JIRA workflow.";
    }

    public boolean isEnabled()
    {
        return true;
    }

    /**
     * This method will always return false as this implementation is not used for DraftWorkflows.
     * @since v3.13
     * @return false
     */
    public boolean isDraftWorkflow()
    {
        return false;
    }

    /**
     * This method will always return false as you cannot edit the Default workflow.
     * @since v3.13
     * @return false
     */
    public boolean hasDraftWorkflow()
    {
        return false;
    }
}