package com.atlassian.jira.webtests.ztests.issue.assign;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.webtests.JIRAWebTest;

import static com.atlassian.jira.permission.ProjectPermissions.ASSIGN_ISSUES;

/**
 * Enterprise only test!
 *
 * @since v3.12
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestAssignToCurrentUserFunction extends JIRAWebTest
{
    public TestAssignToCurrentUserFunction(String name)
    {
        super(name);
    }


    public void setUp()
    {
        super.setUp();
        restoreData("TestAssignToCurrentUserFunction.xml");
    }

    public void testAssignToCurrentUserWithNoAssignIssuePermission()
    {
        //Remove the assign issues permission
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("del_perm_" + ASSIGN_ISSUES.permissionKey() + "_jira-developers");
        submit("Delete");

        gotoIssue("MKY-1");
        assertTextSequence(new String[] { "Assignee:", FRED_FULLNAME, "Reporter:", ADMIN_FULLNAME });

        //lets resolve the issue which as the assignToSelf post-function set.
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");

        assertTextSequence(new String[] { "Assignee:", ADMIN_FULLNAME, "Reporter:", ADMIN_FULLNAME });
    }

    public void testAssignToCurrentUserWithNoAssignableUserPermission()
    {
        //Remove the assignabe user issues permission
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("del_perm_" + ProjectPermissions.ASSIGNABLE_USER.permissionKey() + "_jira-developers");
        submit("Delete");
        clickLink("del_perm_" + ProjectPermissions.ASSIGNABLE_USER.permissionKey() + "_jira-users");
        submit("Delete");

        gotoIssue("MKY-1");
        assertTextSequence(new String[] { "Assignee:", FRED_FULLNAME, "Reporter:", ADMIN_FULLNAME });

        //lets resolve the issue which as the assignToSelf post-function set.
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");

        //no longer have the issue summary on the resolve screen so lets go back to the issue and assert the
        //assignee hasn't changed there.
        gotoIssue("MKY-1");
        assertTextSequence(new String[] { "Assignee:", FRED_FULLNAME, "Reporter:", ADMIN_FULLNAME });
    }


    public void testAssignToCurrentUser()
    {
        gotoIssue("MKY-1");
        assertTextSequence(new String[] { "Assignee:", FRED_FULLNAME, "Reporter:", ADMIN_FULLNAME });

        //lets resolve the issue which as the assignToSelf post-function set.
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");

        assertTextSequence(new String[] { "Assignee:", ADMIN_FULLNAME, "Reporter:", ADMIN_FULLNAME });
    }
}
