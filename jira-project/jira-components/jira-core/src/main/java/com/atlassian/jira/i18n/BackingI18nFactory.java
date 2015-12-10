package com.atlassian.jira.i18n;

import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.util.PluginsTracker;
import com.atlassian.jira.util.I18nHelper;

import java.util.Locale;

/**
 * @since v6.2.3
 */
public interface BackingI18nFactory
{
    /**
     * Create an {@link com.atlassian.jira.util.I18nHelper} for the passed parameters.
     *
     * @param locale the locale for the returned {@code I18nHelper}.
     * @param involvedPluginsTracker register all the plugins associated with the returned helper in this tracker.
     * @param translationTransforms the transforms that should be applied by the returned helper during translation.
     *
     * @return a {@link com.atlassian.jira.util.I18nHelper} for the passed {@code Locale}.
     */
    I18nHelper create(Locale locale, PluginsTracker involvedPluginsTracker,
            final Iterable<? extends TranslationTransform> translationTransforms);
}
