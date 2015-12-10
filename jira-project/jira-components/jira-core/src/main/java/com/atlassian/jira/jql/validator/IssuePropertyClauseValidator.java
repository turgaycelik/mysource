package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;

/**
 * @since 6.2
 */
public class IssuePropertyClauseValidator extends EntityPropertyClauseValidator
{
    public IssuePropertyClauseValidator()
    {
        super(SystemSearchConstants.ISSUE_PROPERTY);
    }
}
