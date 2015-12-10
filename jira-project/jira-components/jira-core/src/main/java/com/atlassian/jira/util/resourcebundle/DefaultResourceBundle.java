package com.atlassian.jira.util.resourcebundle;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.util.concurrent.CopyOnWriteMap;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides access to the default ResourceBundle. That is, the bundle for "JiraWebActionSupport.properties" for the
 * various languanges/locales.
 * <p/>
 * <p> This class caches the ResourceBundle per locale, because the lookup is expensive and causes thread contention.
 * Default ResourceBundles must live in the core ClassLoader and can be cached permanently as they cannot change. This
 * is not so simple for custom translations for plugins (other ResourceBundles), and so they are not included here.
 *
 * @since v4.0
 */
public class DefaultResourceBundle
{
    private static final CopyOnWriteMap<Locale, ResourceBundle> resourceBundleMap = CopyOnWriteMap.newHashMap();
    public static final String DEFAULT_RESOURCE_BUNDLE_NAME = JiraWebActionSupport.class.getName();

    /**
     * Returns the Default Resource Bundle (JiraWebActionSupport.properties) for the given locale.
     *
     * @param locale The locale.
     *
     * @return the Default Resource Bundle (JiraWebActionSupport.properties) for the given locale.
     */
    public static ResourceBundle getDefaultResourceBundle(final Locale locale)
    {
        ResourceBundle resourceBundle = resourceBundleMap.get(locale);
        if (resourceBundle == null)
        {
            if (JiraSystemProperties.isI18nReloadBundles())
            {
                //
                // in dev mode we want to use a resource bundle that can reload itself if the underlying file changes
                // so we use our own home grown ResourceBundle.  This guy is reasonable efficient and its only used
                // in dev mode anyways
                //
                resourceBundle = DebuggingResourceBundle.getDebuggingResourceBundle(DEFAULT_RESOURCE_BUNDLE_NAME,locale);
            }
            if (resourceBundle == null)
            {
                // The default resource bundle must live in the standard class loader because it holds translations for core JIRA.
                resourceBundle = ResourceBundle.getBundle(DEFAULT_RESOURCE_BUNDLE_NAME, locale);
            }
            final ResourceBundle result = resourceBundleMap.putIfAbsent(locale, resourceBundle);
            return (result == null) ? resourceBundle : result;
        }
        return resourceBundle;
    }

    /**
     * Returns <tt>true</tt> if and only if the default resource bundle data is
     * stale for the given locale.  This is used by <tt>I18nBean.CachingFactory</tt>
     * to flush the cache when the default resource bundle is modified.  This
     * is only relevant in dev mode, as otherwise we will not have a
     * <tt>DebuggingResourceBundle</tt> and so can not reload it anyway.
     *
     * @param locale the locale within which to make the check
     * @return <tt>true</tt> if the data has to be reloaded; <tt>false</tt>
     *      otherwise.
     */
    public static boolean isDefaultResourceBundleStale(final Locale locale)
    {
        ResourceBundle resourceBundle = resourceBundleMap.get(locale);
        return resourceBundle instanceof DebuggingResourceBundle && ((DebuggingResourceBundle)resourceBundle).isStale();
    }
}

