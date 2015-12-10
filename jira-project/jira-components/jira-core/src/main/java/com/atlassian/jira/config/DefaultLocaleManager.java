package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class DefaultLocaleManager implements LocaleManager
{
    private final JiraLocaleUtils jiraLocaleUtils;
    private final I18nHelper.BeanFactory beanFactory;

    public DefaultLocaleManager(final JiraLocaleUtils jiraLocaleUtils, final I18nHelper.BeanFactory beanFactory)
    {
        this.jiraLocaleUtils = jiraLocaleUtils;
        this.beanFactory = beanFactory;
    }

    @Override
    public Set<Locale> getInstalledLocales()
    {
        final Set<Locale> ret = new LinkedHashSet<Locale>(jiraLocaleUtils.getInstalledLocales());
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public Map<String, String> getInstalledLocalesWithDefault(Locale defaultLocale, I18nHelper i18nHelper)
    {
        final Map<String, String> installedLocalesWithDefault =
                jiraLocaleUtils.getInstalledLocalesWithDefault(defaultLocale, i18nHelper);
        return Collections.unmodifiableMap(installedLocalesWithDefault);
    }

    @Override
    public Locale getLocale(String locale)
    {
        return jiraLocaleUtils.getLocale(locale);
    }

    @Override
    public Locale getLocaleFor(final ApplicationUser user)
    {
        return I18nBean.getLocaleFromUser(user);
    }

    @Override
    public void validateUserLocale(final User loggedInUser, final String locale, ErrorCollection errorCollection)
    {
        if(StringUtils.equals(locale, DEFAULT_LOCALE))
        {
            return;
        }
        try
        {
            Iterables.find(getInstalledLocales(), new Predicate<Locale>()
            {
                @Override
                public boolean apply(Locale input)
                {
                    return input.toString().equals(locale);
                }
            });
        }
        catch(NoSuchElementException nsee)
        {            
            //no matching valid locale found. The provided locale string must be invalid!
            final I18nHelper i18n = beanFactory.getInstance(loggedInUser);
            errorCollection.addError("userLocale", i18n.getText("preferences.invalid.locale", locale));
        }
    }
}
