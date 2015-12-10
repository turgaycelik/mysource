package com.atlassian.jira.i18n;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import java.util.Locale;

/**
 * Small delegate around the CachingI18nHelperFactory that is registered in
 * pico as the actual I18nHelper.BeanFactory
 */
public class DelegateI18nFactory implements I18nHelper.BeanFactory
{
    private final I18nHelper.BeanFactory delegate;

    public DelegateI18nFactory(CachingI18nFactory delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public I18nHelper getInstance(Locale locale)
    {
        return delegate.getInstance(locale);
    }

    @Override
    public I18nHelper getInstance(User user)
    {
        return delegate.getInstance(user);
    }

    @Override
    public I18nHelper getInstance(ApplicationUser user)
    {
        return delegate.getInstance(user);
    }
}
