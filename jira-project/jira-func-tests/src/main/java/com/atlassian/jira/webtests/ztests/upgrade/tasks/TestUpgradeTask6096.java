package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.UPGRADE_TASKS, Category.TIME_TRACKING, Category.WORKLOGS })
public class TestUpgradeTask6096 extends FuncTestCase
{
    private static final String HSP_1 = "HSP-1";

    public TestUpgradeTask6096(String name)
    {
        this.setName(name);
    }

    @Override
    public void setUpTest()
    {
        administration.restoreData("TestUpgradeTask6096.xml");
    }

    public void testWorklogsResolveMixedCaseUsers()
    {
        navigation.issue().viewIssue(HSP_1);
        clickWorkLogLink();

        // Make sure the names are mapped correctly
        text.assertTextPresent("Fred Normal");
        text.assertTextPresent("Recycled User");
        text.assertTextPresent("missing");
        text.assertTextPresent("mel role");
        text.assertTextPresent("deleted_user");

        // And that keys are not showing up unexpectedly
        text.assertTextNotPresent("ID12345");
        text.assertTextNotPresent("zapped");
        text.assertTextNotPresent("frEd");
        // don't try this with "meL" ...  that string is present from unrelated JS code
    }

    private void clickWorkLogLink()
    {
        if (page.isLinkPresentWithExactText("Work Log"))
        {
            getTester().clickLinkWithText("Work Log");
        }
    }
}
