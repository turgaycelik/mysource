/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.index;

public interface IndexAdmin
{
    String getIndexPath();

    String doActivate() throws Exception;

    String doReindex() throws Exception;

    long getReindexTime();
}
