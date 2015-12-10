package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.PERMISSIONS })
public class TestIssueOperationsWithLimitedPermissions extends FuncTestCase
{
    public static final String RESTRICTED_ISSUE_ID = "10000";
    public static final String RESTRICTED_ISSUE_KEY = "HSP-1";
    public static final String CLOSED_ISSUE_ID = "10020";
    public static final String PERMISSION_ERROR_DESC_ANONYMOUS = "You are not logged in, and do not have the permissions required to act on the selected issue as a guest.";
    public static final String PERMISSION_ERROR_DESC_USER = "You do not have permission to act on this issue.";
    public static final String PERMISSION_ERROR = "You do not have the permission to see the specified issue.";
    private static final String LOGIN = "log in";
    private static final String SIGNUP = "sign up";
    private static final String YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE = "You must log in to access this page.";
    private static final String PERMISSION_VIOLATION_MESSAGE = "It seems that you have tried to perform an operation which you are not permitted to perform.";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        administration.restoreData("TestIssueOperationsWithLimitedPermissionsProEnt.xml");
    }

    public void testIssueOperationsWithLimitedPermissions()
    {
        //testViewIssue()
        assertActionIsInaccessibleToAnonymousUser("/browse/", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, true);
        assertActionIsInaccessibleToUser("/browse/", "new test issue", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("/browse/", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE);

        //testViewIssueVote()
        assertActionIsInaccessibleToAnonymousUser("VoteOrWatchIssue.jspa?vote=vote&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, true);
        assertActionIsInaccessibleToUser("VoteOrWatchIssue.jspa?vote=vote&", "new test issue", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("VoteOrWatchIssue.jspa?vote=vote&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE);
        tester.assertLinkPresent("toggle-vote-issue");
        text.assertTextPresent(new IdLocator(tester, "toggle-vote-issue"), "Remove Vote");
        text.assertTextNotPresent(new IdLocator(tester, "toggle-vote-issue"), "Add Vote");
        assertActionIsInaccessibleToAdminForClosedIssue("VoteOrWatchIssue.jspa?vote=vote&", "An issue that will be closed!", "You cannot vote or change your vote on resolved issues.");

        //testViewIssueUnvote()
        assertActionIsInaccessibleToAnonymousUser("VoteOrWatchIssue.jspa?vote=unvote&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, true);
        assertActionIsInaccessibleToUser("VoteOrWatchIssue.jspa?vote=unvote&", "new test issue", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("VoteOrWatchIssue.jspa?vote=unvote&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE);
        tester.assertLinkPresent("toggle-vote-issue");
        text.assertTextPresent(new IdLocator(tester, "toggle-vote-issue"), "Add Vote");
        text.assertTextNotPresent(new IdLocator(tester, "toggle-vote-issue"), "Remove Vote");
        assertActionIsInaccessibleToAdminForClosedIssue("VoteOrWatchIssue.jspa?vote=unvote&", "An issue that will be closed!", "You cannot vote or change your vote on resolved issues.");

        //testViewIssueStartWatching()
        assertActionIsInaccessibleToAnonymousUser("VoteOrWatchIssue.jspa?watch=watch&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, true);
        assertActionIsInaccessibleToUser("VoteOrWatchIssue.jspa?watch=watch&", "new test issue", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("VoteOrWatchIssue.jspa?watch=watch&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE);
        tester.assertLinkPresent("toggle-watch-issue");
        text.assertTextPresent(new IdLocator(tester, "toggle-watch-issue"), "Stop Watching");
        text.assertTextNotPresent(new IdLocator(tester, "toggle-watch-issue"), "Watch Issue");

        //testViewIssueStopWatching()
        assertActionIsInaccessibleToAnonymousUser("VoteOrWatchIssue.jspa?watch=unwatch&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, true);
        assertActionIsInaccessibleToUser("VoteOrWatchIssue.jspa?watch=unwatch&", "new test issue", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("VoteOrWatchIssue.jspa?watch=unwatch&", "new test issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE);
        tester.assertLinkPresent("toggle-watch-issue");
        text.assertTextNotPresent(new IdLocator(tester, "toggle-watch-issue"), "Stop Watching");
        text.assertTextPresent(new IdLocator(tester, "toggle-watch-issue"), "Watch Issue");

        //testWorkflowUIDispatcher()
        assertActionIsInaccessibleToAnonymousUser("WorkflowUIDispatcher.jspa?action=5&", "Resolve Issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, true);
        assertActionIsInaccessibleToUser("WorkflowUIDispatcher.jspa?action=5&", "Resolve Issue", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("WorkflowUIDispatcher.jspa?action=5&", "Resolve Issue", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE);

        //testAssignIssue()
        assertActionIsInaccessibleToAnonymousUser("AssignIssue!default.jspa?", "assign-issue-submit", PERMISSION_ERROR, true);
        assertActionIsInaccessibleToUser("AssignIssue!default.jspa?", "assign-issue-submit", PERMISSION_ERROR);
        assertActionIsAccessibleToAdmin("AssignIssue!default.jspa?", "assign-issue-submit", PERMISSION_ERROR);
        assertActionIsInaccessibleToAdminForClosedIssue("AssignIssue!default.jspa?", "assign-issue-submit", PERMISSION_VIOLATION_MESSAGE);

        //testCommentAssignIssue()
        assertActionIsInaccessibleToAnonymousUser("CommentAssignIssue!default.jspa?action=5&", "Resolve Issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("CommentAssignIssue!default.jspa?action=5&", "Resolve Issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("CommentAssignIssue!default.jspa?action=5&", "Resolve Issue", PERMISSION_ERROR);

        //testEditIssue()
        assertActionIsInaccessibleToAnonymousUser("EditIssue!default.jspa?", "Edit Issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("EditIssue!default.jspa?", "Edit Issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("EditIssue!default.jspa?", "Edit Issue", PERMISSION_ERROR);
        assertActionIsInaccessibleToAdminForClosedIssue("EditIssue!default.jspa?", null, "You are not allowed to edit this issue due to its current status in the workflow.");

        //testLabels()
        assertActionIsInaccessibleToAnonymousUser("EditLabels!default.jspa?", null, PERMISSION_ERROR, true);
        assertActionIsInaccessibleToUser("EditLabels!default.jspa?", null, PERMISSION_ERROR);
        assertActionIsAccessibleToAdmin("EditLabels!default.jspa?", "Labels", PERMISSION_ERROR);
        assertActionIsInaccessibleToAdminForClosedIssue("EditLabels!default.jspa?", null, PERMISSION_VIOLATION_MESSAGE);

        //testCloneIssueDetails()
        assertActionIsInaccessibleToAnonymousUser("CloneIssueDetails!default.jspa?", "Summary", PERMISSION_ERROR, false);
        assertActionIsInaccessibleToUser("CloneIssueDetails!default.jspa?", "Summary", PERMISSION_ERROR);
        assertActionIsAccessibleToAdmin("CloneIssueDetails!default.jspa?", "Summary", PERMISSION_ERROR);

        //testMoveIssue()
        assertActionIsInaccessibleToAnonymousUser("MoveIssue!default.jspa?", "Current Project", "You are not logged in and do not have the permissions required to browse projects as a guest.", true);
        assertActionIsInaccessibleToUser("MoveIssue!default.jspa?", "Current Project", "You do not have the permissions required to browse any projects.");
        assertActionIsAccessibleToAdmin("MoveIssue!default.jspa?", "homosapien", "You are not logged in and do not have the permissions required to browse projects as a guest.");

        //testViewVoters()
        assertActionIsInaccessibleToAnonymousUser("ViewVoters!default.jspa?", "There are no voters for this issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ViewVoters!default.jspa?", "There are no voters for this issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ViewVoters!default.jspa?", "There are no voters for this issue", PERMISSION_ERROR);

        //testViewVotersAddVote()
        assertActionIsInaccessibleToAnonymousUser("ViewVoters!addVote.jspa?", "There are no voters for this issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ViewVoters!addVote.jspa?", "There are no voters for this issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ViewVoters!addVote.jspa?", "Remove your vote", PERMISSION_ERROR);

        //testViewVotersRemoveVote()
        assertActionIsInaccessibleToAnonymousUser("ViewVoters!removeVote.jspa?", "There are no voters for this issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ViewVoters!removeVote.jspa?", "There are no voters for this issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ViewVoters!removeVote.jspa?", "There are no voters for this issue", PERMISSION_ERROR);

        //testManageWatchers()
        assertActionIsInaccessibleToAnonymousUser("ManageWatchers!default.jspa?", "Watch Issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ManageWatchers!default.jspa?", "Watch Issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ManageWatchers!default.jspa?", "Watch Issue", PERMISSION_ERROR);

        //testManageWatchersStartWatching()
        assertActionIsInaccessibleToAnonymousUser("ManageWatchers!startWatching.jspa?", "Watch Issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ManageWatchers!startWatching.jspa?", "Watch Issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ManageWatchers!startWatching.jspa?", "Stop Watching", PERMISSION_ERROR);

        //testManageWatchersStopWatching()
        assertActionIsInaccessibleToAnonymousUser("ManageWatchers!stopWatching.jspa?", "Watch Issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ManageWatchers!stopWatching.jspa?", "Watch Issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ManageWatchers!stopWatching.jspa?", "Watch Issue", PERMISSION_ERROR);

        //testManageWatchersStartWatchers()
        assertActionIsInaccessibleToAnonymousUser("ManageWatchers!startWatchers.jspa?userNames=admin&", "Watch Issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ManageWatchers!startWatchers.jspa?userNames=admin&", "Watch Issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ManageWatchers!startWatchers.jspa?userNames=admin&", "Stop Watching", PERMISSION_ERROR);

        //testManageWatchersStopWatchers()
        assertActionIsInaccessibleToAnonymousUser("ManageWatchers!stopWatchers.jspa?userNames=admin&", "Watch Issue", PERMISSION_ERROR_DESC_ANONYMOUS, true);
        assertActionIsInaccessibleToUser("ManageWatchers!stopWatchers.jspa?userNames=admin&", "Watch Issue", PERMISSION_ERROR_DESC_USER);
        assertActionIsAccessibleToAdmin("ManageWatchers!stopWatchers.jspa?userNames=admin&", "Stop Watching", PERMISSION_ERROR);

        //testCreateWorklog()
        assertActionIsInaccessibleToAnonymousUser("CreateWorklog!default.jspa?", null, PERMISSION_ERROR, true);
        assertActionIsInaccessibleToUser("CreateWorklog!default.jspa?", null, PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("CreateWorklog!default.jspa?", "Log Work", PERMISSION_ERROR);
        assertActionIsInaccessibleToAdminForClosedIssue("CreateWorklog!default.jspa?", null, PERMISSION_VIOLATION_MESSAGE);

        //testUpdateWorklog!default
        assertActionIsInaccessibleToAnonymousUser("UpdateWorklog!default.jspa?worklogId=10000&", "Edit Work Log", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, false);
        assertActionIsInaccessibleToUser("UpdateWorklog!default.jspa?worklogId=10000&", "Edit Work Log", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("UpdateWorklog!default.jspa?worklogId=10000&", "Edit Work Log", PERMISSION_VIOLATION_MESSAGE);

        //testDeleteWorklog!default
        assertActionIsInaccessibleToAnonymousUser("DeleteWorklog!default.jspa?worklogId=10000&", "Delete Worklog", YOU_MUST_LOG_IN_TO_ACCESS_THIS_PAGE, false);
        assertActionIsInaccessibleToUser("DeleteWorklog!default.jspa?worklogId=10000&", "Delete Worklog", PERMISSION_VIOLATION_MESSAGE);
        assertActionIsAccessibleToAdmin("DeleteWorklog!default.jspa?worklogId=10000&", "Delete Worklog", PERMISSION_VIOLATION_MESSAGE);

        //testEditComment
        assertActionIsInaccessibleToAnonymousUser("EditComment!default.jspa?commentId=10000&", "ignoreMeAndSeeAssertBelow", "You do not have the permission to edit this comment.", false);
        assertActionIsInaccessibleToUser("EditComment!default.jspa?commentId=10000&", "ignoreMeAndSeeAssertBelow", "you do not have the permission to edit this comment.");
        assertActionIsAccessibleToAdmin("EditComment!default.jspa?commentId=10000&", "Edit Comment", "you do not have the permission to edit this comment.");

        //testDeleteComment
        assertActionIsInaccessibleToAnonymousUser("DeleteComment!default.jspa?commentId=10000&", "ignoreMeAndSeeAssertBelow", "You do not have permission to delete comment with id: 10000", false);
        assertActionIsInaccessibleToUser("DeleteComment!default.jspa?commentId=10000&", "ignoreMeAndSeeAssertBelow", "You do not have permission to delete comment with id: 10000");
        assertActionIsAccessibleToAdmin("DeleteComment!default.jspa?commentId=10000&", "Delete Comment", "You do not have permission to delete comment with id: 10000");

        //testCreateSubTaskIssue!default()
        assertActionIsInaccessibleToAnonymousUser("CreateSubTaskIssue!default.jspa?parentIssueId=10000&", "Component/s", "You are not logged in and do not have the permissions required to browse projects as a guest.", true);
        assertActionIsInaccessibleToUser("CreateSubTaskIssue!default.jspa?parentIssueId=10000&", "Component/s", "You do not have the permissions required to browse any projects");
        assertActionIsAccessibleToAdmin("CreateSubTaskIssue!default.jspa?parentIssueId=10000&", "Component/s", PERMISSION_ERROR);

        //testCreateSubTaskIssue()
        assertActionIsInaccessibleToAnonymousUser("CreateSubTaskIssue.jspa?parentIssueId=10000&", "Issue Type", "You are not logged in and do not have the permissions required to browse projects as a guest.", true);
        assertActionIsInaccessibleToUser("CreateSubTaskIssue.jspa?parentIssueId=10000&", "Issue Type", "You do not have the permissions required to browse any projects");
        assertActionIsAccessibleToAdmin("CreateSubTaskIssue.jspa?parentIssueId=10000&", "Issue Type", PERMISSION_ERROR);

        //testCreateSubTaskIssueDetails()
        assertActionIsInaccessibleToAnonymousUser("CreateSubTaskIssueDetails.jspa?parentIssueId=10000&issuetype=5&pid=10000&", "ignoreMeAndSeeAssertBelow", "You are not logged in, and do not have the permissions required to create an issue in this project as a guest.", false);
        assertActionIsInaccessibleToUser("CreateSubTaskIssueDetails.jspa?parentIssueId=10000&issuetype=5&pid=10000&", "ignoreMeAndSeeAssertBelow", "pid: You do not have permission to create issues in this project.");
        assertActionIsAccessibleToAdmin("CreateSubTaskIssueDetails.jspa?parentIssueId=10000&issuetype=5&pid=10000&", "Create Sub-Task", PERMISSION_ERROR);
    }

    private void assertActionIsInaccessibleToAnonymousUser(String actionUrl, String textNotPresent, String error_desc, boolean hasLoginLink)
    {
        navigation.logout();
        page.getFreshXsrfToken();

        if (actionUrl.equals("/browse/"))
        {
            tester.gotoPage("/browse/" + RESTRICTED_ISSUE_KEY + "?atl_token=" + page.getXsrfToken());
        }
        else
        {
            tester.gotoPage("/secure/" + actionUrl + "id=" + RESTRICTED_ISSUE_ID + "&atl_token=" + page.getXsrfToken());
        }
        if (hasLoginLink)
        {
            tester.assertTextPresent(error_desc);
            tester.assertLinkPresentWithText(SIGNUP);
        }
        else
        {
            tester.assertTextPresent(error_desc);
        }
        if (textNotPresent != null)
        {
            tester.assertTextNotPresent(textNotPresent);
        }
    }

    private void assertActionIsInaccessibleToUser(String actionUrl, String textNotPresent, String error_desc)
    {
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        if (actionUrl.equals("/browse/"))
        {
            tester.gotoPage("/browse/" + RESTRICTED_ISSUE_KEY + "?atl_token=" + page.getXsrfToken());
        }
        else
        {
            tester.gotoPage("/secure/" + actionUrl + "id=" + RESTRICTED_ISSUE_ID + "&atl_token=" + page.getXsrfToken());
        }
        tester.assertTextPresent(error_desc);
        tester.assertLinkNotPresentWithText(LOGIN);
        tester.assertLinkNotPresentWithText(SIGNUP);
        if (textNotPresent != null)
        {
            tester.assertTextNotPresent(textNotPresent);
        }
    }

    private void assertActionIsAccessibleToAdmin(String actionUrl, String textPresent, String textNotPresent)
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        if (actionUrl.equals("/browse/"))
        {
            tester.gotoPage("/browse/" + RESTRICTED_ISSUE_KEY + "?atl_token=" + page.getXsrfToken());
        }
        else
        {
            tester.gotoPage("/secure/" + actionUrl + "id=" + RESTRICTED_ISSUE_ID + "&atl_token=" + page.getXsrfToken());
        }
        tester.assertTextNotPresent(textNotPresent);
        tester.assertLinkNotPresentWithText(LOGIN);
        tester.assertLinkNotPresentWithText(SIGNUP);
        tester.assertTextPresent(textPresent);
    }

    private void assertActionIsInaccessibleToAdminForClosedIssue(String actionUrl, String textNotPresent, String error_desc)
    {
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        tester.gotoPage("/secure/" + actionUrl + "id=" + CLOSED_ISSUE_ID + "&atl_token=" + page.getXsrfToken());
        tester.assertTextPresent(error_desc);
        tester.assertLinkNotPresentWithText(LOGIN);
        tester.assertLinkNotPresentWithText(SIGNUP);
        if (textNotPresent != null)
        {
            tester.assertTextNotPresent(textNotPresent);
        }
    }
}
