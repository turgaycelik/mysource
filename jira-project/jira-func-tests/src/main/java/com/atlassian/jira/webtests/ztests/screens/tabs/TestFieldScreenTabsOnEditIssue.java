package com.atlassian.jira.webtests.ztests.screens.tabs;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the functionality of Screen Tabs on the Edit Issue screen.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.WORKLOGS })
public class TestFieldScreenTabsOnEditIssue extends AbstractTestFieldScreenTabs
{
    protected void gotoTabScreenForIssueWithNoWork()
    {
        navigation.issue().gotoEditIssue("HSP-1");
    }

    protected void gotoTabScreenForIssueWithWorkStarted()
    {
        navigation.issue().gotoEditIssue("HSP-2");
    }
}
