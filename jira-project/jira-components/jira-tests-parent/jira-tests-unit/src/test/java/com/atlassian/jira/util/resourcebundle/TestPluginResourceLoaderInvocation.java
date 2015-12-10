package com.atlassian.jira.util.resourcebundle;

import com.atlassian.jira.mock.plugin.MockModuleDescriptor;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.plugin.MockPluginAccessor;
import com.atlassian.jira.mock.plugin.NullModuleDescriptor;
import com.atlassian.jira.mock.plugin.metadata.MockPluginMetadataManager;
import com.atlassian.jira.plugin.MockJiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.language.Language;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.MockLanguageModuleDescriptor;
import com.atlassian.jira.util.collect.IteratorEnumeration;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.isEmpty;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

/**
 * @since v6.2.3
 */
public class TestPluginResourceLoaderInvocation
{
    private final DefaultLanguage defaultLanguage = new DefaultLanguage();
    private final MockPluginAccessor accessor = new MockPluginAccessor();
    private final MockBundleLoader bundleLoader = new MockBundleLoader();
    private final MockPluginMetadataManager pluginMetadataManager = new MockPluginMetadataManager();
    private ResourceLoaderInvocation invocation =
            new PluginResourceLoaderInvocation(accessor, pluginMetadataManager, defaultLanguage, bundleLoader);

    @Test
    public void noPluginsLoadsDefaultLanguage()
    {
        Map<String, String> english1 = defaultLanguage.addResource(Locale.ENGLISH, "key1", "E1");
        Map<String, String> english2 = defaultLanguage.addResource(Locale.ENGLISH, "key2", "E2");
        Map<String, String> taiwan = defaultLanguage.addResource(Locale.TAIWAN, "key1", "T1");

        invocation = invocation.languages().locale(Locale.ENGLISH);

        //English translations should be merged.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(english1).and(english2).merge()));
        assertThat(isEmpty(result.getPlugins()), equalTo(true));

        invocation = invocation.locale(Locale.TAIWAN);
        result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(taiwan).merge()));
        assertThat(isEmpty(result.getPlugins()), equalTo(true));
    }

    @Test
    public void loadWillFindLanguagePacks()
    {
        Plugin langPack = new MockPlugin("langPack");
        final Map<String, String> englishText = createLanguageModule(langPack, Locale.ENGLISH);
        final Map<String, String> frenchText = createLanguageModule(langPack, Locale.FRANCE);

        //Create english french plugin.
        accessor.addPlugin(langPack);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(englishText));
        assertThat(result.getPlugins(), containsInAnyOrder(langPack));

        result = invocation.locale(Locale.FRANCE).load();
        assertThat(result.getData(), equalTo(frenchText));
        assertThat(result.getPlugins(), containsInAnyOrder(langPack));
    }

    @Test
    public void loadWillUseMostSpecificLangPack()
    {
        Plugin englishFrance = new MockPlugin("englishFrance");
        final Map<String, String> englishText = createLanguageModule(englishFrance, Locale.ENGLISH);
        final Map<String, String> franceText = createLanguageModule(englishFrance, Locale.FRANCE, "1", "france");

        Plugin french = new MockPlugin("french");
        final Map<String, String> frenchText = createLanguageModule(french, Locale.FRENCH, "2", "french", "1", "french");
        final Map<String, String> rootText = createLanguageModule(french, Locale.ROOT, "1", "root", "2", "root", "3", "root");

        //Create english french plugin.
        accessor.addPlugins(englishFrance, french);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        //English is just English.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(rootText).and(englishText).merge()));
        assertThat(result.getPlugins(), containsInAnyOrder(englishFrance, french));

        //French is just French.
        result = invocation.locale(Locale.FRENCH).load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(rootText).and(frenchText).merge()));
        assertThat(result.getPlugins(), containsInAnyOrder(french));

        //France is a merge of French and France with France taking precedence.
        result = invocation.locale(Locale.FRANCE).load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(rootText).and(frenchText).and(franceText).merge()));
        assertThat(result.getPlugins(), containsInAnyOrder(french, englishFrance));
    }

    @Test
    public void loadWillUseLangPackAndLanguagePackResources()
    {
        MockPlugin german = new MockPlugin("german");
        final Map<String, String> germanText = createLanguageModule(german, Locale.GERMAN, "german", "true");
        final Map<String, String> germanyText = createLanguageResource(Locale.GERMANY, german, "germany");

        accessor.addPlugins(german);

        invocation = invocation.languages().locale(Locale.GERMANY);

        //Germany should now be a merge of the German langpack and the Germany resource on that langpack with
        // the resource taking precedence.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(germanText).and(germanyText).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(german));
    }

    @Test
    public void loadWillUseLangPackAndLanguagePackResourcesEvenWhenSomeResourcesAreInvalid()
    {
        MockPlugin german = new MockPlugin("german");
        final Map<String, String> germanText = createLanguageModule(german, Locale.GERMAN, "german", "true");
        createLanguageResource(Locale.ENGLISH, german, "english", "not", "here");
        final Map<String, String> germanyText = createLanguageResource(Locale.GERMANY, german, "germany");

        accessor.addPlugins(german);

        invocation = invocation.languages().locale(Locale.GERMANY);

        //Germany should now be a merge of the German langpack and the Germany resource on that langpack with
        // the resource taking precedence. The english resource must be ignored.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(germanText).and(germanyText).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(german));
    }

    @Test
    public void loadIgnoresErrorsInLanguageBundles()
    {
        final MockPlugin error = new MockPlugin("error");
        error.addModuleDescriptor(new ErrorLangPack(error, "broken"));

        final Map<String, String> english1 = createLanguageResource(Locale.ENGLISH, error, "notBroken", "found", "true");
        final NullModuleDescriptor descriptor = createNullModuleDescriptor(error, "goodModuleDescriptor");
        final Map<String, String> notBroken2 = createLanguageResource(Locale.ENGLISH, descriptor, "notBroken2");

        accessor.addPlugins(error);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        final ResourceBundleLoader.LoadResult load = invocation.load();
        assertThat(load.getData(), equalTo(ResourceMerger.of(english1).and(notBroken2).merge()));
        assertThat(load.getPlugins(), Matchers.<Plugin>containsInAnyOrder(error));
    }

    @Test
    public void loadIgnoresErrorsInLanguagePackPlugin()
    {
        MockPlugin error = new MockPlugin("error");
        final ResourceDescriptor descriptor = createLanguageDescriptor(error, "error");

        final Map<String, String> data = ImmutableMap.of("1", "1");
        final MapResourceBundle delegate = new MapResourceBundle(Locale.ENGLISH, data);
        bundleLoader.register(Locale.ENGLISH, error, descriptor, new ErrorResourceBundle(delegate));

        accessor.addPlugins(error);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        assertThat(invocation.load().getData(), equalTo(data));
    }

    @Test
    public void loadIgnoresErrorsWhenLoadingLanguageResourceDescriptors()
    {
        final MockPlugin error = new MockPlugin("error");
        final Map<String, String> notBroken1 = createLanguageResource(Locale.ENGLISH, error, "notBroken", "found", "true");
        final ResourceDescriptor errorDescriptor = createLanguageDescriptor(error, "error");
        final NullModuleDescriptor descriptor = createNullModuleDescriptor(error, "goodModuleDescriptor");
        final Map<String, String> notBroken2 = createLanguageResource(Locale.ENGLISH, descriptor, "notBroken2");

        final MockBundleLoader loader = Mockito.spy(bundleLoader);
        Mockito.doThrow(new RuntimeException("No Load for you."))
                .when(loader).getBundle(Locale.ENGLISH, error, errorDescriptor);

        accessor.addPlugins(error);

        invocation = new PluginResourceLoaderInvocation(accessor, pluginMetadataManager, defaultLanguage, loader)
                .languages().locale(Locale.ENGLISH);

        final ResourceBundleLoader.LoadResult load = invocation.load();
        assertThat(load.getData(), equalTo(ResourceMerger.of(notBroken1).and(notBroken2).merge()));
        assertThat(load.getPlugins(), Matchers.<Plugin>containsInAnyOrder(error));
    }

    @Test
    public void loadWillUsePluginResourcesForLanguage()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        final Map<String, String> pcConfig = createLanguageResource(Locale.GERMANY, projectConfig, "ProjectConfig", "project", "true");

        final NullModuleDescriptor random = createNullModuleDescriptor(projectConfig, "random");
        final Map<String, String> projectConfigModule = createLanguageResource(Locale.GERMANY, random, "ProjectConfigModule");

        accessor.addPlugin(projectConfig);

        //Germany should now be a merge of the Germany resource on the plugin.
        invocation = invocation.languages().locale(Locale.GERMANY);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(pcConfig).and(projectConfigModule).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadWillUsePluginResourcesForLanguageEvenIfSameName()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        final Map<String, String> pcConfig1 = createLanguageResource(Locale.GERMANY, projectConfig, "ProjectConfig", "1", "1");
        final Map<String, String> pcConfig2 = createLanguageResource(Locale.GERMANY, projectConfig, "ProjectConfig", "2", "2");

        accessor.addPlugin(projectConfig);

        //Germany should now be a merge of the Germany resource on the plugin.
        invocation = invocation.languages().locale(Locale.GERMANY);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(pcConfig2).and(pcConfig1).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadWillIgnoreBadPluginResourcesForLanguage()
    {
        //Lookup will only work for GERMANY.
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        createLanguageResource(Locale.GERMANY, projectConfig, "ProjectConfig.de", "ignore", "true");
        final Map<String, String> pcConfigEnglish = createLanguageResource(Locale.ENGLISH, projectConfig, "ProjectConfig");

        //Lookup will be for ENGLISH but will find CANADA.
        final NullModuleDescriptor random = createNullModuleDescriptor(projectConfig, "random");
        createLanguageResource(Locale.ENGLISH, Locale.CANADA, random, "ProjectConfigModule");

        accessor.addPlugin(projectConfig);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        //This lookup must ignore the Germany resource and the Canada resource.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(pcConfigEnglish));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadWillUseMostSpecificResourceForLanguage()
    {
        //Lookup will only work for GERMANY.
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        Map<String, String> germany = createLanguageResource(Locale.GERMANY, projectConfig, "ProjectConfig", "1", "german");

        //Lookup will be for GERMANY but will find GERMAN.
        final NullModuleDescriptor random = createNullModuleDescriptor(projectConfig, "random");
        Map<String, String> german = createLanguageResource(Locale.GERMANY, Locale.GERMAN, random, "ProjectConfigModule.de", "1", "germany", "2", "germany");

        //Lookup will be for GERMANY but will find ROOT.
        Map<String, String> root = createLanguageResource(Locale.GERMANY, Locale.ROOT, random, "1", "ProjectConfigModule.root", "root", "2", "root", "3", "root");

        accessor.addPlugin(projectConfig);

        invocation = invocation.languages().locale(Locale.GERMANY);

        //This lookup will merge GERMANY and GERMAN above with GERMANY taking precedence.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(root).and(german).and(germany).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadMergesLanguageSourcesInCorrectPrecedence()
    {
        //Merging these ENGLISH resources in the correct order will give {"1": "pack", "2": "default", "3": "resource"}.
        Map<String, String> defaultText = defaultLanguage.addResource(Locale.ENGLISH, "1", "default", "2", "default");

        MockPlugin projectConfig = new MockPlugin("projectConfig");
        Map<String, String> resource = createLanguageResource(Locale.ENGLISH, projectConfig, "resource", "1", "resource", "2", "resource", "3", "resource");

        MockPlugin langPack = new MockPlugin("langPack");
        Map<String, String> pack = createLanguageModule(langPack, Locale.ENGLISH, "1", "pack");

        accessor.addPlugins(projectConfig, langPack);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        //Precedence goes langPack, default, resources.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(resource).and(defaultText).and(pack).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig, langPack));
    }

    @Test
    public void loadReportsCorrectLanguagePlugins()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        createLanguageModule(projectConfig, Locale.ENGLISH);
        createLanguageResource(Locale.GERMAN, projectConfig, "ProjectConfig");

        MockPlugin resources = new MockPlugin("resources.en");
        createLanguageResource(Locale.ENGLISH, resources, "pack");

        MockPlugin resources2 = new MockPlugin("german");
        createLanguageResource(Locale.GERMAN, createNullModuleDescriptor(resources2, "lang"), "resources.de");

        MockPlugin resources3 = new MockPlugin("root");
        createLanguageResource(Locale.ENGLISH, Locale.ROOT, createNullModuleDescriptor(resources3, "lang"), "resources.root");

        accessor.addPlugins(projectConfig, resources, resources2, resources3);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        ResourceBundleLoader.LoadResult result = invocation.locale(Locale.ENGLISH).load();
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig, resources, resources3));

        result = invocation.locale(Locale.GERMAN).load();
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig, resources2));
    }

    @Test
    public void loadLanguageIgnoresHelpResources()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        Map<String, String> english = createLanguageModule(projectConfig, Locale.ENGLISH);
        createHelpResource(Locale.ENGLISH, projectConfig, "helpPaths", "help", "true");

        MockPlugin helpPlugin = new MockPlugin("help");
        NullModuleDescriptor helpPathsModule = createNullModuleDescriptor(helpPlugin, "helpPathsModule");
        createHelpResource(Locale.ENGLISH, helpPathsModule, "help", "help", "true");
        createHelpResource(Locale.ENGLISH, helpPlugin, "moduleHelp", "help", "true");

        accessor.addPlugins(projectConfig, helpPlugin);

        invocation = invocation.languages().locale(Locale.ENGLISH);

        //Should only see the english text.
        ResourceBundleLoader.LoadResult result = invocation.locale(Locale.ENGLISH).load();
        assertThat(result.getData(), equalTo(english));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void noPluginsLoadsEmptyHelpPaths()
    {
        defaultLanguage.addResource(Locale.ENGLISH, "key1", "E1");
        defaultLanguage.addResource(Locale.ENGLISH, "key2", "E3");
        defaultLanguage.addResource(Locale.TAIWAN, "key1", "T1");

        invocation = invocation.help().locale(Locale.ENGLISH);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(toMap()));
        assertThat(isEmpty(result.getPlugins()), equalTo(true));

        invocation = invocation.locale(Locale.TAIWAN);
        result = invocation.load();
        assertThat(result.getData(), equalTo(toMap()));
        assertThat(isEmpty(result.getPlugins()), equalTo(true));
    }

    @Test
    public void loadHelpFindsCorrectLanguage()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        Map<String, String> englishHelp = createHelpResource(Locale.ENGLISH, projectConfig, "englishHelp");

        MockPlugin helpPlugin = new MockPlugin("help");
        Map<String, String> germanHelp = createHelpResource(Locale.GERMAN, helpPlugin, "germanHelp");
        NullModuleDescriptor helpPathsModule = createNullModuleDescriptor(helpPlugin, "helpPathsModule");
        Map<String, String> frenchHelp = createHelpResource(Locale.FRENCH, helpPathsModule, "frenchHelp");

        accessor.addPlugins(projectConfig, helpPlugin);

        invocation = invocation.help().locale(Locale.ENGLISH);

        ResourceBundleLoader.LoadResult result = invocation.locale(Locale.ENGLISH).load();
        assertThat(result.getData(), equalTo(englishHelp));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));

        result = invocation.locale(Locale.GERMAN).load();
        assertThat(result.getData(), equalTo(germanHelp));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(helpPlugin));

        result = invocation.locale(Locale.FRENCH).load();
        assertThat(result.getData(), equalTo(frenchHelp));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(helpPlugin));
    }

    @Test
    public void loadWillUseMostSpecificResourceForHelp()
    {
        //Lookup will only work for GERMANY.
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        Map<String, String> germany = createHelpResource(Locale.GERMANY, projectConfig, "ProjectConfig", "1", "german");

        //Lookup will be for GERMANY but will find GERMAN.
        final NullModuleDescriptor random = createNullModuleDescriptor(projectConfig, "random");
        Map<String, String> german = createHelpResource(Locale.GERMANY, Locale.GERMAN, random, "ProjectConfigModule.de", "1", "germany", "2", "germany");

        //Lookup will be for GERMANY but will find ROOT.
        Map<String, String> root = createHelpResource(Locale.GERMANY, Locale.ROOT, random, "1", "ProjectConfigModule.root", "root", "2", "root", "3", "root");

        accessor.addPlugin(projectConfig);

        invocation = invocation.help().locale(Locale.GERMANY);

        //This lookup will merge GERMANY and GERMAN above with GERMANY taking precedence.
        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(root).and(german).and(germany).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadReportsCorrectHelpPlugins()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        createHelpResource(Locale.GERMAN, projectConfig, "ProjectConfig");

        MockPlugin resources = new MockPlugin("resources.en");
        createHelpResource(Locale.ENGLISH, resources, "pack");

        MockPlugin resources2 = new MockPlugin("german");
        createHelpResource(Locale.GERMAN, createNullModuleDescriptor(resources2, "help"), "help.de");

        MockPlugin resources3 = new MockPlugin("root");
        createHelpResource(Locale.ENGLISH, Locale.ROOT, createNullModuleDescriptor(resources3, "help"), "help.root");

        accessor.addPlugins(projectConfig, resources, resources2, resources3);

        invocation = invocation.help().locale(Locale.ENGLISH);

        ResourceBundleLoader.LoadResult result = invocation.locale(Locale.ENGLISH).load();
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(resources, resources3));

        result = invocation.locale(Locale.GERMAN).load();
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig, resources2));
    }

    @Test
    public void loadLetsPluginsOverwriteSystemHelpPathsInRootLocale()
    {
        MockPlugin projectPlugin = new MockPlugin("projectConfig");
        final Map<String, String> config = createHelpResource(Locale.ROOT, projectPlugin, "ProjectConfig",
                "my.location", "PLUGIN");

        MockPlugin systemPlugin = new MockPlugin("systemhelp");
        pluginMetadataManager.addSystemPlugin(systemPlugin);
        final Map<String, String> system = createHelpResource(Locale.ROOT, systemPlugin, "systemhelp",
                "my.location", "SYSTEM");

        accessor.addPlugins(projectPlugin, systemPlugin);

        invocation = invocation.help().locale(Locale.ROOT);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(systemPlugin, projectPlugin));
        assertThat(result.getData(), equalTo(ResourceMerger.of(system).and(config).merge()));
    }

    @Test
    public void loadHelpIgnoresHelpLanguageResources()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        //Lang module to ignore.
        createLanguageModule(projectConfig, Locale.ENGLISH);
        //Lang resource to ignore.
        createLanguageResource(Locale.ENGLISH, projectConfig, "projectConfigLanguage", "language", "true");
        Map<String, String> helpResource = createHelpResource(Locale.ENGLISH, projectConfig, "projectConfigHelp");

        //More lang resources to ignore.
        MockPlugin helpPlugin = new MockPlugin("lang");
        NullModuleDescriptor helpPathsModule = createNullModuleDescriptor(helpPlugin, "langModule");
        createLanguageResource(Locale.ENGLISH, helpPathsModule, "langPluginResource", "language", "true");
        createLanguageResource(Locale.ENGLISH, helpPlugin, "langModuleResource", "language", "true");

        accessor.addPlugins(projectConfig, helpPlugin);

        invocation = invocation.help().locale(Locale.ENGLISH);

        //Should only see the english text.
        ResourceBundleLoader.LoadResult result = invocation.locale(Locale.ENGLISH).load();
        assertThat(result.getData(), equalTo(helpResource));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadIgnoresErrorsInHelpBundles()
    {
        MockPlugin error = new MockPlugin("error");
        final ResourceDescriptor descriptor = createHelpDescriptor(error, "error");

        final Map<String, String> data = ImmutableMap.of("1", "1");
        final MapResourceBundle delegate = new MapResourceBundle(Locale.ENGLISH, data);
        bundleLoader.register(Locale.ENGLISH, error, descriptor, new ErrorResourceBundle(delegate));

        accessor.addPlugins(error);

        invocation = invocation.help().locale(Locale.ENGLISH);

        assertThat(invocation.load().getData(), equalTo(data));
    }

    @Test
    public void loadWillIgnoreNullStringsForLanguages()
    {
        final MockPlugin projectConfig = new MockPlugin("projectConfig");
        final Map<String, String> pcConfig1 = createLanguageResource(Locale.GERMANY, projectConfig, "ProjectConfig", null, "value");
        final Map<String, String> languageModule = createLanguageModule(projectConfig, Locale.GERMANY, "     ", null);
        final NullModuleDescriptor other = createNullModuleDescriptor(projectConfig, "other");
        final Map<String, String> projectModule = createLanguageResource(Locale.GERMANY, Locale.GERMAN, other, "ProjectModule", "", "");

        accessor.addPlugin(projectConfig);

        //Germany should now be a merge of the Germany resource on the plugin.
        invocation = invocation.languages().locale(Locale.GERMANY);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(projectModule)
                .and(pcConfig1)
                .and(languageModule)
                .removeNulls()
                .merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadDoesNotStripForLanguages()
    {
        final MockPlugin projectConfig = new MockPlugin("projectConfig");
        final Map<String, String> pcConfig1 = createLanguageResource(Locale.GERMANY, projectConfig, "ProjectConfig", " 1  ", "    1     ");
        final Map<String, String> languageModule = createLanguageModule(projectConfig, Locale.GERMANY, "  2   ", "\r\n\t   2  \r\n");
        final NullModuleDescriptor other = createNullModuleDescriptor(projectConfig, "other");
        final Map<String, String> projectModule = createLanguageResource(Locale.GERMANY, Locale.GERMAN, other, "ProjectModule", "3", " 3 ");

        accessor.addPlugin(projectConfig);

        //Germany should now be a merge of the Germany resource on the plugin.
        invocation = invocation.languages().locale(Locale.GERMANY);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(projectModule)
                .and(pcConfig1)
                .and(languageModule)
                .merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadIgnoresErrorsWhenLoadingHelpResourceDescriptors()
    {
        final MockPlugin error = new MockPlugin("error");
        final Map<String, String> notBroken1 = createHelpResource(Locale.ENGLISH, error, "notBroken", "found", "true");
        final ResourceDescriptor errorDescriptor = createHelpDescriptor(error, "error");
        final NullModuleDescriptor descriptor = createNullModuleDescriptor(error, "goodModuleDescriptor");
        final Map<String, String> notBroken2 = createHelpResource(Locale.ENGLISH, descriptor, "notBroken2");

        final MockBundleLoader loader = Mockito.spy(bundleLoader);
        Mockito.doThrow(new RuntimeException("No Load for you."))
                .when(loader).getBundle(Locale.ENGLISH, error, errorDescriptor);

        accessor.addPlugins(error);

        invocation = new PluginResourceLoaderInvocation(accessor, pluginMetadataManager, defaultLanguage, loader)
                .help().locale(Locale.ENGLISH);

        final ResourceBundleLoader.LoadResult load = invocation.load();
        assertThat(load.getData(), equalTo(ResourceMerger.of(notBroken1).and(notBroken2).merge()));
        assertThat(load.getPlugins(), Matchers.<Plugin>containsInAnyOrder(error));
    }

    @Test
    public void loadWillUsePluginResourcesForHelpEvenIfSameName()
    {
        MockPlugin projectConfig = new MockPlugin("projectConfig");
        final Map<String, String> pcConfig1 = createHelpResource(Locale.GERMANY, projectConfig, "ProjectConfig", "1", "1");
        final Map<String, String> pcConfig2 = createHelpResource(Locale.GERMANY, projectConfig, "ProjectConfig", "2", "2");

        accessor.addPlugin(projectConfig);

        //Germany should now be a merge of the Germany resource on the plugin.
        invocation = invocation.help().locale(Locale.GERMANY);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(pcConfig2).and(pcConfig1).merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadWillIgnoreNullsForHelp()
    {
        final MockPlugin projectConfig = new MockPlugin("projectConfig");
        final Map<String, String> pcConfig1 = createHelpResource(Locale.GERMANY, projectConfig, "ProjectConfig", null, null);
        final NullModuleDescriptor other = createNullModuleDescriptor(projectConfig, "other");
        final Map<String, String> projectModule = createHelpResource(Locale.GERMANY, Locale.GERMAN, other, "ProjectModule", "    ", null, "abc", "value");

        accessor.addPlugin(projectConfig);

        //Germany should now be a merge of the Germany resource on the plugin.
        invocation = invocation.help().locale(Locale.GERMANY);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(projectModule)
                .and(pcConfig1)
                .removeNulls()
                .merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    @Test
    public void loadHelpWillAllowsSpacesAroundKeysAndValues()
    {
        final MockPlugin projectConfig = new MockPlugin("projectConfig");
        final Map<String, String> pcConfig1 = createHelpResource(Locale.GERMANY, projectConfig, "ProjectConfig", " 1  ", "    1     ");
        final NullModuleDescriptor other = createNullModuleDescriptor(projectConfig, "other");
        final Map<String, String> projectModule = createHelpResource(Locale.GERMANY, Locale.GERMAN, other, "ProjectModule", "3", " 3 ");

        accessor.addPlugin(projectConfig);

        //Germany should now be a merge of the Germany resource on the plugin.
        invocation = invocation.help().locale(Locale.GERMANY);

        ResourceBundleLoader.LoadResult result = invocation.load();
        assertThat(result.getData(), equalTo(ResourceMerger.of(projectModule)
                .and(pcConfig1)
                .merge()));
        assertThat(result.getPlugins(), Matchers.<Plugin>containsInAnyOrder(projectConfig));
    }

    private Map<String, String> createLanguageModule(final Plugin plugin, final Locale locale, String... pairs)
    {
        final MockLanguageModuleDescriptor moduleDescriptor = new MockLanguageModuleDescriptor(plugin, locale.toString());
        moduleDescriptor.setLanguage(locale.getLanguage());
        moduleDescriptor.setCountry(locale.getCountry());
        moduleDescriptor.setEncoding("UTF-8");

        plugin.addModuleDescriptor(moduleDescriptor);
        return bundleLoader.register(moduleDescriptor, pairs);
    }

    private Map<String, String> createHelpResource(final Locale locale, final MockPlugin plugin, final String name, String... pairs)
    {
        final ResourceDescriptor i18nResource = createHelpDescriptor(plugin, name);
        return bundleLoader.register(locale, plugin, i18nResource, pairs);
    }

    private Map<String, String> createLanguageResource(final Locale locale, final MockPlugin plugin, final String name, String... pairs)
    {
        final ResourceDescriptor i18nResource = createLanguageDescriptor(plugin, name);
        return bundleLoader.register(locale, plugin, i18nResource, pairs);
    }

    private ResourceDescriptor createLanguageDescriptor(final MockPlugin plugin, final String name)
    {
        return plugin.createI18nResource(name, format("resource.%s", name));
    }

    private ResourceDescriptor createHelpDescriptor(final MockPlugin plugin, final String name)
    {
        return plugin.createHelpResource(name, format("help.%s", name));
    }

    private Map<String, String> createLanguageResource(final Locale locale, final MockModuleDescriptor<?> moduleDescriptor, final String name, String... pairs)
    {
        return createLanguageResource(locale, locale, moduleDescriptor, name, pairs);
    }

    private Map<String, String> createLanguageResource(final Locale target, Locale actual, final MockModuleDescriptor<?> moduleDescriptor, final String name, String... pairs)
    {
        final ResourceDescriptor i18nResource = createLanguageDescriptor(moduleDescriptor, name);
        return bundleLoader.register(target, actual, moduleDescriptor.getPlugin(), i18nResource, pairs);
    }

    private Map<String, String> createHelpResource(Locale locale, final MockModuleDescriptor<?> moduleDescriptor, final String name, String... pairs)
    {
        return createHelpResource(locale, locale, moduleDescriptor, name, pairs);
    }

    private Map<String, String> createHelpResource(final Locale target, Locale actual, final MockModuleDescriptor<?> moduleDescriptor, final String name, String... pairs)
    {
        final ResourceDescriptor i18nResource = createHelpDescriptor(moduleDescriptor, name);
        return bundleLoader.register(target, actual, moduleDescriptor.getPlugin(), i18nResource, pairs);
    }

    private ResourceDescriptor createHelpDescriptor(final MockModuleDescriptor<?> moduleDescriptor, final String name)
    {
        return moduleDescriptor.createHelpResource(name, format("resource.%s", name));
    }

    private ResourceDescriptor createLanguageDescriptor(final MockModuleDescriptor<?> moduleDescriptor, final String name)
    {
        return moduleDescriptor.createI18nResource(name, format("resource.%s", name));
    }

    private NullModuleDescriptor createNullModuleDescriptor(final MockPlugin plugin, final String name)
    {
        final NullModuleDescriptor nullModuleDescriptor = new NullModuleDescriptor(plugin, name);
        plugin.addModuleDescriptor(nullModuleDescriptor);

        return nullModuleDescriptor;
    }

    private static class DefaultLanguage implements PluginResourceLoaderInvocation.DefaultLanguageSupplier
    {
        private final Multimap<Locale, ResourceBundle> mappings = ArrayListMultimap.create();

        private DefaultLanguage addResource(Locale locale, ResourceBundle bundle)
        {
            mappings.put(locale, bundle);
            return this;
        }

        private Map<String, String> addResource(Locale locale, String...pairs)
        {
            Map<String, String> text = Collections.unmodifiableMap(toMap(pairs));
            addResource(locale, new MapResourceBundle(locale, text));
            return text;
        }

        @Override
        public Iterable<ResourceBundle> apply(@Nullable final Locale input)
        {
            return mappings.get(input);
        }
    }

    private static class MockBundleLoader implements PluginResourceLoaderInvocation.BundleLoader
    {
        private final Map<Object, ResourceBundle> translations = Maps.newHashMap();

        @Override
        public ResourceBundle getBundle(final Locale targetLocale, final Plugin plugin, final ResourceDescriptor descriptor)
        {
            return lookupBundle(targetLocale, plugin, descriptor);
        }

        @Override
        public ResourceBundle getBundle(final LanguageModuleDescriptor descriptor)
        {
            return lookupBundle(descriptor);
        }

        private Map<String, String> register(Locale target, Locale actual, Plugin plugin, ResourceDescriptor descriptor, String...pairs)
        {
            final Map<String, String> bundleMap = toMap(pairs);
            final String key = descriptor.getLocation();
            bundleMap.put(key + ".type", "RESOURCE");
            bundleMap.put(key + ".resourceType", descriptor.getType());
            bundleMap.put(key + ".locale", toString(actual));
            bundleMap.put("locale", toString(actual));

            final Map<String, String> result = Collections.unmodifiableMap(bundleMap);
            register(target, plugin, descriptor, new MapResourceBundle(actual, result));
            return result;
        }

        private void register(Locale locale, Plugin plugin, ResourceDescriptor descriptor, ResourceBundle bundle)
        {
            translations.put(getKey(locale, plugin, descriptor), bundle);
        }

        private Map<String, String> register(Locale locale, Plugin plugin, ResourceDescriptor descriptor, String...pairs)
        {
            return register(locale, locale, plugin, descriptor, pairs);
        }

        private Map<String, String> register(final LanguageModuleDescriptor descriptor, String...pairs)
        {
            final Locale locale = descriptor.getModule().getLocale();

            final Map<String, String> bundleMap = toMap(pairs);
            final String key = descriptor.getCompleteKey();
            bundleMap.put(key + ".type", "MODULE");
            bundleMap.put(key + ".locale", toString(locale));
            bundleMap.put("locale", toString(locale));

            final Map<String, String> result = Collections.unmodifiableMap(bundleMap);
            translations.put(getKey(descriptor), new MapResourceBundle(locale, result));
            return result;
        }

        private static Object getKey(Object...objects)
        {
            List<Object> keys = Lists.newArrayList();
            for (Object object : objects)
            {
                if (object instanceof ResourceDescriptor)
                {
                    object = new SameObject(object);
                }
                keys.add(object);
            }
            return ImmutableList.copyOf(keys);
        }

        private ResourceBundle lookupBundle(Object... keys)
        {
            Object key = getKey(keys);
            ResourceBundle text = translations.get(key);
            if (text == null)
            {
                throw new MissingResourceException("Unable to find resource with key: " + key, null, null);
            }
            return text;
        }

        private static String toString(Locale locale)
        {
            return Locale.ROOT.equals(locale) ? "<ROOT>" : locale.toString();
        }
    }

    private static class ResourceMerger
    {
        private Map<String, String> map = Maps.newHashMap();

        private ResourceMerger(Map<String, String> data)
        {
            map.putAll(data);
        }

        private ResourceMerger and(Map<String, String> data)
        {
            map.putAll(data);
            return this;
        }

        private static ResourceMerger of(Map<String, String> data)
        {
            return new ResourceMerger(data);
        }

        private ResourceMerger removeNulls()
        {
            for (Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext(); )
            {
                final Map.Entry<String, String> entry = iterator.next();
                if (entry.getKey() == null || entry.getValue() == null)
                {
                    iterator.remove();
                }
            }
            return this;
        }

        private Map<String, String> merge()
        {
            return map;
        }
    }

    private static class MapResourceBundle extends ResourceBundle
    {
        private final Map<String, String> resources;
        private final Locale locale;

        private MapResourceBundle(Locale locale, Map<String, String> map)
        {
            this.resources = map;
            this.locale = locale;
        }

        @Override
        protected Object handleGetObject(final String key)
        {
            return resources.get(key);
        }

        @Override
        public Enumeration<String> getKeys()
        {
            return IteratorEnumeration.fromIterable(resources.keySet());
        }

        @Override
        public Locale getLocale()
        {
            return locale;
        }
    }

    private static Map<String, String> toMap(String...pairs)
    {
        Map<String, String> data = Maps.newHashMap();
        if ((pairs.length & 0x1) == 1)
        {
            throw new IllegalArgumentException("Need a even number of pairs.");
        }

        for (int i = 0; i < pairs.length;)
        {
            String key = pairs[i++];
            String value = pairs[i++];
            data.put(key, value);
        }
        return data;
    }

    private static class ErrorResourceBundle extends ResourceBundle
    {
        private static final String ERROR_KEY = "i.want.to.cause.an.error.on.get.and.i.should.not.be.in.the.bundle";

        private final ResourceBundle delegate;

        private ErrorResourceBundle(final ResourceBundle delegate)
        {
            this.delegate = delegate;
        }

        @Override
        protected Object handleGetObject(final String key)
        {
            if (ERROR_KEY.equals(key))
            {
                //This will trigger an exception.
                return null;
            }
            return delegate.getObject(key);
        }

        @Override
        public Enumeration<String> getKeys()
        {
            final List<String> keys = Lists.newArrayList(Collections.list(delegate.getKeys()));
            keys.add(ERROR_KEY);
            return Collections.enumeration(keys);
        }

        @Override
        public Locale getLocale()
        {
            return delegate.getLocale();
        }
    }

    private static class ErrorLangPack extends MockJiraResourcedModuleDescriptor<Language> implements LanguageModuleDescriptor
    {
        public ErrorLangPack(Plugin plugin, String key)
        {
            super(Language.class, plugin, key);
        }

        @Override
        public String getResourceBundleName()
        {
            return error();
        }

        @Override
        public String getEncoding()
        {
            return error();
        }

        @Override
        public void setEncoding(final String encoding)
        {
            error();
        }

        @Override
        public String getLanguage()
        {
            return error();
        }

        @Override
        public void setLanguage(final String language)
        {
            error();
        }

        @Override
        public String getCountry()
        {
            return error();
        }

        @Override
        public void setCountry(final String country)
        {
            error();
        }

        @Override
        public String getVariant()
        {
            return error();
        }

        @Override
        public void setVariant(final String variant)
        {
            error();
        }

        @Override
        public Language getModule()
        {
            return error();
        }

        private static <T> T error()
        {
            throw new RuntimeException("Error");
        }
    }

    private static class SameObject
    {
        private final Object object;

        private SameObject(final Object object)
        {
            this.object = object;
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(this.object);
        }

        @SuppressWarnings ("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(final Object other)
        {
            return other instanceof SameObject && ((SameObject) other).object == this.object;
        }

        @Override
        public String toString()
        {
            return object.toString();
        }
    }
}
