package com.atlassian.jira.web.action.admin.issuetypes.events;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Event indicating that the default Issue Type of an Issue Type Scheme has been changed.
 *
 * @since v6.2
 */
@EventName ("administration.issuetypeschemes.defaultvalue.updated.global")
public class IssueTypeSchemeDefaultValueUpdatedThroughActionEvent
{
}
