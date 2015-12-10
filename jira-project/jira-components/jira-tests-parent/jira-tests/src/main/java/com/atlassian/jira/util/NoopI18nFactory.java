package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Locale;

/**
 * A factory for {@link NoopI18nHelper} objects.
 *
 * @since v4.0
 */
public class NoopI18nFactory implements I18nHelper.BeanFactory
{
    public I18nHelper getInstance(final Locale locale)
    {
        return new NoopI18nHelper(locale);
    }

    public I18nHelper getInstance(final User user)
    {
        return new NoopI18nHelper();
    }

    @Override
    public I18nHelper getInstance(ApplicationUser user)
    {
        return new NoopI18nHelper();
    }
}
