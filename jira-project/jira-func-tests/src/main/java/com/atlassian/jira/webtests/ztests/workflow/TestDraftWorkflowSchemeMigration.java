package com.atlassian.jira.webtests.ztests.workflow;

import java.util.Collections;
import java.util.Map;

import com.atlassian.jira.functest.framework.backdoor.ProjectControlExt;
import com.atlassian.jira.functest.framework.backdoor.WorkflowsControlExt;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.testkit.client.WorkflowSchemesControl;

import com.google.common.collect.ImmutableMap;

import org.xml.sax.SAXException;

/**
 *
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestDraftWorkflowSchemeMigration extends AbstractTestDraftWorkflowSchemeMigration
{
    public TestDraftWorkflowSchemeMigration(String name)
    {
        super(name);
    }

    // JRADEV-16430: Make sure that a workflow's draft is moved from draft to workflow when the workflow becomes
    // inactive through a draft scheme migration associated with only one project. This test does a "quick migration",
    // i.e. one where the project has no issues.
    public void testDrafWorkflowRemovedWhenInactive()
    {
        final String workflowName = "WorkflowWithDraft";

        administration.restoreData("blankWithOldDefault.xml");

        //Create new workflow.
        final WorkflowsControlExt workflowControl = getBackdoor().workflow();
        workflowControl.createWorkflow(workflowName);

        //Create a scheme pointing to the new workflow.
        final WorkflowSchemesControl workflowSchemesControl = getBackdoor().workflowSchemes();
        final WorkflowSchemeData scheme = workflowSchemesControl.createScheme(new WorkflowSchemeData().setName(workflowName).setMapping("Bug", workflowName));

        //Create project using new scheme.
        final String projectKey = "TDWC";
        final String projectName = "testDrafWorkflowChanged";
        final ProjectControlExt projectControl = getBackdoor().project();
        projectControl.addProject(projectName, projectKey, "admin");
        projectControl.setWorkflowScheme(projectKey, scheme.getId());

        //Create a draft of the workflow now it is active.
        workflowControl.createDraftOfWorkflow(workflowName);

        //Create a draft workflow scheme that does not use the new workflow.
        workflowSchemesControl.createDraft(scheme);
        workflowSchemesControl.updateDraftScheme(scheme.getId(), new WorkflowSchemeData().setMapping("Bug", "jira"));

        //Migrate the project. No issues so this should be quick.
        Long schemeId = getBackdoor().workflowSchemes().getWorkflowSchemeByProjectKey(projectKey).getId();
        administration.project().publishWorkflowSchemeDraft(projectName, schemeId, Collections.<String, String>emptyMap(), true);

        WorkflowsControlExt.Workflow workflow = workflowControl.getWorkflowDetailed(workflowName);
        assertFalse("Workflow should no longer have a draft.", workflow.isHasDraft());

        //Make sure the draft is now a workflow in its own right.
        workflow = workflowControl.getWorkflowDetailed("Copy of " + workflowName);
        assertNotNull("Workflow draft has been made its own scheme?", workflow);
        assertEquals("Description should be valid.",
                String.format("(This copy was automatically generated from a draft, when workflow '%s' was made inactive.)", workflowName),
                workflow.getDescription());
    }

    // JRADEV-16430: Make sure that a workflow's draft is moved from draft to workflow when the workflow becomes
    // inactive through a draft scheme migration where the project has issues to migrate.
    public void testDrafWorkflowRemovedWhenInactiveWithIssues()
    {
        final String workflowName = "WorkflowWithDraft";

        administration.restoreData("blankWithOldDefault.xml");

        //Create new workflow.
        final WorkflowsControlExt workflowControl = getBackdoor().workflow();
        workflowControl.createWorkflow(workflowName);

        //Create a scheme pointing to the new workflow.
        final WorkflowSchemesControl workflowSchemesControl = getBackdoor().workflowSchemes();
        final WorkflowSchemeData scheme = workflowSchemesControl.createScheme(new WorkflowSchemeData().setName(workflowName).setMapping("Bug", workflowName));

        //Create project using new scheme.
        final String projectKey = "TDWC";
        final String projectName = "testDrafWorkflowChanged";
        final ProjectControlExt projectControl = getBackdoor().project();
        projectControl.addProject(projectName, projectKey, "admin");
        projectControl.setWorkflowScheme(projectKey, scheme.getId());

        //Create one issue so that we don't do a quick migration
        getBackdoor().issues().createIssue(projectKey, "some issue");

        //Create a draft of the workflow now it is active.
        workflowControl.createDraftOfWorkflow(workflowName);

        //Create a draft workflow scheme that does not use the new workflow.
        workflowSchemesControl.createDraft(scheme);
        workflowSchemesControl.updateDraftScheme(scheme.getId(), new WorkflowSchemeData().setMapping("Bug", "jira"));

        //Migrate the project. No issues so this should be quick.
        Long schemeId = getBackdoor().workflowSchemes().getWorkflowSchemeByProjectKey(projectKey).getId();
        administration.project().publishWorkflowSchemeDraft(projectName, schemeId, Collections.<String, String>emptyMap(), true);

        WorkflowsControlExt.Workflow workflow = workflowControl.getWorkflowDetailed(workflowName);
        assertFalse("Workflow should no longer have a draft.", workflow.isHasDraft());

        //Make sure the draft is now a workflow in its own right.
        workflow = workflowControl.getWorkflowDetailed("Copy of " + workflowName);
        assertNotNull("Workflow draft has been made its own scheme?", workflow);
        assertEquals("Description should be valid.",
                String.format("(This copy was automatically generated from a draft, when workflow '%s' was made inactive.)", workflowName),
                workflow.getDescription());
    }

    public void testWorkflowMigration()
    {
        getBackdoor().restoreDataFromResource("WorkflowSchemePublishingMigrationTest.xml");

        publishDraft();

        assertStandardIssues(TEST_PROJECT_KEY);

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
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
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestBrokenHalfWayDraft.xml");

        publishDraft();

        assertIssuesMigratedAndChangeHistory(TEST_PROJECT_KEY);

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
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
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestIssueWithUnupdatedIssueDraft.xml");

        publishDraft();

        assertIssuesMigratedAndChangeHistory(TEST_PROJECT_KEY);

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Restores data where a workflow migration dies
     * after migrating all issues but without saving the changes from the draft and deleting it. So we have a
     * project which is still associated with its 'old' workflow scheme But all issues have been migrated to workflows
     * in the new workflow scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithUnupdatedWorkflowScheme()
    {
        // Restore data where a workflow migration dies after migrating all issues but still leaving
        // behind the association to the old workflow scheme
        // So we have a project which is still associated with its 'old' workflow scheme
        // But all issues have been migrated to workflows in the new workflow scheme
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestUnchangedSchemeDraft.xml");

        publishDraft();

        assertIssuesAfterMigrationWithUnupdatedWorkflowScheme();

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
    }

    /**
     * A general workflow migration test that tests quite a few cases. The test begins with imported data containing
     * issues in Test project and Homosapien project and then migrates issues in Homosapien project from Homosapien
     * Source Scheme workflow scheme to Homosapien Source Scheme draft. The Homosapien Source Scheme and its draft
     * use different workflows for different issue types. Some issues in the Homosapien project are
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
     * do any migration (as the destination workflow is also Homospien Destination. However, the previously failed
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
        getBackdoor().restoreDataFromResource("WorkflowMigrationHalfMigratedDataNewDestinationDraft.xml");

        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep2!default.jspa?draftMigration=true&projectId=10010&schemeId=10000");

        assertTextPresent("Step 1 of 2");
        assertTextPresent("The current status of each issue needs to be changed so that it is compatible with the new workflows.");
        text.assertTextPresent(locator.css("title"), "Publish Workflows");
        text.assertTextPresent(locator.css("header h2"), "Publish Workflows");

        assertTextPresent("Affected issues: 4 of 8");
        assertTextPresent("Affected issues: 8 of 16");
        assertTextPresent("Affected issues: 2 of 8");

        assertMappingAndSelectOptionsForHalfMigratedDataNewDestination();

        // Associate the new scheme with the project
        submit("Associate");

        // Ensure that the project has been associated with the Destination Workflow scheme
        administration.project().waitForWorkflowMigration(1000, 100);

        assertIssuesInHomosapienProjectAfterHalfMigratedDataNewDestination();

        checkIssuesInTestProjectAfterHalfMigratedDataNewDestination();

        assertScheme(HOMOSAPIEN_PROJECT_NAME, "Homosapien Source Scheme", "The original workflow scheme of the Homosapien project",
                ImmutableMap.of("Custom Issue Type", WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION, "Improvement", JIRA_DEFAULT_WORKFLOW),
                WORKFLOW_HOMOSPIEN_DESTINATION);

        assertNoDraft(HOMOSAPIEN_PROJECT_NAME);
    }

    /**
     * Ensure the issue verifier detects problems before issues are migrated through workflow
     */
    public void testIssueVerifierError()
    {
        // Import data where:
        // TST-2 - does not have a workflow id
        // TST-3 - has an invlid status (with id 30)
        // TST-6 - does not have a workflow id
        // TST-7 - has invalid issue type (with id 10)
        // TST-9 - has invalid issue type (with id 14)
        // TST-11 - has invalid status (with id 20)
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestIssueVerifierDraft.xml");

        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        Long schemeId = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(TEST_PROJECT_KEY).getId();
        administration.project().publishWorkflowSchemeDraft(TEST_PROJECT_NAME, schemeId, statusMapping, false);

        waitForFailedMigration();

        assertIssueVerifierErrorMessages(TEST_PROJECT_KEY);

        assertDraft(TEST_PROJECT_NAME);

        assertScheme(TEST_PROJECT_NAME, SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_DESCRIPTION,
                SOURCE_SCHEME_WORKFLOW_MAPPING, SOURCE_WORKFLOW_1);
    }

    /**
     * Tests that 2 users can look at the same task and get their proper view of it, eg the starter can acknowledge and
     * the other person cant
     */
    public void testMultiAdminTaskProgressFlow()
    {
        getBackdoor().restoreDataFromResource("WorkflowMigrationTwoAdminsDraft.xml");

        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        Long schemeId = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(TEST_PROJECT_KEY).getId();
        administration.project().publishWorkflowSchemeDraft(TEST_PROJECT_NAME, schemeId, statusMapping, false);

        assertMultiAdminTaskProgressFlow(TEST_PROJECT_ID);
    }
}
