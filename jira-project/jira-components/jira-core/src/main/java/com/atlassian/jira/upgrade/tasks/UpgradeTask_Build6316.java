package com.atlassian.jira.upgrade.tasks;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.jdbc.DatabaseUtil;
import org.ofbiz.core.entity.model.ModelEntity;

/**
 * Drop broken unique index on AuditItem entity.
 */
public class UpgradeTask_Build6316 extends DropIndexTask
{
    public static final String AUDIT_ITEM_ENTITY = "AuditItem";

    public UpgradeTask_Build6316()
    {
        super(false);
    }

    @Override
    public String getShortDescription()
    {
        return "Drop unnecessary unique index on AuditItem entity";
    }

    @Override
    public String getBuildNumber()
    {
        return "6316";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final GenericDelegator delegator = getDelegator();
        final String helperName = delegator.getEntityHelper(AUDIT_ITEM_ENTITY).getHelperName();
        final ModelEntity entity = delegator.getModelEntity(AUDIT_ITEM_ENTITY);
        final String tableName = entity.getPlainTableName();
        final DatabaseUtil dbUtil = new DatabaseUtil(helperName);

        final List<String> messages = Lists.newArrayList();

        final Map<String, Set<String>> indexes = dbUtil.getIndexInfo(ImmutableSet.of(tableName), messages, true);

        final String oldIndexName = "idx_audit_item_log_id";

        if (indexes != null && indexes.get(tableName) != null)
        {
            for(String index : indexes.get(tableName))
            {
                if (oldIndexName.equalsIgnoreCase(index))
                {
                    dropIndex(tableName, oldIndexName);
                    break;
                }
            }
        }
    }
}
