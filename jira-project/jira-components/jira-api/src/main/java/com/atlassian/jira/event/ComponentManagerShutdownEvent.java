package com.atlassian.jira.event;

/**
 * Raised when the ComponentManager is about to be shutdown
 *
 * @since v5.2
 */
public class ComponentManagerShutdownEvent
{
    public static final ComponentManagerShutdownEvent INSTANCE = new ComponentManagerShutdownEvent();

    private ComponentManagerShutdownEvent() {}
}
