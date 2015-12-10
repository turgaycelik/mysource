package com.atlassian.jira.help;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.JiraStartedEvent;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.plugin.event.events.PluginRefreshedEvent;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 * @since v6.2.4
 */
@EventComponent
public class CachingHelpUrls implements HelpUrls
{
    private static final Logger LOG = LoggerFactory.getLogger(CachingHelpUrls.class);

    private final HelpUrlsLoader loader;

    @ClusterSafe
    private final LoadingCache<HelpUrlsLoader.HelpUrlsLoaderKey, HelpUrls> cache;

    public CachingHelpUrls(final HelpUrlsLoader loader)
    {
        this.loader = loader;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(30L)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(CacheLoader.from(loader));
    }

    @Nonnull
    @Override
    public HelpUrl getUrl(@Nonnull final String key)
    {
        return getUrls().getUrl(key);
    }

    @Nonnull
    @Override
    public HelpUrl getDefaultUrl()
    {
        return getUrls().getDefaultUrl();
    }

    @Override
    public Iterator<HelpUrl> iterator()
    {
        return getUrls().iterator();
    }

    @Nonnull
    @Override
    public Set<String> getUrlKeys()
    {
        return getUrls().getUrlKeys();
    }

    private HelpUrls getUrls()
    {
        return cache.getUnchecked(loader.keyForCurrentUser());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void clearCache(ClearCacheEvent cacheEvent)
    {
        cache.invalidateAll();
        LOG.debug("Clearing HelpUrls on ClearCacheEvent.");
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void pluginEnabled(final PluginEnabledEvent event)
    {
        cache.invalidateAll();
        LOG.debug("Clearing HelpUrls cached as plugin '{}' enabled.", event.getPlugin().getKey());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void pluginDisabled(final PluginDisabledEvent event)
    {
        cache.invalidateAll();
        LOG.debug("Clearing HelpUrls cached as plugin '{}' disabled.", event.getPlugin().getKey());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void pluginModuleDisabled(final PluginModuleDisabledEvent event)
    {
        cache.invalidateAll();
        LOG.debug("Clearing HelpUrls cached as module '{}' disabled.", event.getModule().getCompleteKey());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void pluginModuleEnabled(final PluginModuleEnabledEvent event)
    {
        cache.invalidateAll();
        LOG.debug("Clearing HelpUrls cached as module '{}' enabled.", event.getModule().getCompleteKey());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void pluginRefreshed(final PluginRefreshedEvent event)
    {
        cache.invalidateAll();
        LOG.debug("Clearing HelpUrls cached as plugin '{}' refreshed.", event.getPlugin().getKey());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void jiraStarted(final JiraStartedEvent event)
    {
        cache.invalidateAll();
        LOG.debug("Clearing HelpUrls on JIRA start.");
    }
}
