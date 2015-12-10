package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.google.common.annotations.VisibleForTesting;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * The standard implementation of {@link com.atlassian.jira.util.I18nHelper}. This class should not be instantiated
 * directly but rather obtained indirectly through the {@link com.atlassian.jira.util.I18nHelper.BeanFactory} or by
 * simply injecting a {@code I18nHelper} directly into your code.
 *
 * In the olden days, there was no {@code I18nHelper} and callers would instantiate this class directly.
 * To keep backwards compatibility this class can still be directly created. However, it became very expensive
 * to create an new bean each time (e.g. we have to scan plugins for translations). To combat this, the class was
 * converted into a flyweight that delegates most calls to an {@code I18nHelper} obtained through a
 * {@code I18nHelper.BeanFactory}. In this way people creating {@code I18nBean}s directly will actually be following
 * JIRA's recommended pattern.
 *
 * @since v4.3 this become a flyweight that wraps the cached version of the Real Thing.
 *
 * @see I18nHelper
 * @see com.atlassian.jira.security.JiraAuthenticationContext#getI18nHelper()
 * @see I18nHelper.BeanFactory
 */
public class I18nBean implements I18nHelper
{
    /**
     * @param user the user
     * @return the user's specified {@link Locale}
     * @since 4.1
     */
    public static Locale getLocaleFromUser(final User user)
    {
        return getUserLocaleStore().getLocale(user);
    }

    public static Locale getLocaleFromUser(final ApplicationUser user)
    {
        return getUserLocaleStore().getLocale(user);
    }

    private static Locale getDefaultLocale()
    {
        return getUserLocaleStore().getDefaultLocale();
    }

    private static UserLocaleStore getUserLocaleStore() {return ComponentAccessor.getComponent(UserLocaleStore.class);}

    private final I18nHelper delegate;

    public I18nBean()
    {
        delegate = getFactory().getInstance(getDefaultLocale());
    }

    public I18nBean(final Locale locale)
    {
        delegate = getFactory().getInstance(locale);
    }

    public I18nBean(final User user)
    {
        this(I18nBean.getLocaleFromUser(user));
    }

    public I18nBean(final ApplicationUser user)
    {
        this(I18nBean.getLocaleFromUser(user));
    }

    public I18nBean(final I18nHelper delegate)
    {
        this.delegate = delegate;
    }

    /**
     * Construct an I18nBean in the given Locale.
     *
     * @param localeString The locale String. eg "fr_CA"
     * @deprecated use {@link #I18nBean(java.util.Locale)} instead
     */
    @Deprecated
    public I18nBean(final String localeString)
    {
        this(LocaleParser.parseLocale(localeString));
    }

    @VisibleForTesting
    protected BeanFactory getFactory()
    {
        return ComponentAccessor.getI18nHelperFactory();
    }

    /////////////////////////////////////
    // A bunch of IDE-generated delegates
    /////////////////////////////////////

    @Override
    public Locale getLocale()
    {
        return delegate.getLocale();
    }

    @Override
    public ResourceBundle getDefaultResourceBundle()
    {
        return delegate.getDefaultResourceBundle();
    }

    @Override
    public Set<String> getKeysForPrefix(String prefix)
    {
        return delegate.getKeysForPrefix(prefix);
    }

    @Override
    public ResourceBundle getResourceBundle()
    {
        return delegate.getResourceBundle();
    }

    @Override
    public String getUnescapedText(String key)
    {
        return delegate.getUnescapedText(key);
    }

    @Override
    public String getUntransformedRawText(String key)
    {
        return delegate.getUntransformedRawText(key);
    }

    @Override
    public boolean isKeyDefined(String key)
    {
        return delegate.isKeyDefined(key);
    }

    @HtmlSafe
    @Override
    public String getText(String key)
    {
        return delegate.getText(key);
    }

    @HtmlSafe
    @Override
    public String getText(String key, String value1)
    {
        return delegate.getText(key, value1);
    }

    @HtmlSafe
    @Override
    public String getText(String key, Object parameters)
    {
        return delegate.getText(key, parameters);
    }

    @HtmlSafe
    @Override
    public String getText(String key, Object value1, Object value2, Object value3)
    {
        return delegate.getText(key, value1, value2, value3);
    }

    @HtmlSafe
    @Override
    public String getText(String key, String value1, String value2)
    {
        return delegate.getText(key, value1, value2);
    }

    @HtmlSafe
    @Override
    public String getText(String key, String value1, String value2, String value3)
    {
        return delegate.getText(key, value1, value2, value3);
    }

    @HtmlSafe
    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4)
    {
        return delegate.getText(key, value1, value2, value3, value4);
    }

    @HtmlSafe
    @Override
    public String getText(String key, String value1, String value2, String value3, String value4)
    {
        return delegate.getText(key, value1, value2, value3, value4);
    }

    @HtmlSafe
    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5);
    }

    @HtmlSafe
    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6);
    }

    @HtmlSafe
    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @HtmlSafe
    @Override
    public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @HtmlSafe
    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7, value8);
    }

    @HtmlSafe
    @Override
    public String getText(String key, String value1, String value2, String value3, String value4, String value5, String value6, String value7, String value8, String value9)
    {
        return delegate.getText(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
    }
}
