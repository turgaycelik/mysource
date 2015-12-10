package com.atlassian.jira.issue.index;

/**
* Event raised when several issues are being reindexed. This is triggered e.g. when a version gets deleted
* and all issues in that version need to be reindexed. This is separate from the "reindex all" that happens
* via the admin section or data import.
* @since v5.0
*/
public class ReindexIssuesStartedEvent
{}
