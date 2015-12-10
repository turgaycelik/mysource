/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.jira.jelly.ActionTagSupport;
import com.atlassian.jira.jelly.IssueContextAccessor;
import com.atlassian.jira.jelly.IssueContextAccessorImpl;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;

public class LinkIssue extends ActionTagSupport
{
    private static final Logger log = Logger.getLogger(LinkIssue.class);
    protected static final String KEY_ISSUE_KEY = "key";
    protected static final String KEY_DESTINATION_ISSUE_KEY = "linkKey";
    protected static final String KEY_LINK_NAME = "linkDesc";
    private final IssueContextAccessor issueContextAccessor;

    public LinkIssue()
    {
        setActionName("LinkExistingIssue");
        issueContextAccessor = new IssueContextAccessorImpl(this);
    }

    protected void prePropertyValidation(XMLOutput output) throws JellyTagException
    {
    }

    protected void endTagExecution(XMLOutput output)
    {
    }

    public String[] getRequiredProperties()
    {
        return new String[] { KEY_ISSUE_KEY, KEY_DESTINATION_ISSUE_KEY, KEY_LINK_NAME };
    }

    public String[] getRequiredContextVariablesAfter()
    {
        return new String[0];
    }

    public String[] getRequiredContextVariables()
    {
        return new String[0];
    }
}
