package com.atlassian.jira.event;

import com.atlassian.annotations.PublicApi;

import java.util.Date;
import java.util.Map;

/**
 * The base event class for all events fired within JIRA.
 *
 * @see com.atlassian.jira.event.issue.IssueEvent
 * @see com.atlassian.jira.event.user.UserEvent
 */
@PublicApi
public interface JiraEvent
{
    /**
     * The time the event was created
     *
     * @return time of the event
     */
    public Date getTime();

    /**
     * A map of parameters which can be used to pass data to a Listener
     *
     * @return event parameters
     */
    public Map<String,Object> getParams();
}
