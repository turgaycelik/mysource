package com.atlassian.jira.portal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.gadgets.dashboard.Color;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.collect.ImmutableList;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Caching store for Portlet Configurations. The cache stores a id -> PortletConfigurationStore and a portalPage.id ->
 * id mapping.
 *
 *
 * @since 3.13
 */
@EventComponent
public class CachingPortletConfigurationStore implements FlushablePortletConfigurationStore
{
    private final PortletConfigurationStore delegateStore;

    /**
     * Stores portalPage.id -> list[configuration.id]
     */
    private final Cache<Long, List<Long>> cacheByPageId;

    /**
     * Stores configuration.id -> configuration.
     */
    private final Cache<Long, CacheObject<PortletConfiguration>> cacheById;

    public CachingPortletConfigurationStore(final PortletConfigurationStore delegateStore, final CacheManager cacheManager)
    {
        notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;

        cacheByPageId  = cacheManager.getCache(CachingPortletConfigurationStore.class.getName() + ".cacheByPageId",
                new ByPageIdCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        cacheById = cacheManager.getCache(CachingPortletConfigurationStore.class.getName() + ".cacheById",
                new ByIdCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cacheByPageId.removeAll();
        cacheById.removeAll();
    }

    public List<PortletConfiguration> getByPortalPage(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);

        final List<Long> configIds = cacheByPageId.get(portalPageId);
        final ArrayList<PortletConfiguration> returnList = new ArrayList<PortletConfiguration>(configIds.size());
        for (final Object element : configIds)
        {
            final Long id = (Long) element;
            final PortletConfiguration portletConfiguration = getByPortletId(id);
            if (portletConfiguration != null)
            {
                returnList.add(portletConfiguration);
            }
        }

        return returnList;
    }    

    public PortletConfiguration getByPortletId(final Long portletId)
    {
        notNull("portletId", portletId);
        return copyConfiguration(cacheById.get(portletId).getValue());
    }

    public void delete(final PortletConfiguration pc)
    {
        notNull("pc", pc);
        notNull("pc.id", pc.getId());
        try
        {
            delegateStore.delete(pc);
        }
        finally
        {
            cacheById.remove(pc.getId());
            cacheByPageId.removeAll();
        }
    }

    public void updateGadgetPosition(final Long gadgetId, final int row, final int column, final Long dashboardId)
    {
        notNull("gadgetId", gadgetId);
        notNull("dashboardId", dashboardId);

        Long existingDashboardId = null;
        try
        {
            final PortletConfiguration portletConfiguration = cacheById.get(gadgetId).getValue();
            existingDashboardId = portletConfiguration == null ? null : portletConfiguration.getDashboardPageId();
            //if the portletConfiguration wasn't cached previously, then look it up in the delegate store.
            if(existingDashboardId == null)
            {
                final PortletConfiguration pc = delegateStore.getByPortletId(gadgetId);
                existingDashboardId = pc.getDashboardPageId();
            }
            //clear both the source and destination dashboard caches.
            delegateStore.updateGadgetPosition(gadgetId, row, column, dashboardId);
        }
        finally
        {
            cacheById.remove(gadgetId);
            cacheByPageId.remove(dashboardId);
            if (existingDashboardId != null)
            {
                cacheByPageId.remove(existingDashboardId);
            }
        }
    }

    public void updateGadgetColor(final Long gadgetId, final Color color)
    {
        notNull("gadgetId", gadgetId);
        notNull("color", color);

        try
        {
            delegateStore.updateGadgetColor(gadgetId, color);
        }
        finally
        {
            cacheById.remove(gadgetId);
        }
    }

    public void updateUserPrefs(final Long gadgetId, final Map<String, String> userPrefs)
    {
        notNull("gadgetId", gadgetId);
        notNull("userPrefs", userPrefs);

        try
        {
            delegateStore.updateUserPrefs(gadgetId, userPrefs);
        }
        finally
        {
            cacheById.remove(gadgetId);
        }
    }

    public void store(final PortletConfiguration pc)
    {
        notNull("pc", pc);
        notNull("pc.id", pc.getId());
        try
        {
            delegateStore.store(pc);
        }
        finally
        {
            cacheById.remove(pc.getId());
            cacheByPageId.removeAll();
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UL_UNRELEASED_LOCK_EXCEPTION_PATH", justification="This appears to be doing exactly the right thing with the finally-clause to release the lock")
    public PortletConfiguration addGadget(final Long pageId, final Long portletConfigurationId, final Integer column, final Integer row,
            final URI gadgetXml, final Color color, final Map<String, String> userPreferences)
    {
        notNull("pageId", pageId);
        notNull("column", column);
        notNull("row", row);
        notNull("gadgetXml", gadgetXml);
        notNull("userPreferences", userPreferences);
        notNull("color", color);

        PortletConfiguration returnConfig = null;
        try
        {
            returnConfig = delegateStore.addGadget(pageId, portletConfigurationId, column, row, gadgetXml, color, userPreferences);
        }
        finally
        {
            if (returnConfig != null)
            {
                // Remove the cached page as this will now be stale and not contain the new entry.
                cacheByPageId.remove(returnConfig.getDashboardPageId());
            }
        }
        return copyConfiguration(returnConfig);
    }

    /**
     * Flush the cache by removing all entries.
     */
    public void flush()
    {
        cacheByPageId.removeAll();
        cacheById.removeAll();
    }

    /**
     * This is a non-caching call.  Will delegate straight through to the db store.
     */
    public EnclosedIterable<PortletConfiguration> getAllPortletConfigurations()
    {
        return delegateStore.getAllPortletConfigurations();
    }

    /**
     * Copy the passed portlet configuration.
     *
     * @param portletConfiguration the portlet configuration to copy.
     * @return the new deeply copied portlet configuration.  The underlying property set is cloned into a new memory
     *         property set.
     */
    private PortletConfiguration copyConfiguration(final PortletConfiguration portletConfiguration)
    {
        if (portletConfiguration != null)
        {
            return new PortletConfigurationImpl(portletConfiguration.getId(), portletConfiguration.getDashboardPageId(),
                    portletConfiguration.getColumn(), portletConfiguration.getRow(),
                    portletConfiguration.getGadgetURI(), portletConfiguration.getColor(), portletConfiguration.getUserPrefs());
        }
        else
        {
            return null;
        }
    }

    private class ByPageIdCacheLoader implements CacheLoader<Long,List<Long>>
    {
        @Override
        public List<Long> load(@Nonnull final Long portletPageId)
        {
            List<PortletConfiguration> configs = delegateStore.getByPortalPage(portletPageId);
            if (configs != null)
            {
                List<Long> ownedSearchRequestIds = new ArrayList<Long>(configs.size());

                for (final PortletConfiguration config : configs)
                {
                    ownedSearchRequestIds.add(config.getId());
                }
                return ImmutableList.copyOf(ownedSearchRequestIds);
            }
            else
            {
                return ImmutableList.of();
            }
        }
    }

    private class ByIdCacheLoader implements CacheLoader<Long, CacheObject<PortletConfiguration>>
    {
        @Override
        public CacheObject<PortletConfiguration> load(@Nonnull final Long portletId)
        {
            return CacheObject.wrap(delegateStore.getByPortletId(portletId));
        }
    }
}
