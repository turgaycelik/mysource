package com.atlassian.jira.i18n;

import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserKey;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Set;

/**
 * Resolves the locale for a particular request.  Depends on the user that's currently logged in, otherwise the system
 * default locale will be used.
 *
 * @since v2.1
 */
public class JiraLocaleResolver implements LocaleResolver
{
    private final LocaleManager localeManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserManager userManager;

    public JiraLocaleResolver(final LocaleManager localeManager, final JiraAuthenticationContext jiraAuthenticationContext,
            final UserManager userManager)
    {
        this.localeManager = localeManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userManager = userManager;
    }

    @Override
    public Locale getLocale(final HttpServletRequest request)
    {
        return jiraAuthenticationContext.getLocale();
    }

    @Override
    public Locale getLocale()
    {
        return jiraAuthenticationContext.getLocale();
    }

    public Locale getLocale(final UserKey userKey)
    {
        ApplicationUser user = userManager.getUserByKey(userKey.getStringValue());

        // handles null user by returning the default locale
        return localeManager.getLocaleFor(user);
    }

    @Override
    public Set<Locale> getSupportedLocales()
    {
        return localeManager.getInstalledLocales();
    }
}
