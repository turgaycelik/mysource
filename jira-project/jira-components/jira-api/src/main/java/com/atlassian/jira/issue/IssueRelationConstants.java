/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

/**
 * Named relations of issues, from the entitymodel.xml file.
 */
public interface IssueRelationConstants
{
    public static final String VERSION = "IssueVersion";
    public static final String FIX_VERSION = "IssueFixVersion";
    public static final String COMPONENT = "IssueComponent";
    public static final String CHANGE_GROUPS = "IssueChangeGroups";
    public static final String WORKFLOW_HISTORY = "IssueWorkflowHistory";
    public static final String COMMENTS = "IssueComments";
    public static final String ATTACHMENTS = "IssueAttachments";
    public static final String TYPE_WORKLOG = "IssueWorklog";
    public static final String LINKS_INWARD = "IssueLinksInward";
    public static final String LINKS_OUTWARD = "IssueLinksOutward";
    public static final String CUSTOM_FIELDS_VALUES = "IssueCustomFieldValues";
}
