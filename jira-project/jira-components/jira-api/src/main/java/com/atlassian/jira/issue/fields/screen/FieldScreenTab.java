package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface  FieldScreenTab
{
    
    Long getId();

    String getName();

    void setName(String name);

    int getPosition();

    void setPosition(int position);

    List<FieldScreenLayoutItem> getFieldScreenLayoutItems();

    FieldScreenLayoutItem getFieldScreenLayoutItem(int poistion);

    void addFieldScreenLayoutItem(String fieldId);

    void addFieldScreenLayoutItem(String fieldId, int position);

    void moveFieldScreenLayoutItemFirst(int fieldPosition);

    void moveFieldScreenLayoutItemUp(int fieldPosition);

    void moveFieldScreenLayoutItemDown(int fieldPosition);

    void moveFieldScreenLayoutItemLast(int fieldPosition);

    FieldScreenLayoutItem removeFieldScreenLayoutItem(int fieldPosition);

    FieldScreenLayoutItem getFieldScreenLayoutItem(String fieldId);
    boolean isContainsField(String fieldId);

    void moveFieldScreenLayoutItemToPosition(Map<Integer, FieldScreenLayoutItem> positionsToFields);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    boolean isModified();

    void setFieldScreen(FieldScreen fieldScreen);

    FieldScreen getFieldScreen();

    void rename(String newName);

    void store();

    void remove();
}
