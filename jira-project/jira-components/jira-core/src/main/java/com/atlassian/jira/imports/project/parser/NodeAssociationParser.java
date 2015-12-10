package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.core.EntityRepresentation;

import java.util.Map;

/**
 * Converts issue version and component information from the backup XML to an object representation.
 *
 * @since v3.13
 */
public interface NodeAssociationParser
{
    static final String NODE_ASSOCIATION_ENTITY_NAME = "NodeAssociation";
    static final String COMPONENT_TYPE = "IssueComponent";
    static final String FIX_VERSION_TYPE = "IssueFixVersion";
    static final String AFFECTS_VERSION_TYPE = "IssueVersion";

    /**
     * Transforms a set of attributes into a NodeAssociation.
     *
     * @param attributes is a map of key value pairs that represent the attributes of a NodeAssocation. The following
     * attributes are required, otherwise a ParseException will be thrown:<br/>
     * <ul>
     * <li>sourceNodeId (required)</li>
     * <li>sourceNodeEntity (required)</li>
     * <li>sinkNodeId (required)</li>
     * <li>sinkNodeEntity (required)</li>
     * <li>associationType (required)</li>
     * </ul>
     * @return a ExternalNodeAssociation if the attributes contain the required attributes.
     *
     * @throws ParseException If the attributes are invalid.
     */
    ExternalNodeAssociation parse(final Map attributes) throws ParseException;

    /**
     * Gets an EntityRepresentation that contains the correct attributes based on the populated fields in the
     * provided node association.
     *
     * @param nodeAssociation contains the populated fields that will end up in the EntityRepresentations map
     * @return an EntityRepresentation that can be persisted using OfBiz
     */
    EntityRepresentation getEntityRepresentation(ExternalNodeAssociation nodeAssociation);
}
