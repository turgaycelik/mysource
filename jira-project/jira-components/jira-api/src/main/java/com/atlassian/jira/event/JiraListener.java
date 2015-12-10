package com.atlassian.jira.event;

import java.util.EventListener;
import java.util.Map;

/**
 * A JIRA-specific {@link EventListener}.
 * <p/>
 * Implementations must have a no-arg constructor so that JIRA can create them.
 *
 * @see com.atlassian.jira.event.issue.IssueEventListener
 * @see com.atlassian.jira.event.user.UserEventListener
 */
public interface JiraListener extends EventListener
{
    /**
     * Initialises the listener with the given parameters.
     *
     * @param params the initialisation parameters given in the Listener Properties
     */
    void init(Map<String, String> params);

    /**
     * Returns the parameters used by this listener.
     *
     * @return a non-null array (can be empty)
     */
    String[] getAcceptedParams();

    /**
     * Indicates whether this listener is internal, meaning it cannot be
     * removed by an administrator.
     *
     * @return <code>true</code> if this is an Atlassian listener, otherwise <coede>false</code>
     */
    boolean isInternal();

    /**
     * Indicates whether JIRA should only create one instance of this listener.
     * For example, having multiple mail listeners would be fine if you wanted
     * multiple mails sent out. For other listeners, such as cache listeners,
     * it makes no sense to have multiple instances.
     *
     * @return whether this listener should be a singleton
     */
    boolean isUnique();

    /**
     * Returns a textual description of the listener.  You can include HTML if
     * required, but do not use tables or DHTML, as the description may be
     * displayed inside tables or frames.
     * <p/>
     * A good description will describe what this listener does and explain the
     * parameters required for configuring it.
     *
     * @return a description of the listener, or null if no description is appropriate
     */
    String getDescription();
}
