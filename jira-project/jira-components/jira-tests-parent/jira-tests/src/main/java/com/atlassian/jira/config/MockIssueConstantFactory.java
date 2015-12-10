package com.atlassian.jira.config;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.priority.PriorityImpl;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.resolution.ResolutionImpl;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.StatusImpl;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.2
 */
public class MockIssueConstantFactory implements IssueConstantFactory
{

    private final StatusCategoryManager statusCategoryManager;

    public MockIssueConstantFactory()
    {
        statusCategoryManager = null;
    }

    public MockIssueConstantFactory(StatusCategoryManager statusCategoryManager){
        this.statusCategoryManager = statusCategoryManager;
    }

    @Override
    public Priority createPriority(GenericValue priorityGv)
    {
        return new PriorityImpl(priorityGv, null, null, null);
    }

    @Override
    public IssueType createIssueType(GenericValue issueTypeGv)
    {
        return new IssueTypeImpl(issueTypeGv, null, null, null, null);
    }

    @Override
    public Resolution createResolution(GenericValue resolutionGv)
    {
        return new ResolutionImpl(resolutionGv, null, null, null);
    }

    @Override
    public Status createStatus(GenericValue statusGv)
    {
        return new StatusImpl(statusGv, null, null, null, statusCategoryManager);
    }
}
