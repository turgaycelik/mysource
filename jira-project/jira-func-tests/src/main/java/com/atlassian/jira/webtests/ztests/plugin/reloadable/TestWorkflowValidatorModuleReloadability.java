package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.admin.plugins.WorkflowValidator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableMap;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.ReferencePluginConstants.REFERENCE_WORKFLOW_VALIDATOR_ERROR_MESSAGE;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.OPEN_STEP_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.REFERENCE_MODULE_RESULT_PARAM;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.START_PROGRESS_LINK_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.START_PROGRESS_TRANSITION_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_SCHEME_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TRANSITION_TAB_TABLE_ID;

/**
 * <p>
 * Test that the 'workflow validator' plugin module type behaves correctly when going back and forth from disabled
 * to enabled state any number of times.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.WORKFLOW, Category.SLOW_IMPORT })
public class TestWorkflowValidatorModuleReloadability extends AbstractReloadableWorkflowComponentTest
{
    private static final String TEST_ISSUE_NAME = "Test issue";

    private WorkflowValidator workflowValidator;

        @Override
    protected void setUpTest()
    {
        super.setUpTest();
        workflowValidator = administration.plugins().referencePlugin().workflowValidator();
        setUpTestWorkflow();
    }

    public void testShouldNotBeVisibleInTheAdminWorkflowsSectionGivenPluginNotEnabled() throws Exception
    {
        assertWorkflowValidatorNotAccessible();
    }


    public void testShouldBeVisibleInTheAdminWorkflowsSectionGivenPluginEnabled() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        assertWorkflowValidatorAccessible();
    }

    public void testShouldBeVisibleInTheAdminWorkflowsSectionGivenTrueReturnValue() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowValidator(true);
        text.assertTextSequence(transitionTableTabLocator(), workflowValidator.moduleName(), "will always validate as true");
    }

    public void testShouldBeVisibleInTheAdminWorkflowsSectionGivenFalseReturnValue() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowValidator(false);
        text.assertTextSequence(transitionTableTabLocator(), workflowValidator.moduleName(), "will always validate as false");
    }

    public void testShouldBeExecutedOnViewIssueAndAllowTransition() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowValidator(true);
        setUpTestScheme();
        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, TEST_WORKFLOW_SCHEME_NAME);
        final String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, TEST_ISSUE_NAME);
        assertStartProgressTransitionIsExecuted(issueKey);
    }

    public void testShouldBeExecutedOnViewIssueAndPreventTransition() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowValidator(false);
        setUpTestScheme();
        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, TEST_WORKFLOW_SCHEME_NAME);
        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, TEST_ISSUE_NAME);
        assertStartProgressTransitionIsNotExecuted(issueKey);
    }


    public void testShouldBeAccessibleAfterReferencePluginEnablingAndDisabling()
    {
        referencePlugin.enable();
        assertWorkflowValidatorAccessible();
        referencePlugin.disable();
        assertWorkflowValidatorNotAccessible();
        referencePlugin.enable();
        assertWorkflowValidatorAccessible();
    }

    public void testShouldBeAccessibleAfterReferenceWorkflowValidatorModuleDisablingAndEnabling()
    {
        referencePlugin.enable();
        assertWorkflowValidatorAccessible();
        for (int i=0; i<3; i++)
        {
            workflowValidator.disable();
            assertWorkflowValidatorNotAccessible();
            workflowValidator.enable();
            assertWorkflowValidatorAccessible();
        }
    }

    private void assertWorkflowValidatorAccessible()
    {
        assertTrue(workflowValidator.canAddTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID));
    }

    private void assertWorkflowValidatorNotAccessible()
    {
        assertFalse(workflowValidator.canAddTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID));
    }

    private void setUpWorkflowValidator(boolean result)
    {
        workflowValidator.addTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID,
                ImmutableMap.of(REFERENCE_MODULE_RESULT_PARAM, Boolean.toString(result)));
    }

    private void assertStartProgressTransitionIsExecuted(String issueKey)
    {
        assertions.getViewIssueAssertions().assertOnViewIssuePage(issueKey);
        tester.clickLink(START_PROGRESS_LINK_ID);
        assertions.getViewIssueAssertions().assertOnViewIssuePage(issueKey);
        assertions.getViewIssueAssertions().assertStatus(STATUS_IN_PROGRESS);
    }

    private void assertStartProgressTransitionIsNotExecuted(String issueKey)
    {
        assertions.getViewIssueAssertions().assertOnViewIssuePage(issueKey);
        tester.clickLink(START_PROGRESS_LINK_ID);
        assertions.getJiraMessageAssertions().assertHasMessage("Workflow Error");
        assertions.getJiraMessageAssertions().assertHasMessage(REFERENCE_WORKFLOW_VALIDATOR_ERROR_MESSAGE);
    }

    private Locator transitionTableTabLocator()
    {
        return locator.id(TRANSITION_TAB_TABLE_ID);
    }

}
