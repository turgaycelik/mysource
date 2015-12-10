package com.atlassian.jira.cluster;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;

/**
 * Responsible for storing and retrieving cluster messages in the underlying database.
 *
 * @since v6.1
 */
public class OfBizClusterMessageStore
{
    // Constants
    @VisibleForTesting protected static final String DESTINATION_NODE = "destinationNode";
    @VisibleForTesting protected static final String ENTITY = "ClusterMessage";
    @VisibleForTesting protected static final String ID = "id";
    @VisibleForTesting protected static final String MESSAGE = "message";
    @VisibleForTesting protected static final String SOURCE_NODE = "sourceNode";
    @VisibleForTesting protected static final String CLAIMED_BY_NODE = "claimedByNode";
    @VisibleForTesting protected static final String MESSAGE_TIME = "messageTime";

    // Fields
    private final OfBizDelegator ofBizDelegator;

    public OfBizClusterMessageStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    /**
     * Stores the message in the underlying database.
     *
     * @param message - the message to store in the underlying table
     */
    public GenericValue storeMessage(@Nonnull final ClusterMessage message)
    {
        return storeFieldMap(getFields(message));
    }

    /**
     * Creates a ClusterMessage and stores it in the underlying database
     *
     * @param sourceNode the ID of the source node (required)
     * @param destinationNode the ID of the destination node (required)
     * @param message the message to send (required)
     */
    public ClusterMessage createMessage(
            @Nonnull final String sourceNode, @Nonnull final String destinationNode, @Nonnull final String message)
    {
        Map<String, Object> fields = ImmutableMap.<String, Object>of(SOURCE_NODE, sourceNode,
                DESTINATION_NODE, destinationNode,
                MESSAGE, message,
                MESSAGE_TIME, new Timestamp(new Date().getTime()));

        GenericValue gv= storeFieldMap(fields);
        return fromGv(gv);
    }

    /**
     * Retrieves the messages destined for the given node, from a source node.  Returns messages
     * with either the specific node id, or destined for all, but they must
     * have been sent from another node - no listening to your own messages.
     * We process each source node separately because the message Ids are not monotonically
     * increasing across nodes.
     *
     *
     * @param sourceNode The sending node
     * @param destinationNode The listening node
     * @param afterMessageId Only messages with a higher Message Id than this id will be returned.
     * If starting beyond is null will return all messages for the source destinationNode pair.
     *
     * @return a non-null list
     */
    public List<ClusterMessage> getMessages(final Node sourceNode, final Node destinationNode, final Long afterMessageId)
    {
        if (!destinationNode.isClustered())
        {
            return ImmutableList.of();
        }
        final String destinationNodeId = destinationNode.getNodeId();
        final String sourceNodeId = sourceNode.getNodeId();
        EntityCondition findByNodeid = new EntityExpr(DESTINATION_NODE, EntityOperator.EQUALS, destinationNodeId);
        EntityCondition findByAllNodes = new EntityExpr(DESTINATION_NODE, EntityOperator.EQUALS, ClusterManager.ALL_NODES);
        EntityCondition findByAnyNode = new EntityExpr(DESTINATION_NODE, EntityOperator.EQUALS, ClusterManager.ANY_NODE);
        EntityCondition findBySourceNodeId = new EntityExpr(SOURCE_NODE, EntityOperator.EQUALS, sourceNodeId);
        EntityConditionList findByNodes = new EntityConditionList(Lists.newArrayList(findByNodeid, findByAnyNode, findByAllNodes), EntityOperator.OR);
        final List<EntityCondition> conditionList = Lists.newArrayList(findByNodes, findBySourceNodeId);
        if (afterMessageId != null)
        {
            conditionList.add(new EntityExpr(ID, EntityOperator.GREATER_THAN, afterMessageId));
        }
        EntityConditionList completeCondition = new EntityConditionList(conditionList, EntityOperator.AND);

        OfBizListIterator it = ofBizDelegator.findListIteratorByCondition(ENTITY, completeCondition, null, null, ImmutableList.of(ID), null);
        try
        {
            List<ClusterMessage> messages = new ArrayList<ClusterMessage>();
            for (GenericValue genericValue : it)
            {
                ClusterMessage message = fromGv(genericValue);

                // We need to claim any "any node" message that we receive
                if (ClusterManager.ANY_NODE.equals(message.getDestinationNode()))
                {
                    int updated = ofBizDelegator.bulkUpdateByAnd(ENTITY,
                            FieldMap.build(CLAIMED_BY_NODE, destinationNodeId),
                            FieldMap.build(ID, message.getId(), CLAIMED_BY_NODE, null));
                    if (updated == 0)
                    {
                        //We lost the race to claim this message, moving along;
                        continue;
                    }
                }
                messages.add(message);
            }
            return messages;
        }
        finally
        {
            if (it != null)
            {
                it.close();
            }
        }
    }


    public int deleteMessage(ClusterMessage clusterMessage)
    {
        return ofBizDelegator.removeById(ENTITY, clusterMessage.getId());
    }

    /**
     * Get the id of the latest message in the store for a node..
     * That is the message with the highest id or null if there are no messages.
     * @param nodeId the id of the node.
     * @return the id of the latest message in the store.
     */
    public Long getLatestMessageByNodeId(String nodeId)
    {
        OfBizListIterator iterator = ofBizDelegator.findListIteratorByCondition(ENTITY, new EntityExpr(SOURCE_NODE, EQUALS, nodeId), null,
                ImmutableList.of(ID), ImmutableList.of(ID + " DESC"), EntityFindOptions.findOptions().maxResults(1));
        try
        {
            for (GenericValue gV : iterator)
            {
                return gV.getLong(ID);
            }
            return null;
        }
        finally
        {
            if (iterator != null)
            {
                iterator.close();
            }
        }
    }

    private ClusterMessage fromGv(@Nonnull final GenericValue gv)
    {
        final Message message = Message.fromString(gv.getString(MESSAGE));
        return new ClusterMessage(gv.getLong(ID), gv.getString(SOURCE_NODE), gv.getString(DESTINATION_NODE),
                gv.getString(CLAIMED_BY_NODE), message, gv.getTimestamp(MESSAGE_TIME));
    }

    private GenericValue  storeFieldMap(@Nonnull Map<String, Object> fields)
    {
        return ofBizDelegator.createValue(ENTITY, fields);
    }

    private Map<String, Object> getFields(final ClusterMessage message)
    {
        return new FieldMap(ID, message.getId())
                .add(SOURCE_NODE, message.getSourceNode())
                .add(DESTINATION_NODE, message.getDestinationNode())
                .add(CLAIMED_BY_NODE, message.getClaimedByNode())
                .add(MESSAGE, message.getMessage())
                .add(MESSAGE_TIME, message.getTimestamp());
    }

    public int deleteMessagesBefore(final Date before)
    {
        EntityCondition deleteCondition = new EntityExpr(MESSAGE_TIME, LESS_THAN, new Timestamp(before.getTime()));
        return ofBizDelegator.removeByCondition(ENTITY, deleteCondition);
    }
}
