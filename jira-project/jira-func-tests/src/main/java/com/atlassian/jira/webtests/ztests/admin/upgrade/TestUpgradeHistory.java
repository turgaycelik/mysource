package com.atlassian.jira.webtests.ztests.admin.upgrade;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.date.DateUtil;
import com.atlassian.jira.webtests.LicenseKeys;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests the display of the upgrade history records in System Info and the separate viewing page.
 *
 * @since v4.1
 */
@WebTest ({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestUpgradeHistory extends FuncTestCase
{
    public void testUpgradeHistory3Versions() throws Exception
    {
        final Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("@@TIMESTAMP1@@", DateUtil.dateAsTimestamp(2009, Calendar.OCTOBER, 11, 9, 45));
        replacements.put("@@TIMESTAMP2@@", DateUtil.dateAsTimestamp(2009, Calendar.OCTOBER, 23, 12, 30));
        administration.restoreDataWithReplacedTokens("TestUpgradeHistory3Versions.xml", replacements);

        navigation.gotoAdminSection("system_info");

        text.assertTextSequence(new WebPageLocator(tester), "Last Upgrade", "v4.0#466", "Get more upgrade history details.");

        tester.clickLink("view_upgrade_history");

        // note: can't test the version just upgraded to since this will always change per release and we don't want to
        // have to upgrade the func tests
        text.assertTextSequence(new WebPageLocator(tester),
                "4.0", "466", "23/Oct/09 12:30 PM",
                "3.8", "209", "11/Oct/09 9:45 AM",
                "3.7.3", "187", "(inferred)", "Unknown");
    }

    public void testUpgradeHistory2VersionsInferred() throws Exception
    {
        final Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("@@TIMESTAMP1@@", DateUtil.dateAsTimestamp(2009, Calendar.OCTOBER, 23, 12, 30));
        administration.restoreDataWithReplacedTokens("TestUpgradeHistory2VersionsInferred.xml", replacements);

        navigation.gotoAdminSection("system_info");

        text.assertTextSequence(new WebPageLocator(tester), "Last Upgrade", "v4.0#466", "Get more upgrade history details.");

        tester.clickLink("view_upgrade_history");

        // note: can't test the version just upgraded to since this will always change per release and we don't want to
        // have to upgrade the func tests
        text.assertTextSequence(new WebPageLocator(tester),
                "4.0", "466", "23/Oct/09 12:30 PM",
                "3.8", "209", "(inferred from 207)", "Unknown");
    }

    public void testUpgradeHistoryAfterSetUp() throws Exception
    {
        resetupJira();

        navigation.gotoAdminSection("system_info");

        // test that no "from version" is displayed in the "Last Upgrade" row
        TableCellLocator locator = new TableCellLocator(tester, "jirainfo", 8, 0);
        assertions.getTextAssertions().assertTextPresent(locator, "Last Upgrade");

        locator = new TableCellLocator(tester, "jirainfo", 9, 1);
        assertions.getTextAssertions().assertTextNotPresent(locator, "(v");
        assertions.getTextAssertions().assertTextNotPresent(locator, "#");
        assertions.getTextAssertions().assertTextNotPresent(locator, ")");

        // check that there is only one row (+ header) in the full table
        tester.clickLink("view_upgrade_history");

        assertEquals("Found more than 2 rows in the upgrade history table",
                2, tester.getDialog().getWebTableBySummaryOrId("upgradehistory").getRowCount());
    }

    private void resetupJira()
    {
        // Revert to not set up state
        administration.restoreNotSetupInstance();
        tester.gotoPage("secure/SetupApplicationProperties.jspa");
        tester.assertTextPresent("Set Up Application Properties");
        // Fill in mandatory fields
        tester.setWorkingForm("jira-setupwizard");
        tester.setFormElement("title", "My JIRA");
        // Submit Application Properties with Default paths.
        tester.submit();
        // skip choosing bundle
        tester.gotoPage("/secure/SetupLicense!default.jspa");
        // License page
        tester.setWorkingForm("setupLicenseForm");
        tester.setFormElement("setupLicenseKey", LicenseKeys.V2_COMMERCIAL.getLicenseString());
        tester.submit();

        // Finish setup with standard values
        // Administrator Account setup
        tester.assertTextPresent("Set Up Administrator Account");
        tester.setFormElement("username", ADMIN_USERNAME);
        tester.setFormElement("password", ADMIN_USERNAME);
        tester.setFormElement("confirm", ADMIN_USERNAME);
        tester.setFormElement("fullname", "Mary Magdelene");
        tester.setFormElement("email", "admin@example.com");
        tester.submit();
        tester.assertTextPresent("Set Up Email Notifications");

        log("Noemail");
        tester.submit("finish");
        log("Noemail");
        // During SetupComplete, the user is automatically logged in
        // Assert that the user is logged in by checking if the profile link is present
        tester.assertLinkPresent("header-details-user-fullname");
    }

}
