package com.atlassian.jira.external.beans;

/**
 * Used to hold information about issue version and component entries.
 *
 * @since v3.13
 */
public class ExternalNodeAssociation
{

    private final String sourceNodeId;
    private final String sourceNodeEntity;
    private final String sinkNodeId;
    private final String sinkNodeEntity;
    private final String associationType;

    public ExternalNodeAssociation(final String sourceNodeId, final String sourceNodeEntity, final String sinkNodeId, final String sinkNodeEntity, final String associationType)
    {
        this.sourceNodeId = sourceNodeId;
        this.sourceNodeEntity = sourceNodeEntity;
        this.sinkNodeId = sinkNodeId;
        this.sinkNodeEntity = sinkNodeEntity;
        this.associationType = associationType;
    }

    public String getSourceNodeId()
    {
        return sourceNodeId;
    }

    public String getSourceNodeEntity()
    {
        return sourceNodeEntity;
    }

    public String getSinkNodeId()
    {
        return sinkNodeId;
    }

    public String getSinkNodeEntity()
    {
        return sinkNodeEntity;
    }

    public String getAssociationType()
    {
        return associationType;
    }
}
