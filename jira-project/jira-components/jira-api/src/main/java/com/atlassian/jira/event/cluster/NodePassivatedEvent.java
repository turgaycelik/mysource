package com.atlassian.jira.event.cluster;

/**
 * Thrown when a clustered JIRA instance has become passive.
 *
 * @since v6.1
 */
public class NodePassivatedEvent
{
    public static final NodePassivatedEvent INSTANCE = new NodePassivatedEvent();

    private NodePassivatedEvent() {}
}
