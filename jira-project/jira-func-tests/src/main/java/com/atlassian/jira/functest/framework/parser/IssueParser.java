package com.atlassian.jira.functest.framework.parser;

import com.atlassian.jira.functest.framework.changehistory.ChangeHistoryList;
import com.atlassian.jira.functest.framework.parser.comment.Comment;
import com.atlassian.jira.functest.framework.parser.issue.ViewIssueDetails;
import com.atlassian.jira.functest.framework.parser.worklog.Worklog;

import java.util.List;

/**
 * Parse Issue related stuff
 *
 * @since v3.13
 */
public interface IssueParser
{
    ViewIssueDetails parseViewIssuePage();

    /**
     * Parse the Change History Table
     *
     * @return List of Change History Sets
     */
   ChangeHistoryList parseChangeHistory();

    /**
     * Parse the comments on the view issue page.
     * @return List of {@link com.atlassian.jira.functest.framework.parser.comment.Comment}'s.
     */
    List<Comment> parseComments();

    /**
     * Parse the worklogs on the view issue page.
     * @return List of {@link com.atlassian.jira.functest.framework.parser.worklog.Worklog}
     */
    List<Worklog> parseWorklogs();

}
