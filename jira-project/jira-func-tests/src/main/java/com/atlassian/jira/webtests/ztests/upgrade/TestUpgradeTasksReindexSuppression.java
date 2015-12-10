package com.atlassian.jira.webtests.ztests.upgrade;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * tests that the system property upgrade.reindex.allowed=false is set that no reindex takes place and that
 * a warning message is displayed and cleared  Unfortunately we can't test the in place upgrade - you will have
 * to do this testing manually for now
 *
 * @since v6.1
 */
@WebTest ({ Category.FUNC_TEST, Category.INFRASTRUCTURE, Category.SLOW_IMPORT })
public class TestUpgradeTasksReindexSuppression extends FuncTestCase
{

    @Override
    protected void setUpTest()
    {
        this.backdoor.systemProperties().setProperty("upgrade.reindex.allowed", "false");
    }


    public void testReindexClearsMessage()
    {
        administration.restoreData("TestUpgradeTasks.xml");
        navigation.gotoAdmin();
        assertNoUpgradeReindexMessage();

    }

    private void assertNoUpgradeReindexMessage()
    {
        assertions.getTextAssertions().assertTextNotPresent(new CssLocator(tester, ".aui-message.info"),
                "While upgrading JIRA");
    }
}
