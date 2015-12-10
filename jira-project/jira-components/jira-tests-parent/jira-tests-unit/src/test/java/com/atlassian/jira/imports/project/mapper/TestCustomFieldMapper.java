package com.atlassian.jira.imports.project.mapper;

import java.util.Collection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestCustomFieldMapper
{
    @Test
    public void testGetIssueTypeIdsForRequiredCustomfield()
    {
        CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        // First flag custom fields used by issues
        customFieldMapper.flagValueAsRequired("12", "1");
        customFieldMapper.flagValueAsRequired("12", "2");
        customFieldMapper.flagValueAsRequired("13", "1");
        // Now flag the Issue Types for these Issues.
        customFieldMapper.flagIssueTypeInUse("1", "30");
        customFieldMapper.flagIssueTypeInUse("2", "31");
        // Flag an Issue that can be ignored
        customFieldMapper.flagIssueTypeInUse("102", "89745324");

        customFieldMapper.registerIssueTypesInUse();

        assertEquals(2, customFieldMapper.getIssueTypeIdsForRequiredCustomField("12").size());
        assertTrue(customFieldMapper.getIssueTypeIdsForRequiredCustomField("12").contains("30"));
        assertTrue(customFieldMapper.getIssueTypeIdsForRequiredCustomField("12").contains("31"));
        assertEquals(1, customFieldMapper.getIssueTypeIdsForRequiredCustomField("13").size());
        assertTrue(customFieldMapper.getIssueTypeIdsForRequiredCustomField("13").contains("30"));
        assertNull(customFieldMapper.getIssueTypeIdsForRequiredCustomField("14"));
    }

    @Test
    public void testGetRequiredOldIds()
    {
        CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1");
        customFieldMapper.flagValueAsRequired("12", "2");
        customFieldMapper.flagValueAsRequired("13", "1");
        customFieldMapper.registerOldValue("12", "CF1");
        customFieldMapper.registerOldValue("13", "CF2");
        customFieldMapper.registerOldValue("14", "CF3");

        Collection requiredValues = customFieldMapper.getRequiredOldIds();
        assertEquals(2, requiredValues.size());
        assertTrue(requiredValues.contains("12"));
        assertTrue(requiredValues.contains("13"));
        assertEquals("CF1", customFieldMapper.getKey("12"));
        assertEquals("CF1", customFieldMapper.getDisplayName("12"));
        assertEquals(null, customFieldMapper.getKey("1012"));
        assertEquals("[1012]", customFieldMapper.getDisplayName("1012"));
    }

    @Test
    public void testGetValuesFromImport()
    {
        CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.registerOldValue("12", "Colour");
        customFieldMapper.registerOldValue("13", "Flavour");
        customFieldMapper.registerOldValue("14", "Spin");

        Collection values = customFieldMapper.getValuesFromImport();
        assertEquals(3, values.size());
        assertTrue(values.contains(new IdKeyPair("12", "Colour")));
        assertTrue(values.contains(new IdKeyPair("13", "Flavour")));
        assertTrue(values.contains(new IdKeyPair("14", "Spin")));
    }

    @Test
    public void testGetMappedId()
    {
        CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("12", "100");
        customFieldMapper.mapValue("13", "105");

        assertEquals("100", customFieldMapper.getMappedId("12"));
        assertEquals("105", customFieldMapper.getMappedId("13"));
        assertEquals(null, customFieldMapper.getMappedId("14"));
    }

    @Test
    public void testMapNull()
    {
        CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("12", "100");
        customFieldMapper.mapValue("12", null);

        assertEquals("100", customFieldMapper.getMappedId("12"));
    }

    @Test
    public void testFlagNullValuesAsRequired()
    {
        CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", null);
        customFieldMapper.flagValueAsRequired(null, "100");
        customFieldMapper.flagValueAsRequired(null, null);

        assertEquals(0, customFieldMapper.getRequiredOldIds().size());
    }

    @Test
    public void testIssueTypesIsSetOfUniqueValues()
    {
        CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        // First flag custom fields used by issues
        customFieldMapper.flagValueAsRequired("12", "100");
        customFieldMapper.flagValueAsRequired("12", "101");
        customFieldMapper.flagValueAsRequired("12", "102");
        // Now flag the Issue Types for these Issues.
        customFieldMapper.flagIssueTypeInUse("100", "30");
        customFieldMapper.flagIssueTypeInUse("101", "30");
        customFieldMapper.flagIssueTypeInUse("102", "30");

        customFieldMapper.registerIssueTypesInUse();

        assertEquals(1, customFieldMapper.getRequiredOldIds().size());
        assertEquals(1, customFieldMapper.getIssueTypeIdsForRequiredCustomField("12").size());
    }

}
