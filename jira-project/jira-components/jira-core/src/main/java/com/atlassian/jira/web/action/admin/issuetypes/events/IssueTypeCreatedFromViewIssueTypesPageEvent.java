package com.atlassian.jira.web.action.admin.issuetypes.events;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Event indicating an Issue Type has been created from the View Issue Types page.
 *
 * @since v6.2
 */
@EventName ("administration.issuetypes.issuetype.created.global")
public class IssueTypeCreatedFromViewIssueTypesPageEvent
{
}
