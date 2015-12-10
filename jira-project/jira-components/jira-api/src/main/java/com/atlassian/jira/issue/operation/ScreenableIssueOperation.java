package com.atlassian.jira.issue.operation;

import com.atlassian.annotations.PublicApi;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 *
 * Implemeneted vy Issue Operations for which a JIRA Admin can customise a Screen to be shown.
 * For example, CReate Issue and Edit Issue are ScreenableIssueOperations. Move issue is not, as
 * it is a wizard that does not simply show
 */
@PublicApi
public interface ScreenableIssueOperation extends IssueOperation
{
    Long getId();
}
