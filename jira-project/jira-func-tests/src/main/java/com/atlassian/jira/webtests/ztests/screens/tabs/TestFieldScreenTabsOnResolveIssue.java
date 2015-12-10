package com.atlassian.jira.webtests.ztests.screens.tabs;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the functionality of Screen Tabs on the Resolve Issue screen.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.WORKLOGS })
public class TestFieldScreenTabsOnResolveIssue extends AbstractTestFieldScreenTabs
{
    protected void gotoTabScreenForIssueWithNoWork()
    {
        navigation.issue().viewIssue("HSP-1");
        tester.clickLinkWithText("Resolve Issue");
    }

    protected void gotoTabScreenForIssueWithWorkStarted()
    {
        navigation.issue().viewIssue("HSP-2");
        tester.clickLinkWithText("Resolve Issue");
    }

    @Override
    protected String[] getFieldsInFirstTab()
    {
        return new String[] {"Resolution", "Summary", "Issue Type", "Priority"};
    }
}