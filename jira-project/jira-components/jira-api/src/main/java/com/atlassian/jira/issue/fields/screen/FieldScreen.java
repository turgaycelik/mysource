package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreen
{
    Long DEFAULT_SCREEN_ID = 1L;

    Long getId();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    List<FieldScreenTab> getTabs();

    FieldScreenTab getTab(int tabPosition);

    FieldScreenTab addTab(String tabName);

    void removeTab(int tabPosition);

    void moveFieldScreenTabToPosition(int tabPosition, int newPosition);

    void moveFieldScreenTabLeft(int tabPosition);

    void moveFieldScreenTabRight(int tabPosition);

    void resequence();

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    /**
     * Indicates whether any fields of the screen have been modified
     */
    boolean isModified();

    void store();

    void remove();

    void setId(Long id);

    boolean containsField(String fieldId);

    void removeFieldScreenLayoutItem(String fieldId);
}
