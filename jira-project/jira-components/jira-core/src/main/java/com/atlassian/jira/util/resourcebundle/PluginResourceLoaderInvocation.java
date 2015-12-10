package com.atlassian.jira.util.resourcebundle;

import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.metadata.PluginMetadataManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import static com.google.common.collect.Iterables.concat;
import static java.lang.String.format;
import static java.util.Collections.sort;

/**
 * Represents the lookup of {@link java.util.ResourceBundle}s in JIRA. The lookup is complicated in JIRA because
 * we have to iterate through language packs (which are plugins), general plugins and JIRA's own static resources
 * to find all the {@code ResourceBundle}s and merge them into one.
 *
 * JIRA currently uses resource bundles to find translations for the {@link com.atlassian.jira.web.bean.I18nBean}
 * and help paths for {@link com.atlassian.jira.web.util.HelpUtil}. Both use basically the same algorithm with
 * only slight tweaks.
 *
 * @since v6.2.3
 */
@NotThreadSafe
class PluginResourceLoaderInvocation extends ResourceLoaderInvocation
{
    private static final Logger log = Logger.getLogger(ResourceBundleLoader.class);

    /**
     * Tunable - Currently I see about 14k keys on a base system.
     * Better to err on the small side to avoid wasting mem, but the default
     * map size of 16 is certainly never going to be adequate and would make
     * the map build slower than it has to be.
     */
    private static final int INITIAL_FLATTENED_MAP_SIZE = 8192;

    private final PluginAccessor accessor;
    private final PluginMetadataManager pluginMetadata;
    private final Function<Locale, Iterable<ResourceBundle>> defaultLanguage;
    private final BundleLoader bundleLoader;

    private Set<Plugin> plugins = Collections.emptySet();

    PluginResourceLoaderInvocation(PluginAccessor accessor, PluginMetadataManager pluginMetadata)
    {
        this(accessor, pluginMetadata, new DefaultLanguageSupplierImpl(), new DefaultBundleLoader());
    }

    @VisibleForTesting
    PluginResourceLoaderInvocation(PluginAccessor accessor, PluginMetadataManager pluginMetadata,
            DefaultLanguageSupplier defaultLanguage, BundleLoader loader)
    {
        this.accessor = accessor;
        this.pluginMetadata = pluginMetadata;
        this.defaultLanguage = defaultLanguage;
        this.bundleLoader = loader;
    }

    @Override
    ResourceBundleLoader.LoadResult load()
    {
        plugins = Sets.newHashSet();

        //
        // The ordering here is important.  We want V2 language packs to be before v1 language packs
        // and we want translation i18n resources to be last of all.  They need to be listed in the
        // reverse order, here (see flattenResourceBundlesToMap for an explanation).
        //
        Iterable<ResourceBundle> translations = loadPluginSourcedBundles();
        if (getMode().includeLangPacks())
        {
            translations = concat(translations, defaultLanguage.apply(getLocale()), loadV2LanguagePackBundles());
        }
        Map<String, String> text = flattenResourceBundlesToMap(Iterables.filter(translations, Predicates.notNull()));
        return new ResourceBundleLoader.LoadResult(text, plugins);
    }

    /**
     * Flattens a collection of <tt>ResourceBundle</tt>s into a simple
     * key-value map for efficient, exception-free lookups at a later time.
     *
     * @param bundles and iterable which will produce the resource bundles
     *      to apply in the order of lowest to highest priority.  The caller
     *      should ensure, for example, that "de" is listed before "de_CH"
     *      so that "de_CH" keys will override those from "de" by overwriting
     *      them in the map.
     * @return an unmodifiable key-value map containing the flattened mapping
     *      of all the key-value pairs from the resource bundles
     */
    private Map<String, String> flattenResourceBundlesToMap(Iterable<ResourceBundle> bundles)
    {
        final Map<String, String> map = new HashMap<String, String>(INITIAL_FLATTENED_MAP_SIZE);
        for (final ResourceBundle bundle : bundles)
        {
            logBundleLoadedMessage(bundle);
            final Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements())
            {
                final String propertyKey = keys.nextElement();
                if (propertyKey != null)
                {
                    try
                    {
                        final String value = bundle.getString(propertyKey);
                        if (value != null)
                        {
                            map.put(propertyKey.intern(), value.intern());
                        }
                    }
                    // None of these should ever happen because we're getting
                    // the list of keys from the resource bundle itself, but
                    // let's play it safe, anyway...
                    catch (RuntimeException e)
                    {
                        logFlatteningException(propertyKey, bundle, e);
                    }
                }
            }
        }
        return ImmutableMap.copyOf(map);
    }

    private void logBundleLoadedMessage(ResourceBundle bundle)
    {
        if (log.isDebugEnabled())
        {
            String name;
            try
            {
                Field bundleNameField = ResourceBundle.class.getDeclaredField("name");
                bundleNameField.setAccessible(true);
                name = (String) bundleNameField.get(bundle);
            }
            catch (Exception e)
            {
                name = String.valueOf(bundle);
            }
            log.debug(format("Adding ResourceBundle: %s bundle; Locale: %s; Mode: %s; %d keys.",
                    name, bundle.getLocale(), getMode(), bundle.keySet().size()));
        }
    }

    private Iterable<ResourceBundle> loadV2LanguagePackBundles()
    {
        final List<ResourceBundle> v2LanguagePacks = new ArrayList<ResourceBundle>();
        final List<LanguageModuleDescriptor> descriptors = accessor.getEnabledModuleDescriptorsByClass(LanguageModuleDescriptor.class);
        for (LanguageModuleDescriptor descriptor : descriptors)
        {
            addV2LanguagePackBundle(v2LanguagePacks, descriptor);
        }
        sort(v2LanguagePacks, ResourceBundleLocaleSorter.INSTANCE);
        return v2LanguagePacks;
    }

    /**
     * Returns whether or not the {@code providedLocale} is suitable for
     * use in the desired {@code targetLocale}.  It is suitable if the
     * provided locale is an exact match or an appropriate fallback for
     * the target locale.  Specifically, this means that:
     * <ol>
     * <li>The provided locale does not specify a language (it is the root locale); or</li>
     * <li>The provided locale does not specify a country and the language matches; or</li>
     * <li>The provided locale specifies both a language and a country, and they both match.</li>
     * </ol>
     * Any locale "variant" information is ignored, here.
     *
     * @param providedLocale the locale provided by the resource under consideration
     * @param targetLocale the target locale
     * @return <tt>true</tt> if the resource should be included; <tt>false</tt>
     *      if it does not provide translations for the target locale
     */
    private static boolean providedLocaleMatches(Locale providedLocale, Locale targetLocale)
    {
        if (providedLocale.getLanguage().length() == 0)
        {
            return true;
        }
        if (!providedLocale.getLanguage().equals(targetLocale.getLanguage()))
        {
            return false;
        }
        if (providedLocale.getCountry().length() == 0)
        {
            return true;
        }
        return providedLocale.getCountry().equals(targetLocale.getCountry());
    }

    private void addV2LanguagePackBundle(List<ResourceBundle> v2LanguagePacks,
            LanguageModuleDescriptor descriptor)
    {
        try
        {
            final Locale providedLocale = descriptor.getModule().getLocale();
            if (providedLocaleMatches(providedLocale, getLocale()))
            {
                final ResourceBundle resourceBundle = bundleLoader.getBundle(descriptor);
                v2LanguagePacks.add(resourceBundle);
                addPlugin(descriptor.getPlugin());

                final Iterable<ResourceDescriptor> i18nResources = Iterables.filter(
                        descriptor.getPlugin().getResourceDescriptors(), getMode().filter());

                //Load any resources.
                for (final ResourceDescriptor resourceDescriptor : i18nResources)
                {
                    try
                    {
                        v2LanguagePacks.add(bundleLoader.getBundle(getLocale(), descriptor.getPlugin(), resourceDescriptor));
                    }
                    catch (MissingResourceException e)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug(format("FAILED plugin resource; targetLocale: %s; pluginKey: %s; location: %s; mode:%s",
                                    getLocale(), descriptor.getPluginKey(), resourceDescriptor.getLocation(), getMode()));
                        }
                    }
                }
                if (log.isDebugEnabled())
                {
                    log.debug("Accepted v2 lang pack; targetLocale=" + getLocale() + "; descriptor=" + descriptor.getCompleteKey() + "; providedLocale=" + providedLocale);
                }
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("IGNORING v2 lang pack; targetLocale=" + getLocale() + "; descriptor=" + descriptor.getCompleteKey() + "; providedLocale=" + providedLocale);
                }
            }
        }
        catch (MissingResourceException mre)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Missing resource for v2 lang pack; targetLocale=" + getLocale() + "; descriptor=" + descriptor.getCompleteKey() + "; ");
            }
        }
        catch (final RuntimeException re)
        {
            // JRA-18831 JRA-30581
            // This and all the other times you see us catching RuntimeException in here are
            // important and (ugly as they are) should not be removed.  Under no circumstance
            // should one module misbehaving interfere with other modules or with the
            // translation system as a whole.
            if (log.isDebugEnabled())
            {
                String descriptorKey;
                try
                {
                    descriptorKey = descriptor.getCompleteKey();
                }
                catch (RuntimeException ex)
                {
                    descriptorKey = "<" + ex + '>';
                }
                log.debug("FAILED v2 lang pack; targetLocale=" + getLocale() + "; descriptor=" + descriptorKey + "; ", re);
            }
        }
    }

    private static void logFlatteningException(String key, ResourceBundle bundle, Exception e)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Failed to resolve key " + key + "  from resource bundle " + bundle + ": " + e, e);
        }
    }

    private Iterable<ResourceBundle> loadPluginSourcedBundles()
    {
        final List<ResourceBundle> pluginBundles = Lists.newArrayList();
        final List<Plugin> enabledPlugins = Lists.newArrayList(accessor.getEnabledPlugins());

        Ordering<Plugin> ordering = Ordering.from(PluginLanguagePackSorter.INSTANCE);

        if (getMode() == Mode.HELP)
        {
            //Okay, help system plugins needs to come first (i.e. less priority) so that plugin writers can overwrite
            //the any system provided help plugins.
            ordering = ordering.compound(Ordering.natural().reverse().onResultOf(new Function<Plugin, Boolean>()
            {
                @Override
                public Boolean apply(final Plugin input)
                {
                    return pluginMetadata.isSystemProvided(input);
                }
            }));
        }

        sort(enabledPlugins, ordering);
        for (final Plugin plugin : enabledPlugins)
        {
            try
            {
                loadPluginSourcedBundles(getLocale(), pluginBundles, plugin);
            }
            catch (final RuntimeException re)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(format("FAILED plugin resources: targetLocale: %s; pluginKey: %s; mode: %s",
                            getLocale(), plugin.getKey(), getMode()), re);
                }
            }
        }
        //The sort is stable which means that resources with the same LOCALE will be ordered as per their associated
        //plugin order above.
        sort(pluginBundles, ResourceBundleLocaleSorter.INSTANCE);
        return pluginBundles;
    }

    private void loadPluginSourcedBundles(Locale targetLocale, List<ResourceBundle> pluginBundles, Plugin plugin)
    {
        for (final ResourceDescriptor resourceDescriptor : getResourceBundleLocations(plugin))
        {
            try
            {
                loadPluginSourcedBundle(targetLocale, pluginBundles, plugin, resourceDescriptor);
            }
            catch (final RuntimeException re)
            {
                // MissingResourceException is handled gracefully below, so the ResourceDescriptor itself
                // must be toxic.  I don't think it should ever happen, but I'm paranoid at this point...
                log.debug(format("FAILED plugin resource; targetLocale: %s; pluginKey: %s; descriptor: <toxic>; mode: %s",
                        targetLocale, plugin.getKey(), getMode()), re);
            }
        }
    }

    private void loadPluginSourcedBundle(Locale targetLocale, List<ResourceBundle> pluginBundles,
            Plugin plugin, ResourceDescriptor descriptor)
    {
        try
        {
            final ResourceBundle resourceBundle =
                    bundleLoader.getBundle(targetLocale, plugin, descriptor);

            if (providedLocaleMatches(resourceBundle.getLocale(), targetLocale))
            {
                if (log.isDebugEnabled())
                {
                    log.debug(format("Accepted plugin resource; targetLocale: %s; pluginKey: %s; descriptor: %s; mode: %s",
                            targetLocale, plugin.getKey(), descriptor.getLocation(), getMode()));
                }
                pluginBundles.add(resourceBundle);
                addPlugin(plugin);
            }
            else if (log.isDebugEnabled())
            {
                log.debug(format("IGNORING plugin resource; targetLocale: %s; pluginKey: %s; descriptor: %s; mode: %s",
                        targetLocale, plugin.getKey(), descriptor.getLocation(), getMode()));
            }
        }
        catch (MissingResourceException mre)
        {
            log.debug(format("FAILED plugin resource; targetLocale: %s; pluginKey: %s; location: %s; mode:%s",
                    targetLocale, plugin.getKey(), descriptor.getLocation(), getMode()));
        }
    }

    private Collection<ResourceDescriptor> getResourceBundleLocations(final Plugin plugin)
    {
        final List<ResourceDescriptor> locations = Lists.newArrayList();
        Iterables.addAll(locations, Iterables.filter(plugin.getResourceDescriptors(), getMode().filter()));
        for (final ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
        {
            try
            {
                Iterables.addAll(locations, Iterables.filter(moduleDescriptor.getResourceDescriptors(), getMode().filter()));
            }
            catch (final RuntimeException re)
            {
                if (log.isDebugEnabled())
                {
                    log.debug(format("FAILED plugin module resource descriptors: pluginKey: %s; moduleKey: %s; mode: %s",
                            plugin.getKey(), moduleDescriptor.getCompleteKey(), getMode()), re);
                }
            }
        }
        return locations;
    }

    private void addPlugin(Plugin plugin)
    {
        plugins.add(plugin);
    }

    /**
     * A comparator that orders resource bundles by how completely
     * specified their locales are.  Specifically:
     * <ol>
     *     <li>the root locale comes first</li>
     *     <li>locales with a language come after the root locale</li>
     *     <li>locales with a language and country come after locales with a language only</li>
     *     <li>No further ordering is imposed within each of these categories.  In
     *          particular, locale variants are not considered, here.</li>
     * </ol>
     * <p>
     * <strong>Note: This comparator imposes orderings that are inconsistent with equals.</strong></li>
     * </p>
     */
    @Immutable
    private static final class ResourceBundleLocaleSorter implements Comparator<ResourceBundle>
    {
        private static final ResourceBundleLocaleSorter INSTANCE = new ResourceBundleLocaleSorter();

        private ResourceBundleLocaleSorter() {}

        @Override
        public int compare(ResourceBundle bundle1, ResourceBundle bundle2)
        {
            final Locale locale1 = bundle1.getLocale();
            final Locale locale2 = bundle2.getLocale();
            if (locale1.getLanguage().length() == 0)
            {
                return (locale2.getLanguage().length() == 0) ? 0 : -1;
            }
            if (locale2.getLanguage().length() == 0)
            {
                return 1;
            }
            if (locale1.getCountry().length() == 0)
            {
                return (locale2.getCountry().length() == 0) ? 0 : -1;
            }
            if (locale2.getCountry().length() == 0)
            {
                return 1;
            }
            return 0;
        }
    }

    /**
     * A comparator that orders plugins to ensure language pack plugins are greatest
     *<strong>Note: This comparator imposes orderings that are inconsistent with equals.</strong>
     * </p>
     */
    @Immutable
    private static final class PluginLanguagePackSorter implements Comparator<Plugin>
    {
        static final PluginLanguagePackSorter INSTANCE = new PluginLanguagePackSorter();

        private PluginLanguagePackSorter() {}

        @Override
        public int compare(Plugin plugin1, Plugin plugin2)
        {
            final boolean isPlugin1LanguagePack = isLanguagePack(plugin1);
            final boolean isPlugin2LanguagePack = isLanguagePack(plugin2);

            if (isPlugin1LanguagePack)
            {
                return isPlugin2LanguagePack ? 0 : 1;
            }
            else
            {
                return isPlugin2LanguagePack ? -1 : 0;
            }
        }

        private static boolean isLanguagePack(Plugin plugin)
        {
            return !plugin.getModuleDescriptorsByModuleClass(LanguageModuleDescriptor.class).isEmpty();
        }
    }

    interface BundleLoader
    {
        ResourceBundle getBundle(final Locale targetLocale, final Plugin plugin, final ResourceDescriptor descriptor);
        ResourceBundle getBundle(final LanguageModuleDescriptor descriptor);
    }

    interface DefaultLanguageSupplier extends Function<Locale, Iterable<ResourceBundle>>
    {
    }

    private static class DefaultLanguageSupplierImpl implements DefaultLanguageSupplier
    {
        private DefaultLanguageSupplierImpl() {}

        @Override
        public Iterable<ResourceBundle> apply(final Locale input)
        {
            return Collections.singletonList(DefaultResourceBundle.getDefaultResourceBundle(input));
        }
    }

    private static class DefaultBundleLoader implements BundleLoader
    {
        private DefaultBundleLoader() {}

        /**
         * The default behavior is to fallback on Locale.getDefault() if the requested
         * locale is not the default.  This leads to surprising results that we do not
         * want, and this <tt>ResourceBundle.Control</tt> prevents it.
         */
        private static final ResourceBundle.Control NO_FALLBACK_CONTROL =
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);


        @Override
        public ResourceBundle getBundle(final Locale targetLocale, final Plugin plugin, final ResourceDescriptor descriptor)
        {
            return ResourceBundle.getBundle(
                    descriptor.getLocation(), targetLocale, plugin.getClassLoader(), NO_FALLBACK_CONTROL);
        }

        @Override
        public ResourceBundle getBundle(final LanguageModuleDescriptor descriptor)
        {
            return ResourceBundle.getBundle(descriptor.getResourceBundleName(), descriptor.getModule().getLocale(),
                    descriptor.getPlugin().getClassLoader(), NO_FALLBACK_CONTROL);
        }
    }
}
