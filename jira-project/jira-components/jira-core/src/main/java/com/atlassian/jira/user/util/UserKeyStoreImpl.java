package com.atlassian.jira.user.util;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityEngine;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUserEntity;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.annotations.VisibleForTesting;

import org.ofbiz.core.entity.DelegatorInterface;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

public class UserKeyStoreImpl implements UserKeyStore
{
    private static final String USER_KEY = "userKey";
    private static final String LOWER_USER_NAME = "lowerUserName";

    private final EntityEngine entityEngine;
    private final OfBizDelegator ofBizDelegator;
    private final DelegatorInterface delegatorInterface;

    private final Cache<LazyCacheKey, CacheObject<String>> keyToUsernameCache;
    private final Cache<LazyCacheKey, CacheObject<String>> usernameToKeyCache;
    private final Cache<String, CacheObject<Long>> userkeyToIdCache;

    public UserKeyStoreImpl(EntityEngine entityEngine, OfBizDelegator ofBizDelegator, DelegatorInterface delegatorInterface, EventPublisher eventPublisher, CacheManager cacheManager)
    {
        this.entityEngine = entityEngine;
        this.ofBizDelegator = ofBizDelegator;
        this.delegatorInterface = delegatorInterface;

        keyToUsernameCache = cacheManager.getCache(UserKeyStoreImpl.class.getName() + ".keyToUsernameCache", new KeyToNameCacheLoader());
        usernameToKeyCache = cacheManager.getCache(UserKeyStoreImpl.class.getName() + ".usernameToKeyCache", new NameToKeyCacheLoader());
        userkeyToIdCache = cacheManager.getCache(UserKeyStoreImpl.class.getName() + ".userkeyToIdCache", new KeyToIdCacheLoader());

        buildCache();
        eventPublisher.register(this);
    }

    @Override
    public String getUsernameForKey(final String key)
    {
        if (key == null)
        {
            return null;
        }
        return keyToUsernameCache.get(new LazyCacheKey(key)).getValue();
    }

    @Override
    public String getKeyForUsername(String username)
    {
        if (username == null)
        {
            return null;
        }
        username = IdentifierUtils.toLowerCase(username);
        // Try to get a mapping from the cache
        return usernameToKeyCache.get(new LazyCacheKey(username)).getValue();
    }

    @Override
    public Long getIdForUserKey(String userkey)
    {
        if (userkey == null)
        {
            return null;
        }
        // Try to get a mapping from the cache
        return userkeyToIdCache.get(userkey).getValue();
    }

    @Override
    public void renameUser(String oldUsername, String newUsername)
    {
        // Lower-case the usernames
        oldUsername = IdentifierUtils.toLowerCase(oldUsername);
        newUsername = IdentifierUtils.toLowerCase(newUsername);
        // Find the key
        String key = Select.stringColumn(USER_KEY).from(Entity.APPLICATION_USER).whereEqual(LOWER_USER_NAME, oldUsername).runWith(ofBizDelegator).singleValue();
        if (key == null)
        {
            throw new IllegalStateException("Trying to rename user '" + oldUsername + "' but no user key is mapped.");
        }

        entityEngine.execute(Update.into(Entity.APPLICATION_USER).set(LOWER_USER_NAME, newUsername).whereEqual(USER_KEY, key));
        usernameToKeyCache.remove(new LazyCacheKey(oldUsername));
        usernameToKeyCache.remove(new LazyCacheKey(newUsername));
        keyToUsernameCache.remove(new LazyCacheKey(key));
        userkeyToIdCache.remove(key);
    }

    @Override
    public String ensureUniqueKeyForNewUser(String username)
    {
        String lowerUsername = IdentifierUtils.toLowerCase(notNull("username", username));
        // First check if we already have a mapping for this username
        ApplicationUserEntity applicationUserEntity = Select.from(Entity.APPLICATION_USER)
                .whereEqual(LOWER_USER_NAME, lowerUsername)
                .runWith(entityEngine).singleValue();
        if (applicationUserEntity != null)
        {
            // a mapping already exists for this username - nothing to do
            return applicationUserEntity.getKey();
        }

        // By default we use the lower-case username as the key: is this available?
        applicationUserEntity = Select.from(Entity.APPLICATION_USER)
                .whereEqual(USER_KEY, lowerUsername)
                .runWith(entityEngine).singleValue();
        if (applicationUserEntity == null)
        {
            // Add an explicit mapping using the lowerusername as the key
            final FieldMap fieldValues = FieldMap.build(USER_KEY, lowerUsername).add(LOWER_USER_NAME, lowerUsername);
            ofBizDelegator.createValue(Entity.APPLICATION_USER.getEntityName(), fieldValues);
            // Update the caches
            usernameToKeyCache.remove(new LazyCacheKey(lowerUsername));
            keyToUsernameCache.remove(new LazyCacheKey(lowerUsername));
            userkeyToIdCache.remove(lowerUsername);
            return lowerUsername;
        }

        // We are creating a new user that is recycling a username that was previously used and so the default user key
        // is taken. We need to create a special unique userkey for this user.

        // First find the next ID for this database table
        final Long id = delegatorInterface.getNextSeqId(Entity.APPLICATION_USER.getEntityName());
        // now we use that id to create a guaranteed unique userkey eg ID10012
        final String userkey = "ID" + id;
        final FieldMap fieldValues = FieldMap.build("id", id).add(USER_KEY, userkey).add(LOWER_USER_NAME, lowerUsername);
        ofBizDelegator.createValue(Entity.APPLICATION_USER.getEntityName(), fieldValues);
        // Update the caches
        usernameToKeyCache.remove(new LazyCacheKey(lowerUsername));
        keyToUsernameCache.remove(new LazyCacheKey(userkey));
        userkeyToIdCache.remove(userkey);
        return userkey;
    }

    @Override
    public String removeByKey(final String key)
    {
        if (key == null)
        {
            return null;
        }

        try
        {
            ApplicationUserEntity user = Select.from(Entity.APPLICATION_USER).whereEqual(USER_KEY, key).runWith(entityEngine).singleValue();
            if (user != null)
            {
                Delete.from(Entity.APPLICATION_USER).whereEqual(USER_KEY, key).execute(entityEngine);
                usernameToKeyCache.remove(new LazyCacheKey(user.getUsername()));
                return user.getUsername();
            }
            return null;
        }
        finally
        {
            userkeyToIdCache.remove(key);
            keyToUsernameCache.remove(new LazyCacheKey(key));
        }
    }

    private void buildCache()
    {
        // This method aggressively loads the local cache during startup or other refresh the world scenarios.
        // It does contain a possible race condition across cluster nodes, but that is regarded as a worthwhile trade off
        // for performance.

        List<ApplicationUserEntity> users = Select.from(Entity.APPLICATION_USER).runWith(entityEngine).asList();
        for (ApplicationUserEntity user : users)
        {
            keyToUsernameCache.get(new LazyCacheKey(user.getKey(), user.getUsername()));
            usernameToKeyCache.get(new LazyCacheKey(user.getUsername(), user.getKey()));
        }
    }

    @SuppressWarnings ("UnusedParameters")
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        keyToUsernameCache.removeAll();
        usernameToKeyCache.removeAll();
        userkeyToIdCache.removeAll();
        buildCache();
    }

    /**
     * This is a lazy value holding key that </br>
     * <ol>
     *     <li>Allows us to eagerly load the cache at instance startup by calling get with a key that also has the value embedded in it.</li>
     *     <li>Use the normal lazy loading semantics to access the cache in normal operations</li>
     * </ol>
     * Cache loaders that use the key, should not go to the database when the key contains a non-null value, but use the value instead.
     */
    @VisibleForTesting
    static class LazyCacheKey implements Serializable
    {
        final String key;
        final String value;

        @VisibleForTesting
        LazyCacheKey(@Nonnull final String key, @Nullable final String value)
        {
            this.key = key;
            this.value = value;
        }

        private LazyCacheKey(@Nonnull final String key)
        {
            this(key, null);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            final LazyCacheKey that = (LazyCacheKey) o;

            return key.equals(that.key);
        }

        @Override
        public int hashCode()
        {
            return key.hashCode();
        }

        @Override
        public String toString()
        {
            return reflectionToString(this, SHORT_PREFIX_STYLE);
        }
    }

    private class KeyToNameCacheLoader implements CacheLoader<LazyCacheKey, CacheObject<String>>
    {
        @Override
        public CacheObject<String> load(@Nonnull final LazyCacheKey key)
        {
            if (key.value != null)
            {
                return CacheObject.wrap(key.value);
            }
            ApplicationUserEntity user = Select.from(Entity.APPLICATION_USER).whereEqual(USER_KEY, key.key).runWith(entityEngine).singleValue();
            return user == null ? CacheObject.<String>NULL() : CacheObject.wrap(user.getUsername());
        }
    }

    private class NameToKeyCacheLoader implements CacheLoader<LazyCacheKey, CacheObject<String>>
    {
        @Override
        public CacheObject<String> load(@Nonnull final LazyCacheKey key)
        {
            if (key.value != null)
            {
                return CacheObject.wrap(key.value);
            }
            ApplicationUserEntity user = Select.from(Entity.APPLICATION_USER).whereEqual(LOWER_USER_NAME, key.key).runWith(entityEngine).singleValue();
            return user == null ? CacheObject.<String>NULL() : CacheObject.wrap(user.getKey());
        }
    }

    private class KeyToIdCacheLoader implements CacheLoader<String, CacheObject<Long>>
    {
        @Override
        public CacheObject<Long> load(@Nonnull final String key)
        {
            ApplicationUserEntity user = Select.from(Entity.APPLICATION_USER).whereEqual(USER_KEY, key).runWith(entityEngine).singleValue();
            return user == null ? CacheObject.<Long>NULL() : CacheObject.wrap(user.getId());
        }
    }
}
