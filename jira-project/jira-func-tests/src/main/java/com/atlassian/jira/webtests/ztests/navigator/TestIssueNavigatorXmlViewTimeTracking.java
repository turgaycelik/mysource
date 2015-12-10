package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import electric.xml.Document;
import electric.xml.ParseException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR })
public class TestIssueNavigatorXmlViewTimeTracking extends JIRAWebTest
{
    public TestIssueNavigatorXmlViewTimeTracking(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestSearchRequestViewsAndIssueViews.xml");
    }

    public void testTimeTrackingEnabled() throws SAXException, IOException, ParseException
    {
        activateSubTasks();
        subTaskify("HSP-12", "HSP-10");
        subTaskify("HSP-11", "HSP-10");

        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
        assertEquals("text/xml", getDialog().getResponse().getContentType());

        Document doc = getDocument();

        // assert HSP-12 does not have any time recorded
        assertAttributeNotPresent(doc, "HSP-12", "timeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-12", "timeestimate");
        assertAttributeNotPresent(doc, "HSP-12", "timespent");
        assertAttributeNotPresent(doc, "HSP-12", "aggregatetimeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-12", "aggregatetimeremainingestimate");
        assertAttributeNotPresent(doc, "HSP-12", "aggregatetimespent");

        // assert HSP-11 has some time recorded, but not aggregates as it is a sub-task
        assertAttributeNotPresent(doc, "HSP-11", "timeoriginalestimate");
        assertAttributePresentWithValue(doc, "HSP-11", "timeestimate", "1800");
        assertAttributePresentWithValue(doc, "HSP-11", "timespent", "12000");
        assertAttributeNotPresent(doc, "HSP-11", "aggregatetimeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-11", "aggregatetimeremainingestimate");
        assertAttributeNotPresent(doc, "HSP-11", "aggregatetimespent");

        // assert HSP-10 has time recorded including its sub-tasks
        assertAttributePresentWithValue(doc, "HSP-10", "timeoriginalestimate", "86400");
        assertAttributePresentWithValue(doc, "HSP-10", "timeestimate", "86400");
        assertAttributeNotPresent(doc, "HSP-10", "timespent");
        assertAttributePresentWithValue(doc, "HSP-10", "aggregatetimeoriginalestimate", "86400");
        assertAttributePresentWithValue(doc, "HSP-10", "aggregatetimeremainingestimate", "88200");
        assertAttributePresentWithValue(doc, "HSP-10", "aggregatetimespent", "12000");
    }

    public void testTimeTrackingDisabled() throws SAXException, IOException, ParseException
    {
        activateSubTasks();
        subTaskify("HSP-12", "HSP-10");
        subTaskify("HSP-11", "HSP-10");
        deactivateTimeTracking();

        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
        assertEquals("text/xml", getDialog().getResponse().getContentType());

        Document doc = getDocument();

        // assert HSP-12 does not have any time recorded
        assertAttributeNotPresent(doc, "HSP-12", "timeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-12", "timeestimate");
        assertAttributeNotPresent(doc, "HSP-12", "timespent");
        assertAttributeNotPresent(doc, "HSP-12", "aggregatetimeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-12", "aggregatetimeremainingestimate");
        assertAttributeNotPresent(doc, "HSP-12", "aggregatetimespent");

        // assert HSP-11 has some time recorded, but not aggregates as it is a sub-task
        assertAttributeNotPresent(doc, "HSP-11", "timeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-11", "timeestimate");
        assertAttributeNotPresent(doc, "HSP-11", "timespent");
        assertAttributeNotPresent(doc, "HSP-11", "aggregatetimeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-11", "aggregatetimeremainingestimate");
        assertAttributeNotPresent(doc, "HSP-11", "aggregatetimespent");

        // assert HSP-10 has time recorded including its sub-tasks
        assertAttributeNotPresent(doc, "HSP-10", "timeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-10", "timeestimate");
        assertAttributeNotPresent(doc, "HSP-10", "timespent");
        assertAttributeNotPresent(doc, "HSP-10", "aggregatetimeoriginalestimate");
        assertAttributeNotPresent(doc, "HSP-10", "aggregatetimeremainingestimate");
        assertAttributeNotPresent(doc, "HSP-10", "aggregatetimespent");
    }

    private void assertAttributeNotPresent(Document doc, String issueKey, String attributeName)
    {
        Node issue = getIssueWithKey(doc, issueKey);
        if (issue != null)
        {
            Node issueAttribute = getIssueAttribute(issue, attributeName);
            assertNull(issueAttribute);
        }
    }

    private void assertAttributePresentWithValue(Document doc, String issueKey, String attributeName, String expectedValue)
    {
        Node issue = getIssueWithKey(doc, issueKey);
        if (issue != null)
        {
            Node issueAttribute = getIssueAttribute(issue, attributeName);
            if (issueAttribute != null)
            {
                NamedNodeMap attributes = issueAttribute.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++)
                {
                    Node attribute = attributes.item(i);
                    if ("seconds".equals(attribute.getNodeName()))
                    {
                        if (expectedValue.equals(attribute.getNodeValue()))
                        {
                            return;
                        }
                        assertEquals(expectedValue, attribute.getNodeValue());
                    }
                }
            }
            fail();
        }
    }

    private Node getIssueWithKey(Document doc, String key)
    {
        if (key != null)
        {
            NodeList issues = doc.getElementsByTagName("item");
            for (int i = 0; i < issues.getLength(); i++)
            {
                Node issue = issues.item(i);
                Node keyAttribute = getIssueAttribute(issue, "key");
                if (key.equals(keyAttribute.getFirstChild().getNodeValue()))
                {
                    return issue;
                }
            }
        }
        return null;
    }

    private Node getIssueAttribute(Node issue, String attribute)
    {
        if (attribute != null)
        {
            NodeList issueAttributes = issue.getChildNodes();
            for (int i = 0; i < issueAttributes.getLength(); i++)
            {
                Node issueAttribute = issueAttributes.item(i);
                if (attribute.equals(issueAttribute.getNodeName()))
                {
                    return issueAttribute;
                }
            }
        }
        return null;
    }

    /**
     * Convert the given issue into a subtask of the given parent issue.
     *
     * @param issueKey  issue key
     * @param parentKey parent issue key
     */
    private void subTaskify(String issueKey, String parentKey)
    {
        gotoIssue(issueKey);
        clickLink("issue-to-subtask");
        setFormElement("parentIssueKey", parentKey);
        submit("Next >>");
        submit("Next >>");
        submit("Finish");
    }

    private Document getDocument() throws IOException, ParseException
    {
        InputStream inputStream = getDialog().getResponse().getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileUtils.copy(inputStream, outputStream);
        return new Document(outputStream.toByteArray());
    }
}