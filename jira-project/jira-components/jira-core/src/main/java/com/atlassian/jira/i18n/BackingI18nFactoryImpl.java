package com.atlassian.jira.i18n;

import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.util.PluginsTracker;
import com.atlassian.jira.util.i18n.I18nTranslationMode;
import com.atlassian.jira.util.resourcebundle.ResourceBundleLoader;
import com.atlassian.jira.web.bean.i18n.TranslationStore;
import com.atlassian.jira.web.bean.i18n.TranslationStoreFactory;
import com.atlassian.plugin.Plugin;

import java.util.Locale;

/**
* @since v6.2.3
*/
public class BackingI18nFactoryImpl implements BackingI18nFactory
{
    private final TranslationStoreFactory storeFactory;
    private final ResourceBundleLoader resourceLoader;
    private final I18nTranslationMode translationMode;

    public BackingI18nFactoryImpl(final TranslationStoreFactory storeFactory, final ResourceBundleLoader resourceLoader,
            final I18nTranslationMode translationMode)
    {
        this.storeFactory = storeFactory;
        this.resourceLoader = resourceLoader.i18n();
        this.translationMode = translationMode;
    }

    @Override
    public BackingI18n create(final Locale locale,
            final PluginsTracker involvedPluginsTracker,
            final Iterable<? extends TranslationTransform> translationTransforms)
    {
        final ResourceBundleLoader.LoadResult load = resourceLoader.locale(locale).load();
        final TranslationStore translationStore = storeFactory.createTranslationStore(load.getData());
        for (Plugin plugin : load.getPlugins())
        {
            involvedPluginsTracker.trackInvolvedPlugin(plugin);
        }

        return new BackingI18n(locale, translationMode, translationTransforms, translationStore);
    }
}
