package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreenSchemeManager
{
    public static final String FIELD_SCREEN_SCHEME_ENTITY_NAME = "FieldScreenScheme";
    public static final Long DEFAULT_FIELD_SCREEN_SCHEME_ID = 1L;

    public Collection<FieldScreenScheme> getFieldScreenSchemes();

    public FieldScreenScheme getFieldScreenScheme(Long id);

    Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems(FieldScreenScheme fieldScreenScheme);

    void createFieldScreenScheme(FieldScreenScheme fieldScreenScheme);

    void updateFieldScreenScheme(FieldScreenScheme fieldScreenScheme);

    void removeFieldSchemeItems(FieldScreenScheme fieldScreenScheme);

    void removeFieldScreenScheme(FieldScreenScheme fieldScreenScheme);

    void createFieldScreenSchemeItem(FieldScreenSchemeItem fieldScreenSchemeItem);

    void updateFieldScreenSchemeItem(FieldScreenSchemeItem fieldScreenSchemeItem);

    void removeFieldScreenSchemeItem(FieldScreenSchemeItem fieldScreenSchemeItem);

    Collection<FieldScreenScheme> getFieldScreenSchemes(FieldScreen fieldScreen);

    void refresh();
}
