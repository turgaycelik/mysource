package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

import org.apache.oro.text.regex.MalformedPatternException;

import static com.atlassian.jira.permission.ProjectPermissions.RESOLVE_ISSUES;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowConditions extends JIRAWebTest
{
    public TestWorkflowConditions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreBlankInstance();
    }

    public void testProjectRoleWorkflowCondition() throws MalformedPatternException
    {
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira").textView().goTo();
        tester.clickLinkWithText("Start Progress");
        tester.clickLinkWithText("Add condition", 0);
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuserinprojectrole-condition");
        tester.submit("Add");
        tester.selectOption("jira.projectrole.id", "Users");
        tester.submit("Add");
        assertTextSequence(new String[] { "Only users in project role ", "Users", " can execute this transition." });

        tester.clickLink("workflow-steps");
        tester.clickLinkWithText("Stop Progress");
        tester.clickLinkWithText("Add condition", 0);
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuserinprojectrole-condition");
        tester.submit("Add");
        tester.selectOption("jira.projectrole.id", "Developers");
        tester.submit("Add");
        assertTextSequence(new String[] { "Only users in project role ", "Developers", " can execute this transition." });

        tester.gotoPage("/secure/admin/workflows/EditWorkflowTransitionConditionParams!default.jspa?workflowStep=1&workflowTransition=4&count=2&workflowName=Copy+of+jira&workflowMode=live");
        tester.selectOption("jira.projectrole.id", "Administrators");
        tester.submit("Update");
        assertTextSequence(new String[] { "Only users in project role ", "Administrators", " can execute this transition." });

        tester.clickLinkWithText("Delete");
        tester.clickLink("project_role_browser");
        tester.setFormElement("name", "");
        tester.clickLink("view_Administrators");
        tester.clickLinkWithText("Start Progress");
        assertTextSequence(new String[] { "Only users in project role ", "Administrators", " can execute this transition." });

    }

    public void testGroupCFAddCondition()
    {

        // Add a CF group picker called CF_GP1 and CF_GP2
        createGroupPickerCF("CF_GP1");
        createGroupPickerCF("CF_GP2");

        // add a WF with a Group CF
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira").textView().goTo();
        tester.clickLinkWithText("Start Progress");
        tester.clickLinkWithText("Add condition", 0);
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuseringroupcf-condition");
        tester.submit("Add");

        // assert "groupcf" field has CF_GRP1 in it <option value="customfield_10000">CF_GP1</option>
        tester.assertTextPresent("<select name=\"groupcf\">");
        tester.assertTextPresent("<option value=\"customfield_10000\"");
        tester.assertTextPresent("CF_GP1");
        tester.assertTextPresent("<option value=\"customfield_10001\"");
        tester.assertTextPresent("CF_GP2");

        tester.submit("Add");

        // now edit it again
        tester.gotoPage("secure/admin/workflows/EditWorkflowTransitionConditionParams!default.jspa?workflowStep=1&workflowTransition=4&count=2&workflowName=Copy+of+jira&workflowMode=live");

        // assert "groupcf" field has CF_GRP1 in it <option value="customfield_10000">CF_GP1</option>
        tester.assertTextPresent("<select name=\"groupcf\">");
        tester.assertTextPresent("<option value=\"customfield_10000\"");
        tester.assertTextPresent("CF_GP1");
        tester.assertTextPresent("<option value=\"customfield_10001\"");
        tester.assertTextPresent("CF_GP2");
    }

    public void testPermissionCondition()
    {
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira").textView().goTo();
        tester.clickLinkWithText("Start Progress");
        tester.clickLinkWithText("Add condition", 0);
        tester.checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:permission-condition");
        tester.submit("Add");
        // Select 'Resolve Issues' from select box 'permission'.
        tester.selectOption("permissionKey", "Resolve Issues");
        tester.submit("Add");
        tester.assertTextPresent("Resolve Issues");
        tester.gotoPage("/secure/admin/workflows/EditWorkflowTransitionConditionParams!default.jspa?workflowMode=live&workflowStep=1&workflowTransition=4&count=2&workflowName=Copy+of+jira");
        tester.assertRadioOptionSelected("permissionKey", RESOLVE_ISSUES.permissionKey());
        tester.assertTextNotPresent("NullPointerException"); //JRA-15643

        // Select 'Create Issues' from select box 'permission'.
        tester.selectOption("permissionKey", "Modify Reporter");
        tester.submit("Update");
        // now check that the edit operation succeeded
        tester.assertTextNotPresent("Resolve Issues");
        tester.assertTextPresent("Modify Reporter");
    }

    private void createGroupPickerCF(String fieldName)
    {
        navigation.gotoAdmin();
        tester.clickLink("view_custom_fields");
        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:grouppicker");
        tester.submit("nextBtn");
        tester.setFormElement("fieldName", fieldName);
        tester.submit("nextBtn");
        tester.submit("Update");
    }
}
