package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;

/**
 * Service class to reveal all business logic in converting an issue to a sub-task, including validation.
 * This business component should be used by all clients: web, rpc-soap, jelly, etc.
 */
@PublicApi
public interface IssueToSubTaskConversionService extends IssueConversionService
{

    /**
     * Validates the given parent issue key for issue key. Any errors are
     * communicated back via error collection in the context.
     *
     * @param context                 jira service context
     * @param issue                   issue to convert
     * @param parentIssue             possible parrent issue to check
     * @param fieldNameParentIssueKey form field name of the parrent issue key
     */
    public void validateParentIssue(JiraServiceContext context, Issue issue, Issue parentIssue, final String fieldNameParentIssueKey);



}
