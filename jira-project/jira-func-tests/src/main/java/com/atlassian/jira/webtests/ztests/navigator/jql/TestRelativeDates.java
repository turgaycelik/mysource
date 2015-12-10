package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Test realated to relative Date formats.
 * JRA-18205, CORE-131
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestRelativeDates extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestRelativeDates.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testRelativeDates() throws Exception
    {
        navigation.issue().viewIssue("TV-1");

        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("duedate", "12/Aug/09");
        tester.submit("Update");

        //minutes
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-100m\"", "TV-1");
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-100minutes\"", "TV-1");
        assertSearchWithError("project = \"TV-Bunnies\" and updated >= \"-100milestone\"", "Date value '-100milestone' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.");

        //hours
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-4h\"", "TV-1");
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-4hours\"", "TV-1");
        assertSearchWithError("project = \"TV-Bunnies\" and updated >= \"-4humor\"", "Date value '-4humor' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.");

        //days
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-4d\"", "TV-1");
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-4days\"", "TV-1");
        assertSearchWithError("project = \"TV-Bunnies\" and updated >= \"-4dummies\"", "Date value '-4dummies' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.");

        //weeks
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-10w, 3minutes\"", "TV-1");
        assertSearchWithResults("project = \"TV-Bunnies\" and updated >= \"-10weeks, 2d\"", "TV-1");
        assertSearchWithError("project = \"TV-Bunnies\" and updated >= \"-10wombat\"", "Date value '-10wombat' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.");
        assertSearchWithError("project = \"TV-Bunnies\" and updated >= \"-10whatever\"", "Date value '-10whatever' for field 'updated' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.");
    }
}
