package com.atlassian.jira.security.auth.trustedapps;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.crowd.embedded.api.User;

public class MockTrustedApplicationManager implements TrustedApplicationManager
{
    private final Cache cache = new Cache();
    private final AtomicInteger sequenceGenerator = new AtomicInteger();

    public MockTrustedApplicationManager(final TrustedApplicationInfo... infos)
    {
        for (final TrustedApplicationInfo o : infos)
        {
            cache.add(o);
            sequenceGenerator.incrementAndGet();
        }
    }

    public Set<TrustedApplicationInfo> getAll()
    {
        return cache.getAll();
    }

    public TrustedApplicationInfo get(final String applicationId)
    {
        return cache.get(applicationId);
    }

    public TrustedApplicationInfo get(final long id)
    {
        return cache.get(id);
    }

    public boolean delete(final User user, final long id)
    {
        return (cache.remove(id) != null);
    }

    @Override
    public boolean delete(User user, String applicationId)
    {
        return cache.removeByApplicationId(applicationId) != null;
    }

    public TrustedApplicationInfo store(final User user, TrustedApplicationInfo info)
    {
        return store(user.getName(), info);
    }

    @Override
    public TrustedApplicationInfo store(String user, TrustedApplicationInfo info)
    {
        if (info.getNumericId() < 1)
        {
            info = new TrustedApplicationBuilder().set(info).setId(sequenceGenerator.incrementAndGet()).toInfo();
        }
        cache.add(info);
        return info;
    }

    private static class Cache
    {
        final Map<Long, TrustedApplicationInfo> byId = new HashMap<Long, TrustedApplicationInfo>();
        final Map<String, TrustedApplicationInfo> byAppId = new HashMap<String, TrustedApplicationInfo>();

        TrustedApplicationInfo get(final long id)
        {
            return byId.get(id);
        }

        TrustedApplicationInfo get(final String applicationId)
        {
            return byAppId.get(applicationId);
        }

        void add(final TrustedApplicationInfo info)
        {
            byId.put(info.getNumericId(), info);
            byAppId.put(info.getID(), info);
        }

        TrustedApplicationInfo remove(final long id)
        {
            final TrustedApplicationInfo info = byId.remove(id);
            if (info != null)
            {
                byAppId.remove(info.getID());
            }
            return info;
        }

        public TrustedApplicationInfo removeByApplicationId(String applicationId)
        {
            TrustedApplicationInfo info = byAppId.remove(applicationId);
            if (info != null)
            {
                byId.remove(info.getNumericId());
            }

            return info;
        }

        Set<TrustedApplicationInfo> getAll()
        {
            return new LinkedHashSet<TrustedApplicationInfo>(byId.values());
        }
    }
}
