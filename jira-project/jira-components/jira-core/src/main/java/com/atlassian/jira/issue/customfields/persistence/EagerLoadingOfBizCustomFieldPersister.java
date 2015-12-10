package com.atlassian.jira.issue.customfields.persistence;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.util.collect.MapBuilder;

import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

/**
 * Many times, the values for custom fields are all retrieved at the same time - such as viewing all values for an
 * issue, or indexing all custom fields. <p/> It therefore makes sense to eagerly loaded, reducing the number of
 * database calls needed.
 */
public class EagerLoadingOfBizCustomFieldPersister extends OfBizCustomFieldValuePersister
{
    public EagerLoadingOfBizCustomFieldPersister(final OfBizDelegator delegator)
    {
        super(delegator);
    }

    @Override
    public void createValues(final CustomField field, final Long issueId, final PersistenceFieldType persistenceFieldType, final Collection values)
    {
        super.createValues(field, issueId, persistenceFieldType, values);
        clearCache();
    }

    @Override
    public void createValues(final CustomField field, final Long issueId, final PersistenceFieldType persistenceFieldType, final Collection values, final String parentKey)
    {
        super.createValues(field, issueId, persistenceFieldType, values, parentKey);
        clearCache();
    }

    @Override
    public void updateValues(final CustomField field, final Long issueId, final PersistenceFieldType persistenceFieldType, final Collection values)
    {
        super.updateValues(field, issueId, persistenceFieldType, values);
        clearCache();
    }

    @Override
    public void updateValues(final CustomField field, final Long issueId, final PersistenceFieldType persistenceFieldType, final Collection values, final String parentKey)
    {
        super.updateValues(field, issueId, persistenceFieldType, values, parentKey);
        clearCache();
    }

    @Override
    public Set<Long> removeValue(final CustomField field, final Long issueId, final PersistenceFieldType persistenceFieldType, final Object value)
    {
        final Set set = super.removeValue(field, issueId, persistenceFieldType, value);
        clearCache();
        return set;
    }

    @Override
    public Set removeAllValues(final String customFieldId)
    {
        final Set set = super.removeAllValues(customFieldId);
        clearCache();
        return set;
    }

    private void clearCache()
    {
        getCache().clear();
    }

    @Override
    protected List<GenericValue> getValuesForTypeAndParent(final CustomField field, final Long issueId, final String parentKey)
    {
        final List cachedValuesForIssue = getValuesForIssueId(issueId);
        return EntityUtil.filterByAnd(cachedValuesForIssue, UtilMisc.toMap(ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(field.getId()),
            ENTITY_PARENT_KEY, parentKey));
    }

    @Override
    protected List<GenericValue> getValuesForType(final CustomField field, final Long issueId)
    {
        final List cachedValuesForIssue = getValuesForIssueId(issueId);
        return EntityUtil.filterByAnd(cachedValuesForIssue, UtilMisc.toMap(ENTITY_CUSTOMFIELD_ID, CustomFieldUtils.getCustomFieldId(field.getId())));
    }

    private List<?> getValuesForIssueId(final Long issueId)
    {
        final Map<CacheKey, SoftReference<List<?>>> queryToResultsCache = getCache();
        final CacheKey key = new CacheKey(issueId);

        // use a SoftReference as reindexAll can put a whole lot of crazy stuff in here...
        // see JRA-12411
        final SoftReference<List<?>> cachedValuesReference = queryToResultsCache.get(key);
        List<?> cachedValuesForIssue = (cachedValuesReference == null) ? null : cachedValuesReference.get();
        if (cachedValuesForIssue == null)
        {
            final Map<String, Object> limitClause = MapBuilder.<String, Object>build(ENTITY_ISSUE_ID, issueId);
            cachedValuesForIssue = delegator.findByAnd(TABLE_CUSTOMFIELD_VALUE, limitClause);
            queryToResultsCache.put(key, new SoftReference<List<?>>(cachedValuesForIssue));
        }
        return cachedValuesForIssue;
    }

    /**
     * A cache to store the custom field values in. Remember this guy is threadlocal so no need to synchronise
     */
    private Map<CacheKey, SoftReference<List<?>>> getCache()
    {
        @SuppressWarnings("unchecked") @ClusterSafe
        Map<CacheKey, SoftReference<List<?>>> cache = (Map<CacheKey, SoftReference<List<?>>>) JiraAuthenticationContextImpl.getRequestCache().get(
            RequestCacheKeys.CUSTOMFIELD_VALUES_CACHE);
        if (cache == null)
        {
            cache = new ConcurrentHashMap<CacheKey, SoftReference<List<?>>>();
            JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.CUSTOMFIELD_VALUES_CACHE, cache);
        }
        return cache;
    }

    static final class CacheKey
    {
        private final Long issueId;

        CacheKey(final Long issueId)
        {
            this.issueId = issueId;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null)
            {
                return false;
            }
            if (!getClass().equals(o.getClass()))
            {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return (issueId == null) ? (cacheKey.issueId == null) : issueId.equals(cacheKey.issueId);
        }

        @Override
        public int hashCode()
        {
            return (issueId != null ? issueId.hashCode() : 0);
        }
    }
}