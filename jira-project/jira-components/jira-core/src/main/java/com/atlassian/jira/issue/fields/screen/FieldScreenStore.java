package com.atlassian.jira.issue.fields.screen;

import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public interface FieldScreenStore
{
    String FIELD_SCREEN_ENTITY_NAME = "FieldScreen";
    String FIELD_SCREEN_TAB_ENTITY_NAME = "FieldScreenTab";
    String FIELD_SCREEN_LAYOUT_ITEM_ENTITY_NAME = "FieldScreenLayoutItem";

    void setFieldScreenManager(FieldScreenManager fieldScreenManager);

    FieldScreen getFieldScreen(Long id);

    /**
     * Returns a List of FieldScreen Ids.
     * @return a List of FieldScreen ids.
     */
    List<Long> getFieldScreenIds();

    /**
     * Returns a List of FieldScreen objects, sorted by name.
     * @return a List of FieldScreen objects, sorted by name.
     */
    List<FieldScreen> getFieldScreens();

    void createFieldScreen(FieldScreen fieldScreen);

    void removeFieldScreen(Long id);

    void updateFieldScreen(FieldScreen fieldScreen);

    void createFieldScreenTab(FieldScreenTab fieldScreenTab);

    void updateFieldScreenTab(FieldScreenTab fieldScreenTab);

    List<FieldScreenTab> getFieldScreenTabs(FieldScreen fieldScreen);

    void updateFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem);

    void removeFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem);

    void removeFieldScreenLayoutItems(FieldScreenTab fieldScreenTab);

    List<FieldScreenLayoutItem> getFieldScreenLayoutItems(FieldScreenTab fieldScreenTab);

    void refresh();

    void createFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem);

    FieldScreenLayoutItem buildNewFieldScreenLayoutItem(GenericValue genericValue);

    void removeFieldScreenTabs(FieldScreen fieldScreen);

    void removeFieldScreenTab(Long id);

    FieldScreenTab getFieldScreenTab(Long fieldScreenTabId);
}
