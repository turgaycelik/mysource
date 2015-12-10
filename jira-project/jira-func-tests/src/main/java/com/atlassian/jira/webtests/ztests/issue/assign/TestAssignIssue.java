package com.atlassian.jira.webtests.ztests.issue.assign;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * This func test replaces the old TestAssignIssue unit test.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestAssignIssue extends FuncTestCase
{
    private String issueKey;
    private String issueKey2 = "TST-1";
    private String issueId;

    public static final String ADMIN_SELECTED_OPTION = " value=\"" + ADMIN_USERNAME + "\">";
    private static final String SEPARATOR = "---------------";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");

        if(administration.project().projectWithKeyExists("HSP"))
        {
            administration.project().deleteProject("homosapien");
        }

        administration.project().addProject("homosapien", "HSP", ADMIN_USERNAME);

        issueKey = navigation.issue().createIssue("homosapien", "Bug", "This is a test issue");
        navigation.issue().assignIssue(issueKey, "", ADMIN_FULLNAME);
        navigation.issue().setEnvironment(issueKey, "test environment");
        navigation.issue().setDescription(issueKey, "description for this is a test issue");

        issueId = navigation.issue().getId(issueKey);

        backdoor.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        administration.generalConfiguration().setAllowUnassignedIssues(true);
    }

    @Override
    protected void tearDownTest()
    {
        //if the user doesn't exist then don't clean up
        //because TestBlankInstancePlusAFewUsers.xml was loaded and it's not applicable
        if (administration.usersAndGroups().userExists(BOB_USERNAME))
        {
            administration.generalConfiguration().setAllowUnassignedIssues(false);
            backdoor.usersAndGroups().deleteUser(BOB_USERNAME);
            navigation.issue().deleteIssue(issueKey);
            administration.project().deleteProject("homosapien");
        }
        super.tearDownTest();
    }

    public void testAssignIssue()
    {
        _testUnassignedNotAvailable();
        _testUserUnassign();
        _testUserAssign();
        _testUnassignUnassignedIssueError();
        _testAlreadyAssignedError();
        _testPermissionError();
        _testReporterInList();
        _testAssignWithComment();

        // JRADEV-7741 - temp fix
//        _testDuplicateNamesInList();
    }

    public void _testUnassignedNotAvailable()
    {
        try
        {
            administration.generalConfiguration().setAllowUnassignedIssues(false);
            navigation.issue().gotoIssue(issueKey);
            tester.clickLink("assign-issue");
            tester.assertRadioOptionNotPresent("assignee", "");
        }
        finally
        {
            administration.generalConfiguration().setAllowUnassignedIssues(true);
        }
    }

    public void _testAssignWithComment()
    {
        administration.restoreData("TestBlankInstancePlusAFewUsers.xml");

        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.issue().unassignIssue(issueKey2, "comment viewable by Administrators", "Administrators");
        navigation.issue().assignIssue(issueKey2, ADMIN_FULLNAME, "comment viewable by jira-administrators", "jira-administrators");

        navigation.issue().unassignIssue(issueKey2, "comment viewable by Users", "Users");
        navigation.issue().assignIssue(issueKey2, ADMIN_FULLNAME, "comment viewable by jira-users", "jira-users");

        navigation.issue().unassignIssue(issueKey2, "comment viewable by Developers", "Developers");
        navigation.issue().assignIssue(issueKey2, ADMIN_FULLNAME, "comment viewable by jira-developers", "jira-developers");

        List<String> userComments = Lists.newArrayList("comment viewable by Users", "comment viewable by jira-users");
        List<String> developerComments = Lists.newArrayList("comment viewable by Developers", "comment viewable by jira-developers");
        List<String> adminComments = Lists.newArrayList("comment viewable by Administrators", "comment viewable by jira-administrators");

        assertions.comments(Iterables.concat(userComments, developerComments, adminComments)).areVisibleTo(ADMIN_USERNAME, issueKey2);

        assertions.comments(userComments).areVisibleTo(FRED_USERNAME, issueKey2);
        assertions.comments(Iterables.concat(developerComments, adminComments)).areNotVisibleTo(FRED_USERNAME, issueKey2);

        assertions.comments(Iterables.concat(userComments, developerComments)).areVisibleTo("devman", issueKey2);
        assertions.comments(adminComments).areNotVisibleTo("devman", issueKey2);

        assertions.comments(Iterables.concat(userComments, adminComments)).areVisibleTo("onlyadmin", issueKey2);
        assertions.comments(developerComments).areNotVisibleTo("onlyadmin", issueKey2);
    }

    public void _testUserUnassign()
    {
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.selectOption("assignee", "Unassigned");
        tester.clickButton("assign-issue-submit");
        tester.assertTextPresent("Unassigned");
    }

    public void _testUserAssign()
    {
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.selectOption("assignee", ADMIN_FULLNAME);
        tester.clickButton("assign-issue-submit");
        tester.assertTextNotPresent("Unassigned");
        tester.assertTextPresent(ADMIN_FULLNAME);
    }

    public void _testUnassignUnassignedIssueError()
    {
        // first unassigne the issue
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.selectOption("assignee", "Unassigned");
        tester.clickButton("assign-issue-submit");

        // try again we should get an error message
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.selectOption("assignee", "Unassigned");
        tester.clickButton("assign-issue-submit");
        tester.assertTextPresent("Issue already unassigned.");
    }

    public void _testAlreadyAssignedError()
    {
        _testUserAssign();

        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.selectOption("assignee", ADMIN_FULLNAME);
        tester.clickButton("assign-issue-submit");
        tester.assertTextPresent("Issue already assigned to " + ADMIN_FULLNAME + " (" + ADMIN_USERNAME + ").");
    }

    public void _testPermissionError()
    {
        try
        {
            // login as a user who does not have assign issue permission
            navigation.logout();
            navigation.login(BOB_USERNAME, BOB_PASSWORD);

            // now jump to the assignIssue screen (as if I were a bad guy)
            tester.gotoPage("/secure/AssignIssue!default.jspa?id=" + issueId);
            tester.assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
            tester.assertButtonNotPresent("assign-issue-submit");
        }
        finally
        {
            // always restore the login
            navigation.logout();
            navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        }
    }

    public void _testReporterInList()
    {
        navigation.issue().gotoIssue(issueKey);
        tester.clickLink("assign-issue");
        tester.setWorkingForm("assign-issue");

        // make certain that the reporter is both at the top and in the main list of assignee's
        text.assertTextSequence(locator.id("assignee").getHTML(), new String[] {ADMIN_SELECTED_OPTION});
    }

    public void _testDuplicateNamesInList()
    {
        administration.restoreData("TestAssigneeDuplicateNames.xml");
        try
        {
            // check that duplicate full names are appended with the username
            navigation.issue().gotoIssue(issueKey2);
            tester.clickLink("edit-issue");
            tester.assertOptionsEqual("assignee", new String[] { "Unassigned", "- Automatic -", ADMIN_FULLNAME, ADMIN_FULLNAME, "Mr Dev Man (devman)", "Mr Dev Man (devman2)" });
            tester.setFormElement("reporter", "devman");
            tester.submit("Update");

            tester.clickLink("edit-issue");
            tester.assertOptionsEqual("assignee", new String[] { "Unassigned", "- Automatic -", ADMIN_FULLNAME, "Mr Dev Man (devman)", ADMIN_FULLNAME, "Mr Dev Man (devman)", "Mr Dev Man (devman2)" });
            tester.setFormElement("reporter", "devman2");
            tester.submit("Update");

            tester.clickLink("edit-issue");
            tester.assertOptionsEqual("assignee", new String[] { "Unassigned", "- Automatic -", ADMIN_FULLNAME, "Mr Dev Man (devman2)", ADMIN_FULLNAME, "Mr Dev Man (devman)", "Mr Dev Man (devman2)" });
        }
        finally
        {
            administration.restoreData("TestBlankInstancePlusAFewUsers.xml");
        }
    }
}
