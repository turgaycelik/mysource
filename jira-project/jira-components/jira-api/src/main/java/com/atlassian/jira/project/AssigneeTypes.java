/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;

/**
 * Enumerated type representing different default issue assignments for
 * {@link Project} and {@link com.atlassian.jira.bc.project.component.ProjectComponent}.
 */
public abstract class AssigneeTypes
{
    /**
     * Assignee is set to the default for the project.
     */
    public static final long PROJECT_DEFAULT = 0;

    /**
     * Assignee is set to the {@link com.atlassian.jira.bc.project.component.ProjectComponent} lead.
     */
    public static final long COMPONENT_LEAD = 1;

    /**
     * Assignee is set to the {@link com.atlassian.jira.project.Project} lead.
     */
    public static final long PROJECT_LEAD = 2;

    /**
     * Issue is left with no assignee.
     */
    public static final long UNASSIGNED = 3;

    /**
     * Message key representing default assignment to project lead.
     */
    public static final String PRETTY_PROJECT_LEAD = "admin.assignee.type.project.lead";

    /**
     * Message key representing no assignee.
     */
    public static final String PRETTY_UNASSIGNED = "admin.assignee.type.unassigned";

    /**
     * Message key representing default assignment to project default assignee.
     */
    public static final String PRETTY_PROJECT_DEFAULT = "admin.assignee.type.project.default";

    /**
     * Message key to forbid unassigned issues.
     */
    public static final String PRETTY_NOT_ALLOWED_UNASSIGNED = "admin.assignee.type.not.allowed";

    /**
     * Message key to say project lead can't be assigned.
     */
    public static final String PRETTY_NOT_ASSIGNABLE_PROJECT_LEAD = "admin.assignee.type.not.assignable";

    /**
     * Message key representing default assignment to ProjectComponent lead.
     */
    public static final String PRETTY_COMPONENT_LEAD = "admin.assignee.type.component.lead";

    /**
     * Message key to say component lead doesn't exist.
     */
    public static final String PRETTY_COMPONENT_LEAD_DOES_NOT_EXIST = "admin.assignee.type.component.lead.not.exist";

    /**
     * Message key to say compoent lead is not assignable.
     */
    public static final String PRETTY_NOT_ASSIGNABLE_COMPONENT_LEAD = "admin.assignee.type.component.lead.not.assignable";

    /**
     * Provides the application property for permitting issues to be unassigned.
     * @return the value of the application property.
     */
    protected static boolean isAllowUnassigned()
    {
        return ComponentAccessor.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }
}
