/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.annotations.PublicApi;

@PublicApi
public interface DocumentConstants
{
    public static final String LUCENE_SORTFIELD_PREFIX = "sort_";
    public static final String ISSUE_ID = "issue_id";
    public static final String ISSUE_KEY = "key";
    public static final String ISSUE_KEY_FOLDED = "key_folded";
    public static final String ISSUE_KEY_NUM_PART = "keynumpart";
    public static final String ISSUE_KEY_NUM_PART_RANGE = "keynumpart_range";
    public static final String PROJECT_ID = "projid";
    public static final String ISSUE_FIXVERSION = "fixfor";
    public static final String ISSUE_COMPONENT = "component";
    public static final String ISSUE_VERSION = "version";
    public static final String ISSUE_DUEDATE = "duedate";
    public static final String ISSUE_SUMMARY = "summary";
    public static final String ISSUE_DESC = "description";

    public static final String ISSUE_SUMMARY_SORT = "summary_sort";
    public static final String ISSUE_TIME_ESTIMATE_ORIG = "timeoriginalestimate";
    public static final String ISSUE_TIME_ESTIMATE_CURR = "timeestimate";
    public static final String ISSUE_TIME_SPENT = "timespent";
    public static final String ISSUE_VOTES = "issue_votes";
    public static final String ISSUE_VOTERS = "issue_voter";
    public static final String ISSUE_WATCHES = "issue_watches";
    public static final String ISSUE_WATCHERS = "issue_watcher";
    public static final String ISSUE_SUBTASKS = "issue_subtasks";
    public static final String ISSUE_PARENTTASK = "issue_parenttask";
    public static final String ISSUE_LINKS = "issue_links";
    public static final String ISSUE_ATTACHMENT = "issue_has_attachment";

    //fields that are just indexed for sorting purposes
    public static final String ISSUE_SORT_SUMMARY = LUCENE_SORTFIELD_PREFIX + "summary";
    public static final String ISSUE_SORT_DESC = LUCENE_SORTFIELD_PREFIX + "description";
    public static final String ISSUE_SORT_ENV = LUCENE_SORTFIELD_PREFIX + "environment";
    public static final String ISSUE_SORT_CREATED = LUCENE_SORTFIELD_PREFIX + "created";
    public static final String ISSUE_SORT_UPDATED = LUCENE_SORTFIELD_PREFIX + "updated";
    public static final String ISSUE_SORT_RESOLUTION_DATE = LUCENE_SORTFIELD_PREFIX + "resolutiondate";
    public static final String ISSUE_SORT_DUEDATE = LUCENE_SORTFIELD_PREFIX + "duedate";
    public static final String PROJECT_KEY = "projkey";
    public static final String PROJECT_NAME = "projname";

    //See comments below
    public static final String ISSUE_ENV = "environment";
    public static final String ISSUE_TYPE = "type";
    public static final String ISSUE_AUTHOR = "issue_author";
    public static final String ISSUE_ASSIGNEE = "issue_assignee";
    public static final String ISSUE_CREATOR = "issue_creator";
    public static final String ISSUE_STATUS = "status";
    public static final String ISSUE_RESOLUTION = "resolution";
    public static final String ISSUE_PRIORITY = "priority";
    public static final String ISSUE_CREATED = "created";
    public static final String ISSUE_UPDATED = "updated";
    public static final String ISSUE_RESOLUTION_DATE = "resolutiondate";
    public static final String ISSUE_CUSTOMFIELD_PREFIX = "customfield_";
    public static final String ISSUE_LABELS = "labels";
    public static final String ISSUE_LABELS_FOLDED = "labels_folded";

    //extra constants
    public static final String ISSUE_UNASSIGNED = "unassigned";
    public static final String ISSUE_NO_AUTHOR = "issue_no_reporter";
    public static final String ISSUE_CURRENT_USER = "issue_current_user";
    public static final String SPECIFIC_USER = "specificuser";
    public static final String SPECIFIC_GROUP = "specificgroup";
    public static final String COMMENT_ID = "id";
    public static final String COMMENT_BODY = "body";
    public static final String COMMENT_LEVEL = "level";
    public static final String COMMENT_LEVEL_ROLE = "role_level";
    public static final String COMMENT_AUTHOR = "comment_author";
    public static final String COMMENT_CREATED = "comment_created";
    public static final String COMMENT_UPDATED = "comment_updated";
    public static final String COMMENT_UPDATE_AUTHOR = "comment_update_author";
    public static final String ISSUE_AUTHOR_GROUP = "issue_author_group";
    public static final String ISSUE_ASSIGNEE_GROUP = "issue_assignee_group";
    public static final String ISSUE_SECURITY_LEVEL = "issue_security_level";
    public static final String CHANGE_DURATION ="ch_duration";
    public static final String CHANGE_DATE = "ch_date";
    public static final String NEXT_CHANGE_DATE="ch_nextchangedate";
    public static final String CHANGE_GROUP_ID="ch_id";
    public static final String CHANGE_ACTIONER = "ch_who";
    public static final String CHANGE_FROM = "ch_from" ;
    public static final String CHANGE_TO = "ch_to" ;
    public static final String OLD_VALUE = "ch_oldvalue";
    public static final String NEW_VALUE = "ch_newvalue";
    public static final String CHANGE_HISTORY_PROTOCOL="ch-";

    public static final String ISSUE_WORKRATIO = "workratio";
    public static final String ISSUE_PROGRESS = "progress";

    // A special field that is used for searching for EMPTY values
    public static final String ISSUE_NON_EMPTY_FIELD_IDS = "nonemptyfieldids";
    // A special field that is used for searching for constraining EMPTY value searches to the issues that are relevant
    public static final String ISSUE_VISIBLE_FIELD_IDS = "visiblefieldids";
    public static final String ISSUE_ANONYMOUS_CREATOR = "issue_anonymous_creator";
    public static final String ISSUE_LAST_VIEWED_DATE = "lastViewed";
}
