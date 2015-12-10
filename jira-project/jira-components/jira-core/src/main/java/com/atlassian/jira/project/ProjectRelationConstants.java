/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.jira.association.NodeAssocationType;

public interface ProjectRelationConstants
{
    public static final String PROJECT_CATEGORY = "ProjectCategory";
    public static final String PROJECT_VERSIONCONTROL = "ProjectVersionControl";
    public static final NodeAssocationType PROJECT_CATEGORY_ASSOC = new NodeAssocationType(PROJECT_CATEGORY, "Project", "ProjectCategory");
}
