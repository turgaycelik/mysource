package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import electric.xml.ParseException;
import electric.xml.XPath;

import org.apache.commons.lang.StringEscapeUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestXmlIssueViewBackwardCompatibility extends JIRAWebTest
{
    final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    final String XML_VIEW_URL = "/si/jira.issueviews:issue-xml/HSP-1/HSP-1.xml";

    public TestXmlIssueViewBackwardCompatibility(String name)
    {
        super(name);
    }

    private String changeUrl(String baseUrl, String url) throws MalformedURLException
    {
        try
        {
            URI baseUri = new URI(baseUrl);
            URI uri = new URI(url);
            URL newUrl = new URL(baseUri.getScheme(), baseUri.getHost(), baseUri.getPort(), baseUri.getPath() + uri.getPath().replaceFirst("/jira/", ""));
            return newUrl.toString();
        }
        catch (URISyntaxException e)
        {
            throw new MalformedURLException();
        }
    }

    private void assertItemsEqual(Document originalDoc, Document doc)
            throws IOException, ParserConfigurationException, SAXException
    {
        // don't compare header as contains JIRA build date
        XPath xPath = new XPath("//item");

        Element originalElement = originalDoc.getElement(xPath);
        Element element = doc.getElement(xPath);
        // new element, so has to be removed
        element.removeElement("project");

        // Need to remove timezone information from the XML documents to ensure this does not break the comparison
        removeTimeZoneFromElementValue(originalDoc, doc, new XPath("//item/created"), null);
        removeTimeZoneFromElementValue(originalDoc, doc, new XPath("//item/updated"), null);
        removeTimeZoneFromElementValue(originalDoc, doc, new XPath("//item/resolved"), null);
        removeTimeZoneFromElementValue(originalDoc, doc, new XPath("//item/due"), null);
        removeTimeZoneFromElementValue(originalDoc, doc, new XPath("//item/comments/comment"), "created");
        removeTimeZoneFromElementValue(originalDoc, doc, new XPath("//item/attachments/attachment"), "created");
        removeTimeZoneFromElementValue(originalDoc, doc, new XPath("//item/customfields/customfield[@key='com.atlassian.jira.plugin.system.customfieldtypes:datetime']/customfieldvalues/customfieldvalue"), null);

        // because of Cargo tests url in original document has to be adjusted
        // adjust link before comparison
        originalElement.getElement("link").getText().setData(changeUrl(getTestContext().getBaseUrl(), originalElement.getElement("link").getText().getData()));
        // adjust type
        originalElement.getElement("type").setAttribute("iconUrl", changeUrl(getTestContext().getBaseUrl(), originalElement.getElement("type").getAttribute("iconUrl")));
        // adjust priority
        originalElement.getElement("priority").setAttribute("iconUrl", changeUrl(getTestContext().getBaseUrl(), originalElement.getElement("priority").getAttribute("iconUrl")));
        // adjust status
        originalElement.getElement("status").setAttribute("iconUrl", changeUrl(getTestContext().getBaseUrl(), originalElement.getElement("status").getAttribute("iconUrl")));

        // formatting doesn't matter here
        XMLUnit.setIgnoreWhitespace(true);

        XMLAssert.assertXMLEqual(originalElement.toString(), element.toString());
    }

    private void removeTimeZoneFromElementValue(Document originalDocument, Document document, XPath xPathToElement, String attributeName)
    {
        final Elements originalElements = originalDocument.getElements(xPathToElement);
        final Elements elements = document.getElements(xPathToElement);

        if (originalElements != null && elements != null)
        {
            while (originalElements.hasMoreElements() && elements.hasMoreElements())
            {
                if (attributeName == null)
                {
                    removeTimeZoneFromElementValue(originalElements.next());
                    removeTimeZoneFromElementValue(elements.next());
                }
                else
                {
                    removeTimeZoneFromAttributeValue(originalElements.next(), attributeName);
                    removeTimeZoneFromAttributeValue(elements.next(), attributeName);
                }
            }
        }
    }

    private void removeTimeZoneFromElementValue(Element dateElement)
    {
        try
        {
            final Date createdDate = dateFormat.parse(dateElement.getText().toString());
            dateElement.setText(dateFormat.format(createdDate));
        }
        catch (java.text.ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void removeTimeZoneFromAttributeValue(Element dateElement, String attributeName)
    {
        try
        {
            final Date createdDate = dateFormat.parse(dateElement.getAttribute(attributeName));
            dateElement.setAttribute(attributeName, dateFormat.format(createdDate));
        }
        catch (java.text.ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertCommentHasDetailsOnRestrictingFields(Document doc, String xmlViewUrl)
    {
        log("Checking the contents of the XML comment of the XML resulsts");
        
        final Node commentNode = doc.getFirstChild();
        assertNotNull(commentNode);
        assertEquals(Node.COMMENT_NODE, commentNode.getNodeType());
        final String commentText = commentNode.getNodeValue();
        assertNotNull(commentText);
        assertTrue(commentText.indexOf("RSS generated by JIRA") > -1);
        assertTrue(commentText.indexOf("It is possible to restrict the fields that are returned in this document by specifying the 'field' parameter in your request.\n"
                                       + "For example, to request only the issue key and summary add field=key&field=summary to the URL of your request.") > -1);


        String expectedUrl = getEnvironmentData().getBaseUrl() + xmlViewUrl;
        if (xmlViewUrl.indexOf("?") > -1)
        {
            expectedUrl +="&";
        }
        else
        {
            expectedUrl += "?";
        }

        expectedUrl += "field=key&field=summary";

        assertTrue(commentText.indexOf(StringEscapeUtils.escapeXml(expectedUrl)) > -1);
    }

    public void testRegularView() throws IOException, ParseException, ParserConfigurationException, SAXException
    {
        restoreData("TestXMLIssueCustomView.xml");

        Document originalDoc = getComparisonDocument("TestXmlIssueView-HSP-1.xml");

        gotoPage(XML_VIEW_URL);

        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();

        assertItemsEqual(originalDoc, doc);

        // Assert that we have a comment in the document that tells the user how to restrict the fields in the results.
        // This comment should only appear if the user did not restrict the fields. As this is the case here, the
        // comment should be there.
        assertCommentHasDetailsOnRestrictingFields(doc, XML_VIEW_URL);
    }

    public void testNoIssueLinksView() throws IOException, ParseException, ParserConfigurationException, SAXException
    {
        restoreData("TestXMLIssueCustomView.xml");
        deactivateIssueLinking();

        Document originalDoc = getComparisonDocument("TestXmlIssueView-HSP-1-no-issue-links.xml");

        // The parameters at the end of the URL are there to test if the comment part of the XML View is working correctly.
        // They should have no affect on the actual XML contents.
        final String xmlViewUrl = XML_VIEW_URL + "l?some=value&another=blah";
        gotoPage(xmlViewUrl);

        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();

        assertItemsEqual(originalDoc, doc);

        // Assert that we have a comment in the document that tells the user how to restrict the fields in the results.
        // This comment should only appear if the user did not restrict the fields. As this is the case here, the
        // comment should be there.
        assertCommentHasDetailsOnRestrictingFields(doc, xmlViewUrl);
    }

    public void testNoTimetrackingView() throws IOException, ParseException, ParserConfigurationException, SAXException
    {
        restoreData("TestXMLIssueCustomView.xml");
        deactivateTimeTracking();

        Document originalDoc = getComparisonDocument("TestXmlIssueView-HSP-1-no-timetracking.xml");

        gotoPage(XML_VIEW_URL);

        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();

        assertItemsEqual(originalDoc, doc);

        // Assert that we have a comment in the document that tells the user how to restrict the fields in the results.
        // This comment should only appear if the user did not restrict the fields. As this is the case here, the
        // comment should be there.
        assertCommentHasDetailsOnRestrictingFields(doc, XML_VIEW_URL);
    }

    public void testTimetrackingHiddenView() throws IOException, ParseException, ParserConfigurationException, SAXException
    {
        restoreData("TestXMLIssueCustomView.xml");
        setHiddenFields("Time Tracking");

        Document originalDoc = getComparisonDocument("TestXmlIssueView-HSP-1-no-timetracking.xml");

        gotoPage(XML_VIEW_URL);

        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();

        assertItemsEqual(originalDoc, doc);

        // Assert that we have a comment in the document that tells the user how to restrict the fields in the results.
        // This comment should only appear if the user did not restrict the fields. As this is the case here, the
        // comment should be there.
        assertCommentHasDetailsOnRestrictingFields(doc, XML_VIEW_URL);
    }

    public void testHideDueDateFieldView() throws IOException, ParseException, ParserConfigurationException, SAXException
    {
        restoreData("TestXMLIssueCustomView.xml");
        setHiddenFields("Due Date");

        Document originalDoc = getComparisonDocument("TestXmlIssueView-HSP-1-due-date-hidden.xml");

        gotoPage(XML_VIEW_URL);

        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();

        assertItemsEqual(originalDoc, doc);

        // Assert that we have a comment in the document that tells the user how to restrict the fields in the results.
        // This comment should only appear if the user did not restrict the fields. As this is the case here, the
        // comment should be there.
        assertCommentHasDetailsOnRestrictingFields(doc, XML_VIEW_URL);
    }

    public void testNoCustomFieldsView() throws IOException, ParseException, ParserConfigurationException, SAXException
    {
        restoreData("TestXMLIssueCustomView.xml");
        removeAllCustomFields();

        Document originalDoc = getComparisonDocument("TestXmlIssueView-HSP-1-no-custom-fields.xml");

        gotoPage(XML_VIEW_URL);

        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();

        assertItemsEqual(originalDoc, doc);

        // Assert that we have a comment in the document that tells the user how to restrict the fields in the results.
        // This comment should only appear if the user did not restrict the fields. As this is the case here, the
        // comment should be there.
        assertCommentHasDetailsOnRestrictingFields(doc, XML_VIEW_URL);
    }

    public void testNoSubtasksView() throws IOException, ParseException, ParserConfigurationException, SAXException
    {
        restoreData("TestXMLIssueCustomView.xml");
        deleteIssue("HSP-3");
        deactivateSubTasks();

        Document originalDoc = getComparisonDocument("TestXmlIssueView-HSP-1-no-subtasks.xml");

        gotoPage(XML_VIEW_URL);

        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();

        assertItemsEqual(originalDoc, doc);

        // Assert that we have a comment in the document that tells the user how to restrict the fields in the results.
        // This comment should only appear if the user did not restrict the fields. As this is the case here, the
        // comment should be there.
        assertCommentHasDetailsOnRestrictingFields(doc, XML_VIEW_URL);
    }

    public Document getComparisonDocument(String fileName) throws ParseException
    {
        return new Document(new File(getEnvironmentData().getXMLDataLocation(), fileName));
    }

    protected Document getDocument() throws IOException, ParseException
    {
        InputStream inputStream = getDialog().getResponse().getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileUtils.copy(inputStream, outputStream);
        return new Document(outputStream.toByteArray());
    }
}