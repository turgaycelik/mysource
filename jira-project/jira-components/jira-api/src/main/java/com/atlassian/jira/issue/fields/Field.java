/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;

@PublicApi
public interface Field extends Comparable
{
    /**
     * The unique id of the field
     */
    String getId();

    /**
     * The i18n key that is used to lookup the field's name when it is displayed
     */
    String getNameKey();

    /**
     * Returns i18n'ed name of the field.
     */
    String getName();
}
