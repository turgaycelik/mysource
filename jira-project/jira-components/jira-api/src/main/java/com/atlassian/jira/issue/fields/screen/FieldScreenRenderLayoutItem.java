package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import webwork.action.Action;

import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreenRenderLayoutItem
{
    OrderableField getOrderableField();

    String getEditHtml(Action action, OperationContext operationContext, Issue issue);

    String getCreateHtml(Action action, OperationContext operationContext, Issue issue);

    String getViewHtml(Action action, OperationContext operationContext, Issue issue);

    String getEditHtml(Action action, OperationContext operationContext, Issue issue, final Map<String, Object> displayParams);

    String getCreateHtml(Action action, OperationContext operationContext, Issue issue, final Map<String, Object> displayParams);

    String getViewHtml(Action action, OperationContext operationContext, Issue issue, final Map<String, Object> displayParams);    

    boolean isShow(Issue issue);

    void populateDefaults(Map fieldValuesHolder, Issue issue);

    boolean isRequired();

    void populateFromIssue(Map fieldValuesHolder, Issue issue);

    String getRendererType();

    FieldLayoutItem getFieldLayoutItem();

    FieldScreenLayoutItem getFieldScreenLayoutItem();
}
