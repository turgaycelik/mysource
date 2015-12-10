/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.comparator;

import java.util.Comparator;

import org.ofbiz.core.entity.GenericValue;

/**
 * Compares two CustomField {@code GenericValue}s.
 */
public class CustomFieldComparator implements Comparator<GenericValue>
{
    private static final String CUSTOM_FIELD_ENTITY_NAME = "CustomField";
    private static final String NAME_FIELD = "name";

    public int compare(GenericValue o1, GenericValue o2)
    {
        if (o1 == null && o2 == null)
        {
            return 0;
        }

        if (o1 == null)
        {
            return -1;
        }

        if (o2 == null)
        {
            return 1;
        }

        if (CUSTOM_FIELD_ENTITY_NAME.equals(o1.getEntityName()) && CUSTOM_FIELD_ENTITY_NAME.equals(o2.getEntityName()))
        {
            return CustomFieldComparators.compareNames(o1.getString(NAME_FIELD), o2.getString(NAME_FIELD));
        }
        throw new IllegalArgumentException(
                "Objects passed must be GenericValues of type " + CUSTOM_FIELD_ENTITY_NAME + ".");
    }
}
