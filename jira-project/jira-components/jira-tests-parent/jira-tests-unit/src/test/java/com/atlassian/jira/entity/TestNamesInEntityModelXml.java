package com.atlassian.jira.entity;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.dom4j.DocumentException;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * This test is to validate that object names in entitymodel.xml files does not exceeded 30 characters as this is limit
 * in oracle db. see ORA-00972 for reference
 *
 * @since v6.2
 */
public class TestNamesInEntityModelXml
{
    @Test
    public void testThatObjectNamesAreShorterThan30Characters() throws DocumentException, XMLStreamException
    {
        final InputStream entities = getClass().getResourceAsStream("/entitydefs/entitymodel.xml");
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        final XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(entities);
        while (xmlStreamReader.hasNext())
        {
            final int next = xmlStreamReader.next();
            if (next == XMLStreamReader.START_ELEMENT)
            {
                if (xmlStreamReader.getAttributeCount() > 0)
                {
                    for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++)
                    {
                        final String attributeName = xmlStreamReader.getAttributeName(i).getLocalPart();
                        if(attributeName.equals("name") || attributeName.equals("table-name"))
                        {
                            final String attributeValue = xmlStreamReader.getAttributeValue(i);
                            assertThat("All object names should be no more than 30 characters see ORA-00972 for reference "+attributeName+"=\""+attributeValue+"\"", attributeValue.length(), Matchers.lessThanOrEqualTo(30));
                        }
                    }
                }
            }
        }

    }

}
