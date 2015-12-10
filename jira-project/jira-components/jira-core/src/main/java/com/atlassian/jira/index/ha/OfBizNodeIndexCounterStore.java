package com.atlassian.jira.index.ha;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.ofbiz.core.entity.EntityOperator.AND;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;

/**
 * Stores the last index operation id for each node
 *
 * @since v6.1
 */
public class OfBizNodeIndexCounterStore
{
    private static final Logger LOG = LoggerFactory.getLogger(OfBizNodeIndexCounterStore.class);

    public static final String ENTITY_NAME = "NodeIndexCounter";
    public static final String ID = "id";
    public static final String VIEW_NAME ="IndexOperationMinId";
    public static final String NODE_ID = "nodeId";
    public static final String SENDING_NODE_ID = "sendingNodeId";
    public static final String INDEX_OP_ID = "indexOperationId";
    public static final String MIN = "min";

    private final OfBizDelegator ofBizDelegator;


    public OfBizNodeIndexCounterStore(final OfBizDelegator ofBizDelegator, final ClusterManager clusterManager)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    /**
     * Record the highest replicated index operation processed by a node,
     * The highest operation is recorded for each node pair
     *
     * @param receivingNodeId Receiving node id
     * @param sendingNodeId Sending node Id
     * @param indexOperationId operation id
     */
    public void storeHighestIdForNode(final String receivingNodeId, final String sendingNodeId, final long indexOperationId)
    {
        final List<EntityCondition> entityConditions = ImmutableList.<EntityCondition>of(new EntityExpr(NODE_ID, EQUALS, receivingNodeId),
                new EntityExpr(SENDING_NODE_ID, EQUALS, sendingNodeId));

        List<GenericValue> gvs = ofBizDelegator.transform(ENTITY_NAME, new EntityConditionList(entityConditions, AND), null, ID, new Transformation()
        {
            @Override
            public void transform(final GenericValue entity)
            {
                if (entity.getLong(INDEX_OP_ID) == null || entity.getLong(INDEX_OP_ID) < indexOperationId)
                {
                    entity.set(INDEX_OP_ID, indexOperationId);
                }
            }
        });
        // If there was no row for the node pair, then we will add one.
        // Unique index on the node pair should protect us from race conditions here.
        if (gvs.isEmpty())
        {
            try
            {
                gvs.add(ofBizDelegator.createValue(ENTITY_NAME, ImmutableMap.<String,Object>of(NODE_ID, receivingNodeId, SENDING_NODE_ID, sendingNodeId, INDEX_OP_ID, indexOperationId)));
            }
            catch (DataAccessException e)
            {
                LOG.warn("Error adding new entry to " + ENTITY_NAME, e);
            }
        }
    }

    private long getStartingPoint()
    {
        List<GenericValue> gvs = ofBizDelegator.findByCondition(VIEW_NAME, null, ImmutableList.of(MIN));
        if (gvs.isEmpty() || gvs.get(0).getLong(MIN) == null)  // Min returns a null if there are no rows.
        {
            return 0;
        }
        else
        {
            return gvs.get(0).getLong(MIN)-1;
        }
    }

    /**
     * Get the highest recorded index operation for a node pair
     * @param receivingNodeId
     * @param sendingNodeId
     * @return
     */
    public long getIndexOperationCounterForNodeId(final String receivingNodeId, final String sendingNodeId)
    {
        final ImmutableList<EntityCondition> entityConditions = ImmutableList.<EntityCondition>of(new EntityExpr(NODE_ID, EQUALS, receivingNodeId),
                new EntityExpr(SENDING_NODE_ID, EQUALS, sendingNodeId));
        List<GenericValue> gvs = ofBizDelegator.findByAnd(ENTITY_NAME, entityConditions);
        if (gvs.isEmpty())
        {
            return getStartingPoint();
        }
        else
        {
            return gvs.get(0).getLong(INDEX_OP_ID);
        }
    }


}
