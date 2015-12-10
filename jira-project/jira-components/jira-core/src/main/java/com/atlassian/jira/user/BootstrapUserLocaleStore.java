package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;

import java.util.Locale;
import javax.annotation.Nonnull;

/**
 * Used when JIRA has no access to the database. At that stage each user will be using the JIRA default locale.
 * The implementation will be replaced with a complete implementation once JIRA is in a state to do so.
 *
 * @since v6.2.3.
 */
public class BootstrapUserLocaleStore implements UserLocaleStore
{
    private final ApplicationProperties properties;

    public BootstrapUserLocaleStore(final ApplicationProperties properties) {this.properties = properties;}

    @Nonnull
    @Override
    public Locale getLocale(final ApplicationUser user)
    {
        return getDefaultLocale();
    }

    @Nonnull
    @Override
    public Locale getLocale(final User user)
    {
        return getDefaultLocale();
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale()
    {
        return properties.getDefaultLocale();
    }
}
