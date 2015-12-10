package com.atlassian.jira.config.properties;

/**
 * This class provides access to system properties.  It should be used to access properties that can't be accessed via
 * ApplicationProperties yet because the world hasn't been brought up yet.
 *
 * @since v4.0
 * @deprecated use {@link com.atlassian.jira.config.properties.JiraProperties} instead.
 */
public final class JiraSystemProperties
{

    private static final JiraProperties instance =
            new JiraPropertiesImpl(new JiraSystemPropertiesCache(new SystemPropertiesAccessor()));

    public static JiraProperties getInstance()
    {
        return instance;
    }

    private JiraSystemProperties()
    {
        // don't instantiate this class.
    }

    /**
     * @return true if jira is running in dev mode (meaning jira.home lock files will be ignored)
     */
    @Deprecated
    public static boolean isDevMode()
    {
        return instance.isDevMode();
    }

    @Deprecated
    public static boolean isXsrfDetectionCheckRequired()
    {
        return instance.isXsrfDetectionCheckRequired();
    }

    @Deprecated
    public static boolean isSuperBatchingDisabled()
    {
        return instance.isSuperBatchingDisabled();
    }

    @Deprecated
    public static boolean isDecodeMailParameters()
    {
        return instance.isDecodeMailParameters();
    }

    @Deprecated
    public static boolean isCustomPathPluginsEnabled()
    {
        return instance.isCustomPathPluginsEnabled();
    }

    @Deprecated
    public static String getCustomDirectoryPlugins()
    {
        return instance.getCustomDirectoryPlugins();
    }

    @Deprecated
    public static boolean isWebSudoDisabled()
    {
        return instance.isWebSudoDisabled();
    }

    @Deprecated
    public static boolean isI18nReloadBundles()
    {
        return instance.isI18nReloadBundles();
    }

    @Deprecated
    public static boolean showPerformanceMonitor()
    {
        return instance.showPerformanceMonitor();
    }

    @Deprecated
    public static boolean isBundledPluginsDisabled()
    {
        return instance.isBundledPluginsDisabled();
    }

    /**
     * @return return true if the system property has been set to decode the "filename" from an e-mail.
     */
    public static boolean isDecodeMailFileName()
    {
        return instance.getBoolean(SystemPropertyKeys.MAIL_DECODE_FILENAME);
    }

    @Deprecated
    public static void resetReferences()
    {
        instance.refresh();
    }
}
