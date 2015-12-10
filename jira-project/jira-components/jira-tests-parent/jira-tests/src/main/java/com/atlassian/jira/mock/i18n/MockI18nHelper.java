package com.atlassian.jira.mock.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.resourcebundle.DefaultResourceBundle;

/**
 * Mock implementation of {@link com.atlassian.jira.util.I18nHelper}.
 *
 * @since v4.2
 */
public class MockI18nHelper implements I18nHelper
{
    public class MockI18nHelperFactory implements BeanFactory
    {
        public I18nHelper getInstance(final Locale locale)
        {
            return MockI18nHelper.this;
        }

        public I18nHelper getInstance(final User user)
        {
            return MockI18nHelper.this;
        }

        @Override
        public I18nHelper getInstance(ApplicationUser user)
        {
            return MockI18nHelper.this;
        }
    }

    private final Map<String,String> values = new HashMap<String,String>();
    private Locale locale;

    public MockI18nHelper(final Locale locale)
    {
        this.locale = locale;
    }

    public BeanFactory factory()
    {
        return new MockI18nHelperFactory();
    }

    public MockI18nHelper()
    {
    }

    public MockI18nHelper stubWith(String key, String value)
    {
        values.put(key, value); return this;
    }

    public boolean hasKey(final String key)
    {
        return values.containsKey(key);
    }

    public String getText(final String key)
    {
        return getText(key, new Object[0]);
    }

    public Locale getLocale()
    {
        return locale;
    }

    public MockI18nHelper setLocale(Locale locale)
    {
        this.locale = locale;
        return this;
    }

    public String getUnescapedText(final String key)
    {
        return getText(key);
    }

    @Override
    public String getUntransformedRawText(String key)
    {
        return getText(key);
    }

    @Override
    public boolean isKeyDefined(String key)
    {
        return values.containsKey(key);
    }

    public String getText(final String key, final String value1)
    {
        return getText(key, EasyList.build(value1));
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4));
    }

    public String getText(final String key, final String value1, final String value2)
    {
        return getText(key, EasyList.build(value1, value2));
    }

    //(added by Shaun during i18n)
    public String getText(final String key, final String value1, final String value2, final String value3)
    {
        return getText(key, EasyList.build(value1, value2, value3));
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6));
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
    {
        return getText(key, CollectionBuilder.list(value1, value2, value3, value4, value5, value6, value7));
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7, value8, value9));
    }

    public String getText(String key, Object value1, Object value2, Object value3)
    {
        return getText(key, EasyList.build(value1, value2, value3));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7, value8));
    }    

    public ResourceBundle getDefaultResourceBundle()
    {
        return null;
    }

    public String getText(final String key, final Object parameters)
    {
        Assertions.notNull("key", key);
        Object[] params = resolveParams(parameters);
        String message = values.get(key);
        if (message == null)
        {
            // build up a fake message by adding the params to the end
            message = key;
            for (int i = 0; i < params.length; i++)
            {
                message += " [{" + i + "}]";
            }
        }
        return new MessageFormat(message).format(params);
    }

    private Object[] resolveParams(final Object parameters)
    {
        final Object[] params;
        if (parameters instanceof List)
        {
            params = ((List<?>) parameters).toArray();
        }
        else if (parameters instanceof Object[])
        {
            params = (Object[]) parameters;
        }
        else
        {
            params = new Object[] { parameters };
        }
        return params;
    }

    public Set<String> getKeysForPrefix(final String prefix)
    {
        return null;
    }

    @Override
    public ResourceBundle getResourceBundle()
    {
        return DefaultResourceBundle.getDefaultResourceBundle(locale);
    }
}
