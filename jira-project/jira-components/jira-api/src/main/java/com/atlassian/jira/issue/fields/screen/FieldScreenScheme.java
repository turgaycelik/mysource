package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.operation.IssueOperation;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreenScheme
{
    Long getId();

    void setId(Long id);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    FieldScreenSchemeItem getFieldScreenSchemeItem(IssueOperation issueOperation);

    Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems();

    void addFieldScreenSchemeItem(FieldScreenSchemeItem fieldScreenSchemeItem);

    FieldScreenSchemeItem removeFieldScreenSchemeItem(IssueOperation issueOperation);

    FieldScreen getFieldScreen(IssueOperation issueOperation);

    void store();

    void remove();
}
