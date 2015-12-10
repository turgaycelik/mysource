package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface IssueTypeScreenSchemeEntity extends Comparable<IssueTypeScreenSchemeEntity>
{
    Long getId();

    String getIssueTypeId();

    GenericValue getIssueType();

    IssueType getIssueTypeObject();

    void setIssueTypeId(String issueTypeId);

    FieldScreenScheme getFieldScreenScheme();

    void setFieldScreenScheme(FieldScreenScheme fieldScreenScheme);

    IssueTypeScreenScheme getIssueTypeScreenScheme();

    void setIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    void store();

    void remove();

    Long getFieldScreenSchemeId();
}
