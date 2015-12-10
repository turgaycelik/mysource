package com.atlassian.jira.cluster;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Responsible for storing and retrieving ClusterNode state in the underlying database
 *
 * @since v6.1
 */
public class OfBizClusterNodeStore
{
    private static final String ENTITY = "ClusterNode";

    static final String NODE_ID = "nodeId";
    static final String NODE_STATE = "nodeState";
    static final String TIMESTAMP = "timestamp";
    static final String IP = "ip";
    static final String CACHE_LISTENER_PORT = "cacheListenerPort";

    private final OfBizDelegator ofBizDelegator;

    public OfBizClusterNodeStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }


    /**
     * Creates the node in the underlying database.
     *
     * @param nodeId - the node id to create and store in the underlying table
     * @param state - the desired node state
     * @param cacheListenerPort the multicast port the server is going to use to listen for changes
     * @return the newly created node
     * @throws DataAccessException if a database error prevents the node from being created
     */
    public Node createNode(@Nonnull final String nodeId, @Nonnull final Node.NodeState state, @Nonnull String ip, @Nonnull Long cacheListenerPort)
    {
        final Node node = new Node(nodeId, state, System.currentTimeMillis(), ip, cacheListenerPort);
        return storeFieldMap(getFields(node), true);
    }


    /**
     * Stores the node in the underlying database.
     *
     * @param nodeId - the node id to store in the underlying table
     * @param state - the desired node state
     * @param cacheListenerPort the multicast port the server is going to use to listen for changes
     * @return the newly created node
     * @throws DataAccessException if a database error prevents the node from being updated
     */
    public Node updateNode(@Nonnull final String nodeId, @Nonnull final Node.NodeState state, @Nonnull String ip, @Nonnull Long cacheListenerPort)
    {
        final Node node = new Node(nodeId, state, System.currentTimeMillis(), ip, cacheListenerPort);
        return storeFieldMap(getFields(node), false);
    }

    /**
     * Deletes the node from the underlying database.
     *
     * @param node the node to delete
     * @return the number of rows deleted
     */
    public int deleteNode(Node node)
    {
        return ofBizDelegator.removeByCondition(ENTITY, new EntityExpr(NODE_ID, EntityOperator.EQUALS, node.getNodeId()));
    }

    @Nullable
    public Node getNode(String nodeId)
    {
        final GenericValue gv = ofBizDelegator.findByPrimaryKey(ENTITY, getPkFields(nodeId));
        return (gv != null) ? fromGv(gv) : null;
    }

    public List<Node> getAllNodes()
    {
        return findNodes(null, null);
    }

    public List<Node> findNodes(final EntityCondition condition, List<String> orderBy)
    {
        final ImmutableList.Builder<Node> list = ImmutableList.builder();
        final List<GenericValue> genericValues = ofBizDelegator.findByCondition(ENTITY, condition, null, orderBy);
        for (GenericValue genericValue : genericValues)
        {
            list.add(fromGv(genericValue));
        }
        return list.build();
    }


    private Node storeFieldMap(@Nonnull Map<String, Object> fields, boolean create)
    {
        final GenericValue gv = ofBizDelegator.makeValue(ENTITY, fields);
        try
        {
            if (create)
            {
                gv.create();
            }
            else
            {
                gv.store();
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Problem storing node", e);
        }
        return fromGv(gv);
    }

    static Node fromGv(@Nonnull final GenericValue gv)
    {
        final Long multicastPort = gv.getLong(CACHE_LISTENER_PORT);
        final String ip = gv.getString(IP);
        final Long timestamp = gv.getLong(TIMESTAMP);

        return new Node(gv.getString(NODE_ID),
                Node.NodeState.valueOf(gv.getString(NODE_STATE)),
                timestamp,
                ip,
                multicastPort);
    }

    static Map<String, Object> getFields(final Node node)
    {
        return new FieldMap(NODE_ID, node.getNodeId())
                .add(NODE_STATE, node.getState().toString())
                .add(TIMESTAMP, node.getTimestamp())
                .add(IP, node.getIp())
                .add(CACHE_LISTENER_PORT, node.getCacheListenerPort());
    }

    static Map<String, Object> getPkFields(final String nodeId)
    {
        return new FieldMap(NODE_ID, nodeId);
    }


}
