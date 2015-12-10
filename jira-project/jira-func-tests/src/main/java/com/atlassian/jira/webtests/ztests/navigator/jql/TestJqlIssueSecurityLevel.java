package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
// todo enable test when we can send IssueTabeClient a different user other than admin
//@WebTest ({ Category.FUNC_TEST, Category.JQL })
//public class TestJqlIssueSecurityLevel extends AbstractJqlFuncTest
//{
//    private static final int ADMINISTRATOR_ROLE = 10002;
//
//    @Override
//    protected void setUpTest()
//    {
//        super.setUpTest();
//        administration.restoreData("TestJqlIssueSecurityLevel.xml");
//    }
//
//    public void testIssueSecurityLevel() throws Exception
//    {
//        // HSP-5 is assigned to admin but ISL = Reporter
//        assertSearchWithResults("assignee = currentUser()",
//                "HSP-7", "HSP-6", "HSP-2", "HSP-1");
//
//        navigation.login(FRED_USERNAME);
//
//        // HSP-4: only jira-dev
//        // HSP-3: only project lead
//        // HSP-2: only current assignee
//        assertSearchWithResults("", "HSP-7", "HSP-6", "HSP-5", "HSP-1");
//
//        // reassign HSP-2 to fred
//        navigation.login(ADMIN_USERNAME);
//        // can't use assignIssue() because that does validation but ISL prevents that
//        navigation.issue().viewIssue("HSP-2");
//        tester.clickLink("assign-issue");
//        tester.selectOption("assignee", FRED_FULLNAME);
//        tester.submit("Assign");
//
//        assertSearchWithResults("assignee = currentUser()",
//                "HSP-7", "HSP-6", "HSP-1");
//
//        navigation.login(FRED_USERNAME);
//        assertSearchWithResults("", "HSP-7", "HSP-6", "HSP-5", "HSP-2", "HSP-1");
//
//        backdoor.usersAndGroups().addUserToGroup(FRED_USERNAME, "jira-developers");
//        assertSearchWithResults("", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-2", "HSP-1");
//
//        navigation.login(ADMIN_USERNAME);
//        administration.project().setProjectLead("homosapien", FRED_USERNAME);
//        navigation.login(FRED_USERNAME);
//        assertSearchWithResults("", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
//    }
//
//    public void testIssueSecurityLevelClause() throws Exception
//    {
//        assertSearchWithError("level = \"admin project role\"", "The value 'admin project role' does not exist for the field 'level'.");
//        administration.roles().addProjectRoleForUser("homosapien", "Administrators", ADMIN_USERNAME);
//
//        assertSearchWithResults("level = \"current assignee\" or level is empty", "HSP-7", "HSP-6", "HSP-2", "HSP-1");
//        assertSearchWithResults("level = \"group = something\" or level is empty", "HSP-7", "HSP-6", "HSP-4", "HSP-1");
//        assertSearchWithResults("level = \"reporter\" or level is empty", "HSP-7", "HSP-6", "HSP-1");
//
//        assertSearchWithError("level = \"single user = fred\" or level is empty", "The value 'single user = fred' does not exist for the field 'level'.");
//
//        navigation.issue().viewIssue("HSP-2");
//        tester.clickLink("assign-issue");
//        tester.selectOption("assignee", FRED_FULLNAME);
//        tester.submit("Assign");
//
//        navigation.login(FRED_USERNAME);
//        assertSearchWithResults("level = \"single user = fred\"");
//        assertSearchWithError("level = \"group = something\" or level is empty", "The value 'group = something' does not exist for the field 'level'.");
//        assertSearchWithError("level = \"project lead\" or level is empty", "The value 'project lead' does not exist for the field 'level'.");
//
//        navigation.login(ADMIN_USERNAME);
//        administration.project().setProjectLead("homosapien", FRED_USERNAME);
//        navigation.login(FRED_USERNAME);
//
//        assertSearchWithResults("level = \"project lead\" or level is empty", "HSP-7", "HSP-6", "HSP-3", "HSP-1");
//        assertSearchWithResults("level = \"reporter\" or level is empty", "HSP-7", "HSP-6", "HSP-5", "HSP-1");
//        assertSearchWithResults("level = \"current assignee\" or level is empty", "HSP-7", "HSP-6", "HSP-2", "HSP-1");
//    }
//
//
//}
