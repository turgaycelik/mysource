package com.atlassian.jira.issue.search;

import com.atlassian.jira.issue.Issue;

/**
 * A basic interface to allow fields to compare two issues
 */
public interface IssueComparator
{
    int compare(Issue issue1, Issue issue2);
}
