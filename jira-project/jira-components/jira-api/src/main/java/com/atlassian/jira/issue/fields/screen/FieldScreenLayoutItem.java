package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import webwork.action.Action;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreenLayoutItem
{
    Long getId();

    /**
     * The position of the layout item on the {@link FieldScreenTab}
     */
    int getPosition();

    void setPosition(int position);

    String getFieldId();

    void setFieldId(String fieldId);

    OrderableField getOrderableField();

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue);

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue);

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue);

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, final Map<String, Object> displayParams);

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, final Map<String, Object> displayParams);

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, final Map<String, Object> displayParams);

    public boolean isShown(Issue issue);

    public FieldScreenTab getFieldScreenTab();

    void setFieldScreenTab(FieldScreenTab fieldScreenTab);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    void store();
    
    void remove();
}
