package com.atlassian.jira.webtests.ztests.timetracking.modern;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * <p>Responsible for holding tests which verify that the time tracking field's components (Original Estimate and
 * Remaining Estimate) are displayed and editable for users with Work On Issues permission.</p>
 * 
 * <p>This is always the expected behaviour when the field is not hidden and
 * {@link com.atlassian.jira.functest.framework.admin.TimeTracking.Mode}
 * is set to {@link com.atlassian.jira.functest.framework.admin.TimeTracking.Mode#MODERN}. </p>
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING })
public class TestTimeTrackingIsEditable extends FuncTestCase
{
    private final static String ORIGINAL_ESTIMATE_FORM_ELEMENT_NAME = "timetracking_originalestimate";
    private final static String REMAINING_ESTIMATE_FORM_ELEMENT_NAME = "timetracking_remainingestimate";
    private final static String RESOLVE_ISSUE_LINK_ID = "action_id_5";

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

    public void testAllEstimatesAreEditableOnCreateIssue() throws Exception
    {
        navigation.issue().goToCreateIssueForm("homosapien", "Improvement");
        assertEstimateInputFieldsArePresent();

        tester.setFormElement("summary", "Test Original and Remaining Estimate Are Editable On Create");
        tester.setFormElement(ORIGINAL_ESTIMATE_FORM_ELEMENT_NAME, "1d");
        tester.setFormElement(REMAINING_ESTIMATE_FORM_ELEMENT_NAME, "1d");
        tester.submit("Create");

        assertIssueHasBeenCreatedOrEditedSuccessfully();
        assertTimeTrackingValuesAreEqualTo("1d","1d","Not Specified");
    }

    public void testsAllEstimatesAreEditableOnEditBeforeLoggingWork()
    {
        // Go to an issue that has no work logged against it. Values = {3d, 3d, Not Specified}
        navigation.issue().gotoIssue("HSP-4");
        tester.clickLink("edit-issue");
        assertEstimateInputFieldsArePresent();

        navigation.issue().setEstimates("HSP-4","4d","4d");
        assertIssueHasBeenCreatedOrEditedSuccessfully();
        assertTimeTrackingValuesAreEqualTo("4d","4d","Not Specified");
    }

    public void testAllEstimatesAreEditableOnEditAfterLoggingWork()
    {
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink("edit-issue");
        assertEstimateInputFieldsArePresent();

        navigation.issue().setEstimates("HSP-1","4d","0");
        assertIssueHasBeenCreatedOrEditedSuccessfully();
        assertTimeTrackingValuesAreEqualTo("4d","0m","3d");
    }

    public void testAllEstimatesAreEditableOnTransitionBeforeLoggingWork()
    {
        addTimeTrackingFieldToResolveIssueScreen();

        navigation.issue().gotoIssue("HSP-4");
        tester.clickLink(RESOLVE_ISSUE_LINK_ID);
        assertEstimateInputFieldsArePresent();

        navigation.issue().resolveIssue("HSP-4","Fixed","Test All Estimates Are Editable On Transition After Logging Work","4d","0");

        assertIssueHasBeenCreatedOrEditedSuccessfully();
        assertTimeTrackingValuesAreEqualTo("4d","0m","Not Specified");        
    }

    public void testAllEstimatesAreEditableOnTransitionAfterLoggingWork()
    {
        addTimeTrackingFieldToResolveIssueScreen();
        
        navigation.issue().gotoIssue("HSP-1");
        tester.clickLink(RESOLVE_ISSUE_LINK_ID);
        assertEstimateInputFieldsArePresent();

        navigation.issue().resolveIssue("HSP-1","Fixed","Test All Estimates Are Editable On Transition After Logging Work","4d","0");

        assertIssueHasBeenCreatedOrEditedSuccessfully();
        assertTimeTrackingValuesAreEqualTo("4d","0m","3d");
    }

    private void assertEstimateInputFieldsArePresent()
    {
        tester.assertFormElementPresent(ORIGINAL_ESTIMATE_FORM_ELEMENT_NAME);
        tester.assertFormElementPresent(REMAINING_ESTIMATE_FORM_ELEMENT_NAME);
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

    private void assertTimeTrackingValuesAreEqualTo(String originalEstimateValue, String remainingEstimateValue, String timeSpentValue)
    {
        text.assertTextPresent(new IdLocator(tester, "tt_single_values_orig"),originalEstimateValue);
        text.assertTextPresent(new IdLocator(tester, "tt_single_values_remain"),remainingEstimateValue);
        text.assertTextPresent(new IdLocator(tester, "tt_single_values_spent"),timeSpentValue);
    }

    private void addTimeTrackingFieldToResolveIssueScreen()
    {
        backdoor.screens().addFieldToScreen("Resolve Issue Screen", "Time Tracking");
    }
}