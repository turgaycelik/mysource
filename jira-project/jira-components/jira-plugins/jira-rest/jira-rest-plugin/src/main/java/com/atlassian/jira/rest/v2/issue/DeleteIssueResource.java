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
 * Extensions to the JIRA issue resource. This code should eventually be moved into JIRA.
 */
public class DeleteIssueResource
{
    private final IssueService issueService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SubTaskManager subTaskManager;

    public DeleteIssueResource(JiraAuthenticationContext jiraAuthenticationContext, IssueService issueService, SubTaskManager subTaskManager)
    {
        this.issueService = issueService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.subTaskManager = subTaskManager;
    }

    public Response deleteIssue(Issue issue, String deleteSubtasks, @Context UriInfo uriInfo)
    {
        IssueService.DeleteValidationResult issueValidationResult = issueService.validateDelete(callingUser(), issue.getId());
        if (!issueValidationResult.isValid())
        {
            throw error(ErrorCollection.of(issueValidationResult.getErrorCollection()));
        }

        // If the issue has subtask, the delete subtask flag must be true before we will delete the issue.
        if (!Boolean.valueOf(deleteSubtasks) && getNumberOfSubTasks(issue) > 0)
        {
            throw error(ErrorCollection.of(jiraAuthenticationContext.getI18nHelper().getText("rest.issue.delete.subtasks.present", issue.getKey()))
                    .reason(com.atlassian.jira.util.ErrorCollection.Reason.VALIDATION_FAILED));
        }

        final com.atlassian.jira.util.ErrorCollection errors = issueService.delete(callingUser(), issueValidationResult);
        if (errors.hasAnyErrors())
        {
            throw error(ErrorCollection.of(errors));
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

    public int getNumberOfSubTasks(Issue issue)
    {
         return subTaskManager.getSubTaskIssueLinks(issue.getLong("id")).size();
    }
}
