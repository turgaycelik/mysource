package com.atlassian.jira.index;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.issue.changehistory.ChangeHistoryGroup;

/**
 * Interface for extractors adding fields based on comments
 * @since 6.2
 */
@ExperimentalApi
public interface ChangeHistorySearchExtractor extends EntitySearchExtractor<ChangeHistoryGroup>
{

}
