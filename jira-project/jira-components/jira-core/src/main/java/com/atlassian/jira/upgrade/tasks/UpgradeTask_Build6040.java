package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.ApplicationUserEntity;
import com.atlassian.jira.user.util.UserKeyStore;
import com.atlassian.jira.user.util.UserKeyStoreImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.entity.Entity.APPLICATION_USER;

/**
 * Map existing usernames to userkeys for rename user.
 *
 * @since v6.0
 *
 */
public class UpgradeTask_Build6040 extends AbstractUpgradeTask
{
    private final EntityEngine entityEngine;

    public UpgradeTask_Build6040(EntityEngine entityEngine)
    {
        super(false);
        this.entityEngine = entityEngine;
    }

    @Override
    public String getBuildNumber()
    {
        return "6040";
    }

    @Override
    public String getShortDescription()
    {
        return "Map existing usernames to userkeys for rename user.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Find distinct mixed case author names
        final SelectQuery<String> selectQuery = Select.distinctString("lowerUserName").from(Entity.Name.USER);
        final List<String> lowerUsernames = entityEngine.run(selectQuery).asList();

        // Find usernames that are already mapped.
        // (For re-running the upgrade task, and also helps our func tests during development off master branch)
        final List<String> mappedUsernameList = Select.stringColumn(APPLICATION_USER.LOWER_USER_NAME).from(APPLICATION_USER).runWith(entityEngine).asList();
        final Set<String> mappedUsernames = new HashSet<String>(mappedUsernameList);

        // Map usernames that are not already in our ApplicationUser table
        for (String lowerUsername : lowerUsernames)
        {
            if (!mappedUsernames.contains(lowerUsername))
                mapUsername(lowerUsername);
        }
        // Force a rebuild of the key-username cache
        UserKeyStoreImpl userKeyStore = (UserKeyStoreImpl) ComponentAccessor.getComponent(UserKeyStore.class);
        userKeyStore.onClearCache(null);
   }

    private void mapUsername(String lowerUsername)
    {
        entityEngine.createValue(APPLICATION_USER, new ApplicationUserEntity(null, lowerUsername, lowerUsername));
    }
}
