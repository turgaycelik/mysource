package com.atlassian.jira.issue.customfields;

import java.util.Arrays;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestCustomFieldTypeCategory
{
    @Test
    public void testFromString()
    {
        assertEquals(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.fromString("ALL").get());
        assertEquals(CustomFieldTypeCategory.STANDARD, CustomFieldTypeCategory.fromString("STANDARD").get());
        assertEquals(CustomFieldTypeCategory.ADVANCED, CustomFieldTypeCategory.fromString("ADVANCED").get());

        assertEquals(CustomFieldTypeCategory.ALL, CustomFieldTypeCategory.fromString("all").get());
        assertEquals(CustomFieldTypeCategory.STANDARD, CustomFieldTypeCategory.fromString("standard").get());
        assertEquals(CustomFieldTypeCategory.ADVANCED, CustomFieldTypeCategory.fromString("adVANCed").get());

        assertFalse(CustomFieldTypeCategory.fromString("random").isPresent());
        assertFalse(CustomFieldTypeCategory.fromString(null).isPresent());
    }

    @Test
    public void testGetNameI18nKey()
    {
        assertNotNull(CustomFieldTypeCategory.ALL.getNameI18nKey());
        assertNotNull(CustomFieldTypeCategory.STANDARD.getNameI18nKey());
        assertNotNull(CustomFieldTypeCategory.ADVANCED.getNameI18nKey());
    }

    @Test
    public void testOrder()
    {
        assertEquals(Arrays.asList(CustomFieldTypeCategory.ALL,
                CustomFieldTypeCategory.STANDARD,
                CustomFieldTypeCategory.ADVANCED),
                CustomFieldTypeCategory.orderedValues());
    }
}
