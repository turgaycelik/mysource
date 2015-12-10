package com.atlassian.jira.security.auth.trustedapps;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class MockTrustedApplicationStore implements TrustedApplicationStore
{
    private final Cache cache = new Cache();
    private final AtomicInteger sequenceGenerator = new AtomicInteger();

    MockTrustedApplicationStore(final Collection<? extends TrustedApplicationData> datas)
    {
        if (datas != null)
        {
            for (final TrustedApplicationData data : datas)
            {
                cache.add(data);
                sequenceGenerator.incrementAndGet();
            }
        }
    }

    public TrustedApplicationData store(TrustedApplicationData data)
    {
        if (data.getId() < 1)
        {
            data = new TrustedApplicationBuilder().set(data).setId(sequenceGenerator.incrementAndGet()).toData();
        }
        cache.add(data);
        return data;
    }

    public TrustedApplicationData getByApplicationId(final String applicationId)
    {
        return cache.get(applicationId);
    }

    public TrustedApplicationData getById(final long id)
    {
        return cache.get(id);
    }

    public Set<TrustedApplicationData> getAll()
    {
        return cache.getAll();
    }

    public boolean delete(final long id)
    {
        return (cache.remove(id) != null);
    }

    @Override
    public boolean delete(String applicationId)
    {
        return cache.removeByApplicationId(applicationId) != null;
    }

    private static class Cache
    {
        final Map<Long, TrustedApplicationData> byId = new HashMap<Long, TrustedApplicationData>();
        final Map<String, TrustedApplicationData> byAppId = new HashMap<String, TrustedApplicationData>();

        TrustedApplicationData get(final long id)
        {
            return byId.get(id);
        }

        TrustedApplicationData get(final String applicationId)
        {
            return byAppId.get(applicationId);
        }

        void add(final TrustedApplicationData data)
        {
            byId.put(data.getId(), data);
            byAppId.put(data.getApplicationId(), data);
        }

        TrustedApplicationData remove(final long id)
        {
            final TrustedApplicationData data = byId.remove(id);
            if (data != null)
            {
                byAppId.remove(data.getApplicationId());
            }
            return data;
        }

        public TrustedApplicationData removeByApplicationId(String applicationId)
        {
            TrustedApplicationData data = byAppId.remove(applicationId);
            if (data != null)
            {
                byId.remove(data.getId());
            }

            return data;
        }

        Set<TrustedApplicationData> getAll()
        {
            return new LinkedHashSet<TrustedApplicationData>(byId.values());
        }
    }
}
