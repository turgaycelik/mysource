package com.atlassian.jira.dev.i18n;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.fugue.Option;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;

import java.util.Locale;

import static com.atlassian.jira.user.preferences.PreferenceKeys.USER_LOCALE;

/**
 * Component for disabling and enabling translations for qunit tests.
 */
public class QunitLocaleSwitcher
{
    public static final Locale EN_MOON = new Locale("en", "MOON");

    private final JiraAuthenticationContext authenticationContext;
    private final UserPreferencesManager userPreferencesManager;
    private final ApplicationProperties applicationProperties;

    public QunitLocaleSwitcher(final JiraAuthenticationContext authenticationContext,
            final UserPreferencesManager userPreferencesManager, final ApplicationProperties applicationProperties)
    {
        this.authenticationContext = authenticationContext;
        this.userPreferencesManager = userPreferencesManager;
        this.applicationProperties = applicationProperties;
    }

    public void disableTranslations()
    {
        final Option<String> oldUserLocale = getUserLocaleString();
        if (oldUserLocale.isDefined())
        {
            setUserLocaleString(EN_MOON.toString());
        }
        applicationProperties.setString(APKeys.JIRA_I18N_DEFAULT_LOCALE, EN_MOON.toString());
    }

    public void resetTranslations()
    {
        applicationProperties.setString(APKeys.JIRA_I18N_DEFAULT_LOCALE, Locale.getDefault().toString());
        setUserLocaleString(null);

    }

    private Option<String> getUserLocaleString()
    {
        final ApplicationUser loggedInUser = authenticationContext.getUser();
        if (loggedInUser != null)
        {
            return Option.option(userPreferencesManager.getExtendedPreferences(loggedInUser).getString(USER_LOCALE));
        }
        return Option.none();
    }


    private void setUserLocaleString(final String locale)
    {
        final ApplicationUser loggedInUser = authenticationContext.getUser();
        if (loggedInUser != null)
        {
            try
            {
                final ExtendedPreferences userPrefs = userPreferencesManager.getExtendedPreferences(loggedInUser);
                if (locale != null)
                {
                    userPrefs.setString(USER_LOCALE, locale);
                }
                else if (userPrefs.containsValue(USER_LOCALE))
                {
                    userPrefs.remove(USER_LOCALE);
                }
            }
            catch (AtlassianCoreException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean shouldDisableTranslationFor(final Locale locale)
    {
        return EN_MOON.equals(locale);
    }
}
