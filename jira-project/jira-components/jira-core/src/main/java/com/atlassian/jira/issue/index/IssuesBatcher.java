package com.atlassian.jira.issue.index;

import com.atlassian.jira.issue.util.IssuesIterable;

/**
 * Splits up a large set of issues into batches.
 *
 * @since v5.2
 */
public interface IssuesBatcher extends Iterable<IssuesIterable>
{
}
