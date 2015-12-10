package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.backdoor.ProjectControlExt;
import com.atlassian.jira.functest.framework.backdoor.WorkflowsControlExt;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.testkit.client.WorkflowSchemesControl;
import org.xml.sax.SAXException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowMigration extends AbstractTestWorkflowSchemeMigration
{

    public TestWorkflowMigration(String name)
    {
        super(name);
    }

    public void testDrafWorkflowRemovedWhenInactive()
    {
        getBackdoor().restoreBlankInstance();

        //Create new workflow.
        final String workflowName = "WorkflowWithDraft";
        final WorkflowsControlExt workflowControl = getBackdoor().workflow();
        workflowControl.createWorkflow(workflowName);

        //Create a scheme pointing to the new workflow.
        final WorkflowSchemesControl workflowSchemesControl = getBackdoor().workflowSchemes();
        final WorkflowSchemeData scheme = workflowSchemesControl.createScheme(new WorkflowSchemeData().setName(workflowName).setMapping("Bug", workflowName));

        //Create project using new scheme.
        final String projectKey = "TDWC";
        final String projectName = "testDrafWorkflowRemovedWhenInactive";
        final ProjectControlExt projectControl = getBackdoor().project();
        projectControl.addProject(projectName, projectKey, "admin");
        projectControl.setWorkflowScheme(projectKey, scheme.getId());

        //Create a draft of the workflow now it is active.
        workflowControl.createDraftOfWorkflow(workflowName);

        //Create a new target scheme to migrate to.
        final String targetSchemeName = workflowName + "Target";
        workflowSchemesControl.createScheme(new WorkflowSchemeData().setName(targetSchemeName).setMapping("Bug", "jira"));

        //Migrate the project. No issues so this sould be quick.
        administration.project().associateWorkflowScheme(projectName, targetSchemeName, Collections.<String, String>emptyMap(), true);

        WorkflowsControlExt.Workflow workflow = workflowControl.getWorkflowDetailed(workflowName);
        assertFalse("Workflow should no longer have a draft.", workflow.isHasDraft());

        //Make sure the draft is now a workflow in its own right.
        workflow = workflowControl.getWorkflowDetailed("Copy of " + workflowName);
        assertNotNull("Workflow draft has been made its own scheme?", workflow);
        assertEquals("Description should be valid.",
                String.format("(This copy was automatically generated from a draft, when workflow '%s' was made inactive.)", workflowName),
                workflow.getDescription());
    }

    /**
     * Tests simple workflow migration
     */
    public void testWorkflowMigration()
    {
        // Restore clean data to perform a workflow migration
        getBackdoor().restoreDataFromResource("WorkflowMigrationTest.xml");

        associateScheme();

        assertStandardIssues(TEST_PROJECT_KEY);
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Imports a project which is still associated with
     * its 'old' workflow scheme but issues TST-1, TST-2, TST-5 and TST-10 have been migrated to new workflows in the
     * new workflow scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationHalfMigratedData()
    {
        // Restore data where a workflow migration dies half way
        // So we have a project which is still associated with its 'old' workflow scheme
        // But issues TST-1, TST-2, TST-5 and TST-10 have been migrated to new workflows in the new workflow scheme
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestBrokenHalfWay.xml");

        associateScheme();

        // Ensure the issues have been migrated to the workflow properly
        assertIssuesMigratedAndChangeHistory(TEST_PROJECT_KEY);
    }

    /**
     * Tests workflow migration with previously failed migtaion. <p/> Restore data where a workflow migration migrates
     * issues TST-1, TST-2, TST-5 and TST-10 then creates a new wf entry for TST-4, marks an existing wf entry as
     * killed, creates a new current step, removes the old one, and then dies. So we have a project which is still
     * associated with its 'old' workflow scheme But some issues have been migrated to workflows in the new workflow
     * scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithUnupdatedIssue()
    {
        // Restore data where a workflow migration migrates issues TST-1, TST-2, TST-5 and TST-10 then
        // creates a new wf entry for TST-4, marks an existing wf entry as killed, creates a new current step, removes the
        // old one, and then dies.
        // So we have a project which is still associated with its 'old' workflow scheme
        // But some issues have been migrated to workflows in the new workflow scheme
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestIssueWithUnupdatedIssue.xml");

        associateScheme();

        // Ensure the issues ahave been migarted to the workflow properly
        assertIssuesMigratedAndChangeHistory(TEST_PROJECT_KEY);
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Restores data where a workflow migration dies
     * after migrating all issues but still leaving behind the association to the old workflow scheme So we have a
     * project which is still associated with its 'old' workflow scheme But all issues have been migrated to workflows
     * in the new workflow scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithUnupdatedWorkflowScheme()
    {
        // Restore data where a workflow migration dies after migrating all issues but still leaving
        // behind the association to the old workflow scheme
        // So we have a project which is still associated with its 'old' workflow scheme
        // But all issues have been migrated to workflows in the new workflow scheme
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestUnchangedScheme.xml");

        associateScheme();

        assertIssuesAfterMigrationWithUnupdatedWorkflowScheme();
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Restores data where a workflow migration migrates
     * all issues, removes the old workflow scheme association from a project and dies without adding an association
     * with new workflow scheme So we have a project which is not associated with any workflow schemes, and JIRA will
     * think that the project is using the default jira workflow. </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithRemovedWorkflowScheme()
    {
        // Restore data where a workflow migration migrates all issues, removes the old
        // workflow scheme association from a project and dies without adding an association
        // with new workflow scheme
        // So we have a project which is not associated with any workflow schemes, and JIRA will think that the
        // project is using the default jira workflow.
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestRemovedWorkflowScheme.xml");

        // All of the issues have been migrated to new workflows, the association from the old
        // workflow scheme has been removed, but the association to the new workflow scheme has not been added.
        // So JIRA thinks that the project is using the default JIRA workflow. So provide the following status mappings:
        Map<String, String> statusMapping = new HashMap<String, String>();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved status
        statusMapping.put("mapping_1_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_1_6", CUSTOM_STATUS_4);
        // - For Reopened status - select Custom Status 3
        statusMapping.put("mapping_1_4", CUSTOM_STATUS_3);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved status
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);
        // - For Reopened status - select Custom Status 3
        statusMapping.put("mapping_4_4", CUSTOM_STATUS_3);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved status
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);
        // - For Reopened status - select Custom Status 3
        statusMapping.put("mapping_2_4", CUSTOM_STATUS_3);

        associateScheme(statusMapping);

        // Ensure the issues have been migrated to the workflow properly

        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.

        // TST-1, Task, has already been migrated from Source Workflow 1 to the default workflow.
        // This issue should not be migrated again as all Tasks in the project use the default workflow (due to the previous migration), and as no scheme
        // is associated with the project, the Tasks do not need to be migarted again
        // So assert that the last change item is how it was from the previous workflow migration
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("TST-1"), false);

        // TST-2, Task, has already been migrated from Source Workflow 1 to the default workflow.
        // This issue should not be migrated again as all Tasks in the project use the default workflow (due to the previous migration), and as no scheme
        // is associated with the project, the Tasks do not need to be migarted again
        // So assert that the last change item is how it was from the previous workflow migration
        new IssueAssertions().status(STATUS_OPEN)
                .addTransitions(TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE)
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("TST-2"), false);

        // TST-3, Improvement, has already been migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        // Should be migrated again and stay in Resolved status.
        // As there is no association with the project the change item will have JIRA_DEFAULT_WORKFLOW as the source workflow
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-3"), false);

        // TST-4, Bug, has alreaddy been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        // As there is no association with the project the change item will have "jira" as the source workflow
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-4"), false);

        // TST-5, Bug, has already been migrated from from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        // As there is no association with the project the change item will have "jira" as the source workflow
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-5"), false);

        // TST-6, Bug, has already been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        // As there is no association with the project the change item will have "jira" as the source workflow
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-6"), false);

        // TST-7, New Feature, has already been migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        // As there is no association with the project the change item will have "jira" as the source workflow
        new IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Go 3")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-7"), false);

        // TST-8, Improvement, has already been migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        // This issue should be migrated again and stay in Resolved status
        // As there is no association with the project the change item will have "jira" as the source workflow
        new IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Go 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-8"), false);

        // TST-9, Task, has already been migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        // This issue should not be migrated again as all Tasks in the project use the default workflow (due to the previous migration), and as no scheme
        // is associated with the project, the Tasks do not need to be migarted again
        // So assert that the last change item is how it was from the previous workflow migration
        new IssueAssertions().status(CLOSED_STATUS_NAME)
                .addTransitions("Reopen Issue")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, SOURCE_WORKFLOW_1, JIRA_DEFAULT_WORKFLOW))
                .assertIssue(getIssue("TST-9"), false);

        // TST-10, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        // As there is no association with the project the change item will have "jira" as the source workflow
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-10"), false);

        // TST-11, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        // As there is no association with the project the change item will have "jira" as the source workflow
        new IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, JIRA_DEFAULT_WORKFLOW, DESTINATION_WORKFLOW))
                .assertIssue(getIssue("TST-11"), false);
    }

    private void associateScheme()
    {
        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        associateScheme(statusMapping);
    }

    private void associateScheme(Map<String, String> statusMapping)
    {
        // Try to migrate the project again
        associateWorkFlowSchemeToProject(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME);
    }

    /**
     * A general workflow migration test that tests quite a few cases. The test begins with imported data containing
     * issues in Test project and Homosapien project and then migrates issues in Homosapien project from Homosapien
     * Source Scheme workflow scheme to Homosapien Destination Scheme. The Homosapien Source Scheme and Homosapien
     * Destination Scheme use different workflows for different issue types. Some issues in the Homosapien project are
     * also 'broken' due to a previously failed workflow migration.
     * <p/>
     * <p/>
     * The state of issues in the Homospaien project and where they are being migrated is discussed below:
     * <p/>
     * <dl> <dt>Bugs</dt> <dd> Some of the Bugs have been migrated from Homosapien Source 1 workflow to Homospien
     * Destination Failure workflow. The migration failed before completion. When project workflow migration failed it
     * left some issues using Homospien Destination Failure and some issues using the old (Homosapien Source 1)
     * workflow. The data is in the state such that the issues that are using the Homospien Destination Failure workflow
     * are in statuses that do not exist in the Homosapien Source 1 workflow. These statuses are: <ul> <li>In
     * Progress</li> <li>Closed</li> <li>Custom Status 4</li> </ul>
     * <p/>
     * Bugs will be migrated to a completely new workflow - Homospien Destination. That is, not the same workflow to
     * which migration failed (Homospien Destination Failure). <br /><br />
     * <p/>
     * <p/>
     * <dt>Improvements</dt> <dd> Improvements are being migrated from Homosapien Source 2 to the default jira workflow.
     * <br /><br /> </dd>
     * <p/>
     * <dt>New Features</dt> <dd> New Features are already using Homospien Destination workflow, so they should not be
     * migrated at all. <br /><br /> </dd>
     * <p/>
     * <dt>Tasks</dt> <dd> The source workflow for the Task is 'Homospien Destination' so normally we would not need to
     * do any migration (as the destination workflow is also Homospien Destination. However, the previous ly failed
     * migration has moved some of the Tasks to the Homospien Destination Failure workflow. These issues need to be
     * moved (back) to the 'Homospien Destination' workflow. Hence we will need to get mapping for statuses: <ul> <li>In
     * Progress</li> <li>Closed</li> </ul>
     * <p/>
     * which exist in Homospien Destination Failure workflow, but do not exist in Homospien Destination workflow. <br
     * /><br /> </dd>
     * <p/>
     * <dt>Sub-Tasks</dt> <dd> Sub-Tasks are using the Homosapien Source 1 workflow and are being migrated to the
     * Homospien Destination workflow. There are no broken Sub-Tasks. <br /><br /> </dd>
     * <p/>
     * <dt>Custom Issue Type</dt> <dd> Issues of Custom Issue Type are moving between Homosapien Custom Issue Type
     * Source workflow and Homosapien Custom Issue Type Destination. All the statuses in Custom Issue Type Source
     * workflow are in Homosapien Custom Issue Type Destination as well, so no status mapping should appear for Custom
     * Issue Type issues </dd> <dl>
     * <p/>
     * The test also ensures that while migrating issues for the Homosapien project, the issues in Test project were not
     * touched.
     */
    public void testWorkflowMigrationHalfMigratedDataNewDestination() throws SAXException
    {
        // Restore data where some issues are using new workflow
        getBackdoor().restoreDataFromResource("WorkflowMigrationHalfMigratedDataNewDestination.xml");

        gotoPage("/secure/project/SelectProjectWorkflowScheme!default.jspa?projectId=10010");

        assertTextNotPresent("There are currently no workflow schemes setup.");
        getAssertions().getLinkAssertions().assertLinkNotPresentWithExactText("//div[@class='aui-page-panel-main']", "Add");

        String destinationSchemeName = "Homosapien Destination Scheme";
        selectOption("schemeId", destinationSchemeName);

        submit("Associate");

        assertTextPresent("Step 2 of 3");

        assertTextPresent("Affected issues: 4 of 8");
        assertTextPresent("Affected issues: 8 of 16");
        assertTextPresent("Affected issues: 2 of 8");

        assertMappingAndSelectOptionsForHalfMigratedDataNewDestination();

        // Associate the new scheme with the project
        submit("Associate");

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(HOMOSAPIEN_PROJECT_NAME, destinationSchemeName);

        assertIssuesInHomosapienProjectAfterHalfMigratedDataNewDestination();

        checkIssuesInTestProjectAfterHalfMigratedDataNewDestination();
    }

    /**
     * Ensure the issue verifier detects problems before issues are migrated through workflow
     */
    public void testIssueVerifier()
    {
        // Import data where:
        // TST-2 - does not have a workflow id
        // TST-3 - has an invlid status (with id 30)
        // TST-6 - does not have a workflow id
        // TST-7 - has invalid issue type (with id 10)
        // TST-9 - has invalid issue type (with id 14)
        // TST-11 - has invalid status (with id 20)
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestIssueVerifier.xml");

        // Make an association between the workflow and the project
        administration.project().associateWorkflowScheme(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME,
                Collections.<String, String>emptyMap(), false);

        waitForFailedMigration();

        assertIssueVerifierErrorMessages(TEST_PROJECT_KEY);
    }

    public void testMultipleActiveWorkflowErrorMessageNotPresentInEnterprise()
    {
        getBackdoor().restoreDataFromResource("WorkflowMigrationTest.xml");
        gotoPage("secure/admin/workflows/ListWorkflows.jspa");
        assertTextNotPresent("An error has occured during workflow activation and the result is multiple active workflows");
    }

    /**
     * Tests that 2 users can look at the same task and get their proper view of it, eg the starter can acknowledge and
     * the other person cant
     */
    public void testMultiAdminTaskProgressFlow()
    {
        getBackdoor().restoreDataFromResource("WorkflowMigrationTwoAdmins.xml");

        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        // Try to migrate the project again
        administration.project().associateWorkflowScheme(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping, false);

        // ok find out what the task id is
        long taskId = getSubmittedTaskId();

        waitForTaskAcknowledgement(taskId);
        assertTextPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextNotPresent("input type=\"submit\" name=\"Done\"");
        validateProgressBarUI(ACKNOWLEDGE);

        // ok connect as another user and have a look at the task
        navigation.logout();
        navigation.login("admin2", "admin2");
        navigation.gotoAdmin();
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId + "&schemeId=10001");

        validateProgressBarUI(DONE);
        assertTextNotPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextPresent("input type=\"submit\" name=\"Done\"");

        // ok go back and acknowledge as the task starter
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.gotoAdmin();
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId + "&schemeId=10001");

        assertTextPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextNotPresent("input type=\"submit\" name=\"Done\"");
        validateProgressBarUI(ACKNOWLEDGE);

        submit(ACKNOWLEDGE);

        // now the task should be cleaned up
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId + "&schemeId=10001");
        assertTextPresent("The task could not be found. Perhaps it has finished and has been acknowledged?");
        assertTextPresent("input type=\"submit\" name=\"Done\"");
    }

    public void testMigrateSchemeWithDraft()
    {
        backdoor.restoreDataFromResource("WorkflowMigrationTestWithDrafts.xml");

        WorkflowSchemeData draft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound(HOMOSAPIEN_PROJECT_NAME);

        administration.project().associateWorkflowScheme(HOMOSAPIEN_PROJECT_NAME, "Two");

        WorkflowSchemeData copiedScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("Copy of One");
        assertNotNull(copiedScheme);
        assertFalse(copiedScheme.isActive());
        assertEquals("(This copy was automatically generated from a draft, when workflow scheme 'One' was made inactive.)", copiedScheme.getDescription());
        assertEquals(draft.getMappings(), copiedScheme.getMappings());

        administration.project().associateWorkflowScheme("monkey", "One");

        draft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound("monkey");
        assertNull(draft);
    }

    public void testMigrateSchemeWithDraftUsedByAnotherProject()
    {
        backdoor.restoreDataFromResource("WorkflowMigrationTestWithDrafts.xml");

        administration.project().associateWorkflowScheme("monkey", "One");

        WorkflowSchemeData homosapienDraft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound(HOMOSAPIEN_PROJECT_NAME);

        administration.project().associateWorkflowScheme(HOMOSAPIEN_PROJECT_NAME, "Two");

        WorkflowSchemeData copiedScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("Copy of One");
        assertNull(copiedScheme);

        WorkflowSchemeData monkeyDraft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound("monkey");
        assertNotNull(monkeyDraft);
        assertEquals(monkeyDraft.getName(), homosapienDraft.getName());
        assertEquals(monkeyDraft.getDescription(), homosapienDraft.getDescription());
        assertEquals(monkeyDraft.getMappings(), homosapienDraft.getMappings());
    }

    public void testMigrateSchemeWithDraftWithTheSameNameAsExistingWorkflowScheme()
    {
        backdoor.restoreDataFromResource("WorkflowMigrationTestWithDrafts.xml");

        // Create a workflow scheme that has the same name as expected draft workflow scheme copy.
        backdoor.workflowSchemes().createScheme(new WorkflowSchemeData().setName("Copy of One"));

        WorkflowSchemeData draft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound(HOMOSAPIEN_PROJECT_NAME);

        administration.project().associateWorkflowScheme(HOMOSAPIEN_PROJECT_NAME, "Two");

        WorkflowSchemeData copiedScheme = backdoor.workflowSchemes().getWorkflowSchemeByNameNullIfNotFound("Copy 2 of One");
        assertNotNull(copiedScheme);
        assertFalse(copiedScheme.isActive());
        assertEquals("(This copy was automatically generated from a draft, when workflow scheme 'One' was made inactive.)", copiedScheme.getDescription());
        assertEquals(draft.getMappings(), copiedScheme.getMappings());

        administration.project().associateWorkflowScheme("monkey", "One");

        draft = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound("monkey");
        assertNull(draft);
    }
}
