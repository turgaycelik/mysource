package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class DefaultFieldScreenStore implements FieldScreenStore
{
    private static final Logger log = Logger.getLogger(DefaultFieldScreenStore.class);

    private final OfBizDelegator ofBizDelegator;
    private final FieldManager fieldManager;
    private FieldScreenManager fieldScreenManager;

    public DefaultFieldScreenStore(OfBizDelegator ofBizDelegator, FieldManager fieldManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.fieldManager = fieldManager;
    }

    public void setFieldScreenManager(FieldScreenManager fieldScreenManager)
    {
        this.fieldScreenManager = fieldScreenManager;
    }

    public FieldScreen getFieldScreen(Long id)
    {
        GenericValue fieldScreenGV = ofBizDelegator.findByPrimaryKey(FIELD_SCREEN_ENTITY_NAME, id);
        if (fieldScreenGV == null)
        {
            return null;
        }

        return buildFieldScreenObject(fieldScreenGV);
    }

    public List<Long> getFieldScreenIds()
    {
        List<Long> fieldScreens = new LinkedList<Long>();
        List<GenericValue> fieldScreenGVs = ofBizDelegator.findByCondition(FIELD_SCREEN_ENTITY_NAME, null, Collections.singletonList("id"));
        for (final GenericValue fieldScreenGV : fieldScreenGVs)
        {
            fieldScreens.add(fieldScreenGV.getLong("id"));
        }

        return fieldScreens;
    }

    public List<FieldScreen> getFieldScreens()
    {
        List<FieldScreen> fieldScreens = new LinkedList<FieldScreen>();
        List<GenericValue> fieldScreenGVs = ofBizDelegator.findAll(FIELD_SCREEN_ENTITY_NAME, Collections.singletonList("name"));
        for (final GenericValue fieldScreenGV : fieldScreenGVs)
        {
            fieldScreens.add(buildFieldScreenObject(fieldScreenGV));
        }

        return fieldScreens;
    }

    public void createFieldScreen(FieldScreen fieldScreen)
    {
        Map<String, Object> params = MapBuilder.<String, Object>newBuilder()
                .add("name", fieldScreen.getName())
                .add("description", fieldScreen.getDescription())
                .toMutableMap();
        if (fieldScreen.getId() != null)
        {
            params.put("id", fieldScreen.getId());
        }

        GenericValue fieldScreenGV = ofBizDelegator.createValue(FieldScreenStore.FIELD_SCREEN_ENTITY_NAME, params);
        fieldScreen.setGenericValue(fieldScreenGV);
    }

    public void removeFieldScreen(Long id)
    {
        ofBizDelegator.removeByAnd(FieldScreenStore.FIELD_SCREEN_ENTITY_NAME, MapBuilder.build("id", id));
    }

    public void updateFieldScreen(FieldScreen fieldScreen)
    {
        ofBizDelegator.store(fieldScreen.getGenericValue());
    }

    public void createFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        GenericValue fieldScreenGV = ofBizDelegator.createValue(FIELD_SCREEN_TAB_ENTITY_NAME,
                MapBuilder.<String, Object>newBuilder().add("name", fieldScreenTab.getName()).add("sequence", (long)fieldScreenTab.getPosition())
                        .add("fieldscreen", fieldScreenTab.getFieldScreen().getId()).toMap());
        fieldScreenTab.setGenericValue(fieldScreenGV);
    }

    public void updateFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        ofBizDelegator.store(fieldScreenTab.getGenericValue());
    }

    public List<FieldScreenTab> getFieldScreenTabs(FieldScreen fieldScreen)
    {
        List<FieldScreenTab> fieldScreenTabs = new ArrayList<FieldScreenTab>();
        List<GenericValue> fieldScreenTabGVs = ofBizDelegator.findByAnd(FieldScreenStore.FIELD_SCREEN_TAB_ENTITY_NAME,
                MapBuilder.build("fieldscreen", fieldScreen.getId()),
                Collections.singletonList("sequence"));
        for (final GenericValue fieldScreenTabGV : fieldScreenTabGVs)
        {
            FieldScreenTab fieldScreenTab = new FieldScreenTabImpl(fieldScreenManager, fieldScreenTabGV);
            fieldScreenTab.setFieldScreen(fieldScreen);
            fieldScreenTabs.add(fieldScreenTab);
        }

        return fieldScreenTabs;
    }

    public void updateFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        ofBizDelegator.store(fieldScreenLayoutItem.getGenericValue());
    }

    public void removeFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        ofBizDelegator.removeByAnd(FIELD_SCREEN_LAYOUT_ITEM_ENTITY_NAME, MapBuilder.build("id", fieldScreenLayoutItem.getId()));
    }

    public void removeFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        ofBizDelegator.removeByAnd(FieldScreenStore.FIELD_SCREEN_LAYOUT_ITEM_ENTITY_NAME, MapBuilder.build("fieldscreentab", fieldScreenTab.getId()));
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        // Leave this as array list as index access is required.
        List<FieldScreenLayoutItem> fieldScreenLayoutItems = new ArrayList<FieldScreenLayoutItem>();
        List<GenericValue> fieldScreenLayoutItemGVs = ofBizDelegator.findByAnd(FieldScreenStore.FIELD_SCREEN_LAYOUT_ITEM_ENTITY_NAME,
                MapBuilder.build("fieldscreentab", fieldScreenTab.getId()),
                Collections.singletonList("sequence"));
        for (final GenericValue fieldScreenLayoutItemGV : fieldScreenLayoutItemGVs)
        {
            FieldScreenLayoutItem fieldScreenLayoutItem = buildNewFieldScreenLayoutItem(fieldScreenLayoutItemGV);
            fieldScreenLayoutItem.setFieldScreenTab(fieldScreenTab);
            fieldScreenLayoutItems.add(fieldScreenLayoutItem);
        }

        return fieldScreenLayoutItems;
    }

    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(GenericValue genericValue)
    {
        return new FieldScreenLayoutItemImpl(fieldScreenManager, fieldManager, genericValue);
    }

    public void removeFieldScreenTabs(FieldScreen fieldScreen)
    {
        ofBizDelegator.removeByAnd(FieldScreenStore.FIELD_SCREEN_TAB_ENTITY_NAME, MapBuilder.build("fieldscreen", fieldScreen.getId()));
    }

    public void removeFieldScreenTab(Long id)
    {
        ofBizDelegator.removeByAnd(FieldScreenStore.FIELD_SCREEN_TAB_ENTITY_NAME, MapBuilder.build("id", id));
    }

    public FieldScreenTab getFieldScreenTab(Long fieldScreenTabId)
    {
        GenericValue fieldScreenTabGV = ofBizDelegator.findByPrimaryKey(FieldScreenStore.FIELD_SCREEN_TAB_ENTITY_NAME, fieldScreenTabId);

        if (fieldScreenTabGV != null)
        {
            FieldScreenTab fieldScreenTab = new FieldScreenTabImpl(fieldScreenManager, fieldScreenTabGV);
            final FieldScreen fieldScreen = getFieldScreen(fieldScreenTabGV.getLong("fieldscreen"));
            fieldScreenTab.setFieldScreen(fieldScreen);

            return fieldScreenTab;
        }
        else
        {
            log.warn("No field screen tab found for id " + fieldScreenTabId);
            return null;
        }
    }

    public void refresh()
    {
        // Do nothing as there is nothing cached here
    }

    public void createFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        GenericValue genericValue = ofBizDelegator.createValue(FieldScreenStore.FIELD_SCREEN_LAYOUT_ITEM_ENTITY_NAME,
                MapBuilder.<String, Object>newBuilder().add("fieldidentifier", fieldScreenLayoutItem.getFieldId())
                        .add("sequence", (long)fieldScreenLayoutItem.getPosition())
                        .add("fieldscreentab", fieldScreenLayoutItem.getFieldScreenTab().getId())
                        .toMap());
        fieldScreenLayoutItem.setGenericValue(genericValue);
    }

    private FieldScreen buildFieldScreenObject(GenericValue fieldScreenGV)
    {
        return new FieldScreenImpl(fieldScreenManager, fieldScreenGV);
    }
}
