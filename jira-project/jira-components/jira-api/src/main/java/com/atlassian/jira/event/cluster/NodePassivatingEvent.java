package com.atlassian.jira.event.cluster;

/**
 * Thrown when a clustered JIRA instance is becoming passive.
 *
 * @since v6.1
 */
public class NodePassivatingEvent
{
    public static final NodePassivatingEvent INSTANCE = new NodePassivatingEvent();

    private NodePassivatingEvent() {}
}
