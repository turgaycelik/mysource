package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.status;

/**
 * Implements the "Edit Issue" use case.
 *
 * @since v5.0
 */
public class UpdateIssueResource
{
    private final IssueInputParametersAssembler issueInputParametersAssembler;
    private final IssueService issueService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public UpdateIssueResource(IssueInputParametersAssembler issueInputParametersAssembler, IssueService issueService, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.issueInputParametersAssembler = issueInputParametersAssembler;
        this.issueService = issueService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public Response editIssue(Issue issue, IssueUpdateBean updateRequest)
    {
        IssueInputParametersAssembler.Result result = issueInputParametersAssembler.makeUpdateAssembler(updateRequest, issue);
        if (result.getErrors().hasAnyErrors()) {
            throw new BadRequestWebException(ErrorCollection.of(result.getErrors()));
        }

        IssueInputParameters inputParameters = result.getParameters();
        User user = jiraAuthenticationContext.getLoggedInUser();

        IssueService.UpdateValidationResult validation = issueService.validateUpdate(user, issue.getId(), inputParameters);
        if (!validation.isValid())
        {
            throw new BadRequestWebException(ErrorCollection.of(validation.getErrorCollection()));
        }

        IssueService.IssueResult issueResult = issueService.update(user, validation);
        if (!issueResult.isValid())
        {
            throw new BadRequestWebException(ErrorCollection.of(issueResult.getErrorCollection()));
        }

        return status(NO_CONTENT).build();
    }

    public Response transitionIssue(Issue issue, IssueUpdateBean issueUpdateBean)
    {
        IssueInputParametersAssembler.Result result = issueInputParametersAssembler.makeTransitionAssember(issueUpdateBean, issue);
        if (result.getErrors().hasAnyErrors()) {
            throw new BadRequestWebException(ErrorCollection.of(result.getErrors()));
        }

        IssueInputParameters inputParameters = result.getParameters();
        User user = jiraAuthenticationContext.getLoggedInUser();

        final IssueService.TransitionValidationResult validationResult = issueService.validateTransition(user, issue.getId(), Integer.valueOf(issueUpdateBean.getTransition().getId()), inputParameters);

        if (!validationResult.isValid())
        {
            throw new BadRequestWebException(ErrorCollection.of(validationResult.getErrorCollection()));
        }
        else
        {
            IssueService.IssueResult transitionResult = issueService.transition(user, validationResult);
            if (!transitionResult.isValid())
            {
                throw new BadRequestWebException(ErrorCollection.of(transitionResult.getErrorCollection()));
            }

            return status(NO_CONTENT).build();
        }
    }

}
