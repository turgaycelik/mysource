package com.atlassian.jira.issue.fields.layout.column;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.map.CacheObject;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.ASSIGNEE;
import static com.atlassian.jira.issue.IssueFieldConstants.CREATED;
import static com.atlassian.jira.issue.IssueFieldConstants.DUE_DATE;
import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_KEY;
import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.jira.issue.IssueFieldConstants.PRIORITY;
import static com.atlassian.jira.issue.IssueFieldConstants.REPORTER;
import static com.atlassian.jira.issue.IssueFieldConstants.RESOLUTION;
import static com.atlassian.jira.issue.IssueFieldConstants.STATUS;
import static com.atlassian.jira.issue.IssueFieldConstants.SUMMARY;
import static com.atlassian.jira.issue.IssueFieldConstants.UPDATED;
import static com.atlassian.jira.issue.fields.layout.column.ColumnLayout.ColumnConfig.FILTER;
import static com.atlassian.jira.issue.fields.layout.column.ColumnLayout.ColumnConfig.SYSTEM;
import static com.atlassian.jira.issue.fields.layout.column.ColumnLayout.ColumnConfig.USER;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.map.CacheObject.wrap;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

@ClusterSafe("Mostly, due to session affinity.  We should revisit this, but using a cluster lock would be overkill for the risk")
@EventComponent
public class DefaultColumnLayoutManager implements ColumnLayoutManager
{
    private static final Logger log = Logger.getLogger(DefaultColumnLayoutManager.class);

    private final Cache<CacheObject<String>, CacheableColumnLayout> userColumnLayoutCache;
    private final Cache<Long, CacheObject<CacheableColumnLayout>> filterColumnLayoutCache;

    private final OfBizDelegator ofBizDelegator;
    private final FieldManager fieldManager;
    private final ColumnLayout defaultColumnLayout;
    private final UserKeyService userKeyService;


    public DefaultColumnLayoutManager(
            final FieldManager fieldManager,
            final OfBizDelegator ofBizDelegator,
            final UserKeyService userKeyService,
            final CacheManager cacheManager)
    {
        this.fieldManager = fieldManager;
        this.ofBizDelegator = ofBizDelegator;
        this.userKeyService = userKeyService;

        this.userColumnLayoutCache = cacheManager.getCache(DefaultColumnLayoutManager.class.getName() + ".userColumnLayoutCache",
                new UserColumnLayoutCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
        this.filterColumnLayoutCache = cacheManager.getCache(DefaultColumnLayoutManager.class.getName() + ".filterColumnLayoutCache",
                new FilterColumnLayoutCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());

        int pos = 0;
        final List<ColumnLayoutItem> cols = newArrayListWithCapacity(11);
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(ISSUE_TYPE), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(ISSUE_KEY), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(SUMMARY), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(ASSIGNEE), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(REPORTER), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(PRIORITY), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(STATUS), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(RESOLUTION), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(CREATED), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(UPDATED), pos++));
        cols.add(new ColumnLayoutItemImpl(fieldManager.getNavigableField(DUE_DATE), pos++));
        defaultColumnLayout = new DefaultColumnLayoutImpl(ImmutableList.copyOf(cols));
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    @Override
    public void refresh()
    {
        // Clear all caches
        userColumnLayoutCache.removeAll();
        filterColumnLayoutCache.removeAll();
    }

    @Override
    public ColumnLayout getColumnLayout(User remoteUser) throws ColumnLayoutStorageException
    {
        final String username = (remoteUser == null ? null : remoteUser.getName());
        final CacheableColumnLayout cacheableColumnLayout = getUserColumnLayout(username);

        final List<ColumnLayoutItem> columnLayoutItems = removeUnavailableColumnLayoutItemsForUser(cacheableColumnLayout.columnLayoutItems, remoteUser);
        return new UserColumnLayoutImpl(columnLayoutItems, remoteUser, cacheableColumnLayout.columnConfig);
    }

    @Override
    public ColumnLayout getDefaultColumnLayout(User remoteUser) throws ColumnLayoutStorageException
    {
        final CacheableColumnLayout defaultColumnLayout = getUserColumnLayout(null);

        final List<ColumnLayoutItem> columnLayoutItems = removeUnavailableColumnLayoutItemsForUser(defaultColumnLayout.columnLayoutItems, remoteUser);
        return new DefaultColumnLayoutImpl(columnLayoutItems);
    }

    @Override
    public ColumnLayout getDefaultColumnLayout() throws ColumnLayoutStorageException
    {
        return new DefaultColumnLayoutImpl(defaultColumnLayout.getColumnLayoutItems());
    }

    @Override
    public EditableDefaultColumnLayout getEditableDefaultColumnLayout()
    {
        final CacheableColumnLayout defaultColumnLayout = getUserColumnLayout(null);

        final List<ColumnLayoutItem> columnLayoutItems = removeUnavailableColumnLayoutItems(defaultColumnLayout.columnLayoutItems);
        return new EditableDefaultColumnLayoutImpl(columnLayoutItems);
    }

    @Override
    public EditableUserColumnLayout getEditableUserColumnLayout(User user)
    {
        notNull("user", user);

        final CacheableColumnLayout columnLayout = getUserColumnLayout(user.getName());

        final List<ColumnLayoutItem> columnLayoutItems = removeUnavailableColumnLayoutItemsForUser(columnLayout.columnLayoutItems, user);
        return new EditableUserColumnLayoutImpl(columnLayoutItems, user, columnLayout.columnConfig);
    }

    @Override
    public void storeEditableDefaultColumnLayout(EditableDefaultColumnLayout editableDefaultColumnLayout)
            throws ColumnLayoutStorageException
    {
        storeEditableColumnLayout(editableDefaultColumnLayout, null);

        // The default column layout has changed, as many users might be using the default
        // column layout clear the whole cache
        userColumnLayoutCache.removeAll();
    }

    @Override
    public void storeEditableUserColumnLayout(EditableUserColumnLayout editableUserColumnLayout)
            throws ColumnLayoutStorageException
    {
        final String userName = editableUserColumnLayout.getUser().getName();
        final String userKey = notNull("userKey", userKeyService.getKeyForUsername(userName));

        storeEditableColumnLayout(editableUserColumnLayout, userKey);

        // Clear the user's column layout items from cache
        userColumnLayoutCache.remove(wrap(userKey));
    }

    @Override
    public void restoreDefaultColumnLayout()
    {
        restoreColumnLayout(null, null);

        // The default column layout has changed, as many users might be using the default
        // column layout clear the whole cache
        userColumnLayoutCache.removeAll();
    }

    @Override
    public void restoreUserColumnLayout(User user)
    {
        notNull("user", user);
        final String userKey = userKeyService.getKeyForUser(user);
        if (userKey == null)
        {
            log.warn("Unable to restore column layout for nonexistant user '" + user.getName() + '\'');
            return;
        }

        restoreColumnLayout(userKey, user.getName());

        // Clear the user's column layout items from cache
        userColumnLayoutCache.remove(wrap(userKey));
    }

    @Override
    public ColumnLayout getColumnLayout(User remoteUser, SearchRequest searchRequest)
            throws ColumnLayoutStorageException
    {
        final String username = (remoteUser == null ? null : remoteUser.getName());

        // need to check for search request specific column layout items
        final CacheableColumnLayout cols = getFilterColumnLayoutItems(username, searchRequest);

        final List<ColumnLayoutItem> columnLayoutItems = removeUnavailableColumnLayoutItemsForUser(cols.columnLayoutItems, remoteUser);
        return new UserColumnLayoutImpl(columnLayoutItems, remoteUser, cols.columnConfig);
    }

    @Override
    public EditableSearchRequestColumnLayout getEditableSearchRequestColumnLayout(User user, SearchRequest searchRequest)
    {
        notNull("user", user);
        notNull("searchRequest", searchRequest);

        final CacheableColumnLayout cols = getFilterColumnLayoutItems(user.getName(), searchRequest);

        final List<ColumnLayoutItem> columnLayoutItems = removeUnavailableColumnLayoutItemsForUser(cols.columnLayoutItems, user);
        return new EditableSearchRequestColumnLayoutImpl(columnLayoutItems, user, searchRequest, cols.columnConfig);
    }

    @Override
    public void storeEditableSearchRequestColumnLayout(EditableSearchRequestColumnLayout editableSearchRequestColumnLayout)
    {
        final Long filterId = editableSearchRequestColumnLayout.getSearchRequest().getId();
        storeFilterColumnLayout(editableSearchRequestColumnLayout, filterId);

        // Clear the search request column layout and search request column layout items from caches
        filterColumnLayoutCache.remove(filterId);
    }

    @Override
    public void restoreSearchRequestColumnLayout(SearchRequest searchRequest) throws ColumnLayoutStorageException
    {
        resetFilterColumnLayout(searchRequest.getId());

        // Clear the search request column layout and search request column layout items from caches
        filterColumnLayoutCache.remove(searchRequest.getId());
    }

    @Override
    public boolean hasColumnLayout(SearchRequest searchRequest)
    {
        return hasColumnLayout(searchRequest.getId());
    }

    @Override
    public boolean hasColumnLayout(Long filterId)
    {
        // If the search request is not saved (loaded) it cannot have a column layout
        if (null == filterId)
        {
            return false;
        }

        final CacheObject<CacheableColumnLayout> filterColumnLayout = filterColumnLayoutCache.get(filterId);
        return filterColumnLayout.hasValue();
    }

    @Override
    public boolean hasColumnLayout(User user)
    {
        notNull("user", user);

        return hasDefaultColumnLayout(user);
    }

    @Override
    public boolean hasDefaultColumnLayout() throws ColumnLayoutStorageException
    {
        return hasDefaultColumnLayout(null);
    }

    private boolean hasDefaultColumnLayout(User user)
    {
        final String username = (user != null) ? user.getName() : null;
        final String userKey = userKeyService.getKeyForUsername(username);
        return (fetchUserColumnLayout(userKey) != null);
    }

    private CacheableColumnLayout getUserColumnLayout(String username)
    {
        final String userKey = userKeyService.getKeyForUsername(username);
        return userColumnLayoutCache.get(wrap(userKey));
    }

    /**
     * THIS METHOD MUST BE SYNCHRONIZED!!!! So that only one thread updates the database at any one time. "Columns are
     * duplicated" if this method is not synchronized.
     */
    private synchronized void storeEditableColumnLayout(ColumnLayout columnLayout, String userKey)
    {
        try
        {
            GenericValue columnLayoutGV = fetchUserColumnLayout(userKey);
            if (columnLayoutGV == null)
            {
                // There is no default, create a new one
                columnLayoutGV = EntityUtils.createValue("ColumnLayout", MapBuilder.<String, Object>build("username", userKey, "searchrequest", null));
            }
            storeColumnLayoutItems(columnLayoutGV, columnLayout);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Could not load ColumnLayout", e);
        }
    }

    private void storeColumnLayoutItems(GenericValue columnLayoutGV, ColumnLayout columnLayout)
            throws GenericEntityException
    {
        // Remove ColumnLayout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
        List<GenericValue> columnLayoutItemGVs = ofBizDelegator.getRelated("ChildColumnLayoutItem", columnLayoutGV);
        ofBizDelegator.removeAll(columnLayoutItemGVs);

        // Retrieve a list of Column Layout Items for this layout
        final List<ColumnLayoutItem> columnLayoutItems = columnLayout.getColumnLayoutItems();
        for (int i = 0; i < columnLayoutItems.size(); i++)
        {
            ColumnLayoutItem columnLayoutItem = columnLayoutItems.get(i);
            EntityUtils.createValue("ColumnLayoutItem", MapBuilder.<String, Object>build(
                    "columnlayout", columnLayoutGV.getLong("id"),
                    "fieldidentifier", columnLayoutItem.getId(),
                    "horizontalposition", new Long(i)));
        }
    }

    /**
     * Restore system defaults by removing the configured layout from the permanent store
     */
    private synchronized void restoreColumnLayout(String userKey, String username)
    {
        try
        {
            GenericValue columnLayoutGV = fetchUserColumnLayout(userKey);
            if (columnLayoutGV != null)
            {
                removeColumnLayoutItems(columnLayoutGV);
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error removing column layout for username " + username + ".", e);
        }
    }

    private GenericValue fetchUserColumnLayout(final String userKey)
    {
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", FieldMap.build("username", userKey, "searchrequest", null)));
    }

    private void removeColumnLayoutItems(GenericValue columnLayoutGV) throws GenericEntityException
    {
        // Remove Column Layout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
        List<GenericValue> columnLayoutItemGVs = ofBizDelegator.getRelated("ChildColumnLayoutItem", columnLayoutGV);
        ofBizDelegator.removeAll(columnLayoutItemGVs);
        ofBizDelegator.removeValue(columnLayoutGV);
    }

    private CacheableColumnLayout getFilterColumnLayoutItems(String username, SearchRequest searchRequest)
    {
        final CacheObject<CacheableColumnLayout> filterColumnLayout = filterColumnLayoutCache.get(searchRequest.getId());
        if (filterColumnLayout.hasValue())
        {
            return filterColumnLayout.getValue();
        }
        else
        {
            // Filter has no columns, fall back to the user's columns and then the default
            return getUserColumnLayout(username);
        }
    }

    private void storeFilterColumnLayout(ColumnLayout columnLayout, Long filterId)
    {
        try
        {
            // Find the column layout in the database if it exists
            GenericValue columnLayoutGV = fetchFilterColumnLayout(filterId);

            if (columnLayoutGV == null)
            {
                // There is no search request layout, create a new one
                columnLayoutGV = EntityUtils.createValue("ColumnLayout", MapBuilder.<String, Object>build("username", null, "searchrequest", filterId));
            }

            storeColumnLayoutItems(columnLayoutGV, columnLayout);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Could not load ColumnLayout", e);
        }
    }

    private GenericValue fetchFilterColumnLayout(final Long filterId)
    {
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("ColumnLayout", FieldMap.build("username", null, "searchrequest", filterId)));
    }

    private void resetFilterColumnLayout(Long filterId)
    {
        // Restore system defaults by removing the configured defaults from the permanent store - DB
        try
        {
            GenericValue columnLayoutGV = fetchFilterColumnLayout(filterId);
            if (columnLayoutGV != null)
            {
                removeColumnLayoutItems(columnLayoutGV);
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Error removing column layout for search request with id  '" + filterId + "'.", e);
        }
    }

    private List<ColumnLayoutItem> removeUnavailableColumnLayoutItems(final List<ColumnLayoutItem> columnLayoutItems)
    {
        try
        {
            return newArrayList(filter(columnLayoutItems, new IsAvailableFieldPredicate(fieldManager.getAllAvailableNavigableFields())));
        }
        catch (FieldException e)
        {
            throw new DataAccessException("Could not retrieve available fields.", e);
        }
    }

    private List<ColumnLayoutItem> removeUnavailableColumnLayoutItemsForUser(final List<ColumnLayoutItem> columnLayoutItems, final User user)
    {
        try
        {
            return newArrayList(filter(columnLayoutItems, new IsAvailableFieldPredicate(fieldManager.getAvailableNavigableFields(user))));
        }
        catch (FieldException e)
        {
            throw new DataAccessException("Could not retrieve available fields.", e);
        }
    }

    private List<ColumnLayoutItem> transformToColumnLayoutItems(final GenericValue columnLayoutGV)
    {
        final List<GenericValue> columnLayoutItemGVs = ofBizDelegator.getRelated("ChildColumnLayoutItem", columnLayoutGV, ImmutableList.of("horizontalposition ASC"));
        final List<ColumnLayoutItem> columnLayoutItems = Lists.newArrayListWithCapacity(columnLayoutItemGVs.size());

        for (GenericValue columnLayoutItemGV : columnLayoutItemGVs)
        {
            if (fieldManager.isNavigableField(columnLayoutItemGV.getString("fieldidentifier")))
            {
                final NavigableField navigableField = fieldManager.getNavigableField(columnLayoutItemGV.getString("fieldidentifier"));
                columnLayoutItems.add(new ColumnLayoutItemImpl(navigableField, columnLayoutItemGV.getLong("horizontalposition").intValue()));
            }
        }
        return columnLayoutItems;
    }

    /**
     * Only allows ColumnLayoutItems that represent available navigable fields.
     */
    private static class IsAvailableFieldPredicate implements Predicate<ColumnLayoutItem>
    {
        private final Set<NavigableField> availableFields;

        private IsAvailableFieldPredicate(final Set<NavigableField> availableFields)
        {
            this.availableFields = availableFields;
        }

        @Override
        public boolean apply(@Nullable final ColumnLayoutItem input)
        {
            return input != null && availableFields.contains(input.getNavigableField());
        }
    }

    /**
     * Replacement for ColumnLayout, does not actually implement the existing interface because it's rubbish.
     */
    private static final class CacheableColumnLayout
    {
        private final ColumnLayout.ColumnConfig columnConfig;
        private final List<ColumnLayoutItem> columnLayoutItems;

        private CacheableColumnLayout(final ColumnLayout.ColumnConfig columnConfig, final List<ColumnLayoutItem> columnLayoutItems)
        {
            this.columnConfig = columnConfig;
            this.columnLayoutItems = columnLayoutItems;
        }
    }

    private class UserColumnLayoutCacheLoader implements CacheLoader<CacheObject<String>, CacheableColumnLayout>
    {
        @Override
        public CacheableColumnLayout load(@Nonnull final CacheObject<String> userKey)
        {
            GenericValue columnLayoutGV = userKey.hasValue() ? fetchUserColumnLayout(userKey.getValue()) : null;
            if (columnLayoutGV == null)
            {
                // The user has no column layout return user the default
                columnLayoutGV = fetchUserColumnLayout(null);
                if (columnLayoutGV == null)
                {
                    return new CacheableColumnLayout(SYSTEM, defaultColumnLayout.getColumnLayoutItems());
                }
            }

            final List<ColumnLayoutItem> columnLayoutItems = transformToColumnLayoutItems(columnLayoutGV);
            return new CacheableColumnLayout(USER, columnLayoutItems);
        }
    }

    private class FilterColumnLayoutCacheLoader implements CacheLoader<Long, CacheObject<CacheableColumnLayout>>
    {
        @Override
        public CacheObject<CacheableColumnLayout> load(@Nonnull final Long filterId)
        {
            final GenericValue columnLayoutGV = fetchFilterColumnLayout(filterId);
            if (columnLayoutGV != null)
            {
                return wrap(new CacheableColumnLayout(FILTER, transformToColumnLayoutItems(columnLayoutGV)));
            }
            return CacheObject.NULL();
        }
    }
}

