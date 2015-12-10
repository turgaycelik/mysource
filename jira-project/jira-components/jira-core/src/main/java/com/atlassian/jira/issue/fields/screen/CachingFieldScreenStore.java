package com.atlassian.jira.issue.fields.screen;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.issue.field.screen.AbstractFieldScreenLayoutItemEvent;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright (c) 2002-2004 All rights reserved.
 */
@EventComponent
public class CachingFieldScreenStore implements FieldScreenStore
{
    private static final Logger log = LoggerFactory.getLogger(CachingFieldScreenStore.class);
    private final FieldScreenStore decoratedStore;

    private final Cache<Long, CacheObject<FieldScreen>> fieldScreenCache;
    private final CachedReference<List<Long>> allScreensCache;

    public CachingFieldScreenStore(FieldScreenStore decoratedStore, CacheManager cacheManager)
    {
        this.decoratedStore = decoratedStore;
        allScreensCache = cacheManager.getCachedReference("com.atlassian.jira.issue.fields.screen.CachingFieldScreenStore.allScreensCache", new AllScreenSupplier());
        fieldScreenCache = cacheManager.getCache("com.atlassian.jira.issue.fields.screen.CachingFieldScreenStore.fieldScreenCache", new FieldScreenCacheLoader(), new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public void setFieldScreenManager(FieldScreenManager fieldScreenManager)
    {
        decoratedStore.setFieldScreenManager(fieldScreenManager);
    }

    public FieldScreen getFieldScreen(Long id)
    {
        // NOTE: we are returning a mutable object from a cache here, which is rubbish. however, this cache has
        // worked like this since JIRA 3.10 so it is not causing any immediate problems. the last time I tried to
        // improve on this I ended up returning an incomplete-copied FieldScreen, which then caused performance
        // problems as everyone who got a FieldScreen from the cache ended up having to read the FieldScreenTabs
        // from the database anyway (JRA-28906).
        // We find out about changes via the AbstractFieldScreenLayoutItemEvent.
        return fieldScreenCache.get(id).getValue();
    }

    public List<Long> getFieldScreenIds()
    {
        return allScreensCache.get();
    }

    public List<FieldScreen> getFieldScreens()
    {
        List<Long> allIds = getFieldScreenIds();
        List<FieldScreen> allScreens = Lists.newArrayListWithCapacity(allIds.size());
        for (Long id : allIds)
        {
            CacheObject<FieldScreen> fieldScreenRef = fieldScreenCache.get(id);
            if (fieldScreenRef.hasValue())
            {
                allScreens.add(fieldScreenRef.getValue());
            }
        }
        return Ordering.from(new ScreenNameComparator()).immutableSortedCopy(allScreens);
    }

    public void createFieldScreen(FieldScreen fieldScreen)
    {
        try
        {
            decoratedStore.createFieldScreen(fieldScreen);
        }
        finally
        {
            refresh();
        }
    }

    public void removeFieldScreen(Long id)
    {
        try
        {
            decoratedStore.removeFieldScreen(id);
        }
        finally
        {
            refresh();
        }
    }

    public void updateFieldScreen(FieldScreen fieldScreen)
    {
        try
        {
            decoratedStore.updateFieldScreen(fieldScreen);
        }
        finally
        {
            fieldScreenCache.remove(fieldScreen.getId());
        }
    }

    public void createFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        try
        {
            decoratedStore.createFieldScreenTab(fieldScreenTab);
        }
        finally
        {
            if (fieldScreenTab != null && fieldScreenTab.getFieldScreen() != null)
            {
                fieldScreenCache.remove(fieldScreenTab.getFieldScreen().getId());
            }
        }
    }

    public void updateFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        try
        {
            decoratedStore.updateFieldScreenTab(fieldScreenTab);
        }
        finally
        {
            if (fieldScreenTab != null && fieldScreenTab.getFieldScreen() != null)
            {
                fieldScreenCache.remove(fieldScreenTab.getFieldScreen().getId());
            }
        }
    }

    public List<FieldScreenTab> getFieldScreenTabs(FieldScreen fieldScreen)
    {
        return decoratedStore.getFieldScreenTabs(fieldScreen);
    }

    public void updateFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.updateFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.removeFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        decoratedStore.removeFieldScreenLayoutItems(fieldScreenTab);
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        return decoratedStore.getFieldScreenLayoutItems(fieldScreenTab);
    }

    public void refresh()
    {
        allScreensCache.reset();
        fieldScreenCache.removeAll();

        if (log.isTraceEnabled())
        {
            log.trace("Called refresh()", new Throwable());
        }
    }

    /**
     * Refreshes a single FieldScreen when there is a change to any of its constituent FieldScreenLayoutItem's.
     *
     * @param event a AbstractFieldScreenLayoutItemEvent
     */
    @EventListener
    public void onFieldScreenLayoutChange(AbstractFieldScreenLayoutItemEvent event)
    {
        fieldScreenCache.remove(event.getFieldScreenId());
    }

    public void createFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.createFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(GenericValue genericValue)
    {
        return decoratedStore.buildNewFieldScreenLayoutItem(genericValue);
    }

    public void removeFieldScreenTabs(FieldScreen fieldScreen)
    {
        decoratedStore.removeFieldScreenTabs(fieldScreen);
        fieldScreenCache.remove(fieldScreen.getId());
    }

    public void removeFieldScreenTab(Long id)
    {
        FieldScreenTab tab = getFieldScreenTab(id);
        try
        {
            decoratedStore.removeFieldScreenTab(id);
        }
        finally
        {
            if (tab != null && tab.getFieldScreen() != null)
            {
                fieldScreenCache.remove(tab.getFieldScreen().getId());
            }
        }
    }

    public FieldScreenTab getFieldScreenTab(Long tabId)
    {
        // @TODO getting the tab by ID is not cached. Only used once at present
        return decoratedStore.getFieldScreenTab(tabId);
    }

    private class AllScreenSupplier implements Supplier<List<Long>>
    {
        @Override
        public List<Long> get()
        {
            return decoratedStore.getFieldScreenIds();
        }
    }

    private class FieldScreenCacheLoader implements CacheLoader<Long, CacheObject<FieldScreen>>
    {
        @Override
        public CacheObject<FieldScreen> load(@Nonnull final Long fieldScreenId)
        {
            return CacheObject.wrap(decoratedStore.getFieldScreen(fieldScreenId));
        }
    }
    private static class ScreenNameComparator implements Comparator<FieldScreen>
    {
        @Override
        public int compare(FieldScreen fs1, FieldScreen fs2)
        {
            return fs1.getName().compareTo(fs2.getName());
        }
    }
}
