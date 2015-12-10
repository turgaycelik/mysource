package com.atlassian.jira.imports.project.mapper;

import java.util.Collection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestSimpleProjectImportIdMapper
{
    @Test
    public void testGetMappedId()
    {
        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.mapValue("10", "110");
        simpleProjectImportIdMapper.mapValue("34", "134");
        simpleProjectImportIdMapper.mapValue("76", "176");
        // this should be ignored
        simpleProjectImportIdMapper.mapValue("76", null);

        assertEquals("110", simpleProjectImportIdMapper.getMappedId("10"));
        assertEquals("134", simpleProjectImportIdMapper.getMappedId("34"));
        assertEquals("176", simpleProjectImportIdMapper.getMappedId("76"));
        assertEquals(null, simpleProjectImportIdMapper.getMappedId("176"));
    }

    @Test
    public void testFlagValueAsRequired()
    {
        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("11");
        simpleProjectImportIdMapper.flagValueAsRequired("12");

        Collection requiredOldIds = simpleProjectImportIdMapper.getRequiredOldIds();
        assertEquals(2, requiredOldIds.size());
        assertTrue(requiredOldIds.contains("11"));
        assertTrue(requiredOldIds.contains("12"));
    }

    @Test
    public void testRegisterOldValue()
    {
        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.registerOldValue("11", "Red");
        simpleProjectImportIdMapper.registerOldValue("12", "Blue");

        assertEquals("Red", simpleProjectImportIdMapper.getKey("11"));
        assertEquals("Red", simpleProjectImportIdMapper.getDisplayName("11"));
        assertEquals("Blue", simpleProjectImportIdMapper.getKey("12"));
        assertEquals("Blue", simpleProjectImportIdMapper.getDisplayName("12"));
        assertEquals(null, simpleProjectImportIdMapper.getKey("13"));
        assertEquals("[13]", simpleProjectImportIdMapper.getDisplayName("13"));
    }

    @Test
    public void testNullMappings()
    {
        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("10");
        simpleProjectImportIdMapper.flagValueAsRequired("11");
        simpleProjectImportIdMapper.registerOldValue("10", "Bug");
        simpleProjectImportIdMapper.registerOldValue("11", "Improvement");
        // Register the new ID as null
        simpleProjectImportIdMapper.mapValue("10", null);

        Collection unMapped = simpleProjectImportIdMapper.getRequiredOldIds();
        assertEquals(2, unMapped.size());
        assertTrue(unMapped.contains("10"));
        assertTrue(unMapped.contains("11"));
    }
}
