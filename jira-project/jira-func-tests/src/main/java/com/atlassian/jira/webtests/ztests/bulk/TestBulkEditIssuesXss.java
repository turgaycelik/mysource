package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.navigation.BulkChangeWizard.BulkOperationsImpl.EDIT;
import static com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation.BulkChangeOption.ALL_PAGES;
import static com.atlassian.jira.webtests.Groups.USERS;

/**
 * Tests for Xss issues in bulk edit, like https://jdog.atlassian.com/browse/JRADEV-3358
 * This behavior was changed as a result of https://jira.atlassian.com/browse/JRA-29811 and OD/BTF differences
 * XSS in custom field names will be disabled in 6.0
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS })
public class TestBulkEditIssuesXss extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestBulkEditIssuesXss.xml");

    }

    public void testCustomFieldNameXss()
    {
        administration.addGlobalPermission(BULK_CHANGE, USERS);
        final IssueNavigatorNavigation inav = navigation.issueNavigator();
        inav.displayAllIssues();
        inav.bulkChange(ALL_PAGES)
                .selectAllIssues()
                .chooseOperation(EDIT);
        // name of the custom field is: mofo<span style="font-size:44px;">&copy;&trade;</span>
        tester.assertTextPresent("mofo");
        tester.assertTextPresent("&amp;copy;&amp;trade;");
        tester.assertTextNotPresent("&copy;&trade;");
    }
}
