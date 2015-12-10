package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.ApplicationUserEntity;
import com.atlassian.jira.user.util.UserKeyStore;
import com.atlassian.jira.user.util.UserKeyStoreImpl;
import com.atlassian.jira.util.Visitor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import static com.atlassian.jira.entity.ApplicationUserEntityFactory.LOWER_USER_NAME;
import static com.atlassian.jira.entity.ApplicationUserEntityFactory.USER_KEY;
import static com.atlassian.jira.entity.Entity.APPLICATION_USER;

/**
 * Ensures that every user that is used as a reporter or assignee has a userkey mapping
 * so that they can be indexed and searched for properly.
 *
 * @since v6.0.6 (as 6104)
 */
public class UpgradeTask_Build6137 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build6137.class);

    final Set<String> unmappedKeys = new HashSet<String>();
    final Set<String> mappedKeys = new HashSet<String>();
    final Set<String> mappedNames = new HashSet<String>();

    final UnmappedKeyCollector unmappedKeyCollector = new UnmappedKeyCollector();

    private final EntityEngine entityEngine;
    private final UserKeyStoreImpl userKeyStore;
    private final boolean debug;

    public UpgradeTask_Build6137(EntityEngine entityEngine, UserKeyStore userKeyStore)
    {
        super(false);
        this.entityEngine = entityEngine;
        this.userKeyStore = (UserKeyStoreImpl)userKeyStore;
        debug = log.isDebugEnabled();
    }

    @Override
    public String getBuildNumber()
    {
        return "6137";
    }

    @Override
    public String getShortDescription()
    {
        return "Ensure that every assignee and reporter has a userkey mapping";
    }

    @Override
    public void doUpgrade(final boolean setupMode) throws Exception
    {
        findUnmappedUserKeys();

        mappedKeys.clear();  // No longer needed

        log.info("Found " + unmappedKeys.size() + " unmapped user" + ((unmappedKeys.size() == 1) ? "" : "s"));

        // We can only hope...
        if (unmappedKeys.isEmpty())
        {
            return;
        }

        for (final String userKey : unmappedKeys)
        {
            final String lowerUserName = findAvailableUserName(userKey);
            if (debug)
            {
                log.debug("Creating mapping for deleted user: userKey=[" + userKey + "] -> lowerUserName=[" + lowerUserName + ']');
            }
            try
            {
                entityEngine.createValue(APPLICATION_USER, new ApplicationUserEntity(null, userKey, lowerUserName));
            }
            catch (DataAccessException dae)
            {
                // The only consequence of this is that the user is unsearchable.  Not worth catastrophic failure
                // if we find something funny in the data.
                log.error("Unable to creating mapping for deleted user: userKey=[" + userKey + "] -> lowerUserName=[" + lowerUserName + "]: " + dae);
            }
        }

        // Force userKeyStore to refresh
        userKeyStore.onClearCache(null);
    }

    private void findUnmappedUserKeys()
    {
        loadExistingApplicationUsers();

        findUnmappedKeysInField("Issue", "assignee");
        findUnmappedKeysInField("Issue", "reporter");
    }

    /**
     * Most of the time we would expect the username to be available, but it is possible that
     * a renamed user has stolen it, in which we need to use some other username instead.
     *
     * @param userKey the userkey that needs to get a username
     * @return the username selected as a match, which will be the same as {@code userKey} if
     *      possible, or that value suffixed with the first unuse
     */
    private String findAvailableUserName(final String userKey)
    {
        // Most of the time the key will be the same as the old username...
        if (mappedNames.add(userKey))
        {
            return userKey;
        }

        // Same logic used by DefaultUserManager.handleDeletedUserEviction.
        int count = 1;
        String lowerUserKey = IdentifierUtils.toLowerCase(userKey);  // Paranoid
        String lowerUserName = lowerUserKey + "#1";

        while (!mappedNames.add(lowerUserName))
        {
            if (count == Integer.MAX_VALUE)
            {
                // Realistically, this should never happen...
                throw new IllegalStateException("Deleted user eviction namespace exhausted");
            }
            lowerUserName = lowerUserKey + '#' + (++count);
        }

        return lowerUserName;
    }

    private void loadExistingApplicationUsers()
    {
        Select.columns(USER_KEY, LOWER_USER_NAME)
                .from(APPLICATION_USER.getEntityName())
                .runWith(entityEngine)
                .visitWith(new Visitor<GenericValue>()
        {
            @Override
            public void visit(final GenericValue genericValue)
            {
                mappedKeys.add(genericValue.getString(USER_KEY));
                mappedNames.add(genericValue.getString(LOWER_USER_NAME));
            }
        });
    }

    private void findUnmappedKeysInField(String entityName, String fieldName)
    {
        Select.distinctString(fieldName)
                .from(entityName)
                .runWith(entityEngine)
                .visitWith(unmappedKeyCollector);
    }

    class UnmappedKeyCollector implements Visitor<String>
    {
        @Override
        public void visit(@Nullable final String userKey)
        {
            if (userKey != null && userKey.length() > 0 && !mappedKeys.contains(userKey))
            {
                unmappedKeys.add(userKey);
            }
        }
    }
}
