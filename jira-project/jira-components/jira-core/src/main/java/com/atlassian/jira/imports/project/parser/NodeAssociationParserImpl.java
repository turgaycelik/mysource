package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class NodeAssociationParserImpl implements NodeAssociationParser
{
    public static final String SOURCE_NODE_ID = "sourceNodeId";
    public static final String SOURCE_NODE_ENTITY = "sourceNodeEntity";
    public static final String SINK_NODE_ID = "sinkNodeId";
    public static final String SINK_NODE_ENTITY = "sinkNodeEntity";
    public static final String ASSOCIATION_TYPE = "associationType";

    public ExternalNodeAssociation parse(final Map attributes) throws ParseException
    {
        Null.not("attributes", attributes);
        // <NodeAssociation sourceNodeId="10001" sourceNodeEntity="Issue" sinkNodeId="10001" sinkNodeEntity="Component" associationType="IssueComponent"/>
        final String sourceNodeId = (String) attributes.get(SOURCE_NODE_ID);
        final String sourceNodeEntity = (String) attributes.get(SOURCE_NODE_ENTITY);
        final String sinkNodeId = (String) attributes.get(SINK_NODE_ID);
        final String sinkNodeEntity = (String) attributes.get(SINK_NODE_ENTITY);
        final String associationType = (String) attributes.get(ASSOCIATION_TYPE);

        // Validate the data
        if (StringUtils.isEmpty(sourceNodeId))
        {
            throw new ParseException("No 'sourceNodeId' field for NodeAssocation.");
        }
        if (StringUtils.isEmpty(sourceNodeEntity))
        {
            throw new ParseException("No 'sourceNodeEntity' field for NodeAssocation.");
        }
        if (StringUtils.isEmpty(sinkNodeId))
        {
            throw new ParseException("No 'sinkNodeId' field for NodeAssocation.");
        }
        if (StringUtils.isEmpty(sinkNodeEntity))
        {
            throw new ParseException("No 'sinkNodeEntity' field for NodeAssocation.");
        }
        if (StringUtils.isEmpty(associationType))
        {
            throw new ParseException("No 'associationType' field for NodeAssocation.");
        }

        return new ExternalNodeAssociation(sourceNodeId, sourceNodeEntity, sinkNodeId, sinkNodeEntity, associationType);
    }

    public EntityRepresentation getEntityRepresentation(final ExternalNodeAssociation nodeAssociation)
    {
        final Map attributes = new HashMap();
        attributes.put(SOURCE_NODE_ID, nodeAssociation.getSourceNodeId());
        attributes.put(SOURCE_NODE_ENTITY, nodeAssociation.getSourceNodeEntity());
        attributes.put(SINK_NODE_ID, nodeAssociation.getSinkNodeId());
        attributes.put(SINK_NODE_ENTITY, nodeAssociation.getSinkNodeEntity());
        attributes.put(ASSOCIATION_TYPE, nodeAssociation.getAssociationType());
        return new EntityRepresentationImpl(NODE_ASSOCIATION_ENTITY_NAME, attributes);
    }
}
