package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import com.atlassian.jira.functest.framework.admin.plugins.WorkflowCondition;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;

import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.OPEN_STEP_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.REFERENCE_MODULE_RESULT_PARAM;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.START_PROGRESS_LINK_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.START_PROGRESS_TRANSITION_ID;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TEST_WORKFLOW_SCHEME_NAME;
import static com.atlassian.jira.webtests.ztests.plugin.reloadable.WorkflowTestConstants.TRANSITION_TAB_TABLE_ID;

/**
 * <p>
 * Test that the 'workflow condition' plugin module type behaves correctly when going back and forth from disabled
 * to enabled state any number of times.
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.RELOADABLE_PLUGINS, Category.REFERENCE_PLUGIN, Category.WORKFLOW, Category.SLOW_IMPORT })
public class TestWorkflowConditionModuleReloadability extends AbstractReloadableWorkflowComponentTest
{
    private static final String TEST_ISSUE_NAME = "Test issue";

    private WorkflowCondition workflowCondition;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        workflowCondition = administration.plugins().referencePlugin().workflowCondition();
        setUpTestWorkflow();
    }


    public void testShouldNotBeVisibleInTheAdminWorkflowsSectionGivenPluginNotEnabled() throws Exception
    {
        assertWorkflowConditionNotAccessible();
    }

    public void testShouldBeVisibleInTheAdminWorkflowsSectionGivenPluginEnabled() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        assertWorkflowConditionAccessible();
    }

    public void testShouldBeVisibleInTheAdminWorkflowsSectionGivenTrueCondition() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowCondition(true);
        text.assertTextSequence(transitionTableTabLocator(), workflowCondition.moduleName(), "will always return true");
    }

    public void testShouldBeVisibleInTheAdminWorkflowsSectionGivenFalseCondition() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowCondition(false);
        text.assertTextSequence(transitionTableTabLocator(), workflowCondition.moduleName(), "will always return false");
    }

    public void testShouldBeExecutedOnViewIssueAndReturnTrue() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowCondition(true);
        setUpTestScheme();
        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, TEST_WORKFLOW_SCHEME_NAME);
        navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, TEST_ISSUE_NAME);
        assertStartProgressTransitionPresent();
    }

    public void testShouldBeExecutedOnViewIssueAndReturnFalse() throws Exception
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowCondition(false);
        setUpTestScheme();
        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, TEST_WORKFLOW_SCHEME_NAME);
        navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, TEST_ISSUE_NAME);
        assertStartProgressTransitionNotPresent();
    }

    public void testShouldBeAccessibleAfterReferencePluginEnablingAndDisabling()
    {
        administration.plugins().referencePlugin().enable();
        assertWorkflowConditionAccessible();
        administration.plugins().referencePlugin().disable();
        assertWorkflowConditionNotAccessible();
        administration.plugins().referencePlugin().enable();
        assertWorkflowConditionAccessible();
    }

    public void testShouldBeAccessibleAfterReferenceWorkflowModuleDisablingAndEnabling()
    {
        administration.plugins().referencePlugin().enable();
        assertWorkflowConditionAccessible();
        for (int i=0; i<3; i++)
        {
            disableReferenceWorkflowConditionModule();
            assertWorkflowConditionNotAccessible();
            enableReferenceWorkflowConditionModule();
            assertWorkflowConditionAccessible();
        }
    }

    @Ignore("pending http://jira.atlassian.com/browse/JRA-23885 -- dkordonski")
    public void testConditionShouldNotBeExecutedWhenAddedToAnIssueAndTheReferencePluginSubsequentlyDisabled()
    {
        administration.plugins().referencePlugin().enable();
        setUpWorkflowCondition(false);
        setUpTestScheme();
        administration.project().associateWorkflowScheme(PROJECT_HOMOSAP, TEST_WORKFLOW_SCHEME_NAME);
        navigation.issue().createIssue(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, TEST_ISSUE_NAME);
        assertStartProgressTransitionNotPresent();
        administration.plugins().referencePlugin().disable();
        navigation.issue().gotoIssue("HSP-1");
        assertStartProgressTransitionPresent();
    }

    private void assertWorkflowConditionNotAccessible()
    {
        assertFalse(workflowCondition.canAddTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID));
    }

    private void assertWorkflowConditionAccessible()
    {
        assertTrue(workflowCondition.canAddTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID));
    }

    private void setUpWorkflowCondition(boolean result)
    {
        workflowCondition.addTo(TEST_WORKFLOW_NAME, OPEN_STEP_ID, START_PROGRESS_TRANSITION_ID,
                ImmutableMap.of(REFERENCE_MODULE_RESULT_PARAM, Boolean.toString(result)));
    }

    private void assertStartProgressTransitionPresent()
    {
        tester.assertLinkPresent(START_PROGRESS_LINK_ID);
    }

    private void assertStartProgressTransitionNotPresent()
    {
        tester.assertLinkNotPresent(START_PROGRESS_LINK_ID);
    }

    private Locator transitionTableTabLocator()
    {
        return locator.id(TRANSITION_TAB_TABLE_ID);
    }

    private void enableReferenceWorkflowConditionModule()
    {
        workflowCondition.enable();
    }

    private void disableReferenceWorkflowConditionModule()
    {
        workflowCondition.disable();
    }
}