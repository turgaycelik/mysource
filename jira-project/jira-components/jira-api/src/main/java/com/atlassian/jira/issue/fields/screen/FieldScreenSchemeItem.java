package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.operation.ScreenableIssueOperation;
import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreenSchemeItem extends Comparable<FieldScreenSchemeItem>
{
    Long getId();

    ScreenableIssueOperation getIssueOperation();

    void setIssueOperation(ScreenableIssueOperation issueOperation);

    FieldScreen getFieldScreen();

    String getIssueOperationName();

    void setFieldScreen(FieldScreen fieldScreen);

    FieldScreenScheme getFieldScreenScheme();

    void setFieldScreenScheme(FieldScreenScheme fieldScreenScheme);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    void store();

    void remove();

    Long getFieldScreenId();
}
