package com.atlassian.jira.cluster;

import java.sql.Timestamp;

import javax.annotation.Nonnull;

/**
 * Represents a message sent from a node
 *
 * @since v6.1
 */
public class ClusterMessage
{
    @Nonnull private final Long id;
    @Nonnull private final String sourceNode;
    @Nonnull private final String destinationNode;
    private final String claimedByNode;
    @Nonnull private final Message message;
    @Nonnull private final Timestamp timestamp;

    public ClusterMessage(@Nonnull final Long id, @Nonnull final String sourceNode, @Nonnull final String destinationNode,
            final String claimedByNode, @Nonnull final Message message, final @Nonnull Timestamp timestamp)
    {
        this.id = id;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.claimedByNode = claimedByNode;
        this.message = message;
        this.timestamp = timestamp;
    }

    @Nonnull public Long getId()
    {
        return id;
    }

    @Nonnull public String getSourceNode()
    {
        return sourceNode;
    }

    @Nonnull public String getDestinationNode()
    {
        return destinationNode;
    }

    public String getClaimedByNode()
    {
        return claimedByNode;
    }

    @Nonnull public Message getMessage()
    {
        return message;
    }

    @Nonnull public Timestamp getTimestamp()
    {
        return timestamp;
    }
}
