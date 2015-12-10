package com.atlassian.jira.event.cluster;

/**
 * Thrown when a clustered JIRA instance has become active.
 *
 * @since v6.1
 */
public class NodeActivatedEvent
{
    public static final NodeActivatedEvent INSTANCE = new NodeActivatedEvent();

    private NodeActivatedEvent() {}
}
