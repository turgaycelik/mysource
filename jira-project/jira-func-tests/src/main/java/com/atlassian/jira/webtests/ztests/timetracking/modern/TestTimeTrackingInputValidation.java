package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for verifying that the time tracking field verifies that data entered in the Time Tracking field is is in
 * the correct format.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingInputValidation extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        // Contains existing data with Time Tracking configuration set to Legacy Mode
        administration.restoreData("TestTimeTrackingBase.xml");

        // We need to disable it in order to enable it again using Modern Mode.
        administration.timeTracking().disable();
        administration.timeTracking().enable("8", "5", "pretty", "hour", TimeTracking.Mode.MODERN);

        // put Log Work on same screen as Time Tracking so we can test their interaction
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").addFieldToScreen("Default Screen");
    }

    public void testTimeTrackingFieldRejectsRubbish()
    {
        // Original Estimate
        navigation.issue().setOriginalEstimate("HSP-5", "[@asksajm1541%<><><<<();>)");
        text.assertTextPresent("The original estimate specified is not valid.");

        // Remaining Estimate
        navigation.issue().setRemainingEstimate("HSP-5", "[#s#~@^%&&*^&*ksajm1541%<><><<<();>)");
        text.assertTextPresent("The remaining estimate specified is not valid.");
    }

    public void testTimeTrackingAcceptsFractionalValues()
    {
        final String key = navigation.issue().createIssue("homosapien", "Bug", "testTimeTrackingAcceptsFractionalValues");

        // set the original estimate to 12.15h, 0.15h = 9m
        navigation.issue().setOriginalEstimate(key, "7.15h");
        navigation.issue().setRemainingEstimate(key, "7.15h");
        text.assertTextSequence(new IdLocator(getTester(), "tt_single_table_info"), "Estimated:", "7h 9m",
                "Remaining:", "7h 9m");

        // switch our printing format over to hours to make sure we display the fraction there
        administration.timeTracking().disable();
        administration.timeTracking().enable("8", "5", "hours", "hour", TimeTracking.Mode.MODERN);

        navigation.issue().viewIssue(key);
        text.assertTextSequence(new IdLocator(getTester(), "tt_single_table_info"), "Estimated:", "7.15h",
                "Remaining:", "7.15h");

        // enter something that falls below our unit of precision (we are at hours, enter something less than one hour)
        navigation.issue().setRemainingEstimate(key, "12m");
        text.assertTextSequence(new IdLocator(getTester(), "tt_single_table_info"), "Estimated:", "7.15h",
                "Remaining:", "0.2h");

        // 12 minutes can be represented as 0.2 without losing precision. but 20 minutes cannot (1/3) make sure
        // that we fall back and display this as minutes
        navigation.issue().setRemainingEstimate(key, "20m");        
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_orig"), "7.15h");
        text.assertTextPresent(new IdLocator(getTester(), "tt_single_values_remain"), "20m");
    }

    public void testTimeTrackingRejectsInvalidTimeUnitSuffixes()
    {
        // Original Estimate
        navigation.issue().setOriginalEstimate("HSP-5", "1y");
        text.assertTextPresent("The original estimate specified is not valid.");

        // Remaining Estimate
        navigation.issue().setRemainingEstimate("HSP-5", "1ms");
        text.assertTextPresent("The remaining estimate specified is not valid.");
    }

    /**
     * <p>This is the behaviour now. This should really be flagged as an input error because it is most likely to be a
     * mistake made by the user.</p>
     * <p>This has been reported this as JRA-20974. When and if, this gets changed
     * this test will fail and will have to be changed to verify the opposite of the current behaviour.</p>
     */
    public void testTimeTrackingAcceptsMoreThanOneEntryQualifiedByTheSameTimeUnit()
    {
        // Original Estimate
        navigation.issue().setOriginalEstimate("HSP-5", "1w 2w");
        text.assertTextNotPresent("The original estimate specified is not valid.");

        // Remaining Estimate
        navigation.issue().setRemainingEstimate("HSP-5", "1h 30h");
        text.assertTextNotPresent("The remaining estimate specified is not valid.");
    }

    public void testTimeTrackingRejectsTimeUnitsNotQualifiedByANumber()
    {
        // Original Estimate
        navigation.issue().setOriginalEstimate("HSP-5", "w 2d");
        text.assertTextPresent("The original estimate specified is not valid.");

        // Remaining Estimate
        navigation.issue().setRemainingEstimate("HSP-5", "1h s");
        text.assertTextPresent("The remaining estimate specified is not valid.");
    }

    /**
     * JRA-20975.
     */
    public void testTimeTrackingAcceptsMoreThanOneEmptyTimeUnitSuffix()
    {
        // Original Estimate
        navigation.issue().setOriginalEstimate("HSP-5", "6 10");
        text.assertTextPresent("The original estimate specified is not valid.");

        // Remaining Estimate
        navigation.issue().setRemainingEstimate("HSP-5", "5 30");
        text.assertTextPresent("The remaining estimate specified is not valid.");
    }
}