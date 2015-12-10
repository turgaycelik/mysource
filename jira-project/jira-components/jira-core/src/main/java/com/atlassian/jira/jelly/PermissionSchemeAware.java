/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import org.ofbiz.core.entity.GenericValue;

public interface PermissionSchemeAware extends UserAware
{
    public boolean hasPermissionScheme();

    public Long getPermissionSchemeId();

    public GenericValue getPermissionScheme();
}
