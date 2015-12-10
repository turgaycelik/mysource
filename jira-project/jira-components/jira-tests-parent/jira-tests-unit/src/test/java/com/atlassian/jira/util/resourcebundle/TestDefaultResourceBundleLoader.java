package com.atlassian.jira.util.resourcebundle;

import com.google.common.base.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Locale;

import static com.atlassian.jira.util.resourcebundle.MockResourceLoaderInvocation.getState;
import static com.atlassian.jira.util.resourcebundle.ResourceLoaderInvocation.Mode.HELP;
import static com.atlassian.jira.util.resourcebundle.ResourceLoaderInvocation.Mode.I18N;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.3
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultResourceBundleLoader
{
    public Supplier<ResourceLoaderInvocation> supplier = new Supplier<ResourceLoaderInvocation>()
    {
        @Override
        public ResourceLoaderInvocation get()
        {
            return new MockResourceLoaderInvocation();
        }
    };

    @Test
    public void localeChangesLocale()
    {
        ResourceBundleLoader english = new DefaultResourceBundleLoader(Locale.ENGLISH, true, supplier);
        assertThat(english.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));

        ResourceBundleLoader germanEnglish = english.locale(Locale.GERMAN);

        assertThat(english.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(germanEnglish.load().getData(), equalTo(getState(Locale.GERMAN, I18N)));

        ResourceBundleLoader englishGermanEnglish = english.locale(Locale.ENGLISH);

        assertThat(englishGermanEnglish.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(english.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(germanEnglish.load().getData(), equalTo(getState(Locale.GERMAN, I18N)));

        ResourceBundleLoader englishEnglish = english.locale(Locale.ENGLISH);

        assertThat(englishGermanEnglish.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(english.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(englishEnglish.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(germanEnglish.load().getData(), equalTo(getState(Locale.GERMAN, I18N)));

        ResourceBundleLoader germanGermanEnglish = germanEnglish.locale(Locale.GERMAN);

        assertThat(englishGermanEnglish.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(english.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(englishEnglish.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(germanEnglish.load().getData(), equalTo(getState(Locale.GERMAN, I18N)));
        assertThat(germanGermanEnglish.load().getData(), equalTo(getState(Locale.GERMAN, I18N)));
    }

    @Test
    public void modeOfLookupCanBeChanged()
    {
        ResourceBundleLoader langLoader = new DefaultResourceBundleLoader(Locale.ENGLISH, true, supplier);
        assertThat(langLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));

        ResourceBundleLoader helpLangLoader = langLoader.helpText();

        assertThat(langLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(helpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, HELP)));

        ResourceBundleLoader langHelpLangLoader = helpLangLoader.i18n();

        assertThat(langLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(langHelpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(helpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, HELP)));

        ResourceBundleLoader langLangLoader = langLoader.i18n();

        assertThat(langLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(langHelpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(helpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, HELP)));
        assertThat(langLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));

        ResourceBundleLoader helpHelpLangLoader = langLoader.helpText();

        assertThat(langLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(langHelpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
        assertThat(helpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, HELP)));
        assertThat(helpHelpLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, HELP)));
        assertThat(langLangLoader.load().getData(), equalTo(getState(Locale.ENGLISH, I18N)));
    }
}
