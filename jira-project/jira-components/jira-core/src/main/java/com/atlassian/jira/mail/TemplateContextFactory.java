/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.event.issue.IssueEvent;

import java.util.Locale;

public interface TemplateContextFactory
{
    public TemplateContext getTemplateContext(Locale locale);

    public TemplateContext getTemplateContext(Locale locale, IssueEvent issueEvent);
}
