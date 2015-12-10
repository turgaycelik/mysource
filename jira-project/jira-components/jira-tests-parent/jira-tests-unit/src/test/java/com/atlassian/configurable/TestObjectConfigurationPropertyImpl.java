/*
 * Created by IntelliJ IDEA.
 * User: owen
 * Date: Jan 23, 2003
 * Time: 2:40:54 PM
 * CVS Revision: $Revision: 1.2 $
 * Last CVS Commit: $Date: 2005/11/22 00:50:07 $
 * Author of last CVS Commit: $Author: detkin $
 * To change this template use Options | File Templates.
 */
package com.atlassian.configurable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class TestObjectConfigurationPropertyImpl
{
    private ObjectConfigurationPropertyImpl values;

    @Before
    public void setUp() throws Exception
    {
        Map<String, String> map = Maps.newHashMap();
        for (int j = 0; j < 6; j++)
        {
            map.put("key" + j, "value" + j);
        }

        values = new ObjectConfigurationPropertyImpl("nameValue", "descriptionValue", "defaultValue", 1, map);
        values.setI18nValues(false);
    }

    @Test
    public void testGetName()
    {
        assertEquals("nameValue", values.getName());
    }

    @Test
    public void testGetDescription()
    {
        assertEquals("descriptionValue", values.getDescription());
    }

    @Test
    public void testGetType()
    {
        assertEquals(1, values.getType());
    }

    @Test
    public void testSize()
    {
        assertEquals(6, values.size());
    }

    @Test
    public void testIsEmpty()
    {
        assertTrue(!values.isEmpty());
    }

    @Test
    public void testContainsKey()
    {
        assertThat((Set<String>) values.keySet(), hasItems(
                "key0",
                "key1",
                "key2",
                "key3",
                "key4",
                "key5"
        ));
    }

    @Test
    public void testContainsValue()
    {
        assertThat((Collection<String>) values.values(), hasItems(
                "value0",
                "value1",
                "value2",
                "value3",
                "value4",
                "value5"
        ));
    }

    @Test
    public void testGet()
    {
        assertThat((String) values.get("key0"), equalTo("value0"));
        assertThat((String) values.get("key1"), equalTo("value1"));
        assertThat((String) values.get("key2"), equalTo("value2"));
        assertThat((String) values.get("key3"), equalTo("value3"));
        assertThat((String) values.get("key4"), equalTo("value4"));
        assertThat((String) values.get("key5"), equalTo("value5"));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testPut()
    {
        values.put(null, null);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testRemove()
    {
        values.remove(null);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testPutAll()
    {
        values.putAll(null);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testClear()
    {
        values.clear();
    }

    @Test
    public void testKeySet()
    {
        assertThat((Set<String>) values.keySet(), hasItems(
                "key0",
                "key1",
                "key2",
                "key3",
                "key4",
                "key5"
        ));
    }

    @Test
    public void testValues()
    {
        assertThat((Collection<String>) values.values(), hasItems(
                "value0",
                "value1",
                "value2",
                "value3",
                "value4",
                "value5"
        ));
    }

    @Test
    public void testEntrySet()
    {
        assertThat((Set<Map.Entry<String, String>>) values.entrySet(), JUnitMatchers.<Map.Entry<String, String>>hasItems(
                new testEntry("key0", "value0"),
                new testEntry("key1", "value1"),
                new testEntry("key2", "value2"),
                new testEntry("key3", "value3"),
                new testEntry("key4", "value4"),
                new testEntry("key5", "value5")
        ));
    }

    @Test
    public void testI18NValues()
    {
        assertFalse(values.isI18nValues());
    }

    @Test
    public void testNotEnabledCondition()
    {
        ObjectConfigurationProperty configurationProperty = new ObjectConfigurationPropertyImpl("nameValue", "descriptionValue", "defaultValue", 1, NotEnabledCondition.class.getName());
        assertFalse(configurationProperty.isEnabled());
    }

    @Test
    public void testEnabledCondition()
    {
        ObjectConfigurationProperty configurationProperty = new ObjectConfigurationPropertyImpl("nameValue", "descriptionValue", "defaultValue", 1, EnabledCondition.TRUE);
        assertTrue(configurationProperty.isEnabled());
    }

    @Test
    public void testConditionEnabledByDefault()
    {
        ObjectConfigurationProperty configurationProperty = new ObjectConfigurationPropertyImpl("nameValue", "descriptionValue", "defaultValue", 1);
        assertTrue(configurationProperty.isEnabled());
    }

    static class testEntry extends AbstractMap.SimpleImmutableEntry<String, String>
    {
        testEntry(String s, String s1)
        {
            super(s, s1);
        }
    }
}
