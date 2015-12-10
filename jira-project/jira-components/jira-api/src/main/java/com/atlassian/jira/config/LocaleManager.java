package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Provides Locale information for this JIRA instance.
 *
 * @since v4.0
 */
@PublicApi
public interface LocaleManager
{
    /**
     * The default locale id.
     */
    static final String DEFAULT_LOCALE = "-1";

    /**
     * Returns a set of locales supported by this JIRA instance.  This is typically the language packs installed.
     *
     * @return a set of locales supported
     */
    Set<Locale> getInstalledLocales();

    /**
     * Returns a mapping of localeString to its displayname.  Also includes a 'Default' locale.
     *
     * @param defaultLocale The locale to use as the default
     * @param i18nHelper Required to internationalize the 'Default'
     * @return A mapping from localeString to its displayname.
     */
    Map<String, String> getInstalledLocalesWithDefault(Locale defaultLocale, I18nHelper i18nHelper);

    /**
     * Given a string, return the corresponding Locale.
     *
     * @param locale Locale in string form
     * @return The {@link java.util.Locale} object
     */
    Locale getLocale(String locale);

    /**
     * Returns the locale for the given user.
     *
     * @param user The user
     * @return the locale for the given user.
     */
    Locale getLocaleFor(ApplicationUser user);

    /**
     * Given a user entered locale string this method ensures that it is in fact a locale that
     * exists in this instance
     *
     * @param loggedInUser The user performing the operation
     * @param locale the locale String to validate (e.g. en_UK)
     * @param errorCollection Error collection to record errors in
     */
    void validateUserLocale(final User loggedInUser, final String locale, final ErrorCollection errorCollection);
}
