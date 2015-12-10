package com.atlassian.jira.webtests.ztests.fields;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * See JRA-18855
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.SCHEMES })
public class TestFieldLayoutSchemes2 extends FuncTestCase
{
    public void testBugCustomDefaultSystem() throws Exception
    {
        administration.restoreData("TestFieldLayoutSchemes2.xml");
        // Goto MKY-1 This issue lives in Monkey Proj and is a Bug.
        // Therefore it should use the Default Field Config - Fix Version is visible
        assertFixVersionVisible("MKY-1");
        // MKY-2 is not a Bug, so it should use the Custom Field Config - Fix Version is Hidden
        assertFixVersionInvisible("MKY-2");

        // This project uses the reverse scheme
        // ANA-1 is a bug, therefore Fix Version is hidden
        assertFixVersionInvisible("ANA-1");
        // ANA-1 is not a bug, therefore Fix Version is shown
        assertFixVersionVisible("ANA-2");
    }

    private void assertFixVersionInvisible(final String issueKey)
    {
        navigation.issue().viewIssue(issueKey);
        // Click Link 'Edit' (id='edit_issue').
        tester.clickLink("edit-issue");
        tester.assertTextNotPresent("Fix Version/s");
    }

    private void assertFixVersionVisible(final String issueKey)
    {
        navigation.issue().viewIssue(issueKey);
        // Click Link 'Edit' (id='edit_issue').
        tester.clickLink("edit-issue");
        tester.assertTextPresent("Fix Version/s");
    }
}
