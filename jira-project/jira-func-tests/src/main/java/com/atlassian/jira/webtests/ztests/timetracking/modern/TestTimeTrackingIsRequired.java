package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests which verify that JIRA enforces the required rule on the time-tracking field.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingIsRequired extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        // Contains existing data with Time Tracking configuration set to Legacy Mode
        administration.restoreData("TestTimeTrackingBase.xml");

        // We need to disable it in order to enable it again using Modern Mode.
        administration.timeTracking().disable();
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);

        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Time Tracking");

        // put Log Work on same screen as Time Tracking so we can test their interaction
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").addFieldToScreen("Default Screen");
    }

    public void testTwoEmptyEstimatesOnCreateAreRejected() throws Exception
    {
        // Both fields are empty
        navigation.issue().goToCreateIssueForm("homosapien","Improvement");
        tester.setFormElement("summary", "Test Empty Original And Remaining Estimates Are Rejected On Create");
        tester.submit("Create");
        
        text.assertTextPresent("Original Estimate is required.");
        text.assertTextPresent("Remaining Estimate is required.");
    }

    public void testSpecifyingOneEstimateIsOkayAndCopiesToOther() throws Exception
    {
        // Case 1 - Remaining Estimate is Empty
        // this will be allowed and will set the Remaining Estimate to be the same as Original Estimate
        navigation.issue().goToCreateIssueForm("homosapien","Improvement");
        tester.setFormElement("summary", "Test Empty Remaining Estimate Is Rejected On Create");
        tester.setFormElement("timetracking_originalestimate","1d");
        tester.submit("Create");

        // no errors
        text.assertTextNotPresent("Original Estimate is required.");
        text.assertTextNotPresent("Remaining Estimate is required.");

        // correct time info
        text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", "1d", "Remaining", "1d");

        // Case 2 - Original Estimate is Empty
        // this will be allowed and will set the Original Estimate to be the same as Remaining Estimate
        navigation.issue().goToCreateIssueForm("homosapien","Improvement");
        tester.setFormElement("summary", "Test Empty Original Estimate Is Rejected On Create");
        tester.setFormElement("timetracking_remainingestimate","5h");
        tester.submit("Create");

        // no errors
        text.assertTextNotPresent("Original Estimate is required.");
        text.assertTextNotPresent("Remaining Estimate is required.");

        // correct time info
        text.assertTextSequence(new IdLocator(tester, "timetrackingmodule"), "Estimated", "5h", "Remaining", "5h");
    }

    public void testEmptyEstimatesOnEditAreRejectedOnlyWhenBothAreEmpty() throws Exception
    {
        // Case 1 - Both fields are Empty
        navigation.issue().setEstimates("HSP-5", "", "");
        text.assertTextPresent("Original Estimate is required.");
        text.assertTextPresent("Remaining Estimate is required.");

        // Case 2 - Remaining Estimate is Empty
        navigation.issue().setEstimates("HSP-4", "8m", "");
        text.assertTextNotPresent("Original Estimate is required.");
        text.assertTextNotPresent("Remaining Estimate is required.");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("8m", "8m", "Not Specified");

        // Case 3 - Original Estimate is Empty
        navigation.issue().setEstimates("HSP-10", "", "4h");
        text.assertTextNotPresent("Original Estimate is required.");
        text.assertTextNotPresent("Remaining Estimate is required.");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("4h", "4h", "1d");
    }

    public void testEmptyEstimatesOnTransitionAreRejectedOnlyWhenBothAreEmpty() throws Exception
    {
        addTimeTrackingFieldToResolveIssueScreen();

        // Case 1 - Both fields are Empty
        navigation.issue().resolveIssue("HSP-5", "Fixed", "Resolving With Empty Estimates", "", "");
        text.assertTextPresent("Original Estimate is required.");
        text.assertTextPresent("Remaining Estimate is required.");

        // Case 2 - Remaining Estimate is Empty
        navigation.issue().resolveIssue("HSP-4", "Fixed", "Resolving With Empty Remaining Estimate", "8m", "");
        text.assertTextNotPresent("Original Estimate is required.");
        text.assertTextNotPresent("Remaining Estimate is required.");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("8m", "8m", "Not Specified");

        // Case 3 - Original Estimate is Empty
        navigation.issue().resolveIssue("HSP-10", "Fixed", "Resolving With Empty Original Estimate", "", "4h");
        text.assertTextNotPresent("Original Estimate is required.");
        text.assertTextNotPresent("Remaining Estimate is required.");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("4h", "4h", "1d");
    }

    private void addTimeTrackingFieldToResolveIssueScreen()
    {
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Time Tracking").addFieldToScreen("Resolve Issue Screen");
    }
}