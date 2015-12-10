package com.atlassian.jira.functest.unittests.config.ps;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import com.atlassian.jira.functest.config.DefaultConfigSequence;
import com.atlassian.jira.functest.config.ps.ConfigPropertySet;
import com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry;
import com.atlassian.jira.functest.config.ps.ConfigPropertySetManager;
import com.atlassian.jira.functest.config.ps.DefaultConfigPropertySetManager;
import junit.framework.TestCase;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry.Type.BOOLEAN;
import static com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry.Type.INTEGER;
import static com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry.Type.LONG;
import static com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry.Type.STRING;
import static com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry.Type.TEXT;

/**
 * Test {@link com.atlassian.jira.functest.config.ps.DefaultConfigPropertySetManager}.
 *
 * @since v4.1
 */
public class TestDefaultConfigPropertySetManager extends TestCase
{
    private static final String ATTRIB_PROPERTY_KEY = "propertyKey";
    private static final String ATTRIB_ID = "id";
    private static final String ATTRIB_VALUE = "value";
    private static final String ATTRIB_TYPE = "type";
    private static final String ATTRIB_ENTITY_NAME = "entityName";
    private static final String ATTRIB_ENTITY_ID = "entityId";
    private static final String ATTRIB_SEQ_NAME = "seqName";
    private static final String ATTRIB_SEQ_ID = "seqId";

    private static final String ELEMENT_OSPROPERTY_ENTRY = "OSPropertyEntry";
    private static final String ELEMENT_OS_PROPERTY_ENTRY = ELEMENT_OSPROPERTY_ENTRY;
    private static final String ELEMENT_OS_PROPERTY_NUMBER = "OSPropertyNumber";
    private static final String ELEMENT_OS_PROPERTY_TEXT = "OSPropertyText";
    private static final String ELEMENT_SEQ_VALUE_ITEM = "SequenceValueItem";
    private static final String ELEMENT_OS_PROPERTY_STRING = "OSPropertyString";

    private static final String ENTITY_ROOT = "entity-engine-xml";

    private long globalId = 10000L;

    public void testLoadPropertySet() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String stringValue = "String";
        final String textValue = "Text";

        final String intKey = "intKey";
        final String longKey = "longKey";
        final String booleanKey = "booleanKey";

        final Element root = createRoot();
        addStringEntry(root, entityName, entityId, stringValue, stringValue);
        addTextEntry(root, entityName, entityId, textValue, textValue);
        addInteger(root, entityName, entityId, intKey, 1);
        addLong(root, entityName, entityId, longKey, 12L);
        addBoolean(root, entityName, entityId, booleanKey, true);

        addInteger(root, entityName, entityId + 1, intKey, Integer.MAX_VALUE);

        final ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(),
                new DefaultConfigSequence(root.getDocument()));
        ConfigPropertySet propertySet = mgr.loadPropertySet(entityName, entityId);

        Map<String, ConfigPropertySetEntry> expectedEntries = new HashMap<String, ConfigPropertySetEntry>();
        
        expectedEntries.put(stringValue, ConfigPropertySetEntry.createStringEntry(stringValue, stringValue));
        expectedEntries.put(textValue, ConfigPropertySetEntry.createTextEntry(textValue, textValue));
        expectedEntries.put(intKey, ConfigPropertySetEntry.createIntegerEntry(intKey, 1));
        expectedEntries.put(longKey, ConfigPropertySetEntry.createLongEntry(longKey, 12L));
        expectedEntries.put(booleanKey, ConfigPropertySetEntry.createBooleanEntry(booleanKey, true));
        assertEquals(expectedEntries, propertySet.entryMap());

        propertySet = mgr.loadPropertySet(entityName, entityId + 1);
        expectedEntries = new HashMap<String, ConfigPropertySetEntry>();
        expectedEntries.put(intKey, ConfigPropertySetEntry.createIntegerEntry(intKey, Integer.MAX_VALUE));
        assertEquals(expectedEntries, propertySet.entryMap());
    }

    public void testLoadPropertySetBrokenXml() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String stringValue = "String";

        //Empty property key.
        Element root = createRoot();
        addStringEntry(root, entityName, entityId, "", stringValue);
        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //bad type.
        root = createRoot();
        addPropertyEntry(root, createId(), entityName, entityId, 78, "blarg");
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }
    }

    public void testLoadPropertyBadString() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String stringValue = "String";

        //No matching value for string property.
        Element root = createRoot();
        addPropertyEntry(root, createId(), entityName, entityId, STRING.getPropertySetType(), stringValue);
        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }
    }

    public void testLoadPropertyBadText() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String stringValue = "String";

        //No value in the value table.
        Element root = createRoot();
        addTextEntry(root, entityName, entityId, stringValue, null);
        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //No matching value for string property.
        root = createRoot();
        addPropertyEntry(root, createId(), entityName, entityId, TEXT.getPropertySetType(), stringValue);
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }
    }

    public void testLoadPropertyBadLong() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String longKey = "longKey";

        //No value in the value table.
        Element root = createRoot();
        addLong(root, entityName, entityId, longKey, null);
        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //NAN
        root = createRoot();
        addStringNumber(root, entityName, entityId, longKey, "something", LONG.getPropertySetType());
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //No matching value for long property.
        root = createRoot();
        addPropertyEntry(root, createId(), entityName, entityId, LONG.getPropertySetType(), longKey);
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }
    }

    public void testLoadPropertyBadInteger() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String intKey = "intKey";

        //No value in the value table.
        Element root = createRoot();
        addInteger(root, entityName, entityId, intKey, null);
        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //NAN
        root = createRoot();
        addStringNumber(root, entityName, entityId, intKey, "something", INTEGER.getPropertySetType());
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //No matching value for int property.
        root = createRoot();
        addPropertyEntry(root, createId(), entityName, entityId, INTEGER.getPropertySetType(), intKey);
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }
    }

    public void testLoadPropertyBadBoolean() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String boolKey = "boolKey";

        //No value in the value table.
        Element root = createRoot();
        addBoolean(root, entityName, entityId, boolKey, null);
        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //NAN
        root = createRoot();
        addStringNumber(root, entityName, entityId, boolKey, "something", BOOLEAN.getPropertySetType());
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }

        //No matching value for int property.
        root = createRoot();
        addPropertyEntry(root, createId(), entityName, entityId, BOOLEAN.getPropertySetType(), boolKey);
        mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        try
        {
            mgr.loadPropertySet(entityName, entityId);
            fail("Should not load property set with bad data.");
        }
        catch (ConfigException e)
        {
            //expected.
        }
    }

    public void testDeletePropertySet()
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String stringValue = "String";
        final String textValue = "Text";

        final String intKey = "intKey";
        final String longKey = "longKey";
        final String booleanKey = "booleanKey";

        final Element root = createRoot();

        addStringEntry(root, entityName, entityId, stringValue, stringValue);
        addTextEntry(root, entityName, entityId, textValue, textValue);
        addInteger(root, entityName, entityId, intKey, 28);
        addLong(root, entityName, entityId, longKey, 12L);
        addBoolean(root, entityName, entityId, booleanKey, true);

        final long otherIntId = addInteger(root, entityName, entityId + 1, intKey, Integer.MAX_VALUE);

        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        mgr.deletePropertySet(entityName, entityId);


        Document doc = root.getDocument();
        assertNoValue(doc, entityName, entityId, stringValue, stringValue);
        assertNoValue(doc, entityName, entityId, textValue, textValue);
        assertNoValue(doc, entityName, entityId, intKey, "28");
        assertNoValue(doc, entityName, entityId, longKey, "12");
        assertNoValue(doc, entityName, entityId, booleanKey, "1");
        //This should not have changed.
        assertInteger(doc, otherIntId, entityName, entityId + 1, intKey, Integer.MAX_VALUE);
    }

    public void testUpdatePropertySet() throws Exception
    {
        final String entityName = "Test";
        final long entityId = 1L;

        final String stringValue = "String";
        final String textValue = "Text";

        final String intKey = "intKey";
        final String longKey = "longKey";
        final String booleanKey = "booleanKey";

        final Element root = createRoot();

        addSequence(root);

        final long stringId = addStringEntry(root, entityName, entityId, stringValue, stringValue);
        final long textId = addTextEntry(root, entityName, entityId, textValue, textValue);
        final long intId = addInteger(root, entityName, entityId, intKey, 1);
        final long longId = addLong(root, entityName, entityId, longKey, 12L);
        final long booleanId = addBoolean(root, entityName, entityId, booleanKey, true);

        final long otherIntId = addInteger(root, entityName, entityId + 1, intKey, Integer.MAX_VALUE);

        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), new DefaultConfigSequence(root.getDocument()));
        final ConfigPropertySet configPropertySet = mgr.loadPropertySet(entityName, entityId);

        //This save should be a no-op.
        configPropertySet.setStringProperty(stringValue, stringValue);
        configPropertySet.setTextProperty(textValue, textValue);
        configPropertySet.setLongProperty(longKey, 12L);
        configPropertySet.setIntegerProperty(intKey, 1);
        configPropertySet.setBooleanProperty(booleanKey, true);
        assertFalse(mgr.savePropertySet(configPropertySet));

        //change the value of a property set.
        configPropertySet.setStringProperty(stringValue, "newStringValue");
        configPropertySet.setTextProperty(textValue, "newTextValue");
        configPropertySet.setIntegerProperty(intKey, 5);
        configPropertySet.setLongProperty(longKey, 19399L);
        configPropertySet.setBooleanProperty(booleanKey, false);

        assertTrue(mgr.savePropertySet(configPropertySet));

        Document doc = root.getDocument();
        //Check a straight up change of values.
        assertString(doc, stringId, entityName, entityId, stringValue, "newStringValue");
        assertText(doc, textId, entityName, entityId, textValue, "newTextValue");
        assertInteger(doc, intId, entityName, entityId, intKey, 5);
        assertLong(doc, longId, entityName, entityId, longKey, 19399L);
        assertBoolean(doc, booleanId, entityName, entityId, booleanKey, false);
        //This should not have changed.
        assertInteger(doc, otherIntId, entityName, entityId + 1, intKey, Integer.MAX_VALUE);

        //Lets try changing the types of all the values.
        configPropertySet.setIntegerProperty(stringValue, 10);
        configPropertySet.setLongProperty(textValue, 101L);
        configPropertySet.setStringProperty(longKey, "long");
        configPropertySet.setBooleanProperty(intKey, false);
        configPropertySet.setTextProperty(booleanKey, "something in the way she moves");

        assertTrue(mgr.savePropertySet(configPropertySet));

        assertInteger(doc, stringId, entityName, entityId, stringValue, 10);
        assertNull(getStringEntry(doc, stringId));
        assertLong(doc, textId, entityName, entityId, textValue, 101L);
        assertNull(getTextEntry(doc, textId));
        assertString(doc, longId, entityName, entityId, longKey, "long");
        assertNull(getNumberEntry(doc, longId));
        assertBoolean(doc, intId, entityName, entityId, intKey, false);
        assertText(doc, booleanId, entityName, entityId, booleanKey, "something in the way she moves");
        assertNull(getNumberEntry(doc, booleanId));
        //This should not have changed.
        assertInteger(doc, otherIntId, entityName, entityId + 1, intKey, Integer.MAX_VALUE);

        //Lets try adding some new values.
        final String newString = "newString";
        final String newText = "newText";
        final String newIntKey = "newInt";
        final String newLongKey = "newLong";
        final String newBoolKey = "newBool";

        configPropertySet.setStringProperty(newString, newString);
        configPropertySet.setTextProperty(newText, newText);
        configPropertySet.setIntegerProperty(newIntKey, 1002);
        configPropertySet.setLongProperty(newLongKey, 10029222L);
        configPropertySet.setBooleanProperty(newBoolKey, false);

        assertTrue(mgr.savePropertySet(configPropertySet));

        assertInteger(doc, stringId, entityName, entityId, stringValue, 10);
        assertLong(doc, textId, entityName, entityId, textValue, 101L);
        assertString(doc, longId, entityName, entityId, longKey, "long");
        assertBoolean(doc, intId, entityName, entityId, intKey, false);
        assertText(doc, booleanId, entityName, entityId, booleanKey, "something in the way she moves");
        assertString(doc, null, entityName, entityId, newString, newString);
        assertText(doc, null, entityName, entityId, newText, newText);
        assertInteger(doc, null, entityName, entityId, newIntKey, 1002);
        assertLong(doc, null, entityName, entityId, newLongKey, 10029222L);
        assertBoolean(doc, null, entityName, entityId, newBoolKey, false);
        //This should not have changed.
        assertInteger(doc, otherIntId, entityName, entityId + 1, intKey, Integer.MAX_VALUE);

        assertFalse(mgr.savePropertySet(configPropertySet));

        //Lets try deleting some of the entries.
        configPropertySet.removeProperty(newString);
        configPropertySet.removeProperty(newText);
        configPropertySet.removeProperty(newIntKey);
        configPropertySet.removeProperty(newLongKey);
        configPropertySet.removeProperty(newBoolKey);

        assertTrue(mgr.savePropertySet(configPropertySet));

        assertInteger(doc, stringId, entityName, entityId, stringValue, 10);
        assertLong(doc, textId, entityName, entityId, textValue, 101L);
        assertString(doc, longId, entityName, entityId, longKey, "long");
        assertBoolean(doc, intId, entityName, entityId, intKey, false);
        assertText(doc, booleanId, entityName, entityId, booleanKey, "something in the way she moves");
        assertNoValue(doc, entityName, entityId, newString, newString);
        assertNoValue(doc, entityName, entityId, newText, newText);
        assertNoValue(doc, entityName, entityId, newIntKey, "1002");
        assertNoValue(doc, entityName, entityId, newLongKey, "10029222");
        assertNoPropertyEntry(doc, entityName, entityId, newBoolKey);

        assertFalse(mgr.savePropertySet(configPropertySet));
    }

    public void testRoundTripping() throws Exception
    {
        final ConfigPropertySet expectedPs = new ConfigPropertySet("jira.properties", 1L);
        final String booleanKey = "boolean";
        final String longKey = "long";
        final String stringKey = "string";
        final String textKey = "text";
        final String intKey = "int";

        expectedPs.setBooleanProperty(booleanKey, true);
        expectedPs.setStringProperty(stringKey, stringKey);
        expectedPs.setTextProperty(textKey, textKey);
        expectedPs.setIntegerProperty(intKey, Integer.MAX_VALUE);
        expectedPs.setLongProperty(longKey, Long.MAX_VALUE);

        final Element root = createRoot();
        assertRoundtip(expectedPs, root);

        expectedPs.setStringProperty(booleanKey, "ahghdsja");
        expectedPs.setIntegerProperty(stringKey, 13243);
        expectedPs.setLongProperty(textKey, 34398201L);
        expectedPs.setBooleanProperty(intKey, false);
        expectedPs.setTextProperty(longKey, "moves me like no other");

        assertRoundtip(expectedPs, root);

        expectedPs.setStringProperty(booleanKey + "new", "ahghdsja");
        expectedPs.setIntegerProperty(stringKey + "new", 13243);
        expectedPs.setLongProperty(textKey + "new", 34398201L);
        expectedPs.setBooleanProperty(intKey + "new", false);
        expectedPs.setTextProperty(longKey + "new", "moves me like no other");

        assertRoundtip(expectedPs, root);

        expectedPs.removeProperty(booleanKey);
        expectedPs.removeProperty(stringKey);
        expectedPs.removeProperty(textKey);
        expectedPs.removeProperty(intKey);
        expectedPs.removeProperty(longKey);

        assertRoundtip(expectedPs, root);
    }

    private void assertRoundtip(ConfigPropertySet expected, final Element root)
    {
        final ConfigSequence sequence = new DefaultConfigSequence(root.getDocument());
        ConfigPropertySetManager mgr = new DefaultConfigPropertySetManager(root.getDocument(), sequence);
        mgr.savePropertySet(expected);
        sequence.save();

        mgr = new DefaultConfigPropertySetManager(root.getDocument(), sequence);
        final ConfigPropertySet actualPs = mgr.loadPropertySet(expected.getEntityName(), expected.getEntityId());

        assertEquals(expected, actualPs);
    }

    private Element createRoot()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Document document = factory.createDocument();
        return document.addElement(ENTITY_ROOT);
    }

    private void addSequence(Element root)
    {
        final Element element = ConfigXmlUtils.createNewElement(root, ELEMENT_SEQ_VALUE_ITEM);
        ConfigXmlUtils.setAttribute(element, ATTRIB_SEQ_NAME, ELEMENT_OSPROPERTY_ENTRY);
        ConfigXmlUtils.setAttribute(element, ATTRIB_SEQ_ID, "50000");
    }

    private long addBoolean(Element root, String entity, Long id, String key, Boolean boolValue)
    {
        final String val;
        if (boolValue == null)
        {
            val = null;
        }
        else
        {
            val = boolValue ? "1" : "0";
        }

        return addStringNumber(root, entity, id, key, val, BOOLEAN.getPropertySetType());
    }

    private long addLong(Element root, String entity, Long id, String key, Long longValue)
    {
        final String value = longValue != null ? longValue.toString() : null;
        return addStringNumber(root, entity, id, key, value, LONG.getPropertySetType());
    }

    private long addInteger(Element root, String entity, Long id, String key, Integer intValue)
    {
        final String value = intValue != null ? intValue.toString() : null;
        return addStringNumber(root, entity, id, key, value, INTEGER.getPropertySetType());
    }

    private long addStringNumber(Element root, String entity, Long id, String key, String value, int type)
    {
        final long returnId = createId();

        addPropertyEntry(root, returnId, entity, id, type, key);

        final Element element = ConfigXmlUtils.createNewElement(root, ELEMENT_OS_PROPERTY_NUMBER);
        ConfigXmlUtils.setAttribute(element, ATTRIB_ID, String.valueOf(returnId));
        ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, value);

        return returnId;
    }

    private long addStringEntry(Element root, String entity, Long id, String key, String value)
    {
        final long returnId = createId();

        addPropertyEntry(root, returnId, entity, id, STRING.getPropertySetType(), key);

        final Element element = ConfigXmlUtils.createNewElement(root, ELEMENT_OS_PROPERTY_STRING);
        ConfigXmlUtils.setAttribute(element, ATTRIB_ID, String.valueOf(returnId));
        ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, value);

        return returnId;
    }

    private long addTextEntry(Element root, String entity, Long id, String key, String value)
    {
        final long returnId = createId();

        addPropertyEntry(root, returnId, entity, id, TEXT.getPropertySetType(), key);

        final Element element = ConfigXmlUtils.createNewElement(root, ELEMENT_OS_PROPERTY_TEXT);
        ConfigXmlUtils.setAttribute(element, ATTRIB_ID, String.valueOf(returnId));
        ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, value);

        return returnId;
    }

    private Element addPropertyEntry(Element document, Long id, String entityType, Long entityId, Integer type, String key)
    {
        final Element element = ConfigXmlUtils.createNewElement(document, ELEMENT_OS_PROPERTY_ENTRY);
        if (id != null)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIB_ID, String.valueOf(id));
        }

        if (entityType != null)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIB_ENTITY_NAME, entityType);
        }

        if (entityId != null)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIB_ENTITY_ID, entityId.toString());
        }

        if (type != null)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIB_TYPE, type.toString());
        }
        if (key != null)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIB_PROPERTY_KEY, key);
        }

        return element;
    }

    private void assertBoolean(Document document, Long id, String entityName, Long entityId, String key, Boolean value)
    {
        final Long actualId = assertPropertyEntry(document, id, entityName, entityId, key, BOOLEAN.getPropertySetType());
        final Element stringValue = getNumberEntry(document, actualId);
        assertNotNull(stringValue);
        final Integer integerValue = ConfigXmlUtils.getIntegerValue(stringValue, ATTRIB_VALUE);
        assertEquals((boolean) value, integerValue != 0);
    }

    private void assertString(Document document, Long id, String entityName, Long entityId, String key, String value)
    {
        final Long actualId = assertPropertyEntry(document, id, entityName, entityId, key, STRING.getPropertySetType());
        final Element stringValue = getStringEntry(document, actualId);
        assertNotNull(stringValue);
        assertEquals(value, ConfigXmlUtils.getTextValue(stringValue, ATTRIB_VALUE));
    }

    private void assertText(Document document, Long id, String entityName, Long entityId, String key, String value)
    {
        final Long actualId = assertPropertyEntry(document, id, entityName, entityId, key, TEXT.getPropertySetType());
        final Element stringValue = getTextEntry(document, actualId);
        assertNotNull(stringValue);
        assertEquals(value, ConfigXmlUtils.getTextValue(stringValue, ATTRIB_VALUE));
    }

    private void assertLong(Document document, Long id, String entityName, Long entityId, String key, Long value)
    {
        final Long actualId = assertPropertyEntry(document, id, entityName, entityId, key, LONG.getPropertySetType());
        final Element longEntry = getNumberEntry(document, actualId);
        assertNotNull(longEntry);
        assertEquals(value, ConfigXmlUtils.getLongValue(longEntry, ATTRIB_VALUE));
    }

    private void assertInteger(Document document, Long id, String entityName, Long entityId, String key, Integer value)
    {
        final Long actualId = assertPropertyEntry(document, id, entityName, entityId, key, INTEGER.getPropertySetType());
        final Element intEntry = getNumberEntry(document, actualId);
        assertNotNull(intEntry);
        assertEquals(value, ConfigXmlUtils.getIntegerValue(intEntry, ATTRIB_VALUE));
    }

    private Long assertPropertyEntry(final Document document, final Long id, final String entityName,
            final Long entityId, final String key, final int type)
    {
        final Element entry;
        if (id != null)
        {
            entry = getPropertyEntry(document, id);
        }
        else
        {
            entry = getPropertyEntry(document, entityName, entityId, key);
        }

        assertNotNull(entry);
        assertEquals(entityName, ConfigXmlUtils.getTextValue(entry, ATTRIB_ENTITY_NAME));
        assertEquals(entityId, ConfigXmlUtils.getLongValue(entry, ATTRIB_ENTITY_ID));
        assertEquals(key, ConfigXmlUtils.getTextValue(entry, ATTRIB_PROPERTY_KEY));
        assertEquals(type, (int) ConfigXmlUtils.getIntegerValue(entry, ATTRIB_TYPE));

        return ConfigXmlUtils.getLongValue(entry, ATTRIB_ID);
    }

    private void assertNoValue(final Document document, final String entityName,
            final Long entityId, final String key, final String value)
    {
        assertNoPropertyEntry(document, entityName, entityId, key);
        String[] tables = new String[] { ELEMENT_OS_PROPERTY_TEXT, ELEMENT_OS_PROPERTY_NUMBER, ELEMENT_OS_PROPERTY_STRING };

        for (String table : tables)
        {
            String xPath = String.format("/entity-engine-xml/%s[@value='%s']", table, value);
            assertFalse("Element '" + table + "' has element with value '" + value + "'.", document.matches(xPath));
        }
    }

    private void assertNoPropertyEntry(final Document document, final String entityName,
            final Long entityId, final String key)
    {
        assertNull(getPropertyEntry(document, entityName, entityId, key));
    }

    private Element getStringEntry(final Document document, final Long id)
    {
        String xPath = String.format("/entity-engine-xml/OSPropertyString[@id='%d']", id);
        return ConfigXmlUtils.getElementByXpath(document, xPath);
    }

    private Element getNumberEntry(final Document document, final Long id)
    {
        String xPath = String.format("/entity-engine-xml/OSPropertyNumber[@id='%d']", id);
        return ConfigXmlUtils.getElementByXpath(document, xPath);
    }

    private Element getTextEntry(final Document document, final Long id)
    {
        String xPath = String.format("/entity-engine-xml/OSPropertyText[@id='%d']", id);
        return ConfigXmlUtils.getElementByXpath(document, xPath);
    }

    private Element getPropertyEntry(Document document, Long id)
    {
        final String xPath = String.format("/entity-engine-xml/OSPropertyEntry[@id='%d']", id);
        return ConfigXmlUtils.getElementByXpath(document, xPath);
    }

    private Element getPropertyEntry(Document document, String entityType, Long entityId, String key)
    {
        final String xPath = String.format("/entity-engine-xml/OSPropertyEntry[@entityName='%s' and @entityId='%d' and @propertyKey='%s']", entityType, entityId, key);
        return ConfigXmlUtils.getElementByXpath(document, xPath);
    }

    private long createId()
    {
        return globalId++;
    }
}