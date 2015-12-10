package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.ImmutableSet;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericHelper;
import org.ofbiz.core.entity.jdbc.DatabaseUtil;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelIndex;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Drop index issue_key
 *
 * @since v6.1
 */
public class UpgradeTask_Build6128 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6128.class);

    private static final String ENTITY_NAME = "Issue";
    private static final String INDEX_NAME = "issue_key";
    private static final String TABLE_NAME = "jiraissue";

    public UpgradeTask_Build6128()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "6128";
    }

    @Override
    public String getShortDescription()
    {
        return "Dropping index issue_key";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        if(isIndexPresent())
        {
            DelegatorInterface delegatorInterface = getOfBizDelegator().getDelegatorInterface();
            GenericHelper helper = delegatorInterface.getEntityHelper(ENTITY_NAME);

            DatabaseUtil dbUtil = new DatabaseUtil(helper.getHelperName());
            ModelEntity issueEntity = delegatorInterface.getModelEntity(ENTITY_NAME);

            ModelIndex issueKeyIndex = new ModelIndex();
            issueKeyIndex.setName(INDEX_NAME);

            final String error = dbUtil.deleteDeclaredIndex(issueEntity, issueKeyIndex);
            if ( error != null)
            {
                throw new RuntimeException(String.format("There was a problem dropping index issue_key: %s "
                        + "-- You should drop this index manually and restart JIRA instance.", error));
            }
        }
        else
        {
            log.info("Index issue_key not present. Skipping this task.");
        }
    }

    private boolean isIndexPresent() throws SQLException, GenericEntityException
    {
        final Connection connection = getDatabaseConnection();
        try
        {
            DelegatorInterface delegatorInterface = getOfBizDelegator().getDelegatorInterface();
            GenericHelper helper = delegatorInterface.getEntityHelper(ENTITY_NAME);

            DatabaseUtil dbUtil = new DatabaseUtil(helper.getHelperName());
            final Map<String,Set<String>> indexInfo = dbUtil.getIndexInfo(ImmutableSet.of(TABLE_NAME), new ArrayList<String>(), true);

            Set<String> indexes = new HashSet<String>();
            for (Set<String> i : indexInfo.values())
            {
                indexes.addAll(i);
            }
            return indexes.contains(INDEX_NAME.toUpperCase());
        }
        finally
        {
            connection.close();
        }
    }
}
