/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.worklog.WorkRatio;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Gets security Level info out of the database
 *
 * @deprecated This information is all available in the Issue object and this object is not actually used anywhere in core JIRA. Since v6.3.
 */
public class IssueTableBean
{
    private final SubTaskManager subTaskManager;
    private final IssueManager issueManager;

    public IssueTableBean()
    {
        subTaskManager = ComponentAccessor.getSubTaskManager();
        issueManager = ComponentAccessor.getIssueManager();
    }

    public GenericValue getSecurityLevel(GenericValue issue) throws Exception
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }

        final IssueSecurityLevelManager secur = ComponentAccessor.getIssueSecurityLevelManager();
        return secur.getIssueSecurity(issue.getLong("security"));
    }

    public Collection getFixVersions(GenericValue issue) throws GenericEntityException
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        return ComponentAccessor.getIssueManager().getEntitiesByIssue(IssueRelationConstants.FIX_VERSION, issue);
    }

    public Collection getVersions(GenericValue issue) throws GenericEntityException
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        return ComponentAccessor.getIssueManager().getEntitiesByIssue(IssueRelationConstants.VERSION, issue);
    }

    public Collection getComponents(GenericValue issue) throws GenericEntityException
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        return ComponentAccessor.getIssueManager().getEntitiesByIssue(IssueRelationConstants.COMPONENT, issue);
    }

    // ---- Work Ratio Methods ----

    /* Returns a string representing the ratio of actual time spent vs original time estimate. */
    public long getWorkRatio(GenericValue issue)
    {
        return WorkRatio.getWorkRatio(issue);
    }

    // Returns a 'padded' string representing the ratio of actual time spent vs original time estimate.
    // The string is padded so that it can be used in a lucene range query.
    public String getPaddedWorkRatio(GenericValue issue)
    {
        return WorkRatio.getPaddedWorkRatio(issue);
    }

    public boolean isWorkEstimateExists(GenericValue issue)
    {
        return issue.get("timeoriginalestimate") != null;
    }

    public String getParentIssueKey(Issue issue) throws GenericEntityException
    {
        return getParentIssueKey(issue.getGenericValue());
    }

    public String getParentIssueKey(GenericValue issue) throws GenericEntityException
    {
        Long parentIssueId = subTaskManager.getParentIssueId(issue);
        if (parentIssueId != null)
        {
            Issue parentIssue = issueManager.getIssueObject(parentIssueId);
            if (parentIssue != null)
            {
                return parentIssue.getKey();
            }
        }
        return null;
    }

    public String getParentIssueSummary(Issue issue) throws GenericEntityException
    {
        return getParentIssueSummary(issue.getGenericValue());
    }

    public String getParentIssueSummary(GenericValue issue) throws GenericEntityException
    {
        Long parentIssueId = subTaskManager.getParentIssueId(issue);
        if (parentIssueId != null)
        {
            Issue parentIssue = issueManager.getIssueObject(parentIssueId);
            if (parentIssue != null)
            {
                return parentIssue.getSummary();
            }
        }
        return null;
    }

    public Collection getSubTasks(GenericValue issue)
    {
        List<GenericValue> subTaskIssues = new LinkedList<GenericValue>();
        for (IssueLink issueLink : subTaskManager.getSubTaskIssueLinks(issue.getLong("id")))
        {
            subTaskIssues.add(issueLink.getDestinationObject().getGenericValue());
        }
        return subTaskIssues;
    }
}
