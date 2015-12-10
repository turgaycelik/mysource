/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

public interface NewUserAware
{
    public boolean hasNewUsername();

    public String getNewUsername();
}
