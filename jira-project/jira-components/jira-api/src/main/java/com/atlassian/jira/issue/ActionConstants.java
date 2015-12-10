/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

/**
 * A holder for constants representing each type of Action in the sense that
 * comment and worklog entities were known as Actions. This should not be
 * confused with Webwork Actions.
 */
public final class ActionConstants
{
    /**
     * A comment on an issue that has an author, a body, and optionally,
     * a group level and role level.
     */
    public static final String TYPE_COMMENT = "comment";
    
    public static final String TYPE_FIELDCHANGE = "fieldchange";

    /**
     * A work log entry.
     */
    public static final String TYPE_WORKLOG = "worklog";

    private ActionConstants()
    {
    }
}
