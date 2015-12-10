package com.atlassian.jira.webtests.ztests.subtask.move;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SUB_TASKS })
public class TestMoveSubTaskIssueType extends JIRAWebTest
{
    public TestMoveSubTaskIssueType(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestMoveSubTaskIssueType.xml");
    }

    public void tearDown()
    {
        getAdministration().restoreBlankInstance();
        super.tearDown();
    }

    public void testHappy()
    {
        gotoIssue("HSP-2");
        clickLink("move-issue");
        assertRadioOptionPresent("operation", "move.subtask.type.operation.name");
        assertRadioOptionPresent("operation", "move.subtask.parent.operation.name");
        checkCheckbox("operation", "move.subtask.type.operation.name");
        submit("Next >>");
        assertTextPresent("Sub-task 2");
        assertTextNotPresent("Sub-task 3");
    }

    public void testNoOtherSubsInProject()
    {
        navigation.gotoAdmin();
        clickLink("subtasks");
        clickLink("del_Sub-task 2");
        submit("Delete");
        gotoIssue("HSP-2");
        clickLink("move-issue");
        assertRadioOptionNotPresent("operation", "move.subtask.type.operation.name");
        assertRadioOptionPresent("operation", "move.subtask.parent.operation.name");
        assertTextPresent("There are no other sub-task issue types associated with this project.");
        
    }

    public void testNoScheme()
    {
        Long projectId = backdoor.project().getProjectId("HSP");
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);

        checkCheckbox("createType", "chooseScheme");
        selectOption("schemeId", "Default Issue Type Scheme");
        submit(" OK ");
        gotoIssue("HSP-2");
        clickLink("move-issue");
        assertRadioOptionPresent("operation", "move.subtask.type.operation.name");
        assertRadioOptionPresent("operation", "move.subtask.parent.operation.name");
        checkCheckbox("operation", "move.subtask.type.operation.name");
        submit("Next >>");
        assertTextPresent("Sub-task 2");
        assertTextPresent("Sub-task 3");
    }

    public void testNoOtherSubsAtAll()
    {
        navigation.gotoAdmin();
        clickLink("subtasks");
        clickLink("del_Sub-task 2");
        submit("Delete");
        clickLink("del_Sub-task 3");
        submit("Delete");

        Long projectId = backdoor.project().getProjectId("HSP");
        tester.gotoPage("/secure/admin/SelectIssueTypeSchemeForProject!default.jspa?projectId=" + projectId);
        checkCheckbox("createType", "chooseScheme");
        selectOption("schemeId", "Default Issue Type Scheme");
        submit(" OK ");
        gotoIssue("HSP-2");
        clickLink("move-issue");
        assertRadioOptionNotPresent("operation", "move.subtask.type.operation.name");
        assertRadioOptionPresent("operation", "move.subtask.parent.operation.name");
        assertTextPresent("There are no other sub-task issue types defined in the system.");

    }
}
