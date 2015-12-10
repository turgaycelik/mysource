package com.atlassian.jira.upgrade.tasks;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.tasks.util.FindMixedCaseUsernames;

import org.ofbiz.core.entity.GenericValue;

/**
 * This upgrade task will ensure that change authors and assignee/reporter change values are lowercase.
 *
 * @since v6.0
 *
 */
public class UpgradeTask_Build6038 extends AbstractUpgradeTask
{
    private final EntityEngine entityEngine;
    private boolean needReindex = false;

    public UpgradeTask_Build6038(EntityEngine entityEngine)
    {
        super(true);
        this.entityEngine = entityEngine;
    }

    @Override
    public boolean isReindexRequired()
    {
        return needReindex;
    }

    @Override
    public String getBuildNumber()
    {
        return "6038";
    }

    @Override
    public String getShortDescription()
    {
        return String.format("Convert author to lowercase in %s, and assignee/reporter values to lowercase in %s",
                Entity.Name.CHANGE_GROUP,
                Entity.Name.CHANGE_ITEM);
    }

    @Override
    public void doUpgrade(boolean setupMode) throws SQLException
    {
        updateChangeGroupAuthors();
        updateChangeItemValues();
    }

    private void updateChangeGroupAuthors() throws SQLException
    {
        // Find distinct mixed case author names
        final SelectQuery<String> selectQuery = Select.distinctString("author").from(Entity.Name.CHANGE_GROUP);
        final Map<String,String> userNamesToConvert = entityEngine.run(selectQuery).consumeWith(FindMixedCaseUsernames.fromStrings());
        if (userNamesToConvert.isEmpty())
        {
            return;
        }

        needReindex = true;
        for (Map.Entry<String,String> entry : userNamesToConvert.entrySet())
        {
            entityEngine.execute( Update.into(Entity.Name.CHANGE_GROUP)
                    .set("author", entry.getValue())
                    .whereEqual("author", entry.getKey()) );
        }
   }

    private void updateChangeItemValues() throws SQLException
    {
        forceChangeItemsToLower("assignee", "oldvalue");
        forceChangeItemsToLower("assignee", "newvalue");
        forceChangeItemsToLower("reporter", "oldvalue");
        forceChangeItemsToLower("reporter", "newvalue");
    }

    private void forceChangeItemsToLower(String issueFieldName, String changeItemFieldName) throws SQLException
    {
        if (isDatabasePickyAboutClobs())
        {
            forceChangeItemsToLowerTheSlowWay(issueFieldName, changeItemFieldName);
            return;
        }

        final SelectQuery<String> fieldQuery = Select.distinctString(changeItemFieldName)
                .from(Entity.Name.CHANGE_ITEM)
                .whereEqual("field", issueFieldName);
        final Map<String,String> usernameMap = entityEngine.run(fieldQuery)
                .consumeWith(FindMixedCaseUsernames.fromStrings());
        if (usernameMap.isEmpty())
        {
            return;
        }

        needReindex = true;
        for (Map.Entry<String,String> entry : usernameMap.entrySet())
        {
            entityEngine.execute( Update.into(Entity.Name.CHANGE_ITEM)
                    .set(changeItemFieldName, entry.getValue())
                    .whereEqual("field", issueFieldName)
                    .andEqual(changeItemFieldName, entry.getKey()) );
        }
    }

    private void forceChangeItemsToLowerTheSlowWay(String issueFieldName, String changeItemFieldName)
    {
        final SelectQuery<GenericValue> fieldQuery = Select.columns("id", changeItemFieldName)
                .from(Entity.Name.CHANGE_ITEM)
                .whereEqual("field", issueFieldName);
        final Map<String,List<Long>> usernameMap = entityEngine.run(fieldQuery)
                .consumeWith(FindMixedCaseUsernames.fromColumnAndReturnIds(changeItemFieldName));
        if (usernameMap.isEmpty())
        {
            return;
        }

        needReindex = true;
        for (Map.Entry<String,List<Long>> entry : usernameMap.entrySet())
        {
            final String lowerUsername = entry.getKey();
            for (Long id : entry.getValue())
            {
                entityEngine.execute( Update.into(Entity.Name.CHANGE_ITEM)
                        .set(changeItemFieldName, lowerUsername)
                        .whereEqual("field", issueFieldName)
                        .andEqual("id", id) );
            }
        }
    }

    // Blacklisting databases that don't allow you to use DISTINCT or WHERE with CLOB values.
    // They will have to go by ID instead. :(
    boolean isDatabasePickyAboutClobs() throws SQLException
    {
        return isORACLE() || isMSSQL();
    }
}
