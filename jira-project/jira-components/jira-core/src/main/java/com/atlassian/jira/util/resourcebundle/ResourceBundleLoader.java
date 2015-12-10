package com.atlassian.jira.util.resourcebundle;

import com.atlassian.plugin.Plugin;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Locale;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * Searches JIRA and its associated plugins for {@link java.util.ResourceBundle}s of the correct type and locale
 * and loads them into a map for consumption in JIRA. This search will iterate through language packs
 * (which are plugins), general plugins and JIRA's own static resources to find all the {@code ResourceBundle}s
 * and merge them into one.
 *
 * <p>The search must be limited to a particular {@link java.util.Locale} and resource type. JIRA currently supports
 * the following resource types:
 * <dl>
 *    <dt>Language</dt><dd>JIRA languages</dd>
 *    <dt>Help Paths</dt><dd>Sources for JIRA's internal Help</dd>
 * </dt>
 *
 * <p>The only safe way to obtain {@code ResourceBundleLoader} is to inject it. Initially, this instance will be
 * configured to search for language resources using JIRA's default {@code Locale}. These settings cannot be changed
 * once the object is created, however, it is easy to create new instances with the correct settings using the
 * {@link #locale(java.util.Locale)}, {@link #helpText()} and {@link #i18n()}. For example:
 *
 * <pre>
 * {@code
 *
 *  &#64;Inject
 *  private ResourceBundleLoader initial;
 *
 *  public void doSomething() {
 *      //Search for the HelpPath resources associated with the US locale.
 *      LoadResult result = initial.locale(Locale.US).helpPath().load();
 *  }
 * }
 * </pre>
 *
 * <p>This object is immutable and may safely be cached and/or shared between threads.</p>
 *
 *
 * @since v6.2.3
 */
@Immutable
public interface ResourceBundleLoader
{
    /**
     * Returns a loader that will search for resources associated with the passed {@code Locale}. This method will not
     * mutate {@code this} and as such the caller must use the returned loader to search against the passed {@code Locale}.
     *
     * @param locale the locale.
     *
     * @return a loader that will search for resources associated with the passed {@code Locale}. The type of search
     * remains unchanged.
     */
    ResourceBundleLoader locale(Locale locale);

    /**
     * Returns a loader that will search for resources associated with {@link com.atlassian.jira.help.HelpUrls}.
     * The {@code Locale} used by the search remains unchanged. This method will not mutate {@code this} and as such
     * the caller must use the returned loader to find the right type of resources.
     *
     * @return a loader that will search for resources associated with {@code HelpUrls}. The {@code Locale} used by
     * the search remains unchanged.
     */
    ResourceBundleLoader helpText();

    /**
     * Returns a loader that will search for resources associated with a {@link com.atlassian.jira.util.I18nHelper}.
     * The {@code Locale} used by the search remains unchanged. This method will not mutate {@code this} and as such
     * the caller must use the returned loader to find the right type of resources.
     *
     * @return a loader that will search for resources associated with a {@code I18nHelper}. The {@code Locale} used by
     * the search remains unchanged.
     */
    ResourceBundleLoader i18n();

    /**
     * Perform a search and load all the configured resources.
     *
     * @return the loaded resources with the plugins that provided them.
     */
    LoadResult load();

    /**
     * Result of the load operation.
     *
     * @since 6.2.3
     */
    class LoadResult
    {
        private final Map<String, String> data;
        private final Iterable<Plugin> plugins;

        public LoadResult(final Map<String, String> data, final Iterable<Plugin> plugins)
        {
            this.data = ImmutableMap.copyOf(data);
            this.plugins = ImmutableSet.copyOf(plugins);
        }

        public Map<String, String> getData()
        {
            return data;
        }

        public Iterable<Plugin> getPlugins()
        {
            return plugins;
        }
    }
}
