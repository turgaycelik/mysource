package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.WebRequest;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.ParseException;
import electric.xml.XPath;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestXmlCustomIssueView extends JIRAWebTest
{
    public TestXmlCustomIssueView(String name)
    {
        super(name);
    }

    public void testNodeFilter() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        checkNodeFilter("field=title", "title");
        checkNodeFilter("field=link", "link");
        checkNodeFilter("field=project", "project");
        checkNodeFilter("field=summary", "summary");
        checkNodeFilter("field=description", "description");
        checkNodeFilter("field=environment", "environment");
        checkNodeFilter("field=issuetype", "type");
        checkNodeFilter("field=priority", "priority");
        checkNodeFilter("field=status", "status");
        checkNodeFilter("field=resolution", "resolution");
        checkNodeFilter("field=security", "security");
        checkNodeFilter("field=assignee", "assignee");
        checkNodeFilter("field=reporter", "reporter");
        checkNodeFilter("field=created", "created");
        checkNodeFilter("field=updated", "updated");
        checkNodeFilter("field=resolutiondate", "resolved");
        checkNodeFilter("field=version", "version");
        checkNodeFilter("field=fixVersions", "fixVersion");
        checkNodeFilter("field=component", "component");
        checkNodeFilter("field=due", "due");
        checkNodeFilter("field=votes", "votes");
        checkNodeFilter("field=comments", "comments");
        checkNodeFilter("field=attachment", "attachments");
        checkNodeFilter("field=subtasks", "subtasks");
        checkNodeFilter("field=issuelinks", "issuelinks");

        checkNodeFilter("field=timespent", "timespent");
        checkNodeFilter("field=timeestimate", "timeestimate");
        checkNodeFilter("field=timeoriginalestimate", "timeoriginalestimate");

        checkNodeFilter("field=aggregatetimeoriginalestimate", "aggregatetimeoriginalestimate");
        checkNodeFilter("field=aggregatetimeestimate", "aggregatetimereaminingestimate");
        checkNodeFilter("field=aggregatetimespent", "aggregatetimespent");
    }

    public void testProjectFieldFilter() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        String fieldParam = "field=project";
        String issueKey = PROJECT_HOMOSAP_KEY + "-1";
        gotoPage(getHSP1ViewPage(fieldParam));
        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();
        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/project");
        assertElementExists(doc, "//item/project[@id='10000']");
        assertElementExists(doc, "//item/project[@key='HSP']");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);
    }

    public void testCustomFieldFilter() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        // check all custom fields - all set
        String fieldParam = "field=allcustom";
        String issueKey = PROJECT_HOMOSAP_KEY + "-1";
        gotoPage(getHSP1ViewPage(fieldParam));
        assertEquals("text/xml", getDialog().getResponse().getContentType());
        Document doc = getDocument();
        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/customfields");
        assertElementExists(doc, "//item/customfields/customfield");
        assertElementExists(doc, "//item/customfields/customfield[@id='customfield_10000']");
        assertElementExists(doc, "//item/customfields/customfield[@id='customfield_10001']");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);

        // check the same for other issue - no custom fields set
        fieldParam = "field=allcustom";
        issueKey = PROJECT_HOMOSAP_KEY + "-2";
        gotoPage(getHSP2ViewPage(fieldParam));
        assertEquals("text/xml", getDialog().getResponse().getContentType());
        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/customfields");
        assertElementNotExists(doc, "//item/customfields/customfield");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);

        // check customfield_10000 only
        fieldParam = "field=customfield_10000";
        issueKey = PROJECT_HOMOSAP_KEY + "-1";
        gotoPage(getHSP1ViewPage(fieldParam));
        assertEquals("text/xml", getDialog().getResponse().getContentType());
        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/customfields");
        assertElementExists(doc, "//item/customfields/customfield");
        assertElementExists(doc, "//item/customfields/customfield[@id='customfield_10000']");
        assertElementNotExists(doc, "//item/customfields/customfield[@id='customfield_10001']");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);

        // check customfield_10001 only
        fieldParam = "field=customfield_10001";
        issueKey = PROJECT_HOMOSAP_KEY + "-1";
        gotoPage(getHSP1ViewPage(fieldParam));
        assertEquals("text/xml", getDialog().getResponse().getContentType());
        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/customfields");
        assertElementExists(doc, "//item/customfields/customfield");
        assertElementExists(doc, "//item/customfields/customfield[@id='customfield_10001']");
        assertElementNotExists(doc, "//item/customfields/customfield[@id='customfield_10000']");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);

        // check customfield_10000 only - but for issue where not set
        fieldParam = "field=customfield_10000";
        issueKey = PROJECT_HOMOSAP_KEY + "-2";
        gotoPage(getHSP2ViewPage(fieldParam));
        assertEquals("text/xml", getDialog().getResponse().getContentType());
        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/customfields");
        assertXpathElementNotPresent(doc, "//item/customfields/customfield");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);

        fieldParam = "field=customfield_10001";
        issueKey = PROJECT_HOMOSAP_KEY + "-2";
        gotoPage(getHSP2ViewPage(fieldParam));
        assertEquals("text/xml", getDialog().getResponse().getContentType());
        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/customfields");
        assertXpathElementNotPresent(doc, "//item/customfields/customfield");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);
    }

    public void testMultipleNodeFilter() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        final String fieldParam = "field=title&field=link&field=project"
                + "&field=summary&field=description"
                + "&field=environment&field=issuetype&field=priority"
                + "&field=status&field=resolution&field=security"
                + "&field=assignee&field=reporter&field=created"
                + "&field=updated&field=resolutiondate&field=version&field=fixVersions"
                + "&field=component&field=due&field=votes"
                + "&field=comments&field=attachments&field=subtasks"
                + "&field=issuelinks&field=timespent&field=timeestimate"
                + "&field=timeoriginalestimate&field=aggregatetimeoriginalestimate"
                + "&field=aggregatetimeestimate&field=aggregatetimespent";


        String issueKey = PROJECT_HOMOSAP_KEY + "-1";
        gotoPage(getHSP1ViewPage(fieldParam));

        assertEquals("text/xml", getDialog().getResponse().getContentType());

        Document doc = getDocument();

        assertExpectedXpathValue(doc, "//item/key", issueKey, null);

        assertElementExists(doc, "//item/title");
        assertElementExists(doc, "//item/link");
        assertElementExists(doc, "//item/project");
        assertElementExists(doc, "//item/project[@key]");
        assertElementExists(doc, "//item/summary");
        assertElementExists(doc, "//item/description");
        assertElementExists(doc, "//item/environment");
        assertElementExists(doc, "//item/summary");
        assertElementExists(doc, "//item/type");
        assertElementExists(doc, "//item/priority");
        assertElementExists(doc, "//item/status");
        assertElementExists(doc, "//item/resolution");
        assertElementExists(doc, "//item/security");
        assertElementExists(doc, "//item/assignee");
        assertElementExists(doc, "//item/reporter");
        assertElementExists(doc, "//item/created");
        assertElementExists(doc, "//item/updated");
        assertElementExists(doc, "//item/resolved");
        assertElementExists(doc, "//item/version");
        assertElementExists(doc, "//item/fixVersion");
        assertElementExists(doc, "//item/component");
        assertElementExists(doc, "//item/due");
        assertElementExists(doc, "//item/votes");
        assertElementExists(doc, "//item/comments");
        assertElementExists(doc, "//item/attachments");
        assertElementExists(doc, "//item/subtasks");
        assertElementExists(doc, "//item/issuelinks");

        assertElementExists(doc, "//item/timespent");
        assertElementExists(doc, "//item/timeoriginalestimate");
        assertElementExists(doc, "//item/timeestimate");

        assertElementExists(doc, "//item/aggregatetimeoriginalestimate");
        assertElementExists(doc, "//item/aggregatetimeremainingestimate");
        assertElementExists(doc, "//item/aggregatetimespent");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);
    }

    public void testParentNodeFilter() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        final String fieldParam = "field=parent";


        String issueKey = PROJECT_HOMOSAP_KEY + "-3";
        gotoPage(getHSP3ViewPage(fieldParam));

        assertEquals("text/xml", getDialog().getResponse().getContentType());

        Document doc = getDocument();

        assertExpectedXpathValue(doc, "//item/key", issueKey, null);
        assertElementExists(doc, "//item/parent");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);
    }

    public void testNodeFilterMapping() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        checkNodeFilter("field=pid", "project");
        checkNodeFilter("field=comment", "comments");
        checkNodeFilter("field=components", "component");
        checkNodeFilter("field=duedate", "due");
        checkNodeFilter("field=type", "type");
        checkNodeFilter("field=versions", "version");
        checkNodeFilter("field=fixfor", "fixVersion");
        checkNodeFilter("field=attachments", "attachments");
        checkNodeFilter("field=resolved", "resolved");
        checkNodeFilter("field=aggregatetimeremainingestimate", "aggregatetimeremainingestimate");
    }

    public void testInvalidFieldDefined()
    {
        restoreData("TestXMLIssueCustomView.xml");

        checkFieldDefinitionError("field");
        checkFieldDefinitionError("field=");
        checkFieldDefinitionError("field=nonExisting");
        checkFieldDefinitionError("field=nonExistin1&field=nonExisting2");
    }

    public void testValidAndInvalidFieldDefined() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        checkNodeFilter("field=project&field=nonExisting2", "project");
    }

    public void testTimetrackingField() throws IOException, ParseException
    {
        restoreData("TestXMLIssueCustomView.xml");

        final String fieldParam = "field=timetracking";

        String issueKey = PROJECT_HOMOSAP_KEY + "-1";
        gotoPage(getHSP1ViewPage(fieldParam));

        assertEquals("text/xml", getDialog().getResponse().getContentType());

        Document doc = getDocument();

        assertExpectedXpathValue(doc, "//item/key", issueKey, null);

        assertElementExists(doc, "//item/timespent");
        assertElementExists(doc, "//item/timeoriginalestimate");
        assertElementExists(doc, "//item/timeestimate");

        assertElementExists(doc, "//item/aggregatetimeoriginalestimate");
        assertElementExists(doc, "//item/aggregatetimeremainingestimate");
        assertElementExists(doc, "//item/aggregatetimespent");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);

        setHiddenFields("Time Tracking");
        gotoPage(getHSP1ViewPage(fieldParam));

        assertEquals("text/xml", getDialog().getResponse().getContentType());

        doc = getDocument();

        assertExpectedXpathValue(doc, "//item/key", issueKey, null);

        assertElementNotExists(doc, "//item/timespent");
        assertElementNotExists(doc, "//item/timeoriginalestimate");
        assertElementNotExists(doc, "//item/timeestimate");

        assertElementNotExists(doc, "//item/aggregatetimeoriginalestimate");
        assertElementNotExists(doc, "//item/aggregatetimeremainingestimate");
        assertElementNotExists(doc, "//item/aggregatetimespent");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);

        deactivateTimeTracking();

        checkDisabledTimetracking(issueKey, "field=timetracking");

        checkDisabledTimetracking(issueKey, "field=timespent");
        checkDisabledTimetracking(issueKey, "field=timeoriginalestimate");
        checkDisabledTimetracking(issueKey, "field=timeestimate");
        checkDisabledTimetracking(issueKey, "field=aggregatetimeoriginalestimate");
        checkDisabledTimetracking(issueKey, "field=aggregatetimeremainingestimate");
        checkDisabledTimetracking(issueKey, "field=aggregatetimespent");
    }

    protected void checkDisabledTimetracking(final String issueKey, final String fieldParam)
            throws IOException, ParseException
    {
        gotoPage(getHSP1ViewPage(fieldParam));

        assertEquals("text/xml", getDialog().getResponse().getContentType());

        Document doc = getDocument();

        assertExpectedXpathValue(doc, "//item/key", issueKey, null);

        assertElementNotExists(doc, "//item/timespent");
        assertElementNotExists(doc, "//item/timeoriginalestimate");
        assertElementNotExists(doc, "//item/timeestimate");

        assertElementNotExists(doc, "//item/aggregatetimeoriginalestimate");
        assertElementNotExists(doc, "//item/aggregatetimeremainingestimate");
        assertElementNotExists(doc, "//item/aggregatetimespent");

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);
    }

    protected void checkFieldDefinitionError(String fieldParam)
    {
        String issueKey = PROJECT_HOMOSAP_KEY + "-1";

        String issueUrl = getEnvironmentData().getBaseUrl().toString()
                + "/si/jira.issueviews:issue-xml/" + issueKey + "/" + issueKey + ".xml?" + fieldParam;
        WebRequest request = new GetMethodWebRequest(issueUrl);
        try
        {
            getTester().getTestContext().getWebClient().sendRequest(request);
            fail("Invalid field param passed validation");
        }
        catch (HttpException e)
        {
            assertEquals(400, e.getResponseCode());
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected String getHSP1ViewPage(String fieldParam)
    {
        return "/si/jira.issueviews:issue-xml/HSP-1/HSP-1.xml?" + fieldParam;
    }

    protected String getHSP2ViewPage(final String fieldParam)
    {
        return "/si/jira.issueviews:issue-xml/HSP-2/HSP-2.xml?" + fieldParam;
    }

    protected String getHSP3ViewPage(final String fieldParam)
    {
        return "/si/jira.issueviews:issue-xml/HSP-3/HSP-3.xml?" + fieldParam;
    }

    private void assertCommentDoesNotHaveDetailsOnRestrictingFields(Document doc)
    {
        log("Checking the contents of the XML comment of the XML resulsts");
        
        final Node commentNode = doc.getFirstChild();
        assertNotNull(commentNode);
        assertEquals(Node.COMMENT_NODE, commentNode.getNodeType());
        final String commentText = commentNode.getNodeValue();
        assertNotNull(commentText);
        assertTrue(commentText.indexOf("RSS generated by JIRA") > -1);
        assertEquals(-1, commentText.indexOf("It is possible to restrict the fields"));
    }


    protected void checkNodeFilter(String fieldParam, String expectedField) throws IOException, ParseException
    {
        String issueKey = PROJECT_HOMOSAP_KEY + "-1";

        gotoPage(getHSP1ViewPage(fieldParam));

        assertEquals("text/xml", getDialog().getResponse().getContentType());

        Document doc = getDocument();

        assertExpectedXpathValue(doc, "//item/key", issueKey, null);

        assertElementForCustomView(doc, "//item/project", expectedField);
        assertElementForCustomView(doc, "//item/title", expectedField);
        assertElementForCustomView(doc, "//item/description", expectedField);
        assertElementForCustomView(doc, "//item/environment", expectedField);
        assertElementForCustomView(doc, "//item/summary", expectedField);
        assertElementForCustomView(doc, "//item/type", expectedField);
        assertElementForCustomView(doc, "//item/priority", expectedField);
        assertElementForCustomView(doc, "//item/status", expectedField);
        assertElementForCustomView(doc, "//item/resolution", expectedField);
        assertElementForCustomView(doc, "//item/security", expectedField);
        assertElementForCustomView(doc, "//item/assignee", expectedField);
        assertElementForCustomView(doc, "//item/reporter", expectedField);
        assertElementForCustomView(doc, "//item/created", expectedField);
        assertElementForCustomView(doc, "//item/updated", expectedField);
        assertElementForCustomView(doc, "//item/version", expectedField);
        assertElementForCustomView(doc, "//item/fixVersion", expectedField);
        assertElementForCustomView(doc, "//item/component", expectedField);
        assertElementForCustomView(doc, "//item/due", expectedField);
        assertElementForCustomView(doc, "//item/votes", expectedField);
        assertElementForCustomView(doc, "//item/comments", expectedField);
        assertElementForCustomView(doc, "//item/attachments", expectedField);
        assertElementForCustomView(doc, "//item/subtasks", expectedField);
        assertElementForCustomView(doc, "//item/issuelinks", expectedField);

        assertElementForCustomView(doc, "//item/timespent", expectedField);
        assertElementForCustomView(doc, "//item/timeoriginalestimate", expectedField);
        assertElementForCustomView(doc, "//item/timeestimate", expectedField);

        assertElementForCustomView(doc, "//item/aggregatetimeoriginalestimate", expectedField);
        assertElementForCustomView(doc, "//item/aggregatetimeestimate", expectedField);
        assertElementForCustomView(doc, "//item/aggregatetimespent", expectedField);

        // Ensure the comment of the document does not have details on restricting the fields of the results.
        // As we have requested specific fields, the comment should not contain this information.
        assertCommentDoesNotHaveDetailsOnRestrictingFields(doc);
    }

    protected void assertElementForCustomView(final Document doc, final String xPathExpression, final String expectedField)
    {
        String field = "/" + expectedField;
        if (xPathExpression.endsWith(field))
        {
            assertElementExists(doc, xPathExpression);
        }
        else
        {
            assertElementNotExists(doc, xPathExpression);
        }
    }

    protected void assertElementExists(final Document doc, final String xPathExpression)
    {
        XPath xPath = new XPath(xPathExpression);
        Element element = doc.getElement(xPath);
        assertNotNull("Element at xpath '" + xPathExpression + "' should not be null", element);
    }

    protected void assertElementNotExists(final Document doc, final String xPathExpression)
    {
        XPath xPath = new XPath(xPathExpression);
        Element element = doc.getElement(xPath);
        assertNull("Element at xpath '" + xPathExpression + "' should not be present", element);
    }

    private void assertXpathElementNotPresent(Document doc, String xpathExpression)
    {
        XPath xPath = new XPath(xpathExpression);
        Element element = doc.getElement(xPath);
        if (element != null)
        {
            fail("XML document contains element on path: " + xpathExpression);
        }
    }

    /**
     * Asserts that the given XPATH expression resolves to the given expected value in the given Document.
     *
     * @param doc the Document to check the XPath of
     * @param xPathExpression The XPath to an element you want to check the value of
     * @param expectedValue the expected value.
     * @param attribute null if you're checking an element value, or an attribute name of the selected element
     */
    public void assertExpectedXpathValue(Document doc, String xPathExpression, String expectedValue, String attribute)
    {
        XPath xPath = new XPath(xPathExpression);
        Element element = doc.getElement(xPath);

        String actualValue;
        if (attribute == null)
        {
            actualValue = element.getText().toString();
        }
        else
        {
            // we are checking an attribute value
            actualValue = element.getAttribute(attribute);
        }
        assertEquals(expectedValue, actualValue);

        gotoPage("/secure/project/ViewProjects.jspa"); //need to go back to a page that is HTML, not XML
    }

    protected Document getDocument() throws IOException, ParseException
    {
        InputStream inputStream = getDialog().getResponse().getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileUtils.copy(inputStream, outputStream);
        return new Document(outputStream.toByteArray());
    }

}