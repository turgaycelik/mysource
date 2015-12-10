package com.atlassian.jira.functest.config.ps;

import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.functest.config.ps.ConfigPropertySetEntry.Type;

/**
 * Loads a property set from an XML configuration file.
 *
 * @since v4.0
 */
public class DefaultConfigPropertySetManager implements ConfigPropertySetManager
{
    private static final Map<ConfigPropertySetEntry.Type, PropertyValueHelper> valueHelper;

    static
    {
        final EnumMap<ConfigPropertySetEntry.Type, PropertyValueHelper> map = new EnumMap<ConfigPropertySetEntry.Type, PropertyValueHelper>(ConfigPropertySetEntry.Type.class);
        map.put(Type.BOOLEAN, new BooleanValueHelper());
        map.put(Type.INTEGER, new IntegerValueHelper());
        map.put(Type.LONG, new LongValueHelper());
        map.put(Type.STRING, new StringValueHelper());
        map.put(Type.TEXT, new TextValueHelper());

        valueHelper = Collections.unmodifiableMap(map);
    }

    private static final String ATTRIB_PROPERTY_KEY = "propertyKey";
    private static final String ATTRIB_ID = "id";
    private static final String ATTRIB_VALUE = "value";
    private static final String ATTRIB_TYPE = "type";
    private static final String ATTRIB_ENTITY_NAME = "entityName";
    private static final String ATTRIB_ENTITY_ID = "entityId";

    private static final String ELEMENT_OS_PROPERTY_ENTRY = "OSPropertyEntry";
    private static final String ELEMENT_OS_PROPERTY_NUMBER = "OSPropertyNumber";
    private static final String ELEMENT_OS_PROPERTY_TEXT = "OSPropertyText";

    private final Document document;
    private final ConfigSequence sequence;

    public DefaultConfigPropertySetManager(Document document, ConfigSequence sequence)
    {
        this.document = document;
        this.sequence = sequence;
    }

    public ConfigPropertySet loadPropertySet(String entityName, long id)
    {
        ConfigPropertySet newSet = new ConfigPropertySet(entityName, id);

        final List<Element> list = getEntries(entityName, id);
        for (Element element : list)
        {
            addForElement(newSet, element);
        }
        return newSet;
    }

    public boolean savePropertySet(ConfigPropertySet propertySet)
    {
        boolean returnValue = false;
        final ConfigPropertySet oldPropertySet = loadPropertySet(propertySet.getEntityName(), propertySet.getEntityId());
        final Map<String, ConfigPropertySetEntry> oldEntries = oldPropertySet.entryMap();

        for (ConfigPropertySetEntry entry : propertySet.entries())
        {
            ConfigPropertySetEntry oldEntry = oldEntries.get(entry.getPropertyName());
            if (oldEntry == null)
            {
                //its a new entry.
                addEntry(propertySet, entry, sequence.getNextId(ELEMENT_OS_PROPERTY_ENTRY));
                returnValue = true;
            }
            else if (!entry.equals(oldEntry))
            {
                updateEntry(propertySet, entry, oldEntry);
                returnValue = true;
            }
        }

        //Find all the elements that must be deleted.
        final Map<String, ConfigPropertySetEntry> deleteMe = new HashMap<String, ConfigPropertySetEntry>(oldEntries);
        deleteMe.keySet().removeAll(propertySet.entryMap().keySet());

        for (ConfigPropertySetEntry deleteEntry : deleteMe.values())
        {
            deleteEntry(propertySet, deleteEntry);
            returnValue = true;
        }

        return returnValue;
    }

    public void deletePropertySet(ConfigPropertySet propertySet)
    {
        for (ConfigPropertySetEntry entry : propertySet.entries())
        {
            deleteEntry(propertySet, entry);
        }
    }

    public void deletePropertySet(final String entityName, final Long id)
    {
        final List<Element> entries = getEntries(entityName, id);
        for (Element entry : entries)
        {
            final Integer type = ConfigXmlUtils.getIntegerValue(entry, ATTRIB_TYPE);
            if (type == null)
            {
                throw new ConfigException("Unable to delete entry '" + elementToString(entry) + "': Element has no type.");
            }

            final PropertyValueHelper helper = getHelper(type);
            final Long entryId = ConfigXmlUtils.getLongValue(entry, ATTRIB_ID);
            if (entryId == null)
            {
                throw new ConfigException("Unable to delete entry '" + elementToString(entry) + ": Element does not have an ID.");
            }

            if (!helper.deleteEntry(document, entryId))
            {
                throw new ConfigException("Unable to delete entry '" + elementToString(entry) + ".");
            }

            ConfigXmlUtils.removeElement(entry);
        }
    }

    private void deleteEntry(ConfigPropertySet propertySet, ConfigPropertySetEntry entry)
    {
        final Element osElement = getEntry(propertySet.getEntityName(), propertySet.getEntityId(), entry.getPropertyName());
        if (osElement == null)
        {
            throw new ConfigException("Unable to delete entry '" + entry + ": Unable to find current DOM element.");
        }

        final PropertyValueHelper helper = valueHelper.get(entry.getPropertyType());
        if (helper == null)
        {
            throw new ConfigException("Unable to delete entry '" + entry + ": Unable to find helper.");
        }

        Long id = ConfigXmlUtils.getLongValue(osElement, ATTRIB_ID);
        if (id == null)
        {
            throw new ConfigException("Unable to delete entry '" + entry + ": Element '" + elementToString(osElement) + "' does not have an ID.");
        }

        if (!helper.deleteEntry(document, id))
        {
            throw new ConfigException("Unable to delete entry'" + entry + "'.");
        }

        ConfigXmlUtils.removeElement(osElement);
    }

    private void updateEntry(ConfigPropertySet propertySet, ConfigPropertySetEntry newEntry, ConfigPropertySetEntry oldEntry)
    {
        final PropertyValueHelper helper = valueHelper.get(newEntry.getPropertyType());
        if (helper == null)
        {
            throw new ConfigException("Unable to update entry '" + oldEntry + "': Unable to find helper for type '" + newEntry.getPropertyType() + "'.");
        }

        final Element osElement = getEntry(propertySet.getEntityName(), propertySet.getEntityId(), newEntry.getPropertyName());
        if (osElement == null)
        {
            throw new ConfigException("Unable to update entry '" + oldEntry + "': Unable to find current DOM element.");
        }

        final Long id = ConfigXmlUtils.getLongValue(osElement, ATTRIB_ID);
        if (id == null)
        {
            throw new ConfigException("Unable to update entry '" + oldEntry + "': Element '" + elementToString(osElement) + "' does not have an ID.");
        }

        if (newEntry.getPropertyType() == oldEntry.getPropertyType())
        {
            //They are the same types, so we can just update the value.
            if (!helper.updateEntry(document, id, newEntry))
            {
                throw new ConfigException("Unable to update entry '" + oldEntry + "' to '" + newEntry + "'.");
            }
        }
        else
        {
            final PropertyValueHelper oldHelper = valueHelper.get(oldEntry.getPropertyType());
            if (oldHelper == null)
            {
                throw new ConfigException("Unable to update entry '" + oldEntry + "': Unable to find helper for type '" + oldEntry.getPropertyType() + "'.");
            }

            if (!oldHelper.deleteEntry(document, id))
            {
                throw new ConfigException("Unable to update entry '" + oldEntry + "': Unable to delete old entry.");
            }

            ConfigXmlUtils.setAttribute(osElement, ATTRIB_TYPE, String.valueOf(newEntry.getPropertyType().getPropertySetType()));
            if (!helper.addEntryToDocument(document, id, newEntry))
            {
                throw new ConfigException("Unable to update entry '" + oldEntry + "': Unable to create new entry '" + newEntry + "'.");
            }
        }
    }

    private void addEntry(ConfigPropertySet propertySet, ConfigPropertySetEntry newEntry, long newId)
    {
        final PropertyValueHelper helper = valueHelper.get(newEntry.getPropertyType());
        if (helper == null)
        {
            throw new ConfigException("Unable to add new entry '" + newEntry + "': Unable to find helper.");
        }

        //<OSPropertyEntry id="10000" entityName="jira.properties" entityId="1" propertyKey="jira.i18n.language.index" type="5"/>
        final Element newElement = ConfigXmlUtils.createNewElement(document.getRootElement(), ELEMENT_OS_PROPERTY_ENTRY);
        ConfigXmlUtils.setAttribute(newElement, ATTRIB_ID, String.valueOf(newId));
        ConfigXmlUtils.setAttribute(newElement, ATTRIB_ENTITY_NAME, propertySet.getEntityName());
        ConfigXmlUtils.setAttribute(newElement, ATTRIB_ENTITY_ID, propertySet.getEntityId().toString());
        ConfigXmlUtils.setAttribute(newElement, ATTRIB_PROPERTY_KEY, newEntry.getPropertyName());
        ConfigXmlUtils.setAttribute(newElement, ATTRIB_TYPE, String.valueOf(newEntry.getPropertyType().getPropertySetType()));

        if (!helper.addEntryToDocument(document, newId, newEntry))
        {
            throw new ConfigException("Unable to create entry for '" + newEntry + "'.");
        }
    }

    private void addForElement(ConfigPropertySet set, Element entry)
    {
        //<OSPropertyEntry id="10000" entityName="jira.properties" entityId="1" propertyKey="jira.i18n.language.index" type="5"/>
        final String propertyKey = ConfigXmlUtils.getTextValue(entry, ATTRIB_PROPERTY_KEY);
        final Long id = ConfigXmlUtils.getLongValue(entry, ATTRIB_ID);
        final Integer type = ConfigXmlUtils.getIntegerValue(entry, ATTRIB_TYPE);

        if (StringUtils.isBlank(propertyKey) || id == null || type == null)
        {
            throw new ConfigException("Unable to read entry: '" + elementToString(entry) + "'.");
        }

        final PropertyValueHelper helper = getHelper(type);
        if (!helper.addEntryToPropertySet(document, propertyKey, id, set))
        {
            throw new ConfigException("Unable to read entry: '" + elementToString(entry) + "'.");
        }
    }

    private PropertyValueHelper getHelper(int type)
    {
        ConfigPropertySetEntry.Type entryType = Type.forPropertySetType(type);
        if (entryType == null)
        {
            throw new ConfigException("Entry type with id '" + type + "' does not exist.");
        }

        final PropertyValueHelper helper = valueHelper.get(entryType);
        if (helper == null)
        {
            throw new ConfigException("Helper for entry type  '" + entryType + "' does not exist.");
        }
        else
        {
            return helper;
        }
    }

    private List<Element> getEntries(String entityName, long id)
    {
        final String xpath = String.format("/entity-engine-xml/OSPropertyEntry[@entityName='%s' and @entityId='%d']", entityName, id);
        return ConfigXmlUtils.getElementsByXpath(document, xpath);
    }

    private Element getEntry(String entityName, long id, String propertyKey)
    {
        final String xpath = String.format("/entity-engine-xml/OSPropertyEntry[@entityName='%s' and @entityId='%d' and @propertyKey='%s']", entityName, id, propertyKey);
        return ConfigXmlUtils.getElementByXpath(document, xpath);
    }

    private static Element getNumberEntry(Document document, long id)
    {
        return ConfigXmlUtils.getElementByXpath(document,
                String.format("/entity-engine-xml/OSPropertyNumber[@id='%d']", id));
    }

    private static Element addNumberEntry(Document document, long id, String value)
    {
        final Element numberElement = ConfigXmlUtils.createNewElement(document.getRootElement(), ELEMENT_OS_PROPERTY_NUMBER);
        ConfigXmlUtils.setAttribute(numberElement, ATTRIB_ID, String.valueOf(id));
        ConfigXmlUtils.setAttribute(numberElement, ATTRIB_VALUE, value);
        return numberElement;
    }

    private static String elementToString(Element element)
    {
        final StringWriter writer = new StringWriter();
        try
        {
            element.write(writer);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unexpected IO exception on string writer.", e);
        }
        finally
        {
            IOUtils.closeQuietly(writer);
        }
        return writer.toString();
    }

    private interface PropertyValueHelper
    {
        ConfigPropertySetEntry.Type getType();
        boolean addEntryToPropertySet(Document document, String propertyName, long id, ConfigPropertySet ps);
        boolean addEntryToDocument(Document document, long id, ConfigPropertySetEntry entry);
        boolean updateEntry(Document document, long id, ConfigPropertySetEntry newEntry);
        boolean deleteEntry(Document document, long id);
    }

    //<OSPropertyNumber id="10005" value="1"/>
    private static class BooleanValueHelper implements PropertyValueHelper
    {
        public ConfigPropertySetEntry.Type getType()
        {
            return Type.BOOLEAN;
        }

        public boolean addEntryToPropertySet(Document document, String propertyName, long id, ConfigPropertySet ps)
        {
            Element element = getNumberEntry(document, id);
            if (element != null)
            {
                Integer boolValue = ConfigXmlUtils.getIntegerValue(element, ATTRIB_VALUE);
                if (boolValue != null)
                {
                    ps.setBooleanProperty(propertyName, boolValue != 0);
                    return true;
                }
            }
            return false;
        }

        public boolean addEntryToDocument(Document document, long id, ConfigPropertySetEntry entry)
        {
            addNumberEntry(document, id, asBoolean(entry));
            return true;
        }

        public boolean updateEntry(Document document, long id, ConfigPropertySetEntry newEntry)
        {
            final Element element = getNumberEntry(document, id);
            if (element != null)
            {
                ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, asBoolean(newEntry));
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean deleteEntry(Document document, long id)
        {
            final Element element = getNumberEntry(document, id);
            return ConfigXmlUtils.removeElement(element);
        }

        private String asBoolean(ConfigPropertySetEntry entry)
        {
            return entry.asBoolean() ? "1" : "0";
        }
    }

    //<OSPropertyNumber id="10005" value="1"/>
    private static class IntegerValueHelper implements PropertyValueHelper
    {
        public ConfigPropertySetEntry.Type getType()
        {
            return Type.INTEGER;
        }

        public boolean addEntryToPropertySet(Document document, String propertyName, long id, ConfigPropertySet ps)
        {
            Element element = getNumberEntry(document, id);
            if (element != null)
            {
                Integer integerValue = ConfigXmlUtils.getIntegerValue(element, ATTRIB_VALUE);
                if (integerValue != null)
                {
                    ps.setIntegerProperty(propertyName, integerValue);
                    return true;
                }
            }
            return false;
        }

        public boolean addEntryToDocument(Document document, long id, ConfigPropertySetEntry entry)
        {
            addNumberEntry(document, id, entry.asString());
            return true;
        }

        public boolean updateEntry(Document document, long id, ConfigPropertySetEntry newEntry)
        {
            final Element element = getNumberEntry(document, id);
            if (element != null)
            {
                ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, newEntry.asString());
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean deleteEntry(Document document, long id)
        {
            final Element element = getNumberEntry(document, id);
            return ConfigXmlUtils.removeElement(element);
        }
    }

    //<OSPropertyNumber id="10005" value="1"/>
    private static class LongValueHelper implements PropertyValueHelper
    {
        public ConfigPropertySetEntry.Type getType()
        {
            return Type.LONG;
        }

        public boolean addEntryToPropertySet(Document document, String propertyName, long id, ConfigPropertySet ps)
        {
            Element element = getNumberEntry(document, id);
            if (element != null)
            {
                Long integerValue = ConfigXmlUtils.getLongValue(element, ATTRIB_VALUE);
                if (integerValue != null)
                {
                    ps.setLongProperty(propertyName, integerValue);
                    return true;
                }
            }
            return false;
        }

        public boolean addEntryToDocument(Document document, long id, ConfigPropertySetEntry entry)
        {
            addNumberEntry(document, id, entry.asString());
            return true;
        }

        public boolean updateEntry(Document document, long id, ConfigPropertySetEntry newEntry)
        {
            final Element element = getNumberEntry(document, id);
            if (element != null)
            {
                ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, newEntry.asString());
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean deleteEntry(Document document, long id)
        {
            final Element element = getNumberEntry(document, id);
            return ConfigXmlUtils.removeElement(element);
        }
    }

    private static class StringValueHelper implements PropertyValueHelper
    {
        private static final String ELEMENT_OS_PROPERTY_STRING = "OSPropertyString";

        public ConfigPropertySetEntry.Type getType()
        {
            return Type.STRING;
        }

        public boolean addEntryToPropertySet(Document document, String propertyName, long id, ConfigPropertySet ps)
        {
            Element element = getStringElement(document, id);
            if (element != null)
            {
                String stringValue = ConfigXmlUtils.getTextValue(element, ATTRIB_VALUE);
                ps.setStringProperty(propertyName, stringValue);
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean addEntryToDocument(Document document, long id, ConfigPropertySetEntry entry)
        {
            Element element = ConfigXmlUtils.createNewElement(document.getRootElement(), ELEMENT_OS_PROPERTY_STRING);
            ConfigXmlUtils.setAttribute(element, ATTRIB_ID, String.valueOf(id));
            ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, entry.asString());

            return true;
        }

        public boolean updateEntry(Document document, long id, ConfigPropertySetEntry newEntry)
        {
            final Element element = getStringElement(document, id);
            if (element != null)
            {
                ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, newEntry.asString());
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean deleteEntry(Document document, long id)
        {
            final Element element = getStringElement(document, id);
            return ConfigXmlUtils.removeElement(element);
        }

        private static Element getStringElement(Document document, long id)
        {
            return ConfigXmlUtils.getElementByXpath(document,
                    String.format("/entity-engine-xml/OSPropertyString[@id='%d']", id));
        }
    }

    private static class TextValueHelper implements PropertyValueHelper
    {
        public ConfigPropertySetEntry.Type getType()
        {
            return Type.TEXT;
        }

        public boolean addEntryToPropertySet(Document document, String propertyName, long id, ConfigPropertySet ps)
        {
            Element element = getTextElement(document, id);
            if (element != null)
            {
                String stringValue = ConfigXmlUtils.getTextValue(element, ATTRIB_VALUE);
                if (stringValue != null)
                {
                    ps.setTextProperty(propertyName, stringValue);
                    return true;
                }
            }
            return false;
        }

        public boolean addEntryToDocument(Document document, long id, ConfigPropertySetEntry entry)
        {
            Element element = ConfigXmlUtils.createNewElement(document.getRootElement(), ELEMENT_OS_PROPERTY_TEXT);
            ConfigXmlUtils.setAttribute(element, ATTRIB_ID, String.valueOf(id));
            ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, entry.asString());

            return true;
        }

        public boolean updateEntry(Document document, long id, ConfigPropertySetEntry newEntry)
        {
            final Element element = getTextElement(document, id);
            if (element != null)
            {
                ConfigXmlUtils.setAttribute(element, ATTRIB_VALUE, newEntry.asString());
                return true;
            }
            else
            {
                return false;
            }
        }

        public boolean deleteEntry(Document document, long id)
        {
            final Element element = getTextElement(document, id);
            return ConfigXmlUtils.removeElement(element);
        }

        private static Element getTextElement(Document document, long id)
        {
            return ConfigXmlUtils.getElementByXpath(document,
                    String.format("/entity-engine-xml/OSPropertyText[@id='%d']", id));
        }
    }
}
