package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.google.common.collect.Maps;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * @since v6.2.3
 */
public class MockUserLocaleStore implements UserLocaleStore
{
    private Locale defaultLocale;
    private final Map<Principal, Locale> data = Maps.newHashMap();

    public MockUserLocaleStore()
    {
        this(Locale.getDefault());
    }

    public MockUserLocaleStore(final Locale defaultLocale)
    {
        this.defaultLocale = defaultLocale;
    }

    @Nonnull
    @Override
    public Locale getLocale(final ApplicationUser user)
    {
        return getLocaleImpl(user);
    }

    @Nonnull
    @Override
    public Locale getLocale(final User user)
    {
        return getLocaleImpl(user);
    }

    public MockUserLocaleStore setLocale(Principal user, Locale locale)
    {
        data.put(user, locale);
        return this;
    }

    private Locale getLocaleImpl(final Principal user)
    {
        final Locale locale = data.get(user);
        return locale == null ? getDefaultLocale() : locale;
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale()
    {
        return defaultLocale;
    }
}
