package com.atlassian.jira.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.InitializingComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.ComponentReference;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.tenancy.api.TenantAccessor;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.component.ComponentAccessor.getComponentReference;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.sal.api.features.DarkFeatureManager.DARKFEATURES_PROPERTIES_FILE_PROPERTY;
import static com.atlassian.sal.api.features.DarkFeatureManager.DARKFEATURES_PROPERTIES_FILE_PROPERTY_DEFAULT;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.concat;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Default implementation of {@link com.atlassian.jira.config.FeatureManager}.
 *
 * @since v4.4
 */
public class DefaultFeatureManager implements FeatureManager, Startable, InitializingComponent
{
    /**
     * Logger for this DefaultFeatureManager instance.
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultFeatureManager.class);

    public static final String FEATURE_RESOURCE_TYPE = "feature";
    public static final Resources.TypeFilter FEATURE_TYPE_FILTER = new Resources.TypeFilter(FEATURE_RESOURCE_TYPE);

    private static final String CORE_FEATURES_RESOURCE = "jira-features.properties";

    private static final Function<String,InputStream> APP_CLASS_LOADER = new Function<String, InputStream>()
    {
        @Override
        public InputStream apply(@Nullable String name)
        {
            return DefaultFeatureManager.class.getClassLoader().getResourceAsStream(name);
        }
    };
    private final JiraAuthenticationContext authenticationContext;
    private final FeatureStore featureStore;
    private final JiraProperties jiraSystemProperties;
    private final TenantAccessor tenantAccessor;

    private static Function<String,InputStream> pluginLoader(final Plugin plugin)
    {
        return new Function<String, InputStream>()
        {
            @Override
            public InputStream apply(@Nullable String name)
            {
                return plugin.getResourceAsStream(name);
            }
        };
    }

    private final FeaturesMapHolder features;
    private final CachedReference<Set<String>> siteFeatures;
    private final Cache<String, Set<String>> userFeaturesCache;

    private final ComponentReference<EventPublisher> eventPublisherRef = getComponentReference(EventPublisher.class);
    private final ComponentReference<PermissionManager> permissionManagerRef = getComponentReference(PermissionManager.class);

    @VisibleForTesting
    DefaultFeatureManager(PropertiesContainer properties, JiraAuthenticationContext authenticationContext,
            FeatureStore featureStore, final JiraProperties jiraSystemProperties, final CacheManager cacheManager, final TenantAccessor tenantAccessor)
    {
        this.jiraSystemProperties = jiraSystemProperties;
        this.authenticationContext = authenticationContext;
        this.featureStore = featureStore;
        this.userFeaturesCache = initUserFeatureCache(cacheManager);
        this.siteFeatures = initSiteFeatureCache(cacheManager);
        this.tenantAccessor = tenantAccessor;

        this.features = new FeaturesMapHolder(properties.properties);
    }

    public DefaultFeatureManager(JiraAuthenticationContext authenticationContext, FeatureStore featureStore,
            final JiraProperties jiraSystemProperties, final CacheManager cacheManager, final TenantAccessor tenantAccessor)
    {
        this.jiraSystemProperties = jiraSystemProperties;
        this.authenticationContext = authenticationContext;
        this.featureStore = featureStore;
        this.userFeaturesCache = initUserFeatureCache(cacheManager);
        this.siteFeatures = initSiteFeatureCache(cacheManager);
        this.tenantAccessor = tenantAccessor;

        this.features = new FeaturesMapHolder(jiraSystemProperties);
    }

    @Override
    public void afterInstantiation() throws Exception
    {
        final EventPublisher eventPublisher = eventPublisherRef.get();
        eventPublisher.register(this);
        eventPublisherRef.get().register(this.features);
    }

    private Cache<String, Set<String>> initUserFeatureCache(final CacheManager cacheManager)
    {
        return cacheManager.getCache(DefaultFeatureManager.class.getName() + ".userFeaturesCache",
                new UserFeatureCacheLoader());
    }

    private CachedReference<Set<String>> initSiteFeatureCache(final CacheManager cacheManager)
    {
        return cacheManager.getCachedReference(DefaultFeatureManager.class, "siteFeatures", new SiteFeaturesSupplier());
    }

    private static Properties loadCoreProperties(final JiraProperties jiraSystemProperties)
    {
        final String salDarkFeaturesProperty = jiraSystemProperties.getProperty(
                DARKFEATURES_PROPERTIES_FILE_PROPERTY, DARKFEATURES_PROPERTIES_FILE_PROPERTY_DEFAULT);

        // get properties from both JIRA and SAL properties files.
        Map<Object, Object> jiraProperties = loadProperties(CORE_FEATURES_RESOURCE, APP_CLASS_LOADER);
        Map<Object, Object> salProperties = APP_CLASS_LOADER.apply(salDarkFeaturesProperty) != null ? loadProperties(salDarkFeaturesProperty, APP_CLASS_LOADER) : Collections.emptyMap();

        final MapBuilder<Object, Object> unionPropertiesBuilder = MapBuilder.newBuilder();

        final Sets.SetView<Object> union = Sets.union(jiraProperties.keySet(), salProperties.keySet());
        for (Object key : union)
        {
            // jira properties trump sal properties, so check them first.
            if (jiraProperties.containsKey(key))
            {
                unionPropertiesBuilder.add(key, jiraProperties.get(key));
            }
            else if (salProperties.containsKey(key))
            {
                unionPropertiesBuilder.add(key, salProperties.get(key));
            }
        }

        // construct a new properties containing the union.
        Properties properties = new Properties();
        properties.putAll(unionPropertiesBuilder.toHashMap());

        // add the system properties
        for (String key : jiraSystemProperties.getProperties().stringPropertyNames())
        {
            if (key.startsWith(FeatureManager.SYSTEM_PROPERTY_PREFIX))
            {
                String featureKey = key.substring(FeatureManager.SYSTEM_PROPERTY_PREFIX.length(), key.length());
                String featureValue = jiraSystemProperties.getProperty(key);

                log.trace("Feature '{}' is set to {} by system properties", featureKey, featureValue);
                properties.setProperty(featureKey, featureValue);
            }
        }

        return properties;
    }

    private static Properties loadProperties(String path, Function<String,InputStream> loader)
    {
        final InputStream propsStream = notNull(String.format("Resource %s not found", path), loader.apply(path));
        try
        {
            final Properties props = new Properties();
            props.load(propsStream);
            return props;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to load properties from " + path, e);
        }
        finally
        {
            closeQuietly(propsStream);
        }
    }

    private boolean isEnabled(String featureKey, boolean isUserSettable)
    {
        return Boolean.TRUE.equals(features.features().get(featureKey))
                || getDarkFeatures(isUserSettable).isFeatureEnabled(featureKey);
    }

    @Override
    public boolean isEnabled(String featureKey)
    {
        // assume the feature is user-settable, since we don't know
        return isEnabled(featureKey, true);
    }

    @Override
    public boolean isEnabled(Feature feature)
    {
        if (feature instanceof CoreFeatures)
        {
            return isEnabled((CoreFeatures)feature);
        }

        // non-core features are always implicitly user-settable
        return isEnabled(feature.featureKey(), true);
    }

    @Override
    public boolean isEnabledForUser(@Nullable final ApplicationUser user, final String featureKey)
    {
        return getDarkFeaturesForUser(user).isFeatureEnabled(featureKey);
    }

    @Override
    public boolean isEnabled(CoreFeatures feature)
    {
        return isEnabled(feature.featureKey(), feature.isDevFeature());
    }

    @Override
    public boolean isOnDemand()
    {
        return isEnabled(CoreFeatures.ON_DEMAND);
    }

    @Override
    public Set<String> getEnabledFeatureKeys()
    {
        return features.enabledFeatures();
    }

    @Override
    public DarkFeatures getDarkFeatures()
    {
        return getDarkFeatures(true);
    }

    private DarkFeatures getDarkFeatures(boolean includeUserSettable)
    {
        if (includeUserSettable)
        {
            ApplicationUser user = authenticationContext.getUser();
            return new DarkFeatures(getEnabledFeatureKeys(), getSiteEnabledFeatures(), getUserEnabledFeatures(user));
        }
        return jiraSystemProperties.isDarkFeaturesDisabled()
                ? new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet())
                : new DarkFeatures(getEnabledFeatureKeys(), getSiteEnabledFeatures(), Collections.<String>emptySet());
    }

    @Override
    public DarkFeatures getDarkFeaturesForUser(@Nullable final ApplicationUser user)
    {
        if (jiraSystemProperties.isDarkFeaturesDisabled())
        {
            return new DarkFeatures(Collections.<String>emptySet(), Collections.<String>emptySet(), Collections.<String>emptySet());
        }

        return new DarkFeatures(getEnabledFeatureKeys(), getSiteEnabledFeatures(), getUserEnabledFeatures(user));
    }

    @Override
    @Deprecated
    public void enableUserDarkFeature(User user, String feature)
    {
        enableUserDarkFeature(ApplicationUsers.from(user), feature);
    }

    @Override
    @Deprecated
    public void disableUserDarkFeature(User user, String feature)
    {
        disableUserDarkFeature(ApplicationUsers.from(user), feature);
    }

    @Override
    public void enableUserDarkFeature(ApplicationUser user, String feature)
    {
        changeUserDarkFeature(user, feature, true);
    }

    @Override
    public void disableUserDarkFeature(ApplicationUser user, String feature)
    {
         changeUserDarkFeature(user, feature, false);
    }

    @Override
    public void enableSiteDarkFeature(String feature)
    {
        changeSiteDarkFeature(feature, true);
    }

    @Override
    public void disableSiteDarkFeature(String feature)
    {
        changeSiteDarkFeature(feature, false);
    }

    @Override
    public boolean hasSiteEditPermission()
    {
        ApplicationUser loggedInUser = authenticationContext.getUser();
        return permissionManagerRef.get().hasPermission(Permissions.ADMINISTER, loggedInUser);
    }

    private void changeUserDarkFeature(ApplicationUser user, String feature, boolean enable)
    {
        // Check 'dark feature' key against CoreFeatures - users should not attempt to
        // enable or disable them.
        final CoreFeatures coreFeature = CoreFeatures.forFeatureKey(feature);
        if (coreFeature != null && !coreFeature.isDevFeature())
        {
            throw new IllegalStateException("User cannot set feature '" + feature + "' at runtime. It must be set by an admin via properties.");
        }

        Set<String> enabledFeatures = Sets.newHashSet(getUserEnabledFeatures(user));
        if (enable == enabledFeatures.contains(feature))
        {
            // No change to make - feature is already enabled or disabled.
            return;
        }

        if (enable)
        {
            featureStore.create(feature,user.getKey());
            enabledFeatures.add(feature);
        }
        else
        {
            featureStore.delete(feature,user.getKey());
            enabledFeatures.remove(feature);
        }

        userFeaturesCache.remove(user.getKey());
        eventPublisherRef.get().publish(enable ? new FeatureEnabledEvent(feature, user) : new FeatureDisabledEvent(feature, user));
    }

    private void changeSiteDarkFeature(String feature, boolean enable)
    {
        if (!hasSiteEditPermission())
            throw new IllegalStateException("User " + authenticationContext.getUser() + " does not have permission to change site dark features");

        Set<String> enabledFeatures = getSiteEnabledFeatures();
        if (enable == enabledFeatures.contains(feature))
        {
            // No change to make - feature is already enabled or disabled.
            return;
        }

        if (enable)
        {
            featureStore.create(feature,null);
            enabledFeatures.add(feature);
        }
        else
        {
            featureStore.delete(feature,null);
            enabledFeatures.remove(feature);
        }
        siteFeatures.reset();
        eventPublisherRef.get().publish(enable ? new FeatureEnabledEvent(feature) : new FeatureDisabledEvent(feature));
    }

    private Set<String> getUserEnabledFeatures(ApplicationUser user)
    {
        if (user != null)
        {
            return userFeaturesCache.get(user.getKey());
        }
        return Sets.newHashSet();
    }

    private Set<String> getUserEnabledFeaturesFromStore(String userKey)
    {
        return featureStore.getUserFeatures(userKey);
    }

    private Set<String> getSiteEnabledFeatures()
    {
        return siteFeatures.get();
    }

    private Set<String> getSiteEnabledFeaturesFromStore()
    {
        return featureStore.getSiteFeatures();
    }

    @EventListener
    @SuppressWarnings ({ "UnusedDeclaration" })
    public void onClearCache(final ClearCacheEvent event)
    {
        userFeaturesCache.removeAll();
        siteFeatures.reset();
    }

    private static List<Properties> loadPluginFeatureProperties()
    {
        final PluginAccessor pluginAccessor = ComponentAccessor.getPluginAccessor();
        List<Properties> features = Lists.newArrayList();
        for (Plugin plugin : pluginAccessor.getEnabledPlugins())
        {
            for (ResourceDescriptor featureDescriptor : getFeatureResources(plugin))
            {
                features.add(loadProperties(featureDescriptor.getLocation(), pluginLoader(plugin)));
            }
        }
        return features;
    }

    private static Collection<ResourceDescriptor> getFeatureResources(Plugin plugin)
    {
        return filter(plugin.getResourceDescriptors(), FEATURE_TYPE_FILTER);
    }

    @Override
    public void start() throws Exception
    {
        features.start();
    }

    public static class FeaturesMapHolder
    {
        private final ResettableLazyReference<ImmutableMap<String,Boolean>> features;
        private final ResettableLazyReference<ImmutableSet<String>> enabledFeatures = new EnabledFeaturesRef();
        private volatile boolean jiraStarted;

        FeaturesMapHolder(final JiraProperties jiraSystemProperties)
        {
            this.features = new ResettableLazyReference<ImmutableMap<String, Boolean>>()
            {
                @Override
                protected ImmutableMap<String, Boolean> create() throws Exception
                {
                    final Iterable<Properties> propertiesToLoad = jiraStarted ? concat(ImmutableList.of(loadCoreProperties(jiraSystemProperties)), loadPluginFeatureProperties()):
                            ImmutableList.of(loadCoreProperties(jiraSystemProperties));
                    return initFeatures(propertiesToLoad);
                }
            };
        }

        FeaturesMapHolder(final Iterable<Properties> properties)
        {
            this.features = new ResettableLazyReference<ImmutableMap<String, Boolean>>()
            {
                @Override
                protected ImmutableMap<String, Boolean> create() throws Exception
                {
                    return initFeatures(properties);
                }
            };
        }

        ImmutableMap<String,Boolean> features()
        {
            return features.get();
        }

        ImmutableSet<String> enabledFeatures()
        {
            return enabledFeatures.get();
        }

        private ImmutableMap<String, Boolean> initFeatures(Iterable<Properties> properties)
        {
            HashMap<String, Boolean> collector = Maps.newHashMap();
            for (Properties singleProperties : properties)
            {
                for (String property : singleProperties.stringPropertyNames())
                {
                    collector.put(property, Boolean.valueOf(singleProperties.getProperty(property)));
                }
            }

            return ImmutableMap.copyOf(collector);
        }

        private ImmutableSet<String> initEnabledFeatures(ImmutableMap<String, Boolean> allFeatures)
        {
            ImmutableSet.Builder<String> collector = ImmutableSet.builder();
            for (Map.Entry<String, Boolean> featureToggle : allFeatures.entrySet())
            {
                if (featureToggle.getValue())
                {
                    collector.add(featureToggle.getKey());
                }
            }

            return collector.build();
        }

        @EventListener
        @SuppressWarnings ({ "UnusedDeclaration" })
        public void onPluginEnabled(PluginEnabledEvent event)
        {
            onPluginEvent(event.getPlugin());
        }

        @EventListener
        @SuppressWarnings ({ "UnusedDeclaration" })
        public void onPluginDisabled(PluginDisabledEvent event)
        {
            onPluginEvent(event.getPlugin());
        }

        public void start()
        {
            jiraStarted = true;
            clearCache();
        }

        private void onPluginEvent(Plugin plugin)
        {
            if (!getFeatureResources(plugin).isEmpty())
            {
                clearCache();
            }
        }

        private void clearCache()
        {
            features.reset();
            enabledFeatures.reset();
        }

        private class EnabledFeaturesRef extends ResettableLazyReference<ImmutableSet<String>>
        {
            @Override
            protected ImmutableSet<String> create() throws Exception
            {
                return initEnabledFeatures(features.get());
            }
        }
    }

    @VisibleForTesting
    static final class PropertiesContainer
    {
        final Iterable<Properties> properties;

        PropertiesContainer(Iterable<Properties> properties)
        {
            this.properties = properties;
        }
    }

    private class UserFeatureCacheLoader implements CacheLoader<String, Set<String>>
    {
        @Override
        public Set<String> load(String key)
        {
            if (isTenanted())
            {
                return getUserEnabledFeaturesFromStore(key);
            }
            // If the instance is not tenanted yet, do not access the DB to load the cache.
            return ImmutableSet.of();
        }
    }

    private class SiteFeaturesSupplier implements Supplier<Set<String>>
    {
        @Override
        public Set<String> get()
        {
            if (isTenanted())
            {
                return getSiteEnabledFeaturesFromStore();
            }
            // If the instance is not tenanted yet, do not access the DB to load the cache.
            return ImmutableSet.of();
        }
    }

    private boolean isTenanted()
    {
        return !Iterables.isEmpty(tenantAccessor.getAvailableTenants());
    }
}
