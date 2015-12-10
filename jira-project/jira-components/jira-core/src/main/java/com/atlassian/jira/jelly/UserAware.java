/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;


import com.atlassian.crowd.embedded.api.User;

public interface UserAware
{
    String[] getRequiredContextVariables();

    String getUsername();

    User getUser();
}
