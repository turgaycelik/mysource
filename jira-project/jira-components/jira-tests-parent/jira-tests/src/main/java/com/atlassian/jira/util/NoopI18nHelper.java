package com.atlassian.jira.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * An {@link com.atlassian.jira.util.I18nHelper} that returns the i18n key concatenated with its arguments.
 * This can be used in tests to ensure correct i18n messages are returned. The {@link com.atlassian.jira.util.NoopI18nHelper#makeTranslation(String, java.util.List)}
 * method can be used to create an expected value of a test.
 *
 * @since v4.0
 */
public class NoopI18nHelper implements I18nHelper
{
    private final Locale locale;

    public NoopI18nHelper()
    {
        this(Locale.ENGLISH);
    }

    public NoopI18nHelper(Locale locale)
    {
        this.locale = locale == null ? Locale.ENGLISH : locale;
    }

    @Override
    public String getText(final String key)
    {
        return makeTranslation(key);
    }

    @Override
    public Locale getLocale()
    {
        return this.locale;
    }

    @Override
    public String getUnescapedText(final String key)
    {
        return key;
    }

    @Override
    public String getUntransformedRawText(String key)
    {
        return key;
    }

    @Override
    public boolean isKeyDefined(String key)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText(final String key, final String value1)
    {
        return makeTranslation(key, value1);
    }

    @Override
    public String getText(String key, Object value1, Object value2, Object value3)
    {
        return makeTranslation(key, value1, value2, value3);
    }

    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4)
    {
        return makeTranslation(key, value1, value2, value3, value4);
    }

    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
    {
        return makeTranslation(key, value1, value2, value3, value4, value5);
    }

    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
    {
        return makeTranslation(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @Override
    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
    {
        return makeTranslation(key, value1, value2, value3, value4, value5, value6, value7, value8);
    }

    @Override
    public String getText(final String key, final String value1, final String value2)
    {
        return makeTranslation(key, value1, value2);
    }

    @Override
    public String getText(final String key, final String value1, final String value2, final String value3)
    {
        return makeTranslation(key, value1, value2, value3);
    }

    @Override
    public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
    {
        return makeTranslation(key, value1, value2, value3, value4);
    }

    @Override
    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
    {
        return makeTranslation(key, value1, value2, value3, value4, value5, value6);
    }

    @Override
    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
    {
        return makeTranslation(key, value1, value2, value3, value4, value5, value6, value7);
    }

    @Override
    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
    {
        return makeTranslation(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
    }

    @Override
    public ResourceBundle getDefaultResourceBundle()
    {
        return new ResourceBundle()
        {
            @Override
            protected Object handleGetObject(final String key)
            {
                return key;
            }

            @Override
            public Enumeration<String> getKeys()
            {
                return Collections.enumeration(Collections.<String>emptyList());
            }
        };
    }

    @Override
    public String getText(final String key, final Object params)
    {
        if (params instanceof List<?>)
        {
            return makeTranslation(key, (List<?>) params);
        }
        else if (params instanceof Object[])
        {
            return makeTranslation(key, Arrays.asList((Object[]) params));
        }
        else
        {
            return makeTranslation(key, Collections.singletonList(params));
        }
    }

    @Override
    public Set<String> getKeysForPrefix(final String prefix)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceBundle getResourceBundle()
    {
        throw new UnsupportedOperationException();
    }

    public static String makeTranslation(final String key, final Object... arguments)
    {
        if (key == null)
        {
            return null;
        }
        else
        {
            return makeTranslation(key, Arrays.asList(arguments));
        }
    }

    public static String makeTranslation(final String key, final List<?> arguments)
    {
        return key + '{' + arguments + '}';
    }
}
