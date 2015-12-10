package com.atlassian.jira.web.action.admin.issuetypes.events;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Event indicating that an Issue Type has been updated through the Admin Interface.
 *
 * @since v6.2
 */
@EventName ("administration.issuetypes.issuetype.updated.global")
public class IssueTypeUpdatedEvent
{
}
