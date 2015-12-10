package com.atlassian.jira.action.admin.export;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import com.atlassian.jira.local.testutils.UtilsForTestSetup;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.Assert;

public class EntityWriterTestUtils
{
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";


    public static void assertExportProducesXML(EntityXmlWriter entityXmlWriter, GenericValue gv, String expectedXMLText) throws DocumentException, GenericEntityException
    {
        try
        {
            StringWriter stringWriter = new StringWriter();
            entityXmlWriter.writeXmlText(gv, new PrintWriter(stringWriter));

            Document expectedXml = new SAXReader().read(new StringReader(XML_HEADER + expectedXMLText));
            Document xml = new SAXReader().read(new StringReader(XML_HEADER + stringWriter.toString()));
//            XMLAssert.assertXMLEqual(expectedXml.asXML(), xml.asXML());
            Assert.assertEquals(expectedXml.asXML(), xml.asXML());
        }
        finally
        {
            UtilsForTestSetup.deleteAllEntities();
        }
    }
}
