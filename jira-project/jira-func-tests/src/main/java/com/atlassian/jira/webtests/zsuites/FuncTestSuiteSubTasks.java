package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.issue.TestCloneIssueWithSubTasks;
import com.atlassian.jira.webtests.ztests.issue.subtasks.TestMoveSubtask;
import com.atlassian.jira.webtests.ztests.subtask.TestClosedParent;
import com.atlassian.jira.webtests.ztests.subtask.TestCreateSubTasks;
import com.atlassian.jira.webtests.ztests.subtask.TestCreateSubTasksContextPermission;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionParentPicker;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionSecurityLevel;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionStep1;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionStep2;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionSystemFields;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionVariousOperations;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionWithFields;
import com.atlassian.jira.webtests.ztests.subtask.TestReindexingSubtasks;
import com.atlassian.jira.webtests.ztests.subtask.TestSecurityLevelOfSubtasks;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskActions;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskProgressBar;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskQuickCreation;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskToIssueConversionSecurityLevel;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskToIssueConversionStep1;
import com.atlassian.jira.webtests.ztests.subtask.move.TestMoveSubTask;
import com.atlassian.jira.webtests.ztests.subtask.move.TestMoveSubTaskEnterprise;
import com.atlassian.jira.webtests.ztests.subtask.move.TestMoveSubTaskIssueType;
import junit.framework.Test;

/**
 *A suite of SubTask related test
 *
 * @since v4.0
 */
public class FuncTestSuiteSubTasks extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteSubTasks();

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

    public FuncTestSuiteSubTasks()
    {
        addTest(TestSubTaskActions.class);
        addTest(TestSubTaskProgressBar.class);
        addTest(TestCreateSubTasks.class);
        addTest(TestSubTaskQuickCreation.class);
        addTest(TestMoveSubTask.class);
        addTest(TestMoveSubTaskIssueType.class);

        addTest(TestCreateSubTasksContextPermission.class);

        addTest(TestIssueToSubTaskConversionSystemFields.class);
        addTest(TestIssueToSubTaskConversionVariousOperations.class);
        addTest(TestIssueToSubTaskConversionStep1.class);
        addTest(TestIssueToSubTaskConversionStep2.class);
        addTest(TestIssueToSubTaskConversionWithFields.class);
        addTest(TestIssueToSubTaskConversionParentPicker.class);
        addTest(TestIssueToSubTaskConversionSecurityLevel.class);
        addTest(TestSubTaskToIssueConversionStep1.class);
        addTest(TestSubTaskToIssueConversionSecurityLevel.class);

        addTest(TestCloneIssueWithSubTasks.class);

        addTest(TestReindexingSubtasks.class);
        addTest(TestSecurityLevelOfSubtasks.class);
        addTest(TestMoveSubTaskEnterprise.class);
        addTest(TestMoveSubtask.class);
        addTest(TestClosedParent.class);
    }
}