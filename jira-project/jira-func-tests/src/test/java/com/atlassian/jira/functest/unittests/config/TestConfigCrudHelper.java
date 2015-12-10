package com.atlassian.jira.functest.unittests.config;

import com.atlassian.jira.functest.config.ConfigCrudHelper;
import com.atlassian.jira.functest.config.ConfigException;
import com.atlassian.jira.functest.config.ConfigObjectWithId;
import com.atlassian.jira.functest.config.ConfigSequence;
import com.atlassian.jira.functest.config.ConfigXmlUtils;
import junit.framework.Assert;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @since v4.3
 */
public class TestConfigCrudHelper
{
    private static final String ELEMENT = "SimpleId";
    
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_VALUE = "soeerwjsrewje";

    @Test
    public void testLoad() throws Exception
    {
        SimpleId id1 = new SimpleId(1L);
        SimpleId id2 = new SimpleId(2L);
        SimpleId id3 = new SimpleId(3L);

        Document document = toDocument(id1, id2, id3);

        Helper helper = new Helper(document, null);
        Assert.assertEquals(asList(id1, id2, id3), helper.load());
    }

    @Test(expected = ConfigException.class)
    public void testLoadWithErrors() throws Exception
    {
        SimpleId id3 = new SimpleId(null);

        Document document = toDocument(id3);

        Helper helper = new Helper(document, null);
        Assert.assertEquals(asList(id3), helper.load());
    }

    @Test
    public void testSaveWithNoChanges() throws Exception
    {
        SimpleId id1 = new SimpleId(1L);
        SimpleId id2 = new SimpleId(2L);
        SimpleId id3 = new SimpleId(3L);

        Document document = toDocument(id1, id2, id3);

        Helper helper = new Helper(document, null);

        List<SimpleId> updateList = asList(id1, id2, id3);
        Assert.assertFalse(helper.save(updateList));
        Assert.assertEquals(updateList, parseDocument(document));
    }

    @Test
    public void testSaveUpdateChanges() throws Exception
    {
        SimpleId id1 = new SimpleId(1L);
        SimpleId id2 = new SimpleId(2L);
        SimpleId id3 = new SimpleId(3L);

        Document document = toDocument(id1, id2, id3);

        Helper helper = new Helper(document, null);

        List<SimpleId> updateList = asList(id1, id2.setValue(null), id3.setValue("jack"));

        Assert.assertTrue(helper.save(updateList));
        Assert.assertEquals(updateList, parseDocument(document));
    }

    @Test
    public void testSaveNewChanges() throws Exception
    {
        SimpleId id1 = new SimpleId(1L);
        SimpleId id2 = new SimpleId(2L);
        SimpleId id3 = new SimpleId(3L, "Info");

        Document document = toDocument(id1, id2);

        ConfigSequence configSequence = EasyMock.createMock(ConfigSequence.class);
        EasyMock.expect(configSequence.getNextId(ELEMENT)).andReturn(id3.getId());

        EasyMock.replay(configSequence);

        Helper helper = new Helper(document, configSequence);

        List<SimpleId> updateList = asList(id1, id2, id3);

        Assert.assertTrue(helper.save(updateList));
        Assert.assertEquals(updateList, parseDocument(document));

        EasyMock.verify(configSequence);
    }

    @Test
    public void testSaveDelete() throws Exception
    {
        SimpleId id1 = new SimpleId(1L);
        SimpleId id2 = new SimpleId(2L);
        SimpleId id3 = new SimpleId(3L, "Info");

        Document document = toDocument(id1, id2, id3);

        Helper helper = new Helper(document, null);

        List<SimpleId> updateList = asList(id1, id2.setValue("Hello"));

        Assert.assertTrue(helper.save(updateList));
        Assert.assertEquals(updateList, parseDocument(document));
        Assert.assertEquals(Collections.singletonList(id3), helper.deletedElements);
    }

    private Document toDocument(SimpleId... ids)
    {
        Element element = createRootElement();
        for (SimpleId id : ids)
        {
            toElement(element, id);
        }
        return element.getDocument();
    }

    private List<SimpleId> parseDocument(Document document)
    {
        List<Element> elements = ConfigXmlUtils.getTopElementsByName(document, ELEMENT);
        List<SimpleId> ids = new ArrayList<SimpleId>();

        for (Element element : elements)
        {
            ids.add(fromElement(element));
        }

        return ids;
    }


    private static Element createRootElement()
    {
        final DocumentFactory factory = DocumentFactory.getInstance();
        final Document document = factory.createDocument();
        return document.addElement("entity-engine-xml");
    }

    private Element toElement(Element root, SimpleId simpleId)
    {
        Element element = root.addElement(ELEMENT);
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, simpleId.getId());
        ConfigXmlUtils.setAttribute(element, ATTRIBUTE_VALUE, simpleId.getValue());

        return element;
    }

    private SimpleId fromElement(Element elements)
    {
        return new SimpleId(ConfigXmlUtils.getLongValue(elements, ATTRIBUTE_ID),
                ConfigXmlUtils.getTextValue(elements, ATTRIBUTE_VALUE));
    }

    private static <T, S extends T> List<T> asList(S... list)
    {
        return new ArrayList<T>(Arrays.asList(list));
    }

    private static class Helper extends ConfigCrudHelper<SimpleId>
    {
        private final List<SimpleId> deletedElements = new ArrayList<SimpleId>();

        public Helper(final Document document, final ConfigSequence configSeqence)
        {
            super(document, configSeqence, ELEMENT);
        }

        @Override
        protected SimpleId elementToObject(Element element)
        {
            return new SimpleId(ConfigXmlUtils.getLongValue(element, ATTRIBUTE_ID),
                    ConfigXmlUtils.getTextValue(element, ATTRIBUTE_VALUE));
        }

        @Override
        protected void updateObject(Element element, SimpleId updateObject, SimpleId oldObject)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, updateObject.getId());
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_VALUE, updateObject.getValue());
        }

        @Override
        protected void newObject(Element element, SimpleId newObject, Long newId)
        {
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_ID, newId);
            ConfigXmlUtils.setAttribute(element, ATTRIBUTE_VALUE, newObject.getValue());
        }

        @Override
        protected void deleteObject(Element element, SimpleId deleteObject)
        {
            deletedElements.add(deleteObject);
        }
    }

    private static class SimpleId implements ConfigObjectWithId
    {
        private final Long id;
        private String value;

        public SimpleId(Long id)
        {
            this(id, String.valueOf(id));
        }

        public SimpleId(Long id, String value)
        {
            this.id = id;
            this.value = value;
        }

        public SimpleId setValue(String value)
        {
            this.value = value;
            return this;
        }

        @Override
        public Long getId()
        {
            return id;
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimpleId simpleId = (SimpleId) o;

            if (id != null ? !id.equals(simpleId.id) : simpleId.id != null) { return false; }
            if (value != null ? !value.equals(simpleId.value) : simpleId.value != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return String.format("SimpleId[%d] = %s", id, value);
        }
    }
}
