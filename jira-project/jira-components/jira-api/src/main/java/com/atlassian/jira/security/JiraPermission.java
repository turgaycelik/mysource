/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

/**
 * Represents a permission
 */
public interface JiraPermission
{
    public int getType();

    public Long getScheme();

    public String getGroup();

    public String getPermType();
}
