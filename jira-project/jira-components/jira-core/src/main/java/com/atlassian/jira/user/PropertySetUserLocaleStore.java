package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.util.Users;
import com.atlassian.jira.util.LocaleParser;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Locale;
import javax.annotation.Nonnull;

/**
 * @since v6.2.3
 */
public class PropertySetUserLocaleStore implements UserLocaleStore
{
    private final ApplicationProperties applicationProperties;
    private final UserPropertyManager userPropertyManager;
    private final UserKeyService userKeys;

    public PropertySetUserLocaleStore(final ApplicationProperties applicationProperties,
            final UserPropertyManager userPropertyManager, final UserKeyService userKeys)
    {
        this.applicationProperties = applicationProperties;
        this.userPropertyManager = userPropertyManager;
        this.userKeys = userKeys;
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale()
    {
        return applicationProperties.getDefaultLocale();
    }

    @Nonnull
    @Override
    public Locale getLocale(final ApplicationUser user)
    {
        if (!Users.isAnonymous(user))
        {
            return getLocaleFromUserPropertySet(userPropertyManager.getPropertySet(user));
        }
        return getDefaultLocale();
    }

    @Nonnull
    @Override
    public Locale getLocale(final User user)
    {
        if (!Users.isAnonymous(user))
        {
            //We don't call userPropertyManager.getPropertySetForUser(user) directly because it is possible that
            //this method will be called before the user has a key (i.e. from I18nBean or HelpUtil in early startup
            // code). To work around this we return the default locale when the user has no key.
            final String keyForUser = userKeys.getKeyForUser(user);
            if (keyForUser != null)
            {
                return getLocaleFromUserPropertySet(userPropertyManager.getPropertySetForUserKey(keyForUser));
            }
        }
        return getDefaultLocale();
    }

    private Locale getLocaleFromUserPropertySet(final PropertySet propertySet)
    {
        if (propertySet != null)
        {
            final Locale locale = LocaleParser.parseLocale(propertySet.getString(PreferenceKeys.USER_LOCALE));
            if (locale != null)
            {
                return locale;
            }
        }
        return getDefaultLocale();
    }
}
