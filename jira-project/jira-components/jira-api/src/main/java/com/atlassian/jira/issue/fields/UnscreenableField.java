package com.atlassian.jira.issue.fields;

/**
 * This is a hack to allow us to have orderable fields that can be configured at the FieldLayoutItem level
 * but that are not ment to be placed on any screens by user configuration. At the time of writting the
 * CommentSystemField is the only example of such a field. If we ever decide to allow users to place
 * the comment field on screens then this can be done away with.
 */
public interface UnscreenableField
{
}
