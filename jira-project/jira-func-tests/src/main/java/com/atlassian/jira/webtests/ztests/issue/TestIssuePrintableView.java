package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssuePrintableView extends JIRAWebTest
{
    public TestIssuePrintableView(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("TestTimeTrackingAggregates.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testPrintableView()
    {
        // this is a sub-task
        navigation.issue().gotoIssue("HSP-10");
        tester.clickLinkWithText("Printable");

        assertTextSequence(new String[] { "parent 2", "HSP-9", "HSP-10", "sub 3" });
        assertTextPresentBeforeText("Status:", "Open");
        assertTextPresentBeforeText("Project:", "homosapien");
        assertTextPresentBeforeText("Component/s:", "None");
        assertTextPresentBeforeText("Affects Version/s:", "None");
        assertTextPresentBeforeText("Fix Version/s:", "None");
        assertTextSequence(new String[] { "Type:", "Sub-task", "Priority:", "Major" });
        assertTextSequence(new String[] { "Reporter:", ADMIN_FULLNAME, "Assignee:", ADMIN_FULLNAME });
        assertTextSequence(new String[] { "Resolution:", "Unresolved", "Votes:", "0" });
        assertTextSequence(new String[] { "Remaining Estimate:", "1 day", "Time Spent:", "1 day", "Original Estimate:", "1 day" });
        assertTableNotPresent(" Remaining Estimate:");
        assertTableNotPresent(" Time Spent:");
        assertTableNotPresent(" Original Estimate:");
        assertTableNotPresent("Sub-Tasks:");

        // this is a parent issue
        tester.gotoPage("/browse/HSP-9");
        tester.clickLinkWithText("Printable");

        assertTextPresentBeforeText("HSP-9", "parent 2");
        assertTextPresentBeforeText("Status:", "Open");
        assertTextPresentBeforeText("Project:", "homosapien");
        assertTextPresentBeforeText("Component/s:", "None");
        assertTextPresentBeforeText("Affects Version/s:", "None");
        assertTextPresentBeforeText("Fix Version/s:", "None");
        assertTextSequence(new String[] { "Type:", "Bug", "Priority:", "Major" });
        assertTextSequence(new String[] { "Reporter:", ADMIN_FULLNAME, "Assignee:", ADMIN_FULLNAME });
        assertTextSequence(new String[] { "Resolution:", "Unresolved", "Votes:", "0" });
        assertTextSequence(new String[] {
                " Remaining Estimate:", "3 days", "Remaining Estimate:", "Not Specified",
                " Time Spent:", "1 day", "Time Spent:", "Not Specified",
                " Original Estimate:", "3 days", "Original Estimate:", "Not Specified"
        });
        assertTextSequence(new String[] { "Sub-Tasks:", "HSP-10", "sub 3", "HSP-11", "sub 4" });
    }

    /**
     * Tests for some xss hacks JSP-145410
     */
    public void testProperHtmlEscaping()
    {
        // make admin's full name contain &trade;
        navigation.userProfile().gotoCurrentUserProfile();
        tester.clickLink("edit_profile_lnk");
        tester.setFormElement("fullName", ADMIN_FULLNAME + " &trade;");
        tester.submit();

        putTradeMarkIntoIssue("HSP-10");
        putTradeMarkIntoIssue("HSP-9");

        // set up issue links to test issue link lists
        createIssueLinkType("related", "is related to", "relates to");
        linkIssueWithComment("HSP-9", "relates to", "HSP-10", null, null);

        // single issue printable
        tester.clickLinkWithText("Printable");
        assertTradeMarkIsEscaped();
        tester.gotoPage("/"); // get off the printable view

        // issue navigator printable
        navigation.issueNavigator().displayPrintableAllIssues();
        assertTradeMarkIsEscaped();
        tester.gotoPage("/"); // get off the printable view

        // issue navigator full content
        navigation.issueNavigator().displayFullContentAllIssues();
        assertTradeMarkIsEscaped();
        tester.gotoPage("/");
    }

    private void putTradeMarkIntoIssue(String issueKey)
    {
        navigation.issue().gotoIssue(issueKey);
        // edit issue and add trademark symbol &trade; entity to the summary and other non-validated text fields
        tester.clickLink("edit-issue");
        tester.setFormElement("summary", "Issue " + issueKey +" &trade;");
        tester.setFormElement("environment", "&trade;");
        tester.setFormElement("description", "&trade;");
        tester.setFormElement("comment", "&trade;");
        tester.submit("Update");
    }

    private void assertTradeMarkIsEscaped()
    {
        assertTextNotPresent("&trade;"); // should not be unescaped
        assertTextPresent("&amp;trade;"); // this is proper entity escaping
    }
}
