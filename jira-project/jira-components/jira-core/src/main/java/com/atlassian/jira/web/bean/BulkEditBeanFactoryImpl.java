package com.atlassian.jira.web.bean;

import com.atlassian.jira.issue.IssueManager;

/**
 * @since v5.0
 */
public class BulkEditBeanFactoryImpl implements BulkEditBeanFactory
{
    private final IssueManager issueManager;

    public BulkEditBeanFactoryImpl(IssueManager issueManager)
    {
        this.issueManager = issueManager;
    }

    @Override
    public BulkEditBean createBulkEditBean()
    {
        return new BulkEditBeanImpl(issueManager);
    }
}
