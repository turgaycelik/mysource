package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests which verify that the UpdateIssueFieldFunction post-function can update the Time
 * Tracking Estimates (Original Estimate and Remaining Estimate).
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingIsEditableByUpdateIssueFieldFunction extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        // Contains existing data with Time Tracking configuration set to Legacy Mode.
        // Be aware that there are two workflow transitions called Close Issue. There's only one that sets the original
        // estimate to 0 through the update issue field post-function magic.
        //
        // The one that doesn't use the post-function is the one linked to the Resolved State.
        // I know it sucks, but it has been inherited from the default jira workflow which also has two "different"
        // transitions called "Close Issue".
        administration.restoreData("TestTimeTrackingUpdateIssueFieldFunction.xml");

        // We need to disable it in order to enable it again using Modern Mode.
        administration.timeTracking().disable();
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);

        // put Log Work on same screen as Time Tracking so we can test their interaction
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").addFieldToScreen("Default Screen");
    }

    /**
     * Tests that the UpdateIssueField post-function can set the value of the Remaining Estimate.
     *
     * The Resolve Transition in the test data's workflow has an UpdateIssueField post-function call that sets the
     * remaining estimate to 0.
     */
    public void testCanEditRemainingEstimateOnResolve()
    {
        navigation.issue().resolveIssue("HSP-1","Fixed","Resolving the Issue as Fixed. This should set the remaining estimate to 0");
        assertTimeTrackingValuesAreEqualTo("1w","0m","3d");
    }

    /**
     * Tests that the UpdateIssueField post-function can set the value of the Original Estimate.
     *
     * The Close Transition in the test data's workflow has an UpdateIssueField post-function call that sets the
     * original estimate to 0. See note about this transition in {@link #setUpTest()}
     */
    public void testCanEditOriginalEstimateOnClose()
    {
        navigation.issue().closeIssue("HSP-4","Won't Fix","Closing the Issue as Won't Fix. This should set the original estimate to 0");
        assertTimeTrackingValuesAreEqualTo("0m","0m","1w");
    }

    /**
     * Tests that the UpdateIssueField post-function can set the value of the Original and Remaining Estimate in one transition.
     *
     * The Reopen Transition in the test data's workflow has two UpdateIssueField post-function calls that set the
     * original estimate to 2 and the remaining estimate to 2.
     */
    public void testCanEditOriginalAndRemainingEstimateOnReopen()
    {
        navigation.issue().reopenIssue("HSP-2");
        assertTimeTrackingValuesAreEqualTo("2h","2h","Not Specified");
    }

    private void assertTimeTrackingValuesAreEqualTo(String originalEstimateValue, String remainingEstimateValue, String timeSpentValue)
    {
        text.assertTextPresent(new IdLocator(tester, "tt_single_values_orig"),originalEstimateValue);
        text.assertTextPresent(new IdLocator(tester, "tt_single_values_remain"),remainingEstimateValue);
        text.assertTextPresent(new IdLocator(tester, "tt_single_values_spent"),timeSpentValue);
    }
}