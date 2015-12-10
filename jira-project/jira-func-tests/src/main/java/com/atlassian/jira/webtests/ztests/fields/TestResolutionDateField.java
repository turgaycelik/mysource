package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.ISSUE_NAVIGATOR, Category.ISSUES })
public class TestResolutionDateField extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestResolutionDateField.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testViewIssuePage()
    {
        //check HSP-1 doesn't show the resolution date
        navigation.issue().viewIssue("HSP-1");
        Locator locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextNotPresent(locator, "Resolved");

        //check HSP-2 shows the resolution date
        navigation.issue().viewIssue("HSP-2");
        locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "Second test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Resolved");
    }

    public void testResolveIssueUpdatesDate()
    {
        //check HSP-1 doesn't show the resolution date
        navigation.issue().viewIssue("HSP-1");
        Locator locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextNotPresent(locator, "Resolved");

        // resolve the issue
        tester.clickLink("action_id_5");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //now check the issue shows the resolved date.
        locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextPresent(locator, "Resolved");

        //also check the navigator columns:
        navigation.issueNavigator().displayAllIssues();
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 2, 1), "HSP-1");
        //can't really assert today's date exactly here because of timezone issues with the different builds etc...
        final String resolvedCell = tester.getDialog().getWebTableBySummaryOrId("issuetable").getCellAsText(2, 11).trim();
        assertTrue(resolvedCell.length() > 0);

        //now re-open the issue to ensure that clears the resolution date
        navigation.issue().viewIssue("HSP-1");
        tester.clickLink("action_id_3");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //check resolved is no longer shown
        locator = new XPathLocator(tester, "//header//h1");
        text.assertTextPresent(locator, "First test issue");

        locator = new IdLocator(tester, "status-val");
        text.assertTextNotPresent(locator, "Resolved");

        //also check the issue navigator no longer shows it
        //also check the navigator columns:
        navigation.issueNavigator().displayAllIssues();
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 2, 1), "HSP-1");
        assertTableCellEmpty("issuetable", 2, 11);
    }

    public void testIssueNavigatorColumns()
    {
        //show all issues
        navigation.issueNavigator().displayAllIssues();

        //check that HSP-2 displays a Resolved date, and HSP-1 doesn't.
        
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 0, 11), "Resolved");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 1, 1), "HSP-2");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 1, 11), "14/Oct/08");
        text.assertTextPresent(new TableCellLocator(tester, "issuetable", 2, 1), "HSP-1");
        assertTableCellEmpty("issuetable", 2, 11);
    }

    public void testIssueViews()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLinkWithText("Printable");
        tester.assertTextNotPresent("Resolved:");

        navigation.issue().viewIssue("HSP-2");
        tester.clickLinkWithText("Printable");
        tester.assertTextPresent("Resolved:");

        //word view uses the same template as the printable view so no need to test it.

        //Xml view is tested in TestXmlIssueView
    }

    private void assertTableCellEmpty(String tableId, int row, int col)
    {
        final String resolvedCell = tester.getDialog().getWebTableBySummaryOrId(tableId).getCellAsText(row, col).trim();
        assertTrue(resolvedCell.length() == 0);
    }
}
