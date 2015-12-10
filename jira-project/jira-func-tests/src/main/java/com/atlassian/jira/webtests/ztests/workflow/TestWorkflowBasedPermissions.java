package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads data with a slightly customized permission scheme and a 'ChangeRequest' workflow XML with added <meta> attributes
 * to restrict permissions per status.
 */
@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.WORKFLOW })
public class TestWorkflowBasedPermissions extends JIRAWebTest
{
    public TestWorkflowBasedPermissions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("WorkflowBasedPermissions.zip");
        getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        // Ensure attachments point to the correct directory
        administration.attachments().enable();
    }

    @Override
    public void tearDown()
    {
//        getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        super.tearDown();
    }

    public void testWorkflowPermissions()
    {
        _testSingleIssuePermissions();

        _testBulkEdit();
    }

    private void _testSingleIssuePermissions()
    {
        log("Check that non-overridden 'assignables' are correct");
        login("test", "test");
        createIssueStep1("Test Project", "Bug");
        assertFormElementPresent("assignee");
        assertOptionValuesEqual("assignee", new String[] { "-1", "assignable", "devadmin", "developer", "test" });

        log("Test that the 'assignables' list is overridden when creating issues");
        createIssueStep1("Test Project", "ChangeRequest");
        assertOptionValuesEqual("assignee", new String[] { "-1", "assignable", "devadmin", "developer" });

        log("Testing normal permissions on Open state");
        // Check that the normal permissions are all working
        //                                                  browse, assign, attach, clone,  comment,create, delete, edit,   link,   move
        assertIssuePermissions("test", "TP-8", new boolean[] { true, true, true, true, true, true, true, true, true, true });
        assertIssuePermissions("test", "TP-9", new boolean[] { true, true, true, true, true, false, true, true, true, true });
        gotoIssue("TP-8");
        clickLink("assign-issue");
        assertOptionValuesEqual("assignee", new String[] { "-1", "developer", "test", "assignable", "devadmin", "developer", "test"});

        log("Set the assignee to 'developer'");
        selectOption("assignee", "Joe Developer");
        submit();
        assertTextPresentBeforeText("Assignee:", "Joe Developer");

        log("Progress workflow to 'Approved' status");
        clickLinkWithText("Approved");
        setFormElement("summary", "Approved CR");
        submit();

//      <meta name="jira.permission.assign.user">assignable</meta>
//      <meta name="jira.permission.attach.group">jira-developers</meta>
//      <meta name="jira.permission.comment.reporter"></meta>
//      <meta name="jira.permission.delete.lead"></meta>
//      <meta name="jira.permission.edit.group">${pkey}-interest</meta>
//      <meta name="jira.permission.link.user">manager</meta>
//      <meta name="jira.permission.link.group">jira-qa</meta>
//      <meta name="jira.permission.move.group">moredevs</meta>

        //                                                          browse, assign, attach, clone,  comment,    create, delete, edit,   link,   move
        assertIssuePermissions("test", "TP-8", new boolean[] { true, false, true, true, true, true, true, false, false, false });
        assertIssuePermissions("user1", "TP-8", new boolean[] { true, false, false, true, false, true, false, false, false, false });
        assertIssuePermissions("qa", "TP-8", new boolean[] { true, false, false, true, false, true, false, false, false, false });
        assertIssuePermissions("tpcustomer", "TP-8", new boolean[] { true, false, false, true, false, true, false, true, false, false });
        assertIssuePermissions("assignable", "TP-8", new boolean[] { true, true, true, true, false, true, false, false, false, false });
        assertIssuePermissions("manager", "TP-8", new boolean[] { true, false, false, true, false, true, false, false, true, false });
        assertIssuePermissions("devadmin", "TP-8", new boolean[] { true, false, true, true, false, true, false, false, false, true });

        // Subtasks can only be browsed, and cloned (since they can be created in the parent).
        assertIssuePermissions("test", "TP-9", new boolean[] { true, false, false, true, false, false, false, false, false, false });

        gotoIssue("TP-8");
        log("Check that permissions are back to normal in 'In Progress' state");
        clickLinkWithText("Start Progress");
        setFormElement("summary", "In Progress CR");
        submit();
        assertIssuePermissions("test", "TP-8", new boolean[] { true, true, true, true, true, true, true, true, true, true });

        gotoIssue("TP-8");
        log("Check that the correct users are assignable in 'In Progress' state");
        clickLink("assign-issue");
        assertOptionValuesEqual("assignee", new String[] { "-1", "developer", "test", "assignable", "devadmin", "developer", "test"});

        gotoIssue("TP-10");
        log("Check that a subtask in the 'Approved' state becomes invisible");
        clickLinkWithText("Approved");
        setFormElement("summary", "hideme");
        submit();
        assertTextPresent("Permission Violation");

        gotoIssue("TP-8");
        log("Check that on 'Resolve' transition screen, we see subset of assignees permissible for Resolved issues");
        clickLinkWithText("Resolve");
        assertOptionValuesEqual("assignee", new String[] { "-1", "developer" , "developer" });

        log("Check that we can resolve the issue");
        setFormElement("summary", "Resolved CR");
        selectOption("assignee", "Joe Developer");
        setFormElement("customfield_10000", "developer"); // Set the 'Customer' field to developer; only this user will be allowed to edit
        submit();
        assertTextPresentBeforeText("Assignee:", "Joe Developer");

        log("Check that in the 'Resolved' state, editing is limited to developer (custom field val), and permissions are otherwise normal");
        //                                                          browse, assign, attach, clone,  comment,    create, delete, edit,   link,   move
        assertIssuePermissions("test", "TP-8", new boolean[] { true, true, true, true, true, true, true, false, true, true });
        assertIssuePermissions("developer", "TP-8", new boolean[] { true, true, true, true, true, true, false, true, true, true });
        assertIssuePermissions("qa", "TP-8", new boolean[] { true, false, true, true, true, true, false, false, false, false });
        assertIssuePermissions("tpcustomer", "TP-8", new boolean[] { true, false, true, true, true, true, false, false, false, false });
        assertIssuePermissions("assignable", "TP-8", new boolean[] { true, true, true, true, true, true, false, false, true, true });
        assertIssuePermissions("manager", "TP-8", new boolean[] { true, false, true, true, true, true, false, false, true, false });
        assertIssuePermissions("devadmin", "TP-8", new boolean[] { true, true, true, true, true, true, false, false, true, true });
    }

    private void assertIssuePermissions(String user, String key, boolean[] permissions)
    {
        login(user, user);
        gotoIssue(key);
        log("Testing " + key + " permissions for " + user);
        if (permissions[0]) // browse
        {
            if (permissions[1])
            {
                assertLinkPresent("assign-issue");
            }
            else
            {
                assertLinkNotPresent("assign-issue");
            }
            if (permissions[2])
            {
                assertLinkPresent("attach-file");
            }
            else
            {
                assertLinkNotPresent("attach-file");
            }
            if (permissions[3])
            {
                assertLinkPresent("clone-issue");
            }
            else
            {
                assertLinkNotPresent("clone-issue");
            }
            if (permissions[4])
            {
                assertLinkPresent("comment-issue");
                assertLinkPresent("footer-comment-button");
            }
            else
            {
                assertLinkNotPresent("comment-issue");
                assertLinkNotPresent("footer-comment-button");
            }
            if (permissions[5])
            {
                assertLinkPresent("create-subtask");
            }
            else
            {
                assertLinkNotPresent("create-subtask");
            }
            if (permissions[6])
            {
                assertLinkPresent("delete-issue");
            }
            else
            {
                assertLinkNotPresent("delete-issue");
            }
            if (permissions[7])
            {
                assertLinkPresent("edit-issue");
            }
            else
            {
                assertLinkNotPresent("edit-issue");
            }
            if (permissions[8])
            {
                assertLinkPresent("link-issue");
            }
            else
            {
                assertLinkNotPresent("link-issue");
            }
            if (permissions[9])
            {
                assertLinkPresent("move-issue");
            }
            else
            {
                assertLinkNotPresent("move-issue");
            }
        }
        else
        {
            assertTextPresent("Permission Violation");
        }
    }

    private void _testBulkEdit()
    {
        // Ensure that bulk
        clickLink(LINK_ASSIGN_ISSUE);
        assertOptionValuesEqual("assignee", new String[] { "-1", "developer" , "developer" });

        gotoIssue("TP-11");
        clickLink(LINK_ASSIGN_ISSUE);
        assertOptionValuesEqual("assignee", new String[] { "-1", "test", "assignable", "devadmin", "developer", "test"});

        displayAllIssues();

        bulkChangeIncludeAllPages();

        bulkChangeSelectIssues(Arrays.asList("TP-8", "TP-11"));

        bulkChangeChooseOperationEdit();

        // Ensure only 'developer' option is available as TP-8 is in the Resolved status
        assertOptionValuesEqual("assignee", new String[] { "-1", "developer" , "developer" });

        Map<String, String> fields = new HashMap<String, String>();
        fields.put(FIELD_ASSIGNEE, "Joe Developer");
        bulkEditOperationDetailsSetAs(fields);

        fields = new HashMap<String, String>();
        fields.put(FIELD_ASSIGNEE, "Joe Developer");
        bulkEditConfirmEdit(fields);
        bulkChangeConfirm();

        gotoIssue("TP-8");
        assertTextPresentBeforeText("Assignee", "Joe Developer");

        gotoIssue("TP-11");
        assertTextPresentBeforeText("Assignee", "Joe Developer");

        // Now ensure that if only one issue is selected all of its assignees are available
        displayAllIssues();

        bulkChangeIncludeAllPages();

        bulkChangeSelectIssues(Arrays.asList("TP-11"));

        bulkChangeChooseOperationEdit();

        assertOptionValuesEqual("assignee", new String[] { "-1", "developer", "assignable", "devadmin", "developer" });

        fields = new HashMap<String, String>();
        fields.put(FIELD_ASSIGNEE, "Dev-Admin");
        bulkEditOperationDetailsSetAs(fields);

        fields = new HashMap<String, String>();
        fields.put(FIELD_ASSIGNEE, "Dev-Admin");
        bulkEditConfirmEdit(fields);
        bulkChangeConfirm();

        gotoIssue("TP-11");
        assertTextPresentBeforeText("Assignee", "Dev-Admin");
    }
}
