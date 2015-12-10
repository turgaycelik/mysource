package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebTest ({ Category.FUNC_TEST, Category.CLONE_ISSUE, Category.ISSUES, Category.SUB_TASKS })
public class TestCloneIssueWithSubTasks extends JIRAWebTest
{
    protected static final String CLONE_LINKS_CHECKBOX_LABEL = "Clone Links";
    protected static final String CLONE_SUBTASKS_CHECKBOX_LABEL = "Clone Sub Tasks";
    protected static final String CLONE_SUMMARY_PREFIX = "CLONE - ";
    protected static final String SUMMARY_FIELD_NAME = "summary";
    protected static final String SUBTASKS_TEXT = "Sub-Tasks";
    protected static final String LINKS_TEXT = "Issue Links";
    protected static final String CLONE_LINKS_CHECKBOX_NAME = "cloneLinks";
    private static final String CLONE_SUBTASKS_CHECKBOX_NAME = "cloneSubTasks";

    public TestCloneIssueWithSubTasks(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        // Restore different data to the parent test, as this test tests sub-tasks as well.
        restoreData("TestCloneIssueWithSubTasks.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
    }

    public void testCloneNoLinks()
    {
        // Test cloning an issue with no links and no sub-tasks
        gotoIssueAndClickClone("HSP-1");
        // Ensure linsk chekcbox is not shown as there are no links to clone
        assertTextNotPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks checkbox is not there as there are no sub-tasks to clone
        assertTextNotPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        String cloneKey = cloneIssue("HSP", "Test Issue", false);

        // Ensure that all fields were cloned correctly
        assertTextPresentBeforeText("Type", "Task");
        assertTextPresentBeforeText("Status", "Open");
        assertTextPresentBeforeText("Priority", "Minor");
        assertTextPresentBeforeText("Assignee", ADMIN_FULLNAME);
        assertTextPresentBeforeText("Reporter", ADMIN_FULLNAME);
        assertTextPresentBeforeText("Vote", "0");
        assertTextPresentBeforeText("Watch", "0");

        // Ordering fixed for JRA-15011
        assertTextSequence(new String[]{"Component/s", "New Component 1", "New Component 2"});
        assertTextSequence(new String[]{"Affects Version/s", "New Version 1", "New Version 4"});
        assertTextSequence(new String[]{"Fix Version/s", "New Version 4", "New Version 5"});

        assertTextSequence(new String[]{"Original Estimate", "4 days", "Remaining Estimate", "4 days", "Time Spent", "Not Specified"});
        assertTextSequence(new String[]{"Environment", "Test Environment"});
        assertTextPresentBeforeText("Description", "Test Description");

        // Check that sub-tasks did not magically appear
        assertTextNotPresent(SUBTASKS_TEXT);
        // Check that links did not magically appear
        assertTextNotPresent(LINKS_TEXT);

        //check that the item details were clone is reflected in the index
        assertIndexedFieldCorrect("//item", EasyMap.build("type", "Task", "status", "Open", "priority", "Minor", "votes", "0", "description", "Test Description"), null, cloneKey);
    }

    public void testClonePermission()
    {
        // The user should only be allowed to clone issues if they have Create permission in the issue's project
        gotoIssue("HSP-1");
        // ensure the clone link is there
        assertLinkPresent(LINK_CLONE_ISSUE);
        // Remove the create permission
        removeGroupPermission(CREATE_ISSUE, Groups.USERS);
        // Navigate back to the issue
        gotoIssue("HSP-1");
        // Ensure teh clone link is not there
        assertLinkNotPresent(LINK_CLONE_ISSUE);
    }

    public void testCloneWithLinks()
    {
        // Test cloning issue with links and no sub-tasks
        gotoIssueAndClickClone("HSP-2");
        // Ensure the links checkbox is shown as the issue has links
        assertTextPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure the sub-taks checkbox is not shown as the issue has no sub-tasks to clone
        assertTextNotPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        // Select that we do want to clone links
        checkCheckbox(CLONE_LINKS_CHECKBOX_NAME, "true");

        String clonedKey = cloneIssue("HSP", "Test Issue with link", false);

        // Ensure that sub-tasks did not magically appear from somewhere :)
        assertTextNotPresent(SUBTASKS_TEXT);

        // Ensure that all the linsk were copied across
        assertTextPresent(LINKS_TEXT);
        assertTextSequence(new String[]{"out", "HSP-3"});
        assertTextSequence(new String[]{"in", "HSP-4"});
        assertTextSequence(new String[]{"outward", "HSP-5"});
        assertTextSequence(new String[]{"inward", "HSP-6"});

        //test that the cloned issue links and summary have been indexed correctly
        assertIndexedFieldCorrect("//item", EasyMap.build("summary", "CLONE - Test Issue with link", "resolution", "Unresolved"), null, clonedKey);
        assertIndexedFieldCorrect("//item/issuelinks/issuelinktype", EasyMap.build("name", "Test Link 1"), null, clonedKey);

    }

    public void testCloneIssueLinksDisabled()
    {
        // Disable issue links in admin section
        disableIssueLinks();

        // Test cloning issue which has links and sub-tasks
        gotoIssueAndClickClone("HSP-2");

        // Ensure links checkbox is not present as links are disabled
        assertTextNotPresent(CLONE_LINKS_CHECKBOX_LABEL);

        cloneIssue("HSP", "Test Issue with link", false);

        // Ensure links were not cloned
        assertTextNotPresent(LINKS_TEXT);
    }

    public void testCloneWithClonersLink()
    {
        createClonersLinkType();

        // Test cloning an issue with the cloners link present
        String originalIssueKey = "HSP-1";
        gotoIssueAndClickClone(originalIssueKey);

        // Ensure links checkbox is not shown as there are no links to clone
        assertTextNotPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks checkbox is not there as there are no sub-tasks to clone
        assertTextNotPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        String cloneIssueKey = cloneIssue("HSP", "Test Issue", true);

        // Check that sub-tasks did not magically appear
        assertTextNotPresent(SUBTASKS_TEXT);

        // Check that the cloners link was created
        assertTextPresent(LINKS_TEXT);
        assertTextSequence(new String[]{CLONERS_OUTWARD_LINK_NAME, originalIssueKey});

        // Ensure the Cloners link appears on the issue that was cloned
        gotoIssue(originalIssueKey);
        assertTextPresent(LINKS_TEXT);
        assertTextSequence(new String[]{CLONERS_INWARD_LINK_NAME, cloneIssueKey});
    }


    protected void gotoIssueAndClickClone(String issueKey)
    {
        gotoIssue(issueKey);
        clickLink(LINK_CLONE_ISSUE);
    }

    protected String cloneIssue(String projectKey, String summary, boolean cloneLinkPresent)
    {
        summary = CLONE_SUMMARY_PREFIX + summary;
        assertFormElementEquals(SUMMARY_FIELD_NAME, summary);

        if (cloneLinkPresent)
        {
            assertTextNotPresent("The clone link type \"" + CLONERS_LINK_TYPE_NAME + "\" does not exist. A link to the original issue will not be created.");
        }
        else
        {
            assertTextPresent("The clone link type \"" + CLONERS_LINK_TYPE_NAME + "\" does not exist. A link to the original issue will not be created.");
        }

        submit();

        // Ensure we are on the browse issue page
        assertTrue(getDialog().getResponse().getURL().getPath().indexOf("/browse") > -1);

        // Check that the the issue was cloned properly
        assertTextPresent(summary);

        try
        {
            return extractIssueKey(projectKey);
        }
        catch (Exception e)
        {
            fail("Unable to retrieve issue key" + e.getMessage());
            return "fail";
        }
    }

    public void testCloneNoLinksWithSubTask()
    {
        // Test cloning an issue with sub-tasks and no links
        gotoIssueAndClickClone("HSP-7");
        // Ensure links check box is not there as there are no links to clone
        assertTextNotPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks check box is there as there are sub-tasks to clone
        assertTextPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        // Ensure that sub-tasks check box is checked
        assertFormElementEquals(CLONE_SUBTASKS_CHECKBOX_NAME, "true");
        // As sub-tasks form element is checked by default no need to select it

        cloneIssue("HSP", "Test Issue with sub task", false);

        // Test that links did not magically appear
        assertTextNotPresent(LINKS_TEXT);

        // Ensure all sub-tasks are there
        assertTextPresent(SUBTASKS_TEXT);
        assertTextPresent("Test Sub task");
        assertTextPresent("Another Sub Task");
    }

    public void testCloneWithLinksAndSubTasks()
    {
        // Test cloning with links and sub-tasks
        gotoIssueAndClickClone("HSP-10");
        // Ensure links checkbox is there as there are links to clone
        assertTextPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks checkbox is there as there are links to clone
        assertTextPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        // Check the clone links checkbox. The sub-tasks checkbox should be checked automatically
        checkCheckbox(CLONE_LINKS_CHECKBOX_NAME, "true");

        cloneIssue("HSP", "Issue with link and sub-task", false);

        // Ensure links were cloned correctly
        assertTextPresent(LINKS_TEXT);
        assertTextSequence(new String[]{"out", "HSP-3"});
        assertTextSequence(new String[]{"inward", "HSP-6"});

        // Ensure sub-tasks were cloned correctly
        assertTextPresent(SUBTASKS_TEXT);
        assertTextPresent("Another test subtask");
        assertTextPresent("Second sub-task");
    }

    public void testCloneIssueWithLinksAndSubTasksNoLinks()
    {
        // Test cloning issue which has links and sub-tasks
        gotoIssueAndClickClone("HSP-10");
        // Ensure links checkbox is there as there are links to clone
        assertTextPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks checkbox is there as there are links to clone
        assertTextPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        // Do not check the links checkbox
        // Subtasks check box is checked by default

        cloneIssue("HSP", "Issue with link and sub-task", false);

        // Ensure links were not cloned
        assertTextNotPresent(LINKS_TEXT);

        // Ensure sub-tasks were cloned correctly
        assertTextPresent(SUBTASKS_TEXT);
        assertTextPresent("Another test subtask");
        assertTextPresent("Second sub-task");
    }

    public void testCloneIssueWithLinksAndSubTasksNoSubTasks()
    {
        // Test cloning issue which has links and sub-tasks
        gotoIssueAndClickClone("HSP-10");
        // Ensure links checkbox is there as there are links to clone
        assertTextPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks checkbox is there as there are links to clone
        assertTextPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        // Check the links checkbox
        checkCheckbox(CLONE_LINKS_CHECKBOX_NAME, "true");

        // Uncheck the sub-tasks checkbox
        uncheckCheckbox(CLONE_SUBTASKS_CHECKBOX_NAME);

        cloneIssue("HSP", "Issue with link and sub-task", false);

        // Ensure links were not cloned
        assertTextPresent(LINKS_TEXT);
        assertTextSequence(new String[]{"out", "HSP-3"});
        assertTextSequence(new String[]{"inward", "HSP-6"});

        // Ensure sub-tasks were not cloned
        assertTextNotPresent(SUBTASKS_TEXT);
    }

    public void testCloneIssueWithLinksAndSubTasksNoSubTasksNoLinks()
    {
        // Test cloning issue which has links and sub-tasks
        gotoIssueAndClickClone("HSP-10");
        // Ensure links checkbox is there as there are links to clone
        assertTextPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks checkbox is there as there are links to clone
        assertTextPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        // Do not check the links checkbox
        // Uncheck the sub-tasks checkbox
        uncheckCheckbox(CLONE_SUBTASKS_CHECKBOX_NAME);

        cloneIssue("HSP", "Issue with link and sub-task", false);

        // Ensure links were not cloned
        assertTextNotPresent(LINKS_TEXT);
        // Ensure sub-tasks were not cloned
        assertTextNotPresent(SUBTASKS_TEXT);
    }

    public void testCloneIssueWithSubTaskWithLink()
    {
        gotoIssueAndClickClone("HSP-13");
        // Ensure links checkbox is there as an issue has a sub task with a link
        assertTextPresent(CLONE_LINKS_CHECKBOX_LABEL);
        // Ensure sub-tasks checkbox is there as there are links to clone
        assertTextPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        // Check the links checkbox
        checkCheckbox(CLONE_LINKS_CHECKBOX_NAME, "true");

        cloneIssue("HSP", "Test issue with subtask which has a link", false);

        // Ensure links are not there as the cloned issue does not have any links
        assertTextNotPresent(LINKS_TEXT);
        // Ensure sub-tasks were cloned correctly
        assertTextPresent(SUBTASKS_TEXT);
        String subTaskWithLinkSummary = "Sub task with link";
        assertTextPresent(subTaskWithLinkSummary);
        assertTextPresent("One more subtask");

        clickLinkWithText(subTaskWithLinkSummary);
        assertTextPresent(LINKS_TEXT);
        assertTextSequence(new String[]{"in", "HSP-4"});
        assertTextSequence(new String[]{"outward", "HSP-5"});
    }

    public void testCloneIssueLinksDisabledWithSubTasks()
    {
        // Disable issue links in admin section
        disableIssueLinks();

        // Test cloning issue which has links and sub-tasks
        gotoIssueAndClickClone("HSP-10");

        // Ensure links checkbox is not present as links are disabled
        assertTextNotPresent(CLONE_LINKS_CHECKBOX_LABEL);

        // Ensure the sub-task check box is there as there are sub-tasks to clone
        assertTextPresent(CLONE_SUBTASKS_CHECKBOX_LABEL);

        cloneIssue("HSP", "Issue with link and sub-task", false);

        // Ensure links were not cloned
        assertTextNotPresent(LINKS_TEXT);

        // Ensure sub-tasks were cloned correctly
        assertTextPresent(SUBTASKS_TEXT);
        assertTextPresent("Another test subtask");
        assertTextPresent("Second sub-task");
    }

    //JRA-18731
    public void testCloneResolvedIssueClearsResolutionDate()
    {
        gotoIssue("HSP-1");
        //first we need to resolve the issue, so we can test cloning a resolved issue.
        tester.clickLink("action_id_5");
        tester.setWorkingForm("issue-workflow-transition");
        tester.setFormElement("comment", "This issue is now done!");
        tester.submit("Transition");
        assertTextSequence(new String[] { "Test Issue", "Due", "Created", "Updated", "Resolved" });

        //now lets clone the issue!
        tester.clickLink("clone-issue");
        tester.setFormElement("summary", "CLONE - Test Issue");
        tester.submit("Create");

        tester.assertTextPresent("HSP-17");
        assertTextSequence(new String[] { "CLONE - Test Issue", "Due", "Created", "Updated"});
        assertTextNotPresent("Resolved");
    }
}
