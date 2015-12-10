package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Extensions to the JIRA issue resource.
 */
public class AssignIssueResource
{
    private final IssueService issueService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public AssignIssueResource(JiraAuthenticationContext jiraAuthenticationContext, IssueService issueService)
    {
        this.issueService = issueService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public Response assignIssue(Issue issue, String assignee)
    {
        IssueService.AssignValidationResult issueValidationResult = issueService.validateAssign(callingUser(), issue.getId(), assignee);
        if (!issueValidationResult.isValid())
        {
            throw error(ErrorCollection.of(issueValidationResult.getErrorCollection()));
        }

        final IssueService.IssueResult issueResult = issueService.assign(callingUser(), issueValidationResult);
        if (!issueResult.isValid())
        {
            throw error(ErrorCollection.of(issueResult.getErrorCollection()));
        }

        return Response.noContent().cacheControl(never()).build();
    }

    protected WebApplicationException error(final ErrorCollection errors)
    {
        return new WebApplicationException(Response.status(errors.getStatus()).entity(errors).cacheControl(never()).build());
    }

    protected User callingUser()
    {
        return jiraAuthenticationContext.getLoggedInUser();
    }
}
