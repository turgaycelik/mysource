package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.core.util.FileUtils;
import com.atlassian.core.util.XMLUtils;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.ParseException;
import electric.xml.XPath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Tests XML Issue view for escaping chars, present nodes, etc.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestXmlIssueView extends FuncTestCase
{
    private static final String ISSUE1 = "HSP-1";
    private static final String TESTUSERNAME = "testuser";
    private static final String TESTUSERFNAME = "User Fullname";
    private static final String TESTUSEREMAIL = "test@test.com";
    private static final String TESTUSERPASS = "12345";
    private static final String TESTPROJECTSUMMARY = "a new bug";
    private static final String TESTDESCRIP = "A nice description";
    private static final String TESTENVI = "A test environment";
    private static final String i18n = "\u0126\u0118\u0139\u0139\u0150";


    public void testPotentiallyInvalidCharacters() throws ParseException, IOException
    {
        administration.restoreBlankInstance();
        _testSingleCharacter("<", "&lt;");
        _testSingleCharacter(">", "&gt;");
        _testSingleCharacter("&", "&amp;");
        //null XML character should be escaped
        _testSingleCharacter("\u0000", "");
        // The following can fail if you don't have your DB and JDBC drivers configured to use Unicode.
        // eg see http://www.atlassian.com/software/jira/docs/latest/databases/mysql.html for instructions on setting up
        // MySQL correctly to use UTF-8.
        _testSingleCharacter(i18n, XMLUtils.escape(i18n));
        _testSingleCharacter("\u0014", "");
    }

    private void _testSingleCharacter(String unescapedCharacter, String escapedCharacter)
    {
        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, "Bug", "(Test Issue with " + unescapedCharacter + ")");
        navigation.issue().gotoIssue(key);
        tester.clickLinkWithText("XML");
        assertEquals("text/xml", tester.getDialog().getResponse().getContentType());
        assertions.getTextAssertions().assertTextPresent("(Test Issue with " + escapedCharacter + ")");

        tester.gotoPage("/secure/project/ViewProjects.jspa"); //need to go back to a page that is HTML, not XML
    }

    public void testUsers() throws IOException, ParseException
    {
        _testEscapedUsers("(", "%28");
        _testEscapedUsers(")", "%29");
        _testEscapedUsers("'", "%27");
    }

    public void _testEscapedUsers(String unEscapedCharacter, String urlEncodedCharacter) throws IOException
    {
        administration.restoreBlankInstance();

        administration.usersAndGroups().addUser(TESTUSERNAME + unEscapedCharacter, TESTUSERPASS, TESTUSERFNAME, TESTUSEREMAIL);
        administration.usersAndGroups().addUserToGroup(TESTUSERNAME + urlEncodedCharacter, "jira-developers");

        navigation.logout();
        navigation.login(TESTUSERNAME + unEscapedCharacter, TESTUSERPASS);

        String key = navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Test Issue      ");
        navigation.issue().gotoIssue(key);
        tester.clickLinkWithText("XML");
        assertEquals("text/xml", tester.getDialog().getResponse().getContentType());

        // now check if the web client can get the response as a Document object,
        // if so then the XML escaping has occurred correctly and there's no need
        // to make any assertions about it.
        try
        {
            getDocument();
        }
        catch (ParseException e)
        {
            fail("document not well-formed");
        }
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testEscapingCommentVisibility() throws Exception
    {
        administration.restoreData("TestXMLIssueView.xml");
        navigation.issue().gotoIssue(PROJECT_MONKEY_KEY + "-1");
        tester.clickLinkWithText("XML");
        Document doc = getDocument();
        assertExpectedXpathValue(doc, "//item/comments/comment[1]", ADMIN_USERNAME, "author");
        assertExpectedXpathValue(doc, "//item/comments/comment[1]", "<Xml Nasty & Role>", "rolelevel");
    }

    public void testAggregateTimeTracking() throws IOException, ParseException
    {
        administration.restoreData("TestSearchRequestViewsAndIssueViews.xml");

        navigation.issue().gotoIssue("HSP-10");
        tester.clickLinkWithText("XML");
        Document doc = getDocument();
        assertExpectedXpathValue(doc, "//item/timeoriginalestimate", "86400", "seconds");
        assertExpectedXpathValue(doc, "//item/timeestimate", "86400", "seconds");
        assertXpathElementNotPresent(doc, "//item/timespent");
        assertXpathElementNotPresent(doc, "//item/aggregatetimeoriginalestimate");
        assertXpathElementNotPresent(doc, "//item/aggregatetimeremainingestimate");
        assertXpathElementNotPresent(doc, "//item/aggregatetimespent");

        administration.subtasks().enable();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
        subTaskify("HSP-12", "HSP-10");
        subTaskify("HSP-11", "HSP-10");

        navigation.issue().gotoIssue("HSP-10");
        tester.clickLinkWithText("XML");
        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/timeoriginalestimate", "86400", "seconds");
        assertExpectedXpathValue(doc, "//item/timeestimate", "86400", "seconds");
        assertXpathElementNotPresent(doc, "//item/timespent");
        assertExpectedXpathValue(doc, "//item/aggregatetimeoriginalestimate", "86400", "seconds");
        assertExpectedXpathValue(doc, "//item/aggregatetimeremainingestimate", "88200", "seconds");
        assertExpectedXpathValue(doc, "//item/aggregatetimespent", "12000", "seconds");
    }

    /**
     * Convert the given issue into a subtask of the given parent issue.
     *
     * @param issueKey  issue key
     * @param parentKey parent issue key
     */
    protected void subTaskify(String issueKey, String parentKey)
    {
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("issue-to-subtask");
        tester.setFormElement("parentIssueKey", parentKey);
        tester.submit("Next >>");
        tester.submit("Next >>");
        tester.submit("Finish");
    }

    // JRA-16508 -- ensure that attachments appear in XML view even if the field is hidden.
    public void testAttachmentsAlwaysPresent() throws IOException, ParseException
    {
        administration.restoreData("TestXMLIssueView.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().hideField(2);

        navigation.issue().gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
        tester.clickLinkWithText("XML");
        Document doc = getDocument();
        assertExpectedXpathValue(doc, "//item/attachments/attachment", "test.txt", "name");
        assertExpectedXpathValue(doc, "//item/attachments/attachment", "8", "size");
        assertExpectedXpathValue(doc, "//item/attachments/attachment", ADMIN_USERNAME, "author");
    }

    public void testNodeExistence() throws IOException, ParseException
    {
        administration.restoreData("TestXMLIssueView.xml");
        navigation.issue().gotoIssue(PROJECT_HOMOSAP_KEY + "-1");
        tester.clickLinkWithText("XML");
        assertEquals("text/xml", tester.getDialog().getResponse().getContentType());

        Document doc = getDocument();

        // When comparing values ensure that the spaces are included as spaces could break some clients. For example, see JRA-16175
        assertExpectedXpathValue(doc, "//channel/link", getEnvironmentData().getBaseUrl().toString(), null);

        assertElementExists(doc, "//item/created");
        assertElementExists(doc, "//item/updated");

        assertExpectedXpathValue(doc, "//item/title", "[" + PROJECT_HOMOSAP_KEY + "-1] " + TESTPROJECTSUMMARY, null);
        // When comparing values ensure that the spaces are included as spaces could break some clients. For example, see JRA-16175 
        assertExpectedXpathValue(doc, "//item/link", getEnvironmentData().getBaseUrl().toString() + "/browse/" + PROJECT_HOMOSAP_KEY + "-1", null);
        assertExpectedXpathValue(doc, "//item/description", TESTDESCRIP, null);//description is there
        assertExpectedXpathValue(doc, "//item/environment", TESTENVI, null);
        assertExpectedXpathValue(doc, "//item/resolution", "Unresolved", null);
        assertExpectedXpathValue(doc, "//item/votes", "0", null);

        assertExpectedXpathValue(doc, "//item/assignee", ADMIN_FULLNAME, null);
        assertExpectedXpathValue(doc, "//item/assignee", ADMIN_USERNAME, "username");
        assertExpectedXpathValue(doc, "//item/reporter", ADMIN_USERNAME, "username");

        assertExpectedXpathValue(doc, "//item/fixVersion[1]", "New Version 1", null);
        assertExpectedXpathValue(doc, "//item/fixVersion[2]", "New Version 4", null);

        assertExpectedXpathValue(doc, "//item/version[1]", "New Version 1", null);
        assertExpectedXpathValue(doc, "//item/version[2]", "New Version 4", null);

        assertExpectedXpathValue(doc, "//item/attachments/attachment", "test.txt", "name");
        assertExpectedXpathValue(doc, "//item/attachments/attachment", "8", "size");
        assertExpectedXpathValue(doc, "//item/attachments/attachment", ADMIN_USERNAME, "author");

        assertExpectedXpathValue(doc, "//item/component[1]", "New Component 1", null);
        assertExpectedXpathValue(doc, "//item/key", PROJECT_HOMOSAP_KEY + "-1", null);

        assertExpectedXpathValue(doc, "//item/comments/comment[1]", ADMIN_USERNAME, "author");
        assertExpectedXpathValue(doc, "//item/comments/comment[2]", "a nice comment", null);
        assertExpectedXpathValue(doc, "//item/comments/comment[3]", "10020", "id");

        assertExpectedXpathValue(doc, "//item/type", "Bug", null); //it is a bug
        assertExpectedXpathValue(doc, "//item/type", "1", "id");

        assertExpectedXpathValue(doc, "//item/priority", "Major", null);
        assertExpectedXpathValue(doc, "//item/priority", "3", "id");

        assertExpectedXpathValue(doc, "//item/status", "4", "id");
        assertExpectedXpathValue(doc, "//item/status", "Reopened", null);


        //create a resolved issue so that we can check it shows the resolved date in the xml view
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        tester.setFormElement("summary", "My test issue for resolved date");
        tester.submit("Create");

        //resolve the issue
        tester.clickLink("action_id_5");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //check we're on the right screen.
        tester.assertTextPresent("My test issue for resolved date");
        tester.assertTextPresent("Resolved");

        //now check its XML view
        tester.clickLinkWithText("XML");
        assertEquals("text/xml", tester.getDialog().getResponse().getContentType());

        doc = getDocument();

        assertElementExists(doc, "//item/created");
        assertElementExists(doc, "//item/updated");
        assertElementExists(doc, "//item/resolved");
    }

    private void assertElementExists(final Document doc, final String xPathExpression)
    {
        XPath xPath = new XPath(xPathExpression);
        Element element = doc.getElement(xPath);
        assertNotNull("Element at xpath '" + xPathExpression + "' should not be null", element);
    }

    //JRA-13343
    public void testRawRssView() throws IOException, ParseException
    {
        administration.restoreData("TestXMLIssueView.xml");

        //setup some testdata
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink("edit-issue");
        tester.setFormElement("description", "some test data\n\n<badchars?>");
        tester.setFormElement("environment", "some test environment\n\n<badchars?>");
        tester.submit("Update");

        tester.gotoPage("/si/jira.issueviews:issue-xml/HSP-1/HSP-1.xml?rssMode=raw");
        assertEquals("text/xml", tester.getDialog().getResponse().getContentType());

        Document doc = getDocument();
        assertExpectedXpathValue(doc, "//item/description", "<![CDATA[some test data\r\n\r\n<badchars?>]]>", null);//description is there
        assertExpectedXpathValue(doc, "//item/environment", "<![CDATA[some test environment\r\n\r\n<badchars?>]]>", null);

        tester.gotoPage("/si/jira.issueviews:issue-xml/HSP-1/HSP-1.xml?rssMode=somebadmode");
        assertEquals("text/xml", tester.getDialog().getResponse().getContentType());

        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/description", "some test data\r&lt;br/&gt;\n\r&lt;br/&gt;\n&amp;lt;badchars?&amp;gt;", null);//description is there
        assertExpectedXpathValue(doc, "//item/environment", "some test environment\r&lt;br/&gt;\n\r&lt;br/&gt;\n&amp;lt;badchars?&amp;gt;", null);

        tester.gotoPage("/si/jira.issueviews:issue-xml/HSP-1/HSP-1.xml");
        assertEquals("text/xml", tester.getDialog().getResponse().getContentType());

        doc = getDocument();
        assertExpectedXpathValue(doc, "//item/description", "some test data\r&lt;br/&gt;\n\r&lt;br/&gt;\n&amp;lt;badchars?&amp;gt;", null);//description is there
        assertExpectedXpathValue(doc, "//item/environment", "some test environment\r&lt;br/&gt;\n\r&lt;br/&gt;\n&amp;lt;badchars?&amp;gt;", null);
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
     * Asserts that the given XPATH expression resolves to the given expected
     * value in the given Document.
     *
     * @param doc             the Document to check the XPath of
     * @param xPathExpression The XPath to an element you want to check the value of
     * @param expectedValue   the expected value.
     * @param attribute       null if you're checking an element value, or an attribute name of the selected element
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

        tester.gotoPage("/secure/project/ViewProjects.jspa"); //need to go back to a page that is HTML, not XML
    }

    //tests if <item><type = x>"type" where x correctly corresponds to "type"

    //hide each field in turn and see if this is reflected in the xml
    public void testCustomFields() throws IOException, ParseException
    {
        administration.restoreData("TestXMLIssueView.xml");//restore issue with all fields populated for xml test

        //_hidefield(node to check hidden, field to hide)
        hideFields("//item/version", "Affects Version/s");
        hideFields("//item/assignee", "Assignee");
        //_hideFields("//item/attachments","Attachment"); //not meant to be hidden
        hideFields("//item/component", "Component/s");
        hideFields("//item/due", "Due Date");
        hideFields("//item/environment", "Environment");
        hideFields("//item/fixVersion", "Fix Version/s");
        hideFields("//item/priority", "Priority");
        hideFields("//item/reporter", "Reporter");
        hideFields("//item/resolution", "Resolution"); //this is playing up: not showing/not hiding
        hideFields("//item/security", "Security Level"); //this isn't displaying
    }

    public void testTranslation() throws IOException, ParseException
    {
        try
        {
            _testTranslation("//item/type", "Bug");
            _testTranslation("//item/priority", "Major");
            _testTranslation("//item/status", "Open");
            _testTranslation("//item/resolution", "Unresolved");
        }
        finally
        {
            // This needs to be done so the next test doesn't fail
            // as the PrettyDurationFormatter caches the i18n bean
            navigation.gotoDashboard();
            administration.restoreData("TestXMLIssueView.xml");
            navigation.userProfile().gotoCurrentUserProfile();
            tester.clickLink("edit_prefs_lnk");
            tester.selectOption("userLocale", "English (UK)");
            tester.setWorkingForm("update-user-preferences");
            tester.submit();
        }
    }

    public void _testTranslation(String xpathexpr, String notExpected) throws IOException, ParseException
    {
        navigation.gotoDashboard();
        administration.restoreData("TestXMLIssueView.xml");
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("edit_prefs_lnk");
        tester.setFormElement("userIssuesPerPage", "50");
        tester.selectOption("userLocale", "Deutsch (Deutschland)");
        tester.submit();

        navigation.issue().createIssue(null, null, "Testing German");
        tester.clickLinkWithText("XML");

        Document doc = getDocument();
        XPath xPath = new XPath(xpathexpr);
        Element element = doc.getElement(xPath);

        // This will throw a NPE if the xpath doesn't actually exist in the document
        element.getElement(xPath);

        assertNotSame("Test if" + notExpected + "was translated", element.getText().toString(), notExpected);
    }


    private void hideFields(String findNode, String fieldToHide) throws IOException, ParseException
    {
        administration.fieldConfigurations().defaultFieldConfiguration().hideFields(fieldToHide);

        navigation.issue().gotoIssue(ISSUE1);
        tester.clickLinkWithText("XML");

        Document doc = getDocument();
        XPath xPath = new XPath(findNode);
        Element element = doc.getElement(xPath);

        assertNull("Testing that " + fieldToHide + " node is NOT shown in xml view if hidden", element);

        administration.fieldConfigurations().defaultFieldConfiguration().showFields(fieldToHide);
    }

    private Document getDocument() throws IOException, ParseException
    {
        InputStream inputStream = tester.getDialog().getResponse().getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileUtils.copy(inputStream, outputStream);
        return new Document(outputStream.toByteArray());
    }
}
