package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests which verify that JIRA enforces the optional setting on the time-tracking field.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingIsOptional extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        // Contains existing data with Time Tracking configuration set to Legacy Mode
        administration.restoreData("TestTimeTrackingBase.xml");

        // We need to disable it in order to enable it again using Modern Mode.
        administration.timeTracking().disable();
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);

        // put Log Work on same screen as Time Tracking so we can test their interaction
        administration.fieldConfigurations().defaultFieldConfiguration().getScreens("Log Work").addFieldToScreen("Default Screen");
    }

    public void testEmptyEstimatesOnCreateAreAccepted() throws Exception
    {
        // Case 1 - Both fields are empty
        navigation.issue().goToCreateIssueForm("homosapien", "Improvement");
        tester.setFormElement("summary", "Test Empty Original And Remaining Estimates Are Accepted On Create");
        tester.submit("Create");

        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();

        // Case 2 - Remaining Estimate is Empty
        navigation.issue().goToCreateIssueForm("homosapien", "Improvement");
        tester.setFormElement("summary", "Test Empty Remaining Estimate Is Accepted On Create");
        tester.setFormElement("timetracking_originalestimate", "1d");
        tester.submit("Create");

        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();

        // Case 3 - Original Estimate is Empty
        navigation.issue().goToCreateIssueForm("homosapien", "Improvement");
        tester.setFormElement("summary", "Test Empty Original Estimate Is Accepted On Create");
        tester.setFormElement("timetracking_remainingestimate", "1d");
        tester.submit("Create");

        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();
    }

    public void testEmptyEstimatesOnEditAreAccepted() throws Exception
    {
        // Case 1 - Both fields are Empty
        navigation.issue().setEstimates("HSP-5", "", "");
        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();

        // Case 2 - Remaining Estimate is Empty
        navigation.issue().setRemainingEstimate("HSP-5", "");
        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();

        // Case 3 - Original Estimate is Empty
        navigation.issue().setOriginalEstimate("HSP-5", "");
        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();
    }

    public void testEmptyEstimatesOnTransitionAreAccepted() throws Exception
    {
        addTimeTrackingFieldToResolveIssueScreen();

        // Case 1 - Both fields are Empty
        navigation.issue().resolveIssue("HSP-5", "Fixed", "Resolving With Empty Estimates", "", "");
        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();

        // Case 2 - Remaining Estimate is Empty
        navigation.issue().resolveIssue("HSP-4", "Fixed", "Resolving With Empty Remaining Estimate", null, "");
        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();

        // Case 3 - Original Estimate is Empty
        navigation.issue().resolveIssue("HSP-10", "Fixed", "Resolving With Empty Original Estimate", "", null);
        assertIsRequiredErrorMessagesAreNotPresent();
        assertIssueHasBeenCreatedOrEditedSuccessfully();
    }

    private void addTimeTrackingFieldToResolveIssueScreen()
    {
        backdoor.screens().addFieldToScreen("Resolve Issue Screen", "Time Tracking");
    }


    /**
     * Assert that the issue got created or edited successfully and therefore we have landed on the View Issue Page, by
     * looking up the issue key and confirming that the issue key is not null.
     */
    private void assertIssueHasBeenCreatedOrEditedSuccessfully()
    {
        final String issueKeyTagId = "key-val";
        tester.assertElementPresent(issueKeyTagId);
        assertNotNull(new IdLocator(tester, issueKeyTagId));
    }

    /**
     * Confirm that the "[field_name] is required." error messages have not been displayed on screen.
     */
    private void assertIsRequiredErrorMessagesAreNotPresent()
    {
        text.assertTextNotPresent("Original Estimate is required.");
        text.assertTextNotPresent("Remaining Estimate is required.");
    }
}