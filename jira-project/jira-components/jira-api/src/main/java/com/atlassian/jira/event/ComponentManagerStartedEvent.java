package com.atlassian.jira.event;

/**
 * Raised when the ComponentManager has started
 *
 * @since v5.2
 */
public class ComponentManagerStartedEvent
{
     public static final ComponentManagerStartedEvent INSTANCE = new ComponentManagerStartedEvent();

     private ComponentManagerStartedEvent() {}
}
