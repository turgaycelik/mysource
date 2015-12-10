package com.atlassian.jira.event.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.AbstractEvent;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Map;

/**
 * A UserEvent. The user is the user that the event is occurring on. The initiating user is the person who triggered the
 * event.
 */
public class UserEvent extends AbstractEvent
{
    private User user;
    private final int eventType;
    private User initiatingUser;

    /**
     * @param user The user this event refers to
     */
    public UserEvent(User user, int eventType)
    {
        super();
        this.user = user;
        this.eventType = eventType;
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
        this.initiatingUser = authenticationContext.getLoggedInUser();
    }

    /**
     * @param params Parameters retrieved by the Listener
     * @param user   The user this event refers to
     */
    public UserEvent(Map<String,Object> params, User user, int eventType)
    {
        super(params);
        this.user = user;
        this.eventType = eventType;
        JiraAuthenticationContext authenticationContext = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
        this.initiatingUser = authenticationContext.getLoggedInUser();
    }

    /**
     * Returns the user that the event is occurring on.
     *
     * @return the user that the event is occurring on.
     */
    public User getUser()
    {
        return user;
    }

    /**
     * Returns the user who triggered the event.
     *
     * @return the user who triggered the event.
     */
    public User getInitiatingUser()
    {
        return initiatingUser;
    }

    public int getEventType()
    {
        return eventType;
    }
}
