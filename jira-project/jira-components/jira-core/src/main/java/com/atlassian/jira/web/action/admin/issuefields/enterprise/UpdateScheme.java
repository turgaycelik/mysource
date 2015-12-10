/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

public interface UpdateScheme
{
    String INVALID_NAME = "You must specifiy a name.";
    String NAME_ALREADY_EXISTS = "A scheme with that name already exists.";
    String FIELD_LAYOUT_ERROR = "Could not access Field Layouts";

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);
}
