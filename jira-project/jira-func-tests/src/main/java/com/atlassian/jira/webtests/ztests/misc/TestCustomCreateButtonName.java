package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Load a data set where the active workflow has a modified 'create' issue link name.
 * 
 */
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestCustomCreateButtonName extends FuncTestCase
{
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("CustomCreateIssueLink.xml");
    }

    public void tearDownTest()
    {
        administration.restoreBlankInstance();
    }

    public void testWorkflowPermissions()
    {
        navigation.issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);

        text.assertTextPresent(locator.css("#content > header h1"), "Create Issue");
        log("Checking presence of custom submit button name");
        tester.assertSubmitButtonValue("Create", "Custom Submit Name");
    }
}
