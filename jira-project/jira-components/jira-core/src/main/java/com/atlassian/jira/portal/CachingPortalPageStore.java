package com.atlassian.jira.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.collect.ImmutableList;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Caching store for {@link com.atlassian.jira.portal.PortalPage}. The cache stores a id -> PortalPage and a
 * portalPage.owner -> id mapping.
 *
 * @since v3.13
 */
@EventComponent
public class CachingPortalPageStore implements PortalPageStore
{
    private final PortalPageStore delegateStore;

    /**
     * Stores portalPage.owner -> list[portalPage.id]
     */
    private final Cache<String, List<Long>> cacheByUser;

    /**
     * Stores portalPage.id -> portalPage
     */
    private final Cache<Long, CacheObject<PortalPage>> cacheById;

    /**
     * The id of the System Default Portal Page.
     */
    private volatile Long systemDefaultPortalPageId = null;

    public CachingPortalPageStore(final PortalPageStore delegateStore, final CacheManager cacheManager)
    {
        Assertions.notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;

        cacheByUser  = cacheManager.getCache(CachingPortalPageStore.class.getName() + ".cacheByUser",
                new ByUserCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        cacheById = cacheManager.getCache(CachingPortalPageStore.class.getName() + ".cacheById",
                new ByIdCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        flush();
    }

    public EnclosedIterable<PortalPage> get(final RetrievalDescriptor ids)
    {
        return delegateStore.get(ids);
    }

    public EnclosedIterable<PortalPage> getAll()
    {
        return delegateStore.getAll();
    }

    public PortalPage getSystemDefaultPortalPage()
    {
        // this can be called by two threads at the same time. They should both return the same value so
        // we don't need to stop them.
        if (systemDefaultPortalPageId == null)
        {
            final PortalPage page = delegateStore.getSystemDefaultPortalPage();
            if (page != null)
            {
                systemDefaultPortalPageId = page.getId();
            }
        }
        if (systemDefaultPortalPageId != null)
        {
            return getPortalPage(systemDefaultPortalPageId);
        }
        else
        {
            return null;
        }
    }

    @Override
    public Collection<PortalPage> getAllOwnedPortalPages(final ApplicationUser owner)
    {
        Assertions.notNull("owner", owner);
        return getAllOwnedPortalPages(owner.getKey());
    }

    @Override
    public Collection<PortalPage> getAllOwnedPortalPages(final String userKey)
    {
        Assertions.notNull("userKey", userKey);

        Collection<Long> ownedPageIds = cacheByUser.get(userKey);
        final Collection<PortalPage> returnPages = new ArrayList<PortalPage>(ownedPageIds.size());
        for (final Long id : ownedPageIds)
        {
            final PortalPage portalPage = getPortalPage(id);
            if (portalPage != null)
            {
                returnPages.add(portalPage);
            }
        }

        return returnPages;
    }

    @Override
    public PortalPage getPortalPageByOwnerAndName(final ApplicationUser owner, final String portalPageName)
    {
        // We let this pass directly through to the store as this is not used very often.
        return delegateStore.getPortalPageByOwnerAndName(owner, portalPageName);
    }

    public PortalPage getPortalPage(final Long portalPageId)
    {
        Assertions.notNull("portalPageId", portalPageId);
        return copyPortalPage(cacheById.get(portalPageId).getValue());
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UL_UNRELEASED_LOCK_EXCEPTION_PATH", justification="This appears to be doing exactly the right thing with the finally-clause to release the lock")
    public PortalPage create(final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("portalPage.name", portalPage.getName());
        Assertions.notNull("portalPage.owner", portalPage.getOwner());

        PortalPage returnPage = null;
        try
        {
            returnPage = delegateStore.create(portalPage);
        }
        finally
        {
            if (returnPage != null)
            {
                cacheByUser.remove(portalPage.getOwner().getKey());
                cacheById.remove(returnPage.getId());
            }
        }
        return copyPortalPage(returnPage);
    }

    public PortalPage update(final PortalPage portalPage)
    {
        Assertions.notNull("portalPage", portalPage);
        Assertions.notNull("portalPage.id", portalPage.getId());
        final ApplicationUser newOwner = portalPage.getOwner();
        if (!portalPage.isSystemDefaultPortalPage())
        {
            Assertions.notNull("portalPage.owner", newOwner);
        }

        PortalPage returnPage = null;
        PortalPage oldPortalPage = null;

        try
        {
            oldPortalPage = delegateStore.getPortalPage(portalPage.getId());
            returnPage = delegateStore.update(portalPage);
        }
        finally
        {
            cacheById.remove(portalPage.getId());
            if (portalPage.getOwner() != null && (oldPortalPage == null || portalPage.getOwner() != oldPortalPage.getOwner()))
            {
                cacheByUser.remove(portalPage.getOwner().getKey());
            }
            if (oldPortalPage != null && oldPortalPage.getOwner() != portalPage.getOwner())
            {
                cacheByUser.remove(oldPortalPage.getOwner().getKey());
            }
        }
        return returnPage;
    }

    public boolean updatePortalPageOptimisticLock(final Long portalPageId, final Long currentVersion)
    {
        Assertions.notNull("portalPageId", portalPageId);
        Assertions.notNull("currentVersion", currentVersion);

        try
        {
            return delegateStore.updatePortalPageOptimisticLock(portalPageId, currentVersion);
        }
        finally
        {
            cacheById.remove(portalPageId);
        }
    }

    public PortalPage adjustFavouriteCount(final SharedEntity portalPage, final int incrementValue)
    {
        notNull("portalPage", portalPage);
        notNull("portalPage.id", portalPage.getId());

        PortalPage returnPage = null;
        try
        {
            returnPage = delegateStore.adjustFavouriteCount(portalPage, incrementValue);
        }
        finally
        {
            cacheById.remove(portalPage.getId());
        }

        return returnPage;
    }

    public void delete(final Long portalPageId)
    {
        notNull("portalPageId", portalPageId);

        PortalPage oldPage = null;
        try
        {
            oldPage = delegateStore.getPortalPage(portalPageId);
            delegateStore.delete(portalPageId);
        }
        finally
        {
            cacheById.remove(portalPageId);
            if (oldPage != null && oldPage.getOwner() != null)
            {
                cacheByUser.remove(oldPage.getOwner().getKey());
            }
        }
    }

    public void flush()
    {
        cacheById.removeAll();
        cacheByUser.removeAll();
    }

    /**
     * Make a copy of the search request.
     *
     * @param portalPage the search request to copy.
     *
     * @return the copied search request.
     */
    private PortalPage copyPortalPage(final PortalPage portalPage)
    {
        return portalPage != null ? new PortalPage.Builder().portalPage(portalPage).build() : null;
    }

    private class ByUserCacheLoader implements CacheLoader<String, List<Long>>
    {
        @Override
        public List<Long> load(@Nonnull final String ownerKey)
        {
            final Collection<PortalPage> requestsFromDatabase = delegateStore.getAllOwnedPortalPages(ownerKey);

            if (requestsFromDatabase != null)
            {
                List<Long> ownedPortalPageIds = new ArrayList<Long>(requestsFromDatabase.size());

                for (final PortalPage PortalPage : requestsFromDatabase)
                {
                    ownedPortalPageIds.add(PortalPage.getId());
                }
                return ImmutableList.copyOf(ownedPortalPageIds);
            }
            else
            {
                return ImmutableList.of();
            }
        }
    }

    private class ByIdCacheLoader implements CacheLoader<Long, CacheObject<PortalPage>>
    {
        @Override
        public CacheObject<PortalPage> load(@Nonnull final Long portalPageId)
        {
            return CacheObject.wrap(delegateStore.getPortalPage(portalPageId));
        }

    }
}
