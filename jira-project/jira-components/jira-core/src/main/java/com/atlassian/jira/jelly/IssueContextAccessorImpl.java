/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Tag;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class IssueContextAccessorImpl implements IssueContextAccessor, IssueAware
{
    private static final Logger log = Logger.getLogger(IssueContextAccessorImpl.class);
    private boolean hasIssueId = false;
    private Long issueId = null;
    private final Tag tag;

    public IssueContextAccessorImpl(Tag tag)
    {
        this.tag = tag;
    }

    public void setIssue(Long issueId)
    {
        setPreviousIssue();
        resetIssueContext();
        setIssueContext(issueId);
    }

    public void setIssue(String issueKey)
    {
        setPreviousIssue();
        resetIssueContext();
        setIssueContext(issueKey);
    }

    public void setIssue(GenericValue issue)
    {
        setPreviousIssue();
        resetIssueContext();
        setIssueContext(issue);
    }

    public void loadPreviousIssue()
    {
        if (hasIssueId)
        {
            resetIssueContext();
            setIssueContext(issueId);
            hasIssueId = false;
            issueId = null;
        }
    }

    private void setPreviousIssue()
    {
        // Store the old project
        if (hasIssue())
        {
            hasIssueId = true;
            this.issueId = getIssueId();
        }
    }

    private void resetIssueContext()
    {
        // Reset the current context
        getContext().removeVariable(JellyTagConstants.ISSUE_ID);
        getContext().removeVariable(JellyTagConstants.ISSUE_KEY);
    }

    private void setIssueContext(Long issueId)
    {
        final GenericValue project = ComponentAccessor.getIssueManager().getIssue(issueId);
        setIssueContext(project);
    }

    private void setIssueContext(String issueKey)
    {
        try
        {
            final GenericValue project = ComponentAccessor.getIssueManager().getIssue(issueKey);
            setIssueContext(project);
        }
        catch (GenericEntityException e)
        {
            log.error(e, e);
        }
    }

    private void setIssueContext(GenericValue issue)
    {
        // Retrieve the new issue
        if (issue != null)
        {
            getContext().setVariable(JellyTagConstants.ISSUE_ID, issue.getLong("id"));
            getContext().setVariable(JellyTagConstants.ISSUE_KEY, issue.getString("key"));
        }
    }

    public JellyContext getContext()
    {
        return tag.getContext();
    }

    public boolean hasIssue()
    {
        return getContext().getVariables().containsKey(JellyTagConstants.ISSUE_ID);
    }

    public Long getIssueId()
    {
        if (hasIssue())
            return (Long) getContext().getVariable(JellyTagConstants.ISSUE_ID);
        else
            return null;
    }

    public GenericValue getIssue()
    {
        return ComponentAccessor.getIssueManager().getIssue(getIssueId());
    }
}
