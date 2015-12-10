package com.atlassian.jira.webtests.ztests.navigator;

import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractTestIssueNavigatorColumnsView;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.util.Arrays;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestIssueNavigatorPrintableView extends AbstractTestIssueNavigatorColumnsView
{
    public TestIssueNavigatorPrintableView(String name)
    {
        super(name);
    }

    public void testTimeTrackingPrintableView() throws SAXException
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        activateSubTasks();
        issueTableClient.getIssueTable(""); // create session
        backdoor.columnControl().addLoggedInUserColumns(Arrays.asList(
                "aggregatetimeoriginalestimate", "aggregateprogress", "aggregatetimeestimate", "aggregatetimespent"));

        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?jqlQuery=&tempMax=1000");

        //Check that the issue table contains the correct fields for each issue.
        WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");

        tableCellHasText(issueTable, 0, 44, "\u03A3 Original Estimate");
        tableCellHasText(issueTable, 0, 45, "\u03A3 Remaining Estimate");
        tableCellHasText(issueTable, 0, 46, "\u03A3 Time Spent");
        tableCellHasText(issueTable, 0, 47, "\u03A3 Progress");

        tableCellHasText(issueTable, 2, 45, "30 minutes");
        tableCellHasText(issueTable, 2, 46, "3 hours, 20 minutes");
        tableCellHasText(issueTable, 2, 47, "86%");

        tableCellHasText(issueTable, 3, 44, "1 day");
        tableCellHasText(issueTable, 3, 45, "1 day");
        tableCellHasText(issueTable, 3, 47, "0%");
    }

    public void testAllColumnsPrintableView()
    {
        log("Issue Navigator: Test that the printable view shows all required items");
        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?jqlQuery=&tempMax=1000");
        try
        {
            //Check that the issue table contains the correct fields for each issue.
            WebTable issueTable = getDialog().getResponse().getTableWithID("issuetable");

            for (Item item : items)
            {
                new ItemVerifier(this, item, issueTable, getEnvironmentData().getBaseUrl()).verify();
            }
        }
        catch (Exception e)
        {
            log("Failed to parse the printable view", e);
            fail();
        }
    }

    //JRA-15984
    public void testPrintableViewXSSBug()
    {
        String filter = backdoor.filters().createFilter("", "<script>alert('evil');</script>");
        //now view the printable view and esure the name is encoded properly.
        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/" + filter + "/SearchRequest-" + filter + ".html?tempMax=1000");
        assertTextPresent("&lt;script&gt;alert(&#39;evil&#39;);&lt;/script&gt; (Your Company JIRA)");
    }

    public void testAllColumnsPrintableViewDaysFormat()
    {
        reconfigureTimetracking(FORMAT_DAYS);
        testAllColumnsPrintableView();
    }

    public void testAllColumnsPrintableViewHoursFormat()
    {
        reconfigureTimetracking(FORMAT_HOURS);
        testAllColumnsPrintableView();
    }

    public void testSearchRequestHeaderSummaryDisplaysCorrectTotals()
    {
        log("Issue Navigator: Test that the printable view shows all required items");
        gotoPage("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?sorter/field=issuekey&sorter/order=DESC&tempMax=2");

        assertTextPresent("<b>1</b>&ndash;<b>2</b> of <b>3</b>");

        gotoPage("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?sorter/field=issuekey&sorter/order=DESC");

        assertTextPresent("<b>1</b>&ndash;<b>3</b> of <b>3</b>");
    }

    public void testSearchRequestSummaryWithSimpleAndAdvancedQuery() throws Exception
    {
        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?jqlQuery=status%20=%20'In Progress'&tempMax=1000");

        // make our assertions about Simple query summary
        CssLocator summaryLocator = new CssLocator(tester, ".result-header");
        text.assertTextSequence(summaryLocator, "Status", "In Progress");
        text.assertTextNotPresent(summaryLocator, "JQL Query");
        text.assertTextNotPresent(summaryLocator, "status = 'In Progress'");


        tester.gotoPage("/sr/jira.issueviews:searchrequest-printable/temp/SearchRequest.html?jqlQuery=status%20!=%20'In Progress'&tempMax=1000");

        // make our assertions about Advanced query summary
        summaryLocator = new CssLocator(tester, ".result-header");
        text.assertTextSequence(summaryLocator, "JQL Query", "status != 'In Progress'");
        text.assertTextNotPresent(summaryLocator, "Status");
    }
}
