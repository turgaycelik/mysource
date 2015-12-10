package com.atlassian.jira.util.resourcebundle;

import com.atlassian.plugin.Plugin;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * @since v6.2.3
 */
public class MockResourceBundleLoader implements ResourceBundleLoader
{
    private final Locale locale;
    private final Map<Locale, MockResourceBundleLoader> localeMap;
    private Mode mode;
    private Map<Mode, LoadResult> result = new EnumMap<Mode, LoadResult>(Mode.class);

    public MockResourceBundleLoader()
    {
        this(Locale.TAIWAN);
    }

    public MockResourceBundleLoader(final Locale locale)
    {
        this(locale, Maps.<Locale, MockResourceBundleLoader>newHashMap());
    }

    private MockResourceBundleLoader(final Locale locale, Map<Locale, MockResourceBundleLoader> registry)
    {
        this.locale = locale;
        this.localeMap = registry;
    }

    @Override
    public MockResourceBundleLoader locale(final Locale locale)
    {
        if (locale.equals(this.locale))
        {
            return this;
        }
        else
        {
            MockResourceBundleLoader localeLoader = localeMap.get(locale);
            if (localeLoader == null)
            {
                localeMap.put(locale, localeLoader = new MockResourceBundleLoader(locale, localeMap));
            }
            return localeLoader;
        }
    }

    @Override
    public MockResourceBundleLoader helpText()
    {
        mode = Mode.HELP;
        return this;
    }

    @Override
    public MockResourceBundleLoader i18n()
    {
        mode = Mode.I18N;
        return this;
    }

    @Override
    public LoadResult load()
    {
        LoadResult loadResult = result.get(mode);
        if (loadResult == null)
        {
            loadResult = emptyResult();
        }
        return loadResult;
    }

    private LoadResult emptyResult()
    {
        return new LoadResult(Collections.<String, String>emptyMap(), Collections.<Plugin>emptyList());
    }

    private LoadResult load(Mode mode)
    {
        LoadResult loadResult = result.get(mode);
        if (loadResult == null)
        {
            loadResult = emptyResult();
        }
        return loadResult;
    }

    public MockResourceBundleLoader registerI18n(Map<String, String> text, Plugin... plugins)
    {
        return register(Mode.I18N, text, plugins);
    }

    public MockResourceBundleLoader registerHelp(Map<String, String> text, Plugin...plugins)
    {
        return register(Mode.HELP, text, plugins);
    }

    private MockResourceBundleLoader register(final Mode mode, Map<String, String> text, Plugin...plugins)
    {
        result.put(mode, new LoadResult(text, Arrays.asList(plugins)));
        return this;
    }

    private static enum Mode
    {
        I18N, HELP
    }
}
