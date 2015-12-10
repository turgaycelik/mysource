package com.atlassian.jira.issue.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.entity.SelectQuery;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.ofbiz.DatabaseIterable;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.ofbiz.PagedDatabaseIterable;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntityAccessor.RetrievalDescriptor;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.Visitor;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.Query;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityExprList;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * OfBiz implementation of {@link SearchRequestStore}
 *
 * @since v3.13
 */
public class OfBizSearchRequestStore implements SearchRequestStore
{
    static final SharedEntity.TypeDescriptor<SearchRequest> ENTITY_TYPE = SearchRequest.ENTITY_TYPE;

    private static final Logger log = Logger.getLogger(OfBizSearchRequestStore.class);
    private final JqlQueryParser jqlQueryParser;
    private final SearchService searchService;
    private final UserManager userManager;

    static final class Table
    {
        static final String NAME = OfBizSearchRequestStore.ENTITY_TYPE.getName();
        static final String COUNT = NAME + "Count";
        static final String SHARE = NAME + "ShareView";
    }

    private static final class Column
    {
        private static final String AUTHOR = "author";
        private static final String NAME = "name";
        private static final String NAME_LOWER = "nameLower";
        private static final String ID = "id";
        private static final String PROJECT = "project";
        private static final String DESCRIPTION = "description";
        private static final String USER = "user";
        private static final String REQUEST = "request";
        private static final String FAV_COUNT = "favCount";

        // used to count entities
        private static final String COUNT = "count";
    }

    private final OfBizDelegator delegator;

    private final Resolver<GenericValue, SearchRequest> searchRequestResolver = new Resolver<GenericValue, SearchRequest>()
    {
        public SearchRequest get(final GenericValue input)
        {
            return convertGVToRequest(input);
        }
    };

    private final Resolver<GenericValue, IndexableSharedEntity<SearchRequest>> indexableSharedEntityResolver = new Resolver<GenericValue, IndexableSharedEntity<SearchRequest>>()
    {
        public IndexableSharedEntity<SearchRequest> get(final GenericValue input)
        {
            return convertGVToIndexableSharedEntity(input);
        }
    };

    public OfBizSearchRequestStore(final OfBizDelegator delegator, final JqlQueryParser jqlQueryParser,
            final SearchService searchService, UserManager userManager)
    {
        this.jqlQueryParser = jqlQueryParser;
        this.searchService = searchService;
        this.userManager = userManager;
        Assertions.notNull("delegator", delegator);
        this.delegator = delegator;
    }

    @Override
    public EnclosedIterable<SearchRequest> get(final RetrievalDescriptor descriptor)
    {
        // if order is important provide a key resolver
        final Resolver<SearchRequest, Long> keyResolver = (descriptor.preserveOrder()) ? new Resolver<SearchRequest, Long>()
        {
            public Long get(final SearchRequest input)
            {
                return (input).getId();
            }
        } : null;
        return new PagedDatabaseIterable<SearchRequest, Long>(descriptor.getIds(), keyResolver)
        {
            @Override
            protected OfBizListIterator createListIterator(final List<Long> ids)
            {
                return delegator.findListIteratorByCondition(Table.NAME, new EntityExpr(Column.ID, EntityOperator.IN, ids));
            }

            @Override
            protected Resolver<GenericValue, SearchRequest> getResolver()
            {
                return searchRequestResolver;
            }
        };
    }

    @Override
    public void visitAll(Visitor<SearchRequestEntity> visitor)
    {
        Select.from(Entity.SEARCH_REQUEST).runWith(delegator).visitWith(visitor);
    }

    @Override
    public EnclosedIterable<SearchRequest> getAll()
    {
        try
        {
            return new DatabaseIterable<SearchRequest>(new Long(delegator.getCount(Table.NAME)).intValue(), searchRequestResolver)
            {
                @Override
                protected OfBizListIterator createListIterator()
                {
                    return delegator.findListIteratorByCondition(Table.NAME, null);
                }
            };
        }
        catch (final DataAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EnclosedIterable<IndexableSharedEntity<SearchRequest>> getAllIndexableSharedEntities()
    {
        try
        {
            return new DatabaseIterable<IndexableSharedEntity<SearchRequest>>(new Long(delegator.getCount(Table.NAME)).intValue(), indexableSharedEntityResolver)
            {
                @Override
                protected OfBizListIterator createListIterator()
                {
                    return delegator.findListIteratorByCondition(Table.NAME, null);
                }
            };
        }
        catch (final DataAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    int getSearchRequestCountForProject(final Long projectId)
    {
        final EntityCondition condition = new EntityFieldMap(EasyMap.build(Column.PROJECT, projectId), EntityOperator.AND);
        final GenericValue countGV = EntityUtil.getOnly(delegator.findByCondition(Table.COUNT, condition, EasyList.build(Column.COUNT),
            Collections.EMPTY_LIST));
        return countGV.getLong(Column.COUNT).intValue();
    }

    @Override
    public Collection<SearchRequest> getAllOwnedSearchRequests(final ApplicationUser author)
    {
        if (author == null)
        {
            return Collections.emptyList();
        }
        return getAllOwnedSearchRequests(author.getKey());
    }

    @Override
    public Collection<SearchRequest> getAllOwnedSearchRequests(final String userKey)
    {
        return CollectionUtil.transform(getSearchRequestGVsOwnedBy(userKey).iterator(), searchRequestResolver);
    }

    @Override
    public SearchRequest getRequestByAuthorAndName(final ApplicationUser author, final String name)
    {
        if (author == null)
        {
            return null;
        }
        final List<GenericValue> genericValues = delegator.findByAnd(Table.NAME,
                EasyMap.build(Column.AUTHOR, author.getKey(), Column.NAME, name));
        return convertGVToRequest(EntityUtil.getOnly(genericValues));
    }

    @Override
    public SearchRequest getSearchRequest(final Long id)
    {
        return convertGVToRequest(findByPrimaryKey(id));
    }

    @Override
    public SearchRequest create(final SearchRequest request)
    {
        Assertions.notNull("request", request);
        Assertions.notNull("request name", request.getName());
        String name = request.getName();
        final String authorKey = request.getOwner() == null ? null : request.getOwner().getKey();
        final GenericValue gv = delegator.createValue(Table.NAME,
                FieldMap.build(Column.NAME, name)
                        .add(Column.NAME_LOWER, name.toLowerCase())
                        .add(Column.PROJECT, null)
                        .add(Column.DESCRIPTION, request.getDescription())
                        .add(Column.AUTHOR, authorKey)
                        .add(Column.USER, authorKey)
                        .add(Column.REQUEST, searchService.getJqlString(request.getQuery()))
                        .add(Column.FAV_COUNT, new Long(0))
                );
        return convertGVToRequest(gv);
    }

    @Override
    public SearchRequest update(final SearchRequest request)
    {
        Assertions.notNull("request", request);
        final GenericValue searchFilter = findByPrimaryKey(request.getId());
        Assertions.notNull("request name", request.getName());
        String name = request.getName();
        searchFilter.setString(Column.NAME, name);
        searchFilter.setString(Column.NAME_LOWER, name.toLowerCase());
        searchFilter.setString(Column.DESCRIPTION, request.getDescription());
        searchFilter.setString(Column.REQUEST, searchService.getJqlString(request.getQuery()));
        searchFilter.setString(Column.AUTHOR, ApplicationUsers.getKeyFor(request.getOwner()));
        // Don't want to override the fav count
        searchFilter.remove(Column.FAV_COUNT);
        delegator.store(searchFilter);

        return getSearchRequest(request.getId());
    }

    @Override
    public SearchRequest adjustFavouriteCount(final Long searchRequestId, final int incrementValue)
    {
        Assertions.notNull("searchRequestId", searchRequestId);

        final GenericValue gv = findByPrimaryKey(searchRequestId);
        if (gv == null)
        {
            return null;
        }

        final Long currentFavcount = gv.getLong(Column.FAV_COUNT);
        long newFavcount;
        if (currentFavcount == null)
        {
            newFavcount = 0;
        }
        else
        {
            newFavcount = currentFavcount.longValue();
        }

        newFavcount += incrementValue;
        if (newFavcount < 0)
        {
            newFavcount = 0;
        }

        gv.set(Column.FAV_COUNT, new Long(newFavcount));

        delegator.store(gv);

        return getSearchRequest(searchRequestId);
    }

    @Override
    public void delete(final Long id)
    {
        final GenericValue gv = findByPrimaryKey(id);
        // check if we didn't find a match - return null
        if (gv == null)
        {
            throw new DataAccessException("SearchRequest does not exist.  Can not delete.");
        }
        else
        {
            delegator.removeValue(gv);
        }
    }

    @Override
    public EnclosedIterable<SearchRequest> getSearchRequests(final Project project)
    {
        Assertions.notNull("project", project);
        final Long projectId = project.getId();
        return new DatabaseIterable<SearchRequest>(getSearchRequestCountForProject(projectId), searchRequestResolver)
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return delegator.findListIteratorByCondition(Table.NAME, new EntityExpr(Column.PROJECT, EntityOperator.EQUALS, project.getId()));
            }
        };
    }

    @Override
    public List<SearchRequest> findByNameIgnoreCase(final String name)
    {
        SelectQuery<GenericValue> selectQuery = Select.columns().from("SearchRequest").whereEqual("nameLower", name.toLowerCase());
        return selectQuery.runWith(delegator).consumeWith(new EntityListConsumer<GenericValue, List<SearchRequest>>()
        {
            List<SearchRequest> results = new ArrayList<SearchRequest>();

            @Override
            public void consume(GenericValue entity)
            {
                results.add(searchRequestResolver.get(entity));
            }

            @Override
            public List<SearchRequest> result()
            {
                return results;
            }
        });
    }

    @Override
    public EnclosedIterable<SearchRequest> getSearchRequests(final Group group)
    {
        notNull("group", group);
        final Resolver<GenericValue, SearchRequest> resolver = new Resolver<GenericValue, SearchRequest>()
        {
            public SearchRequest get(final GenericValue sharedEntityGV)
            {
                final GenericDelegator genericDelegator = sharedEntityGV.getDelegator();
                return convertGVToRequest(genericDelegator.makeValue(Table.NAME, filter(sharedEntityGV.getAllFields())));
            }

            private final Map<String, ?> filter(final Map<String, ?> fields)
            {
                final Map<String, ?> result = new HashMap<String, Object>(fields);
                result.remove("param1");
                result.remove("param2");
                result.remove("entityId");
                result.remove("type");
                result.remove("entityType");
                return Collections.unmodifiableMap(result);
            }
        };

        final EntityExprList expr = new EntityExprList(EasyList.build(new EntityExpr("type", EntityOperator.EQUALS, GroupShareType.TYPE.get()),
            new EntityExpr("param1", EntityOperator.EQUALS, group.getName())), EntityOperator.AND);

        return new DatabaseIterable<SearchRequest>(-1, resolver)
        {
            @Override
            protected OfBizListIterator createListIterator()
            {
                return delegator.findListIteratorByCondition(Table.SHARE, expr);
            }
        };
    }

    /*
     * Get the GenericValue for a given Id.
     */
    private GenericValue findByPrimaryKey(final Long id)
    {
        return delegator.findById(Table.NAME, id);
    }

    /*
     * Get all GVs for a given user
     */
    private List<GenericValue> getSearchRequestGVsOwnedBy(final String authorKey)
    {
        if (authorKey == null)
        {
            return Collections.emptyList();
        }
        // Private requests
        final List<EntityExpr> entityExpressions = new ArrayList<EntityExpr>();
        entityExpressions.add(new EntityExpr(Column.AUTHOR, EntityOperator.EQUALS, authorKey));
        return delegator.findByOr(Table.NAME, entityExpressions, EasyList.build(Column.NAME));
    }

    /**
     * @param requestGV the search request record to convert
     * @return IndexableSharedEntity representing the search request record; null if record was null.
     */
    private IndexableSharedEntity<SearchRequest> convertGVToIndexableSharedEntity(final GenericValue requestGV)
    {
        if (requestGV == null)
        {
            return null;
        }

        ApplicationUser author = userManager.getUserByKey(requestGV.getString("author"));
        final String name = requestGV.getString("name");
        final String description = requestGV.getString("description");
        final Long id = requestGV.getLong("id");
        Long favCount = requestGV.getLong("favCount");
        if (favCount == null)
        {
            favCount = 0L;
        }
        return new IndexableSharedEntity<SearchRequest>(id, name, description, SearchRequest.ENTITY_TYPE, author, favCount);
    }

    // TODO: The following two methods need to be resolved into one.
    // Need to work out what we are going to do with these errors and handle it correctly thrown during creation

    /*
     * Converts a GenericValue to a SearchRequest. Throws DataAccess Exception if SearchException is thrown.
     */

    private SearchRequest convertGVToRequest(final GenericValue requestGV)
    {
        if (requestGV == null)
        {
            return null;
        }

        final Query query = getSearchQueryFromGv(requestGV);
        final ApplicationUser author = userManager.getUserByKeyEvenWhenUnknown(requestGV.getString("author"));
        final String name = requestGV.getString("name");
        final String description = requestGV.getString("description");
        final Long id = requestGV.getLong("id");
        Long favCount = requestGV.getLong("favCount");
        if (favCount == null)
        {
            favCount = 0L;
        }
        return new SearchRequest(query, author, name, description, id, favCount);
    }

    Query getSearchQueryFromGv(final GenericValue searchRequestGv)
    {
        // JRA-19580 - Oracle stores "" as null
        final String queryString = searchRequestGv.getString("request");
        final String jql = (queryString == null) ? "" : queryString;

        try
        {
            return jqlQueryParser.parseQuery(jql);
        }
        catch (JqlParseException e)
        {
            log.error("A JQL query exception was thrown parsing, error loading search request with id '" + searchRequestGv.getLong("id") + "' and name '" + searchRequestGv.getString("name") + "' owned by '" + searchRequestGv.getString("author") + "' query '" + jql + "'.");
            throw new DataAccessException(e);
        }
    }
}
