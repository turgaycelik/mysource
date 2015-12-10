package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import org.apache.commons.lang.StringUtils;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.PERMISSIONS,
        Category.PROJECTS , Category.ISSUE_TYPES, Category.SCREENS, Category.FIELDS, Category.CUSTOM_FIELDS,
        Category.SECURITY})
public class TestDeleteProject extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestDeleteProject.xml");
    }

    public void testDeleteProjectNoPermission()
    {
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);

        tester.gotoPage("secure/project/DeleteProject.jspa?pid=10000&confirm=true&returnUrl=ViewProjects.jspa");
        tester.assertTextPresent("Welcome to jWebTest JIRA installation");

        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //check the project still exists.
        navigation.gotoAdminSection("view_projects");
        tester.assertTextPresent("homosapien");
    }

    public void testDeleteProject()
    {
        administration.restoreData("TestDeleteProjectEnterprise.xml");

        navigation.gotoAdminSection("view_projects");
        tester.assertTextPresent("homosapien");

        //check the issues exist before deletion
        navigation.issue().viewIssue("HSP-1");
        tester.assertTextPresent("Test issue 1");
        navigation.issue().viewIssue("HSP-2");
        tester.assertTextPresent("JIRA needs to be more Web 2.0");

        WorkflowSchemeData draft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound("homosapien");

        navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10000");
        tester.assertTextPresent("Delete Project: homosapien");
        tester.submit("Delete");

        //check the project has been deleted
        tester.assertTextNotPresent("homosapien");

        //now go through all the related stuff and check the projct is gone (permission schemes, etc)
        navigation.gotoAdminSection("notification_schemes");
        tester.assertTextPresent("Notification Schemes");
        tester.assertTextPresent("The table below shows the notification schemes currently configured for this server");
        tester.assertTextNotPresent("homosapien");

        navigation.gotoAdminSection("permission_schemes");
        tester.assertTextPresent("Permission Schemes");
        tester.assertTextPresent("Permission Schemes allow you to create a set of permissions and apply this set of permissions to any project.");
        tester.assertTextNotPresent("homosapien");

        //custom fields
        navigation.gotoAdminSection("view_custom_fields");
        tester.assertTextPresent("Custom Fields");
        tester.assertTextNotPresent("homosapien");
        tester.assertTextPresent("Not configured for any context");

        //now lets check the issues have been deleted
        navigation.issue().viewIssue("HSP-1");
        tester.assertTextPresent("The issue you are trying to view does not exist.");
        navigation.issue().viewIssue("HSP-2");
        tester.assertTextPresent("The issue you are trying to view does not exist.");

        //issue sec schemes
        navigation.gotoAdminSection("security_schemes");
        tester.assertTextPresent("Issue Security Schemes allow you to control who can and cannot view issues.");
        tester.assertTextNotPresent("homosapien");

        //workflow schemes
        WorkflowSchemeData customerWorflowScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("Customer workflow");
        assertNotNull(customerWorflowScheme);
        assertFalse(customerWorflowScheme.isActive());

        WorkflowSchemeData copyOfCustomerWorkflowScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("Copy of Customer workflow");
        assertNotNull(copyOfCustomerWorkflowScheme);
        assertFalse(copyOfCustomerWorkflowScheme.isActive());
        assertEquals("(This copy was automatically generated from a draft, when workflow scheme 'Customer workflow' was made inactive.)", copyOfCustomerWorkflowScheme.getDescription());
        assertEquals(draft.getMappings(), copyOfCustomerWorkflowScheme.getMappings());

        assertNoDraft("homosapien");

        //field configuration schemes
        navigation.gotoAdminSection("issue_fields");
        tester.assertTextPresent("The table below shows the current Field Configuration Schemes and the projects they are associated with.");
        tester.assertTextNotPresent("homosapien");

        //issue type screen schemes
        navigation.gotoAdminSection("issue_type_screen_scheme");
        text.assertTextPresent(locator.css("h2"), "Issue Type Screen Schemes");
        tester.assertTextNotPresent("homosapien");
    }

    public void testDeleteProjectNotExists()
    {
        navigation.gotoAdmin();
        tester.gotoPage("secure/project/DeleteProject.jspa?pid=20000&confirm=true&returnUrl=ViewProjects.jspa&atl_token=" + page.getXsrfToken());
        tester.assertTextPresent("Delete Project");
        assertions.getJiraFormAssertions().assertFormErrMsg("Project with id '20,000' does not exist. Perhaps it was deleted?");
    }

    public void testDeleteProjectWithoutWorkflowSchemeDraft()
    {
        administration.restoreData("TestDeleteProjectEnterprise.xml");

        navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10010");
        tester.assertTextPresent("Delete Project: without draft WF scheme");
        tester.submit("Delete");

        //workflow schemes
        navigation.gotoAdminSection("workflow_schemes");
        tester.assertTextPresent("Workflow Schemes allow you to define which workflows apply to given issue types and projects.");
        tester.assertTextNotPresent("without draft WF scheme");
        tester.assertTextNotPresent("Copy of ");

        WorkflowSchemeData customerWorflowScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("without draft WF scheme");
        assertNull(customerWorflowScheme);

        WorkflowSchemeData copyOfCustomerWorkflowScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("Copy of without draft WF scheme");
        assertNull(copyOfCustomerWorkflowScheme);

        assertNoDraft("without draft WF scheme");
    }

    public void testDeleteProjectWithWorkflowSchemeDraftWithTheSameNameAsExistingWorkflowScheme()
    {
        administration.restoreData("TestDeleteProjectEnterprise.xml");

        testDraftCopyRenaming("Copy of Customer workflow", "Customer workflow", "Copy 2 of Customer workflow");
    }

    public void testDeleteProjectWithWorkflowSchemeDraftWithTheSameNameAsExistingWorkflowSchemeAbbreviating()
    {
        administration.restoreData("TestDeleteProjectEnterprise.xml");

        String existingSchemeName = "Copy of " + StringUtils.repeat("1", 244) + "...";
        String originalSchemeName = StringUtils.repeat("1", 255);
        String expectedCopyName = "Copy 2 of " + StringUtils.repeat("1", 242) + "...";

        backdoor.workflowSchemes().createScheme(new WorkflowSchemeData().setName(originalSchemeName));
        administration.project().associateWorkflowScheme("homosapien", originalSchemeName);
        administration.project().createWorkflowSchemeDraft("HSP");

        testDraftCopyRenaming(existingSchemeName, originalSchemeName, expectedCopyName);
    }

    private void testDraftCopyRenaming(String existingSchemeName, String originalSchemeName, String expectedCopyName)
    {
        // Create a workflow scheme that has the same name as expected draft workflow scheme copy.
        backdoor.workflowSchemes().createScheme(new WorkflowSchemeData().setName(existingSchemeName));

        WorkflowSchemeData draft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound("homosapien");

        navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10000");
        tester.assertTextPresent("Delete Project: homosapien");
        tester.submit("Delete");

        //workflow schemes
        WorkflowSchemeData originalScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound(originalSchemeName);
        assertNotNull(originalScheme);
        assertFalse(originalScheme.isActive());

        WorkflowSchemeData existingScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound(existingSchemeName);
        assertNotNull(existingScheme);
        assertFalse(existingScheme.isActive());

        WorkflowSchemeData copiedScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound(expectedCopyName);
        assertNotNull(copiedScheme);
        assertFalse(copiedScheme.isActive());
        assertEquals("(This copy was automatically generated from a draft, when workflow scheme '" + originalSchemeName + "' was made inactive.)", copiedScheme.getDescription());
        assertEquals(draft.getMappings(), copiedScheme.getMappings());

        assertNoDraft("homosapien");
    }

    public void testDeleteProjectWithWorkflowSchemeDraftUsedByAnotherProject()
    {
        administration.restoreData("TestDeleteProjectEnterprise.xml");

        administration.project().associateWorkflowScheme("monkey", "Customer workflow");

        WorkflowSchemeData draftBeforeDelete = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound("homosapien");

        navigation.gotoAdminSection("view_projects");
        tester.clickLink("delete_project_10000");
        tester.assertTextPresent("Delete Project: homosapien");
        tester.submit("Delete");

        WorkflowSchemeData copyOfCustomerWorkflowScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("Copy of Customer workflow");
        assertNull(copyOfCustomerWorkflowScheme);

        WorkflowSchemeData draftAfterDelete = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound("monkey");
        assertNotNull(draftAfterDelete);
        assertEquals(draftBeforeDelete.getId(), draftAfterDelete.getId());
        assertEquals(draftBeforeDelete.getName(), draftAfterDelete.getName());
        assertEquals(draftBeforeDelete.getDescription(), draftAfterDelete.getDescription());
        assertEquals(draftBeforeDelete.getMappings(), draftAfterDelete.getMappings());
    }

    private void assertNoDraft(String projectName)
    {
        WorkflowSchemeData draft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound(projectName);
        assertNull(draft);
    }
}
