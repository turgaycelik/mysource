package com.atlassian.jira.action.admin.export;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizFactory;

import org.dom4j.DocumentException;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.xml.sax.SAXException;

public class TestDefaultEntityXmlWriter
{
    static {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    @Before
    public void setUp() throws Exception
    {
        final MockComponentWorker componentAccessorWorker = new MockComponentWorker();
        componentAccessorWorker.registerMock(OfBizDelegator.class, OfBizFactory.getOfBizDelegator());
        ComponentAccessor.initialiseWorker(componentAccessorWorker);
    }

    @Test
    public void testSingleEntity() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "key", "TST-10"));
        String expectedXMLText = "<Issue id=\"1001\" key=\"TST-10\"/>";
        EntityWriterTestUtils.assertExportProducesXML(new DefaultEntityXmlWriter(), gv, expectedXMLText);
    }

    @Test
    public void testSingleEntityWithLineBreaks() throws GenericEntityException, DocumentException, IOException, ParserConfigurationException, SAXException
    {
        GenericValue gv = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", "1001", "description", "abc\ndef"));
        //for some reason I can't get dom4j to output XML without keeping formatting - so we need to format this exactly the same.
        String expectedXMLText = "<Issue id=\"1001\">\n        <description><![CDATA[abc\ndef]]></description>\n    </Issue>";
        EntityWriterTestUtils.assertExportProducesXML(new DefaultEntityXmlWriter(), gv, expectedXMLText);
    }
    

}
