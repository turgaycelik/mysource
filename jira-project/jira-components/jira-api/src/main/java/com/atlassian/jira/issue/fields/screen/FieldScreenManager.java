package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreenManager
{
    // Field Screens
    FieldScreen getFieldScreen(Long id);

    Collection<FieldScreen> getFieldScreens();

    Collection<FieldScreenTab> getFieldScreenTabs(String fieldId);

    void createFieldScreen(FieldScreen fieldScreen);

    void updateFieldScreen(FieldScreen fieldScreen);

    void removeFieldScreen(Long id);

    // Field Screen Tabs
    void createFieldScreenTab(FieldScreenTab fieldScreenTab);

    void updateFieldScreenTab(FieldScreenTab fieldScreenTab);

    void removeFieldScreenTabs(FieldScreen fieldScreen);

    void removeFieldScreenTab(Long id);

    List<FieldScreenTab> getFieldScreenTabs(FieldScreen fieldScreen);

    // Field Screen Layout Items
    void createFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem);

    void updateFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem);

    void removeFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem);

    void removeFieldScreenLayoutItems(FieldScreenTab fieldScreenTab);

    List<FieldScreenLayoutItem> getFieldScreenLayoutItems(FieldScreenTab fieldScreenTab);

    void removeFieldScreenItems(String fieldId);

    void refresh();

    FieldScreenLayoutItem buildNewFieldScreenLayoutItem(String fieldId);

    FieldScreenTab getFieldScreenTab(Long fieldScreenTabId);
}
