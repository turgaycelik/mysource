/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.annotations.PublicApi;

/**
 * A marker interface to describe the default editable field layout.
 * <b>Note</b>: These are called Field Configurations in the UI.
 */
@PublicApi
public interface EditableDefaultFieldLayout extends EditableFieldLayout
{
    public static final String NAME = "Default Field Configuration";
    public static final String DESCRIPTION = "The default field configuration";
}
