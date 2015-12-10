package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.admin.plugins.WorkflowFunction;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.Collections;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.OPEN_STEP_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.START_PROGRESS_LINK_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.START_PROGRESS_TRANSITION_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_SCHEME_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TRANSITION_TAB_TABLE_ID;

/**
 * <p>
 * Test that the 'workflow function' plugin module type behaves correctly when going back and forth from disabled
 * to enabled state any number of times.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.WORKFLOW, Category.SLOW_IMPORT })
public class TestWorkflowFunctionModuleReloadability extends AbstractReloadableWorkflowComponentTest
{
    private WorkflowFunction workflowFunction;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        workflowFunction = administration.plugins().referencePlugin().workflowFunction();
        setUpTestWorkflow();
    }


    public void testShouldNotBeVisibleInTheAdminWorkflowsSectionGivenPluginNotEnabled() throws Exception
    {
        assertWorkflowFunctionNotAccessible();
    }

    public void testShouldBeVisibleInTheAdminWorkflowsSectionGivenPluginEnabled() throws Exception
    {
        referencePlugin.enable();
        assertWorkflowFunctionAccessible();
    }

    public void testShouldBeVisibleInTheAdminWorkflowsSectionAfterAddingGivenPluginEnabled() throws Exception
    {
        referencePlugin.enable();
        setUpWorkflowFunction();
        text.assertTextPresent(transitionTableTabLocator(), workflowFunction.moduleName());
    }

    public void testShouldBeExecutedOnIssueTransitionGivenPluginEnabled() throws Exception
    {
        referencePlugin.enable();
        setUpWorkflowFunction();
        setUpTestScheme();
        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, TEST_WORKFLOW_SCHEME_NAME);
        String issueKey = navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, "Test issue");
        startProgress();
        assertFunctionAppliedOnIssueTransition(issueKey);
    }

    public void testShouldBeAccessibleAfterReferencePluginEnablingAndDisabling()
    {
        referencePlugin.enable();
        assertWorkflowFunctionAccessible();
        referencePlugin.disable();
        assertWorkflowFunctionNotAccessible();
        referencePlugin.enable();
        assertWorkflowFunctionAccessible();
    }

    public void testShouldBeAccessibleAfterReferenceWorkflowFunctionModuleDisablingAndEnabling()
    {
        referencePlugin.enable();
        assertWorkflowFunctionAccessible();
        for (int i=0; i<3; i++)
        {
            disableReferenceWorkflowFunctionModule();
            assertWorkflowFunctionNotAccessible();
            enableReferenceWorkflowFunctionModule();
            assertWorkflowFunctionAccessible();
        }
    }

    private void assertWorkflowFunctionAccessible()
    {
        assertTrue(workflowFunction.canAddTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID));
    }

    private void assertWorkflowFunctionNotAccessible()
    {
        assertFalse(workflowFunction.canAddTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID));
    }

    private void setUpWorkflowFunction()
    {
        workflowFunction.addTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID);
    }

    private String workflowFunctionComment(String issueKey)
    {
        return String.format("[ReferenceWorkflowFunction] transition ID %d of issue %s by %s",
                START_PROGRESS_TRANSITION_ID, issueKey, ADMIN_USERNAME);
    }

    private void assertFunctionAppliedOnIssueTransition(String issueKey)
    {
        assertions.comments(Collections.singletonList(workflowFunctionComment(issueKey))).areVisibleTo(ADMIN_USERNAME, issueKey);
    }

    private void startProgress()
    {
        tester.clickLink(START_PROGRESS_LINK_ID);
    }

    private Locator transitionTableTabLocator()
    {
        return locator.id(TRANSITION_TAB_TABLE_ID);
    }

    private void enableReferenceWorkflowFunctionModule()
    {
        workflowFunction.enable();
    }

    private void disableReferenceWorkflowFunctionModule()
    {
        workflowFunction.disable();
    }
}
