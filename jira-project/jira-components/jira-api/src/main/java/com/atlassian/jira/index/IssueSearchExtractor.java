package com.atlassian.jira.index;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.issue.Issue;

/**
 * Interface for extractors adding fields based on issues
 * @since 6.2
 */
@ExperimentalApi
public interface IssueSearchExtractor extends EntitySearchExtractor<Issue>
{

}
