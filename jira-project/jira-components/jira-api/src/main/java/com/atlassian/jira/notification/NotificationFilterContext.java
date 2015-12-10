package com.atlassian.jira.notification;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.type.NotificationType;

import java.util.HashMap;
import java.util.Map;

/**
 * A context object is passed to each {@link com.atlassian.jira.notification.NotificationFilter} instance.  It tells you
 * why the notification is being sent.  It also allows state to be associated with it to help out each plugin.
 *
 * @since 6.0
 */
@PublicApi
public class NotificationFilterContext
{
    private final NotificationReason reason;
    private final Issue issue;
    private final Map<String, Object> state;

    public NotificationFilterContext()
    {
        this(null, null);
    }


    NotificationFilterContext(NotificationReason reason)
    {
        this(reason, null);
    }

    NotificationFilterContext(NotificationReason reason, Issue issue)
    {
        this.reason = reason;
        this.issue = issue;
        this.state = new HashMap<String, Object>();
    }

    /**
     * Copies the state from one NotificationFilterContext object to another
     * @param copy the object to copy
     */
    NotificationFilterContext(NotificationFilterContext copy)
    {
        this.reason = copy.reason;
        this.issue = copy.issue;
        this.state = copy.state;
    }

    public NotificationReason getReason()
    {
        return reason;
    }

    public Issue getIssue()
    {
        return issue;
    }

    /**
     * Allows you to get previously put state into this context.
     *
     * @return the value of the state object
     */
    public Object get(String stateKey)
    {
        return state.get(stateKey);
    }

    /**
     * Allows you to put state into the Context.
     *
     * @param stateKey the key of the staTe object
     * @param stateValue the value of the state object
     * @return the previs value eg {@link java.util.Map#put(Object, Object)}
     */
    public Object put(String stateKey, Object stateValue)
    {
        return state.put(stateKey, stateValue);
    }
}
