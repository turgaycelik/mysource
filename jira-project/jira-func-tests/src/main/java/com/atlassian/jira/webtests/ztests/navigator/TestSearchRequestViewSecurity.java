package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@WebTest ({ Category.FUNC_TEST, Category.FILTERS, Category.SECURITY })
public class TestSearchRequestViewSecurity extends FuncTestCase
{
    private static final String SR_PRINTABLE_SUCCESS = "/sr/jira.issueviews:searchrequest-printable/10000/SearchRequest-10000.html";
    private static final String SR_FULL_CONTENT_SUCCESS = "/sr/jira.issueviews:searchrequest-fullcontent/10000/SearchRequest-10000.html";
    private static final String SR_XML_SUCCESS = "/sr/jira.issueviews:searchrequest-xml/10000/SearchRequest-10000.xml";
    private static final String SR_RSS_SUCCESS = "/sr/jira.issueviews:searchrequest-rss/10000/SearchRequest-10000.xml";
    private static final String SR_RSS_COMMENTS_SUCCESS = "/sr/jira.issueviews:searchrequest-comments-rss/10000/SearchRequest-10000.xml";
    private static final String SR_WORD_SUCCESS = "/sr/jira.issueviews:searchrequest-word/10000/SearchRequest-10000.doc";
    private static final String SR_EXCEL_ALL_SUCCESS = "/sr/jira.issueviews:searchrequest-excel-all-fields/10000/SearchRequest-10000.xls";
    private static final String SR_EXCEL_CURRENT_SUCCESS = "/sr/jira.issueviews:searchrequest-excel-current-fields/10000/SearchRequest-10000.xls";

    private static final String SR_GLOBAL_PRINTABLE_SUCCESS = "/sr/jira.issueviews:searchrequest-printable/10010/SearchRequest-10010.html";
    private static final String SR_GLOBAL_FULL_CONTENT_SUCCESS = "/sr/jira.issueviews:searchrequest-fullcontent/10010/SearchRequest-10010.html";
    private static final String SR_GLOBAL_XML_SUCCESS = "/sr/jira.issueviews:searchrequest-xml/10010/SearchRequest-10010.xml";
    private static final String SR_GLOBAL_RSS_SUCCESS = "/sr/jira.issueviews:searchrequest-rss/10010/SearchRequest-10010.xml";
    private static final String SR_GLOBAL_RSS_COMMENTS_SUCCESS = "/sr/jira.issueviews:searchrequest-comments-rss/10010/SearchRequest-10010.xml";
    private static final String SR_GLOBAL_WORD_SUCCESS = "/sr/jira.issueviews:searchrequest-word/10010/SearchRequest-10010.doc";
    private static final String SR_GLOBAL_EXCEL_ALL_SUCCESS = "/sr/jira.issueviews:searchrequest-excel-all-fields/10010/SearchRequest-10010.xls";
    private static final String SR_GLOBAL_EXCEL_CURRENT_SUCCESS = "/sr/jira.issueviews:searchrequest-excel-current-fields/10010/SearchRequest-10010.xls";


    private static final String SR_RSS_COMMENTS_FAILED = "/sr/jira.issueviews:searchrequest-comments-rss/10001/SearchRequest-10000.xml";
    private static final String SR_PRINTABLE_FAILED = "/sr/jira.issueviews:searchrequest-printable/10001/SearchRequest-10000.html";
    private static final String SR_FULL_CONTENT_FAILED = "/sr/jira.issueviews:searchrequest-fullcontent/10001/SearchRequest-10000.html";
    private static final String SR_XML_FAILED = "/sr/jira.issueviews:searchrequest-xml/10001/SearchRequest-10000.xml";
    private static final String SR_RSS_FAILED = "/sr/jira.issueviews:searchrequest-rss/10001/SearchRequest-10000.xml";
    private static final String SR_WORD_FAILED = "/sr/jira.issueviews:searchrequest-word/10001/SearchRequest-10000.doc";
    private static final String SR_EXCEL_ALL_FAILED = "/sr/jira.issueviews:searchrequest-excel-all-fields/10001/SearchRequest-10000.xls";
    private static final String SR_EXCEL_CURRENT_FAILED = "/sr/jira.issueviews:searchrequest-excel-current-fields/10001/SearchRequest-10000.xls";

    private static final class ContentTypes
    {
        private static final String XML = "text/xml";
        private static final String RSS = "application/rss+xml";
        private static final String WORD = "application/vnd.ms-word";
        private static final String EXCEL = "application/vnd.ms-excel";
    }

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestSearchRequestViewSecurity.xml");
    }

    public void testGlobalSearchRequestRequiresNoLogin()
            throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        //Printable view
        navigation.logout();
        tester.gotoPage(SR_GLOBAL_PRINTABLE_SUCCESS);
        text.assertTextPresent(locator.page(), "HSP-1");
        text.assertTextPresent(locator.page(), "Bug 01");
        text.assertTextPresent(locator.page(), "Back to previous view");

        //Full content view
        navigation.logout();
        tester.gotoPage(SR_GLOBAL_FULL_CONTENT_SUCCESS);
        text.assertTextPresent(locator.page(), "HSP-1");
        text.assertTextPresent(locator.page(), "Bug 01");
        text.assertTextPresent(locator.page(), "Back to previous view");

        //Special case for XML view.
        navigation.logout();
        tester.gotoPage(SR_GLOBAL_XML_SUCCESS);
        assertEquals(ContentTypes.XML, tester.getDialog().getResponse().getContentType());
        String responseText = tester.getDialog().getResponse().getText();
        Document doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        String xpath = "//rss[contains(@version,'0.92')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);

        //Issues RSS feed.  Should prompt for authorization
        navigation.logout();
        try
        {
            tester.gotoPage(SR_GLOBAL_RSS_SUCCESS);
        }
        catch (RuntimeException re)
        {
            fail("Exception occurred: " + re);
        }
        assertEquals(ContentTypes.RSS, tester.getDialog().getResponse().getContentType());
        responseText = tester.getDialog().getResponse().getText();
        doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        xpath = "//rss[contains(@version,'2.0')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);
        xpath = "//item";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);

        //Comments RSS feed.  Should prompt for authorization
        navigation.logout();
        try
        {
            tester.gotoPage(SR_GLOBAL_RSS_COMMENTS_SUCCESS);
        }
        catch (RuntimeException re)
        {
            fail("Exception occurred: " + re);
        }
        assertEquals(ContentTypes.RSS, tester.getDialog().getResponse().getContentType());
        responseText = tester.getDialog().getResponse().getText();
        doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        xpath = "//rss[contains(@version,'2.0')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);
        xpath = "//item";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);

        //Word view
        navigation.logout();
        tester.gotoPage(SR_GLOBAL_WORD_SUCCESS);
        assertEquals(ContentTypes.WORD, tester.getDialog().getResponse().getContentType());

        //Excel All fields view
        navigation.logout();
        tester.gotoPage(SR_GLOBAL_EXCEL_ALL_SUCCESS);
        assertEquals(ContentTypes.EXCEL, tester.getDialog().getResponse().getContentType());

        //Excel current fields view
        navigation.logout();
        tester.gotoPage(SR_GLOBAL_EXCEL_CURRENT_SUCCESS);
        assertEquals(ContentTypes.EXCEL, tester.getDialog().getResponse().getContentType());
    }

    public void testErrorsWithGzipFilter()
    {
        //JRADEV-3406
        administration.generalConfiguration().turnOnGZipCompression();

        navigation.logout();

        navigation.gotoPage("/sr/jira.issueviews:searchrequest-printable/10000/SearchRequest-10000.html");
        text.assertTextPresent(locator.page(), "You must log in to access this page.");

        navigation.login(ADMIN_USERNAME, ADMIN_USERNAME);

        navigation.gotoPage("/sr/jira.issueviews:searchrequest-printable/10002/SearchRequest-10002.html");
        text.assertTextPresent(locator.page(), "Error processing Search Request");
    }

    //Tests that a login is required and after a login the searchrequest can be retrieved successfully.
    public void testLoginRequiredSuccess()
            throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        //Printable view
        navigation.logout();
        tester.gotoPage(SR_PRINTABLE_SUCCESS);
        checkNoLoginPageAndClickLink();
        loginFromErrorPage(ADMIN_USERNAME, ADMIN_PASSWORD);
        text.assertTextPresent(locator.page(), "HSP-1");
        text.assertTextPresent(locator.page(), "Bug 01");
        text.assertTextPresent(locator.page(), "Back to previous view");

        //Full content view
        navigation.logout();
        tester.gotoPage(SR_FULL_CONTENT_SUCCESS);
        checkNoLoginPageAndClickLink();
        loginFromErrorPage(ADMIN_USERNAME, ADMIN_PASSWORD);
        text.assertTextPresent(locator.page(), "HSP-1");
        text.assertTextPresent(locator.page(), "Bug 01");
        text.assertTextPresent(locator.page(), "Back to previous view");

        //Special case for XML view.  This will not require a login, but return an empty XML document instead.
        navigation.logout();
        tester.gotoPage(SR_XML_SUCCESS);
        assertEquals(ContentTypes.XML, tester.getDialog().getResponse().getContentType());
        String responseText = tester.getDialog().getResponse().getText();
        Document doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        String xpath = "//rss[contains(@version,'0.92')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);
        xpath = "//item";
        log("Testing for non-existance of xpath [" + xpath + "]");
        XMLAssert.assertXpathNotExists(xpath, doc);
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        tester.gotoPage(SR_XML_SUCCESS);
        assertEquals(ContentTypes.XML, tester.getDialog().getResponse().getContentType());
        responseText = tester.getDialog().getResponse().getText();
        doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        xpath = "//rss[contains(@version,'0.92')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);
        xpath = "//item";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);

        //Issues RSS feed.  Should prompt for authorization
        navigation.logout();
        try
        {
            tester.gotoPage(SR_RSS_SUCCESS);
            fail("Should have been prompted for authentication");
        }
        catch (RuntimeException re)
        {
            assertTrue(re.getMessage().contains("AuthorizationRequiredException"));
            log("Received auth challenge!");
        }
        tester.gotoPage(SR_RSS_SUCCESS + "?os_username=admin&os_password=admin");
        assertEquals(ContentTypes.RSS, tester.getDialog().getResponse().getContentType());
        responseText = tester.getDialog().getResponse().getText();
        doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        xpath = "//rss[contains(@version,'2.0')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);
        xpath = "//item";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);

        //Comments RSS feed.  Should prompt for authorization
        navigation.logout();
        try
        {
            tester.gotoPage(SR_RSS_COMMENTS_SUCCESS);
            fail("Should have been prompted for authentication");
        }
        catch (RuntimeException re)
        {
            assertTrue(re.getMessage().contains("AuthorizationRequiredException"));
            log("Received auth challenge!");
        }
        tester.gotoPage(SR_RSS_COMMENTS_SUCCESS + "?os_username=admin&os_password=admin");
        assertEquals(ContentTypes.RSS, tester.getDialog().getResponse().getContentType());
        responseText = tester.getDialog().getResponse().getText();
        doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        xpath = "//rss[contains(@version,'2.0')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);
        xpath = "//item";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);

        //Word view
        navigation.logout();
        tester.gotoPage(SR_WORD_SUCCESS);
        checkNoLoginPageAndClickLink();
        loginFromErrorPage(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(ContentTypes.WORD, tester.getDialog().getResponse().getContentType());

        //Excel All fields view
        navigation.logout();
        tester.gotoPage(SR_EXCEL_ALL_SUCCESS);
        checkNoLoginPageAndClickLink();
        loginFromErrorPage(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(ContentTypes.EXCEL, tester.getDialog().getResponse().getContentType());

        //Excel current fields view
        navigation.logout();
        tester.gotoPage(SR_EXCEL_CURRENT_SUCCESS);
        checkNoLoginPageAndClickLink();
        loginFromErrorPage(ADMIN_USERNAME, ADMIN_PASSWORD);
        assertEquals(ContentTypes.EXCEL, tester.getDialog().getResponse().getContentType());
    }

    public void testLoginRequiredAndSearchRequestDoesntExist()
            throws IOException, ParserConfigurationException, SAXException, TransformerException
    {
        checkLoginInvalidSRorPermission(SR_PRINTABLE_FAILED, SR_FULL_CONTENT_FAILED, SR_XML_FAILED,
                SR_RSS_FAILED, SR_RSS_COMMENTS_FAILED, SR_WORD_FAILED, SR_EXCEL_ALL_FAILED,
                SR_EXCEL_CURRENT_FAILED, ADMIN_USERNAME);
    }

    public void testLoginRequiredAndInvalidPermission()
            throws TransformerException, IOException, ParserConfigurationException, SAXException
    {
        checkLoginInvalidSRorPermission(SR_PRINTABLE_SUCCESS, SR_FULL_CONTENT_SUCCESS, SR_XML_SUCCESS,
                SR_RSS_SUCCESS, SR_RSS_COMMENTS_SUCCESS, SR_WORD_SUCCESS, SR_EXCEL_ALL_SUCCESS,
                SR_EXCEL_CURRENT_SUCCESS, FRED_USERNAME);
    }

    private void loginFromErrorPage(String username, String password)
    {
        tester.setFormElement("os_username", username);
        tester.setFormElement("os_password", password);
        tester.setWorkingForm("login-form");
        tester.submit();
    }

    private void checkNoLoginPageAndClickLink()
    {
        text.assertTextPresent(locator.page(), "You must log in to access this page.");
    }

    private void checkErrorProcessingSearchRequest()
    {
        text.assertTextPresent(locator.page(), "Error processing Search Request");
        text.assertTextPresent(locator.page(), "The saved filter you are trying to view no longer exists or you do not have access rights to view it.");
    }

    private void checkLoginSearchRequestDoesntExist(String url, String username, String password)
    {
        navigation.logout();
        tester.gotoPage(url);
        checkNoLoginPageAndClickLink();
        loginFromErrorPage(username, password);
        checkErrorProcessingSearchRequest();
    }

    private void checkLoginInvalidSRorPermission(String printable, String fullContent, String xml, String rss, String rssComments, String word, String excelAll, String excelCurrent, String usernamePassword)
            throws IOException, SAXException, ParserConfigurationException, TransformerException
    {
        checkLoginSearchRequestDoesntExist(printable, usernamePassword, usernamePassword);

        //Full content view
        checkLoginSearchRequestDoesntExist(fullContent, usernamePassword, usernamePassword);

        //Special case for XML view.  This will not require a login, but return an empty XML document instead.
        navigation.logout();
        tester.gotoPage(xml);
        assertEquals(ContentTypes.XML, tester.getDialog().getResponse().getContentType());
        String responseText = tester.getDialog().getResponse().getText();
        Document doc = XMLUnit.buildControlDocument(responseText);
        //only check for the correct version of rss.
        String xpath = "//rss[contains(@version,'0.92')]";
        log("Testing for xpath [" + xpath + "]");
        XMLAssert.assertXpathExists(xpath, doc);
        xpath = "//item";
        log("Testing for non-existance of xpath [" + xpath + "]");
        XMLAssert.assertXpathNotExists(xpath, doc);

        //Issues RSS feed.  Should prompt for authorization
        navigation.logout();
        try
        {
            tester.gotoPage(rss);
            fail("Should have been prompted for authentication");
        }
        catch (RuntimeException re)
        {
            assertTrue(re.getMessage().contains("AuthorizationRequiredException"));
            log("Received auth challenge!");
        }
        try
        {
            tester.gotoPage(rss + "?os_username=admin&os_password=admin");
        }
        catch (RuntimeException e)
        {
            assertTrue(e.getMessage().contains("Error on HTTP request: 403"));
            log("recieved 403 error");
        }

        //Comments RSS feed.  Should prompt for authorization
        navigation.logout();
        try
        {
            tester.gotoPage(rssComments);
            fail("Should have been prompted for authentication");
        }
        catch (RuntimeException re)
        {
            assertTrue(re.getMessage().contains("AuthorizationRequiredException"));
            log("Received auth challenge!");
        }
        try
        {
            tester.gotoPage(rssComments + "?os_username=admin&os_password=admin");
        }
        catch (RuntimeException e)
        {
            assertTrue(e.getMessage().contains("Error on HTTP request: 403"));
            log("recieved 403 error");
        }

        //Word view
        checkLoginSearchRequestDoesntExist(word, usernamePassword, usernamePassword);

        //Excel All fields view
        checkLoginSearchRequestDoesntExist(excelAll, usernamePassword, usernamePassword);

        //Excel current fields view
        checkLoginSearchRequestDoesntExist(excelCurrent, usernamePassword, usernamePassword);
    }
}