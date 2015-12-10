package com.atlassian.jira.web.bean;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Mock I18nBean to get around the problem of having to lookup a default locale via
 * applicationProperties via the DB.
 * If you want to assert the key and not use any property bundles you can use MockI18nHelper.
 *
 * @since v3.13
 */
public class MockI18nBean extends I18nBean
{
    public MockI18nBean()
    {
        super(new SimpleDelegate());
    }

    public MockI18nBean(Locale locale)
    {
        super(new SimpleDelegate(locale));
    }

    @Override
    protected BeanFactory getFactory()
    {
        return new BeanFactory()
        {
            public I18nHelper getInstance(Locale locale)
            {
                return new SimpleDelegate(locale);
            }

            public I18nHelper getInstance(User user)
            {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public I18nHelper getInstance(ApplicationUser user)
            {
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }

    public static class MockI18nBeanFactory implements I18nHelper.BeanFactory
    {
        public I18nHelper getInstance(final Locale locale)
        {
            return new MockI18nBean();
        }

        public I18nHelper getInstance(final User user)
        {
            return new MockI18nBean();
        }

        @Override
        public I18nHelper getInstance(ApplicationUser user)
        {
            return new MockI18nBean();
        }
    }

    private static class SimpleDelegate implements I18nHelper
    {
        private static final Object[] EMPTY_ARRAY = { };

        private final ResourceBundle bundle;
        private final Locale locale;

        private SimpleDelegate()
        {
            this(Locale.ENGLISH);
        }

        private SimpleDelegate(Locale locale)
        {
            this.bundle = ResourceBundle.getBundle(JiraWebActionSupport.class.getName(), locale);
            this.locale = locale;
        }

        @Override
        public Locale getLocale()
        {
            return this.locale;
        }

        @Override
        public ResourceBundle getDefaultResourceBundle()
        {
            return bundle;
        }

        @Override
        public String getUnescapedText(final String key)
        {
            return getTranslation(key);
        }

        @Override
        public String getUntransformedRawText(final String key)
        {
            return getTranslation(key);
        }

        private String getTranslation(final String key)
        {
            if (bundle.containsKey(key))
            {
                return bundle.getString(key);
            }
            else
            {
                return key;
            }
        }

        @Override
        public boolean isKeyDefined(final String key)
        {
            return bundle.containsKey(key);
        }

        @Override
        public String getText(final String key)
        {
            return formatI18nMsg(key);
        }

        @Override
        public String getText(final String key, final String value1)
        {
            return formatI18nMsg(key, value1);
        }

        @Override
        public String getText(final String key, final String value1, final String value2)
        {
            return formatI18nMsg(key, value1, value2);
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3)
        {
            return formatI18nMsg(key, value1, value2, value3);
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
        {
            return formatI18nMsg(key, value1, value2, value3, value4);
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3)
        {
            return formatI18nMsg(key, value1, value2, value3);
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4)
        {
            return formatI18nMsg(key, value1, value2, value3, value4);
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5)
        {
            return formatI18nMsg(key, value1, value2, value3, value4, value5);
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
        {
            return formatI18nMsg(key, value1, value2, value3, value4, value5, value6);
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7)
        {
            return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7);
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
        {
            return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7);
        }

        @Override
        public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6, final Object value7, final Object value8)
        {
            return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7, value8);
        }

        @Override
        public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
        {
            return formatI18nMsg(key, value1, value2, value3, value4, value5, value6, value7, value8, value9);
        }

        @Override
        public String getText(final String key, final Object parameters)
        {
            final Object[] substitutionParameters;
            if (parameters instanceof Object[])
            {
                substitutionParameters = (Object[]) parameters;
            }
            else if (parameters instanceof Iterable)
            {
                substitutionParameters = Iterables.toArray((Iterable<?>) parameters, Object.class);
            }
            else if (parameters == null)
            {
                substitutionParameters = EMPTY_ARRAY;
            }
            else
            {
                substitutionParameters = new Object[] { parameters };
            }
            return formatI18nMsg(key, substitutionParameters);

        }

        @Override
        public Set<String> getKeysForPrefix(final String prefix)
        {
            return Sets.filter(bundle.keySet(), new Predicate<String>()
            {
                @Override
                public boolean apply(final String input)
                {
                    return input.startsWith(prefix);
                }
            });
        }

        @Override
        public ResourceBundle getResourceBundle()
        {
            return bundle;
        }

        private String formatI18nMsg(String key, final Object...substitutionParameters)
        {
            if (key == null || !bundle.containsKey(key))
            {
                return key;
            }
            else
            {
                final MessageFormat mf = new MessageFormat(bundle.getString(key), locale);
                return mf.format((substitutionParameters == null) ? EMPTY_ARRAY : substitutionParameters);
            }
        }
    }
}
