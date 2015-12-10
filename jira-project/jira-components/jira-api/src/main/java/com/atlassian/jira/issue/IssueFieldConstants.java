/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IssueFieldConstants
{
    private static final Map<String, String> fieldIdsToLabels;
    public static final String PROJECT = "project";
    public static final String FORM_TOKEN = "formToken";
    public static final String AFFECTED_VERSIONS = "versions";
    public static final String ASSIGNEE = "assignee";
    public static final String REPORTER = "reporter";
    public static final String CREATOR = "creator";
    public static final String COMPONENTS = "components";
    public static final String COMMENT = "comment";
    public static final String DESCRIPTION = "description";
    public static final String DUE_DATE = "duedate";
    public static final String ENVIRONMENT = "environment";
    public static final String FIX_FOR_VERSIONS = "fixVersions";
    public static final String ISSUE_KEY = "issuekey";
    public static final String ISSUE_NUMBER = "number";
    public static final String ISSUE_TYPE = "issuetype";
    public static final String THUMBNAIL = "thumbnail";
    public static final String ISSUE_LINKS = "issuelinks";
    public static final String LAST_VIEWED = "lastViewed";
    public static final String WORKRATIO = "workratio";
    // The column displays subtasks for the issue
    public static final String SUBTASKS = "subtasks";
    public static final String ATTACHMENT = "attachment";
    public static final String PRIORITY = "priority";
    public static final String SECURITY = "security";
    public static final String SUMMARY = "summary";
    public static final String TIMETRACKING = "timetracking";
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String RESOLUTION_DATE = "resolutiondate";
    public static final String STATUS = "status";
    public static final String RESOLUTION = "resolution";
    public static final String LABELS = "labels";
    public static final String WORKLOG = "worklog";
    public static final String TIME_ORIGINAL_ESTIMATE = "timeoriginalestimate";
    public static final String TIME_ESTIMATE = "timeestimate";
    public static final String TIME_SPENT = "timespent";
    public static final String AGGREGATE_TIME_SPENT = "aggregatetimespent";
    public static final String AGGREGATE_TIME_ESTIMATE = "aggregatetimeestimate";
    public static final String AGGREGATE_TIME_ORIGINAL_ESTIMATE = "aggregatetimeoriginalestimate";
    public static final String AGGREGATE_PROGRESS = "aggregateprogress";
    public static final String PROGRESS = "progress";
    public static final String VOTES = "votes";
    public static final String VOTERS = "voter";
    public static final String WATCHES = "watches";
    public static final String WATCHERS = "watcher";
    public static final int BLOCKER_PRIORITY_ID = 1;
    public static final int CRITICAL_PRIORITY_ID = 2;
    public static final int MAJOR_PRIORITY_ID = 3;
    public static final int MINOR_PRIORITY_ID = 4;
    public static final int TRIVIAL_PRIORITY_ID = 5;
    public static final String BLOCKER_PRIORITY = "Blocker";
    public static final String CRITICAL_PRIORITY = "Critical";
    public static final String MAJOR_PRIORITY = "Major";
    public static final String MINOR_PRIORITY = "Minor";
    public static final String TRIVIAL_PRIORITY = "Trivial";
    public static final int FIXED_RESOLUTION_ID = 1;
    public static final int WONTFIX_RESOLUTION_ID = 2;
    public static final int DUPLICATE_RESOLUTION_ID = 3;
    public static final int INCOMPLETE_RESOLUTION_ID = 4;
    public static final int CANNOTREPRODUCE_RESOLUTION_ID = 5;
    public static final String FIXED_RESOLUTION = "Fixed";
    public static final String WONTFIX_RESOLUTION = "Won't Fix";
    public static final String DUPLICATE_RESOLUTION = "Duplicate";
    public static final String INCOMPLETE_RESOLUTION = "Incomplete";
    public static final String CANNOTREPRODUCE_RESOLUTION = "Cannot Reproduce";
    public static final int BUG_TYPE_ID = 1;
    public static final int NEWFEATURE_TYPE_ID = 2;
    public static final int TASK_TYPE_ID = 3;
    public static final int IMPROVEMENT_TYPE_ID = 4;
    public static final String BUG_TYPE = "Bug";
    public static final String NEWFEATURE_TYPE = "New Feature";
    public static final String TASK_TYPE = "Task";
    public static final String IMPROVEMENT_TYPE = "Improvement";
    public static final int OPEN_STATUS_ID = 1;
    public static final int UNASSIGNED_STATUS_ID = 2; // obsolete as of jira 2.5
    public static final int INPROGRESS_STATUS_ID = 3; // In Progress
    public static final int REOPENED_STATUS_ID = 4; // Reopened
    public static final int RESOLVED_STATUS_ID = 5; // Resolved
    public static final int CLOSED_STATUS_ID = 6; // Closed
    public static final String OPEN_STATUS = "Open";
    public static final String UNASSIGNED_STATUS = "Unassigned"; // obsolete as of jira 2.5
    public static final String INPROGRESS_STATUS = "In progress";
    public static final String REOPENED_STATUS = "Reopened";
    public static final String RESOLVED_STATUS = "Resolved";
    public static final String CLOSED_STATUS = "Closed";
    public static final String WORKLOG_ID = "WorklogId";
    public static final String WORKLOG_TIME_SPENT = "WorklogTimeSpent";

    public static String getStatusFromId(final int statusId)
    {
        switch (statusId)
        {
            case OPEN_STATUS_ID:
                return OPEN_STATUS;

            case UNASSIGNED_STATUS_ID:
                return UNASSIGNED_STATUS;

            case INPROGRESS_STATUS_ID:
                return INPROGRESS_STATUS;

            case REOPENED_STATUS_ID:
                return REOPENED_STATUS;

            case RESOLVED_STATUS_ID:
                return RESOLVED_STATUS;

            case CLOSED_STATUS_ID:
                return CLOSED_STATUS;

            default:
                return "undefined";
        }
    }

    /* Mapping from JIRA issue statuses to issue workflow statuses, which are slightly out of synch */
    public static int getWorkflowStatusFromIssueStatus(final int issueStatus)
    {
        switch (issueStatus)
        {
            case 1:
                return 1;

            case 2:
                return 2;

            case 3:
                return 3;

            case 4:
                return 5;

            case 5:
                return 4;

            case 6:
                return 6;

            default:
                return -1;
        }
    }

    static
    {
        final Map<String, String> result = new HashMap<String, String>();
        result.put(AFFECTED_VERSIONS, "Affects Versions");
        result.put(ASSIGNEE, "Assignee");
        result.put(DESCRIPTION, "Description");
        result.put(CREATED, "Created");
        result.put(COMPONENTS, "Components");
        result.put(DUE_DATE, "Due Date");
        result.put(ENVIRONMENT, "Environment");
        result.put(FIX_FOR_VERSIONS, "Fix For Versions");
        result.put(ISSUE_TYPE, "Issue Type");
        result.put(PRIORITY, "Priority");
        result.put(SECURITY, "Issue Level Security");
        result.put(SUMMARY, "Summary");
        result.put(TIMETRACKING, "Time Tracking");
        result.put(TIME_SPENT, "Time Spent");
        fieldIdsToLabels = Collections.unmodifiableMap(result);
    }

    public static Map<String, String> getFieldIdsToLabels()
    {
        return fieldIdsToLabels;
    }

    public static boolean isRequiredField(final String field)
    {
        return ("summary".equals(field) || "issuetype".equals(field));
    }
}
