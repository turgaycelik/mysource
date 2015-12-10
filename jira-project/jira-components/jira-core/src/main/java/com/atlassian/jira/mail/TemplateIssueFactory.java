/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.issue.Issue;

public interface TemplateIssueFactory
{
    public TemplateIssue getTemplateIssue(Issue issue);
}
