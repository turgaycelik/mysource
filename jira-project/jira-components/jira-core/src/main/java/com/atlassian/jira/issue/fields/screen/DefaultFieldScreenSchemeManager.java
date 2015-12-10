package com.atlassian.jira.issue.fields.screen;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.map.CacheObject;

import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@EventComponent
public class DefaultFieldScreenSchemeManager implements FieldScreenSchemeManager
{
    private static final String FIELD_SCREEN_SCHEME_ITEM_ENTITY_NAME = "FieldScreenSchemeItem";

    private final OfBizDelegator ofBizDelegator;
    private final FieldScreenManager fieldScreenManager;

    private final Cache<Long, CacheObject<FieldScreenScheme>> schemeCache;

    public DefaultFieldScreenSchemeManager(final OfBizDelegator ofBizDelegator, final FieldScreenManager fieldScreenManager, final CacheManager cacheManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.fieldScreenManager = fieldScreenManager;
        schemeCache = cacheManager.getCache(DefaultFieldScreenSchemeManager.class.getName() + ".schemeCache",
                new FieldScreenSchemeCacheLoader(),
                new CacheSettingsBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build());
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public Collection<FieldScreenScheme> getFieldScreenSchemes()
    {
        return buildFieldScreenSchemes(ofBizDelegator.findAll(FIELD_SCREEN_SCHEME_ENTITY_NAME, Collections.singletonList("name")));
    }

    private Collection<FieldScreenScheme> buildFieldScreenSchemes(final List<GenericValue> fieldScreenSchemeGVs)
    {
        final List<FieldScreenScheme> fieldScreenSchemes = new LinkedList<FieldScreenScheme>();
        for (final GenericValue element : fieldScreenSchemeGVs)
        {
            fieldScreenSchemes.add(buildFieldScreenScheme(element));
        }
        return fieldScreenSchemes;
    }

    public FieldScreenScheme getFieldScreenScheme(final Long id)
    {
        return schemeCache.get(id).getValue();
    }

    protected FieldScreenScheme retrieveFieldScreenScheme(final Long id)
    {
        GenericValue fieldScreenSchemeGV = getOfBizDelegator().findById(FIELD_SCREEN_SCHEME_ENTITY_NAME, id);
        return buildFieldScreenScheme(fieldScreenSchemeGV);
    }

    protected FieldScreenSchemeImpl buildFieldScreenScheme(final GenericValue genericValue)
    {
        return new FieldScreenSchemeImpl(this, genericValue);
    }

    public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems(final FieldScreenScheme fieldScreenScheme)
    {
        final List<FieldScreenSchemeItem> fieldScreenSchemeItems = new LinkedList<FieldScreenSchemeItem>();
        final List<GenericValue> fieldScreenSchemeItemGVs = ofBizDelegator.findByAnd(FIELD_SCREEN_SCHEME_ITEM_ENTITY_NAME,
                MapBuilder.build("fieldscreenscheme", fieldScreenScheme.getId()));
        for (final GenericValue fieldScreenSchemeItemGV : fieldScreenSchemeItemGVs)
        {
            final FieldScreenSchemeItem fieldScreenSchemeItem = buildFieldScreenSchemeItem(fieldScreenSchemeItemGV);
            fieldScreenSchemeItem.setFieldScreenScheme(fieldScreenScheme);
            fieldScreenSchemeItems.add(fieldScreenSchemeItem);
        }

        return fieldScreenSchemeItems;
    }

    protected FieldScreenSchemeItem buildFieldScreenSchemeItem(final GenericValue genericValue)
    {
        final FieldScreenSchemeItem fieldScreenSchemeItem = new FieldScreenSchemeItemImpl(this, genericValue, fieldScreenManager);
        fieldScreenSchemeItem.setIssueOperation(IssueOperations.getIssueOperation(genericValue.getLong("operation")));
        fieldScreenSchemeItem.setFieldScreen(fieldScreenManager.getFieldScreen(genericValue.getLong("fieldscreen")));
        return fieldScreenSchemeItem;
    }

    public void createFieldScreenScheme(final FieldScreenScheme fieldScreenScheme)
    {
        //Used by upgrade tasks - so should stay here for all editions of JIRA
        final MapBuilder<String, Object> params = MapBuilder.<String, Object>newBuilder("name", fieldScreenScheme.getName());
        params.add("description", fieldScreenScheme.getDescription()).toMap();
        if (fieldScreenScheme.getId() != null)
        {
            params.add("id", fieldScreenScheme.getId());
        }

        final GenericValue fieldScreenSchemeGV = ofBizDelegator.createValue(FIELD_SCREEN_SCHEME_ENTITY_NAME, params.toMap());
        fieldScreenScheme.setGenericValue(fieldScreenSchemeGV);
        // We cache misses, so best to be sure to invalidate this entry if it exists, which it probably won't
        schemeCache.remove(fieldScreenScheme.getId());
    }

    public void updateFieldScreenScheme(final FieldScreenScheme fieldScreenScheme)
    {
        //Used by upgrade tasks - so should stay here for all editions of JIRA
        ofBizDelegator.store(fieldScreenScheme.getGenericValue());
        schemeCache.remove(fieldScreenScheme.getId());
    }

    public void removeFieldSchemeItems(FieldScreenScheme fieldScreenScheme)
    {
        getOfBizDelegator().removeByAnd(FIELD_SCREEN_SCHEME_ITEM_ENTITY_NAME, MapBuilder.build("fieldscreenscheme", fieldScreenScheme.getId()));
    }

    public void removeFieldScreenScheme(final FieldScreenScheme fieldScreenScheme)
    {
        ofBizDelegator.removeByAnd(FIELD_SCREEN_SCHEME_ENTITY_NAME, MapBuilder.build("id", fieldScreenScheme.getId()));
        schemeCache.remove(fieldScreenScheme.getId());
    }

    public void createFieldScreenSchemeItem(final FieldScreenSchemeItem fieldScreenSchemeItem)
    {
        Long issueOperationId = null;
        if (fieldScreenSchemeItem.getIssueOperation() != null)
        {
            issueOperationId = fieldScreenSchemeItem.getIssueOperation().getId();
        }

        Map<String, Object> builder = MapBuilder.<String, Object>newBuilder("operation", issueOperationId)
                .add("fieldscreen", fieldScreenSchemeItem.getFieldScreen().getId())
                .add("fieldscreenscheme", fieldScreenSchemeItem.getFieldScreenScheme().getId())
                .toMap();

        final GenericValue fieldScreenSchemeItemGV = ofBizDelegator.createValue(FIELD_SCREEN_SCHEME_ITEM_ENTITY_NAME, builder);
        fieldScreenSchemeItem.setGenericValue(fieldScreenSchemeItemGV);

        schemeCache.remove(fieldScreenSchemeItem.getFieldScreenScheme().getId());
    }

    public void updateFieldScreenSchemeItem(final FieldScreenSchemeItem fieldScreenSchemeItem)
    {
        ofBizDelegator.store(fieldScreenSchemeItem.getGenericValue());
        schemeCache.remove(fieldScreenSchemeItem.getFieldScreenScheme().getId());
    }

    public void removeFieldScreenSchemeItem(final FieldScreenSchemeItem fieldScreenSchemeItem)
    {
        ofBizDelegator.removeByAnd(FIELD_SCREEN_SCHEME_ITEM_ENTITY_NAME, MapBuilder.build("id", fieldScreenSchemeItem.getId()));
        schemeCache.remove(fieldScreenSchemeItem.getFieldScreenScheme().getId());
    }

    public Collection<FieldScreenScheme> getFieldScreenSchemes(final FieldScreen fieldScreen)
    {
        final Set<FieldScreenScheme> fieldScreenSchemes = new LinkedHashSet<FieldScreenScheme>();
        // Find all the field screen scheme items with this field screen
        final List<GenericValue> fieldScreenSchemeItemGVs = ofBizDelegator.findByAnd(FIELD_SCREEN_SCHEME_ITEM_ENTITY_NAME,
            MapBuilder.build("fieldscreen", fieldScreen.getId()), Collections.singletonList("name"));
        for (final GenericValue fieldScreenSchemeItemGV : fieldScreenSchemeItemGVs)
        {
            fieldScreenSchemes.add(getFieldScreenScheme(fieldScreenSchemeItemGV.getLong("fieldscreenscheme")));
        }

        return fieldScreenSchemes;
    }

    protected OfBizDelegator getOfBizDelegator()
    {
        return ofBizDelegator;
    }

    public void refresh()
    {
        schemeCache.removeAll();
    }

    private class FieldScreenSchemeCacheLoader implements CacheLoader<Long, CacheObject<FieldScreenScheme>>
    {
        @Override
        public CacheObject<FieldScreenScheme> load(@Nonnull final Long id)
        {
            final FieldScreenScheme fieldScreenScheme = retrieveFieldScreenScheme(id);
            // retrieveFieldScreenScheme always returns something even if its content is empty
            if (fieldScreenScheme.getId() == null)
            {
                return CacheObject.NULL();
            }
            return CacheObject.wrap(fieldScreenScheme);
        }

    }
}
