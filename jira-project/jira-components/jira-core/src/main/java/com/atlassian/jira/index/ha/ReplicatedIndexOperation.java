package com.atlassian.jira.index.ha;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity;

import com.google.common.collect.Maps;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This class represents an Indexing Operation on a node
 *
 * @since v6.1
 */
public class ReplicatedIndexOperation
{
    public enum Operation
    {
        UPDATE(), CREATE(), DELETE(), FULL_REINDEX_START(), FULL_REINDEX_END(true), BACKGROUND_REINDEX_START(), BACKGROUND_REINDEX_END(true), PROJECT_REINDEX();

        private final boolean reindexEnd;

        private Operation()
        {
            this(false);
        }

        private Operation(final boolean reindexEnd)
        {
            this.reindexEnd = reindexEnd;
        }

        public boolean isReindexEnd()
        {
            return reindexEnd;
        }
    }

    public enum AffectedIndex
    {
        ISSUE, COMMENT, CHANGEHISTORY, SHAREDENTITY, ALL
    }

    public enum SharedEntityType
    {
        SEARCH_REQUEST(SearchRequest.ENTITY_TYPE), PORTAL_PAGE(PortalPage.ENTITY_TYPE), NONE(null);

        private SharedEntity.TypeDescriptor typeDescriptor;

        private static Map<SharedEntity.TypeDescriptor, SharedEntityType> typeDescriptorSharedEntityTypeMap;

        private SharedEntityType(SharedEntity.TypeDescriptor typeDescriptor)
        {
            this.typeDescriptor = typeDescriptor;
        }

        public SharedEntity.TypeDescriptor getTypeDescriptor()
        {
            return typeDescriptor;
        }

        public static SharedEntityType  fromTypeDescriptor(SharedEntity.TypeDescriptor typeDescriptor)
        {
            if (typeDescriptorSharedEntityTypeMap == null)
            {
                initialiseTypeDescriptorMap();
            }
            if (typeDescriptor == null)
            {
                return SharedEntityType.NONE;
            }
            else
            {
                return typeDescriptorSharedEntityTypeMap.get(typeDescriptor);
            }
        }

        private static void initialiseTypeDescriptorMap()
        {
            typeDescriptorSharedEntityTypeMap = Maps.newHashMap();
            for (SharedEntityType type : values())
            {
                if (type.getTypeDescriptor() != null)
                {
                    typeDescriptorSharedEntityTypeMap.put(type.getTypeDescriptor(), type);
                }
            }
        }
    }

    public static final String ENTITY = "ReplicatedIndexOperation";
    public static final String ID = "id";
    public static final String INDEX_TIME = "indexTime";
    public static final String OPERATION = "operation";
    public static final String AFFECTED_INDEX="affectedIndex";
    public static final String ENTITY_TYPE = "entityType";
    public static final String NODE_ID = "nodeId";
    public static final String AFFECTED_IDS = "affectedIds";
    public static final String BACKUP_FILENAME = "filename";

    private final Timestamp indexTime;
    private final long id;
    private final String nodeId;
    private final Set<Long> affectedIds;
    private final Operation operation;
    private final AffectedIndex affectedIndex;
    private final SharedEntityType entityType;
    private final String backupFilename;

    ReplicatedIndexOperation(final long id, @Nonnull final String nodeId, @Nonnull final Timestamp indexTime,
            @Nonnull final AffectedIndex affectedIndex, @Nonnull final SharedEntityType entityType, @Nonnull final Operation operation,
            @Nonnull final Set<Long> affectedIds, final String backupFilename)
    {
        this.indexTime = notNull("indexTime", indexTime);
        this.id = notNull("id", id);
        this.nodeId = notNull("nodeId", nodeId);
        this.affectedIndex = notNull("affectedIndex", affectedIndex);
        this.affectedIds = notNull("affectedIds", affectedIds);
        this.operation = notNull("operation", operation);
        this.entityType = notNull("entityType", entityType);
        this.backupFilename = backupFilename == null ? "" : backupFilename;
    }

    public Date getIndexTime()
    {
        return indexTime;
    }

    public long getId()
    {
        return id;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public AffectedIndex getAffectedIndex()
    {
        return affectedIndex;
    }

    public SharedEntityType getEntityType()
    {
        return entityType;
    }

    public Operation getOperation()
    {
        return operation;
    }

    public Set<Long> getAffectedIds()
    {
        return affectedIds;
    }

    public String getBackupFilename()
    {
        return backupFilename;
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof ReplicatedIndexOperation && ((ReplicatedIndexOperation)o).id == id;
    }

    @Override
    public int hashCode()
    {
        return (int) (id ^ (id >>> 32));
    }
}
