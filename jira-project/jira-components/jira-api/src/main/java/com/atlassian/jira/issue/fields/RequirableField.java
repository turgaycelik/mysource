/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;

/**
 * A marker interface for fields in JIRA which can be made "required" as part of a {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout}.
 */
@PublicApi
public interface RequirableField extends Field
{
}
