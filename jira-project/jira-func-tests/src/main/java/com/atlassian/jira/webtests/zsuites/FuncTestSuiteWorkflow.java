package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.security.xsrf.TestXsrfWorkflow;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkWorkflowTransition;
import com.atlassian.jira.webtests.ztests.email.TestBulkWorkflowTransitionNotification;
import com.atlassian.jira.webtests.ztests.issue.TestCloneIssueWithValidation;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowTransitionPermission;
import com.atlassian.jira.webtests.ztests.workflow.TestAddWorkflowTransition;
import com.atlassian.jira.webtests.ztests.workflow.TestCustomWorkflow;
import com.atlassian.jira.webtests.ztests.workflow.TestCustomWorkflowScreenLocalization;
import com.atlassian.jira.webtests.ztests.workflow.TestDraftWorkflow;
import com.atlassian.jira.webtests.ztests.workflow.TestDraftWorkflowSchemeMigration;
import com.atlassian.jira.webtests.ztests.workflow.TestDraftWorkflowSchemeMultipleProjectsMigration;
import com.atlassian.jira.webtests.ztests.workflow.TestEditWorkflowDispatcher;
import com.atlassian.jira.webtests.ztests.workflow.TestTransitionWorkflowScreen;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkFlowActions;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkFlowSchemes;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowBasedPermissions;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowConditions;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowDesigner;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowEditing;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowEditor;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowGlobalPostFunctions;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowMigration;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowNameEditing;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowTransitionView;
import com.atlassian.jira.webtests.ztests.workflow.TestWorkflowWithOriginalStepTransitions;
import junit.framework.Test;

/**
 * A FuncTestSuite of Workflow related tests
 *
 * @since v4.0
 */
public class FuncTestSuiteWorkflow extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteWorkflow();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteWorkflow()
    {
        addTest(TestWorkFlowActions.class);
        addTest(TestTransitionWorkflowScreen.class);

        addTest(TestWorkFlowSchemes.class);
        addTest(TestCustomWorkflow.class);
        addTest(TestAddWorkflowTransition.class);
        addTest(TestWorkflowMigration.class);
        addTest(TestDraftWorkflowSchemeMigration.class);
        addTest(TestDraftWorkflowSchemeMultipleProjectsMigration.class);
        addTest(TestWorkflowNameEditing.class);

        addTest(TestBulkWorkflowTransition.class);
        addTest(TestWorkflowBasedPermissions.class);

        addTest(TestWorkflowTransitionView.class);
        addTest(TestWorkflowEditing.class);
        addTest(TestWorkflowConditions.class);
        addTest(TestWorkflowTransitionPermission.class);

        addTest(TestWorkflowEditor.class);
        addTest(TestWorkflowGlobalPostFunctions.class);
        addTest(TestWorkflowWithOriginalStepTransitions.class);
        addTest(TestBulkWorkflowTransitionNotification.class);
        addTest(TestDraftWorkflow.class);
        addTest(TestCustomWorkflowScreenLocalization.class);

        addTest(TestWorkflowDesigner.class);
        addTest(TestEditWorkflowDispatcher.class);
        addTest(TestXsrfWorkflow.class);

        addTest(TestCloneIssueWithValidation.class);
    }
}
