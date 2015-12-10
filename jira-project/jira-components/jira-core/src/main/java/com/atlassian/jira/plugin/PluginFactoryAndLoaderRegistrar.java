package com.atlassian.jira.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.Application;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.factories.PluginFactory;
import com.atlassian.plugin.factories.XmlDynamicPluginFactory;
import com.atlassian.plugin.loaders.BundledPluginLoader;
import com.atlassian.plugin.loaders.DirectoryPluginLoader;
import com.atlassian.plugin.loaders.PluginLoader;
import com.atlassian.plugin.loaders.SinglePluginLoader;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.atlassian.plugin.osgi.factory.OsgiBundleFactory;
import com.atlassian.plugin.osgi.factory.OsgiPluginFactory;
import com.atlassian.plugin.osgi.factory.RemotablePluginFactory;
import com.atlassian.plugin.osgi.factory.UnloadableStaticPluginFactory;
import com.atlassian.plugin.servlet.ServletContextFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A simple registrar of plugin factories and plugin loaders
 *
 * @since v4.4
 */
class PluginFactoryAndLoaderRegistrar
{
    private static final Logger log = Logger.getLogger(PluginFactoryAndLoaderRegistrar.class);

    private static final String BUNDLED_PLUGIN_LOCATION = "/WEB-INF/atlassian-bundled-plugins";

    private final PluginEventManager pluginEventManager;
    private final OsgiContainerManager osgiContainerManager;
    private final PluginPath pathFactory;
    private final ServletContextFactory servletContextFactory;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraFailedPluginTracker jiraFailedPluginTracker;
    private final JiraProperties jiraSystemProperties;

    PluginFactoryAndLoaderRegistrar(PluginEventManager pluginEventManager, OsgiContainerManager osgiContainerManager,
            PluginPath pathFactory, ServletContextFactory servletContextFactory, BuildUtilsInfo buildUtilsInfo,
            JiraFailedPluginTracker jiraFailedPluginTracker, final JiraProperties jiraSystemProperties)
    {
        this.pluginEventManager = pluginEventManager;
        this.osgiContainerManager = osgiContainerManager;
        this.pathFactory = pathFactory;
        this.servletContextFactory = servletContextFactory;
        this.buildUtilsInfo = buildUtilsInfo;
        this.jiraFailedPluginTracker = jiraFailedPluginTracker;
        this.jiraSystemProperties = jiraSystemProperties;
    }

    /**
     * This allows every plugin found to be loaded
     *
     * @return a list of plugin factories which is in fact a singleton of the {@link MasterPluginFactory}
     */
    public List<PluginFactory> getDefaultPluginFactories()
    {
        final ArrayList<Pattern> everyPluginWhiteList = Lists.newArrayList(Pattern.compile(".*"));
        return getDefaultPluginFactories(everyPluginWhiteList);
    }

    /**
     * This allows only a select list of plugins found to be loaded
     *
     * @param pluginWhitelist the whitelist of plugins deployment units that are allowed to be loaded
     * @return a list of plugin factories which is in fact a singleton of the {@link MasterPluginFactory}
     */
    public List<PluginFactory> getDefaultPluginFactories(final List<Pattern> pluginWhitelist)
    {
        final Set<Application> jiraApplications = jiraApplications(buildUtilsInfo);

        // this loads Atlassian Plugins (as OSGi bundles) transforming them into full OSGi bundles if necessary
        final PluginFactory osgiPluginFactory = new OsgiPluginFactory(
                PluginAccessor.Descriptor.FILENAME,
                jiraApplications,
                pathFactory.getOsgiPersistentCache(),
                osgiContainerManager,
                pluginEventManager);

        // this loads OSGi bundles
        final PluginFactory osgiBundleFactory = new OsgiBundleFactory(osgiContainerManager, pluginEventManager);

        // this loads version 3 plugins
        final RemotablePluginFactory remotablePluginFactory = new RemotablePluginFactory(
                PluginAccessor.Descriptor.FILENAME,
                jiraApplications,
                osgiContainerManager,
                pluginEventManager);

        // this loads just-XML-files that describe a plugin
        final PluginFactory xmlDynamicFactory = new XmlDynamicPluginFactory(jiraApplications);

        // this loads "UnloadablePlugins" in the case that the user drops a Plugins 1 plugin into the plugins 2 installation directory.
        final UnloadableStaticPluginFactory unloadableStaticPluginFactory = new UnloadableStaticPluginFactory(PluginAccessor.Descriptor.FILENAME);

        final List<PluginFactory> pluginFactories = ImmutableList.of(osgiPluginFactory, osgiBundleFactory, remotablePluginFactory, xmlDynamicFactory, unloadableStaticPluginFactory);
        final MasterPluginFactory masterPluginFactory = new MasterPluginFactory(pluginFactories, pluginWhitelist, jiraFailedPluginTracker);
        return ImmutableList.<PluginFactory>of(masterPluginFactory);
    }

    private Set<Application> jiraApplications(BuildUtilsInfo buildUtilsInfo)
    {
        return ImmutableSet.<Application>of(
                new JiraApplication("jira", buildUtilsInfo),
                new JiraApplication("com.atlassian.jira", buildUtilsInfo));
    }

    public PluginLoader getBundledPluginsLoader(List<PluginFactory> pluginFactories)
    {
        final String bundledPluginOverride = jiraSystemProperties.getProperty("jira.dev.bundledplugins.url");
        final String bundledPluginUrlString;
        if (bundledPluginOverride != null)
        {
            bundledPluginUrlString = bundledPluginOverride;
            log.warn("Bundled plugins being loaded from override " + bundledPluginUrlString);
        }
        else
        {
            String bundledPluginPath = servletContextFactory.getServletContext().getRealPath(BUNDLED_PLUGIN_LOCATION);
            if (bundledPluginPath == null)
            {
                throw new IllegalStateException(
                        "Running JIRA from a packed WAR is not supported."
                                + " Configure your Servlet container to unpack the WAR before running it."
                                + " (Cannot resolve real path for '" + BUNDLED_PLUGIN_LOCATION + "')");
            }
            else
            {
                bundledPluginUrlString = new File(bundledPluginPath).toURI().toString();
            }
        }
        // Directory prior version exploded atlassian-bundled-plugins.zip into. We continue to use
        // this as the directory required by the current BundledPluginLoader constructor.
        final File legacyBundledPluginsDirectory = pathFactory.getBundledPluginsDirectory();
        try
        {
            // Clean out old copies of plugins from prior explosions
            FileUtils.cleanDirectory(legacyBundledPluginsDirectory);
        }
        catch(IOException eio)
        {
            // We don't use the directory any more, and there's not much we can do if we can't clean it,
            // so see if we can get a admin's attention.
            log.warn("Cannot clean '" + legacyBundledPluginsDirectory + "': " + eio.getMessage());
        }

        try
        {
            final URL bundledPluginUrl = new URL(bundledPluginUrlString);
            // Note legacyBundlePluginsDirectory is unused since bundledPluginUrl is never a .zip
            // anymore. Once we have a more appropriate constructor (PLUGDEV-43) we should use that
            // and remove usage of legacyBundledPluginsDirectory, moving the cleanup code to an
            // upgrade task.
            // TODO: https://jdog.jira-dev.com/browse/JDEV-27508
            return new BundledPluginLoader(bundledPluginUrl, legacyBundledPluginsDirectory, pluginFactories, pluginEventManager);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException("Can't form url to bundled plugins directory at: " + BUNDLED_PLUGIN_LOCATION, e);
        }
    }


    public PluginLoader getCustomDirectoryPluginLoader(List<PluginFactory> pluginFactories)
    {
        File customPluginPath = pathFactory.getCustomPluginsDirectory();
        if (customPluginPath != null)
        {
            return new DirectoryPluginLoader(customPluginPath, pluginFactories, pluginEventManager);
        }
        else
        {
            return null;
        }
    }

    public List<PluginLoader> getDefaultSystemPluginLoaders()
    {
        return Lists.<PluginLoader>newArrayList(
                new SinglePluginLoader("system-workflow-plugin.xml"),
                new SinglePluginLoader("system-customfieldtypes-plugin.xml"),
                new SinglePluginLoader("system-reports-plugin.xml"),

                //load the link resolvers and renderer components before the renderers get loaded.
                new SinglePluginLoader("system-contentlinkresolvers-plugin.xml"),
                new SinglePluginLoader("system-renderercomponentfactories-plugin.xml"),
                new SinglePluginLoader("system-renderers-plugin.xml"),
                new SinglePluginLoader("system-macros-plugin.xml"),
                new SinglePluginLoader("system-issueoperations-plugin.xml"),
                new SinglePluginLoader("system-issuetabpanels-plugin.xml"),
                new SinglePluginLoader("system-comment-field-renderer.xml"),
                new SinglePluginLoader("webfragment/system-user-nav-bar-sections.xml"),
                new SinglePluginLoader("webfragment/system-admin-sections.xml"),
                new SinglePluginLoader("webfragment/system-preset-filters-sections.xml"),
                new SinglePluginLoader("webfragment/system-view-project-operations-sections.xml"),
                new SinglePluginLoader("webfragment/system-user-profile-links.xml"),
                new SinglePluginLoader("webfragment/system-hints.xml"),
                new SinglePluginLoader("system-issueviews-plugin.xml"),
                new SinglePluginLoader("system-projectroleactors-plugin.xml"),
                new SinglePluginLoader("system-webresources-plugin.xml"),
                new SinglePluginLoader("system-top-navigation-plugin.xml"),
                new SinglePluginLoader("system-footer-plugin.xml"),
                new SinglePluginLoader("system-user-format-plugin.xml"),
                new SinglePluginLoader("system-user-profile-panels.xml"),
                new SinglePluginLoader("system-jql-function-plugin.xml"),
                new SinglePluginLoader("system-keyboard-shortcuts-plugin.xml"),
                new SinglePluginLoader("system-global-permissions.xml"),
                new SinglePluginLoader("system-project-permissions.xml"),
                new SinglePluginLoader("system-licenseroles-plugin.xml"),
                new SinglePluginLoader("webfragment/system-browse-project-operations-sections.xml"),
                new SinglePluginLoader("webfragment/system-workflowtransitiontabs-links.xml"),
                new SinglePluginLoader("system-helppaths-plugin.xml")
        );
    }

    public List<PluginLoader> getBootstrapSystemPluginLoaders()
    {
        return Lists.<PluginLoader>newArrayList(
                new SinglePluginLoader("system-webresources-plugin.xml"),
                new SinglePluginLoader("system-helppaths-plugin.xml")
        );
    }

    public PluginLoader getDirectoryPluginLoader(List<PluginFactory> pluginFactories)
    {
        return new DirectoryPluginLoader(pathFactory.getInstalledPluginsDirectory(), pluginFactories, pluginEventManager);
    }
}
