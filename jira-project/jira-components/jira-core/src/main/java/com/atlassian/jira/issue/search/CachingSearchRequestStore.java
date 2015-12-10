package com.atlassian.jira.issue.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Visitor;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.collect.ImmutableList;

/**
 * Caching store for {@link SearchRequest}.
 *
 * @since v3.13
 */
@EventComponent
public class CachingSearchRequestStore implements SearchRequestStore
{
    private final SearchRequestStore delegateStore;

    /**
     * Stores searchrequest.owner -> set[searchrequest.id]
     */
    private final Cache<String, List<Long>> cacheByUser;

    /**
     * Stores searchrequest.id -> searchrequest
     */
    private final Cache<Long, CacheObject<SearchRequest>> cacheById;

    public CachingSearchRequestStore(final SearchRequestStore delegateStore, final CacheManager cacheManager)
    {
        Assertions.notNull("delegateStore", delegateStore);

        this.delegateStore = delegateStore;

        cacheByUser  = cacheManager.getCache(CachingSearchRequestStore.class.getName() + ".cacheByUser",
                new ByUserCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        cacheById = cacheManager.getCache(CachingSearchRequestStore.class.getName() + ".cacheById",
                new ByIdCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cacheById.removeAll();
        cacheByUser.removeAll();
    }

    @Override
    public EnclosedIterable<SearchRequest> get(final RetrievalDescriptor descriptor)
    {
        return delegateStore.get(descriptor);
    }

    @Override
    public EnclosedIterable<SearchRequest> getAll()
    {
        return delegateStore.getAll();
    }

    @Override
    public void visitAll(Visitor<SearchRequestEntity> visitor)
    {
        delegateStore.visitAll(visitor);
    }

    @Override
    public EnclosedIterable<IndexableSharedEntity<SearchRequest>> getAllIndexableSharedEntities()
    {
        return delegateStore.getAllIndexableSharedEntities();
    }

    @Override
    public Collection<SearchRequest> getAllOwnedSearchRequests(final ApplicationUser owner)
    {
        Assertions.notNull("owner", owner);
        return getAllOwnedSearchRequests(owner.getKey());
    }

    @Override
    public Collection<SearchRequest> getAllOwnedSearchRequests(final String userKey)
    {
        Assertions.notNull("userKey", userKey);

        final Collection<Long> ownedSearchRequestIds = cacheByUser.get(userKey);
        final Collection<SearchRequest> returnPages = new ArrayList<SearchRequest>(ownedSearchRequestIds.size());
        for (final Long id : ownedSearchRequestIds)
        {
            final SearchRequest searchRequest = getSearchRequest(id);
            if (searchRequest != null)
            {
                returnPages.add(searchRequest);
            }
        }
        return returnPages;
    }

    @Override
    public SearchRequest getRequestByAuthorAndName(final ApplicationUser author, final String name)
    {
        return delegateStore.getRequestByAuthorAndName(author, name);
    }

    @Override
    public SearchRequest getSearchRequest(@Nonnull final Long searchRequestId)
    {
        Assertions.notNull("searchRequestId", searchRequestId);
        return copySearch(cacheById.get(searchRequestId).getValue());
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UL_UNRELEASED_LOCK_EXCEPTION_PATH", justification="This appears to be doing exactly the right thing with the finally-clause to release the lock")
    @Override
    public SearchRequest create(@Nonnull final SearchRequest request)
    {
        Assertions.notNull("request", request);

        SearchRequest savedRequest = null;
        try
        {
            savedRequest = delegateStore.create(request);
        }
        finally
        {
            if (savedRequest != null)
            {
                cacheById.remove(savedRequest.getId());
                if (request.getOwner() != null)
                {
                    cacheByUser.remove(request.getOwner().getKey());
                }
            }
        }
        return copySearch(savedRequest);
    }

    @Override
    public SearchRequest update(@Nonnull final SearchRequest request)
    {
        Assertions.notNull("request", request);
        Assertions.notNull("request.id", request.getId());

        SearchRequest returnRequest = null;
        SearchRequest oldSearchRequest = null;
        try
        {
            oldSearchRequest = delegateStore.getSearchRequest(request.getId());
            returnRequest = delegateStore.update(request);
        }
        finally
        {
            cacheById.remove(request.getId());
            if (request.getOwner() != null && (oldSearchRequest == null || request.getOwner() != oldSearchRequest.getOwner()))
            {
                cacheByUser.remove(request.getOwner().getKey());
            }
            if (oldSearchRequest != null && oldSearchRequest.getOwner() != request.getOwner())
            {
                cacheByUser.remove(oldSearchRequest.getOwner().getKey());
            }
        }
        return copySearch(returnRequest);
    }

    @Override
    public SearchRequest adjustFavouriteCount(@Nonnull final Long searchRequestId, final int incrementValue)
    {
        Assertions.notNull("searchRequestId", searchRequestId);

        SearchRequest returnRequest = null;
        try
        {
            returnRequest = delegateStore.adjustFavouriteCount(searchRequestId, incrementValue);
        }
        finally
        {
            cacheById.remove(searchRequestId);
        }
        return copySearch(returnRequest);
    }

    @Override
    public void delete(@Nonnull final Long searchId)
    {
        Assertions.notNull("searchId", searchId);

        SearchRequest oldSearchRequest = null;
        try
        {
            oldSearchRequest = delegateStore.getSearchRequest(searchId);
            delegateStore.delete(searchId);
        }
        finally
        {
            cacheById.remove(searchId);
            if (oldSearchRequest != null && oldSearchRequest.getOwner() != null)
            {
                cacheByUser.remove(oldSearchRequest.getOwner().getKey());
            }
        }
    }

    @Override
    public EnclosedIterable<SearchRequest> getSearchRequests(final Project project)
    {
        return delegateStore.getSearchRequests(project);
    }

    @Override
    @Nonnull
    public List<SearchRequest> findByNameIgnoreCase(String name)
    {
        return delegateStore.findByNameIgnoreCase(name);
    }

    @Override
    public EnclosedIterable<SearchRequest> getSearchRequests(final Group group)
    {
        return delegateStore.getSearchRequests(group);
    }

    /**
     * Make a copy of the search request.
     *
     * @param searchRequest the search request to copy.
     *
     * @return the copied search request.
     */
    private SearchRequest copySearch(final SearchRequest searchRequest)
    {
        return searchRequest != null ? new SearchRequest(searchRequest) : null;
    }

    private class ByUserCacheLoader implements CacheLoader<String, List<Long>>
    {
        @Override
        public List<Long> load(@Nonnull final String ownerKey)
        {
            final Collection<SearchRequest> requestsFromDatabase = delegateStore.getAllOwnedSearchRequests(ownerKey);

            if (requestsFromDatabase != null)
            {
                List<Long> ownedSearchRequestIds = new ArrayList<Long>(requestsFromDatabase.size());

                for (final SearchRequest searchRequest : requestsFromDatabase)
                {
                    ownedSearchRequestIds.add(searchRequest.getId());
                }
                return ImmutableList.copyOf(ownedSearchRequestIds);
            }
            else
            {
                return ImmutableList.of();
            }
        }
    }

    private class ByIdCacheLoader implements CacheLoader<Long, CacheObject<SearchRequest>>
    {
        @Override
        public CacheObject<SearchRequest> load(@Nonnull final Long searchRequestId)
        {
            return CacheObject.wrap(delegateStore.getSearchRequest(searchRequestId));
        }

    }
}
