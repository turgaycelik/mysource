package com.atlassian.jira.issue.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;

@EventComponent
public class DefaultIssueLinkTypeManager implements IssueLinkTypeManager
{
    private final OfBizDelegator delegator;
    private final CachedReference<Map<Long, IssueLinkType>> cache;

    public DefaultIssueLinkTypeManager(final OfBizDelegator delegator, final CacheManager cacheManager)
    {
        this.delegator = delegator;
        this.cache = cacheManager.getCachedReference("com.atlassian.jira.issue.link.DefaultIssueLinkTypeManager.types",
                new IssueLinkTypesSupplier());
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCache();
    }

    public void createIssueLinkType(final String name, final String outward, final String inward, final String style)
    {
        // Ensure all parameters are set
        try
        {
            notBlank("name", name);
            notBlank("outward", outward);
            notBlank("inward", inward);
            delegator.createValue(OfBizDelegator.ISSUE_LINK_TYPE, MapBuilder.<String, Object>build(IssueLinkType.NAME_FIELD_NAME, name,
                IssueLinkType.OUTWARD_FIELD_NAME, outward, IssueLinkType.INWARD_FIELD_NAME, inward, IssueLinkType.STYLE_FIELD_NAME, style));
        }
        finally
        {
            clearCache();
        }
    }

    public IssueLinkType getIssueLinkType(final Long id)
    {
        return getIssueLinkType(id, true);
    }

    @Override
    public IssueLinkType getIssueLinkType(Long id, /* ignored */ boolean excludeSystemLinks)
    {
        return cache.get().get(id);
    }

    public Collection<IssueLinkType> getIssueLinkTypesByName(final String name)
    {
        return buildIssueLinkTypes(queryDatabase(OfBizDelegator.ISSUE_LINK_TYPE, MapBuilder.<String, Object>build(IssueLinkType.NAME_FIELD_NAME, name)), false);
    }

    public Collection<IssueLinkType> getIssueLinkTypesByInwardDescription(final String desc)
    {
        final Predicate<GenericValue> inwardNamePredicate = new Predicate<GenericValue>()
        {
            public boolean evaluate(final GenericValue input)
            {
                return input.getString(IssueLinkType.INWARD_FIELD_NAME).equalsIgnoreCase(desc);
            }
        };
        return getIssueLinkTypesByPredicate(inwardNamePredicate);
    }

    public Collection<IssueLinkType> getIssueLinkTypesByOutwardDescription(final String desc)
    {
        final Predicate<GenericValue> inwardNamePredicate = new Predicate<GenericValue>()
        {
            public boolean evaluate(final GenericValue input)
            {
                return input.getString(IssueLinkType.OUTWARD_FIELD_NAME).equalsIgnoreCase(desc);
            }
        };
        return getIssueLinkTypesByPredicate(inwardNamePredicate);
    }

    private Collection<IssueLinkType> getIssueLinkTypesByPredicate(final Predicate<GenericValue> predicate)
    {
        final Collection<GenericValue> inwardLinkTypes = CollectionUtil.filter(
                queryDatabase(OfBizDelegator.ISSUE_LINK_TYPE, MapBuilder.<String, Object>emptyMap()),
                predicate);

        return buildIssueLinkTypes(inwardLinkTypes, false);
    }

    public Collection<IssueLinkType> getIssueLinkTypesByStyle(final String style)
    {
        return buildIssueLinkTypes(queryDatabase(OfBizDelegator.ISSUE_LINK_TYPE, MapBuilder.<String, Object>build(IssueLinkType.STYLE_FIELD_NAME, style)), false);
    }

    public void updateIssueLinkType(final IssueLinkType issueLinkType, final String name, final String outward, final String inward)
    {
        try
        {
            GenericValue gvIssueLinkType = issueLinkType.getGenericValue();
            gvIssueLinkType.set(IssueLinkType.NAME_FIELD_NAME, name);
            gvIssueLinkType.set(IssueLinkType.OUTWARD_FIELD_NAME, outward);
            gvIssueLinkType.set(IssueLinkType.INWARD_FIELD_NAME, inward);
            issueLinkType.store();
        }
        finally
        {
            clearCache();
        }
    }

    public void removeIssueLinkType(final Long issueLinkTypeId)
    {
        try
        {
            deleteFromDatabase(OfBizDelegator.ISSUE_LINK_TYPE, ImmutableMap.of("id", issueLinkTypeId));
        }
        finally
        {
            clearCache();
        }
    }

    /**
     * Find only the user defined link types
     *
     */
    public Collection<IssueLinkType> getIssueLinkTypes()
    {
        return this.getIssueLinkTypes(true);
    }

    @Override
    public Collection<IssueLinkType> getIssueLinkTypes(boolean excludeSystemLinks)
    {
        Collection<IssueLinkType> types = ImmutableList.copyOf(cache.get().values());
        if (!excludeSystemLinks)
        {
            return types;
        }
        return CollectionUtil.filter(types, new Predicate<IssueLinkType>()
        {
            @Override
            public boolean evaluate(final IssueLinkType type)
            {
                return !type.isSystemLinkType();
            }
        });

    }

    private List<IssueLinkType> buildIssueLinkTypes(final Collection<GenericValue> issueLinkTypeGVs, final boolean excludeSystemLinks)
    {
        final List<IssueLinkType> issueLinkTypes = new ArrayList<IssueLinkType>();
        for (final GenericValue issueLinkTypeGV : issueLinkTypeGVs)
        {
            final IssueLinkType ilt = buildIssueLinkType(issueLinkTypeGV);
            if (!excludeSystemLinks || !ilt.isSystemLinkType())
            {
                issueLinkTypes.add(ilt);
            }
        }
        return issueLinkTypes;
    }

    private void clearCache()
    {
        cache.reset();
    }

    private IssueLinkType buildIssueLinkType(final GenericValue linkTypeGV)
    {
        return new IssueLinkTypeImpl(linkTypeGV);
    }

    private List<GenericValue> queryDatabase(final String entityName, final Map<String, Object> criteria)
    {
        final List<String> sortOrder = Collections.emptyList();
        return queryDatabase(entityName, criteria, sortOrder);
    }

    private List<GenericValue> queryDatabase(final String entityName, final Map<String, Object> criteria, final List<String> sortOrder)
    {
        return delegator.findByAnd(entityName, criteria, sortOrder);
    }

    private void deleteFromDatabase(final String entityName, final Map<String, ?> criteria)
    {
        // Delete the link type from the database
        delegator.removeByAnd(entityName, criteria);
    }

    private class IssueLinkTypesSupplier implements Supplier<Map<Long, IssueLinkType>>
    {
        @Override
        public Map<Long, IssueLinkType> get()
        {
            final Map<Long, IssueLinkType> types = new LinkedHashMap<Long, IssueLinkType>();
            final Collection<GenericValue> gvTypes = delegator.findAll(OfBizDelegator.ISSUE_LINK_TYPE, ImmutableList.of("linkname" + " ASC"));

            if (gvTypes != null)
            {
                for (final GenericValue gv : gvTypes)
                {
                    IssueLinkType type = buildIssueLinkType(gv);
                    types.put(type.getId(), type);
                }
            }

            return types;
        }
    }
}
