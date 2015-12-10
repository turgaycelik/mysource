package com.atlassian.jira.help;

import com.atlassian.jira.mock.MockFeatureManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.resourcebundle.MockResourceBundleLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * @since v6.2.4
 */
public class DefaultHelpUrlsLoaderTest
{
    private static final String DEFAULT_KEY = "default";
    private static final String DEFAULT_URL = "https://confluence.atlassian.com/display/JIRA/";

    private MockResourceBundleLoader loader = new MockResourceBundleLoader(Locale.ENGLISH);
    private MockSimpleAuthenticationContext ctx =
            new MockSimpleAuthenticationContext(new MockUser("bbain"), Locale.ENGLISH, new NoopI18nHelper(Locale.ENGLISH));
    private MockFeatureManager featureManager = new MockFeatureManager();
    private MockLocalHelpUrls internalHelpUrlLoader = new MockLocalHelpUrls();
    private I18nHelper.BeanFactory factory = new NoopI18nFactory();
    private MockHelpUrlsParser urlParser = new MockHelpUrlsParser();
    private DefaultHelpUrlsLoader defaultHelpUrlLoader = new DefaultHelpUrlsLoader(loader, ctx, featureManager,
            internalHelpUrlLoader, factory, urlParser);

    @Test
    public void getCurrentUserKeyReturnsCorrectState()
    {
        assertKeyForCurrentUser(Locale.ENGLISH, true);
        assertKeyForCurrentUser(Locale.GERMAN, false);
    }

    @Test
    public void loadNoDefaultsAndNoPluginsReturnsABasicHelpPath()
    {
        HelpUrls load = defaultHelpUrlLoader.apply(new DefaultHelpUrlsLoader.LoaderKey(Locale.ENGLISH, false));

        assertThat(load.getUrlKeys(), containsInAnyOrder(DEFAULT_KEY));
        HelpUrlMatcher defaultMatcher = defaultDefaultMatcher();
        assertThat(load.getUrl(DEFAULT_KEY), defaultMatcher);
        assertThat(load.getDefaultUrl(), defaultMatcher);
        assertThat(load.getUrl("other"), defaultMatcher);
    }

    @Test
    public void loadReadsInternalUrls()
    {
        MockHelpUrl one = internalHelpUrlLoader.add("one");
        MockHelpUrl two = internalHelpUrlLoader.add("two");

        HelpUrls load = defaultHelpUrlLoader.apply(new DefaultHelpUrlsLoader.LoaderKey(Locale.ENGLISH, false));
        HelpUrlMatcher defaultMatcher = defaultDefaultMatcher();

        assertThat(load.getUrlKeys(), containsInAnyOrder(DEFAULT_KEY, one.getKey(), two.getKey()));
        assertThat(load.getUrl(DEFAULT_KEY), defaultMatcher);
        assertThat(load.getUrl("three"), defaultMatcher);
        assertThat(load.getUrl(one.getKey()), new HelpUrlMatcher(one));
        assertThat(load.getUrl(two.getKey()), new HelpUrlMatcher(two));
    }

    @Test
    public void loadReadExternalUrls()
    {
        urlParser.defaultUrl("defaultUrl", "defaultTitle");
        urlParser.createUrlOd("onDemand", "OD+Demand");
        MockHelpUrl btf = urlParser.createUrl("btf", "BTF");

        loader.registerHelp(ImmutableMap.of(btf.getKey(), "btfAlt"));

        HelpUrls load = defaultHelpUrlLoader.apply(new DefaultHelpUrlsLoader.LoaderKey(Locale.ENGLISH, false));

        assertThat(load.getUrlKeys(), containsInAnyOrder(DEFAULT_KEY, btf.getKey()));
        HelpUrlMatcher defaultMatcher = new HelpUrlMatcher(urlParser.getGeneratedDefault(null));
        assertThat(load.getUrl(DEFAULT_KEY), defaultMatcher);
        assertThat(load.getUrl("three"), defaultMatcher);
        assertThat(load.getUrl(btf.getKey()), new HelpUrlMatcher(urlParser.getGeneratedUrl(btf, "btfAlt")));
    }

    @Test
    public void loadReadExternalUrlsOd()
    {
        urlParser.defaultUrl("defaultUrl", "defaultTitle");
        MockHelpUrl onDemand = urlParser.createUrlOd("onDemand", "OD+Demand");
        MockHelpUrl btf = urlParser.createUrl("btf", "BTF");

        loader.registerHelp(ImmutableMap.of(btf.getKey(), "btfAlt", onDemand.getKey(), "odAlt", "default", "defaultAlt"));

        HelpUrls load = defaultHelpUrlLoader.apply(new DefaultHelpUrlsLoader.LoaderKey(Locale.ENGLISH, true));

        assertThat(load.getUrlKeys(), containsInAnyOrder(DEFAULT_KEY, btf.getKey(), onDemand.getKey()));
        HelpUrlMatcher defaultMatcher = new HelpUrlMatcher(urlParser.getGeneratedDefault("defaultAlt"));
        assertThat(load.getUrl(DEFAULT_KEY), defaultMatcher);
        assertThat(load.getUrl("three"), defaultMatcher);
        assertThat(load.getUrl(btf.getKey()), new HelpUrlMatcher(urlParser.getGeneratedUrl(btf, "btfAlt")));
        assertThat(load.getUrl(onDemand.getKey()), new HelpUrlMatcher(urlParser.getGeneratedUrl(onDemand, "odAlt")));
    }

    @Test
    public void urlLoadingOrder()
    {
        List<HelpUrl> urls = Lists.newArrayList();

        //This HelpUrl is overwritten by a plugin.
        MockHelpUrl internalOne = new MockHelpUrl().setKey("one").setUrl("internal");

        //This HelpUrl is not overwritten by a plugin.
        MockHelpUrl two = new MockHelpUrl().setKey("two").setUrl("external");
        MockHelpUrl one = new MockHelpUrl().setKey("one").setUrl("external");

        urls.add(two);
        urls.add(one);

        internalHelpUrlLoader.add(internalOne).add(two);
        urlParser.defaultUrl("defaultUrl", "defaultTitle");
        urlParser.register(one);
        loader.helpText().locale(Locale.ENGLISH).registerHelp(ImmutableMap.of("one", "one"));

        HelpUrls load = defaultHelpUrlLoader.apply(new DefaultHelpUrlsLoader.LoaderKey(Locale.ENGLISH, false));

        assertThat(load.getUrlKeys(), containsInAnyOrder(DEFAULT_KEY, one.getKey(), two.getKey()));
        HelpUrlMatcher defaultMatcher = new HelpUrlMatcher(urlParser.getGeneratedDefault(null));
        assertThat(load.getUrl(DEFAULT_KEY), defaultMatcher);
        assertThat(load.getUrl("three"), defaultMatcher);
        assertThat(load.getUrl(one.getKey()), new HelpUrlMatcher(urlParser.getGeneratedUrl(one, "one")));
        assertThat(load.getUrl(two.getKey()), new HelpUrlMatcher(two));
    }

    private HelpUrlMatcher defaultDefaultMatcher()
    {
        return new HelpUrlMatcher()
                .key(DEFAULT_KEY)
                .url(DEFAULT_URL)
                .title(NoopI18nHelper.makeTranslation("jira.help.paths.help.title"));
    }

    private void assertKeyForCurrentUser(Locale locale, boolean onDemand)
    {
        featureManager.setOnDemand(onDemand);
        ctx.setLocale(locale);

        HelpUrlsLoader.HelpUrlsLoaderKey key = defaultHelpUrlLoader.keyForCurrentUser();

        assertThat(key, Matchers.instanceOf(DefaultHelpUrlsLoader.LoaderKey.class));
        assertThat(key, Matchers.<HelpUrlsLoader.HelpUrlsLoaderKey>equalTo(new DefaultHelpUrlsLoader.LoaderKey(locale, onDemand)));
    }
}
