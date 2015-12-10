package com.atlassian.jira.web.component;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestWebComponentUtils
{

    @Test
    public void testConvertStringToCollectionNull()
    {
        final Collection collection = WebComponentUtils.convertStringToCollection(null);
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testConvertStringToCollectionEmptyString()
    {
        final Collection collection = WebComponentUtils.convertStringToCollection("");
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testConvertStringToCollectionEmptySingleValue()
    {
        final Collection collection = WebComponentUtils.convertStringToCollection("Java ruleZ!");
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.size());
        assertTrue(collection.contains("Java ruleZ!"));
    }

    @Test
    public void testConvertStringToCollectionEmptyMultipleValues()
    {
        final Collection collection = WebComponentUtils.convertStringToCollection("Java,ruleZ,!");
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
        assertEquals(3, collection.size());
        assertTrue(collection.contains("Java"));
        assertTrue(collection.contains("ruleZ"));
        assertTrue(collection.contains("!"));
        assertFalse(collection.contains("java"));
        assertFalse(collection.contains("JAVA"));
    }

    @Test
    public void testGetRemovalValuesNullParams()
    {
        final Collection collection = WebComponentUtils.getRemovalValues(null, "");
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testGetRemovalValuesEmptyParams()
    {
        final Collection collection = WebComponentUtils.getRemovalValues(new HashMap(), "");
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testGetRemovalValuesNotFound()
    {
        final HashMap params = new HashMap();
        params.put("groovy", "rocks");
        params.put("groovy.and.java", "work together");
        final Collection collection = WebComponentUtils.getRemovalValues(params, "java");
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    @Test
    public void testGetRemovalValuesFound()
    {
        final HashMap params = new HashMap();
        params.put("groovy", "rocks");
        params.put("groovy.and.java", "work together");
        params.put("java.and.groovy", "grails?");
        params.put("java_and_groovy", "grails!");
        final Collection collection = WebComponentUtils.getRemovalValues(params, "java_");
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
        assertEquals(1, collection.size());
        assertTrue(collection.contains("and_groovy"));
    }

    @Test
    public void testGetRemovalValuesFoundWeird()
    {
        final HashMap params = new HashMap();
        params.put("java_lang.test", "test");
        params.put("java_lang_", "hmmm");
        params.put("java_lang_groovy", "yuppie!");
        params.put("java_langthis_is_wrong", "nooo!");
        final Collection collection = WebComponentUtils.getRemovalValues(params, "java_lang_");
        assertNotNull(collection);
        assertFalse(collection.isEmpty());
        assertEquals(2, collection.size());
        assertTrue(collection.contains(""));
        assertTrue(collection.contains("groovy"));
    }

}
