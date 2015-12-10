package com.atlassian.jira.web.action.workflow;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;

/**
 * This action handles workflow action without screens (WorkflowUIDispatcher redirects to this action).
 */
public class SimpleWorkflowAction extends AbstractIssueSelectAction
{
    private int action;
    private final IssueService issueService;

    public SimpleWorkflowAction(final IssueService issueService)
    {
        this.issueService = issueService;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final IssueService.TransitionValidationResult transitionValidationResult = issueService.validateTransition(
                getLoggedInUser(), getIssueObject().getId(), getAction(), issueService.newIssueInputParameters());

        if (!transitionValidationResult.isValid())
        {
            addErrorCollection(transitionValidationResult.getErrorCollection());
            return ERROR;
        }

        final IssueService.IssueResult transitionResult = issueService.transition(getLoggedInUser(), transitionValidationResult);

        if (!transitionResult.isValid())
        {
            addErrorCollection(transitionResult.getErrorCollection());
            return ERROR;
        }

        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return redirectToView();
    }

    private int getAction()
    {
        return action;
    }

    public void setAction(final int action)
    {
        this.action = action;
    }
}
