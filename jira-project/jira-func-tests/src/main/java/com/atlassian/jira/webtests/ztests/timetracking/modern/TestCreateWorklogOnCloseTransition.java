package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Uses the tests defined in {@link AbstractTestCreateWorklogAsField} to test the "log work" system field on Close
 * transitions.
 *
 * @since v4.2
 */
@WebTest({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestCreateWorklogOnCloseTransition extends AbstractTestCreateWorklogAsField
{
    private LogWorkRunner logWorkRunner;

    protected LogWorkRunner getLogWorkRunner()
    {
        if (logWorkRunner == null)
        {
            logWorkRunner = new CloseTransitionLogWorkRunner();
        }
        return logWorkRunner;
    }

    class CloseTransitionLogWorkRunner implements LogWorkRunner
    {
        public void gotoLogWorkScreen1()
        {
            navigation.issue().viewIssue(HSP_1);
            tester.clickLinkWithText("Close Issue");
        }

        public boolean isCommentFieldShown()
        {
            return true;
        }

        public void gotoLogWorkScreen2()
        {
            navigation.issue().gotoIssue(HSP_1);
            tester.clickLinkWithText("Reopen Issue");
        }

        public void gotoLogWorkScreenWithOriginalEstimate(final String originalEstimate)
        {
            // preset the original estimate
            navigation.issue().viewIssue(HSP_1);
            tester.clickLink("edit-issue");
            tester.setFormElement("timetracking", originalEstimate);
            tester.submit("Update");

            // now go to log work screen
            gotoLogWorkScreen1();
        }

        public void gotoLogWorkResult()
        {
            navigation.issue().viewIssue(HSP_1);
        }

        public boolean isLoggingWorkTwiceSupported()
        {
            return true;
        }
    }

    @Override
    public void testWorklogNoPermToCreate()
    {
        super.testWorklogNoPermToCreate();
    }

    @Override
    public void testWorklogTimeTrackingDisabled()
    {
        super.testWorklogTimeTrackingDisabled();
    }

    @Override
    public void testLogWorkLeaveEstimateNoteCorrect()
    {
        super.testLogWorkLeaveEstimateNoteCorrect();
    }

    @Override
    public void testMandatoryFields()
    {
        super.testMandatoryFields();
    }

    @Override
    public void testInvalidFormattedDurationFields()
    {
        super.testInvalidFormattedDurationFields();
    }

    @Override
    public void testBadFractionDuration()
    {
        super.testBadFractionDuration();
    }

    @Override
    public void testGoodFractionDuration() throws Exception
    {
        super.testGoodFractionDuration();
    }

    @Override
    public void testInvalidTimeSpentZero()
    {
        super.testInvalidTimeSpentZero();
    }

    @Override
    public void testInvalidStartDateField()
    {
        super.testInvalidStartDateField();
    }

    @Override
    public void testAutoAdjustEstimate()
    {
        super.testAutoAdjustEstimate();
    }

    @Override
    public void testNewEstimate()
    {
        super.testNewEstimate();
    }

    @Override
    public void testLeaveExistingEstimate()
    {
        super.testLeaveExistingEstimate();
    }

    @Override
    public void testManuallyAdjustEstimate()
    {
        super.testManuallyAdjustEstimate();
    }

    @Override
    public void testLogWorkVisibleToAll()
    {
        super.testLogWorkVisibleToAll();
    }

    @Override
    public void testLogWorkVisibleToAllDaysTimeFormat()
    {
        super.testLogWorkVisibleToAllDaysTimeFormat();
    }

    @Override
    public void testLogWorkVisibleToAllHoursTimeFormat()
    {
        super.testLogWorkVisibleToAllHoursTimeFormat();
    }

    @Override
    public void testLogWorkVisibleToRole()
    {
        super.testLogWorkVisibleToRole();
    }

    @Override
    public void testLogWorkVisibleToRoleDaysTimeFormat()
    {
        super.testLogWorkVisibleToRoleDaysTimeFormat();
    }

    @Override
    public void testLogWorkVisibleToRoleHoursTimeFormat()
    {
        super.testLogWorkVisibleToRoleHoursTimeFormat();
    }

    @Override
    public void testLogWorkVisibleToGroup()
    {
        super.testLogWorkVisibleToGroup();
    }

    @Override
    public void testLogWorkVisibleToGroupDaysTimeFormat()
    {
        super.testLogWorkVisibleToGroupDaysTimeFormat();
    }

    @Override
    public void testLogWorkVisibleToGroupHoursTimeFormat()
    {
        super.testLogWorkVisibleToGroupHoursTimeFormat();
    }

    @Override
    public void testLogWorkCommentsNotCopiedWhenCopyingDisabled()
    {
        super.testLogWorkCommentsNotCopiedWhenCopyingDisabled();
    }

    @Override
    public void testLogWorkCommentsCopiedWhenCopyingEnabled()
    {
        super.testLogWorkCommentsCopiedWhenCopyingEnabled();
    }

    @Override
    public void testLogWorkDateIsStartDate()
    {
        super.testLogWorkDateIsStartDate();
    }

    @Override
    public void testChangeHistory() throws Exception
    {
        super.testChangeHistory();
    }
}