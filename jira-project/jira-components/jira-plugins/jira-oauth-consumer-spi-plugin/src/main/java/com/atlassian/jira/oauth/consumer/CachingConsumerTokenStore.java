package com.atlassian.jira.oauth.consumer;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Caching implementation of the Consumer Token Store.  This cache uses an LRU map limited to 4000 entries.
 * <p/>
 * Writes will clear the cache value, to be lazy loaded on the next get.
 *
 * @since v4.0
 */
public class CachingConsumerTokenStore implements ConsumerTokenStore, InitializingBean, DisposableBean
{

    private final Cache<String, CacheObject<ConsumerToken>> cache;

    private ConsumerTokenStore delegateStore;
    private final EventPublisher eventPublisher;

    public CachingConsumerTokenStore(final ConsumerTokenStore delegateStore, final EventPublisher eventPublisher,
            final CacheManager cacheManager)
    {
        this.eventPublisher = eventPublisher;
        notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;

        cache = cacheManager.getCache(CachingConsumerTokenStore.class.getName() + ".cache",
            new ConsumerTokenCacheLoader(),
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

    public ConsumerToken get(final Key key)
    {
        notNull("key", key);

        return cache.get(key.getKey()).getValue();
    }

    public Map<Key, ConsumerToken> getConsumerTokens(String consumerKey)
    {
        return delegateStore.getConsumerTokens(consumerKey);
    }

    public ConsumerToken put(final Key key, final ConsumerToken token)
    {
        notNull("key", key);
        notNull("token", token);
        try
        {
            return delegateStore.put(key, token);
        }
        finally
        {
            cache.remove(key.getKey());
        }
    }

    public void remove(final Key key)
    {
        notNull("key", key);
        try
        {
            delegateStore.remove(key);
        }
        finally
        {
            cache.remove(key.getKey());
        }
    }

    public void removeTokensForConsumer(final String consumerKey)
    {
        notNull("consumerKey", consumerKey);

        //don't really care about concurrency here.  The worst that will happen is that a stale/invalid token
        //will be returned from the cache that's not in the database any longer.  This will result in a failed
        //Oauth request.
        delegateStore.removeTokensForConsumer(consumerKey);
        //perhaps we should iterate here, and only remove the tokens for the consumer however this
        //should be a very infrequent operation.
        cache.removeAll();
    }

    private class ConsumerTokenCacheLoader implements CacheLoader<String,CacheObject<ConsumerToken>>
    {
        @Override
        public CacheObject<ConsumerToken> load(@Nonnull final String key)
        {
            return CacheObject.wrap(delegateStore.get(new Key(key)));
        }
    }
}
