package com.atlassian.jira.config.properties;

import java.io.InputStream;
import java.util.LinkedHashMap;

import com.atlassian.jira.bc.admin.ApplicationPropertyMetadata;
import com.atlassian.validation.EnumValidator;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Unit test for {@link MetadataLoader}.
 *
 *
 * @since v4.4
 */
public class TestMetadataLoader extends TestCase
{
    private LinkedHashMap<String, ApplicationPropertyMetadata> map;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MetadataLoader ml = new MetadataLoader();

        InputStream xmlStream = getClass().getResourceAsStream("testmetadata.xml");
        map = ml.loadMetadata(xmlStream, "testmetadata.xml");
    }

    @Test
    public void testLoadMetadata() throws Exception {

        ApplicationPropertyMetadata metadata = getAndCheck("simple.key", "boolean", "true");
        assertEquals("Simple Name", metadata.getName());
        assertEquals("Yo, Simple Description, Dog", metadata.getDescription());
        assertEquals(false, metadata.isSysadminEditable());
        assertEquals(true, metadata.isRequiresRestart());

        metadata = getAndCheck("enum.key", "enum", "ni");
        assertEquals("Enumeration Test", metadata.getName());
        assertEquals("This tests multiple options in an enumeration", metadata.getDescription());
        assertEquals(true, metadata.isSysadminEditable());
        assertEquals(false, metadata.isRequiresRestart());
        assertTrue(metadata.getValidator() instanceof EnumValidator);
        assertTrue(metadata.getValidator().validate("ichi").isValid());
        assertTrue(metadata.getValidator().validate("ni").isValid());
        assertTrue(metadata.getValidator().validate("san").isValid());
        assertFalse(metadata.getValidator().validate("foo").isValid());

        metadata = getAndCheck("jira.special.number", "uint", "5000");
        assertEquals(true, metadata.isSysadminEditable());

        metadata = getAndCheck("jira.scrabble.words", "list<string>", "qat,qis,qua,suq,qi");
        assertEquals(true, metadata.getValidator().validate("mock").isValid());
        assertEquals(false, metadata.getValidator().validate("wok").isValid());
        assertEquals(true, metadata.isSysadminEditable());

        metadata = getAndCheck("jira.minimal.option", "music genre", "bubblegum-dubstep");
        assertEquals(true, metadata.isSysadminEditable());
        assertEquals(true, metadata.isRequiresRestart());


    }

    private ApplicationPropertyMetadata getAndCheck(String key, String type, String defaultValue)
    {
        ApplicationPropertyMetadata metadata = map.get(key);
        assertEquals(key, metadata.getKey());
        assertEquals(type, metadata.getType());
        assertEquals(defaultValue, metadata.getDefaultValue());
        return metadata;
    }
}
