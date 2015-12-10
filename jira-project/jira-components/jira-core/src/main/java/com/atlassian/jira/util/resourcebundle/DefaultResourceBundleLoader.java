package com.atlassian.jira.util.resourcebundle;

import com.google.common.base.Supplier;

import java.util.Locale;

/**
 * @since v6.2.3
 */
class DefaultResourceBundleLoader implements ResourceBundleLoader
{
    private final boolean i18n;
    private final Supplier<ResourceLoaderInvocation> invocationSupplier;
    private final Locale locale;

    DefaultResourceBundleLoader(Locale locale, final boolean i18n, Supplier<ResourceLoaderInvocation> invocationSupplier)
    {
        this.locale = locale;
        this.i18n = i18n;
        this.invocationSupplier = invocationSupplier;
    }

    @Override
    public final ResourceBundleLoader locale(final Locale locale)
    {
        if (this.locale.equals(locale))
        {
            return this;
        }
        else
        {
            return new DefaultResourceBundleLoader(locale, i18n, invocationSupplier);
        }
    }

    @Override
    public final ResourceBundleLoader helpText()
    {
        return setI18n(false);
    }

    @Override
    public final ResourceBundleLoader i18n()
    {
        return setI18n(true);
    }

    private ResourceBundleLoader setI18n(boolean i18n)
    {
        if (this.i18n == i18n)
        {
            return this;
        }
        else
        {
            return new DefaultResourceBundleLoader(locale, i18n, invocationSupplier);
        }
    }

    @Override
    public final LoadResult load()
    {
        ResourceLoaderInvocation invocation = invocationSupplier.get().locale(locale);

        if (i18n)
        {
            invocation = invocation.languages();
        }
        else
        {
            invocation = invocation.help();
        }
        return invocation.load();
    }
}
