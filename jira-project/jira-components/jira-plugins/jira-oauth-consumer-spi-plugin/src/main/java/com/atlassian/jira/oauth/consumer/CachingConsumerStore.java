package com.atlassian.jira.oauth.consumer;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides a caching consumer store implementation.  Note that in general on the consumer side there's really only one
 * consumer (the instance 'itself').  But there's the ability for administrators to add additional consumer service
 * information because sites like Netflix and TripIt assign developers the consumer key and consumer secret that they
 * should use when communicating with the site. So to make it possible to write gadgets for these services, we need a
 * way to add that consumer information and have a gadget pick that consumer information to use.
 *
 * @since v4.0
 */
public class CachingConsumerStore implements ConsumerServiceStore, InitializingBean, DisposableBean
{
    private final ConsumerCache cache;
    private final ConsumerServiceStore delegateStore;
    private final EventPublisher eventPublisher;

    public CachingConsumerStore(final ConsumerServiceStore delegateStore, final EventPublisher eventPublisher,
            final CacheManager cacheManager)
    {
        notNull("delegateStore", delegateStore);
        this.cache = new ConsumerCache(cacheManager, delegateStore);
        this.delegateStore = delegateStore;
        this.eventPublisher = eventPublisher;
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
    public void clearCaches(final ClearCacheEvent event)
    {
        cache.clear();
    }

    public ConsumerAndSecret get(final String service)
    {
        notNull("service", service);
        return cache.getByService(service);
    }

    public ConsumerAndSecret getByKey(final String key)
    {
        notNull("key", key);

        final String service = cache.getServiceForKey(key);
        //get the cached consumer via its service name.
        return service == null ? null : get(service);
    }

    public void put(final String service, final ConsumerAndSecret cas)
    {
        notNull("service", service);
        notNull("cas", cas);
        notNull("cas.consumer", cas.getConsumer());

        delegateStore.put(service, cas);
        // We clear the cached entry for this consumer so that a get()
        // will retrieve the current value afresh from the database.
        cache.removeByService(cas);
    }

    public void removeByKey(final String key)
    {
        notNull("key", key);
        try
        {
            delegateStore.removeByKey(key);
        }
        finally
        {
            cache.removeByKey(key);
        }
    }

    public Iterable<Consumer> getAllServiceProviders()
    {
        //non-caching call.  There shouldn't be many of these anyway.
        return delegateStore.getAllServiceProviders();
    }

    private static class ConsumerCache
    {
        private final Cache<String, CacheObject<ConsumerAndSecret>> consumerCache;
        //maintains a key -> service mapping.
        private final Cache<String, CacheObject<String>> cacheByKey;
        private final ConsumerServiceStore delegateStore;

        private ConsumerCache(final CacheManager cacheManager, final ConsumerServiceStore delegateStore)
        {
            this.delegateStore = delegateStore;
            consumerCache = cacheManager.getCache(CachingConsumerStore.class.getName() + ".consumerCache",
                    new ConsumerCacheLoader(),
                    new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
            cacheByKey = cacheManager.getCache(CachingConsumerStore.class.getName() + ".cacheByKey",
                new ConsumerCacheByKeyLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
        }


        public ConsumerAndSecret getByService(final String service)
        {
            return consumerCache.get(service).getValue();
        }

        public String getServiceForKey(final String key)
        {
            return cacheByKey.get(key).getValue();
        }

        public void removeByService(final ConsumerAndSecret cas)
        {
            consumerCache.remove(cas.getServiceName());
            cacheByKey.remove(cas.getConsumer().getKey());
        }

        public void removeByKey(final String key)
        {
            final String service = cacheByKey.get(key).getValue();
            cacheByKey.remove(key);
            if (service != null)
            {
                consumerCache.remove(service);
            }
        }

        public void clear()
        {
            consumerCache.removeAll();
            cacheByKey.removeAll();
        }

        private class ConsumerCacheLoader implements CacheLoader<String, CacheObject<ConsumerAndSecret>>
        {
            @Override
            public CacheObject<ConsumerAndSecret> load(@Nonnull final String key)
            {
                return CacheObject.wrap(delegateStore.get(key));
            }
        }

        private class ConsumerCacheByKeyLoader implements CacheLoader<String, CacheObject<String>>
        {
            @Override
            public CacheObject<String> load(@Nonnull final String key)
            {
                final ConsumerAndSecret consumerAndSecret = delegateStore.getByKey(key);
                if (consumerAndSecret == null)
                {
                    return CacheObject.NULL();
                }
                return CacheObject.wrap(consumerAndSecret.getServiceName());
            }
        }
    }

}
