/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.crowd.embedded.api.User;
import org.apache.commons.jelly.JellyContext;

public interface NewUserContextAccessor
{
    public JellyContext getContext();

    public void setNewUser(String username);

    public void setNewUser(User user);

    public void loadPreviousNewUser();
}
