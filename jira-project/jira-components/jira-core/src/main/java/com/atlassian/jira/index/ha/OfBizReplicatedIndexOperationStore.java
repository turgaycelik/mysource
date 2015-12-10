package com.atlassian.jira.index.ha;

import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityExprList;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.index.ha.ReplicatedIndexOperation.ENTITY;
import static org.ofbiz.core.entity.EntityOperator.AND;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.GREATER_THAN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN;
import static org.ofbiz.core.entity.EntityOperator.NOT_EQUAL;

/**
 * Stores index operation events
 *
 * @since v6.1
 */
public class OfBizReplicatedIndexOperationStore
{
    private static final String MAX_VIEW = "IndexOperationMaxIdForNodeId";
    private static final String MAX_ID = "max";

    private final OfBizDelegator ofBizDelegator;
    private final ComponentReference<ClusterManager> clusterManagerRef = ComponentAccessor.getComponentReference(ClusterManager.class);
    private final ReplicatedIndexOperationFactory operationFactory;

    public OfBizReplicatedIndexOperationStore(final OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.operationFactory = new ReplicatedIndexOperationFactory();
    }

    /**
     * Creates and stores the index operation in the underlying database, only if the node is in a cluster.
     *
     * @param indexTime  time of indexing operation
     * @param operation  the {@link ReplicatedIndexOperation.Operation} that was performed on the underlying index
     * @param affectedIds   Set of issueids that participated in the index opertaion
     * @return  the operation that was inserted into the underlying db - may be null if the node is not clustered
     */
    @Nullable
    public ReplicatedIndexOperation createIndexOperation(@Nonnull final Timestamp indexTime,
                                                         @Nonnull final ReplicatedIndexOperation.AffectedIndex affectedIndex,
                                                         @Nonnull final ReplicatedIndexOperation.SharedEntityType entityType,
                                                         @Nonnull final ReplicatedIndexOperation.Operation operation,
                                                         @Nonnull final Set<Long> affectedIds,
                                                         @Nonnull final String backupFilename)
    {
        final String nodeId = getClusterManager().getNodeId();
        if (nodeId != null && (affectedIds.size()>0 || affectedIndex.equals(ReplicatedIndexOperation.AffectedIndex.ALL)))
        {
            Map<String, Object> indexOperationFields = getIndexOperationFields(indexTime, affectedIndex,
                                    entityType, operation, nodeId, affectedIds, backupFilename);
            return operationFactory.build(ofBizDelegator.createValue(ReplicatedIndexOperation.ENTITY, indexOperationFields));
        }
        return null;
    }

    /**
     * Returns a set of IndexOperations that have happened since the time specified.
     *
     * @param sinceTime
     * @return  a set of {@link ReplicatedIndexOperation} that have taken place on other nodes after a specified time
     */
    public Set<ReplicatedIndexOperation> getIndexOperationsAfter(final Date sinceTime)
    {
        final Set<ReplicatedIndexOperation> replicatedIndexOperations = Sets.newHashSet();
        final String nodeId = getClusterManager().getNodeId();
        final ImmutableList<EntityCondition> entityConditions = ImmutableList.<EntityCondition>of(
                new EntityExpr(ReplicatedIndexOperation.NODE_ID, NOT_EQUAL, nodeId),
                new EntityExpr(ReplicatedIndexOperation.INDEX_TIME, GREATER_THAN, sinceTime));

        final List<GenericValue> gvs =  ofBizDelegator.findByAnd(ReplicatedIndexOperation.ENTITY, entityConditions);
        for (GenericValue gv : gvs)
        {
            replicatedIndexOperations.add(operationFactory.build(gv));
        }
        return replicatedIndexOperations;
    }

    public int purgeOldOperations(final String sourceNodeId, final Date before)
    {
        // Get the very last operation for this node. We don't want to purge this otherwise we forget anything ever happened
        final Long latestOperationId = getLatestOperation(sourceNodeId);
        if (latestOperationId != null)
        {
            final ImmutableList<EntityExpr> entityConditions = ImmutableList.of(
                    new EntityExpr(ReplicatedIndexOperation.NODE_ID, EQUALS, sourceNodeId),
                    new EntityExpr(ReplicatedIndexOperation.ID, LESS_THAN, latestOperationId),
                    new EntityExpr(ReplicatedIndexOperation.INDEX_TIME, LESS_THAN, new Timestamp(before.getTime())));
            return ofBizDelegator.removeByCondition(ReplicatedIndexOperation.ENTITY, new EntityExprList(entityConditions, AND));
        }
        return 0;
    }

    /**
     * Returns a set of IndexOperations that have happened since the time specified.
     * We do this by node as the ids are not monotonically increasing across nodes.
     *
     * @param sourceNodeId Node to get operations sent from
     * @param id Id of preceeding operation
     * @return a set of {@link ReplicatedIndexOperation} that have taken place on other nodes after a specified id
     */
    public Set<ReplicatedIndexOperation> getIndexOperationsAfter(String sourceNodeId, final Long id)
    {
        final Set<ReplicatedIndexOperation> replicatedIndexOperations = Sets.newHashSet();
        final ImmutableList<EntityCondition> entityConditions = ImmutableList.<EntityCondition>of(
                new EntityExpr(ReplicatedIndexOperation.NODE_ID, EQUALS, sourceNodeId),
                new EntityExpr(ReplicatedIndexOperation.ID, GREATER_THAN, id));

        final List<GenericValue> gvs =  ofBizDelegator.findByAnd(ReplicatedIndexOperation.ENTITY, entityConditions);
        for (GenericValue gv : gvs)
        {
            replicatedIndexOperations.add(operationFactory.build(gv));
        }
        return replicatedIndexOperations;
    }

    /**
     * get an operation by id
     *
     * @return the operation.
     */
    public ReplicatedIndexOperation getOperation(long id)
    {
        final GenericValue gv =  ofBizDelegator.findById(ENTITY, id);
        return gv == null ? null : operationFactory.build(gv);
    }

    /**
     * Returns The latest operation for this node
     *
     * @param sourceNodeId source node of the operation
     * @return id of the latest operation.
     */
    public Long getLatestOperation(final String sourceNodeId)
    {
        final List<GenericValue> gvs =  ofBizDelegator.findByAnd(MAX_VIEW, FieldMap.build(ReplicatedIndexOperation.NODE_ID, sourceNodeId));
        if (gvs.isEmpty())
        {
            return null;
        }
        else
        {
            return gvs.get(0).getLong(MAX_ID);
        }
    }

    /**
     *
     * @param id the NodeIndexOperation id to check
     * @return  true if the database contains this index operation
     */
    public boolean contains(long id)
    {
        GenericValue gv =  ofBizDelegator.findByPrimaryKey(ReplicatedIndexOperation.ENTITY, id);
        return gv != null;
    }

    private ClusterManager getClusterManager()
    {
        return clusterManagerRef.get();
    }

    private static String serialize(final Set<Long> ids)
    {
        return StringUtils.join(ids, ',');
    }


    private static Map<String, Object> getIndexOperationFields(final Timestamp indexTime,
                                        final ReplicatedIndexOperation.AffectedIndex affectedIndex,
                                        final ReplicatedIndexOperation.SharedEntityType entityType,
                                        final ReplicatedIndexOperation.Operation operation,
                                        final String nodeId,
                                        final Set<Long> affectedIds,
                                        final String backupFilename)
    {
        return ImmutableMap.<String,Object>builder()
                .put(ReplicatedIndexOperation.INDEX_TIME,indexTime)
                .put(ReplicatedIndexOperation.OPERATION, operation.toString())
                .put(ReplicatedIndexOperation.NODE_ID, nodeId)
                .put(ReplicatedIndexOperation.AFFECTED_INDEX, affectedIndex.toString())
                .put(ReplicatedIndexOperation.ENTITY_TYPE, entityType.toString())
                .put(ReplicatedIndexOperation.AFFECTED_IDS, serialize(affectedIds))
                .put(ReplicatedIndexOperation.BACKUP_FILENAME, backupFilename)
                .build();
    }
}

