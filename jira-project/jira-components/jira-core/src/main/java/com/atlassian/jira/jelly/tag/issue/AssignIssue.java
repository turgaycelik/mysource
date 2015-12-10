/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.jira.jelly.IssueContextAccessor;
import com.atlassian.jira.jelly.IssueContextAccessorImpl;
import com.atlassian.jira.jelly.tag.IssueAwareActionTagSupport;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class AssignIssue extends IssueAwareActionTagSupport implements IssueContextAccessor
{
    private static final transient Logger log = Logger.getLogger(AssignIssue.class);
    private static final String KEY_ISSUE_ID = "key";
    private static final String KEY_ISSUE_ASSIGNEE = "assignee";
    private boolean hasPreviousUsername = false;
    private String previousUsername = null;
    private final IssueContextAccessor issueContextAccessor;

    public AssignIssue()
    {
        setActionName("AssignIssue");
        issueContextAccessor = new IssueContextAccessorImpl(this);
    }

    protected void preContextValidation()
    {
        if (getProperties().containsKey(KEY_ISSUE_ID))
        {
            setIssue(getProperty(KEY_ISSUE_ID));
        }

        super.preContextValidation();
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        setProperty(KEY_ISSUE_ID, getIssue().getString("key"));

        // Make sure the usernames are lowercase.
        String assignee = getProperty(KEY_ISSUE_ASSIGNEE);
        if (assignee != null)
        {
            setProperty(KEY_ISSUE_ASSIGNEE, assignee.toLowerCase());
        }
    }

    protected void endTagExecution(XMLOutput output)
    {
        super.endTagExecution(output);
        loadPreviousIssue();
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_ISSUE_ID, KEY_ISSUE_ASSIGNEE };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    public void setIssue(Long issueId)
    {
        issueContextAccessor.setIssue(issueId);
    }

    public void setIssue(String issueKey)
    {
        issueContextAccessor.setIssue(issueKey);
    }

    public void setIssue(GenericValue issue)
    {
        issueContextAccessor.setIssue(issue);
    }

    public void loadPreviousIssue()
    {
        issueContextAccessor.loadPreviousIssue();
    }
}
