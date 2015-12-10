package com.atlassian.jira.association;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.IssueRelationConstants;

/**
 * Config for a particular Node Association.
 *
 * @since v4.4
 */
public class NodeAssocationType
{
    public static final NodeAssocationType ISSUE_TO_COMPONENT = new NodeAssocationType(IssueRelationConstants.COMPONENT, Entity.Name.ISSUE, Entity.Name.COMPONENT);
    public static final NodeAssocationType ISSUE_TO_AFFECTS_VERISON = new NodeAssocationType(IssueRelationConstants.VERSION, Entity.Name.ISSUE, Entity.Name.VERSION);
    public static final NodeAssocationType ISSUE_TO_FIX_VERISON = new NodeAssocationType(IssueRelationConstants.FIX_VERSION, Entity.Name.ISSUE, Entity.Name.VERSION);

    private final String name;
    private final String sourceEntityName;
    private final String sinkEntityName;

    public NodeAssocationType(String name, String sourceEntityName, String sinkEntityName)
    {
        this.name = name;
        this.sourceEntityName = sourceEntityName;
        this.sinkEntityName = sinkEntityName;
    }

    /**
     * Returns the identifying name of the Association Type.
     *
     * @return the identifying name of the Association Type.
     */
    String getName()
    {
        return name;
    }

    /**
     * Returns the entity name of the source entity.
     *
     * @return the entity name of the source entity.
     */
    String getSourceEntityName()
    {
        return sourceEntityName;
    }

    /**
     * Returns the entity name of the sink (destination) entity.
     *
     * @return the entity name of the sink entity.
     */
    String getSinkEntityName()
    {
        return sinkEntityName;
    }
}
