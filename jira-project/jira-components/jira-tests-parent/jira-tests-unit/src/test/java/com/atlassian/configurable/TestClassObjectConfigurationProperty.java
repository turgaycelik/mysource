package com.atlassian.configurable;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.jira.util.JiraUtils.loadComponent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith (MockitoJUnitRunner.class)
public class TestClassObjectConfigurationProperty
{
    ValuesGeneratorObjectConfigurationProperty property;

    @Test
    public void testNullEnabledCondition()
    {
        property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, MyValueGenerator.class.getName(), null);
        assertTrue("testNullEnabledCondition", property.isEnabled());
    }

    @Test
    public void testNotEnabledCondition()
    {
        property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue", ObjectConfigurationTypes.SELECT, MyValueGenerator.class.getName(), NotEnabledCondition.class.getName());
        assertFalse("testNotEnabledCondition", property.isEnabled());
    }

    @Test
    public void testValuesGenerator()
    {
        property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue",
                ObjectConfigurationTypes.SELECT, MyValueGenerator.class.getName(), NotEnabledCondition.class.getName())
        {
            @Override
            ValuesGenerator createValuesGenerator(final Class<ValuesGenerator> valuesGeneratorClass)
            {
                return new MyValueGenerator();
            }
        };
        assertEquals("1", property.get("one"));
    }

    @Test
    public void testNullValuesGenerator()
    {
        property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue",
                ObjectConfigurationTypes.SELECT, null, NotEnabledCondition.class.getName())
        {
            @Override
            ValuesGenerator createValuesGenerator(final Class<ValuesGenerator> valuesGeneratorClass)
            {
                return new MyValueGenerator();
            }
        };
        assertNull(property.get("one"));
    }

    @Test
    public void testBogusValuesGenerator()
    {
        property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue",
                ObjectConfigurationTypes.SELECT, "blah.de.1234.blah", NotEnabledCondition.class.getName())
        {
            @Override
            ValuesGenerator createValuesGenerator(final Class<ValuesGenerator> valuesGeneratorClass)
            {
                return new MyValueGenerator();
            }
        };
        assertNull(property.get("one"));
    }

    @Test
    public void testIncorrectValuesGenerator()
    {
        property = new ValuesGeneratorObjectConfigurationProperty("name", "description", "defaultValue",
                ObjectConfigurationTypes.SELECT, Object.class.getName(), NotEnabledCondition.class.getName())
        {
            @Override
            ValuesGenerator createValuesGenerator(final Class<ValuesGenerator> valuesGeneratorClass)
            {
                return (ValuesGenerator) new Object();
            }
        };
        assertNull(property.get("one"));
    }

    static class MyValueGenerator implements ValuesGenerator
    {
        public Map getValues(Map userParams)
        {
            Map<String, String> values = new HashMap<String, String>();
            values.put("one", "1");
            values.put("two", "2");
            return values;
        }
    }
}
