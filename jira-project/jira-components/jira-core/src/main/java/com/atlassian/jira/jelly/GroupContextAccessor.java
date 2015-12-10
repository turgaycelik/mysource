/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.crowd.embedded.api.Group;
import org.apache.commons.jelly.JellyContext;

public interface GroupContextAccessor
{
    public JellyContext getContext();

    public void setGroup(String groupname);

    public void setGroup(Group group);

    public void loadPreviousGroup();
}
