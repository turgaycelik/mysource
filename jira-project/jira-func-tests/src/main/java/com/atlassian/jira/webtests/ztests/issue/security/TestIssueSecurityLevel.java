package com.atlassian.jira.webtests.ztests.issue.security;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import net.sourceforge.jwebunit.ExpectedRow;
import net.sourceforge.jwebunit.ExpectedTable;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Note that Issue Security Levels are only available in the Enterprise Edition.
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING, Category.SECURITY, Category.ISSUES })
public class TestIssueSecurityLevel extends FuncTestCase
{
    protected void setUpTest()
    {
        this.administration.restoreData("TestIssueSecurityLevel.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "bill");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "gandhi");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "joe");
    }

    public void testIssueNavigatorVisibility()
    {
        this.navigation.login("bill");

        // Bill is the Cow project Lead, is in the groups "Cowboys", and "jira-users", and the project role users (for Cow)
        // let Bill view "all" issues.
        navigation.issueNavigator().displayAllIssues();

        // Assert the table 'issuetable'
        final ExpectedTable expectedTable = new ExpectedTable();
        expectedTable.appendRow(new ExpectedRow(new String[] {"T", "Key", "Summary", "Assignee", "Reporter", "P", "Status", "Resolution", "Created", "Updated", "Due", ""}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "PIG-15", "Visible to All Project Leads", "Mahatma Gandhi", "Mark", "", "Open", "Unresolved", "20/Jan/11", "20/Jan/11", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-47", "Cowboys and Admins", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-46", "Any User (project role)", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-44", "Friendly Users - Bill included", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-41", "Friendly Group - Cowboy Group", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-40", "Security Level Project Lead", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-39", "Security Level Bill User", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-37", "Issue Assigned to Bill", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-36", "Issue added by Bill.", "Henry Ford", "Wild Bill Hickock", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-35", "Milk the jersey", "Henry Ford", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-34", "Get along little doggy", "Henry Ford", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "","Actions"}));
        tester.assertTableEquals("issuetable", expectedTable);

        // Redundant - but test for specific issues not being visible:
        // COW-38 "Assignee and Admins Only" and this issue is NOT assigned to bill.
        tester.assertTextNotPresent("COW-38");
        tester.assertTextNotPresent("Issue not assigned to Bill");
        // COW-42   	 Friendly Group, Bill not in "Friend Group" custom field.
        tester.assertTextNotPresent("COW-42");
        tester.assertTextNotPresent("Friendly Group - No Group");
        // COW-43   	 Friendly Group - Bill not in "Friend Group" custom field.
        tester.assertTextNotPresent("COW-43");
        tester.assertTextNotPresent("Friendly Group - jira-developers Group");
        // COW-45   	 "Friendly Users": Bill is not in the Custom field
        tester.assertTextNotPresent("COW-45");
        tester.assertTextNotPresent("Friendly Users - Bill not included");
        // COW-48 "Reporter and Developers Only" : Bill didn't report this.
        tester.assertTextNotPresent("COW-48");
        tester.assertTextNotPresent("Reporters and Developers - not reported by Bill");
    }

    public void testBrowseIssueVisibility()
    {
        this.navigation.login("bill");

        // Bill is the Cow project Lead, is in the groups "Cowboys", and "jira-users", and the project role users (for Cow)
        // let Bill view "all" issues.

        //    COW-34   	No Security Level
        navigation.issue().viewIssue("COW-34");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-35 	No Security Level
        navigation.issue().viewIssue("COW-35");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-36 	Reporter and Developers Only
        navigation.issue().viewIssue("COW-36");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-37 	Assignee and Admins Only
        navigation.issue().viewIssue("COW-37");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-39 	Bill User
        navigation.issue().viewIssue("COW-39");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-40 	Project Lead
        navigation.issue().viewIssue("COW-40");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-41 	Friendly Group
        navigation.issue().viewIssue("COW-41");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-44 	Friendly Users
        navigation.issue().viewIssue("COW-44");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-46 	Any User
        navigation.issue().viewIssue("COW-46");
        tester.assertTextNotPresent("Permission Violation");
        //    COW-47 	Cowboys and Admins
        navigation.issue().viewIssue("COW-47");
        tester.assertTextNotPresent("Permission Violation");

        // The following should not be visible:
        // COW-38 "Assignee and Admins Only" and this issue is NOT assigned to bill.
        navigation.issue().viewIssue("COW-38");
        tester.assertTextPresent("Permission Violation");
        // COW-42   	 Friendly Group, Bill not in "Friend Group" custom field.
        navigation.issue().viewIssue("COW-42");
        tester.assertTextPresent("Permission Violation");
        // COW-43   	 Friendly Group - Bill not in "Friend Group" custom field.
        navigation.issue().viewIssue("COW-43");
        tester.assertTextPresent("Permission Violation");
        // COW-45   	 "Friendly Users": Bill is not in the Custom field
        navigation.issue().viewIssue("COW-45");
        tester.assertTextPresent("Permission Violation");
        // COW-48 "Reporter and Developers Only" : Bill didn't report this.
        navigation.issue().viewIssue("COW-48");
        tester.assertTextPresent("Permission Violation");
    }

    /**
     * Tests that the user can only see issues he has permission for in Browse Issue.
     */
    public void testIssueNavigatorVisibility_User_Gandhi()
    {
        // log in as gandhi
        this.navigation.login("gandhi");

        // gandhi is the Rattus Project Lead (not Cow), is in the groups "jira-developers", and "jira-users", and the project role users, and developers (for Cow)
        // let gandhi view "all" issues.
        navigation.issueNavigator().displayAllIssues();
        // Assert the table 'issuetable'
        final ExpectedTable expectedTable = new ExpectedTable();
        expectedTable.appendRow(new ExpectedRow(new String[] {"T", "Key", "Summary", "Assignee", "Reporter", "P", "Status", "Resolution", "Created", "Updated", "Due", ""}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "PIG-15", "Visible to All Project Leads", "Mahatma Gandhi", "Mark", "", "Open", "Unresolved", "20/Jan/11", "20/Jan/11", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-48", "Reporters and Developers - not reported by Bill", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-46", "Any User (project role)", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-45", "Friendly Users - Bill not included", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-44", "Friendly Users - Bill included", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-43", "Friendly Group - jira-developers Group", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-36", "Issue added by Bill.", "Henry Ford", "Wild Bill Hickock", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-35", "Milk the jersey", "Henry Ford", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-34", "Get along little doggy", "Henry Ford", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        tester.assertTableEquals("issuetable", expectedTable);

        // Redundant - but test for specific issues not being visible:
        // COW-37 "Assignee and Admins Only" and this issue is NOT assigned to gandhi.
        tester.assertTextNotPresent("COW-37");
        tester.assertTextNotPresent("Issue assigned to Bill");
        // COW-38 "Assignee and Admins Only" and this issue is NOT assigned to gandhi.
        tester.assertTextNotPresent("COW-38");
        tester.assertTextNotPresent("Issue not assigned to Bill");
        // COW-39 only user "bill"
        tester.assertTextNotPresent("COW-39");
        tester.assertTextNotPresent("Security Level Bill User");
        // COW-40 Project Lead for Cow
        tester.assertTextNotPresent("COW-40");
        tester.assertTextNotPresent("Security Level Project Leadl");
        // COW-41 Friendly Group, gandhi not in "Friend Group" custom field.
        tester.assertTextNotPresent("COW-41");
        tester.assertTextNotPresent("Friendly Group - Cowboy Group");
        // COW-42   	 Friendly Group, gandhi not in "Friend Group" custom field.
        tester.assertTextNotPresent("COW-42");
        tester.assertTextNotPresent("Friendly Group - No Group");
        // COW-47 "Cowboys and Admins" : gandhi didn't report this.
        tester.assertTextNotPresent("COW-47");
        tester.assertTextNotPresent("Cowboys and Admins");
    }

    public void testProjectLeadVisibility() throws Exception
    {
        this.navigation.login("joe");

        // Joe is the project lead for LEO
        navigation.issueNavigator().displayAllIssues();

        // Assert the table 'issuetable'
        final ExpectedTable expectedTable = new ExpectedTable();
        expectedTable.appendRow(new ExpectedRow(new String[] {"T", "Key", "Summary", "Assignee", "Reporter", "P", "Status", "Resolution", "Created", "Updated", "Due", ""}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "PIG-15", "Visible to All Project Leads", "Mahatma Gandhi", "Mark", "", "Open", "Unresolved", "20/Jan/11", "20/Jan/11", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "LEO-1", "Only Visible to Joe", "Murray", "Mark", "", "Open", "Unresolved", "20/Jan/11", "20/Jan/11", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-46", "Any User (project role)", "Wild Bill Hickock", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-35", "Milk the jersey", "Henry Ford", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        expectedTable.appendRow(new ExpectedRow(new String[] {"", "COW-34", "Get along little doggy", "Henry Ford", "Mark", "", "Open", "Unresolved", "08/Jul/08", "08/Jul/08", "", "Actions"}));
        tester.assertTableEquals("issuetable", expectedTable);
    }
}
