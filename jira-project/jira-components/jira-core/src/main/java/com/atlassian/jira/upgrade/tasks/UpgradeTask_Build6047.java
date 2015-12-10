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
 * JRA-17011: usernames in ServiceProvider OAuth tokens should be lowercase to match userkeys. Update the storage.
 *
 * @since v6.0
 */
public class UpgradeTask_Build6047 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build6047.class);

    static final String OAUTH_SP_TOKEN_ENTITY = "OAuthServiceProviderToken";
    static final String USERNAME = "username";

    private final EntityEngine entityEngine;

    public UpgradeTask_Build6047(EntityEngine entityEngine)
    {
        super(false);
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "6047";
    }

    @Override
    public String getShortDescription()
    {
        return "Convert usernames to lowercase in oauthsptoken table.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        final SelectQuery<String> fieldQuery = Select.distinctString(USERNAME).from(OAUTH_SP_TOKEN_ENTITY);
        final Map<String,String> usernameMap = entityEngine.run(fieldQuery)
                .consumeWith(FindMixedCaseUsernames.fromStrings());
        if (usernameMap.isEmpty())
        {
            return;
        }

        LOG.info(String.format("Analysing %d OAuthServiceProviderTokens...", usernameMap.size()));
        for (Map.Entry<String,String> entry : usernameMap.entrySet())
        {
            entityEngine.execute(Update.into(OAUTH_SP_TOKEN_ENTITY)
                    .set(USERNAME, entry.getValue())
                    .whereEqual(USERNAME, entry.getKey()));
        }
    }
}
