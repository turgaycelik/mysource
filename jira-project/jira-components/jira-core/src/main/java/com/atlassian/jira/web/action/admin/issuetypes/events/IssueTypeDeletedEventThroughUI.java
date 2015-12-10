package com.atlassian.jira.web.action.admin.issuetypes.events;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Event indicating that an Issue Type has been deleted through the admin interface.
 *
 * @since v6.2
 */
@EventName ("administration.issuetypes.issuetype.deleted.global")
public class IssueTypeDeletedEventThroughUI
{
}
