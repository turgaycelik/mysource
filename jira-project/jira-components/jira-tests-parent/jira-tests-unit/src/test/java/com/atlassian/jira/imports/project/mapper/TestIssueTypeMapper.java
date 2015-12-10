package com.atlassian.jira.imports.project.mapper;

import java.util.Collection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestIssueTypeMapper
{
    @Test
    public void testGetMappedId()
    {
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("10", "110");
        issueTypeMapper.mapValue("34", "134");
        issueTypeMapper.mapValue("76", "176");

        assertEquals("110", issueTypeMapper.getMappedId("10"));
        assertEquals("134", issueTypeMapper.getMappedId("34"));
        assertEquals("176", issueTypeMapper.getMappedId("76"));
        assertEquals(null, issueTypeMapper.getMappedId("176"));
    }

    @Test
    public void testIssueTypeSubTasks()
    {
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.registerOldValue("10", "Bug", true);
        issueTypeMapper.registerOldValue("34", "Improvement", false);
        issueTypeMapper.mapValue("10", "110");
        issueTypeMapper.mapValue("34", "134");
        issueTypeMapper.mapValue("76", "176");

        assertEquals("110", issueTypeMapper.getMappedId("10"));
        assertEquals("134", issueTypeMapper.getMappedId("34"));
        assertEquals("176", issueTypeMapper.getMappedId("76"));
        assertEquals(null, issueTypeMapper.getMappedId("176"));
        assertTrue(issueTypeMapper.isSubTask("10"));
        assertFalse(issueTypeMapper.isSubTask("34"));
    }

    @Test
    public void testGetRequiredValues()
    {
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("10");
        issueTypeMapper.flagValueAsRequired("11");
        issueTypeMapper.flagValueAsRequired("12");
        issueTypeMapper.registerOldValue("10", "Bug", true);
        issueTypeMapper.registerOldValue("11", "Improvement", true);

        Collection required = issueTypeMapper.getRequiredOldIds();
        assertEquals(3, required.size());
        assertTrue(required.contains("10"));
        assertTrue(required.contains("11"));
        assertTrue(required.contains("12"));
        assertEquals("Bug", issueTypeMapper.getKey("10"));
        assertEquals("Improvement", issueTypeMapper.getKey("11"));
    }
    
    @Test
    public void testGetValuesFromImport()
    {
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.registerOldValue("10", "Bug", true);
        issueTypeMapper.registerOldValue("34", "Improvement", false);

        final Collection valuesFromImport = issueTypeMapper.getValuesFromImport();
        assertEquals(2, valuesFromImport.size());
        assertTrue(valuesFromImport.contains(new IdKeyPair("10", "Bug")));
        assertTrue(valuesFromImport.contains(new IdKeyPair("34", "Improvement")));
    }

    @Test
    public void testNullMappings()
    {
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("10", "20");
        // Register the new ID as null
        issueTypeMapper.mapValue("10", null);

        assertEquals("20", issueTypeMapper.getMappedId("10"));
    }

    @Test
    public void testFlagNull()
    {
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired(null);
        assertTrue(issueTypeMapper.getRequiredOldIds().isEmpty());
    }

    @Test
    public void testGetDisplayName()
    {
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.registerOldValue("10", "Bug", true);
        issueTypeMapper.registerOldValue("11", "Improvement", true);

        assertEquals("Bug", issueTypeMapper.getDisplayName("10"));
        assertEquals("Improvement", issueTypeMapper.getDisplayName("11"));
        assertEquals("[12]", issueTypeMapper.getDisplayName("12"));
    }
}
