package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.fields.EditFieldConstants;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigation.BulkChangeWizard;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * JRADEV-2291 - although Time Tracking fields are not available for Bulk Edit, they are available for Bulk Move.
 * <p/>
 * We need to test that it behaves as expected in both Modern and Legacy modes.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestBulkMoveTimeTracking extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        // data contains two issues - one with no TT info set, and one with some work already logged
        administration.restoreData("TestTimeTrackingBulkMove.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testValidationInModernMode() throws Exception
    {
        // make Log Work required so we can check that it does not actually show up during Bulk Move
        administration.fieldConfigurations().defaultFieldConfiguration().requireField("Log Work");

        // advance wizard up until the point where we are inputting data
        BulkChangeWizard wizard = bulkMoveAllIssuesToProject("monkey");

        // assert that Log Work field is not there but Time Tracking is
        text.assertTextNotPresent(new WebPageLocator(tester), "Log Work");
        text.assertTextPresent(new WebPageLocator(tester), "Time Tracking");

        // try to progress without entering anything for timetracking
        wizard.finaliseFields();

        // should receive errors saying Original Estimate and Remaining Estimate are required
        WebPageLocator pageLocator = new WebPageLocator(tester);
        text.assertTextPresent(pageLocator, "Original Estimate is required");
        text.assertTextPresent(pageLocator, "Remaining Estimate is required");

        // enter invalid value for one but not the other
        wizard.setFieldValue(EditFieldConstants.TIMETRACKING_ORIGINALESTIMATE, "xxx")
                .setFieldValue(EditFieldConstants.TIMETRACKING_REMAININGESTIMATE, "6h")
                .finaliseFields();

        // should receive errors saying Original Estimate and Remaining Estimate are required
        pageLocator = new WebPageLocator(tester);
        text.assertTextPresent(pageLocator, "The original estimate specified is not valid.");
        text.assertTextNotPresent(pageLocator, "The remaining estimate specified is not valid.");

        // swap
        wizard.setFieldValue(EditFieldConstants.TIMETRACKING_ORIGINALESTIMATE, "6h")
                .setFieldValue(EditFieldConstants.TIMETRACKING_REMAININGESTIMATE, "xxx")
                .finaliseFields();

        pageLocator = new WebPageLocator(tester);
        text.assertTextNotPresent(pageLocator, "The original estimate specified is not valid.");
        text.assertTextPresent(pageLocator, "The remaining estimate specified is not valid.");
    }

    public void testValidationInLegacyMode() throws Exception
    {
        administration.timeTracking().switchMode(TimeTracking.Mode.LEGACY);

        // advance wizard up until the point where we are inputting data
        BulkChangeWizard wizard = bulkMoveAllIssuesToProject("monkey");

        // try to progress without entering anything for timetracking
        wizard.finaliseFields();

        // should receive error saying Original Estimate is required
        WebPageLocator pageLocator = new WebPageLocator(tester);
        text.assertTextPresent(pageLocator, "Time Tracking is required.");

        // enter invalid value
        wizard.setFieldValue(EditFieldConstants.TIMETRACKING, "xxx")
                .finaliseFields();

        // should receive at least one error saying either Original Estimate or Remaining Estimate is invalid
        // (depends on which issue is considered first)
        pageLocator = new WebPageLocator(tester);
        text.assertAtLeastOneTextPresent(pageLocator, "The original estimate specified is not valid.", "The remaining estimate specified is not valid.");
    }

    public void testOnlyOriginalEstimateSpecifiedNoRetainInModernMode() throws Exception
    {
        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING_ORIGINALESTIMATE, "6h")
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues - since only original estimate was entered, it will be copied to remaining estimate
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("6h", "6h", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("6h", "6h", "Not Specified");
    }

    public void testOnlyOriginalEstimateSpecifiedRetainCheckedInModernMode() throws Exception
    {
        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING_ORIGINALESTIMATE, "6h")
                .checkRetainForField(EditFieldConstants.TIMETRACKING)
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues - since only original estimate was entered, it will be copied to remaining estimate
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("1w", "1d", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("6h", "6h", "Not Specified");
    }

    public void testOnlyRemainingEstimateSpecifiedNoRetainInModernMode() throws Exception
    {
        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING_REMAININGESTIMATE, "3h")
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues - since only remaining estimate was entered, it will be copied to original estimate
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("3h", "3h", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("3h", "3h", "Not Specified");
    }

    public void testOnlyRemainingEstimateSpecifiedRetainCheckedInModernMode() throws Exception
    {
        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING_REMAININGESTIMATE, "3h")
                .checkRetainForField(EditFieldConstants.TIMETRACKING)
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues - since only remaining estimate was entered, it will be copied to original estimate
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("1w", "1d", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("3h", "3h", "Not Specified");
    }

    public void testBothSpecifiedNoRetainInModernMode() throws Exception
    {
        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING_ORIGINALESTIMATE, "4h")
                .setFieldValue(EditFieldConstants.TIMETRACKING_REMAININGESTIMATE, "2h")
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("4h", "2h", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("4h", "2h", "Not Specified");
    }

    public void testBothSpecifiedRetainCheckedInModernMode() throws Exception
    {
        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING_ORIGINALESTIMATE, "4h")
                .setFieldValue(EditFieldConstants.TIMETRACKING_REMAININGESTIMATE, "2h")
                .checkRetainForField(EditFieldConstants.TIMETRACKING)
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("1w", "1d", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("4h", "2h", "Not Specified");
    }

    public void testEstimateSpecifiedNoRetainInLegacyMode() throws Exception
    {
        administration.timeTracking().switchMode(TimeTracking.Mode.LEGACY);

        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING, "1h")
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("1w", "1h", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("1h", "1h", "Not Specified");
    }
    
    public void testEstimateSpecifiedRetainCheckedInLegacyMode() throws Exception
    {
        administration.timeTracking().switchMode(TimeTracking.Mode.LEGACY);

        // advance wizard up until the point where we are inputting data
        // set only the original estimate
        bulkMoveAllIssuesToProject("monkey")
                .setFieldValue(EditFieldConstants.TIMETRACKING, "1h")
                .checkRetainForField(EditFieldConstants.TIMETRACKING)
                .finaliseFields()
                .complete();
        waitAndReloadBulkOperationProgressPage();

        // verify time tracking data in resultant issues
        tester.clickLinkWithText("Issue 1");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("1w", "1d", "3d");
        navigation.issue().returnToSearch();

        tester.clickLinkWithText("Issue to move with no Time Tracking");
        assertions.getViewIssueAssertions().assertTimeTrackingInfo("1h", "1h", "Not Specified");
    }

    /**
     * Searches for all issues and then initiates the Bulk Move wizard for all the issues returned. The issues will be
     * moved to the specified project.
     *
     * @param projectName the name of the project to which all issues will be moved.
     * @return the instance of the BulkChangeWizard.
     */
    private BulkChangeWizard bulkMoveAllIssuesToProject(final String projectName)
    {
        navigation.issueNavigator().displayAllIssues();

        return navigation.issueNavigator()
                .bulkChange(IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(BulkChangeWizard.BulkOperationsImpl.MOVE)
                .chooseTargetContextForAll(projectName);
    }
}
