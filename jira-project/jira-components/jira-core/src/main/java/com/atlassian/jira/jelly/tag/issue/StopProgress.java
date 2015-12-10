/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.jira.jelly.IssueContextAccessor;
import com.atlassian.jira.jelly.IssueContextAccessorImpl;
import com.atlassian.jira.jelly.tag.IssueAwareActionTagSupport;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class StopProgress extends IssueAwareActionTagSupport implements IssueContextAccessor
{
    private static final transient Logger log = Logger.getLogger(StopProgress.class);
    private static final String KEY_ISSUE_ID = "key";
    private static final String KEY_PROGRESS_STARTER = "starter";
    private static final String ACTION_ID = "action";
    private boolean hasPreviousUsername = false;
    private String previousUsername = null;
    private final IssueContextAccessor issueContextAccessor;

    public StopProgress()
    {
        setActionName("WorkflowUIDispatcher");
        issueContextAccessor = new IssueContextAccessorImpl(this);
    }

    protected void preContextValidation()
    {
        if (getProperties().containsKey(KEY_ISSUE_ID))
        {
            setIssue(getProperty(KEY_ISSUE_ID));
        }

        if (getProperties().containsKey(KEY_PROGRESS_STARTER))
        {
            setPreviousUsername(getUsername());
            getContext().setVariable(JellyTagConstants.USERNAME, getProperty(KEY_PROGRESS_STARTER.toLowerCase()));
        }
        super.preContextValidation();
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
        setProperty(ACTION_ID, "301");
        setProperty(KEY_ISSUE_ID, getIssue().getString("key"));
    }

    protected void endTagExecution(XMLOutput output)
    {
        super.endTagExecution(output);
        if (hasPreviousUsername)
            getContext().setVariable(JellyTagConstants.USERNAME, getPreviousUsername());
        loadPreviousIssue();
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_ISSUE_ID, ACTION_ID };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    private String getPreviousUsername()
    {
        return previousUsername;
    }

    private void setPreviousUsername(String previousUsername)
    {
        this.hasPreviousUsername = true;
        this.previousUsername = previousUsername;
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
