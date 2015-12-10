package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableMap;
import org.xml.sax.SAXException;

import java.util.Map;

/**
 *
 * @since v5.2
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestDraftWorkflowSchemeMultipleProjectsMigration extends AbstractTestDraftWorkflowSchemeMigration
{
    public TestDraftWorkflowSchemeMultipleProjectsMigration(String name)
    {
        super(name);
    }

    public void testMigrateMultipleProjects()
    {
        getBackdoor().restoreDataFromResource("WorkflowSchemePublishingMultipleMigrationTest.xml");

        publishDraft();

        assertStandardIssues(TEST_PROJECT_KEY);
        assertStandardIssues(TEST_PROJECT_KEY_II);

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
        assertSchemeAndNoDraft(TEST_PROJECT_NAME_II);

        assertNoScheme(COPY_OF_SOURCE_WORKFLOW_SCHEME);

        assertSchemeIdChanged(SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_ID);
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Imports a project which is still associated with
     * its 'old' workflow scheme but issues TST-1, TST-2, TST-5 and TST-10 have been migrated to new workflows in the
     * new workflow scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationHalfMigratedDataInTheFirstProject()
    {
        // Restore data where a workflow migration dies half way
        // So we have a project which is still associated with its 'old' workflow scheme
        // But issues TST-1, TST-2, TST-5 and TST-10 have been migrated to new workflows in the new workflow scheme
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestBrokenHalfWayMultiple.xml");

        publishDraft();

        assertIssuesMigratedAndChangeHistory(TEST_PROJECT_KEY);
        assertStandardIssues(TEST_PROJECT_KEY_II);

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
        assertSchemeAndNoDraft(TEST_PROJECT_NAME_II);

        assertInactiveSchemeExists(COPY_OF_SOURCE_WORKFLOW_SCHEME, COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION);

        assertNoScheme(COPY_2_OF_SOURCE_WORKFLOW_SCHEME);

        assertSchemeIdChanged(SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_ID);
    }

    /**
     * Tests workflow migration with previously failed migtaion. <p/> Restore data where a workflow migration migrates
     * issues TST-1, TST-2, TST-5 and TST-10 then creates a new wf entry for TST-4, marks an existing wf entry as
     * killed, creates a new current step, removes the old one, and then dies. So we have a project which is still
     * associated with its 'old' workflow scheme But some issues have been migrated to workflows in the new workflow
     * scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithUnupdatedIssueInTheFirstProject()
    {
        // Restore data where a workflow migration migrates issues TST-1, TST-2, TST-5 and TST-10 then
        // creates a new wf entry for TST-4, marks an existing wf entry as killed, creates a new current step, removes the
        // old one, and then dies.
        // So we have a project which is still associated with its 'old' workflow scheme
        // But some issues have been migrated to workflows in the new workflow scheme
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestIssueWithUnupdatedIssueMultiple.xml");

        publishDraft();

        assertIssuesMigratedAndChangeHistory(TEST_PROJECT_KEY);
        assertStandardIssues(TEST_PROJECT_KEY_II);

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
        assertSchemeAndNoDraft(TEST_PROJECT_NAME_II);

        assertInactiveSchemeExists(COPY_OF_SOURCE_WORKFLOW_SCHEME, COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION);

        assertNoScheme(COPY_2_OF_SOURCE_WORKFLOW_SCHEME);

        assertSchemeIdChanged(SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_ID);
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Restores data where a workflow migration dies
     * after migrating all issues but still leaving behind the association to the old workflow scheme So we have a
     * project which is still associated with its 'old' workflow scheme But all issues have been migrated to workflows
     * in the new workflow scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithUnupdatedWorkflowSchemeInTheFirstProject()
    {
        // Restore data where a workflow migration dies after migrating all issues but still leaving
        // behind the association to the old workflow scheme
        // So we have a project which is still associated with its 'old' workflow scheme
        // But all issues have been migrated to workflows in the new workflow scheme
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestUnchangedSchemeMultiple.xml");

        publishDraft();

        assertIssuesAfterMigrationWithUnupdatedWorkflowScheme();

        assertStandardIssues(TEST_PROJECT_KEY_II);

        assertSchemeAndNoDraft(TEST_PROJECT_NAME);
        assertSchemeAndNoDraft(TEST_PROJECT_NAME_II);

        assertInactiveSchemeExists(COPY_OF_SOURCE_WORKFLOW_SCHEME, COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION);

        assertNoScheme(COPY_2_OF_SOURCE_WORKFLOW_SCHEME);

        assertSchemeIdChanged(SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_ID);
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
    public void testWorkflowMigrationHalfMigratedDataNewDestinationInTheFirstProject() throws SAXException
    {
        // Restore data where some issues are using new workflow
        getBackdoor().restoreDataFromResource("WorkflowMigrationHalfMigratedDataNewDestinationMultiple.xml");

        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep2!default.jspa?draftMigration=true&projectId=10010&schemeId=10000");

        assertTextPresent("Step 1 of 2");
        assertTextPresent("The current status of each issue needs to be changed so that it is compatible with the new workflows.");

        assertTextPresent("Affected issues: 6 of 12");
        assertTextPresent("Affected issues: 10 of 20");
        assertTextPresent("Affected issues: 2 of 12");

        assertMappingAndSelectOptionsForHalfMigratedDataNewDestination();

        // Associate the new scheme with the project
        submit("Associate");

        // Ensure that the project has been associated with the Destination Workflow scheme
        administration.project().waitForWorkflowMigration(1000, 100);

        assertIssuesInHomosapienProjectAfterHalfMigratedDataNewDestination();

        checkIssuesInTestIIProjectAfterHalfMigratedDataNewDestination();

        checkIssuesInTestProjectAfterHalfMigratedDataNewDestination();

        assertScheme(HOMOSAPIEN_PROJECT_NAME, "Homosapien Source Scheme", "The original workflow scheme of the Homosapien project",
                ImmutableMap.of("Custom Issue Type", WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION, "Improvement", JIRA_DEFAULT_WORKFLOW),
                WORKFLOW_HOMOSPIEN_DESTINATION);
        assertScheme(TEST_PROJECT_NAME_II, "Homosapien Source Scheme", "The original workflow scheme of the Homosapien project",
                ImmutableMap.of("Custom Issue Type", WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION, "Improvement", JIRA_DEFAULT_WORKFLOW),
                WORKFLOW_HOMOSPIEN_DESTINATION);

        assertNoDraft(HOMOSAPIEN_PROJECT_NAME);
        assertNoDraft(TEST_PROJECT_NAME_II);

        assertInactiveSchemeExists("Copy of Homosapien Source Scheme", "The original workflow scheme of the Homosapien project (This copy was automatically generated from a draft, as an intermediate scheme for migration)");

        assertNoScheme("Copy 2 of Homosapien Source Scheme");

        assertSchemeIdChanged("Homosapien Source Scheme", 10010L);
    }

    private void checkIssuesInTestIIProjectAfterHalfMigratedDataNewDestination()
    {
        new AbstractTestWorkflowSchemeMigration.IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3))
                .assertIssue(getIssue("TSTII-2"), false);

        new AbstractTestWorkflowSchemeMigration.IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue("TSTII-3"), false);

        new AbstractTestWorkflowSchemeMigration.IssueAssertions().status(IN_PROGRESS_STATUS_NAME)
                .addTransitions("Stop Progress", "Close Issue", "Resolve Issue")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, IN_PROGRESS_STATUS_NAME))
                .assertIssue(getIssue("TSTII-10"), false);

        new AbstractTestWorkflowSchemeMigration.IssueAssertions().status(RESOLVED_STATUS_NAME)
                .addTransitions("Close Issue", "Reopen Issue")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, JIRA_DEFAULT_WORKFLOW),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_3, RESOLVED_STATUS_NAME))
                .assertIssue(getIssue("TSTII-11"), false);

        new AbstractTestWorkflowSchemeMigration.IssueAssertions().status(CUSTOM_STATUS_3)
                .addTransitions("Go custom 4")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3))
                .assertIssue(getIssue("TSTII-20"), false);

        new AbstractTestWorkflowSchemeMigration.IssueAssertions().status(CUSTOM_STATUS_4)
                .addTransitions("Resolve")
                .addHistoryItems(item(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        item(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4))
                .assertIssue(getIssue("TSTII-21"), false);
    }

    /**
     * Ensure the issue verifier detects problems before issues are migrated through workflow
     */
    public void testIssueVerifierErrorInOneProject()
    {
        // Import data where:
        // TST-2 - does not have a workflow id
        // TST-3 - has an invlid status (with id 30)
        // TST-6 - does not have a workflow id
        // TST-7 - has invalid issue type (with id 10)
        // TST-9 - has invalid issue type (with id 14)
        // TST-11 - has invalid status (with id 20)
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestIssueVerifierMultipleErrorsInTst.xml");

        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        Long schemeId = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(TEST_PROJECT_KEY).getId();
        administration.project().publishWorkflowSchemeDraft(TEST_PROJECT_NAME, schemeId, statusMapping, false);

        waitForFailedMigration();

        assertIssueVerifierErrorMessages(TEST_PROJECT_KEY);

        assertDraft(TEST_PROJECT_NAME);

        assertStandardIssues(TEST_PROJECT_KEY_II);

        assertNoDraft(TEST_PROJECT_NAME_II);

        assertScheme(TEST_PROJECT_NAME, SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_DESCRIPTION,
                SOURCE_SCHEME_WORKFLOW_MAPPING, SOURCE_WORKFLOW_1);

        assertScheme(TEST_PROJECT_NAME_II, COPY_OF_SOURCE_WORKFLOW_SCHEME, COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION,
                DRAFT_WORKFLOW_MAPPING, DESTINATION_WORKFLOW);

        assertSchemeId(SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_ID);
    }

    /**
     * Ensure the issue verifier detects problems before issues are migrated through workflow
     */
    public void testIssueVerifierErrorInBothProjects()
    {
        // Import data where:
        // TST-2 - does not have a workflow id
        // TST-3 - has an invlid status (with id 30)
        // TST-6 - does not have a workflow id
        // TST-7 - has invalid issue type (with id 10)
        // TST-9 - has invalid issue type (with id 14)
        // TST-11 - has invalid status (with id 20)
        // TSTII-2 - does not have a workflow id
        // TSTII-3 - has an invlid status (with id 30)
        // TSTII-6 - does not have a workflow id
        // TSTII-7 - has invalid issue type (with id 10)
        // TSTII-9 - has invalid issue type (with id 14)
        // TSTII-11 - has invalid status (with id 20)
        getBackdoor().restoreDataFromResource("WorkflowMigrationTestIssueVerifierMultipleErrorsInBoth.xml");

        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        Long schemeId = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(TEST_PROJECT_KEY).getId();
        administration.project().publishWorkflowSchemeDraft(TEST_PROJECT_NAME, schemeId, statusMapping, false);

        waitForFailedMigration();

        assertIssueVerifierErrorMessages(TEST_PROJECT_KEY);
        assertIssueVerifierErrorMessages(TEST_PROJECT_KEY_II);

        assertInactiveSchemeExists(COPY_OF_SOURCE_WORKFLOW_SCHEME, COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION);

        assertDraft(TEST_PROJECT_NAME);
        assertDraft(TEST_PROJECT_NAME_II);

        assertScheme(TEST_PROJECT_NAME, SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_DESCRIPTION,
                SOURCE_SCHEME_WORKFLOW_MAPPING, SOURCE_WORKFLOW_1);

        assertScheme(TEST_PROJECT_NAME_II, SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_DESCRIPTION,
                SOURCE_SCHEME_WORKFLOW_MAPPING, SOURCE_WORKFLOW_1);

        assertSchemeId(SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_ID);
    }

    public void testMigrateMultipleProjectsIfNewProjectIsAssociatedWithTheScheme()
    {
        getBackdoor().restoreDataFromResource("WorkflowSchemePublishingMultipleMigrationTest.xml");

        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep2!default.jspa?draftMigration=true&projectId=10000&schemeId=10100");

        associateNewProjectToScheme(SOURCE_SCHEME_ID);

        submit("Associate");

        administration.project().waitForWorkflowMigration(1000, 100);

        assertScheme(TEST_PROJECT_NAME, COPY_OF_SOURCE_WORKFLOW_SCHEME, COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION,
                DRAFT_WORKFLOW_MAPPING, DESTINATION_WORKFLOW);

        assertScheme(TEST_PROJECT_NAME_II, COPY_OF_SOURCE_WORKFLOW_SCHEME, COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION,
                DRAFT_WORKFLOW_MAPPING, DESTINATION_WORKFLOW);

        assertSchemeId(SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_ID);
    }

    private void associateNewProjectToScheme(long schemeId)
    {
        backdoor.project().addProject("Test Project III", "TSTIII", "admin");
        backdoor.project().setWorkflowScheme("TSTIII", schemeId);
    }

    /**
     * Tests that 2 users can look at the same task and get their proper view of it, eg the starter can acknowledge and
     * the other person cant
     */
    public void testMultiAdminTaskProgressFlowMultipleProjects()
    {
        getBackdoor().restoreDataFromResource("WorkflowMigrationTwoAdminsMultiple.xml");

        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        Long schemeId = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(TEST_PROJECT_KEY).getId();
        administration.project().publishWorkflowSchemeDraft(TEST_PROJECT_NAME, schemeId, statusMapping, false);

        assertMultiAdminTaskProgressFlow(TEST_PROJECT_ID);
    }

    public void testMultiAdminTaskProgressFlowMultipleProjectsGoToSecondProject()
    {
        getBackdoor().restoreDataFromResource("WorkflowMigrationTwoAdminsMultiple.xml");

        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        Long schemeId = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(TEST_PROJECT_KEY).getId();
        administration.project().publishWorkflowSchemeDraft(TEST_PROJECT_NAME, schemeId, statusMapping, false);

        assertMultiAdminTaskProgressFlow(TEST_PROJECT_ID_II);
    }

}
