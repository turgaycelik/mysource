package com.atlassian.jira.index.ha;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.ofbiz.FieldMap;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Factory for converting GVs to {@link ReplicatedIndexOperation}
 *
 * @since v6.1
 */
public class ReplicatedIndexOperationFactory extends AbstractEntityFactory<ReplicatedIndexOperation>
{

    @Override
    public Map<String, Object> fieldMapFrom(@Nonnull ReplicatedIndexOperation value)
    {

        return new FieldMap(ReplicatedIndexOperation.ID, value.getId())
                .add(ReplicatedIndexOperation.NODE_ID, value.getNodeId())
                .add(ReplicatedIndexOperation.INDEX_TIME, value.getIndexTime())
                .add(ReplicatedIndexOperation.AFFECTED_INDEX, value.getAffectedIndex().toString())
                .add(ReplicatedIndexOperation.ENTITY_TYPE, value.getEntityType().toString())
                .add(ReplicatedIndexOperation.OPERATION, value.getOperation().toString())
                .add(ReplicatedIndexOperation.AFFECTED_IDS, serialize(value.getAffectedIds()))
                .add(ReplicatedIndexOperation.BACKUP_FILENAME, value.getBackupFilename());
    }

    @Override
    public String getEntityName()
    {
        return "ReplicatedIndexOperation";
    }

    @Override
    public ReplicatedIndexOperation build(@Nonnull GenericValue gv)
    {
        return new ReplicatedIndexOperation(gv.getLong(ReplicatedIndexOperation.ID),
                gv.getString(ReplicatedIndexOperation.NODE_ID),
                gv.getTimestamp(ReplicatedIndexOperation.INDEX_TIME),
                ReplicatedIndexOperation.AffectedIndex.valueOf(gv.getString(ReplicatedIndexOperation.AFFECTED_INDEX)),
                ReplicatedIndexOperation.SharedEntityType.valueOf(gv.getString(ReplicatedIndexOperation.ENTITY_TYPE)),
                ReplicatedIndexOperation.Operation.valueOf(gv.getString(ReplicatedIndexOperation.OPERATION)),
                deserialize(gv.getString(ReplicatedIndexOperation.AFFECTED_IDS)),
                gv.getString(ReplicatedIndexOperation.BACKUP_FILENAME));
    }

    private Set<Long> deserialize(final String ids)
    {
        if (StringUtils.isBlank(ids))
            return Sets.newHashSet();

        final String[] affectedIdStrings = ids.split(",");

        final Set<Long> affectedIds = Sets.newHashSet();
        for (String id : affectedIdStrings)
        {
            affectedIds.add(Long.parseLong(id));
        }
        return affectedIds;
    }

    private String serialize(final Set<Long> ids)
    {
        return StringUtils.join(ids, ",");
    }

}
