package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.RENAME_USER;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;

/**
 * Responsible for verifying that references to change authors and assignee / reporter change values are forced
 * to lowercase, as required for renameable user functionality.
 *
 * @since v6.0
 */
@WebTest ({ FUNC_TEST, RENAME_USER, UPGRADE_TASKS })
public class TestUpgradeTask6038 extends FuncTestCase
{
    public static final String CHANGE_VALUE_LOCATOR = "//table[@id='changehistory_%s']/tbody/tr/td[%s]//span[@class='hist-value']";
    public static final String OLD_VALUE_COLUMN = "2";
    public static final String NEW_VALUE_COLUMN = "3";
    public static final String LOWERCASED_USERKEY = "bb";

    public void testUsernamesForcedToLowercase()
    {
        administration.restoreDataWithBuildNumber("TestUpgradeTask6038.xml", 6003);
        navigation.gotoPage("browse/DEMO-2?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel");
        text.assertTextPresent(locator.xpath(String.format(CHANGE_VALUE_LOCATOR, "10000", NEW_VALUE_COLUMN)).getText(), LOWERCASED_USERKEY);
        text.assertTextPresent(locator.xpath(String.format(CHANGE_VALUE_LOCATOR, "10001", NEW_VALUE_COLUMN)).getText(), LOWERCASED_USERKEY);
        text.assertTextPresent(locator.xpath(String.format(CHANGE_VALUE_LOCATOR, "10002", OLD_VALUE_COLUMN)).getText(), LOWERCASED_USERKEY);
        text.assertTextPresent(locator.xpath(String.format(CHANGE_VALUE_LOCATOR, "10003", OLD_VALUE_COLUMN)).getText(), LOWERCASED_USERKEY);
    }
}