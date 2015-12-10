package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;

import static com.atlassian.jira.functest.framework.navigator.IssueTypeCondition.IssueType;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW, Category.ISSUES, Category.CLONE_ISSUE })
public class TestCloneIssueWithValidation extends FuncTestCase
{

    private static final String TEST_WORKFLOW_NAME = "TEST_WORKFLOW";
    private static final String TEST_PROJECT_NAME = "TEST_PROJECT";
    private static final String TEST_PROJECT_KEY = "TEST";
    private static final String ORIGINAL_ISSUE_SUMMARY = "OriginalIssue";
    private static final String DEFAULT_USERNAME_PREFIX = "steve";
    private static final String PERMISSION_REQUIRED_WORKFLOW_VALIDATOR = "com.atlassian.jira.plugin.system.workflow:permission-validator";
    public static final String WORKFLOW_SCHEME_NAME = "SCHEME_NAME";
    public static final String WORKFLOW_SCHEME_DESC = "This is created intentionally for testing";

    private Long projectId;
    private String originalIssueId;
    private Long workflowSchemeId;

    @Override
    protected void tearDownTest()
    {
        super.tearDownTest();
        loginAsAdmin();
        administration.usersAndGroups().deleteUser(DEFAULT_USERNAME_PREFIX);
        administration.project().deleteProject(projectId);
        backdoor.workflowSchemes().deleteScheme(workflowSchemeId);
        administration.workflows().goTo().delete(TEST_WORKFLOW_NAME);
    }

    public void testShouldStopCloneOperation_IfThereIsAnyValidationFailureInCreateTransition()
    {
        if (administration.usersAndGroups().userExists(DEFAULT_USERNAME_PREFIX) == false)
        {
            createUsers(DEFAULT_USERNAME_PREFIX);
        }
        givenPermissionRequiredValidatorInCreateTransition();
        createIssueWhichWillBeClonedLater();

        loginAsUserWithoutAdministratorPermission();
        goToIssue(originalIssueId).andClone();

        assertErrorMessageShownUp().goToIssue(originalIssueId).assertSelfReferenceCloneLinkNotPresent();

    }

    private void givenPermissionRequiredValidatorInCreateTransition()
    {
        loginAsAdmin();
        administration.workflows().goTo().copyWorkflow("jira", TEST_WORKFLOW_NAME);
        projectId = backdoor.project().addProject(TEST_PROJECT_NAME, TEST_PROJECT_KEY, ADMIN_USERNAME);
        workflowSchemeId = backdoor.workflowSchemes().createScheme(newWorkflowScheme(WORKFLOW_SCHEME_NAME, WORKFLOW_SCHEME_DESC)).getId();
        backdoor.workflowSchemes().updateScheme(workflowScheme(workflowSchemeId).setDefaultWorkflow(TEST_WORKFLOW_NAME));
        backdoor.workflowSchemes().updateScheme(workflowScheme(workflowSchemeId).setMapping(IssueType.NEW_FEATURE.getName(), TEST_WORKFLOW_NAME));

        administration.project().associateWorkflowScheme(TEST_PROJECT_NAME, WORKFLOW_SCHEME_NAME);
        administration.workflows().goTo().workflowInitialStep(TEST_WORKFLOW_NAME).createTransition();
        tester.clickLinkWithText("Add validator");
        tester.setWorkingForm(JIRA_FORM_NAME);
        tester.checkCheckbox("type", PERMISSION_REQUIRED_WORKFLOW_VALIDATOR);
        tester.submit("Add");
        tester.selectOption("permissionKey", "Administer Projects");
        tester.submit("Add");

        tester.clickLink("publish_draft_workflow");

        tester.setWorkingForm("publish-workflow");
        tester.checkRadioOption("enableBackup", "false");
        tester.submit("Publish");
    }

    private static WorkflowSchemeData newWorkflowScheme(String workflowSchemeName, String description)
    {
        WorkflowSchemeData newWorkflowScheme = new WorkflowSchemeData();
        newWorkflowScheme.setActive(true);
        newWorkflowScheme.setDescription(description);
        newWorkflowScheme.setName(workflowSchemeName);
        return newWorkflowScheme;
    }

    private WorkflowSchemeData workflowScheme(Long id)
    {
        return backdoor.workflowSchemes().getWorkflowScheme(id);
    }

    private void createIssueWhichWillBeClonedLater()
    {
        loginAsAdmin();
        originalIssueId = navigation.issue().createIssue(TEST_PROJECT_NAME, IssueType.NEW_FEATURE.getName(), ORIGINAL_ISSUE_SUMMARY);
    }

    private void loginAsAdmin()
    {
        navigation.logout();
        navigation.login(ADMIN_USERNAME);
    }

    private void loginAsUserWithoutAdministratorPermission()
    {
        navigation.logout();
        navigation.login(DEFAULT_USERNAME_PREFIX);
    }

    private void createUsers(String... usernames)
    {
        if (usernames.length == 0)
        {
            createTenDefaultUsers();
        }
        else
        {
            for (String username : usernames)
            {
                administration.usersAndGroups().addUser(username);
            }
        }
    }

    private void createTenDefaultUsers()
    {
        for (int i = 1; i <= 10; i++)
        {
            administration.usersAndGroups().addUser(DEFAULT_USERNAME_PREFIX + i);
        }
    }

    private TestCloneIssueWithValidation goToIssue(String issueKey)
    {
        navigation.issue().gotoIssue(issueKey);
        return this;
    }

    private TestCloneIssueWithValidation andClone()
    {
        tester.clickLink(LINK_CLONE_ISSUE);
        tester.setWorkingForm("assign-issue");
        tester.getDialog().submit();
        return this;
    }

    private TestCloneIssueWithValidation assertSelfReferenceCloneLinkNotPresent()
    {
        tester.assertElementNotPresent("internal-" + backdoor.issues().getIssue(originalIssueId).id + "_10001");
        return this;
    }

    private TestCloneIssueWithValidation assertErrorMessageShownUp()
    {
        tester.assertTextInElement("assign-issue", "User '" + DEFAULT_USERNAME_PREFIX + "' doesn't have the 'Administer Projects' permission");
        return this;
    }
}
