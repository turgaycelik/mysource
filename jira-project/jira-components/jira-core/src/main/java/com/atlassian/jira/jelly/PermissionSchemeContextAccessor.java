/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.apache.commons.jelly.JellyContext;

public interface PermissionSchemeContextAccessor
{
    public JellyContext getContext();

    public void setPermissionScheme(Long schemeId);

    public void loadPreviousPermissionScheme();
}
