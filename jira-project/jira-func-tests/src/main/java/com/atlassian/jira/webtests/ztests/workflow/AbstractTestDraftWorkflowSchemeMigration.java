package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.util.collect.MapBuilder;

import java.util.Map;

/**
 *
 * @since v5.2
 */
public abstract class AbstractTestDraftWorkflowSchemeMigration extends AbstractTestWorkflowSchemeMigration
{
    protected static final String TEST_PROJECT_NAME_II = "Test Project II";
    protected static final String TEST_PROJECT_KEY_II = "TSTII";

    protected static final long TEST_PROJECT_ID = 10000L;
    protected static final long TEST_PROJECT_ID_II = 10010L;

    protected static final String COPY_OF_SOURCE_WORKFLOW_SCHEME = "Copy of Source Workflow Scheme";
    protected static final String COPY_2_OF_SOURCE_WORKFLOW_SCHEME = "Copy 2 of Source Workflow Scheme";
    protected static final String SOURCE_SCHEME_DESCRIPTION = "The workflow scheme the project started on";
    protected static final String COPY_OF_SOURCE_WORKFLOW_SCHEME_DESCRIPTION = "The workflow scheme the project started on (This copy was automatically generated from a draft, as an intermediate scheme for migration)";

    protected static final long SOURCE_SCHEME_ID = 10000L;

    protected static final Map<String, String> DRAFT_WORKFLOW_MAPPING = MapBuilder.<String, String>newBuilder()
            .add("Task", JIRA_DEFAULT_WORKFLOW).toMap();

    protected static final Map<String, String> SOURCE_SCHEME_WORKFLOW_MAPPING = MapBuilder.<String, String>newBuilder()
            .add("Bug", SOURCE_WORKFLOW_2).toMap();

    public AbstractTestDraftWorkflowSchemeMigration(String name)
    {
        super(name);
    }

    protected void publishDraft()
    {
        Map<String, String> statusMapping = createTestWorkflowMigrationMapping();

        Long schemeId = backdoor.workflowSchemes().getWorkflowSchemeDraftByProjectKey(TEST_PROJECT_KEY).getId();
        administration.project().publishWorkflowSchemeDraft(TEST_PROJECT_NAME, schemeId, statusMapping, true);
    }

    protected void assertSchemeAndNoDraft(String projectName)
    {
        assertScheme(projectName);
        assertNoDraft(projectName);
    }

    protected void assertNoDraft(String projectName)
    {
        assertNull(getBackdoor().workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound(projectName));
    }

    protected void assertDraft(String projectName)
    {
        WorkflowSchemeData draft = getBackdoor().workflowSchemes().getWorkflowSchemeDraftByProjectNameNullIfNotFound(projectName);

        assertNotNull(draft);
        assertEquals(SOURCE_WORKFLOW_SCHEME, draft.getName());
        assertEquals(DRAFT_WORKFLOW_MAPPING, draft.getMappings());
    }

    protected void assertScheme(String projectName)
    {
        assertScheme(projectName, SOURCE_WORKFLOW_SCHEME, SOURCE_SCHEME_DESCRIPTION, DRAFT_WORKFLOW_MAPPING, DESTINATION_WORKFLOW);
    }

    protected void assertScheme(String projectName, String schemeName, String schemeDescription, Map<String, String> workflowMapping, String defaultWorkflow)
    {
        WorkflowSchemeData schemeData = getBackdoor().workflowSchemes().getWorkflowSchemeByProjectName(projectName);

        assertEquals(schemeName, schemeData.getName());
        assertEquals(schemeDescription, schemeData.getDescription());
        assertEquals(workflowMapping, schemeData.getMappings());
        assertEquals(defaultWorkflow, schemeData.getDefaultWorkflow());
    }

    protected void assertSchemeIdChanged(String schemeName, long id)
    {
        WorkflowSchemeData schemeData = getBackdoor().workflowSchemes().getWorkflowSchemeByName(schemeName);

        assertFalse(schemeData.getId().equals(id));
    }

    protected void assertSchemeId(String schemeName, long id)
    {
        WorkflowSchemeData schemeData = getBackdoor().workflowSchemes().getWorkflowSchemeByName(schemeName);

        assertEquals((long) schemeData.getId(), id);
    }

    protected void assertInactiveSchemeExists(String schemeName, String schemeDescription)
    {
        WorkflowSchemeData schemeData = getBackdoor().workflowSchemes().getWorkflowSchemeByNameNullIfNotFound(schemeName);

        assertNotNull(schemeData);
        assertEquals(schemeDescription, schemeData.getDescription());
        assertFalse(schemeData.isActive());
    }

    protected void assertNoScheme(String schemeName)
    {
        assertNull(getBackdoor().workflowSchemes().getWorkflowSchemeByNameNullIfNotFound(schemeName));
    }

    protected void assertMultiAdminTaskProgressFlow(long projectId)
    {
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
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=" + projectId + "&taskId=" + taskId + "&draftMigration=true");

        validateProgressBarUI(DONE);
        assertTextNotPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextPresent("input type=\"submit\" name=\"Done\"");

        // ok go back and acknowledge as the task starter
        navigation.logout();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.gotoAdmin();
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=" + projectId + "&taskId=" + taskId + "&draftMigration=true");

        assertTextPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextNotPresent("input type=\"submit\" name=\"Done\"");
        validateProgressBarUI(ACKNOWLEDGE);

        submit(ACKNOWLEDGE);

        // now the task should be cleaned up
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=" + projectId + "&taskId=" + taskId + "&draftMigration=true");
        assertTextPresent("The task could not be found. Perhaps it has finished and has been acknowledged?");
        assertTextPresent("input type=\"submit\" name=\"Done\"");
    }
}
