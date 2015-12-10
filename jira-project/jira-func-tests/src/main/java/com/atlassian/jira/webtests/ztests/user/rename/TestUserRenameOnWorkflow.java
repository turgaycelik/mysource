package com.atlassian.jira.webtests.ztests.user.rename;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v6.0
 */
@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS, Category.RENAME_USER, Category.ISSUES, Category.ISSUE_NAVIGATOR})
public class TestUserRenameOnWorkflow extends FuncTestCase
{

    public static final String DAIRY_COMPONENT_ID = "10000";
    public static final String BEEF_COMPONENT_ID = "10001";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        administration.restoreData("user_rename_post_functions.xml");
        //    KEY       USERNAME    NAME
        //    bb	    betty	    Betty Boop
        //    ID10001	bb	        Bob Belcher
        //    cc	    cat	        Crazy Cat
        //    ID10101	cc	        Candy Chaos

        // COMPONENT    LEAD
        // Beef         ID10001|bb
        // Dairy        bb|betty

        // COW Project Lead is ID10001|bb|Bob Belcher

        //  POST FUNCTIONS:
        //  start progress    set assignee to bb|betty (only if current user is assignee)
        //  resolve           set assignee to ID10001|bb
        //  reopen            assign to lead
        //  stop progress     assign to current user
    }

    public void testPostFunctionAssignsToRenamedUser()
    {
        Map<String, String[]> withDairyComponent = new HashMap<String, String[]>();
        Map<String, String[]> withBeefComponent = new HashMap<String, String[]>();
        withDairyComponent.put("components", new String[] { DAIRY_COMPONENT_ID });
        withBeefComponent.put("components", new String[] { BEEF_COMPONENT_ID });
        renameUser("bb", "robert");
        renameUser("betty", "bboop");
        navigation.login("robert","bb");

        // Check renamed user still gets assigned by a post function
        String dairyIssueKey = navigation.issue().createIssue(
                "Bovine",
                "Task",
                "Milk Old Bessie",
                withDairyComponent);
        navigation.issue().gotoIssue(dairyIssueKey);
        navigation.clickLinkWithExactText("Assign To Me");
        navigation.clickLinkWithExactText("Start Progress");
        assertEquals("bboop", assigneeOf(dairyIssueKey));

        // Check recycled user still gets assigned by a post function
        navigation.issue().resolveIssue(dairyIssueKey, "Fixed", "Old Bessie was not cooperative, but the deed is done.");
        assertEquals("robert", assigneeOf(dairyIssueKey));

        // Check recycled component lead still gets assigned by a post function
        navigation.issue().reopenIssue(dairyIssueKey);
        assertEquals("bboop", assigneeOf(dairyIssueKey));

        // Set up for "assign to current user test"
        navigation.login("bboop", "betty");
        navigation.issue().gotoIssue(dairyIssueKey);
        navigation.clickLinkWithExactText("Start Progress");
        assertEquals("bboop", assigneeOf(dairyIssueKey));

        navigation.login("robert", "bb");

        // Check "assign to current user" still works when current user is recycled
        navigation.issue().gotoIssue(dairyIssueKey);
        navigation.clickLinkWithExactText("Stop Progress");
        assertEquals("robert", assigneeOf(dairyIssueKey));

        // Check renamed component lead still gets assigned by a post function
        String beefIssueKey = navigation.issue().createIssue(
                "Bovine",
                "Task",
                "Slaughter the young heifer.",
                withBeefComponent
        );
        navigation.issue().resolveIssue(beefIssueKey, "Won't Fix", "I couldn't bring myself to do it!");
        navigation.issue().reopenIssue(beefIssueKey);
        assertEquals("robert", assigneeOf(beefIssueKey));

        // Check renamed project lead still gets assigned by a post function
        String calvingIssueKey = navigation.issue().createIssue(
                "Bovine",
                "Task",
                "Breed Bessie and Buster"
        );
        navigation.issue().resolveIssue(calvingIssueKey, "Cannot Reproduce", "They cannot reproduce");
        navigation.issue().reopenIssue(calvingIssueKey);
        assertEquals("robert", assigneeOf(calvingIssueKey));
    }

    private String assigneeOf(String key)
    {
        return backdoor.issues().getIssue(key).fields.assignee.name;
    }

    private void renameUser(String from, String to)
    {
        // Rename bb to bob
        navigation.gotoPage(String.format("secure/admin/user/EditUser!default.jspa?editName=%s", from));
        tester.setFormElement("username", to);
        tester.submit("Update");
    }
}