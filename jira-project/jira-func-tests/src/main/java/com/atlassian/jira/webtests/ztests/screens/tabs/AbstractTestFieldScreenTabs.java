package com.atlassian.jira.webtests.ztests.screens.tabs;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.DUEDATE;
import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.REPORTER;
import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.TIMETRACKING;
import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.TIMETRACKING_ORIGINALESTIMATE;
import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.TIMETRACKING_REMAININGESTIMATE;
import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.WORKLOG_ACTIVATE;
import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.WORKLOG_TIMELOGGED;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import org.w3c.dom.Node;

/**
 * Tests the functionality of Screen Tabs on an arbitrary "editing" screen.
 *
 * @since v4.2
 */
public abstract class AbstractTestFieldScreenTabs extends FuncTestCase
{
    private static final String CLASS_HAS_ERRORS = "has-errors";

    private static final String ORIGINAL_ESTIMATE_ERROR_MESSAGE = "The original estimate specified is not valid.";
    private static final String REMAINING_ESTIMATE_ERROR_MESSAGE = "The remaining estimate specified is not valid.";
    private static final String TIME_LOGGED_ERROR_MESSAGE = "Invalid time duration entered.";

    /**
     * Navigate to the screen where editing of fields can take place for an issue with no work logged on it yet.
     */
    protected abstract void gotoTabScreenForIssueWithNoWork();

    /**
     * Navigate to the screen where editing of fields can take place for an issue with some work logged on it.
     *
     * @throws UnsupportedOperationException if {@link #canShowScreenForIssueWithWork()} returns <code>false</code>.
     */
    protected abstract void gotoTabScreenForIssueWithWorkStarted();

    /**
     * @return true if it makes sense to get to a screen where we are editing fields for an issue with some work logged
     *         on it i.e. any time except Creating an issue.
     */
    protected boolean canShowScreenForIssueWithWork()
    {
        return true;
    }

    /**
     * @return the names of the fields to be displayed in the first tab on the screen.
     */
    protected String[] getFieldsInFirstTab()
    {
        return new String[] { "Summary", "Issue Type", "Priority" };
    }

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestFieldScreenTabs.xml");
        backdoor.darkFeatures().enableForSite("jira.no.frother.reporter.field");
    }

    @Override
    protected void tearDownTest()
    {
        backdoor.darkFeatures().disableForSite("jira.no.frother.reporter.field");
        super.tearDownTest();
    }

    public void testErrorsForTimeTrackingAndWorklog() throws Exception
    {
        // worklog and timetracking errors - work not started
        _testErrorsForTimeTrackingAndWorklog_LegacyModeWorkNotStarted();

        // worklog and timetracking errors - work started
        // only makes sense if we can get to this screen for an issue with work started
        if (canShowScreenForIssueWithWork())
        {
            _testErrorsForTimeTrackingAndWorklog_LegacyModeWorkStarted();
        }

        // tests in modern mode
        _testErrorsForTimeTrackingAndWorklog_ModernMode();
    }

    private void _testErrorsForTimeTrackingAndWorklog_LegacyModeWorkNotStarted()
    {
        gotoTabScreenForIssueWithNoWork();
        tester.setFormElement(TIMETRACKING, "xxx");
        tester.setFormElement(WORKLOG_TIMELOGGED, "5m");
        tester.submit();
        assertErrorAppearsOnTimeTrackingTab(ORIGINAL_ESTIMATE_ERROR_MESSAGE);

        tester.setFormElement(TIMETRACKING, "5h");
        tester.setFormElement(WORKLOG_TIMELOGGED, "yyy");
        tester.submit();
        assertErrorAppearsOnWorkLogTab(TIME_LOGGED_ERROR_MESSAGE);
    }

    private void _testErrorsForTimeTrackingAndWorklog_LegacyModeWorkStarted()
    {
        gotoTabScreenForIssueWithWorkStarted();
        tester.setFormElement(TIMETRACKING, "xxx");
        tester.setFormElement(WORKLOG_TIMELOGGED, "5m");
        tester.submit();
        assertErrorAppearsOnWorkLogTab(REMAINING_ESTIMATE_ERROR_MESSAGE);

        tester.setFormElement(TIMETRACKING, "5h");
        tester.setFormElement(WORKLOG_TIMELOGGED, "yyy");
        tester.checkCheckbox(WORKLOG_ACTIVATE, "true");
        tester.submit();
        assertErrorAppearsOnWorkLogTab(TIME_LOGGED_ERROR_MESSAGE);
    }

    private void _testErrorsForTimeTrackingAndWorklog_ModernMode()
    {
        enableModernMode();
        gotoTabScreenForIssueWithNoWork();
        tester.setFormElement(TIMETRACKING_ORIGINALESTIMATE, "xxx");
        tester.setFormElement(TIMETRACKING_REMAININGESTIMATE, "4m");
        tester.setFormElement(WORKLOG_TIMELOGGED, "5m");
        tester.submit();
        assertErrorAppearsOnTimeTrackingTab(ORIGINAL_ESTIMATE_ERROR_MESSAGE);

        tester.setFormElement(TIMETRACKING_ORIGINALESTIMATE, "5h");
        tester.setFormElement(TIMETRACKING_REMAININGESTIMATE, "zzz");
        tester.setFormElement(WORKLOG_TIMELOGGED, "2h");
        tester.submit();
        assertErrorAppearsOnWorkLogTab(REMAINING_ESTIMATE_ERROR_MESSAGE);

        tester.checkCheckbox(WORKLOG_ACTIVATE, "true");
        tester.setFormElement(TIMETRACKING_ORIGINALESTIMATE, "5h");
        tester.setFormElement(TIMETRACKING_REMAININGESTIMATE, "15m");
        tester.setFormElement(WORKLOG_TIMELOGGED, "xxx");
        tester.submit();
        assertErrorAppearsOnWorkLogTab(TIME_LOGGED_ERROR_MESSAGE);
    }

    private void assertErrorAppearsOnWorkLogTab(final String errorMessage)
    {    // create errors for time tracking in legacy mode
        assertTabIsSelected("Tab 2");
        assertTabsHaveErrors("Tab 2");
        text.assertTextPresent(new IdLocator(tester, "tab2"), errorMessage);
    }

    private void assertErrorAppearsOnTimeTrackingTab(final String errorMessage)
    {
        // create errors for time tracking in legacy mode
        assertTabIsSelected("Tab 4");
        assertTabsHaveErrors("Tab 4");
        text.assertTextPresent(new IdLocator(tester, "tab4"), errorMessage);
    }

    public void testErrorsOnMultipleTabs() throws Exception
    {
        // create errors for Tabs 2, 3 and 4 and submit
        gotoTabScreenForIssueWithNoWork();
        tester.setFormElement(DUEDATE, "xxx");
        tester.setFormElement(REPORTER, "yyy");
        tester.setFormElement(TIMETRACKING, "zzz");
        tester.submit();

        // assert Tabs 2, 3 and 4 indicate errors
        assertTabsDontHaveErrors("Field Tab");
        assertTabsHaveErrors("Tab 2", "Tab 3", "Tab 4");

        // assert Tab 2 is the selected tab as it is the first tab with errors
        assertTabIsSelected("Tab 2");

        // remove error from Tab 2
        tester.setFormElement(DUEDATE, "");
        tester.submit();

        // now Tabs 3 and 4 indicate errors and Tab 3 is selected
        assertTabsDontHaveErrors("Field Tab", "Tab 2");
        assertTabsHaveErrors("Tab 3", "Tab 4");
        assertTabIsSelected("Tab 3");
    }

    public void testPresentationOfTabs() throws Exception
    {
        // time tracking is in legacy mode - therefore Original and Remaining Estimate don't ever appear together
        gotoTabScreenForIssueWithNoWork();
        assertFieldsInTabsForLegacyModeWorkNotStarted();

        // enable modern mode for time tracking - therefore Original and Remaining Estimate can appear together
        enableModernMode();
        gotoTabScreenForIssueWithNoWork();
        assertFieldsInTabsForModernMode();

        if (canShowScreenForIssueWithWork())
        {
            enableLegacyMode();
            gotoTabScreenForIssueWithWorkStarted();
            assertFieldsInTabsForLegacyModeWorkStarted();

            enableModernMode();
            gotoTabScreenForIssueWithWorkStarted();
            assertFieldsInTabsForModernMode();
        }
    }

    private void assertFieldsInTabsForLegacyModeWorkNotStarted()
    {
        assertFieldsInTab("tab1", getFieldsInFirstTab());
        assertFieldsInTab("tab2", "Time Spent", "Date Started", "Remaining Estimate", "Affects Version/s", "Component/s", "Due Date");
        assertFieldsInTab("tab3", "Environment", "Reporter", "Assignee", "Fix Version/s");
        assertFieldsInTab("tab4", "Labels", "Original Estimate", "Description");
    }

    private void assertFieldsInTabsForLegacyModeWorkStarted()
    {
        assertFieldsInTab("tab1", getFieldsInFirstTab());
        assertFieldsInTab("tab2", "Log Work", "Remaining Estimate", "Affects Version/s", "Component/s", "Due Date");
        assertFieldsInTab("tab3", "Environment", "Reporter", "Assignee", "Fix Version/s");
        assertFieldsInTab("tab4", "Labels", "Description");
    }

    private void assertFieldsInTabsForModernMode()
    {
        assertFieldsInTab("tab1", getFieldsInFirstTab());
        assertFieldsInTab("tab2", "Log Work", "Remaining Estimate", "Affects Version/s", "Component/s", "Due Date");
        assertFieldsInTab("tab3", "Environment", "Reporter", "Assignee", "Fix Version/s");
        assertFieldsInTab("tab4", "Labels", "Original Estimate", "Description");
    }

    private void enableModernMode()
    {
        administration.timeTracking().disable();
        administration.timeTracking().enable(TimeTracking.Mode.MODERN);
    }

    private void enableLegacyMode()
    {
        administration.timeTracking().disable();
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
    }

    private void assertTabsDontHaveErrors(final String... tabNames)
    {
        for (String tabName : tabNames)
        {
            assertFalse("Tab with name '" + tabName + "' had error class", page.getLinksWithExactText(tabName)[0].getClassName().contains(CLASS_HAS_ERRORS));
        }
    }

    private void assertTabsHaveErrors(final String... tabNames)
    {
        for (String tabName : tabNames)
        {
            assertEquals("Tab with name '" + tabName + "' did not have error class", CLASS_HAS_ERRORS, page.getLinksWithExactText(tabName)[0].getClassName());
        }
    }

    private void assertTabIsSelected(final String tabName)
    {
        final XPathLocator locator = new XPathLocator(tester, String.format("//li[@class='menu-item active-tab']/a/strong[contains(text(), '%s')]", tabName));
        final Node[] nodes = locator.getNodes();
        assertTrue("Could not find tab with name '" + tabName + "' that was selected.", nodes != null && nodes.length == 1);
    }

    private void assertFieldsInTab(final String tabId, final String... fields)
    {
        final IdLocator tabContainerLocator = new IdLocator(tester, tabId);
        text.assertTextSequence(tabContainerLocator, fields);
    }
}
