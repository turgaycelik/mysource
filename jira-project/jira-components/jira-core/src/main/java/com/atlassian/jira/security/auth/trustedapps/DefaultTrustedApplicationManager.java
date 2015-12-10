/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.dbc.Null;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/* @since v3.12 */
public class DefaultTrustedApplicationManager implements TrustedApplicationManager
{
    private static final Logger log = LoggerFactory.getLogger(DefaultTrustedApplicationManager.class);

    private final TrustedApplicationStore store;

    public DefaultTrustedApplicationManager(final TrustedApplicationStore store)
    {
        Null.not("store", store);
        this.store = store;
    }

    public Set<TrustedApplicationInfo> getAll()
    {
        final Set<TrustedApplicationData> allData = store.getAll();
        final Set<TrustedApplicationInfo> result = new LinkedHashSet<TrustedApplicationInfo>(allData.size());
        for (final TrustedApplicationData data : allData)
        {
            result.add(transform(data));
        }
        return result;
    }

    public TrustedApplicationInfo get(final String applicationId)
    {
        return transform(store.getByApplicationId(applicationId));
    }

    public TrustedApplicationInfo get(final long id)
    {
        return transform(store.getById(id));
    }

    public boolean delete(final User user, final long id)
    {
        ///CLOVER:OFF
        log.info("{} is deleting TrustedApplication: {}", user, id);
        ///CLOVER:ON

        return store.delete(id);
    }

    public boolean delete(User user, String applicationId)
    {
        return store.delete(applicationId);
    }

    public TrustedApplicationInfo store(final User user, final TrustedApplicationInfo info)
    {
        return store(user.getName(), info);
    }

    @Override
    public TrustedApplicationInfo store(String user, TrustedApplicationInfo info)
    {
        ///CLOVER:OFF
        if (log.isInfoEnabled())
        {
            log.info(user + " is storing TrustedApplication: " + info.getNumericId() + " applicationId: " + info.getID());
        }
        ///CLOVER:ON

        final long id = info.getNumericId();
        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        if (id > 0)
        {
            final TrustedApplicationData data = store.getById(id);
            if (data == null)
            {
                throw new IllegalArgumentException("Attempting to update non-existant TrustedApplication: " + info.getID());
            }
            if (!data.getApplicationId().equals(info.getID()))
            {
                throw new IllegalArgumentException(
                    "Cannot change the TrustedApplication ID from: " + data.getApplicationId() + " to: " + info.getID());
            }
            builder.set(data);
        }
        builder.set(info);

        final Date now = new Date();
        if (id == 0)
        {
            builder.setCreated(now);
            builder.setCreatedBy(user);
        }
        builder.setUpdated(now);
        builder.setUpdatedBy(user);
        builder.set(store.store(builder.toData()));
        return builder.toInfo();
    }

    private TrustedApplicationInfo transform(final TrustedApplicationData data)
    {
        return (data == null) ? null : new TrustedApplicationBuilder().set(data).toInfo();
    }
}