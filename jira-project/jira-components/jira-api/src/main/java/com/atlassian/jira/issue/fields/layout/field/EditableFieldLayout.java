/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.annotations.PublicApi;

/**
 * Used to describe an EditableFieldLayout.
 * <b>Note</b>: These are called Field Configurations in the UI.
 */
@PublicApi
public interface EditableFieldLayout extends FieldLayout
{
    public void setName(String name);

    public void setDescription(String description);

    public void show(FieldLayoutItem fieldLayoutItem);

    public void hide(FieldLayoutItem fieldLayoutItem);

    public void makeRequired(FieldLayoutItem fieldLayoutItem);

    public void makeOptional(FieldLayoutItem fieldLayoutItem);

    public void setDescription(FieldLayoutItem fieldLayoutItem, String description);

    public void setRendererType(FieldLayoutItem fieldLayoutItem, String rendererType);

    public String getType();
}
