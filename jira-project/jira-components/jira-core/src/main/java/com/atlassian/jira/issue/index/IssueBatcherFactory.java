package com.atlassian.jira.issue.index;

import org.ofbiz.core.entity.EntityCondition;

/**
 * @since v6.1
 */
public interface IssueBatcherFactory
{
    IssuesBatcher getBatcher();

    IssuesBatcher getBatcher(IssueIdBatcher.Spy spy);

    IssuesBatcher getBatcher(EntityCondition condition);

    IssuesBatcher getBatcher(EntityCondition condition, IssueIdBatcher.Spy spy);
    
    IssuesBatcher getBatcher(EntityCondition condition, IssueIdBatcher.Spy spy, int batchSize);
}
