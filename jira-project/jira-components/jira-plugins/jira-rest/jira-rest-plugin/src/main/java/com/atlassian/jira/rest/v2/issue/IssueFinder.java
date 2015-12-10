package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;

/**
 * A simple service that encapsulates the logic if finding an Issue by id-or-key.
 * This implementation will attempt to find issues even if the case of the key is wrong, or if the issue is moved.
 *
 * @since v5.0
 */
public class IssueFinder
{
    private final com.atlassian.jira.issue.fields.rest.IssueFinder finder;

    public IssueFinder(com.atlassian.jira.issue.fields.rest.IssueFinder finder)
    {
        this.finder = finder;
    }

    /**
     *
     * @throws NotFoundWebException if not found
     * @throws RESTException on permission problems etc
     */
    public MutableIssue getIssueObject(String issueIdOrKey) throws NotFoundWebException
    {
        com.atlassian.jira.util.ErrorCollection errors = new SimpleErrorCollection();
        MutableIssue issue = (MutableIssue) finder.findIssue(issueIdOrKey, errors);
        if (issue == null)
        {
            throw new RESTException(ErrorCollection.of(errors));
        }

        return issue;
    }

}
