package com.atlassian.jira.webtests.ztests.screens.tabs;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.fields.EditFieldConstants.SUMMARY;

/**
 * Tests the functionality of Screen Tabs on the Create Issue screen.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.WORKLOGS })
public class TestFieldScreenTabsOnCreateIssue extends AbstractTestFieldScreenTabs
{
    protected void gotoTabScreenForIssueWithNoWork()
    {
        navigation.issue().goToCreateIssueForm("homosapien", "Bug");
        tester.setFormElement(SUMMARY, "This is the summary of the new issue");
    }

    protected void gotoTabScreenForIssueWithWorkStarted()
    {
        throw new UnsupportedOperationException("Can't be creating an issue that already has work.");
    }

    protected boolean canShowScreenForIssueWithWork()
    {
        return false;
    }

    @Override
    protected String[] getFieldsInFirstTab()
    {
        // no Issue Type in Create Issue form
        return new String[] {"Summary", "Priority"};
    }
}