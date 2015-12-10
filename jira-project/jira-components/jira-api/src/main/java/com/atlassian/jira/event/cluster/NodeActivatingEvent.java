package com.atlassian.jira.event.cluster;

/**
 * Thrown when a clustered JIRA instance is becoming active.
 *
 * @since v6.1
 */
public class NodeActivatingEvent
{
    public static final NodeActivatingEvent INSTANCE = new NodeActivatingEvent();

    private NodeActivatingEvent() {}
}
