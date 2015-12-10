package com.atlassian.jira.association;

import com.atlassian.jira.exception.DataAccessException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Manages associations between different types of entities.
 *
 * @since v4.4
 * @see UserAssociationStore
 */
public interface NodeAssociationStore
{
    public GenericValue getAssociation(GenericValue source, GenericValue sink, String associationType)
            throws DataAccessException;

    /**
     * Operates on NodeAssociations - gets MANY sinks from ONE source
     *
     * @param source The source node to find all associated sink nodes for.
     * @param sinkEntityName The sink Entity type.
     * @param associationType the association type
     * @throws DataAccessException If there is a DB Exception.
     *
     * @return List of Sinks for the given Source
     */
    public List<GenericValue> getSinksFromSource(GenericValue source, String sinkEntityName, String associationType)
            throws DataAccessException;

    public List<GenericValue> getSinksFromSource(String sourceEntityName, Long sourceNodeId, String sinkEntityName, String associationType);

    /**
     * Operates on NodeAssociations - gets MANY sources from ONE sink
     *
     * @throws DataAccessException If there is a DB Exception.
     */
    public List<GenericValue> getSourcesFromSink(GenericValue sink, String sourceName, String associationType)
            throws DataAccessException;

    /**
     * Create an association between two entities, given a particular association type.
     * <p/>
     * If the association already exists - it will not be created.
     *
     * @param source the source
     * @param sink the sink
     * @param associationType the Association Type
     *
     * @return The new association, or the existing association if it already existed.
     *
     * @throws DataAccessException If there is a DB Exception.
     */
    public GenericValue createAssociation(GenericValue source, GenericValue sink, String associationType)
            throws DataAccessException;

    /**
     * Create an association between two entities, given a particular association type.
     * <p/>
     * If the association already exists - it will not be created.
     * <p/>
     */
    public void createAssociation(NodeAssocationType type, Long sourceNodeId, Long sinkNodeId);

    /**
     * Removes the given association if it exists.
     *
     * @param type NodeAssocation Type
     * @param sourceNodeId The Source Node
     * @param sinkNodeId The Sink Node (destination node).
     */
    public void removeAssociation(NodeAssocationType type, Long sourceNodeId, Long sinkNodeId);

    /**
     * Create an association between two entities, given a particular association type.
     * <p/>
     * If the association already exists - it will not be created.
     * <p/>
     * NOTE: this is a convenience method that should only be used when you are certain of the related entity id's. This
     * method does not verify the integrity of the links it creates.
     *
     * @return The new association, or the existing association if it already existed.
     *
     * @throws DataAccessException If there is a DB Exception.
     */
    public GenericValue createAssociation(String sourceNodeEntity, Long sourceNodeId, String sinkNodeEntity, Long sinkNodeId, String associationType)
            throws DataAccessException;

    public void removeAssociation(GenericValue source, GenericValue sink, String associationType)
            throws DataAccessException;

    /**
     * Remove all entity<->entity associations, given the source.
     *
     * @param source the Source
     *
     * @throws DataAccessException If there is a DB Exception.
     */
    public void removeAssociationsFromSource(GenericValue source) throws DataAccessException;

    /**
     * Remove associations of the given type from the given source.
     *
     * @param nodeAssocationType the NodeAssocationType
     * @param sourceId the ID of the source
     */
    public void removeAssociationsFromSource(NodeAssocationType nodeAssocationType, Long sourceId);

    /**
     * Remove all entity<->entity associations, given the sink.
     *
     * @param sink the sink
     *
     * @throws DataAccessException If there is a DB Exception.
     */
    public void removeAssociationsFromSink(GenericValue sink) throws DataAccessException;

    /**
     * Swap all associations of a particular type from one sink to another.
     * <p/>
     * Used in ComponentDelete and VersionDelete.
     *
     * @param sourceEntityType the Source Entity Type
     * @param associationType the Association Type
     * @param oldSink the From sink
     * @param newSink the To sink
     *
     * @throws DataAccessException If there is a DB Exception.
     */
    public void swapAssociation(String sourceEntityType, String associationType, GenericValue oldSink, GenericValue newSink)
            throws DataAccessException;

    /**
     * Swaps all associations for a given list of entities (say move a list of unresolved issue entities to a new fix for version)
     *
     * @param entities the entities
     * @param associationType the Association Type
     * @param fromSink the From sink
     * @param toSink the To sink
     *
     * @throws DataAccessException If there is a DB Exception.
     */
    public void swapAssociation(List<GenericValue> entities, String associationType, GenericValue fromSink, GenericValue toSink)
            throws DataAccessException;

    public List<Long> getSinkIdsFromSource(GenericValue source, String sinkEntityName, String associationType);

    public List<Long> getSinkIdsFromSource(NodeAssocationType nodeAssocationType, Long sourceId);

    public List<Long> getSourceIdsFromSink(GenericValue sink, String sourceEntityName, String associationType);

    public List<Long> getSourceIdsFromSink(NodeAssocationType nodeAssocationType, Long sinkId);
}
