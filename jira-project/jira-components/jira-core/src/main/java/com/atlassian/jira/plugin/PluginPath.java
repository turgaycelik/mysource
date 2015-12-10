package com.atlassian.jira.plugin;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.startup.JiraHomeLocator;
import com.atlassian.jira.util.PathUtils;
import com.atlassian.plugin.osgi.container.OsgiContainerException;
import com.atlassian.plugin.osgi.container.OsgiPersistentCache;
import com.atlassian.plugin.osgi.container.impl.DefaultOsgiPersistentCache;
import org.apache.log4j.Logger;

import java.io.File;

public interface PluginPath
{
    final static String PLUGINS_DIRECTORY = "plugins";
    final static String INSTALLED_PLUGINS_SUBDIR = PathUtils.joinPaths(PLUGINS_DIRECTORY, "installed-plugins");
    final static String OSGI_SUBDIR = PathUtils.joinPaths(PLUGINS_DIRECTORY, ".osgi-plugins");

    /**
     * Although we no longer copy bundled plugins to this directory, we maintain it (empty) in order
     * to return from {@link #getBundledPluginsDirectory}, until we have a plugins with PLUGDEV-43
     * and we can move this defintion to an upgrade task which removes the directory entirely.
     *
     * TODO: https://jdog.jira-dev.com/browse/JDEV-27508
     */
    final static String BUNDLED_SUBDIR = PathUtils.joinPaths(PLUGINS_DIRECTORY, ".bundled-plugins");

    boolean isConfigured();

    /**
     * The directory where plugins are placed to install them.
     * 
     * @return a File that points to the root plugins dir (ie. ${jira.home}/plugins/installed-plugins)
     */
    File getInstalledPluginsDirectory();

    /**
     * The directory where bundled plugins were stored historically.
     *
     * This is being maintained to provide a directory for the BundledPluginLoader until we have a
     * plugins with PLUGDEV-43 and can remove this entirely.
     *
     * TODO: https://jdog.jira-dev.com/browse/JDEV-27508
     *
     * @return A file that points to the old bundled plugins directory. (ie. ${jira.home}/plugins/.bundled-plugins)
     *
     * @deprecated This method should not be used by new code, and will be removed when plugins is upgraded.
     */
    File getBundledPluginsDirectory();

    /**
     * The custom directory specified by the system property atlassian.jira.pluginScanDirectory to load plugins from.
     *
     * @return A file that points to the path specified by the system property atlassian.jira.pluginScanDirectory.
     */
    File getCustomPluginsDirectory();

    /**
     * Bascially handles caching of plugin jars (bundled etc) and clearing those directories.
     * @return A OsgiPersistentCache
     */
    OsgiPersistentCache getOsgiPersistentCache();

    public static class JiraHomeAdapter implements PluginPath
    {
        private final JiraHome jiraHome;
        private final File pluginsDirectory;
        private final File localPluginsDirectory;
        private final File installedDirectory;
        private final File bundledPluginsDirectory;
        private final File customPluginsDirectory;

        private final OsgiPersistentCache osgiPersistentCache;

        private static final Logger log = Logger.getLogger(JiraHomeAdapter.class);

        public JiraHomeAdapter()
        {
            this.jiraHome = new JiraHomeLocator.SystemJiraHome();
            // normally these directories are created by the JiraHomeStartupCheck. However, we also need them to exist
            // for unit testing so we wrap them in createDirectoryIfNecessary
            pluginsDirectory = createDirectoryIfNecessary(jiraHome.getHome(), PLUGINS_DIRECTORY);
            localPluginsDirectory = createDirectoryIfNecessary(jiraHome.getLocalHome(), PLUGINS_DIRECTORY);
            installedDirectory = createDirectoryIfNecessary(jiraHome.getHome(), INSTALLED_PLUGINS_SUBDIR);
            bundledPluginsDirectory = createDirectoryIfNecessary(jiraHome.getLocalHome(), BUNDLED_SUBDIR);

            boolean isEnabled = JiraSystemProperties.isCustomPathPluginsEnabled();
            if (isEnabled)
            {
                File customPluginDirectoryCheck = new File(JiraSystemProperties.getCustomDirectoryPlugins());
                if (customPluginDirectoryCheck.canRead() && customPluginDirectoryCheck.isDirectory())
                {
                    customPluginsDirectory = customPluginDirectoryCheck;
                }
                else
                {
                    log.error("The Custom Plugin Directory path: "+customPluginDirectoryCheck.getAbsolutePath()+" does not exist or can not be read.");
                    customPluginsDirectory = null;
                }
            }
            else
            {
                customPluginsDirectory = null;
            }

            try
            {
                final File osgiDirectory = createDirectoryIfNecessary(jiraHome.getLocalHome(), OSGI_SUBDIR);
                osgiPersistentCache = new DefaultOsgiPersistentCache(osgiDirectory);
            }
            catch (OsgiContainerException ex)
            {
                // this is just to debug an issue on JWI, but may be useful to keep for support
                final Throwable cause = ex.getCause();
                if (cause != null)
                {
                    String message = cause.getMessage();
                    if (message != null && message.startsWith("Unable to delete file: "))
                    {
                        // Find the named file we are failing on:
                        File file = new File(message.substring("Unable to delete file: ".length()));
                        log.error(message + "  exists:" + file.exists() + "  canRead:" + file.canRead() + "  canWrite:" + file.canWrite() + "  isDirectory:" + file.isDirectory());
                    }
                }
                throw ex;
            }
        }

        public boolean isConfigured()
        {
            try
            {
                jiraHome.getHomePath();
                return true;
            }
            catch (final IllegalStateException e)
            {
                return false;
            }
        }

        public File getInstalledPluginsDirectory()
        {
            return installedDirectory;
        }

        public File getBundledPluginsDirectory()
        {
            return bundledPluginsDirectory;
        }

        public File getCustomPluginsDirectory()
        {
            return customPluginsDirectory;
        }

        public OsgiPersistentCache getOsgiPersistentCache()
        {
            return osgiPersistentCache;
        }

        private File createDirectoryIfNecessary(final File root, final String path)
        {
            final File dir = new File(root, path);
            if (!dir.exists() && !dir.mkdirs())
            {
                throw new IllegalStateException("Unable to create directory '" + dir + "'");
            }
            if (!dir.isDirectory())
            {
                throw new IllegalStateException("File exists but is not a directory '" + dir + "'");
            }
            return dir;
        }
    }
}
