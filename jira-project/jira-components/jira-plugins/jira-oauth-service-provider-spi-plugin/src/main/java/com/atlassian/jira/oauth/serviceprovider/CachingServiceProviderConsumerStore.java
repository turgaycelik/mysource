package com.atlassian.jira.oauth.serviceprovider;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.StoreException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Caching Consumer Store, responsible for providing Consumers added by administrators in the Admin section. This list
 * will most likely not be very large since consumers are manually added by administrators.
 * <p/>
 * Need to provide a cache however since lookups of a single consumer have to be fast, since everytime and OAUTH request
 * comes in, we need to check if the consumer for this request has been added by an admin.
 *
 * @since v4.0
 */
public class CachingServiceProviderConsumerStore implements ServiceProviderConsumerStore, InitializingBean, DisposableBean
{
    //no need here for an LRU map since this cache shouldn't really grow very large.
    private final Cache<String, CacheObject<Consumer>> cache;

    private ServiceProviderConsumerStore delegateStore;
    private final EventPublisher eventPublisher;

    public CachingServiceProviderConsumerStore(final ServiceProviderConsumerStore delegateStore, final EventPublisher eventPublisher,
            CacheManager cacheManager)
    {
        this.eventPublisher = eventPublisher;
        notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;

        cache = cacheManager.getCache(CachingServiceProviderConsumerStore.class.getName() + ".cache",
                new ConsumerCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
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
    public void clearCaches(ClearCacheEvent event)
    {
        cache.removeAll();
    }

    public void put(final Consumer consumer) throws StoreException
    {
        notNull("consumer", consumer);
        notNull("consumer.key", consumer.getKey());
        notNull("consumer.name", consumer.getName());
        notNull("consumer.publicKey", consumer.getPublicKey());

        try
        {
            delegateStore.put(consumer);
        }
        finally
        {
            cache.remove(consumer.getKey());
        }
    }

    public Consumer get(final String key) throws StoreException
    {
        notNull("key", key);
        return cache.get(key).getValue();
    }

    public Iterable<Consumer> getAll() throws StoreException
    {
        //this will only be used in the admin section when displaying all consumers, so not caching this
        //call.
        return delegateStore.getAll();
    }

    public void remove(final String key) throws StoreException
    {
        notNull("key", key);
        try
        {
            delegateStore.remove(key);
        }
        finally
        {
            cache.remove(key);
        }
    }

    private class ConsumerCacheLoader implements CacheLoader<String, CacheObject<Consumer>>
    {
        @Override
        public CacheObject<Consumer> load(@Nonnull final String key)
        {
            return CacheObject.wrap(delegateStore.get(key));
        }
    }
}
