/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag;

public interface JellyTagConstants
{
    public static final String MISSING_VARIABLE = "jelly.missing.one.variable.that.is.required";

    // user constants
    public static final String USER = "jelly.user";
    public static final String USERNAME = "jelly.username";
    public static final String GROUP_NAME = "jelly.group.name";
    public static final String NEW_USERNAME = "jelly.new.username";
    public static final String PASSWORD = "jelly.password";

    // project constants
    public static final String PROJECT_ID = "jelly.project.id";
    public static final String PROJECT_KEY = "jelly.project.key";

    // permission constants
    public static final String PERMISSION_SCHEME_ID = "jelly.permission.scheme.id";
    public static final String ISSUE_SCHEME_ID = "jelly.issue.scheme.id";
    public static final String ISSUE_SCHEME_LEVEL_ID = "jelly.issue.scheme.level.id";

    // issue constants
    public static final String ISSUE_ID = "jelly.issue.id";
    public static final String ISSUE_KEY = "jelly.issue.key";

    // custom field constants
    public static final String CUSTOM_FIELD_ID = "custom.field.id";
}
