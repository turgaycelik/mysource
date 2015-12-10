package com.atlassian.jira.webtests.ztests.plugin.reloadable;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Workflow test constants.
 *
 * @since v4.3
 */
public final class WorkflowTestConstants
{
    private WorkflowTestConstants()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static final String DEFAULT_WORKFLOW_NAME = asString("jira");

    public static final String TEST_WORKFLOW_NAME = asString("Test workflow");
    public static final String TEST_WORKFLOW_SCHEME_NAME = asString("Test workflow scheme");
    public static final String TEST_WORKFLOW_SCHEME_DESC = asString("A workflow scheme for tests");
    public static final int OPEN_STEP_ID = 1;

    public static final int START_PROGRESS_TRANSITION_ID = 4;
    public static final String START_PROGRESS_LINK_ID = asString("action_id_4");
    public static final String TRANSITION_TAB_TABLE_ID = asString("workflow-transition-tab");

    public static final String REFERENCE_MODULE_RESULT_PARAM = asString("reference-module-result");
}
