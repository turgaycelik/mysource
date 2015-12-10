package com.atlassian.jira.imports.project.mapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestAbstractMapper
{
    @Test
    public void testRegisterOldValue()
    {
        AbstractMapper abstractMapper = new AbstractMapper(){};
        abstractMapper.registerOldValue("1", "Apple");
        abstractMapper.registerOldValue("2", "Banana");

        // Check the Registered IDs
        assertEquals(2, abstractMapper.getRegisteredOldIds().size());
        assertTrue(abstractMapper.getRegisteredOldIds().contains("1"));
        assertTrue(abstractMapper.getRegisteredOldIds().contains("2"));
        // Check the Keys
        assertEquals("Apple", abstractMapper.getKey("1"));
        assertEquals("Apple", abstractMapper.getDisplayName("1"));
        assertEquals("Banana", abstractMapper.getKey("2"));
        assertEquals("Banana", abstractMapper.getDisplayName("2"));
        assertEquals(null, abstractMapper.getKey("3"));
        assertEquals("[3]", abstractMapper.getDisplayName("3"));
    }

    @Test
    public void testFlagAsRequired()
    {
        AbstractMapper abstractMapper = new AbstractMapper(){};
        abstractMapper.flagValueAsRequired("1");
        abstractMapper.flagValueAsRequired("3");
        abstractMapper.flagValueAsRequired(null);

        assertEquals(2, abstractMapper.getRequiredOldIds().size());
        assertTrue(abstractMapper.getRequiredOldIds().contains("1"));
        assertTrue(abstractMapper.getRequiredOldIds().contains("3"));
    }

    @Test
    public void testMapValue()
    {
        AbstractMapper abstractMapper = new AbstractMapper(){};
        abstractMapper.mapValue("1", "101");
        abstractMapper.mapValue("2", "102");
        abstractMapper.mapValue("2", null);

        assertEquals("101", abstractMapper.getMappedId("1"));
        assertEquals("102", abstractMapper.getMappedId("2"));
        assertEquals(null, abstractMapper.getMappedId("3"));
    }

    @Test
    public void testClearMappedValues()
    {
        AbstractMapper abstractMapper = new AbstractMapper(){};
        abstractMapper.mapValue("1", "101");
        abstractMapper.mapValue("2", "102");
        abstractMapper.mapValue("2", null);

        assertEquals("101", abstractMapper.getMappedId("1"));
        assertEquals("102", abstractMapper.getMappedId("2"));
        assertEquals(null, abstractMapper.getMappedId("3"));

        // now clear the values and make sure nothing is mapped
        abstractMapper.clearMappedValues();
        assertNull(abstractMapper.getMappedId("1"));
        assertNull(abstractMapper.getMappedId("2"));
        assertNull(abstractMapper.getMappedId("3"));
    }

}
