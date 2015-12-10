package com.atlassian.jira.imports.project.mapper;

import java.util.Collection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestStatusMapper
{
    @Test
    public void testGetIssueTypeIdsForRequiredStatus()
    {
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("12", "1");
        statusMapper.flagValueAsRequired("12", "2");
        statusMapper.flagValueAsRequired("13", "1");

        assertNull(statusMapper.getIssueTypeIdsForRequiredStatus("14"));
        assertEquals(2, statusMapper.getIssueTypeIdsForRequiredStatus("12").size());
        assertTrue(statusMapper.getIssueTypeIdsForRequiredStatus("12").contains("1"));
        assertTrue(statusMapper.getIssueTypeIdsForRequiredStatus("12").contains("2"));
        assertEquals(1, statusMapper.getIssueTypeIdsForRequiredStatus("13").size());
        assertTrue(statusMapper.getIssueTypeIdsForRequiredStatus("13").contains("1"));
    }

    @Test
    public void testGetRequiredOldIds()
    {
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("12", "1");
        statusMapper.flagValueAsRequired("12", "2");
        statusMapper.flagValueAsRequired("13", "1");

        Collection requiredValues = statusMapper.getRequiredOldIds();
        assertEquals(2, requiredValues.size());
        assertTrue(requiredValues.contains("12"));
        assertTrue(requiredValues.contains("13"));
    }

    @Test
    public void testGetValuesFromImport()
    {
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.registerOldValue("12", "Closed");
        statusMapper.registerOldValue("13", "In Progress");
        statusMapper.registerOldValue("14", "Open");

        Collection values = statusMapper.getValuesFromImport();
        assertEquals(3, values.size());
        assertTrue(values.contains(new IdKeyPair("12", "Closed")));
        assertTrue(values.contains(new IdKeyPair("13", "In Progress")));
        assertTrue(values.contains(new IdKeyPair("14", "Open")));
    }
    
    @Test
    public void testGetMappedId()
    {
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.mapValue("12", "100");
        statusMapper.mapValue("13", "105");

        assertEquals("100", statusMapper.getMappedId("12"));
        assertEquals("105", statusMapper.getMappedId("13"));
        assertEquals(null, statusMapper.getMappedId("14"));
    }

    @Test
    public void testMapNull()
    {
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.mapValue("12", "100");
        statusMapper.mapValue("12", null);

        assertEquals("100", statusMapper.getMappedId("12"));
    }

    @Test
    public void testFlagNullValuesAsRequired()
    {
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("12", null);
        statusMapper.flagValueAsRequired(null, "100");
        statusMapper.flagValueAsRequired(null, null);

        assertEquals(0, statusMapper.getRequiredOldIds().size());
    }

    @Test
    public void testIssueTypesIsSetOfUniqueValues()
    {
        StatusMapper statusMapper = new StatusMapper();
        statusMapper.flagValueAsRequired("12", "100");
        statusMapper.flagValueAsRequired("12", "100");
        statusMapper.flagValueAsRequired("12", "100");

        assertEquals(1, statusMapper.getIssueTypeIdsForRequiredStatus("12").size());
    }

}
