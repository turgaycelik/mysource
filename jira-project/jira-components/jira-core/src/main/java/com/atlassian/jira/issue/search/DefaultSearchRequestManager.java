package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.index.SharedEntityIndexer;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.Visitor;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.Query;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultSearchRequestManager implements SearchRequestManager
{
    private final ColumnLayoutManager columnLayoutManager;
    private final SubscriptionManager subscriptionManager;
    private final ShareManager shareManager;
    private final SearchRequestStore searchRequestStore;
    private final SharedEntityIndexer indexer;
    private final SearchService searchService;
    private final UserUtil userUtil;

    /**
     * Used to set the share permissions on a SearchRequest.
     */
    private final Resolver<SearchRequest, SearchRequest> searchRequestPermissionsResolver = new Resolver<SearchRequest, SearchRequest>()
    {
        public SearchRequest get(final SearchRequest request)
        {
            return setSharePermissions(request);
        }
    };

    /**
     * Used to set the share permissions on a SharedEntity.
     */
    private final Resolver<IndexableSharedEntity<SearchRequest>, SharedEntity> sharedEntityPermissionsResolver = new Resolver<IndexableSharedEntity<SearchRequest>, SharedEntity>()
    {
        public SharedEntity get(final IndexableSharedEntity<SearchRequest> entity)
        {
            return setSharePermissions(entity);
        }
    };

    public DefaultSearchRequestManager(final ColumnLayoutManager columnLayoutManager,
            final SubscriptionManager subscriptionManager, final ShareManager shareManager,
            final SearchRequestStore searchRequestStore, final SharedEntityIndexer indexer,
            final SearchService searchService, final UserUtil userUtil)
    {
        this.columnLayoutManager = notNull("columnLayoutManager", columnLayoutManager);
        this.subscriptionManager = notNull("subscriptionManager", subscriptionManager);
        this.shareManager = notNull("shareManager", shareManager);
        this.searchRequestStore = notNull("searchRequestStore", searchRequestStore);
        this.indexer = notNull("indexer", indexer);
        this.searchService = notNull("searchService", searchService);
        this.userUtil = notNull("userUtil", userUtil);
    }

    @Override
    public EnclosedIterable<SearchRequest> get(final RetrievalDescriptor descriptor)
    {
        return Transformed.enclosedIterable(searchRequestStore.get(descriptor), searchRequestPermissionsResolver);
    }

    @Override
    public EnclosedIterable<SearchRequest> get(final User user, final RetrievalDescriptor descriptor)
    {
        return Transformed.enclosedIterable(searchRequestStore.get(descriptor), createPermissionSanitisingResolver(ApplicationUsers.from(user)));
    }

    @Override
    public EnclosedIterable<SearchRequest> getAll()
    {
        return Transformed.enclosedIterable(searchRequestStore.getAll(), searchRequestPermissionsResolver);
    }

    @Override
    public void visitAll(Visitor<SearchRequestEntity> visitor)
    {
        searchRequestStore.visitAll(visitor);
    }

    @Override
    public EnclosedIterable<SharedEntity> getAllIndexableSharedEntities()
    {
        return Transformed.enclosedIterable(searchRequestStore.getAllIndexableSharedEntities(), sharedEntityPermissionsResolver);
    }

    @Override
    public Collection<SearchRequest> getAllOwnedSearchRequests(final ApplicationUser user)
    {
        final Collection<SearchRequest> searchRequests = searchRequestStore.getAllOwnedSearchRequests(user);
        if (searchRequests == null)
        {
            return Collections.emptyList();
        }
        return CollectionUtil.transform(searchRequests.iterator(), createPermissionSanitisingResolver(user));
    }

    @Override
    public Collection<SearchRequest> getAllOwnedSearchRequests(final User user)
    {
        return getAllOwnedSearchRequests(ApplicationUsers.from(user));
    }

    @Override
    public SearchRequest getOwnedSearchRequestByName(final ApplicationUser author, final String name)
    {
        Assertions.notNull("name", name);

        final SearchRequest searchRequest = searchRequestStore.getRequestByAuthorAndName(author, name);
        return setSanitisedQuery(author, setSharePermissions(searchRequest));
    }

    @Override
    public SearchRequest getOwnedSearchRequestByName(final User author, final String name)
    {
        return getOwnedSearchRequestByName(ApplicationUsers.from(author), name);
    }

    @Override
    public SearchRequest getSearchRequestById(final ApplicationUser user, final Long id)
    {
        Assertions.notNull("id", id);

        final SearchRequest searchRequest = searchRequestStore.getSearchRequest(id);

        if ((searchRequest != null) && isSharedWith(searchRequest, user))
        {
            return setSanitisedQuery(user, setSharePermissions(searchRequest));
        }
        return null;
    }

    @Override
    public SearchRequest getSearchRequestById(final User user, final Long id)
    {
        return getSearchRequestById(ApplicationUsers.from(user), id);
    }

    @Override
    public SearchRequest getSearchRequestById(final Long id)
    {
        Assertions.notNull("id", id);

        final SearchRequest searchRequest = searchRequestStore.getSearchRequest(id);

        if (searchRequest != null)
        {
            return setSharePermissions(searchRequest);
        }
        return null;
    }

    @Override
    public List<SearchRequest> findByNameIgnoreCase(String name)
    {
        List<SearchRequest> searchRequests = searchRequestStore.findByNameIgnoreCase(name);
        for (SearchRequest searchRequest : searchRequests)
        {
            setSharePermissions(searchRequest);
        }
        return searchRequests;
    }

    @Override
    public String getSearchRequestOwnerUserName(final Long id)
    {
        Assertions.notNull("id", id);

        final SearchRequest searchRequest = searchRequestStore.getSearchRequest(id);
        if (searchRequest != null)
        {
            return searchRequest.getOwner() == null ? null : searchRequest.getOwner().getUsername();
        }
        return null;
    }

    @Override
    public ApplicationUser getSearchRequestOwner(final Long id)
    {
        Assertions.notNull("id", id);

        final SearchRequest searchRequest = searchRequestStore.getSearchRequest(id);
        if (searchRequest != null)
        {
            return searchRequest.getOwner();
        }
        return null;
    }

    @Override
    public SearchRequest create(final SearchRequest request)
    {
        validateSearchRequestCreate(request);

        final SearchRequest searchRequest = searchRequestStore.create(request);
        searchRequest.setPermissions(request.getPermissions());
        searchRequest.setPermissions(shareManager.updateSharePermissions(searchRequest));
        indexer.index(searchRequest).await();
        return setSanitisedQuery(searchRequest.getOwner(), searchRequest);
    }

    @Override
    public SearchRequest update(final SearchRequest request)
    {
        validateSearchRequestUpdate(request);

        final SearchRequest searchRequest = searchRequestStore.update(request);
        searchRequest.setPermissions(shareManager.updateSharePermissions(request));
        indexer.index(searchRequest).await();
        return setSanitisedQuery(searchRequest.getOwner(), searchRequest);
    }

    @Override
    public void delete(final Long id)
    {
        final SearchRequest searchRequest = searchRequestStore.getSearchRequest(notNull("id", id));
        if (searchRequest != null)
        {
            deleteSearchRequest(searchRequest);
        }
    }

    private void deleteSearchRequest(final SearchRequest searchRequest)
    {
        try
        {
            CollectionUtil.foreach(subscriptionManager.getAllSubscriptions(searchRequest.getId()), new Consumer<GenericValue>()
            {
                public void consume(final GenericValue subscription)
                {
                    try
                    {
                        subscriptionManager.deleteSubscription(subscription.getLong("id"));
                    }
                    catch (final Exception e)
                    {
                        throw new DataAccessException("Error occurred while deleting searchRequest.", e);
                    }
                }
            });
        }
        catch (final Exception e)
        {
            throw new DataAccessException("Error occurred while deleting searchRequest.", e);
        }

        try
        {
            // Check if the search request has Column Layout items
            if (columnLayoutManager.hasColumnLayout(searchRequest))
            {
                // If so remove it (the restore method actually removes the column layout)
                columnLayoutManager.restoreSearchRequestColumnLayout(searchRequest);
            }
        }
        catch (final ColumnLayoutStorageException e)
        {
            throw new DataAccessException("Error occurred while deleting searchRequest.", e);
        }

        // remove all the shares.
        shareManager.deletePermissions(searchRequest);

        // Delete the actual searchRequest
        searchRequestStore.delete(searchRequest.getId());

        // deindex
        indexer.deIndex(searchRequest).await();
    }

    @Override
    public TypeDescriptor<SearchRequest> getType()
    {
        return SearchRequest.ENTITY_TYPE;
    }

    @Override
    public void adjustFavouriteCount(final SharedEntity entity, final int adjustmentValue)
    {
        Assertions.notNull("entity", entity);
        Assertions.equals("SearchRequestManager can only adjust favourite counts for Search Requests.", SearchRequest.ENTITY_TYPE,
            entity.getEntityType());

        final SearchRequest searchRequest = searchRequestStore.adjustFavouriteCount(entity.getId(), adjustmentValue);
        setSharePermissions(searchRequest);
        indexer.index(searchRequest).await();
    }

    /**
     * Sanitises the filter's {@link com.atlassian.query.Query} (if set and if necessary).
     *
     * @param searcher the user to sanitise for
     * @param filter the search request
     * @return the input search request with the search query modified.
     */
    SearchRequest setSanitisedQuery(final ApplicationUser searcher, final SearchRequest filter)
    {
        if (filter != null)
        {
            final Query query = filter.getQuery();
            final Query sanitisedQuery = searchService.sanitiseSearchQuery(ApplicationUsers.toDirectoryUser(searcher), query);
            if (!query.equals(sanitisedQuery))
            {
                final boolean isModified = filter.isModified();
                filter.setQuery(sanitisedQuery);
                filter.setModified(isModified);
            }
        }
        return filter;
    }

    private boolean isSharedWith(final SearchRequest entity, final ApplicationUser user)
    {
        return shareManager.isSharedWith(user, entity);
    }

    private SearchRequest setSharePermissions(final SearchRequest filter)
    {
        if (filter != null)
        {
            filter.setPermissions(shareManager.getSharePermissions(filter));
        }
        return filter;
    }

    private SharedEntity setSharePermissions(final IndexableSharedEntity<SearchRequest> entity)
    {
        if (entity != null)
        {
            entity.setPermissions(shareManager.getSharePermissions(entity));
        }
        return entity;
    }

    private void validateSearchRequestCreate(final SearchRequest request)
    {
        Assertions.notNull("request", request);
        Assertions.notNull("request.owner.user.name", request.getOwner() == null ? null : request.getOwner().getUsername());
        Assertions.notNull("request.name", request.getName());
    }

    private void validateSearchRequestUpdate(final SearchRequest request)
    {
        validateSearchRequestCreate(request);
        Assertions.notNull("request.id", request.getId());
    }

    @Override
    public SharedEntitySearchResult<SearchRequest> search(final SharedEntitySearchParameters searchParameters, final ApplicationUser user, final int pagePosition, final int pageWidth)
    {
        Assertions.notNull("searchParameters", searchParameters);
        Assertions.not("pagePosition < 0", pagePosition < 0);
        Assertions.not("pageWidth <= 0", pageWidth <= 0);

        return indexer.getSearcher(SearchRequest.ENTITY_TYPE).search(searchParameters, user, pagePosition, pageWidth);
    }

    @Override
    public SharedEntitySearchResult<SearchRequest> search(final SharedEntitySearchParameters searchParameters, final User user, final int pagePosition, final int pageWidth)
    {
        return search(searchParameters, ApplicationUsers.from(user), pagePosition, pageWidth);
    }

    @Override
    public SearchRequest getSharedEntity(final Long entityId)
    {
        Assertions.notNull("entityId", entityId);
        return setSharePermissions(searchRequestStore.getSearchRequest(entityId));
    }

    @Override
    public SearchRequest getSharedEntity(final User user, final Long entityId)
    {
        Assertions.notNull("entityId", entityId);
        return getSearchRequestById(ApplicationUsers.from(user), entityId);
    }

    @Override
    public boolean hasPermissionToUse(final User user, final SearchRequest entity)
    {
        Assertions.notNull("entity", entity);
        Assertions.equals("SearchRequestManager can only adjust favourite counts for Search Requests.", SearchRequest.ENTITY_TYPE,
            entity.getEntityType());
        return shareManager.isSharedWith(ApplicationUsers.from(user), entity);
    }

    private Resolver<SearchRequest, SearchRequest> createPermissionSanitisingResolver(final ApplicationUser searcher)
    {
        return new Resolver<SearchRequest, SearchRequest>()
        {
            public SearchRequest get(final SearchRequest request)
            {
                return setSanitisedQuery(searcher, setSharePermissions(request));
            }
        };
    }
}
