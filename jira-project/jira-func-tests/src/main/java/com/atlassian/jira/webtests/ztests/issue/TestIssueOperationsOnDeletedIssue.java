package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueOperationsOnDeletedIssue extends JIRAWebTest
{
    public static final String DELETED_ISSUE_ID = "10000";
    public static final String DELETED_ISSUE_KEY = "HSP-1";
    public static final String EXISTING_ISSUE_ID = "10001";
    public static final String EXISTING_ISSUE_KEY = "HSP-2";
    public static final String DELETED_ISSUE_ERROR = "The issue no longer exists.";
    public static final String DELETED_VIEW_ISSUE_ERROR = "The issue you are trying to view does not exist.";
    private static final String MANAGE_WATCHERS = "Watchers";

    public TestIssueOperationsOnDeletedIssue(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueOperationsOnDeletedIssueProEnt.xml");
    }

    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    public void testIssueOperationsOnDeletedIssue()
    {
        _testIssueOperationsOnDeletedIssueStandard();
        _testIssueOperationsOnDeletedIssueProfessionalAndEnterprise();
    }

    public void _testIssueOperationsOnDeletedIssueStandard()
    {
        //testViewIssue()
        assertDeletedIssueErrorKickass("Details");

        //testViewIssueVote()
        assertDeletedIssueError("VoteOrWatchIssue.jspa?vote=vote&", "Details", true);

        //testViewIssueUnvote()
        assertDeletedIssueError("VoteOrWatchIssue.jspa?vote=unvote&", "Details", true);


        //testViewIssueStartWatching()
        assertDeletedIssueError("VoteOrWatchIssue.jspa?watch=watch&", "Details", true);

        //testViewIssueStopWatching()
        assertDeletedIssueError("VoteOrWatchIssue.jspa?watch=unwatch&", "Details", true);

        //testWorkflowUIDispatcher()
        assertDeletedIssueError("WorkflowUIDispatcher.jspa?action=5&", "Resolve Issue");
        gotoPage("/secure/WorkflowUIDispatcher.jspa?id=" + EXISTING_ISSUE_ID + "&action=5&atl_token=" + page.getXsrfToken());
        assertTextPresent("Resolve Issue");
        gotoPage("/secure/WorkflowUIDispatcher.jspa?id=" + DELETED_ISSUE_ID + "&action=5&atl_token=" + page.getXsrfToken());
        assertTextPresent(DELETED_ISSUE_ERROR);
        assertTextNotPresent("Resolve Issue");

        //testCommentAssignIssue()
        assertDeletedIssueError("CommentAssignIssue!default.jspa?action=5&", "Resolve Issue");

        //testAssignIssue()
        assertDeletedIssueError("AssignIssue!default.jspa?", "Assign");

        //testEditIssue()
        assertDeletedIssueError("EditIssue!default.jspa?", "Edit Issue");

        //testCloneIssueDetails()
        assertDeletedIssueError("CloneIssueDetails!default.jspa?", "Summary");

        //testMoveIssue()
        assertDeletedIssueError("MoveIssue!default.jspa?", "homosapien");

        //testViewVoters()
        assertDeletedIssueError("ViewVoters!default.jspa?", "Voters");

        //testViewVotersAddVote()
        assertDeletedIssueError("ViewVoters!addVote.jspa?", "Voters");

        //testViewVotersRemoveVote()
        assertDeletedIssueError("ViewVoters!removeVote.jspa?", "Voters");

        //testManageWatchers()
        assertDeletedIssueError("ManageWatchers!default.jspa?", MANAGE_WATCHERS);

        //testManageWatchersStartWatching()
        assertDeletedIssueError("ManageWatchers!startWatching.jspa?", MANAGE_WATCHERS);

        //testManageWatchersStopWatching()
        assertDeletedIssueError("ManageWatchers!stopWatching.jspa?", MANAGE_WATCHERS);

        //testManageWatchersStartWatchers()
        assertDeletedIssueError("ManageWatchers!startWatchers.jspa?userNames=admin&", MANAGE_WATCHERS);

        //testManageWatchersStopWatchers()
        assertDeletedIssueError("ManageWatchers!stopWatchers.jspa?userNames=admin&", MANAGE_WATCHERS);

        //testCreateWorklog()
        assertDeletedIssueError("CreateWorklog!default.jspa?", "Log Work");

        //testUpdateWorklog!default
        assertDeletedIssueError("UpdateWorklog!default.jspa?worklogId=10000&", "Edit Work Log");

        //testDeleteWorklog!default
        assertDeletedIssueError("DeleteWorklog!default.jspa?worklogId=10000&", "Delete Worklog");

        //testEditComment
        assertDeletedIssueError("EditComment!default.jspa?commentId=10000&", "Edit Comment", "first comment", false);

        //testDeleteComment - does not care if issue doesnt exist
        //assertDeletedIssueError("DeleteComment!default.jspa?", "Delete Comment");
    }

    private void _testIssueOperationsOnDeletedIssueProfessionalAndEnterprise()
    {
        //testCreateSubTaskIssue!default()
        gotoPage("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=" + EXISTING_ISSUE_ID);
        assertTextPresent("Create Sub-Task");
        gotoPage("/secure/CreateSubTaskIssue!default.jspa?parentIssueId=" + DELETED_ISSUE_ID);
        assertTextPresent("The issue no longer exists.");

        //testCreateSubTaskIssue()
        gotoPage("/secure/CreateSubTaskIssue.jspa?parentIssueId=" + EXISTING_ISSUE_ID);
        assertTextPresent("Create Sub-Task");
        gotoPage("/secure/CreateSubTaskIssue.jspa?parentIssueId=" + DELETED_ISSUE_ID);
        assertTextPresent("The issue no longer exists.");

        //testCreateSubTaskIssueDetails()
        gotoFieldConfigurationDefault();
        //hide security level field
        clickLink("hide_15");
        gotoPage(page.addXsrfToken("/secure/CreateSubTaskIssueDetails.jspa?issuetype=5&pid=10000&summary=createdSubtask&assignee=admin&reporter=admin&parentIssueId=" + EXISTING_ISSUE_ID));
        assertTextPresent("createdSubtask");
        assertTextNotPresent("Create Sub-Task");
        assertTextNotPresent("Enter the details of the issue");
        assertTextPresent("Sub-task");
        gotoPage(page.addXsrfToken("/secure/CreateSubTaskIssueDetails.jspa?issuetype=5&pid=10000&summary=createdSubtask&assignee=admin&reporter=admin&parentIssueId=" + DELETED_ISSUE_ID));
        assertTextPresent("Parent Issue cannot be null");
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods
    private void assertDeletedIssueError(String actionUrl, String textNotPresent, boolean isViewIssue)
    {
        assertDeletedIssueError(actionUrl, textNotPresent, textNotPresent, isViewIssue);
    }

    private void assertDeletedIssueError(String actionUrl, String textNotPresent)
    {
        assertDeletedIssueError(actionUrl, textNotPresent, textNotPresent, false);
    }

    private void assertDeletedIssueErrorKickass(String textNotPresent)
    {
        gotoPage("/browse/" + EXISTING_ISSUE_KEY + "?atl_token=" + page.getXsrfToken());
        assertTextPresent(textNotPresent);
        gotoPage("/browse/" + DELETED_ISSUE_KEY + "?atl_token=" + page.getXsrfToken());
        assertTextPresent(DELETED_VIEW_ISSUE_ERROR);
    }

    private void assertDeletedIssueError(String actionUrl, String textPresent, String textNotPresent, boolean isViewIssue)
    {
        gotoPage("/secure/" + actionUrl + "id=" + EXISTING_ISSUE_ID + "&atl_token=" + page.getXsrfToken());
        assertTextPresent(textPresent);
        gotoPage("/secure/" + actionUrl + "id=" + DELETED_ISSUE_ID + "&atl_token=" + page.getXsrfToken());
        if (isViewIssue)
        {
            assertTextPresent(DELETED_VIEW_ISSUE_ERROR);
        }
        else
        {
            assertTextPresent(DELETED_ISSUE_ERROR);
        }
    }
}
