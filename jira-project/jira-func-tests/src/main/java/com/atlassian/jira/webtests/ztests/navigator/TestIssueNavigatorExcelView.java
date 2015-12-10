/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractTestIssueNavigatorColumnsView;
import com.meterware.httpunit.WebResponseUtil;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigatorExcelView extends AbstractTestIssueNavigatorColumnsView
{
    public TestIssueNavigatorExcelView(String name)
    {
        super(name);
    }

    public void testAllExcelViewHaveCorrectLinkToTheFilter()
    {
        _testExcelViewHaveCorrectLinkToTheFilter("currentExcelFields");
    }

    public void _testExcelViewHaveCorrectLinkToTheFilter(String excelView)
    {
        log("Issue Navigator: Test that the " + excelView + " views show correct link to the search filter");

        String filterName = "rssview";
        String filterId = backdoor.filters().createFilter("issuetype=\"New Feature\"", filterName);


        // goto the rss Issues view
        tester.gotoPage("/sr/jira.issueviews:searchrequest-excel-all-fields/" + filterId + "/SearchRequest-" + filterId + ".xls?tempMax=1000");

        try
        {
            //need to change the content type to "text/html" so that HTTPUnit understands the response.
            //This can only be done from within the com.meterware.httpunit package and we therefore use the
            //WebResponseUtil class.
            if (!WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html"))
            {
                log("Failed to replace response content type with 'text/html'");
                fail();
            }
            else
            {
                //check that the link to the filter is correct
                String expectedFilterUrl = getEnvironmentData().getBaseUrl().toString() + "/secure/IssueNavigator.jspa?requestId=" + filterId;
                String expectedLink = expectedFilterUrl + "\">" + filterName;
                assertTextPresent(expectedLink);
            }
        }
        catch (Exception e)
        {
            log("Failed to parse the printable view", e);
            fail();
        }
    }

    public void testAllColumnsExcelView()
    {
        log("Issue Navigator: Test that the excel (All fields) view shows all required items");
        goToExcelViewForAllIssues();
        try
        {
            //need to change the content type to "text/html" so that HTTPUnit understands the response.
            //This can only be done from within the com.meterware.httpunit package and we therefore use the
            //WebResponseUtil class.
            if (!WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html"))
            {
                log("Failed to replace response content type with 'text/html'");
                fail();
            }
            else
            {
                //Check that the issue table contains the correct fields for each issue.
                WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");

                for (Object itemObject : items)
                {
                    Item item = (Item) itemObject;
                    new ExcelItemVerifier(this, item, issueTable, getEnvironmentData().getBaseUrl()).verify();
                }
            }
        }
        catch (Exception e)
        {
            log("Failed to parse the printable view", e);
            fail();
        }
    }

    private void goToExcelViewForAllIssues()
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-excel-all-fields/temp/SearchRequest.xls?jqlQuery=&tempMax=1000");
    }

    public void testExcelFilenameWithNonAsciiCharacters()
    {
        final String encodedFilename = "%D0%B5%D1%81%D1%82%D0%B8%D1%80%D0%BE%D0%B2%D0%B0%D0%BD%D0%B8%D1%8F%20%28jWebTest%20JIRA%20installation%29.xls";
        final String oldUserAgent = getDialog().getWebClient().getClientProperties().getUserAgent();

        try
        {
            log("Issue Navigator: Test that the excel view generates the correct filename when the search request has non-ASCII characters");
            administration.restoreData("TestSearchRequestViewNonAsciiSearchName.xml");

            // first test "IE"
            tester.getDialog().getWebClient().getClientProperties().setUserAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)");
            tester.gotoPage("/sr/jira.issueviews:searchrequest-excel-all-fields/10000/SearchRequest-10000.xls?tempMax=1000");
            String contentDisposition = tester.getDialog().getResponse().getHeaderField("content-disposition");
            assertFalse(contentDisposition.indexOf("filename*=UTF-8''" + encodedFilename) >= 0);
            assertTrue("Expected the content disposition to contain '" + encodedFilename + "' but got '" + contentDisposition + "'!", contentDisposition.indexOf("filename=\"" + encodedFilename + "\"") >= 0);
            gotoPage("/sr/jira.issueviews:searchrequest-excel-current-fields/10000/SearchRequest-10000.xls?tempMax=1000");
            contentDisposition = tester.getDialog().getResponse().getHeaderField("content-disposition");
            assertFalse(contentDisposition.indexOf("filename*=UTF-8''" + encodedFilename) >= 0);
            assertTrue(contentDisposition.indexOf("filename=\"" + encodedFilename + "\"") >= 0);

            // next test "Mozilla"
            tester.getDialog().getWebClient().getClientProperties().setUserAgent("Mozilla/5.001 (windows; U; NT4.0; en-US; rv:1.0) Gecko/25250101");
            tester.gotoPage("/sr/jira.issueviews:searchrequest-excel-all-fields/10000/SearchRequest-10000.xls?tempMax=1000");
            contentDisposition = tester.getDialog().getResponse().getHeaderField("content-disposition");
            assertTrue(contentDisposition.indexOf("filename*=UTF-8''" + encodedFilename) >= 0);
            assertFalse(contentDisposition.indexOf("filename=\"" + encodedFilename + "\"") >= 0);
            tester.gotoPage("/sr/jira.issueviews:searchrequest-excel-current-fields/10000/SearchRequest-10000.xls?tempMax=1000");
            contentDisposition = tester.getDialog().getResponse().getHeaderField("content-disposition");
            assertTrue(contentDisposition.indexOf("filename*=UTF-8''" + encodedFilename) >= 0);
            assertFalse(contentDisposition.indexOf("filename=\"" + encodedFilename + "\"") >= 0);
        }
        finally
        {
            // restore old user agent
            tester.getDialog().getWebClient().getClientProperties().setUserAgent(oldUserAgent);
        }
    }

    public void testExcelViewCanBeCached()
    {
        log("Issue Navigator: Test that the excel view does not contain the Cache-control: no-cache header");
        goToExcelViewForAllIssues();

        //need to change the content type to "text/html" so that HTTPUnit understands the response.
        //This can only be done from within the com.meterware.httpunit package and we therefore use the
        //WebResponseUtil class.
        if (!WebResponseUtil.replaceResponseContentType(getDialog().getResponse(), "text/html"))
        {
            log("Failed to replace response content type with 'text/html'");
            fail();
        }
        else
        {
            // JRA-14030: Make sure the Excel response does not set Cache-control: no-cache headers
            assertResponseCanBeCached();
        }
    }

    public void testTempMax() throws SAXException, IOException
    {
        // Test that a tempMax of 1 will force the view to only return one record
        gotoPage("/sr/jira.issueviews:searchrequest-excel-current-fields/temp/SearchRequest.xls?pid=10000&sorter/field=issuekey&sorter/order=DESC&tempMax=1&noResponseHeaders=true");
        WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");

        // ensure that our description says we are only showing one issue
        assertTextSequence(new String[] { "Displaying", "1", "issues at" });

        assertEquals(2, issueTable.getRowCount());
    }

    public void testPagerStartParam() throws SAXException
    {
        // Check to make sure there are a total of 3 record plus a row for the header
        gotoPage("/sr/jira.issueviews:searchrequest-excel-current-fields/temp/SearchRequest.xls?pid=10000&sorter/field=issuekey&sorter/order=DESC&noResponseHeaders=true");
        WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");
        assertEquals(4, issueTable.getRowCount());

        // If we specifiy the start to be one before the end then we should only get one record
        gotoPage("/sr/jira.issueviews:searchrequest-excel-current-fields/temp/SearchRequest.xls?pid=10000&sorter/field=issuekey&sorter/order=DESC&noResponseHeaders=true&pager/start=2");
        issueTable = getDialog().getResponse().getTableWithID("issuetable");
        assertEquals(2, issueTable.getRowCount());
    }

    public void testGroupCustomFieldEscaped() throws Exception
    {
        // JRA-14759: need to ensure Group custom fields are properly escaped in excel view as well as regular views
        restoreData("TestXssCustomFields.xml");
        gotoPage("/sr/jira.issueviews:searchrequest-excel-all-fields/10000/SearchRequest-10000.xls?tempMax=1000");
        assertEquals("application/vnd.ms-excel",getDialog().getResponse().getContentType());
        String text = getDialog().getResponse().getText();
        assertTrue(text.indexOf("&lt;xxx&gt;delta&lt;/xxx&gt;") >= 0);
        assertFalse(text.indexOf("<xxx>delta</xxx>") >= 0);
    }

    protected void initFieldColumnMap()
    {
        issueFieldColumnMap.add(ISSUE_PROJECT);
        issueFieldColumnMap.add(ISSUE_KEY);
        issueFieldColumnMap.add(ISSUE_SUMMARY);
        issueFieldColumnMap.add(ISSUE_TYPE);
        issueFieldColumnMap.add(ISSUE_STATUS);
        issueFieldColumnMap.add(ISSUE_PRIORITY);
        issueFieldColumnMap.add(ISSUE_RESOLUTION);
        issueFieldColumnMap.add(ISSUE_ASSIGNEE);
        issueFieldColumnMap.add(ISSUE_REPORTER);
        issueFieldColumnMap.add(ISSUE_CREATOR);
        issueFieldColumnMap.add(ISSUE_CREATED);
        issueFieldColumnMap.add(ISSUE_LAST_VIEWED);
        issueFieldColumnMap.add(ISSUE_UPDATED);
        issueFieldColumnMap.add(ISSUE_RESOLVED);
        issueFieldColumnMap.add(ISSUE_AFFECTS_VERSIONS);
        issueFieldColumnMap.add(ISSUE_FIX_VERSIONS);
        issueFieldColumnMap.add(ISSUE_COMPONENTS);
        issueFieldColumnMap.add(ISSUE_DUE);
        issueFieldColumnMap.add(ISSUE_VOTES);
        issueFieldColumnMap.add(ISSUE_WATCHES);
        issueFieldColumnMap.add(ISSUE_IMAGES);
        issueFieldColumnMap.add(ISSUE_ORIGINAL_ESTIMATE);
        issueFieldColumnMap.add(ISSUE_REMAINING_ESTIMATE);
        issueFieldColumnMap.add(ISSUE_TIME_SPENT);
        issueFieldColumnMap.add(ISSUE_WORK_RATIO);
        issueFieldColumnMap.add(ISSUE_LINKS);
        issueFieldColumnMap.add(ISSUE_ENVIRONMENT);
        issueFieldColumnMap.add(ISSUE_DESCRIPTION);
        issueFieldColumnMap.add(ISSUE_SECURITY_LEVEL);
        issueFieldColumnMap.add(ISSUE_PROGRESS);
        issueFieldColumnMap.add(ISSUE_LABELS);
        issueFieldColumnMap.add(ISSUE_CASCADING_SELECT_FIELD);
        issueFieldColumnMap.add(ISSUE_DATE_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_DATE_TIME_FIELD);
        issueFieldColumnMap.add(ISSUE_FREE_TEXT_FIELD);
        issueFieldColumnMap.add(ISSUE_GROUP_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_IMPORT_ID_FIELD);
        issueFieldColumnMap.add(ISSUE_MULTI_CHECKBOXES_FIELD);
        issueFieldColumnMap.add(ISSUE_MULTI_GROUP_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_MULTI_SELECT_FIELD);
        issueFieldColumnMap.add(ISSUE_MULTI_USER_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_NUMBER_FIELD);
        issueFieldColumnMap.add(ISSUE_PROJECT_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_ROTEXT_FIELD);
        issueFieldColumnMap.add(ISSUE_RADIO_BUTTONS_FIELD);
        issueFieldColumnMap.add(ISSUE_SELECT_LIST);
        issueFieldColumnMap.add(ISSUE_SINGLE_VERSION_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_TEXT_FIELD255);
        issueFieldColumnMap.add(ISSUE_URL_FIELD);
        issueFieldColumnMap.add(ISSUE_USER_PICKER_FIELD);
        issueFieldColumnMap.add(ISSUE_VERSION_PICKER_FIELD);
    }

    protected class ExcelItemVerifier extends AbstractTestIssueNavigatorColumnsView.ItemVerifier
    {

        public ExcelItemVerifier(AbstractTestIssueNavigatorColumnsView test, Item item, WebTable table, URL baseUrl)
        {
            super(test, item, table, baseUrl);
        }

        /**
         * NOTE: Order of verifications is irrelevant
         */
        public void verify()
        {
            String key = item.getAttribute(ATT_KEY);
            test.log("Checking item [" + key + "] on row [" + row + "]");

            //this should have the same link as the key column.
            final String issueLink = baseUrl + "/browse/" + key;

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_TYPE, ATT_TYPE);

            verifyLinkExists(ISSUE_KEY, issueLink);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_SUMMARY, ATT_SUMMARY);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_ASSIGNEE, ATT_ASSIGNEE);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_REPORTER, ATT_REPORTER);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_CREATOR, ATT_CREATOR);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_PRIORITY, ATT_PRIORITY);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_STATUS, ATT_STATUS);

            final String resolution = item.getAttribute(ATT_RESOLUTION);
            verifyCellValueEmptyOrEquals(ISSUE_RESOLUTION, resolution.equals("Unresolved") ? "Unresolved" : resolution);

            // For the following three dates, we are only testing that the cells are not empty.
            // Should perhaps add more detail to the test Items in the future to contain the date details
            verifyCellIssueAttributeEmptyOrEquals(ISSUE_CREATED, ATT_DATE_CREATED);

            verifyCellIssueAttributeEmptyOrEquals(LAST_VIEWED, ATT_LAST_VIEWED);
            verifyCellIssueAttributeEmptyOrEquals(ISSUE_UPDATED, ATT_DATE_UPDATED);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_DUE, ATT_DATE_DUE);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_RESOLVED, ATT_DATE_RESOLVED);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_AFFECTS_VERSIONS, ATT_VERSION);

            verifyCustomFieldDisplayValues(ISSUE_CASCADING_SELECT_FIELD, CF_CASCADING_SELECT_FIELD);

            for (String component : item.getComponents())
            {
                verifyCellValueEmptyOrEquals(ISSUE_COMPONENTS, component);
            }

            verifyCustomFieldDisplayValues(ISSUE_DATE_PICKER_FIELD, CF_DATE_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_DATE_TIME_FIELD, CF_DATE_TIME_FIELD);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_DESCRIPTION, ATT_DESCRIPTION);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_SECURITY_LEVEL, ATT_SECURITY_LEVEL);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_ENVIRONMENT, ATT_ENVIRONMENT);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_FIX_VERSIONS, ATT_FIX_VERSION);

            verifyCustomFieldDisplayValues(ISSUE_FREE_TEXT_FIELD, CF_FREE_TEXT_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_GROUP_PICKER_FIELD, CF_GROUP_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_IMPORT_ID_FIELD, CF_IMPORT_ID_FIELD);

            //Links
            final IssueLinks links = item.getLinks();
            for (final Object o1 : links.getInLinks())
            {
                IssueLink link = (IssueLink) o1;
                verifyCellValueEmptyOrEquals(ISSUE_LINKS, link.getLink());
            }
            for (final Object o : links.getOutLinks())
            {
                IssueLink link = (IssueLink) o;
                verifyCellValueEmptyOrEquals(ISSUE_LINKS, link.getLink());
            }

            // TODO: Extend tests to cover sub tasks as well?

            verifyCustomFieldDisplayValues(ISSUE_MULTI_CHECKBOXES_FIELD, CF_MULTI_CHECKBOXES_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_MULTI_GROUP_PICKER_FIELD, CF_MULTI_GROUP_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_MULTI_SELECT_FIELD, CF_MULTI_SELECT_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_MULTI_USER_PICKER_FIELD, CF_MULTI_USER_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_NUMBER_FIELD, CF_NUMBER_FIELD);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_ORIGINAL_ESTIMATE, ATT_TIMEORIGINALESTIMATE);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_PROJECT, ATT_PROJECT);

            verifyCustomFieldDisplayValues(ISSUE_PROJECT_PICKER_FIELD, CF_PROJECT_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_ROTEXT_FIELD, CF_RO_TEXT_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_RADIO_BUTTONS_FIELD, CF_RADIO_BUTTONS_FIELD);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_REMAINING_ESTIMATE, ATT_REMAINING_ESTIMATE);

            verifyCustomFieldDisplayValues(ISSUE_SELECT_LIST, CF_SELECT_LIST);

            verifyCustomFieldDisplayValues(ISSUE_SINGLE_VERSION_PICKER_FIELD, CF_SINGLE_VERSION_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_TEXT_FIELD255, CF_TEXT_FIELD255);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_TIME_SPENT, ATT_TIMESPENT);

            verifyCustomFieldDisplayValues(ISSUE_URL_FIELD, CF_URLFIELD);
            verifyCustomFieldLinks(ISSUE_URL_FIELD, CF_URLFIELD);

            verifyCustomFieldDisplayValues(ISSUE_USER_PICKER_FIELD, CF_USER_PICKER_FIELD);

            verifyCustomFieldDisplayValues(ISSUE_VERSION_PICKER_FIELD, CF_VERSION_PICKER_FIELD);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_VOTES, ATT_VOTES);

            verifyCellIssueAttributeEmptyOrEquals(ISSUE_WORK_RATIO, ATT_WORK_RATIO);

        }
    }

    protected Item createItem1()
    {
        final Item item = super.createItem1();
        item.setAttribute(ATT_LAST_VIEWED, null);
        return item;
    }
    protected Item createItem2()
    {
        final Item item = super.createItem2();
        //time estimates are in seconds in excel
        item.setAttribute(ATT_REMAINING_ESTIMATE, "1800");
        item.setAttribute(ATT_TIMESPENT, "12000");
        item.setAttribute(ATT_LAST_VIEWED, null);
        return item;
    }

    protected Item createItem3()
    {
        final Item item = super.createItem3();
        //time estimates are in seconds in excel
        item.setAttribute(ATT_TIMEORIGINALESTIMATE, "86400");
        item.setAttribute(ATT_REMAINING_ESTIMATE, "86400");
        item.setAttribute(ATT_LAST_VIEWED, null);

        return item;
    }
}
