package com.atlassian.jira.util.resourcebundle;

import com.atlassian.plugin.Resources;

import java.util.Locale;

/**
 * Represents the lookup of {@link java.util.ResourceBundle}s in JIRA. The lookup is complicated in JIRA because
 * we have to iterate through language packs (which are plugins), general plugins and JIRA's own static resources
 * to find all the {@code ResourceBundle}s and merge them into one.
 *
 * JIRA currently uses resource bundles to find translations for the {@link com.atlassian.jira.web.bean.I18nBean}
 * and help paths for {@link com.atlassian.jira.web.util.HelpUtil}. Both use basically the same algorithm with
 * only slight tweaks.
 *
 * @since v6.2.3
 */
abstract class ResourceLoaderInvocation
{
    private Locale locale = Locale.getDefault();
    private Mode mode = Mode.I18N;

    ResourceLoaderInvocation languages()
    {
        this.mode = Mode.I18N;
        return this;
    }

    ResourceLoaderInvocation help()
    {
        this.mode = Mode.HELP;
        return this;
    }

    ResourceLoaderInvocation locale(Locale locale)
    {
        this.locale = locale;
        return this;
    }

    Locale getLocale()
    {
        return locale;
    }

    Mode getMode()
    {
        return mode;
    }

    abstract ResourceBundleLoader.LoadResult load();

    enum Mode
    {
        I18N(true, "i18n"), HELP(false, "helpPaths");

        private final boolean includeLangPacks;
        private final Resources.TypeFilter filter;

        private Mode(final boolean includeLangPacks, final String filter)
        {
            this.includeLangPacks = includeLangPacks;
            this.filter = new Resources.TypeFilter(filter);
        }

        boolean includeLangPacks()
        {
            return includeLangPacks;
        }

        Resources.TypeFilter filter()
        {
            return filter;
        }
    }
}
