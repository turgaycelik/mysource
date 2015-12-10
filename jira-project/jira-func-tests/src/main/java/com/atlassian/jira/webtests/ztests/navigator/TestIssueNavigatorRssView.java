package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractTestIssueNavigatorXmlView;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebResponseUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dom4j.DocumentException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigatorRssView extends AbstractTestIssueNavigatorXmlView
{
    protected static final String XML_LINK_COMMENT_AFTER_12APR = "updated+%3E%3D+2006-04-12";
    protected static final String XML_LINK_COMMENT_BEFORE_16APR = "updated+%3C%3D+2006-04-16";
    protected static final String[] XML_LINK_COMMENT_23_25NOV = new String[] { "updated+%3C%3D+2006-11-25", "updated+%3E%3D+2006-11-23" };
    protected static final String[] XML_LINK_COMMENT_12_16APR = new String[] { XML_LINK_COMMENT_AFTER_12APR, XML_LINK_COMMENT_BEFORE_16APR };

    public TestIssueNavigatorRssView(String name)
    {
        super(name);
    }

    public void testCommentsRssFeedUpdated() throws IOException, SAXException, ParserConfigurationException, TransformerException
    {
        restoreData("TestSearchRequestViewsAndIssueViews.xml");
        administration.generalConfiguration().setJiraLocale("English (UK)");
        // grant edit-all-comments permission to us
        grantGroupPermission(34, "jira-users");

        // Jump to the link to the comment rss feed and make sure there are no items to begin with
        gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?pid=10000&updated%3Aprevious=-2d&sorter/field=issuekey&sorter/order=DESC&tempMax=1000");
        WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html");

        String xpath = "//item";
        String responseText = getDialog().getResponse().getText();
        Document doc = XMLUnit.buildControlDocument(responseText);

        XMLAssert.assertXpathNotExists(xpath, doc);

        gotoPage("/secure/Dashboard.jspa");
        gotoIssue("HSP-11");

        // edit first comment
        clickLink("edit_comment_10000");
        setWorkingForm("comment-edit");
        final String newDescription = "RSS rocks!";
        setFormElement("comment", newDescription);
        submit("Save");

        // search for all issues that were updated in last 2 days
        gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?pid=10000&updated%3Aprevious=-2d&sorter/field=issuekey&sorter/order=DESC&tempMax=1000");

        // verify the updated comment is changed in RSS feed
        responseText = getDialog().getResponse().getText();
        doc = XMLUnit.buildControlDocument(responseText);
        final String NEW_XML_LINK_COMMENT = "/secure/IssueNavigator.jspa?reset=true&amp;jqlQuery=project+%3D+HSP+AND+updated+%3E%3D+-2d";
        xpath = "//channel[title='" + XML_TITLE + "'][contains(link,'" + getEnvironmentData().getBaseUrl()
                + NEW_XML_LINK_COMMENT + "')][description='" + XML_DESCRIPTION_MULTIPLE + "'][language='"
                + XML_LANGUAGE + "']";

        log("Searching for existence of xpath " + xpath);
        XMLAssert.assertXpathExists(xpath, doc);

        Item item = commentItem3;
        xpath = "//item[title='" + item.getAttribute(ATT_TITLE) + "']" +
                "[contains(link,'" + item.getAttribute("link") + "')]" +
                "[contains(description, '" + newDescription + "')]" +
                "[contains(description, '" + item.getAttribute("description_link_profile") + "')]" +
                "[contains(description, 'Edited by')]" +
                "[contains(description, '" + item.getAttribute("description_link_issue") + "')]";
        log("Searching for existence of xpath " + xpath);
        XMLAssert.assertXpathExists(xpath, doc);
    }

    public void testRssIssuesViewHaveCorrectLinkToTheFilter() throws DocumentException
    {
        String filterId = backdoor.filters().createFilter("issuetype=\"New Feature\"", "rssfeed");
        tester.gotoPage("/sr/jira.issueviews:searchrequest-rss/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=1000");
        assertAndGetLinkToFilterWithId(filterId);
    }

    public void testRssCommentsViewHaveCorrectLinkToTheFilter() throws DocumentException
    {
        String filterId = backdoor.filters().createFilter("issuetype=\"New Feature\"", "rssfeed");
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/" + filterId + "/SearchRequest-" + filterId + ".xml?tempMax=1000");
        assertAndGetLinkToFilterWithId(filterId);
    }


    public void testRssIssueFeedWithSubtaskTimeTracking()
    {
        // test simple issues with time tracking info
        tester.gotoPage("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
        assertTextSequence(new String[] {
                "HSP-12",
                "Remaining Estimate:", "Not Specified",
                "Time Spent:", "Not Specified",
                "Original Estimate:", "Not Specified"
        });
        assertTextSequence(new String[] {
                "HSP-11",
                "Remaining Estimate:", "30 minutes",
                "Time Spent:", "3 hours, 20 minutes",
                "Original Estimate:", "Not Specified"
        });
        assertTextSequence(new String[] {
                "HSP-10",
                "Remaining Estimate:", "1 day",
                "Time Spent:", "Not Specified",
                "Original Estimate:", "1 day"
        });
        assertTextNotPresent(" Remaining Estimate");
        assertTextNotPresent(" Time Spent");
        assertTextNotPresent(" Original Estimate");

        // test that issue with sub-tasks contains aggregate info
        goBackFromRssView();
        activateSubTasks();
        subTaskify("HSP-12", "HSP-10");
        subTaskify("HSP-11", "HSP-10");
        tester.gotoPage("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
        assertTextSequence(new String[] {
                "HSP-12",
                "Remaining Estimate:", "Not Specified",
                "Time Spent:", "Not Specified",
                "Original Estimate:", "Not Specified"
        });
        assertTextSequence(new String[] {
                "HSP-11",
                "Remaining Estimate:", "30 minutes",
                "Time Spent:", "3 hours, 20 minutes",
                "Original Estimate:", "Not Specified"
        });
        assertTextSequence(new String[] {
                "HSP-10",
                " Remaining Estimate:", "1 day, 30 minutes", "Remaining Estimate:", "1 day",
                " Time Spent:", "3 hours, 20 minutes", "Time Spent:", "Not Specified",
                " Original Estimate:", "1 day", "Original Estimate:", "1 day"
        });

        // test that no time tracking info present if time tracking disabled
        goBackFromRssView();
        deactivateTimeTracking();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
        assertTextNotPresent("Remaining Estimate");
        assertTextNotPresent("Time Spent");
        assertTextNotPresent("Original Estimate");
    }

    // JRA-16825. when redirected by view=rss pattern verify that our regex results
    // in a URL that doesn't contain "?&" which causes Tomcat to log warnings.
    // (i.e. "IssueNavigator.jspa?&reset=true" is bad)
    public void testRedirectRegex()
    {
        Pattern pattern = Pattern.compile(".*\\?&.*");
        List<String> urls = new ArrayList<String>()
        {
            {
                add("/secure/IssueNavigator.jspa?reset=true&decorator=none&view=rss");
                add("/secure/IssueNavigator.jspa?reset=true&view=rss&decorator=none");
                add("/secure/IssueNavigator.jspa?view=rss&reset=true&decorator=none");
            }
        };

        for (String url : urls)
        {
            gotoPage(url);
            final URL responseUrl = getDialog().getResponse().getURL();
            assertFalse(pattern.matcher(responseUrl.toString()).matches());
        }
    }

    public void testRssIssueFeed()
    {
        log("Issue Navigator RSS : Test that the RSS page shows all required items for an issue feed");
        tester.gotoPage("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
        try
        {
            String responseText = getDialog().getResponse().getText();

            // JRA-13821: check the content-type of the response is the standard 'application/rss+xml'
            checkRssResponseHeaders(getDialog().getResponse());

            // check that the RFC822 pubDate is being used in the RSS feed
            checkPubDatesRFC822(responseText);

            Document doc = XMLUnit.buildControlDocument(responseText);
            String xpath = "//channel[title='" + XML_TITLE + "'][contains(link,'" + getEnvironmentData().getBaseUrl() + XML_LINK_COMMENT + "')][description='" +
                           XML_DESCRIPTION_MULTIPLE + "'][language='" + XML_LANGUAGE + "']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);

            xpath = "//rss[contains(@version,'2.0')]";
            XMLAssert.assertXpathExists(xpath, doc);

            for (Item item : items)
            {
                String xPath = "//item[title='" + item.getAttribute(ATT_TITLE) + "']" +
                               "[contains(link,'" + item.getAttribute(ATT_LINK) + "')]" +
                               "[contains(guid,'" + item.getAttribute(ATT_LINK) + "')]" +
                               "[contains(author,'" + item.getAttribute(ATT_REPORTER) + "')]";
                log("Searching for existence of xpath " + xPath);
                XMLAssert.assertXpathExists(xPath, doc);
            }

        }
        catch (Exception e)
        {
            log("Failed to parse the rss for comments", e);
            fail();
        }
    }

    public void testCommentRssFeedForIssues()
    {
        log("Issue Navigator RSS: Test that the RSS page shows all required items for a comment feed");
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=&tempMax=1000");
        try
        {
            String responseText = getDialog().getResponse().getText();

            // JRA-13821: check the content-type of the response is the standard 'application/rss+xml'
            checkRssResponseHeaders(getDialog().getResponse());

            // check that the RFC822 pubDate is being used in the RSS feed
            checkPubDatesRFC822(responseText);

            Document doc = XMLUnit.buildControlDocument(responseText);
            String xpath = "//channel[title='" + XML_TITLE + "'][contains(link,'" + getEnvironmentData().getBaseUrl() + XML_LINK_COMMENT + "')][description='" +
                           XML_DESCRIPTION_MULTIPLE + "'][language='" + XML_LANGUAGE + "']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);

            for (Item item : commentItems)
            {
                String xPath = "//item[title='" + item.getAttribute("title") + "']" +
                               "[contains(link,'" + item.getAttribute("link") + "')]" +
                               "[contains(pubDate, '" + item.getAttribute("pubDate") + "')]" +
                               "[contains(description, '" + item.getAttribute("description") + "')]" +
                               "[contains(description, '" + item.getAttribute("description_link_profile") + "')]" +
                               "[not(contains(description, 'Edited by'))]" +
                               "[contains(description, '" + item.getAttribute("description_link_issue") + "')]";
                log("Searching for existence of xpath " + xPath);
                XMLAssert.assertXpathExists(xPath, doc);
            }
        }
        catch (Exception e)
        {
            log("Failed to parse the rss for comments", e);
            fail();
        }
    }

    public void testCommentRssFeedForIssuesTempMax()
    {
        // Make sure that we limit
        log("Issue Navigator RSS: Test that the RSS page shows all required items for a comment feed");
        gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?sorter/field=issuekey&sorter/order=DESC&tempMax=1");
        try
        {
            String responseText = getDialog().getResponse().getText();
            Document doc = XMLUnit.buildControlDocument(responseText);
            String xpath = "//channel[title='" + XML_TITLE + "'][contains(link,'" + getEnvironmentData().getBaseUrl() + XML_LINK_COMMENT + "')][description='" +
                           XML_DESCRIPTION_MULTIPLE + "'][language='" + XML_LANGUAGE + "']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);

            String xPathItem1 = "//item[contains(description, 'no comment')]";
            String xPathItem2 = "//item[contains(description, 'Developers, developers, developers!')]";
            String xPathItem3 = "//item[contains(description, 'This is my first comment')]";
            log("Searching for existence of xpath " + xPathItem1);
            XMLAssert.assertXpathExists(xPathItem1, doc);
            log("Searching for absence of xpath " + xPathItem2);
            XMLAssert.assertXpathNotExists(xPathItem2, doc);
            log("Searching for absence of xpath " + xPathItem3);
            XMLAssert.assertXpathNotExists(xPathItem3, doc);
        }
        catch (Exception e)
        {
            log("Failed to parse the rss for comments", e);
            fail();
        }
    }

    public void testCommentRssFeedForIssuesWithAbsoluteDateRange()
    {
        restoreData("TestIssueNavigatorRssCommentsFeed.xml");
        administration.generalConfiguration().setJiraLocale("English (UK)");
        log("Issue Navigator RSS: Test that the RSS page shows all required items for a comment feed");

        // from
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3E%3D+2006-04-12&tempMax=1000");
        // Check that only comments 2 and 4 (with a date after 12/Apr/06) are listed in the RSS. Comments 1 and 3
        // should not be listed as they were made before 12/Apr/06.
        checkComments(XML_LINK_COMMENT_AFTER_12APR, false, true, false, true);

        // to
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3C%3D+2006-04-16&tempMax=1000");
        // Check that only comments 1, 2 and 3 (with a date before 16/Apr/06) are listed in the RSS. Comment 4
        // should not be listed as it was made after 16/Apr/06.
        checkComments(XML_LINK_COMMENT_BEFORE_16APR, true, true, true, false);

        // from - to matching an issue, but no comments
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3E%3D+2006-11-23+AND+updated+%3C%3D+2006-11-25&tempMax=1000");
        // Check that no comments are displayed in the RSS.
        checkComments(XML_LINK_COMMENT_23_25NOV, false, false, false, false);


        // from-to range matching an issue with three comments, but only one comment is
        // in the RSS feed (i.e. within the from-to range)
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3E%3D+2006-04-12+AND+updated+%3C%3D+2006-04-16&tempMax=1000");
        // Check that only comment 2 (13/Apr) is shown in the RSS feed.
        checkComments(XML_LINK_COMMENT_12_16APR, false, true, false, false);
    }

    public void testCommentRssFeedForIssuesWithRelativeDateRange()
    {
        restoreData("TestIssueNavigatorRssCommentsFeed.xml");
        administration.generalConfiguration().setJiraLocale("English (UK)");

        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3E%3D+\"" + dateToRelativeString(2006, 4, 17) + "\"&tempMax=1000");
        // should not be listed as they were made before 17/Apr/06.
        checkCommentsOnly(false, false, false, true);


        // to
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3C%3D+\"" + dateToRelativeString(2006, 4, 17) + "\"&tempMax=1000");
        // Check that only comments 1, 2 and 3 (with a date before 17/Apr/06) are listed in the RSS. Comment 4
        // should not be listed as it was made after 17/Apr/06.
        checkCommentsOnly(true, true, true, false);


        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3E%3D+\"" + dateToRelativeString(2006, 11, 23) + "\"+AND+updated+%3C%3D+\"" + dateToRelativeString(2006, 11, 25) + "\"&tempMax=1000");
        // Check that no comments are displayed in the RSS.
        checkCommentsOnly(false, false, false, false);


        // from-to range matching an issue with three comments, but only one comment is
        // in the RSS feed (i.e. within the from-to range)
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/temp/SearchRequest.xml?jqlQuery=updated+%3E%3D+\"" + dateToRelativeString(2006, 4, 10) + "\"+AND+updated+%3C%3D+\"" + dateToRelativeString(2006, 4, 17) + "\"&tempMax=1000");
        // Check that only 1 comment (on 13th/Apr) is shown in the RSS feed.
        checkCommentsOnly(false, true, false, false);
    }

    /**
     * JRA-15127
     */
    public void testRssIssueFeedForIssueCountTag()
    {
        log("Issue Navigator RSS : Test that the RSS page shows correct values for the <issue> tag");

        int start = 0;
        // iterate and check some combinations of start and tempMax
        for (int tempMax = 0; tempMax < items.size() + 3; tempMax++)
        {
            // when tempMax is more than the item size, test different start issue
            if (tempMax > items.size())
            {
                start = items.size() / 2;
            }

            checkIssueCountTag(start, tempMax);
        }
    }

    public void testRssIssueFeedForXssDescription()
    {
        restoreData("TestSearchRequestViewsAndIssueViewsXss.xml");
        addHTMLtoDescription();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-rss/10000/SearchRequest-10000.xml?tempMax=1000");
        assertTextNotPresent("&trade;");
    }

    public void testRssCommentFeedForXssDescription()
    {
        restoreData("TestSearchRequestViewsAndIssueViewsXss.xml");
        addHTMLtoDescription();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-comments-rss/10000/SearchRequest-10000.xml?tempMax=1000");
        assertTextNotPresent("&trade;");
    }

    public void testXmlIssueFeedForXssDescription()
    {
        restoreData("TestSearchRequestViewsAndIssueViewsXss.xml");
        addHTMLtoDescription();
        tester.gotoPage("/sr/jira.issueviews:searchrequest-xml/10000/SearchRequest-10000.xml?tempMax=1000");
        assertTextNotPresent("&trade;");
    }

    private void addHTMLtoDescription()
    {
        // create a filter with a description with &trade; in it
        tester.gotoPage("/secure/EditFilter!default.jspa?returnUrl=ManageFilters.jspa&filterId=10000");
        tester.setFormElement("filterName", "all issues &trade;");
        tester.setFormElement("filterDescription", "&trade;");
        tester.submit("Save");
        // now show issue nav results for it
        // Click Link 'HSP Unresolved' (id='filterlink_10010').
    }

    private String dateToRelativeString(int year, int month, int date)
    {
        Calendar cal = Calendar.getInstance();
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long today = cal.getTimeInMillis();
        cal.set(year, month - 1, date);
        long days = (today - cal.getTimeInMillis()) / 86400000;
        if (days > 0)
        {
            return "-" + days + "d " + h + "h " + m + "m";
        }
        else
        {
            return (-days) + "d " + h + "h " + m + "m";
        }
    }

    private void checkComments(String link, boolean hasComment1, boolean hasComment2, boolean hasComment3, boolean hasComment4)
    {
        try
        {
            String responseText = getDialog().getResponse().getText();
            Document doc = XMLUnit.buildControlDocument(responseText);
            String xpath = "//channel[title='" + XML_TITLE + "'][contains(link,'" + link +
                           "')][description='" + XML_DESCRIPTION_MULTIPLE + "'][language='"
                           + XML_LANGUAGE + "']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);
            checkCommentsOnly(hasComment1, hasComment2, hasComment3, hasComment4);
        }
        catch (Exception e)
        {
            log("Failed to parse the rss for comments", e);
            fail();
        }
    }

    private void checkCommentsOnly(boolean hasComment1, boolean hasComment2, boolean hasComment3, boolean hasComment4)
    {
        try
        {
            String responseText = getDialog().getResponse().getText();
            Document doc = XMLUnit.buildControlDocument(responseText);
            checkXpath(hasComment1, commentItem1, doc);
            checkXpath(hasComment2, commentItem2, doc);
            checkXpath(hasComment3, commentItem3, doc);
            checkXpath(hasComment4, commentItem4, doc);
        }
        catch (Exception e)
        {
            log("Failed to parse the rss for comments", e);
            fail();
        }
    }

    private void checkComments(String[] links, boolean hasComment1, boolean hasComment2, boolean hasComment3, boolean hasComment4)
    {
        try
        {
            String responseText = getDialog().getResponse().getText();
            Document doc = XMLUnit.buildControlDocument(responseText);
            StringBuilder sb = new StringBuilder();
            sb.append("//channel[title='");
            sb.append(XML_TITLE);
            sb.append("']");
            for (String link : links)
            {
                sb.append("[contains(link,'");
                sb.append(link);
                sb.append("')]");
            }
            sb.append("[description='");
            sb.append(XML_DESCRIPTION_MULTIPLE);
            sb.append("'][language='");
            sb.append(XML_LANGUAGE);
            sb.append("']");
            String xpath = sb.toString();
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);

            checkCommentsOnly(hasComment1, hasComment2, hasComment3, hasComment4);
        }
        catch (Exception e)
        {
            log("Failed to parse the rss for comments", e);
            fail();
        }
    }

    private void checkXpath(boolean expected, Item comment, Document doc) throws TransformerException
    {
        String xPath = "//item[title='" + comment.getAttribute("title") + "']" +
                       "[contains(link,'" + comment.getAttribute("link") + "')]" +
                       "[contains(pubDate, '" + comment.getAttribute("pubDate") + "')]" +
                       "[contains(description, '" + comment.getAttribute("description") + "')]" +
                       "[contains(description, '" + comment.getAttribute("description_link_profile") + "')]" +
                       "[contains(description, '" + comment.getAttribute("description_link_issue") + "')]";
        if (expected)
        {
            log("Searching for existence of xpath " + xPath);
            XMLAssert.assertXpathExists(xPath, doc);
        }
        else
        {
            log("Searching for non-existence of xpath " + xPath);
            XMLAssert.assertXpathNotExists(xPath, doc);
        }

    }

    /**
     * This is a little trick we need to do to return to some HTML page.
     */
    private void goBackFromRssView()
    {
        gotoPage("/secure/project/ViewProjects.jspa"); // It could be any valid URL in JIRA.
    }

    private void checkRssResponseHeaders(WebResponse response)
    {
        String contentType = response.getHeaderField("content-type");
        // JRA-17367: check that headers are compatible with Outlook 2007
        assertEquals("", response.getHeaderField("Pragma"));
        assertResponseCanBeCached();
        final String cacheControl = response.getHeaderField("Cache-Control");
        assertTrue(cacheControl.indexOf("private") > -1);
        assertTrue(cacheControl.indexOf("must-revalidate") > -1);
        assertTrue(cacheControl.indexOf("max-age") > -1);
        assertTrue("RSS response should be Content-Type: application/rss+xml", contentType.indexOf("application/rss+xml") >= 0);
    }

    private void checkPubDatesRFC822(String responseText)
    {
        SimpleDateFormat rcf822Format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        int si = responseText.indexOf("<pubDate>");
        while (si != -1)
        {
            int ei = responseText.indexOf("</pubDate>", si);

            assertTrue("missing pubDate element", ei != -1);

            String dateStr = responseText.substring(si + "<pubDate>".length(), ei);
            // now parse the RFC822 date
            ParsePosition pp = new ParsePosition(0);
            Date dt = rcf822Format.parse(dateStr, pp);
            assertEquals("Rfc822 date parse failure", -1, pp.getErrorIndex());
            assertNotNull("Rfc822 date parse failure", dt);

            si = responseText.indexOf("<pubDate>", ei);
        }
    }

    // JRA-15127
    private void checkIssueCountTag(int start, int tempMax)
    {
        gotoPage("/sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?sorter/field=issuekey&sorter/order=DESC&pager/start=" + start + "&tempMax=" + tempMax);
        try
        {
            String responseText = getDialog().getResponse().getText();

            int end = Math.min(start + tempMax, items.size());
            Document doc = XMLUnit.buildControlDocument(responseText);
            String xpath = "//issue[@start='" + start + "']" +
                           "[@end='" + end + "']" +
                           "[@total='" + items.size() + "']";
            log("Searching for existence of xpath " + xpath);
            XMLAssert.assertXpathExists(xpath, doc);
        }
        catch (Exception e)
        {
            log("Failed to parse the rss for issue counts", e);
            fail();
        }
    }
}
