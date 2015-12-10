package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.SystemTenantOnly;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the Time Tracking Admin page
 *
 * @since 3.12.2
 */
@SystemTenantOnly
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING, Category.TIME_TRACKING })
public class TestTimeTrackingAdmin extends FuncTestCase
{
    private static class ErrorMessages
    {
        private static final String ISSUE_OPS_DISABLED_ACTIVE = "The Issue Operations plugin is currently disabled, and thus users will not be able to log work done against any issues.";
        private static final String ISSUE_OPS_DISABLED_INACTIVE = "The Issue Operations plugin is currently disabled, and so even if you activate Time Tracking, users will still not be able to log work done against any issues. You should enable the plugin before activating Time Tracking.";
        private static final String LOG_WORK_DISABLED_ACTIVE = "The Log Work module is currently disabled, and thus users will not be able to log work done against any issues.";
        private static final String LOG_WORK_DISABLED_INACTIVE = "The Log Work module is currently disabled, and so even if you activate Time Tracking, users will still not be able to log work done against any issues. You should enable the module before activating Time Tracking.";
    }

    public void setUpTest()
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();
        administration.plugins().enablePlugin("com.atlassian.jira.plugin.system.issueoperations");
        administration.plugins().enablePluginModule("com.atlassian.jira.plugin.system.issueoperations", "com.atlassian.jira.plugin.system.issueoperations:log-work");
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);
    }

    /**
     * Error message should only appear when the plugin is disabled.
     * The message differs when Time Tracking is activated/deactivated.
     */
    public void testIssueOperationsPluginDisabled()
    {
        navigation.gotoAdminSection("timetracking");
        tester.assertTextNotPresent(ErrorMessages.ISSUE_OPS_DISABLED_ACTIVE);
        administration.timeTracking().disable();
        tester.assertTextNotPresent(ErrorMessages.ISSUE_OPS_DISABLED_INACTIVE);
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        administration.plugins().disablePlugin("com.atlassian.jira.plugin.system.issueoperations");
        navigation.gotoAdminSection("timetracking");
        tester.assertTextPresent(ErrorMessages.ISSUE_OPS_DISABLED_ACTIVE);
        administration.timeTracking().disable();
        tester.assertTextPresent(ErrorMessages.ISSUE_OPS_DISABLED_INACTIVE);
    }

    /**
     * Error message should only appear when the module is disabled.
     * The message differs when Time Tracking is activated/deactivated.
     */
    public void testLogWorkModuleDisabled()
    {
        navigation.gotoAdminSection("timetracking");
        tester.assertTextNotPresent(ErrorMessages.LOG_WORK_DISABLED_ACTIVE);
        administration.timeTracking().disable();
        tester.assertTextNotPresent(ErrorMessages.LOG_WORK_DISABLED_INACTIVE);
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.issueoperations", "com.atlassian.jira.plugin.system.issueoperations:log-work");
        navigation.gotoAdminSection("timetracking");
        tester.assertTextPresent(ErrorMessages.LOG_WORK_DISABLED_ACTIVE);
        administration.timeTracking().disable();
        tester.assertTextPresent(ErrorMessages.LOG_WORK_DISABLED_INACTIVE);
    }

    /**
     * If both the plugin and module are disabled (achieved by disabling the module first),
     * the error message for the plugin should be shown (as you can't re-enable a module without first enabling the plugin.
     */
    @SystemTenantOnly
    public void testIssueOperationsPluginDisabledAndLogWorkModuleDisabled()
    {
        navigation.gotoAdminSection("timetracking");
        tester.assertTextNotPresent(ErrorMessages.ISSUE_OPS_DISABLED_ACTIVE);
        tester.assertTextNotPresent(ErrorMessages.LOG_WORK_DISABLED_ACTIVE);
        administration.timeTracking().disable();
        tester.assertTextNotPresent(ErrorMessages.ISSUE_OPS_DISABLED_INACTIVE);
        tester.assertTextNotPresent(ErrorMessages.LOG_WORK_DISABLED_INACTIVE);
        administration.timeTracking().enable(TimeTracking.Mode.LEGACY);

        administration.plugins().disablePluginModule("com.atlassian.jira.plugin.system.issueoperations", "com.atlassian.jira.plugin.system.issueoperations:log-work");
        administration.plugins().disablePlugin("com.atlassian.jira.plugin.system.issueoperations");
        navigation.gotoAdminSection("timetracking");
        tester.assertTextPresent(ErrorMessages.ISSUE_OPS_DISABLED_ACTIVE);
        tester.assertTextNotPresent(ErrorMessages.LOG_WORK_DISABLED_ACTIVE);
        administration.timeTracking().disable();
        tester.assertTextPresent(ErrorMessages.ISSUE_OPS_DISABLED_INACTIVE);
        tester.assertTextNotPresent(ErrorMessages.LOG_WORK_DISABLED_INACTIVE);
    }

    public void testValidation() throws Exception
    {
        administration.timeTracking().disable();
        enableWithErrors("0", "7", "The number must be greater than 0.");
        enableWithErrors("-5", "7", "The number must be greater than 0.");

        enableWithErrors("5", "0", "The number must be greater than 0.");
        enableWithErrors("5", "-7", "The number must be greater than 0.");

        enableWithErrors("badtext", "7", "The specified value is not a number.");
        enableWithErrors("25.5.5", "7", "The specified value is not a number.");

        enableWithErrors("8", "badtext", "The specified value is not a number.");
        enableWithErrors("8", "25.5.5", "The specified value is not a number.");

        enableWithErrors("8.253", "7", "A maximum of two decimal points of accuracy is permitted.");
        enableWithErrors("8", "7.5423", "A maximum of two decimal points of accuracy is permitted.");
    }

    private void enableWithErrors(final String hoursPerDay, final String daysPerWeek, final String error)
    {
        tester.gotoPage("/secure/admin/jira/TimeTrackingAdmin!default.jspa");

        assertTrue(tester.getDialog().hasSubmitButton("Activate"));
        tester.setFormElement("hoursPerDay", hoursPerDay);
        tester.setFormElement("daysPerWeek", daysPerWeek);
        tester.checkCheckbox("timeTrackingFormat", "pretty");
        tester.selectOption("defaultUnit", "hour");
        tester.submit("Activate");

        text.assertTextPresent(new XPathLocator(tester, "//td[@class='jiraformheader']"), "Time Tracking is currently OFF.");
        text.assertTextPresent(new XPathLocator(tester, "//span[@class='errMsg']"), error);
    }


}
