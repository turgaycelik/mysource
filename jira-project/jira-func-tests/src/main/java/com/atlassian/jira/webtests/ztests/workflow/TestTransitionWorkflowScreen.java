package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests related to the transition workflow screen.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestTransitionWorkflowScreen extends JIRAWebTest
{
    public static final String TEST_BACKUP_XML = "TestTransitionWorkflowScreen.xml";

    private static final String COMMENT_VALUE = "i should survive the error";

    private static final String COMMENT_1 = "This issue is resolved now.";
    private static final String COMMENT_2 = "Viewable by developers group.";
    private static final String COMMENT_3 = "Viewable by Developers role.";
    private static final String COMMENT_4 = "Viewable by jira-adminiistrators role.";
    private static final String COMMENT_5 = "Viewable by Administrators role.";
    private static final String HSP_1 = "HSP-1";
    private static final String HSP_2 = "HSP-2";
    private static final String HSP_3 = "HSP-3";
    private static final String HSP_4 = "HSP-4";
    private static final String HSP_5 = "HSP-5";

    public TestTransitionWorkflowScreen(String name)
    {
        super(name);
    }

    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    public void testCommentValueRemainsOnError()
    {
        restoreData(TEST_BACKUP_XML);
        enableCommentGroupVisibility(Boolean.TRUE);

        String issueKey = HSP_1;
        try
        {
            gotoIssue(issueKey);
            clickLinkWithText("Resolve Issue");
            setWorkingForm("issue-workflow-transition");
            setFormElement("summary", "");
            setFormElement("comment", COMMENT_VALUE);
            selectOption("commentLevel", "jira-developers"); //visible to Users role
            submit("Transition");

            assertTextPresent("You must specify a summary of the issue");
            assertTextPresent(COMMENT_VALUE); //comment value retained?

            setFormElement("summary", "summary of resolving issue");
            submit("Transition");

            //make sure the commentlevel value survived the error and so now fred can't see it
            checkCommentVisibility(FRED_USERNAME, issueKey, null, EasyList.build(COMMENT_VALUE));
            //admin should be able to see it
            checkCommentVisibility(ADMIN_USERNAME, issueKey, EasyList.build(COMMENT_VALUE), null);
        }
        finally
        {
            deleteIssue(issueKey);
            removeFieldFromFieldScreen(RESOLVE_FIELD_SCREEN_NAME, new String[] { "Summary" });
        }
    }

    public void testTransitionIssueForTheCommentVisibility()
    {
        restoreData(TEST_BACKUP_XML);
        enableCommentGroupVisibility(Boolean.TRUE);
        // resolve issue with comment visible to all users
        displayAllIssues();
        clickLinkWithText(HSP_1);
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        setFormElement("comment", COMMENT_1);
        submit("Transition");

        // resolve issue with comment visible only to jira-developers group
        displayAllIssues();
        clickLinkWithText(HSP_2);
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        setFormElement("comment", COMMENT_2);
        selectOption("commentLevel", "jira-developers");
        submit("Transition");

        // resolve issue with comment visible only to Developers role
        displayAllIssues();
        clickLinkWithText(HSP_3);
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        setFormElement("comment", COMMENT_3);
        selectOption("commentLevel", "Developers");
        submit("Transition");

        // resolve issue with comment visible only to Adminstrators role
        displayAllIssues();
        clickLinkWithText(HSP_4);
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        setFormElement("comment", COMMENT_4);
        selectOption("commentLevel", "Administrators");
        submit("Transition");

        // resolve issue with comment visible only to jira-administrators role
        displayAllIssues();
        clickLinkWithText(HSP_5);
        clickLink("action_id_5");
        setWorkingForm("issue-workflow-transition");
        setFormElement("comment", COMMENT_5);
        selectOption("commentLevel", "jira-administrators");
        submit("Transition");

        // verify that Fred can see general comment but not others as he is not in the visibility groups
        checkCommentVisibility(FRED_USERNAME, "HSP-1", EasyList.build(COMMENT_1), null);
        checkCommentVisibility(FRED_USERNAME, "HSP-2", null, EasyList.build(COMMENT_2));
        checkCommentVisibility(FRED_USERNAME, "HSP-3", null, EasyList.build(COMMENT_3));

        checkCommentVisibility(ADMIN_USERNAME, "HSP-1", EasyList.build(COMMENT_1), null);
        checkCommentVisibility(ADMIN_USERNAME, "HSP-2", EasyList.build(COMMENT_2), null);
        checkCommentVisibility(ADMIN_USERNAME, "HSP-3", EasyList.build(COMMENT_3), null);

        // verify devman can only see dev resolve comments and not admin resolve comments
        checkCommentVisibility("devman", "HSP-2", EasyList.build(COMMENT_2), null);
        checkCommentVisibility("devman", "HSP-3", EasyList.build(COMMENT_3), null);
        checkCommentVisibility("devman", "HSP-4", null, EasyList.build(COMMENT_4));
        checkCommentVisibility("devman", "HSP-5", null, EasyList.build(COMMENT_5));

        // verify adminman can only see admin resolve comment and not dev resolve comments
        checkCommentVisibility("adminman", "HSP-2", null, EasyList.build(COMMENT_2));
        checkCommentVisibility("adminman", "HSP-3", null, EasyList.build(COMMENT_3));
        checkCommentVisibility("adminman", "HSP-4", EasyList.build(COMMENT_4), null);
        checkCommentVisibility("adminman", "HSP-5", EasyList.build(COMMENT_5), null);
    }

    public void testTransitionWithBlankDescription() throws Exception
    {
        restoreData("TestTransitionDescriptionEmpty.xml");
        gotoIssue("HMS-1");
        assertTextPresent("Start Progress - a description");
        assertTextNotPresent("Resolve Issue - ");
    }
}
