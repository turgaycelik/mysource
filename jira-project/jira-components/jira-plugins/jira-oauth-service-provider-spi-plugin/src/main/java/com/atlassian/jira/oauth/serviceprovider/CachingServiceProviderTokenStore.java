package com.atlassian.jira.oauth.serviceprovider;

import java.util.concurrent.TimeUnit;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.user.UserRenamedEvent;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import com.atlassian.oauth.serviceprovider.StoreException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Caching Service provider token store, responsible for caching OAuth service provider authentication tokens.
 *
 * @since v4.0
 */
public class CachingServiceProviderTokenStore implements ServiceProviderTokenStore, InitializingBean, DisposableBean
{
    private final Cache<String, CacheObject<ServiceProviderToken>> cache;

    private ServiceProviderTokenStore delegateStore;
    private final EventPublisher eventPublisher;

    public CachingServiceProviderTokenStore(final ServiceProviderTokenStore delegateStore, final EventPublisher eventPublisher,
            final CacheManager cacheManager)
    {
        this.delegateStore = delegateStore;
        this.eventPublisher = eventPublisher;

        cache = cacheManager.getCache(CachingServiceProviderTokenStore.class.getName() + ".cache",
                new TokenCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).maxEntries(4000).build());
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception

    {
        eventPublisher.register(this);
    }

    @EventListener
    @SuppressWarnings("unused")
    public void clearCaches(ClearCacheEvent event)
    {
        cache.removeAll();
    }

    @EventListener
    @SuppressWarnings("unused")
    public void onUserRenamed(final UserRenamedEvent event)
    {
        // The cache contains ServiceProviderTokens which contain User objects. If a user is renamed, and there is a token
        // for that user, the corresponding User object is now invalid. As this shouldn't happen very often, we simply clear the cache.
        cache.removeAll();
    }

    @Override
    public ServiceProviderToken get(final String token) throws StoreException
    {
        notNull("token", token);
        return cache.get(token).getValue();
    }

    @Override
    public Iterable<ServiceProviderToken> getAccessTokensForUser(final String username)
    {
        //this will only be called when a user goes to this section in their profile,
        //so this can be a non-caching call.
        return delegateStore.getAccessTokensForUser(username);
    }

    @Override
    public ServiceProviderToken put(final ServiceProviderToken token) throws StoreException
    {
        notNull("token", token);

        try
        {
            return delegateStore.put(token);
        }
        finally
        {
            cache.remove(token.toString());
        }
    }

    @Override
    public void removeAndNotify(final String token) throws StoreException
    {
        notNull("token", token);

        try
        {
            delegateStore.removeAndNotify(token);
        }
        finally
        {
            cache.remove(token);
        }
    }

    @Override
    public void removeExpiredTokensAndNotify() throws StoreException
    {
        //Will be called every 10 hrs.  Perhaps we should do some smarter cache removal here.
        try
        {
            delegateStore.removeExpiredTokensAndNotify();
        }
        finally
        {
            cache.removeAll();
        }
    }

    /**
     * @since 5.1
     */
    @Override
    public void removeExpiredSessionsAndNotify() throws StoreException
    {

        try
        {
            delegateStore.removeExpiredSessionsAndNotify();
        }
        finally
        {
            cache.removeAll();
        }
    }

    @Override
    public void removeByConsumer(final String consumerKey)
    {
        //This will only be called when an admin removes an oauth consumer, so not very often.
        try
        {
            delegateStore.removeByConsumer(consumerKey);
        }
        finally
        {
            cache.removeAll();
        }
    }

    private class TokenCacheLoader implements CacheLoader<String, CacheObject<ServiceProviderToken>>
    {
        @Override
        public CacheObject<ServiceProviderToken> load(final String key)
        {
            return CacheObject.wrap(delegateStore.get(key));
        }
    }
}
