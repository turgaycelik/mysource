package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.tasks.util.FindMixedCaseUsernames;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * JRA-16974: usernames in RememberMeToken should be lowercase to match userkeys. Update the storage.
 *
 * @since v6.0
 */
public class UpgradeTask_Build6044 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build6044.class);

    static final String REMEMBER_ME_ENTITY = "RememberMeToken";
    static final String USERNAME = "username";

    private final EntityEngine entityEngine;

    public UpgradeTask_Build6044(EntityEngine entityEngine)
    {
        super(false);
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "6044";
    }

    @Override
    public String getShortDescription()
    {
        return "Convert usernames to lowercase in RememberMeToken table.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final SelectQuery<String> fieldQuery = Select.distinctString(USERNAME).from(REMEMBER_ME_ENTITY);
        final Map<String,String> usernameMap = entityEngine.run(fieldQuery)
                .consumeWith(FindMixedCaseUsernames.fromStrings());
        if (usernameMap.isEmpty())
        {
            return;
        }

        LOG.info(String.format("Analysing %d RememberMeTokens...", usernameMap.size()));
        for (Map.Entry<String,String> entry : usernameMap.entrySet())
        {
            entityEngine.execute(Update.into(REMEMBER_ME_ENTITY)
                    .set(USERNAME, entry.getValue())
                    .whereEqual(USERNAME, entry.getKey()));
        }
    }
}
