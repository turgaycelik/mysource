package com.atlassian.jira.i18n;

import com.atlassian.jira.plugin.language.AppendTextTransform;
import com.atlassian.jira.plugin.language.PrependTextTransform;
import com.atlassian.jira.plugin.language.TranslationTransform;
import com.atlassian.jira.plugin.util.MockPluginTracker;
import com.atlassian.jira.util.resourcebundle.MockResourceBundleLoader;
import com.atlassian.jira.web.bean.MockI18nTranslationMode;
import com.atlassian.jira.web.bean.i18n.MockTranslationStoreFactory;
import com.atlassian.plugin.Plugin;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.3
 */
public class TestBackingI18nFactoryImpl
{
    private final MockTranslationStoreFactory storeFactory = new MockTranslationStoreFactory();
    private final MockResourceBundleLoader resourceLoader = new MockResourceBundleLoader(Locale.ENGLISH);
    private final MockI18nTranslationMode translationMode = new MockI18nTranslationMode();
    private final BackingI18nFactoryImpl i18nFactory = new BackingI18nFactoryImpl(storeFactory, resourceLoader, translationMode);
    private final MockPluginTracker tracker = new MockPluginTracker();

    @Test
    public void createReturnsBackingBeanInCorrectLocale()
    {
        resourceLoader.locale(Locale.GERMANY).i18n().registerI18n(ImmutableMap.of("german", "yes"));

        final BackingI18n backingI18n = i18nFactory.create(Locale.GERMANY, tracker, Collections.<TranslationTransform>emptyList());

        assertThat(backingI18n.getLocale(), equalTo(Locale.GERMANY));
        assertThat(backingI18n.getText("german"), equalTo("yes"));
    }

    @Test
    public void createReturnsBackingBeanWithTrackedPlugins()
    {
        final Plugin plugin1 = Mockito.mock(Plugin.class);
        final Plugin plugin2 = Mockito.mock(Plugin.class);

        resourceLoader.locale(Locale.GERMANY).i18n().registerI18n(ImmutableMap.<String, String>of(), plugin1, plugin2);
        i18nFactory.create(Locale.GERMANY, tracker, Collections.<TranslationTransform>emptyList());

        assertThat(tracker.getTrackedPlugins(), contains(plugin1, plugin2));
    }

    @Test
    public void createReturnsBackingBeanWithTranslations()
    {
        final List<TranslationTransform> translationTransforms = Arrays.asList(new PrependTextTransform("Start"),
                new AppendTextTransform("End"));

        resourceLoader.locale(Locale.ITALY).i18n().registerI18n(ImmutableMap.of("italy", "beer"));
        final BackingI18n backingI18n = i18nFactory.create(Locale.ITALY, tracker, translationTransforms);

        assertThat(backingI18n.getText("italy"), equalTo("StartbeerEnd"));
    }

    @Test
    public void createsI18nWithEmptyValues()
    {
        resourceLoader.locale(Locale.FRENCH).i18n().registerI18n(ImmutableMap.of("abc.key", ""));
        final BackingI18n backingI18n = i18nFactory.create(Locale.FRENCH, tracker, Collections.<TranslationTransform>emptyList());

        assertThat(backingI18n.getText("abc.key"), equalTo(""));
        assertThat(backingI18n.getLocale(), equalTo(Locale.FRENCH));
    }
}
