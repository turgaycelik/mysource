package com.atlassian.jira.issue.resolution;

import com.atlassian.jira.issue.MockIssueConstant;

/**
 * @since v3.13
 */
public class MockResolution extends MockIssueConstant implements Resolution
{
    public MockResolution(String id, String name)
    {
        super(id, name);
    }
}
