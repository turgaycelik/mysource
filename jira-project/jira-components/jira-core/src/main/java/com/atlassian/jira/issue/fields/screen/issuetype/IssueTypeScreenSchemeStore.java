package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public interface IssueTypeScreenSchemeStore
{
    String ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME = "IssueTypeScreenScheme";

    Collection getIssueTypeScreenSchemes();

    IssueTypeScreenScheme getIssueTypeScreenScheme(Long id);
}
