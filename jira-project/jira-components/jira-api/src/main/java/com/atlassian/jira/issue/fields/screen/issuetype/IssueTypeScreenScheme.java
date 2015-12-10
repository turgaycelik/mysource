package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.issuetype.IssueType;

import org.ofbiz.core.entity.GenericValue;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface IssueTypeScreenScheme
{
    Long DEFAULT_SCHEME_ID = 1L;

    Long getId();

    void setId(Long id);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    void store();

    void remove();

    Collection<IssueTypeScreenSchemeEntity> getEntities();

    IssueTypeScreenSchemeEntity getEntity(String issueTypeId);

    /**
     * Return {@link com.atlassian.jira.issue.fields.screen.FieldScreenScheme} that JIRA will actually use
     * for the passed {@link com.atlassian.jira.issue.issuetype.IssueType}. This method will never return
     * {@code null} as each {@code IssueType} must have an associated {@code FieldScreenScheme} that either comes
     * from an explicit mapping or the default mapping in the scheme.
     *
     * @param type the {@code IssueType} to query.
     * @return the {@code FieldScreenScheme} for the passed {@code IssueType}. Cannot be null.
     * @since 6.2
     */
    @Nonnull
    FieldScreenScheme getEffectiveFieldScreenScheme(@Nonnull IssueType type);

    void addEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    void removeEntity(String issueTypeId);

    boolean containsEntity(String issueTypeId);

    Collection<GenericValue> getProjects();

    boolean isDefault();
}
