package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.navigator.ColumnsCondition;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Some more tests for column context.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL, Category.SLOW_IMPORT})
public class TestContextColumns extends FuncTestCase
{
    private static boolean needsrestore = true;

    private static enum Project
    {
        ONE, TWO, THREE, FOUR
    }

    private static enum IssueType
    {
        BUG, IMPROVEMENT, FEATURE, TASK, SUBTASK
    }

    @Override
    protected void setUpTest()
    {
        if (needsrestore)
        {
            // do not use restoreData - it causes subtle cache issues
            administration.restoreDataSlowOldWay("TestJqlContextFields.xml");
            needsrestore = false;
        }

        navigation.login(ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, FRED_USERNAME);
    }

    //
    // Test to check that the columns implied by the project clause are correct.
    //
    public void testProjectField() throws Exception
    {
        assertJqlColumns("project = one", columnsForAdminUser(), getIssuesForProjects(Project.ONE));
        assertJqlColumns("project = two", columnsForAdminUser(), getIssuesForProjects(Project.TWO));
        assertJqlColumns("project = three", columnsForAdminUser(), getIssuesForProjects(Project.THREE));

        assertJqlColumns("project != two", columnsForAdminUser(), getIssuesAndRemoveProject(Project.TWO));
        assertJqlColumns("project != four", columnsForAdminUser(), getIssuesAndRemoveProject(Project.FOUR));
        assertJqlColumns("project != one", columnsForAdminUser(), getIssuesAndRemoveProject(Project.ONE));

        assertJqlColumns("project in (one, three)", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE));
        assertJqlColumns("project = one or project = three", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE));

        assertJqlColumns("project not in (one, two)", columnsForAdminUser(), getIssuesAndRemoveProject(Project.ONE, Project.TWO));
        assertJqlColumns("project != one and project != two", columnsForAdminUser(), getIssuesAndRemoveProject(Project.ONE, Project.TWO));
        assertJqlColumns("project not in (one, two, three)", columnsForAdminUser(), getIssuesAndRemoveProject(Project.ONE, Project.THREE, Project.TWO));
        assertJqlColumns("project != one and project != two and project != three", columnsForAdminUser(), getIssuesAndRemoveProject(Project.ONE, Project.THREE, Project.TWO));

        assertJqlColumns("project in (empty, two)", columnsForAdminUser(), getIssuesForProjects(Project.TWO));
        assertJqlColumns("project = empty or project = two", columnsForAdminUser(), getIssuesForProjects(Project.TWO));

        assertJqlColumns("project is not empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("project != empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("project != null", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("project not in (empty)", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("project not in (empty, three)", columnsForAdminUser(), getIssuesAndRemoveProject(Project.THREE));
        assertJqlColumns("project != empty and project != three", columnsForAdminUser(), getIssuesAndRemoveProject(Project.THREE));

        //Something a little more complex.
        assertJqlColumns("(project != one or project != two)", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("project != one or project = one", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("project in (one, two) and project = one", columnsForAdminUser(), getIssuesForProjects(Project.ONE));
        assertJqlColumns("project in (one, two) and project not in (three, one)", columnsForAdminUser(), getIssuesForProjects(Project.TWO));

        //Check for someone who cannot see all projects. Fred can't see project three.
        navigation.login(FRED_USERNAME);
        navigation.gotoDashboard();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        assertJqlColumns("project != two", columnsForFredUser(), Issue.ONE1, Issue.FOUR3);
    }

    //
    // Test to check that the columns implied by the issue type clause are correct.
    //
    public void testIssueType() throws Exception
    {
        assertJqlColumns("type = bug", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type = task", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("type = new\\ feature", columnsForAdminUser(), Issue.THREE1);

        assertJqlColumns("type != bug", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE2, Issue.ONE1, Issue.FOUR3));
        assertJqlColumns("type != task", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("type != new\\ feature", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));

        assertJqlColumns("type in (bug, task)", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type = bug or type = task", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type in ('new feature', task)", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("type = 'new feature' or type = task", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);

        assertJqlColumns("type not in (bug, task)", columnsForAdminUser(), Issue.TWO2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("not (type = bug or type = task)", columnsForAdminUser(), Issue.TWO2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("type not in ('new feature', task)", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("type != 'new feature' and not type = task", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("type in (empty, bug)", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("type = empty or type = bug", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR3);

        assertJqlColumns("type is not empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("type != empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("type not in (empty)", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("type not in (empty, bug)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE2, Issue.ONE1, Issue.FOUR3));
        assertJqlColumns("type is not empty and type != bug", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE2, Issue.ONE1, Issue.FOUR3));

        //Complex examples.
        assertJqlColumns("type in (bug, task) and type = task", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("type in (bug, task) and type not in (task, 'new feature')", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR3);
    }

    //
    //Test for fields that actually don't have any context.
    //
    public void testFieldsWithNoContext() throws Exception
    {
        //Check the assignee field.
        List<Issue> issues = getIssuesAndRemoveIssues(Issue.ONE1);
        assertJqlColumns("assignee = admin", columnsForAdminUser(), issues);
        assertJqlColumns("assignee != fred", columnsForAdminUser(), issues);
        assertJqlColumns("assignee is not empty", columnsForAdminUser(), issues);
        assertJqlColumns("assignee is empty", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("assignee in (admin, empty)", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("assignee not in (fred, empty)", columnsForAdminUser(), issues);

        assertJqlColumns("comment ~ donkey", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("comment !~ JIRA order by key asc", columnsForAdminUser(), Issue.FOUR2, Issue.THREE1);

        assertJqlColumns("created < 7d", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created <= 7d", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created > 1000", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created >= 1000", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created != now()", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created is not empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created != null", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created not in (1881, 34883)", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("created not in (empty, 47458)", columnsForAdminUser(), Issue.ALL_ISSUES);

        issues = Arrays.asList(Issue.TWO1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("duedate = empty", columnsForAdminUser(), getIssuesAndRemoveIssues(issues));
        assertJqlColumns("duedate is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(issues));
        assertJqlColumns("duedate in (empty)", columnsForAdminUser(), getIssuesAndRemoveIssues(issues));
        assertJqlColumns("duedate < 7d", columnsForAdminUser(), issues);
        assertJqlColumns("duedate <= 7d", columnsForAdminUser(), issues);
        assertJqlColumns("duedate > 1000", columnsForAdminUser(), issues);
        assertJqlColumns("duedate >= 1000", columnsForAdminUser(), issues);
        assertJqlColumns("duedate != now()", columnsForAdminUser(), issues);
        assertJqlColumns("duedate is not empty", columnsForAdminUser(), issues);
        assertJqlColumns("duedate != null", columnsForAdminUser(), issues);
        assertJqlColumns("duedate not in (1881, 34883)", columnsForAdminUser(), issues);
        assertJqlColumns("duedate not in (empty, 47458)", columnsForAdminUser(), issues);

        assertJqlColumns("description ~ suns", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("description !~ suns", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("description is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1, Issue.ONE1));
        assertJqlColumns("description is not empty", columnsForAdminUser(), Issue.THREE1, Issue.ONE1);

        assertJqlColumns("environment ~ jira", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("environment !~ jira", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("environment is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.ONE1));
        assertJqlColumns("environment is not empty", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);

        assertJqlColumns("originalEstimate = 5m", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("originalEstimate != 5m", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("originalEstimate in (5m, '5h 3m')", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("originalEstimate not in (5m, '5h 3m')", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("originalEstimate is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("originalEstimate = empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("originalEstimate in (empty, 5m)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("originalEstimate is not empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("originalEstimate != empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("originalEstimate not in (empty, 5m)", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("originalEstimate < 1d", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("originalEstimate <= 1d", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("originalEstimate > 5d", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("originalEstimate >= 5d", columnsForAdminUser(), Issue.THREE1);

        issues = Arrays.asList(Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2);
        assertJqlColumns("priority = major", columnsForAdminUser(), issues);
        assertJqlColumns("priority != major", columnsForAdminUser(), getIssuesAndRemoveIssues(issues));
        issues = Arrays.asList(Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2);
        assertJqlColumns("priority in (major, critical)", columnsForAdminUser(), issues);
        assertJqlColumns("priority not in (major, critical)", columnsForAdminUser(), getIssuesAndRemoveIssues(issues));
        issues = Arrays.asList(Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2);
        assertJqlColumns("priority in (major, empty)", columnsForAdminUser(), issues);
        assertJqlColumns("priority is not empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("priority != empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("priority not in (empty, trivial)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("priority >= major", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.FOUR1));
        assertJqlColumns("priority > major", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("priority < major", columnsForAdminUser(), Issue.TWO1, Issue.FOUR1);
        assertJqlColumns("priority <= major", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));

        assertJqlColumns("remainingEstimate = 4m", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("remainingEstimate != 4m", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("remainingEstimate in (4m, '5h 3m')", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("remainingEstimate not in (4m, '5h 3m')", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("remainingEstimate is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("remainingEstimate = empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("remainingEstimate in (empty, 4m)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("remainingEstimate is not empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("remainingEstimate != empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("remainingEstimate not in (empty, 4m)", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("remainingEstimate < 1d", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("remainingEstimate <= 1d", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("remainingEstimate > 5d", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("remainingEstimate >= 5d", columnsForAdminUser(), Issue.THREE1);

        assertJqlColumns("reporter = admin", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("reporter != admin", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("reporter in (admin, fred)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("reporter not in (fred, dylan)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("reporter is empty", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("reporter = empty", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("reporter in (empty, fred)", columnsForAdminUser(), Issue.THREE1, Issue.ONE1);
        assertJqlColumns("reporter is not empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("reporter != empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("reporter not in (empty, fred)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("resolution = \"Won't Fix\"", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("resolution != \"Won't Fix\"", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("resolution in (\"Won't Fix\", fixed)", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("resolution not in (duplicate, fixed)", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("resolution is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolution = empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolution in (empty, duplicate)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("resolution is NOT empty", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolution != empty", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolution not in (empty, duplicate)", columnsForAdminUser(), Issue.ONE1);

        assertJqlColumns("resolutionDate < '2009/08/02'", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("resolutionDate <= '2009/08/02'", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("resolutionDate > '2009/08/02'", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("resolutionDate >= '2009/08/02'", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("resolutionDate != now()", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate not in (now(), '2009/02/20')", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolutionDate = empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolutionDate in (empty, now())", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("resolutionDate is not empty", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate != null", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("resolutionDate not in (null, now())", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);

        assertJqlColumns("summary ~ suns", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("summary !~ suns order by key desc", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("summary is not empty order by key desc", columnsForAdminUser(), Issue.ALL_ISSUES);

        assertJqlColumns("timeSpent = 1m", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("timeSpent != 1m", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("timeSpent in (1m, '5h 3m')", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("timeSpent not in (1m, '5h 3m')", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("timeSpent is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("timeSpent = empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("timeSpent in (empty, 1m)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("timeSpent is not empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("timeSpent != empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("timeSpent not in (empty, 1m)", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("timeSpent < 1d", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("timeSpent <= 1d", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("timeSpent > 5d", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("timeSpent >= 5d", columnsForAdminUser(), Issue.THREE1);

        assertJqlColumns("updated < 7d", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated <= 7d", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated > 1000", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated >= 1000", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated != 2004-08-10", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated is not empty", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated != null", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated not in (1881, 34883)", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("updated not in (empty, 47458)", columnsForAdminUser(), Issue.ALL_ISSUES);

        assertJqlColumns("votes = 0", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("votes != 0", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("votes in (0, 2, 4)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("votes not in (0, 2, 4)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("votes < 1", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("votes <= 1", columnsForAdminUser(), Issue.ALL_ISSUES);
        assertJqlColumns("votes > 0", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("votes >= 0", columnsForAdminUser(), Issue.ALL_ISSUES);

        assertJqlColumns("workRatio = 20", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("workRatio != 20", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("workRatio in (20, 21)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("workRatio not in (20, 39393)", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("workRatio is empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("workRatio = empty", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1, Issue.THREE1));
        assertJqlColumns("workRatio in (empty, 20)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("workRatio is not empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("workRatio != empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("workRatio not in (empty, 20)", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("workRatio < 10", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("workRatio <= 10", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("workRatio > 10", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("workRatio >= 10", columnsForAdminUser(), Issue.TWO1);
    }

    //
    // Test the context for the category clause.
    //
    public void testCategoryContext() throws Exception
    {
        assertJqlColumns("category = catone", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE));
        assertJqlColumns("category = cattwo", columnsForAdminUser(), getIssuesForProjects(Project.FOUR));

        assertJqlColumns("category != catone", columnsForAdminUser(), getIssuesForProjects(Project.FOUR));
        assertJqlColumns("category != catthree", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));

        assertJqlColumns("category in (catone, cattwo)", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category = catone or category = cattwo", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));

        assertJqlColumns("category not in (cattwo, catthree)", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE));
        assertJqlColumns("not category = cattwo and category != catthree", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE));

        assertJqlColumns("category is empty", columnsForAdminUser(), getIssuesForProjects(Project.TWO));
        assertJqlColumns("category = empty", columnsForAdminUser(), getIssuesForProjects(Project.TWO));
        assertJqlColumns("category in (empty)", columnsForAdminUser(), getIssuesForProjects(Project.TWO));
        assertJqlColumns("category in (empty, catone)", columnsForAdminUser(), getIssuesForProjects(Project.TWO, Project.ONE, Project.THREE));
        assertJqlColumns("category = empty or category = catone", columnsForAdminUser(), getIssuesForProjects(Project.TWO, Project.ONE, Project.THREE));
        assertJqlColumns("category in (empty, catone, catthree)", columnsForAdminUser(), getIssuesForProjects(Project.TWO, Project.ONE, Project.THREE));

        assertJqlColumns("category is not empty", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category != empty", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category not in (empty)", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category not in (empty, catthree)", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category != empty and category != catthree", columnsForAdminUser(), getIssuesForProjects(Project.ONE, Project.THREE, Project.FOUR));
        assertJqlColumns("category not in (empty, catone)", columnsForAdminUser(), getIssuesForProjects(Project.FOUR));
        assertJqlColumns("not (category is empty or category = catone)", columnsForAdminUser(), getIssuesForProjects(Project.FOUR));

        navigation.login(FRED_USERNAME);
        assertJqlColumns("category = catone", columnsForFredUser(), getIssuesForProjects(Project.ONE));
        assertJqlColumns("category != catthree", columnsForFredUser(), Issue.ONE1, Issue.FOUR3);
    }

    //
    // Test the context for the affectedVersion clause.
    //
    public void testAffectedVersion() throws Exception
    {
        assertSearchingBySystemVersionField("affectedVersion", columnsForAdminUser());
    }

    //
    // Test the context for fixVersion clause.
    //
    public void testFixVersion()
    {
        assertSearchingBySystemVersionField("fixVersion", columnsForAdminUser());
    }

    //
    // Test for the component system field.
    //
    public void testComponent() throws Exception
    {
        /*
            Test data contains:

            proj:one -> comp:{one, two, three}
            proj:two -> comp:{two, twoonly}
            proj:three -> comp:{three}
            proj:four -> comp:empty

         */

        assertJqlColumns("component = one", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("component = two", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("component = three", columnsForAdminUser(), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component = twoonly", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("component != one", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component != two", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component != three", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("component != twoonly", columnsForAdminUser(), Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("component in (one, two)", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("component = one or component = two", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("component in (one, three)", columnsForAdminUser(), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component = one or component = three", columnsForAdminUser(), Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component in (twoonly)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("component = twoonly", columnsForAdminUser(), Issue.TWO1);

        assertJqlColumns("component not in (one, two)", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("not (component = one or component = two)", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component not in (twoonly, two)", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component != twoonly and component != two", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("component not in (one, two, three)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("component != one and not (component = two or component = three)", columnsForAdminUser(), Issue.TWO1);

        assertJqlColumns("component is empty", columnsForAdminUser(), Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component = empty", columnsForAdminUser(), Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component in (empty, twoonly)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component is empty or component = twoonly", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("component in (empty, two)", columnsForAdminUser(), Issue.TWO2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("not (component is not empty and component != two)", columnsForAdminUser(), Issue.TWO2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("component is not empty", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component != null", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("component not in (empty, three)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("not (component = empty or component = three)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("component not in (empty, two, three, one)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("component is not empty and component != two and component != three and component != one", columnsForAdminUser(), Issue.TWO1);
    }

    //
    // Test for the issuekey system field.
    //
    public void testIssueContext() throws Exception
    {
        assertJqlColumns("issuekey = 'one-1'", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("issuekey = 'two-1'", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("issuekey = 'three-1'", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("issuekey = 'three-2'", columnsForAdminUser(), Issue.THREE2);
        assertJqlColumns("issuekey = 'four-1'", columnsForAdminUser(), Issue.FOUR1);

        assertJqlColumns("issuekey != 'one-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1));
        assertJqlColumns("issuekey != 'two-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("issuekey != 'three-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));
        assertJqlColumns("issuekey != 'three-2'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE2));
        assertJqlColumns("issuekey != 'four-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.FOUR1));

        assertJqlColumns("issuekey in ('one-1', 'two-1')", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("issuekey = 'one-1' or key = 'two-1'", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("issuekey in ('three-2', 'three-1')", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("issuekey = 'three-2' or key = 'three-1'", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);

        assertJqlColumns("issuekey not in ('one-1', 'two-1')", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.ONE1, Issue.TWO1));
        assertJqlColumns("issuekey not in ('four-1', 'three-1')", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.FOUR1, Issue.THREE1));

        assertJqlColumns("key > 'three-1'", columnsForAdminUser(), Issue.THREE2);
        assertJqlColumns("key < 'three-2'", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("key >= 'three-1'", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
        assertJqlColumns("key >= 'one-1'", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("key <= 'three-1'", columnsForAdminUser(), Issue.THREE1);
        assertJqlColumns("key <= 'two-1'", columnsForAdminUser(), Issue.TWO1);
    }

    //
    // Test for the "level" field.
    //
    public void testLevelContext() throws Exception
    {
        /*
            proj:one -> level:{oneadmin}
            proj:two -> level:{twoonly}
            proj:three -> level:{oneadmin, threeonly}
            proj:four -> level:{fouronly}

         */

        assertJqlColumns("level = oneadmin", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("level = threeonly", columnsForAdminUser(), Issue.THREE2);
        assertJqlColumns("level = fouronly", columnsForAdminUser(), Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("level != oneadmin", columnsForAdminUser(), Issue.THREE2, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != threeonly", columnsForAdminUser(), Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != fouronly", columnsForAdminUser(), Issue.THREE2, Issue.ONE1);

        assertJqlColumns("level in (oneadmin, threeonly)", columnsForAdminUser(), Issue.THREE2, Issue.ONE1);
        assertJqlColumns("level = oneadmin or level = threeonly", columnsForAdminUser(), Issue.THREE2, Issue.ONE1);
        assertJqlColumns("level in (fouronly)", columnsForAdminUser(), Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level = fouronly", columnsForAdminUser(), Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("level not in (oneadmin, threeonly)", columnsForAdminUser(), Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != oneadmin and level != threeonly", columnsForAdminUser(), Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (oneadmin, fouronly)", columnsForAdminUser(), Issue.THREE2);
        assertJqlColumns("level != oneadmin and level != fouronly", columnsForAdminUser(), Issue.THREE2);

        assertJqlColumns("level is empty", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level = empty", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level in (empty)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level in (empty, threeonly)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3);
        assertJqlColumns("level is empty or level = threeonly", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3);

        assertJqlColumns("level is not empty", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != empty", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (empty)", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (empty, empty)", columnsForAdminUser(), Issue.THREE2, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level not in (empty, threeonly)", columnsForAdminUser(), Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("level != empty and level != threeonly", columnsForAdminUser(), Issue.ONE1, Issue.FOUR2, Issue.FOUR1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("level = oneadmin", columnsForFredUser(), Issue.ONE1);
    }

    //
    // Test for the "parent" field.
    //
    public void testParentContext() throws Exception
    {
        /*
            parent:two-2 = two-1 => (proj:two, issuetype: subtask)
            parent:four-2 = four-1 => (project:four, issuetype: subtask}
         */

        assertJqlColumns("parent = 'two-1'", columnsForAdminUser(), Issue.TWO2);
        assertJqlColumns("parent = 'four-1'", columnsForAdminUser(), Issue.FOUR2);

        assertJqlColumns("parent != 'four-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.FOUR2));
        assertJqlColumns("parent != 'three-1'", columnsForAdminUser(), getIssuesAndRemoveIssues());
        assertJqlColumns("parent != 'two-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO2));

        assertJqlColumns("parent in ('two-1', 'three-1')", columnsForAdminUser(), Issue.TWO2);
        assertJqlColumns("parent in ('two-1', 'four-1')", columnsForAdminUser(), Issue.TWO2, Issue.FOUR2);
        assertJqlColumns("parent = 'two-1' or parent = 'three-1'", columnsForAdminUser(), Issue.TWO2);
        assertJqlColumns("parent = 'two-1' or parent = 'four-1'", columnsForAdminUser(), Issue.TWO2, Issue.FOUR2);

        assertJqlColumns("parent not in ('two-1', 'three-1')", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO2));
        assertJqlColumns("parent != 'two-1' and  parent != 'three-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO2));
        assertJqlColumns("parent not in ('two-1', 'four-1')", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.FOUR2, Issue.TWO2));
        assertJqlColumns("parent != 'two-1' and  parent != 'four-1'", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.FOUR2, Issue.TWO2));
        assertJqlColumns("parent not in ('one-1', 'three-1')", columnsForAdminUser(), getIssuesAndRemoveIssues());
        assertJqlColumns("not parent = 'one-1' and parent != 'three-1'", columnsForAdminUser(), getIssuesAndRemoveIssues());
    }

    public void testSavedFilter() throws Exception
    {
        /*
            filter:onefilter = { (issuekey = 'one-1')[admin not shared], ( project = two) [fred shared globally] }
            filter:taskfilter = { (type = task)[admin not shared] }
            filter:threefilter = { (issuekey =< 'three-1')[admin shared globally] }
         */

        assertJqlColumns("savedFilter = taskfilter", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("savedFilter = onefilter", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.ONE1);
        assertJqlColumns("savedFilter = threefilter", columnsForAdminUser(), Issue.THREE1);

        assertJqlColumns("savedFilter != taskfilter", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO1));
        assertJqlColumns("savedFilter != onefilter", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.TWO2, Issue.TWO1, Issue.ONE1));
        assertJqlColumns("savedFilter != threefilter", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1));

        assertJqlColumns("savedFilter in (taskfilter, threefilter)", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("savedFilter = taskfilter or filter = threefilter", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertJqlColumns("savedFilter in (onefilter, threefilter)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("savedFilter = onefilter or filter = threefilter", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("savedFilter not in (taskfilter, threefilter)", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1, Issue.TWO1));
        assertJqlColumns("savedFilter != taskfilter and savedFilter != threefilter", columnsForAdminUser(), getIssuesAndRemoveIssues(Issue.THREE1, Issue.TWO1));
        assertJqlColumns("savedFilter not   in (onefilter, threefilter)", columnsForAdminUser(), Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("savedFilter != onefilter and filter != threefilter", columnsForAdminUser(), Issue.THREE2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("savedFilter not   in (onefilter, taskfilter)", columnsForAdminUser(), Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("savedFilter != onefilter and savedFilter != taskfilter", columnsForAdminUser(), Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("savedFilter = onefilter", columnsForFredUser(), Issue.TWO2, Issue.TWO1);
    }

    public void testStatusContext() throws Exception
    {
        /*
            status:two -> {(proj:two, type:task)}
            status:three -> {(proj:three, *), (proj:four, *)}
            status:open -> {(*, *)}
         */

        assertJqlColumns("status = open", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("status = two", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("status = three", columnsForAdminUser(), Issue.THREE1, Issue.FOUR3, Issue.FOUR1);

        assertJqlColumns("status != open", columnsForAdminUser(), Issue.TWO1, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status != two", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("status != three", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR2);

        assertJqlColumns("status in (open, two)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("status = open or status = two", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("status in (two, three)", columnsForAdminUser(), Issue.TWO1, Issue.THREE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status = two or status = three", columnsForAdminUser(), Issue.TWO1, Issue.THREE1, Issue.FOUR3, Issue.FOUR1);

        assertJqlColumns("status not in (open, two)", columnsForAdminUser(), Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status != open and status != two", columnsForAdminUser(), Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertJqlColumns("status not in (three, two)", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("status != three and status != two", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("status not in (three, two, open)", columnsForAdminUser(), Issue.ONE1);
        assertJqlColumns("not (status = three or status = two or status = open)", columnsForAdminUser(), Issue.ONE1);

        assertJqlColumns("status in (empty, two)", columnsForAdminUser(), Issue.TWO1);
        assertJqlColumns("status = empty or status = two", columnsForAdminUser(), Issue.TWO1);

        assertJqlColumns("status is not empty", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("status not in (empty)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("status not in (empty, three, two)", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("not (status = empty or status = three or status = two)", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.ONE1, Issue.FOUR2);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("status = three", columnsForFredUser(), Issue.FOUR3);
    }

    public void testDatePicker() throws Exception
    {
        /*
            DatePickerBoth -> {("one", IssueType.BUG)}
            DatePickerComplex -> {(*, task), (one, *), (two, improvement)}
            DatePickerGlobal -> {(*, *)}
            DatePickerProject -> {(two, *), (three, *)}
            DatePickerType -> {(*, IssueType.IMPROVEMENT)}
         */
        assertDateCustomField("datepickerglobal", "2009-08-15",
                columnsForAdminUser(),
                Issue.TWO1, Issue.ONE1, Issue.FOUR1);

        assertDateCustomField("DatePickerComplex", "2009-08-15",
                columnsForAdminUser(),
                Issue.ONE1);

        assertDateCustomField("DatePickerBoth", "2009-08-15", columnsForAdminUser(), Issue.ONE1);

        assertDateCustomField("DatePickerProject", "2009-08-15", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);

        assertDateCustomField("DatePickerType", "2009-08-15", columnsForAdminUser(), Issue.FOUR1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("DatePickerProject = 2009-08-15",
                columnsForFredUser(),
                Issue.TWO1);
    }

    public void testDatePickerSearchingForEmptyValues() throws Exception
    {
        assertDateCustomFieldIsEmpty("datepickerglobal", "2009-08-15",
                columnsForAdminUser(),
                Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2);

        assertDateCustomFieldIsEmpty("DatePickerComplex", "2009-08-15", Collections.<Field>emptyList());

        assertDateCustomFieldIsEmpty("DatePickerBoth", "2009-08-15", Collections.<Field>emptyList());

        assertDateCustomFieldIsEmpty("DatePickerProject", "2009-08-15", columnsForAdminUser(), Issue.TWO2, Issue.THREE2);

        assertDateCustomFieldIsEmpty("DatePickerType", "2009-08-15", Collections.<Field>emptyList());
    }

    public void testDateTimePicker() throws Exception
    {
        /*
            DateTimeBoth -> {(Project.FOUR, IssueType.BUG)}
            DateTimeComplex -> {(Project.FOUR, IssueType.IMPROVEMENT), (Project.THREE, *), (*, IssueType.TASK)}
            DateTimeGlobal ->  {(*, *)}
            DateTimeProject -> {(Project.ONE, *)};
            DateTimeType -> {(*, IssueType.FEATURE)};
        */
        assertDateCustomField("DateTimeGlobal", "2009-08-07",
                columnsForAdminUser(),
                Issue.ONE1, Issue.FOUR3);

        assertDateCustomField("DateTimeComplex", "2009-08-07",
                columnsForAdminUser(),
                Issue.TWO1);

        assertDateCustomField("DateTimeBoth", "2009-08-07", columnsForAdminUser(),
                Issue.FOUR3);

        assertDateCustomField("DateTimeProject", "2009-08-07", columnsForAdminUser(),
                Issue.ONE1);

        assertDateCustomField("DateTimeType", "2009-08-07", columnsForAdminUser(),
                Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("DateTimeComplex = 2009-08-07",
                columnsForFredUser(),
                Issue.TWO1);
    }

    public void testDateTimePickerSearchingForEmptyValues() throws Exception
    {
        assertDateCustomFieldIsEmpty("DateTimeGlobal", "2009-08-07",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);

        assertDateCustomFieldIsEmpty("DateTimeComplex", "2009-08-07",
                columnsForAdminUser(),
                Issue.THREE2, Issue.THREE1, Issue.FOUR1);

        assertDateCustomFieldIsEmpty("DateTimeBoth", "2009-08-07", Collections.<Field>emptyList());

        assertDateCustomFieldIsEmpty("DateTimeProject", "2009-08-07", Collections.<Field>emptyList());

        assertDateCustomFieldIsEmpty("DateTimeType", "2009-08-07", Collections.<Field>emptyList());
    }

    public void testFreeTextField() throws Exception
    {
        /*
            FreeTextBoth -> {(Project.TWO, IssueType.TASK)}
            FreeTextComplex -> {(Project.ONE, IssueType.FEATURE), (Project.FOUR, *), (*, IssueType.BUG)}
            FreeTextGlobal -> {(*, *)}
            FreeTextProject -> {(Project.FOUR, *), (Project.THREE, *)}
            FreeTextType -> {(*, IssueType.BUG)}
         */

        assertTextField("freetextglobal", columnsForAdminUser(), Issue.THREE1);
        assertTextField("FreeTextBoth", columnsForAdminUser(), Issue.TWO1);
        assertTextField("FreeTextProject", columnsForAdminUser(), Issue.THREE2, Issue.FOUR3, Issue.FOUR1);
        assertTextField("FreeTextType", columnsForAdminUser(), Issue.THREE2, Issue.ONE1);
        assertTextField("FreeTextComplex", columnsForAdminUser(), Issue.FOUR1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("freetextproject ~ match order by key desc", columnsForFredUser(), Issue.FOUR3);
    }

    public void testFreeTextFieldSearchingForEmptyValues() throws Exception
    {
        assertTextFieldIsEmpty("freetextglobal", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertTextFieldIsEmpty("FreeTextBoth", Collections.<Field>emptyList());
        assertTextFieldIsEmpty("FreeTextProject", columnsForAdminUser(), Issue.THREE1, Issue.FOUR2);
        assertTextFieldIsEmpty("FreeTextType", columnsForAdminUser(), Issue.FOUR3);
        assertTextFieldIsEmpty("FreeTextComplex", columnsForAdminUser(), Issue.THREE2, Issue.FOUR3, Issue.FOUR2);
    }

    public void testTextField() throws Exception
    {
        /*
            TextBoth -> {(Project.TWO, IssueType.SUBTASK)}
            TextComplex -> {(Project.THREE, IssueType.FEATURE), (*, IssueType.TASK)}
            TextGlobal -> {(*, *)};
            TextProject -> {(Project.ONE, *)}
            TextType -> {(*, IssueType.IMPROVEMENT)}
        */

        assertTextField("textglobal", columnsForAdminUser(), Issue.TWO2, Issue.ONE1);
        assertTextField("TextBoth", columnsForAdminUser(), Issue.TWO2);
        assertTextField("TextProject", columnsForAdminUser(), Issue.ONE1);
        assertTextField("TextType", columnsForAdminUser(), Issue.FOUR1);
        assertTextField("TextComplex", columnsForAdminUser(), Issue.TWO1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("TextComplex ~ match order by key desc", columnsForFredUser(), Issue.TWO1);
    }

    public void testTextFieldSearchingForEmptyValues()
    {
        assertTextFieldIsEmpty("textglobal", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertTextFieldIsEmpty("TextBoth", Collections.<Field>emptyList());
        assertTextFieldIsEmpty("TextProject", Collections.<Field>emptyList());
        assertTextFieldIsEmpty("TextType", Collections.<Field>emptyList());
        assertTextFieldIsEmpty("TextComplex", columnsForAdminUser(), Issue.THREE1);
    }

    public void testUrlField() throws Exception
    {
        /*
            UrlBoth -> {(Project.FOUR, IssueType.SUBTASK)}
            UrlComplex -> {(Project.THREE, *), (*, IssueType.IMPROVEMENT)}
            UrlGlobal -> {(Context.GLOBAL)}
            UrlProject -> {(Project.TWO, *), (Project.THREE, *)}
            UrlType ->  {(*, IssueType.BUG)}
        */

        assertUrlField("urlglobal", columnsForAdminUser(), Issue.FOUR2);
        assertUrlField("urlBoth", columnsForAdminUser(), Issue.FOUR2);
        assertUrlField("urlProject", columnsForAdminUser(), Issue.TWO1, Issue.THREE1);
        assertUrlField("urlType", columnsForAdminUser(), Issue.ONE1);
        assertUrlField("urlComplex", columnsForAdminUser(), Issue.THREE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("urlProject = '" + URLEncoder.encode("http://match.com", "UTF-8") + "'", columnsForFredUser(), Issue.TWO1);
    }

    public void testUrlFieldSearchingForEmptyValues() throws Exception
    {
        assertStringFieldIsEmpty("urlglobal", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR1);
        assertStringFieldIsEmpty("urlBoth", Collections.<Field>emptyList());
        assertStringFieldIsEmpty("urlProject", columnsForAdminUser(), Issue.TWO2, Issue.THREE2);
        assertStringFieldIsEmpty("urlType", columnsForAdminUser(), Issue.THREE2, Issue.FOUR3);
        assertStringFieldIsEmpty("urlComplex", columnsForAdminUser(), Issue.THREE2, Issue.FOUR1);
    }

    public void testReadOnlyField() throws Exception
    {
        /*
            ReadTextBoth -> {(Project.THREE, IssueType.BUG)}
            ReadTextComplex -> {(Project.THREE, *), (Project.TWO, *), (*, IssueType.IMPROVEMENT)}
            ReadTextGlobal ->  {(*, *)}
            ReadTextProject -> {(Project.FOUR, *)}
            ReadTextType -> {(*, IssueType.FEATURE), (*, IssueType.TASK)}
        */
        assertTextField("readtextglobal", columnsForAdminUser(), Issue.FOUR1);
        assertTextField("readtextBoth", columnsForAdminUser(), Issue.THREE2);
        assertTextField("readtextProject", columnsForAdminUser(), Issue.FOUR1);
        assertTextField("readtextType", columnsForAdminUser(), Issue.TWO1);
        assertTextField("readtextComplex", columnsForAdminUser(), Issue.TWO2);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("ReadTextComplex ~ 'match'", columnsForFredUser(), Issue.TWO2);
    }

    public void testUserPickerField() throws Exception
    {
        /*
            UserBoth -> {(Project.TWO, IssueType.SUBTASK)}
            UserComplex -> {(Project.TWO, IssueType.TASK), (*,IssueType.BUG)}
            UserGlobal -> {(*, *)}
            UserProject -> {(Project.ONE, *), (Project.THREE, *)}
            UserType -> {(*, IssueType.FEATURE)}
        */
        assertUserField("UserGlobal", columnsForAdminUser(), Issue.ONE1);
        assertUserField("userBoth", columnsForAdminUser(), Issue.TWO2);
        assertUserField("userProject", columnsForAdminUser(), Issue.ONE1);
        assertUserField("userType", columnsForAdminUser(), Issue.THREE1);
        assertUserField("userComplex", columnsForAdminUser(), Issue.ONE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("userProject = 'admin'", columnsForFredUser(), Issue.ONE1);
    }

    public void testUserPickerFieldSearchingForEmptyValues() throws Exception
    {
        assertUserFieldIsEmpty("UserGlobal", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertUserFieldIsEmpty("userBoth", Collections.<Field>emptyList());
        assertUserFieldIsEmpty("userProject", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
        assertUserFieldIsEmpty("userType", Collections.<Field>emptyList());
        assertUserFieldIsEmpty("userComplex", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.FOUR3);
    }

    public void testMultiUserPickerField() throws Exception
    {
        /*
            MultiUserBoth -> {(Project.FOUR, IssueType.BUG)}
            MultiUserComplex {(Project.THREE, *), (Project.ONE, IssueType.BUG)}
            MultiUserGlobal -> {(*, *)}
            MultiUserProject -> {(Project.TWO, *), (Project.THREE, *)}
            MultiUserType -> {(*, IssueType.TASK)}
         */
        assertUserField("MultiUserGlobal", columnsForAdminUser(), Issue.TWO1, Issue.FOUR3);
        assertUserField("multiuserBoth", columnsForAdminUser(), Issue.FOUR3);
        assertUserField("multiuserProject", columnsForAdminUser(), Issue.TWO1);
        assertUserField("multiuserType", columnsForAdminUser(), Issue.TWO1);
        assertUserField("multiuserComplex", columnsForAdminUser(), Issue.THREE2);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("multiuserProject = 'admin'", columnsForFredUser(), Issue.TWO1);
    }

    public void testMultiUserPickerFieldSearchingForEmptyValues() throws Exception
    {
        assertUserFieldIsEmpty("MultiUserGlobal", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertUserFieldIsEmpty("multiuserBoth", Collections.<Field>emptyList());
        assertUserFieldIsEmpty("multiuserProject", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.THREE1);
        assertUserFieldIsEmpty("multiuserType", Collections.<Field>emptyList());
        assertUserFieldIsEmpty("multiuserComplex", columnsForAdminUser(), Issue.THREE1, Issue.ONE1);
    }

    public void testGroupPickerField() throws Exception
    {
        /*
            GroupBoth -> {(Project.THREE, IssueType.BUG)}
            GroupComplex -> {(Project.THREE, *), (Project.ONE, IssueType.BUG)}
            GroupGlobal -> {(*, *)}
            GroupProject -> {(Project.FOUR, *)}
            GroupType -> {(*, IssueType.FEATURE)}
        */

        assertGroupField("GroupGlobal", columnsForAdminUser(), Issue.TWO2);
        assertGroupField("GroupBoth", columnsForAdminUser(), Issue.THREE2);
        assertGroupField("GroupProject", columnsForAdminUser(), Issue.FOUR1);
        assertGroupField("GroupType", columnsForAdminUser(), Issue.THREE1);
        assertGroupField("GroupComplex", columnsForAdminUser(), Issue.THREE2, Issue.ONE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("GroupComplex = 'jira-developers'", columnsForFredUser(), Issue.ONE1);
    }

    public void testGroupPikerFieldSearchingForEmptyValues() throws Exception
    {
        assertGroupFieldIsEmpty("GroupGlobal", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertGroupFieldIsEmpty("GroupBoth", Collections.<Field>emptyList());
        assertGroupFieldIsEmpty("GroupProject", columnsForAdminUser(), Issue.FOUR3, Issue.FOUR2);
        assertGroupFieldIsEmpty("GroupType", Collections.<Field>emptyList());
        assertGroupFieldIsEmpty("GroupComplex", columnsForAdminUser(), Issue.THREE1);
    }

    public void testMultiGroupPickerField() throws Exception
    {
        /*
            MultiGroupBoth -> {(Project.THREE, IssueType.BUG)}
            MultiGroupComplex -> {(Project.THREE, *), (Project.ONE, IssueType.BUG)}
            MultiGroupGlobal -> {(*, *)}
            MultiGroupProject -> {(Project.FOUR, *)}
            MultiGroupType -> {(*, IssueType.FEATURE)}
        */

        assertGroupField("MultiGroupGlobal", columnsForAdminUser(), Issue.TWO1);
        assertGroupField("MultiGroupBoth", columnsForAdminUser(), Issue.THREE2);
        assertGroupField("MultiGroupProject", columnsForAdminUser(), Issue.FOUR3);
        assertGroupField("MultiGroupType", columnsForAdminUser(), Issue.THREE1);
        assertGroupField("MultiGroupComplex", columnsForAdminUser(), Issue.ONE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("GroupComplex = 'jira-developers'", columnsForFredUser(), Issue.ONE1);
    }

    public void testMultiGroupPickerFieldSearchingForEmptyValues() throws Exception
    {
        assertGroupFieldIsEmpty("MultiGroupGlobal", columnsForAdminUser(), Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertGroupFieldIsEmpty("MultiGroupBoth", Collections.<Field>emptyList());
        assertGroupFieldIsEmpty("MultiGroupProject", columnsForAdminUser(), Issue.FOUR2, Issue.FOUR1);
        assertGroupFieldIsEmpty("MultiGroupType", Collections.<Field>emptyList());
        assertGroupFieldIsEmpty("MultiGroupComplex", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
    }

    public void testNumberField() throws Exception
    {
        /*
            NumberBoth -> {(Project.FOUR, IssueType.SUBTASK)}
            NumberComplex -> {(Project.THREE, IssueType.FEATURE), (Project.ONE, IssueType.BUG)}
            NumberGlobal -> {(Context.GLOBAL)}
            NumberProject -> {(Project.FOUR, *), (Project.THREE, *)}
            NumberType -> {(*, IssueType.FEATURE)}
         */
        assertNumberField("NumberGlobal", 67, columnsForAdminUser(), Issue.THREE1);
        assertNumberField("NumberBoth", 67, columnsForAdminUser(), Issue.FOUR2);
        assertNumberField("NumberProject", 67, columnsForAdminUser(), Issue.THREE1);
        assertNumberField("NumberType", 67, columnsForAdminUser(), Issue.THREE1);
        assertNumberField("NumberComplex", 67, columnsForAdminUser(), Issue.THREE1, Issue.ONE1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("NumberComplex = 67", columnsForFredUser(), Issue.ONE1);
    }

    public void testImportIdField()
    {
        /*
            ImportBoth -> {(Project.TWO, IssueType.TASK)}
            ImportComplex -> {(Project.THREE, IssueType.BUG), (Project.FOUR, *)}
            ImportGlobal -> {(*, *)}
            ImportProject -> {(Project.ONE, *)}
            ImportType -> {(*, IssueType.BUG)}
         */

        assertNumberField("ImportGlobal", 48, columnsForAdminUser(), Issue.TWO1);
        assertNumberField("ImportBoth", 48, columnsForAdminUser(), Issue.TWO1);
        assertNumberField("ImportProject", 48, columnsForAdminUser(), Issue.ONE1);
        assertNumberField("ImportType", 48, columnsForAdminUser(), Issue.ONE1);
        assertNumberField("ImportComplex", 48, columnsForAdminUser(), Issue.FOUR3);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("ImportComplex = 48", columnsForFredUser(), Issue.FOUR3);

    }

    public void testProjectPicker() throws Exception
    {
        /*
            ProjectBoth -> {(Project.THREE, IssueType.FEATURE)}
            ProjectComplex -> {(Project.TWO, IssueType.SUBTASK), (Project.FOUR, *)}
            ProjectGlobal -> {(*, *)}
            ProjectProject -> {(Project.THREE, *), (Project.ONE, *)}
            ProjectType -> {(*, IssueType.SUBTASK)}
        */

        assertProjectPicker("projectglobal", columnsForAdminUser(), Issue.THREE1);
        assertProjectPicker("projectBoth", columnsForAdminUser(), Issue.THREE1);
        assertProjectPicker("ProjectProject", columnsForAdminUser(), Issue.THREE1, Issue.ONE1);
        assertProjectPicker("PROJECTType", columnsForAdminUser(), Issue.TWO2);
        assertProjectPicker("PROJectComplex", columnsForAdminUser(), Issue.TWO2);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("ProjectProject = one", columnsForFredUser(), Issue.ONE1);
    }

//    Otherwise it doesn't work. Might be a bug
    public void testCascadingSelect() throws Exception
    {
        /*
            CascasingSelectComplex = { (Project.FOUR, IssueType.SUBTASK){one}, (Project.THREE, *){[one, oneone], [one, onetwo], two}, (Project.TWO, *){[one, oneone], [one, onetwo], two}
         */

        assertJqlColumns("CascasingSelectComplex = one",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex = onetwo",
                columnsForAdminUser(),
                Issue.TWO1);
        assertJqlColumns("CascasingSelectComplex = two",
                columnsForAdminUser(),
                Issue.THREE2);

        assertJqlColumns("CascasingSelectComplex != one",
                columnsForAdminUser(),
                Issue.THREE2);
        assertJqlColumns("CascasingSelectComplex != onetwo",
                columnsForAdminUser(),
                Issue.TWO2, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex != oneone",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex != two",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);

        assertJqlColumns("CascasingSelectComplex in (one, two)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex = one or  CascasingSelectComplex = two",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);

        assertJqlColumns("CascasingSelectComplex not in (two, oneone)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);
        assertJqlColumns("CascasingSelectComplex != two and not CascasingSelectComplex = oneone",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.FOUR2);

        assertJqlColumns("CascasingSelectCOMPlex is empty",
                columnsForAdminUser(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex = empty",
                columnsForAdminUser(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex in (empty)",
                columnsForAdminUser(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex in (empty, oneone)",
                columnsForAdminUser(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex is empty or CascasingSelectCOMPlex = oneone",
                columnsForAdminUser(),
                Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex in (empty, onetwo)",
                columnsForAdminUser(),
                Issue.TWO1, Issue.THREE1);
        assertJqlColumns("CascasingSelectCOMPlex = empty or CascasingSelectCOMPlex = onetwo",
                columnsForAdminUser(),
                Issue.TWO1, Issue.THREE1);

        assertJqlColumns("CascasingSelectCOMPlex is not empty",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectCOMPlex != empty",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectCOMPlex not in (empty)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.FOUR2);
        assertJqlColumns("CascasingSelectCOMPlex not in (empty, one)",
                columnsForAdminUser(),
                Issue.THREE2);
        assertJqlColumns("CascasingSelectCOMPlex != empty and CascasingSelectCOMPlex != one",
                columnsForAdminUser(),
                Issue.THREE2);

        /*
            CascadingSelectProject = { (two, *) {[two, two]}, (three,*) {[three, two]}, (four, *) {[four, one], [four, two]}, (one, *){one}}
         */
//        TODO Seems to be related to JRADEV-21369
//        assertJqlColumns("CascadingSelectProject = one",
//                new Context().addProjects(Project.ONE, Project.FOUR),
//                Issue.ONE1);
//        assertJqlColumns("CascadingSelectProject = two",
//                new Context().addProjects(Project.TWO, Project.THREE, Project.FOUR),
//                Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR2);
//        assertJqlColumns("CascadingSelectProject = three",
//                new Context().addProjects(Project.THREE),
//                Issue.THREE2, Issue.THREE1);
//        assertJqlColumns("CascadingSelectProject = four",
//                new Context().addProjects(Project.FOUR),
//                Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns("CascadingSelectProject != one",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject != two",
                columnsForAdminUser(),
                Issue.THREE2, Issue.ONE1, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject != three",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject != four",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("CascadingSelectProject in (one, three)",
                columnsForAdminUser(),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("CascadingSelectProject = one or CascadingSelectProject = three",
                columnsForAdminUser(),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("CascadingSelectProject in (cascadeoption(one), three)",
                columnsForAdminUser(),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns("CascadingSelectProject in cascadeoption(one) or CascadingSelectProject = three",
                columnsForAdminUser(),
                Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumns("cascadingselectproject not in (one, two)",
                columnsForAdminUser(),
                Issue.THREE2, Issue.FOUR1);
        assertJqlColumns("not cascadingselectproject = one and cascadingselectproject != two",
                columnsForAdminUser(),
                Issue.THREE2, Issue.FOUR1);

        assertJqlColumns("cascadingselectproject is empty",
                columnsForAdminUser(),
                Issue.FOUR3);
        assertJqlColumns("cascadingselectproject = empty",
                columnsForAdminUser(),
                Issue.FOUR3);
        assertJqlColumns("cascadingselectproject in (empty)",
                columnsForAdminUser(),
                Issue.FOUR3);
        assertJqlColumns("cascadingselectproject in (empty, cascadeoption(one))",
                columnsForAdminUser(),
                Issue.ONE1, Issue.FOUR3);
        assertJqlColumns("cascadingselectproject is empty or cascadingselectproject in       cascadeoption(one)",
                columnsForAdminUser(),
                Issue.ONE1, Issue.FOUR3);

        assertJqlColumns("cascadingselectproject is not empty",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("cascadingselectproject != empty",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("cascadingselectproject not in (empty)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("cascadingselectproject not in (empty, one)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);

        //
        // Test the cascading option function
        //
        assertJqlColumns("CascadingSelectProject IN cascadeoption(four, none)",
                columnsForAdminUser(),
                Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject IN cascadeoption(two, none)",
                columnsForAdminUser(),
                Issue.TWO2);
        assertJqlColumns("CascadingSelectProject IN cascadeoption(four, two)",
                columnsForAdminUser(),
                Issue.FOUR2);
        assertJqlColumns("CascadingSelectProject IN cascadeoption(two)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(four, none)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(one, none)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(four, two)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(two, two)",
                columnsForAdminUser(),
                Issue.TWO2, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns("CascadingSelectProject NOT IN cascadeoption(two)",
                columnsForAdminUser(),
                Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR2, Issue.FOUR1);

        navigation.login(FRED_USERNAME);
        assertJqlColumns("CascadingSelectProject != one",
                columnsForFredUser(),
                Issue.TWO2, Issue.TWO1);
    }

    //
    // Test for the logical operator combining rules.
    //

    public void testLogicalOperatorsAndContext() throws Exception
    {
        /*
            All the combinations of context.

            (*, *) OP (*, *)
            (P, *) OP (*, *)
            (*, T) OP (*, *)
            (P, T) OP (*, *)

            (*, *) OP (P, *)
            (P, *) OP (P, *)
            (*, T) OP (P, *)
            (P, T) OP (P, *)

            (*, *) OP (*, T)
            (P, *) OP (*, T)
            (*, T) OP (*, T)
            (P, T) OP (*, T)

            (*, *) OP (P, T)
            (P, *) OP (P, T)
            (*, T) OP (P, T)
            (P, T) OP (P, T)

         */

        //
        // Test the "AND" operator.
        //

        //(*, *) AND (*, *)
        assertJqlColumns("summary ~ suns and comment ~ suns order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, *) AND (*, *)
        assertJqlColumns("project = three and summary ~ suns order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(*, T) AND (*, *)
        assertJqlColumns("type = 'new feature' and summary ~ suns order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, T) AND (*, *)
        assertJqlColumns("key = 'three-1' and summary ~ suns order by key desc", columnsForAdminUser(), Issue.THREE1);

        //(*, *) AND (P, *)
        assertJqlColumns("summary ~ suns and project = three order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(*, T) AND (P, *)
        assertJqlColumns("type = 'task' and project = two", columnsForAdminUser(), Issue.TWO1);
        //(P, T) AND (P, *)
        assertJqlColumns("key = one-1 and project = one", columnsForAdminUser(), Issue.ONE1);

        //(*, *) AND (*, T)
        assertJqlColumns("summary ~ suns and type = 'new feature' order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, *) AND (*, T)
        assertJqlColumns("project = two and type = 'task'", columnsForAdminUser(), Issue.TWO1);
        //(P, T) AND (*, T)
        assertJqlColumns("key = one-1 and type = 'bug'", columnsForAdminUser(), Issue.ONE1);

        //(*, *) AND (P, T)
        assertJqlColumns("summary ~ suns and key = three-1 order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, *) AND (P, T)
        assertJqlColumns("project = two and key = two-1", columnsForAdminUser(), Issue.TWO1);
        //(*, T) AND (P, T)
        assertJqlColumns("type = task and key = two-1", columnsForAdminUser(), Issue.TWO1);
        //(P, T) AND (P, T)
        assertJqlColumns("key = two-1 and key = two-1", columnsForAdminUser(), Issue.TWO1);

        //
        //Test the "OR" operator.
        //

        //(*, *) OR (*, *)
        assertJqlColumns("summary ~ suns or comment ~ suns order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, *) OR (*, *)
        assertJqlColumns("project = three or summary ~ suns order by key desc", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
        //(*, T) OR (*, *)
        assertJqlColumns("type = 'new feature' or summary ~ suns order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, T) OR (*, *)
        assertJqlColumns("key = 'three-1' or summary ~ suns order by key desc", columnsForAdminUser(), Issue.THREE1);

        //(*, *) OR (P, *)
        assertJqlColumns("summary ~ suns or project = three order by key desc", columnsForAdminUser(), Issue.THREE2, Issue.THREE1);
        //(P, *) OR (P, *)
        assertJqlColumns("project = four or category = cattwo", columnsForAdminUser(), Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        //(*, T) OR (P, *)
        assertJqlColumns("type = 'task' or project = two", columnsForAdminUser(), Issue.TWO2, Issue.TWO1);
        //(P, T) OR (P, *)
        assertJqlColumns("key = one-1 or project = one", columnsForAdminUser(), Issue.ONE1);

        //(*, *) OR (*, T)
        assertJqlColumns("summary ~ suns or type = 'new feature' order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, *) OR (*, T)
        assertJqlColumns("project = two or type = 'task'", columnsForAdminUser(), Issue.TWO2, Issue.TWO1);
        //(*, T), (*, T)
        assertJqlColumns("type = task or type = 'bug'", columnsForAdminUser(), Issue.TWO1, Issue.THREE2, Issue.ONE1, Issue.FOUR3);
        //(P, T) OR (*, T)
        assertJqlColumns("key = one-1 or type = 'task'", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);

        //(*, *) OR (P, T)
        assertJqlColumns("summary ~ suns or key = three-1 order by key desc", columnsForAdminUser(), Issue.THREE1);
        //(P, *) OR (P, T)
        assertJqlColumns("project = two or key = two-1", columnsForAdminUser(), Issue.TWO2, Issue.TWO1);
        //(*, T) OR (P, T)
        assertJqlColumns("type = task or key = two-1", columnsForAdminUser(), Issue.TWO1);
        //(P, T) OR (P, T)
        assertJqlColumns("key = two-1 or key = one-1", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);

        //
        //Lets test some logic rules.
        //

        // a and b or c and d = (a and b) or (c and d) = b and a or c and d = d and c or a and b
        List<Issue> issues = Arrays.asList(Issue.TWO1, Issue.ONE1);
        assertJqlColumns("project = one and type = bug or type = task and project = two", columnsForAdminUser(), issues);
        assertJqlColumns("(project = one and type = bug) or (type = task and project = two)", columnsForAdminUser(), issues);
        assertJqlColumns("type = bug and project = one or type = task and project = two", columnsForAdminUser(), issues);
        assertJqlColumns("(project = two and type = task  ) or (project = one and type = bug)", columnsForAdminUser(), issues);

        // a and (b or c) = a and b or a and c
        issues = Arrays.asList(Issue.THREE2, Issue.ONE1);
        assertJqlColumns("type = bug and (project = one or key >= three-1)", columnsForAdminUser(), issues);
        assertJqlColumns("type = bug and project = one or type = bug and issue >= three-1", columnsForAdminUser(), issues);

        //
        //Some complex examples.
        //
        assertJqlColumns("project = one and type = bug or type = task", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("project = one and type = bug or type = task and project = two", columnsForAdminUser(), Issue.TWO1, Issue.ONE1);
        assertJqlColumns("project in (one, three) and type = bug or type = \"New Feature\"", columnsForAdminUser(), Issue.THREE2, Issue.THREE1, Issue.ONE1);

        Set<IssueType> types = getIssueTypesAndRemove(IssueType.BUG);
        assertJqlColumns("project in (one, three) and type != 'bug'", columnsForAdminUser(), Issue.THREE1);

        types = EnumSet.of(IssueType.FEATURE, IssueType.IMPROVEMENT, IssueType.TASK);
        assertJqlColumns("project in (one, three) and type in ('new feature', Improvement, task)", columnsForAdminUser(), Issue.THREE1);

        types = getIssueTypesAndRemove(IssueType.BUG);
        assertJqlColumns("project in (one, two) and type not in (bug)", columnsForAdminUser(), Issue.TWO2, Issue.TWO1);

        assertJqlColumns("project != one and type != bug", columnsForAdminUser(), Issue.TWO2, Issue.TWO1, Issue.THREE1, Issue.FOUR2, Issue.FOUR1);

        //Fred can't see project three.
        navigation.login(FRED_USERNAME);
        assertJqlColumns("project != one and type != bug", columnsForFredUser(), Issue.TWO2, Issue.TWO1);
    }

    private void assertTextField(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertJqlColumns(String.format("%s ~ 'match' order by key desc", fieldName), expectedColumns, issues);

        assertJqlColumns(String.format("%s !~ empty order by key desc", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s is not empty order by key desc", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s is not null order by key desc", fieldName), expectedColumns, issues);
    }

    private void assertTextFieldIsEmpty(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertJqlColumns(String.format("%s ~ empty order by key desc", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s is empty order by key desc", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s ~ null order by key desc", fieldName), expectedColumns, issues);
    }

    private void assertProjectPicker(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertStringEqualsField(fieldName, "one", "two", expectedColumns, issues);
    }

    private void assertUserField(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertStringEqualsField(fieldName, ADMIN_USERNAME, FRED_USERNAME, expectedColumns, issues);
    }

    private void assertUserFieldIsEmpty(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertStringFieldIsEmpty(fieldName, expectedColumns, issues);
    }

    private void assertGroupField(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertStringEqualsField(fieldName, "jira-developers", "jira-administrators", expectedColumns, issues);
    }

    private void assertGroupFieldIsEmpty(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertStringFieldIsEmpty(fieldName, expectedColumns, issues);
    }

    private void assertUrlField(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        try
        {
            assertStringEqualsField(fieldName, URLEncoder.encode("http://match.com", "UTF-8"), "other", expectedColumns, issues);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertStringEqualsField(String fieldName, String match, String notMatch, List<Field> expectedColumns, Issue... issues)
    {
        assertJqlColumns(String.format("%s = '%s'", fieldName, match), expectedColumns, issues);
        assertJqlColumns(String.format("%1$s in ('%2$s', '%3$s')", fieldName, match, notMatch), expectedColumns, issues);
        assertJqlColumns(String.format("%1$s = '%2$s' or %1$s = '%3$s'", fieldName, match, notMatch), expectedColumns, issues);

        assertJqlColumns(String.format("%s != '%s'", fieldName, notMatch), expectedColumns, issues);
        assertJqlColumns(String.format("%1$s not in ('%2$s')", fieldName, notMatch), expectedColumns, issues);

        assertJqlColumns(String.format("%s != empty", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s is not empty", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s is not null", fieldName), expectedColumns, issues);
    }

    private void assertStringFieldIsEmpty(String fieldName, List<Field> expectedColumns, Issue... issues)
    {
        assertJqlColumns(String.format("%s is empty", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s = empty", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s = null", fieldName), expectedColumns, issues);
    }

    private void assertSearchingBySystemVersionField(String fieldName, List<Field> expectedColumns)
    {
        assertMultiVersionField(fieldName, expectedColumns);
    }

    private void assertMultiVersionField(String fieldName, List<Field> expectedColumns)
    {
        assertJqlColumns(String.format("%s = twoonly", fieldName), expectedColumns, Issue.TWO1);
        assertJqlColumns(String.format("%s = two", fieldName), expectedColumns, Issue.TWO1, Issue.ONE1);

        assertJqlColumns(String.format("%s != two", fieldName), expectedColumns, Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%s != fouronly", fieldName), expectedColumns, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);

        assertJqlColumns(String.format("%s in (fouronly, three)", fieldName), expectedColumns, Issue.THREE2, Issue.ONE1);
        assertJqlColumns(String.format("%1$s = fouronly or %1$s = three", fieldName), expectedColumns, Issue.THREE2, Issue.ONE1);
        assertJqlColumns(String.format("%s in (fouronly, threeonly)", fieldName), expectedColumns, Issue.THREE1);
        assertJqlColumns(String.format("(%1$s  = fouronly or %1$s = threeonly)", fieldName), expectedColumns, Issue.THREE1);

        assertJqlColumns(String.format("%s not in (fouronly, threeonly)", fieldName), expectedColumns, Issue.TWO1, Issue.THREE2, Issue.ONE1);
        assertJqlColumns(String.format("not(%1$s = fouronly or %1$s = threeonly)", fieldName), expectedColumns, Issue.TWO1, Issue.THREE2, Issue.ONE1);
        assertJqlColumns(String.format("%s not in (one, two, twoonly)", fieldName), expectedColumns, Issue.THREE1);
        assertJqlColumns(String.format("not %1$s = one and %1$s != two and %1$s != twoonly", fieldName), expectedColumns, Issue.THREE1);

        assertJqlColumns(String.format("%s is empty", fieldName), expectedColumns, Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns(String.format("%s = empty", fieldName), expectedColumns, Issue.TWO2, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns(String.format("%s in (empty, two)", fieldName), expectedColumns, Issue.TWO2, Issue.TWO1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);
        assertJqlColumns(String.format("%1$s = empty or %1$s = two", fieldName), expectedColumns, Issue.TWO2, Issue.TWO1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        assertJqlColumns(String.format("%s is not empty", fieldName), expectedColumns, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns(String.format("%s != null", fieldName), expectedColumns, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns(String.format("%s not in (null, one, two, three)", fieldName), expectedColumns, Issue.THREE1);
        assertJqlColumns(String.format("%1$s not in (null, one) and %1$s != two and not %1$s = three", fieldName), expectedColumns, Issue.THREE1);

        assertJqlColumns(String.format("%s > one", fieldName), expectedColumns, Issue.ONE1);
        assertJqlColumns(String.format("%s > two", fieldName), expectedColumns, Issue.TWO1, Issue.ONE1);
        assertJqlColumns(String.format("%s > three", fieldName), expectedColumns, Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%s > threeonly", fieldName), expectedColumns, Issue.THREE2);
        assertJqlColumns(String.format("%s < one", fieldName), expectedColumns, Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%s < two", fieldName), expectedColumns, Issue.ONE1);
        assertJqlColumns(String.format("%s < three", fieldName), expectedColumns, Issue.ONE1);
        assertJqlColumns(String.format("%s < twoonly", fieldName), expectedColumns, Issue.TWO1);
        assertJqlColumns(String.format("%s < threeonly", fieldName), expectedColumns, Issue.THREE2);

        assertJqlColumns(String.format("%s >= one", fieldName), expectedColumns, Issue.THREE2, Issue.ONE1);
        assertJqlColumns(String.format("%s >= two", fieldName), expectedColumns, Issue.TWO1, Issue.ONE1);
        assertJqlColumns(String.format("%s >= three", fieldName), expectedColumns, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns(String.format("%s >= twoonly", fieldName), expectedColumns, Issue.TWO1);
        assertJqlColumns(String.format("%s >= threeonly", fieldName), expectedColumns, Issue.THREE2, Issue.THREE1);
        assertJqlColumns(String.format("%s <= one", fieldName), expectedColumns, Issue.THREE2, Issue.THREE1, Issue.ONE1);
        assertJqlColumns(String.format("%s <= two", fieldName), expectedColumns, Issue.TWO1, Issue.ONE1);
        assertJqlColumns(String.format("%s <= three", fieldName), expectedColumns, Issue.THREE2, Issue.ONE1);
        assertJqlColumns(String.format("%s <= twoonly", fieldName), expectedColumns, Issue.TWO1);
        assertJqlColumns(String.format("%s <= threeonly", fieldName), expectedColumns, Issue.THREE2, Issue.THREE1);
    }

    private void assertDateCustomField(String fieldName, String date, List<Field> expectedColumns, Issue... includeIssues)
            throws ParseException
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date parsedDate = dateFormat.parse(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsedDate);
        cal.add(Calendar.MONTH, -1);
        String previousDate = dateFormat.format(cal.getTime());

        cal.setTime(parsedDate);
        cal.add(Calendar.MONTH, 1);
        String nextDate = dateFormat.format(cal.getTime());

        assertRangeField(fieldName, previousDate, date, nextDate, expectedColumns, includeIssues);
    }

    private void assertDateCustomFieldIsEmpty(String fieldName, String date, List<Field> expectedColumns, Issue... expectedIssues)
            throws ParseException
    {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final Date parsedDate = dateFormat.parse(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(parsedDate);
        cal.add(Calendar.MONTH, -1);
        String previousDate = dateFormat.format(cal.getTime());

        assertJqlColumns(String.format("%s is empty", fieldName), expectedColumns, expectedIssues);
        assertJqlColumns(String.format("%s = empty", fieldName), expectedColumns, expectedIssues);
        assertJqlColumns(String.format("%s = null", fieldName), expectedColumns, expectedIssues);
        assertJqlColumns(String.format("%s in (empty, %s)", fieldName, previousDate), expectedColumns, expectedIssues);
        assertJqlColumns(String.format("%1$s is empty or %1$s = %2$s", fieldName, previousDate), expectedColumns, expectedIssues);
    }

    private void assertNumberField(String fieldName, int value, List<Field> expectedColumns, Issue... issues)
    {
        assertRangeField(fieldName, String.valueOf(value - 2), String.valueOf(value), String.valueOf(value + 100), expectedColumns, issues);
    }

    private void assertRangeField(String fieldName, String prev, String value, String next, List<Field> expectedColumns, Issue... issues)
    {
        assertJqlColumns(String.format("%s = %s", fieldName, value), expectedColumns, issues);
        assertJqlColumns(String.format("%s != %s", fieldName, prev), expectedColumns, issues);
        assertJqlColumns(String.format("%s >= %s", fieldName, value), expectedColumns, issues);
        assertJqlColumns(String.format("%s <= %s", fieldName, value), expectedColumns, issues);
        assertJqlColumns(String.format("%s < %s", fieldName, next), expectedColumns, issues);
        assertJqlColumns(String.format("%s > %s", fieldName, prev), expectedColumns, issues);

        assertJqlColumns(String.format("%s in (%s)", fieldName, value), expectedColumns, issues);
        assertJqlColumns(String.format("%s in (%s, %s)", fieldName, value, next), expectedColumns, issues);
        assertJqlColumns(String.format("%1$s = %2$s or %1$s = %3$s", fieldName, value, next), expectedColumns, issues);

        assertJqlColumns(String.format("%s not in (%s)", fieldName, prev), expectedColumns, issues);
        assertJqlColumns(String.format("%s not in (%s, %s)", fieldName, prev, next), expectedColumns, issues);
        assertJqlColumns(String.format("%1$s != %2$s and %1$s != %3$s", fieldName, prev, next), expectedColumns, issues);

        assertJqlColumns(String.format("%s is not empty", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s != empty", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s != null", fieldName), expectedColumns, issues);
        assertJqlColumns(String.format("%s not in (empty, %s)", fieldName, prev), expectedColumns, issues);
        assertJqlColumns(String.format("%1$s is not empty and %1$s != %2$s", fieldName, prev), expectedColumns, issues);
    }

    private Set<IssueType> getIssueTypesAndRemove(final IssueType... remove)
    {
        Set<IssueType> types = EnumSet.allOf(IssueType.class);
        types.removeAll(Arrays.asList(remove));
        return types;
    }

    private List<Issue> getIssuesAndRemoveIssues(Issue... remove)
    {
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES);
        issues.removeAll(Arrays.asList(remove));
        return issues;
    }

    private List<Issue> getIssuesAndRemoveIssues(Collection<Issue> remove)
    {
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES);
        issues.removeAll(remove);
        return issues;
    }

    private List<Issue> getIssuesAndRemoveProject(Project... projects)
    {
        if (projects.length == 0)
        {
            return Issue.ALL_ISSUES;
        }

        Set<Project> projectCheck = EnumSet.copyOf(Arrays.asList(projects));
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES.size());

        for (Issue issue : Issue.ALL_ISSUES)
        {
            if (!projectCheck.contains(issue.getProject()))
            {
                issues.add(issue);
            }
        }
        return issues;
    }

    private List<Issue> getIssuesForProjects(Project... projects)
    {
        if (projects.length == 0)
        {
            return Issue.ALL_ISSUES;
        }

        Set<Project> projectCheck = EnumSet.copyOf(Arrays.asList(projects));
        List<Issue> issues = new ArrayList<Issue>(Issue.ALL_ISSUES.size());

        for (Issue issue : Issue.ALL_ISSUES)
        {
            if (projectCheck.contains(issue.getProject()))
            {
                issues.add(issue);
            }
        }
        return issues;
    }

    private void assertJqlColumns(String jqlQuery, List<Field> expectedColumns, Collection<Issue> expectedIssues)
    {
        assertJqlColumns(jqlQuery, expectedColumns, expectedIssues.toArray(new Issue[expectedIssues.size()]));
    }

    private void assertJqlColumns(String jqlQuery, List<Field> expectedColumns, Issue... expectedIssues)
    {
        navigation.issueNavigator().createSearch(jqlQuery);
        assertColumns(jqlQuery, expectedColumns, expectedIssues);
    }

    private void assertColumns(String msg, List<Field> expectedColumns, Issue... expectedIssues)
    {
        List<String> columnNames = extractColumnNames(expectedColumns);
        final List<SearchResultsCondition> condition = new ArrayList<SearchResultsCondition>();
        condition.add(new ColumnsCondition(columnNames));
        condition.add(new ContainsIssueKeysCondition(text, issuesToKeys(expectedIssues)));
        condition.add(new NumberOfIssuesCondition(text, expectedIssues.length));

        log(String.format("Checking that columns '%s' are visible for '%s'", columnNames, msg));

        assertions.getIssueNavigatorAssertions().assertSearchResults(condition);
    }

    private static String[] issuesToKeys(Issue... issues)
    {
        String[] keys = new String[issues.length];
        int i = 0;
        for (Issue issue : issues)
        {
            keys[i++] = issue.getKey();
        }
        return keys;
    }

    private List<String> extractColumnNames(List<Field> columns)
    {
        final List<String> column = new ArrayList<String>();

        for (Field defaultField : columns)
        {
                column.add(defaultField.getFieldName());
        }

        return column;
    }

    private List<Field> getDefaultFields()
    {
        List<Field> fields = new ArrayList<Field>();

        fields.add(Field.TYPE);
        fields.add(Field.KEY);
        fields.add(Field.STATUS);

        fields.add(Field.DATE_PICKER_BOTH);
        fields.add(Field.DATE_PICKER_COMPLEX);
        fields.add(Field.DATE_PICKER_GLOBAL);
        fields.add(Field.DATE_PICKER_PROJECT);
        fields.add(Field.DATE_PICKER_TYPE);

        fields.add(Field.DATE_TIME_BOTH);
        fields.add(Field.DATE_TIME_COMPLEX);
        fields.add(Field.DATE_TIME_GLOBAL);
        fields.add(Field.DATE_TIME_PROJECT);
        fields.add(Field.DATE_TIME_TYPE);

        fields.add(Field.URL_BOTH);
        fields.add(Field.URL_COMPLEX);
        fields.add(Field.URL_GLOBAL);
        fields.add(Field.URL_PROJECT);
        fields.add(Field.URL_TYPE);

        fields.add(Field.USER_BOTH);
        fields.add(Field.USER_COMPLEX);
        fields.add(Field.USER_GLOBAL);
        fields.add(Field.USER_PROJECT);
        fields.add(Field.USER_TYPE);

        fields.add(Field.MULTI_USER_BOTH);
        fields.add(Field.MULTI_USER_COMPLEX);
        fields.add(Field.MULTI_USER_GLOBAL);
        fields.add(Field.MULTI_USER_PROJECT);
        fields.add(Field.MULTI_USER_TYPE);

        fields.add(Field.GROUP_BOTH);
        fields.add(Field.GROUP_COMPLEX);
        fields.add(Field.GROUP_GLOBAL);
        fields.add(Field.GROUP_PROJECT);
        fields.add(Field.GROUP_TYPE);

        fields.add(Field.MULTI_GROUP_BOTH);
        fields.add(Field.MULTI_GROUP_COMPLEX);
        fields.add(Field.MULTI_GROUP_GLOBAL);
        fields.add(Field.MULTI_GROUP_PROJECT);
        fields.add(Field.MULTI_GROUP_TYPE);

        fields.add(Field.NUMBER_BOTH);
        fields.add(Field.NUMBER_COMPLEX);
        fields.add(Field.NUMBER_GLOBAL);
        fields.add(Field.NUMBER_PROJECT);
        fields.add(Field.NUMBER_TYPE);

        fields.add(Field.IMPORT_BOTH);
        fields.add(Field.IMPORT_COMPLEX);
        fields.add(Field.IMPORT_GLOBAL);
        fields.add(Field.IMPORT_PROJECT);
        fields.add(Field.IMPORT_TYPE);

        fields.add(Field.PROJECT_BOTH);
        fields.add(Field.PROJECT_COMPLEX);
        fields.add(Field.PROJECT_GLOBAL);
        fields.add(Field.PROJECT_PROJECT);
        fields.add(Field.PROJECT_TYPE);

        fields.add(Field.SINGLE_VERSION_BOTH);
        fields.add(Field.SINGLE_VERSION_COMPLEX);
        fields.add(Field.SINGLE_VERSION_GLOBAL);
        fields.add(Field.SINGLE_VERSION_PROJECT);
        fields.add(Field.SINGLE_VERSION_TYPE);

        fields.add(Field.MULTI_VERSION_BOTH);
        fields.add(Field.MULTI_VERSION_COMPLEX);
        fields.add(Field.MULTI_VERSION_GLOBAL);
        fields.add(Field.MULTI_VERSION_PROJECT);
        fields.add(Field.MULTI_VERSION_TYPE);

        fields.add(Field.FREE_TEXT_BOTH);
        fields.add(Field.FREE_TEXT_COMPLEX);
        fields.add(Field.FREE_TEXT_GLOBAL);
        fields.add(Field.FREE_TEXT_PROJECT);
        fields.add(Field.FREE_TEXT_TYPE);

        fields.add(Field.SELECT_LIST_COMPLEX);
        fields.add(Field.SELECT_LIST_PROJECT_GLOBAL);
        fields.add(Field.SELECT_LIST_TYPE);

        fields.add(Field.RADIO_COMPLEX);
        fields.add(Field.RADIO_PROJECT_GLOBAL);
        fields.add(Field.RADIO_TYPE);

        fields.add(Field.READ_TEXT_BOTH);
        fields.add(Field.READ_TEXT_COMPLEX);
        fields.add(Field.READ_TEXT_GLOBAL);
        fields.add(Field.READ_TEXT_PROJECT);
        fields.add(Field.READ_TEXT_TYPE);

        fields.add(Field.TEXT_BOTH);
        fields.add(Field.TEXT_COMPLEX);
        fields.add(Field.TEXT_GLOBAL);
        fields.add(Field.TEXT_PROJECT);
        fields.add(Field.TEXT_TYPE);

        fields.add(Field.CHECKBOX_COMPLEX);
        fields.add(Field.CHECKBOX_PROJECT_GLOBAL);
        fields.add(Field.CHECKBOX_TYPE);

        fields.add(Field.MULTI_SELECT_COMPLEX);
        fields.add(Field.MULTI_SELECT_PROJECT_GLOBAL);
        fields.add(Field.MULTI_SELECT_TYPE);

        fields.add(Field.CASCADING_SELECT_COMPLEX);
        fields.add(Field.CASCADING_SELECT_PROJECT);

        fields.add(Field.INVISIBLE_FIELD);

        return fields;
    }

    private List<Field> columnsForAdminUser()
    {
        return getDefaultFields();
    }

    private List<Field> columnsForFredUser()
    {
        List<Field> fields = getDefaultFields();
        fields.remove(Field.READ_TEXT_BOTH);
        fields.remove(Field.MULTI_VERSION_BOTH);
        fields.remove(Field.GROUP_BOTH);
        fields.remove(Field.SINGLE_VERSION_BOTH);
        fields.remove(Field.PROJECT_BOTH);
        fields.remove(Field.MULTI_GROUP_BOTH);
        fields.remove(Field.INVISIBLE_FIELD);
        return fields;
    }

    private static class Issue implements Comparable<Issue>
    {
        private static final Issue TWO2 = new Issue("TWO-2", Project.TWO, IssueType.SUBTASK);
        private static final Issue TWO1 = new Issue("TWO-1", Project.TWO, IssueType.TASK);
        private static final Issue THREE2 = new Issue("THREE-2", Project.THREE, IssueType.BUG);
        private static final Issue THREE1 = new Issue("THREE-1", Project.THREE, IssueType.FEATURE);
        private static final Issue ONE1 = new Issue("ONE-1", Project.ONE, IssueType.BUG);
        private static final Issue FOUR3 = new Issue("FOUR-3", Project.FOUR, IssueType.BUG);
        private static final Issue FOUR2 = new Issue("FOUR-2", Project.FOUR, IssueType.SUBTASK);
        private static final Issue FOUR1 = new Issue("FOUR-1", Project.FOUR, IssueType.IMPROVEMENT);

        private static final List<Issue> ALL_ISSUES = Arrays.asList(Issue.TWO2, Issue.TWO1, Issue.THREE2, Issue.THREE1, Issue.ONE1, Issue.FOUR3, Issue.FOUR2, Issue.FOUR1);

        private final String key;
        private final Project project;
        private final IssueType type;

        private Issue(final String key, final Project project, final IssueType type)
        {
            this.key = key;
            this.project = project;
            this.type = type;
        }

        public String getKey()
        {
            return key;
        }

        public Project getProject()
        {
            return project;
        }

        public IssueType getType()
        {
            return type;
        }

        @Override
        public String toString()
        {
            return key;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final Issue issue = (Issue) o;

            return !(key != null ? !key.equals(issue.key) : issue.key != null);

        }

        @Override
        public int hashCode()
        {
            return key != null ? key.hashCode() : 0;
        }

        public int compareTo(final Issue o)
        {
            return key.compareTo(o.key);
        }
    }

    private static class Field
    {
        private static final Field TYPE = new Field("T");
        private static final Field KEY = new Field("Key");
        private static final Field STATUS = new Field("Status");

        private static final Field DATE_PICKER_BOTH = new Field("DatePickerBoth");
        private static final Field DATE_PICKER_COMPLEX = new Field("DatePickerComplex");
        private static final Field DATE_PICKER_GLOBAL = new Field("DatePickerGlobal");
        private static final Field DATE_PICKER_PROJECT = new Field("DatePickerProject");
        private static final Field DATE_PICKER_TYPE = new Field("DatePickerType");

        private static final Field DATE_TIME_BOTH = new Field("DateTimeBoth");
        private static final Field DATE_TIME_COMPLEX = new Field("DateTimeComplex");
        private static final Field DATE_TIME_GLOBAL = new Field("DateTimeGlobal");
        private static final Field DATE_TIME_PROJECT = new Field("DateTimeProject");
        private static final Field DATE_TIME_TYPE = new Field("DateTimeType");

        private static final Field FREE_TEXT_BOTH = new Field("FreeTextBoth");
        private static final Field FREE_TEXT_COMPLEX = new Field("FreeTextComplex");
        private static final Field FREE_TEXT_GLOBAL = new Field("FreeTextGlobal");
        private static final Field FREE_TEXT_PROJECT = new Field("FreeTextProject");
        private static final Field FREE_TEXT_TYPE = new Field("FreeTextType");

        private static final Field TEXT_BOTH = new Field("TextBoth");
        private static final Field TEXT_COMPLEX = new Field("TextComplex");
        private static final Field TEXT_GLOBAL = new Field("TextGlobal");
        private static final Field TEXT_PROJECT = new Field("TextProject");
        private static final Field TEXT_TYPE = new Field("TextType");

        private static final Field URL_BOTH = new Field("UrlBoth");
        private static final Field URL_COMPLEX = new Field("UrlComplex");
        private static final Field URL_GLOBAL = new Field("UrlGlobal");
        private static final Field URL_PROJECT = new Field("UrlProject");
        private static final Field URL_TYPE = new Field("UrlType");

        private static final Field READ_TEXT_COMPLEX = new Field("ReadTextComplex");
        private static final Field READ_TEXT_BOTH = new Field("ReadTextBoth");
        private static final Field READ_TEXT_GLOBAL = new Field("ReadTextGlobal");
        private static final Field READ_TEXT_PROJECT = new Field("ReadTextProject");
        private static final Field READ_TEXT_TYPE = new Field("ReadTextType");

        private static final Field USER_BOTH = new Field("UserBoth");
        private static final Field USER_COMPLEX = new Field("UserComplex");
        private static final Field USER_GLOBAL = new Field("UserGlobal");
        private static final Field USER_PROJECT = new Field("UserProject");
        private static final Field USER_TYPE = new Field("UserType");

        private static final Field MULTI_USER_BOTH = new Field("MultiUserBoth");
        private static final Field MULTI_USER_COMPLEX = new Field("MultiUserComplex");
        private static final Field MULTI_USER_GLOBAL = new Field("MultiUserGlobal");
        private static final Field MULTI_USER_PROJECT = new Field("MultiUserProject");
        private static final Field MULTI_USER_TYPE = new Field("MultiUserType");

        private static final Field GROUP_BOTH = new Field("GroupBoth");
        private static final Field GROUP_COMPLEX = new Field("GroupComplex");
        private static final Field GROUP_GLOBAL = new Field("GroupGlobal");
        private static final Field GROUP_PROJECT = new Field("GroupProject");
        private static final Field GROUP_TYPE = new Field("GroupType");

        private static final Field MULTI_GROUP_BOTH = new Field("MultiGroupBoth");
        private static final Field MULTI_GROUP_COMPLEX = new Field("MultiGroupComplex");
        private static final Field MULTI_GROUP_GLOBAL = new Field("MultiGroupGlobal");
        private static final Field MULTI_GROUP_PROJECT = new Field("MultiGroupProject");
        private static final Field MULTI_GROUP_TYPE = new Field("MultiGroupType");

        private static final Field NUMBER_BOTH = new Field("NumberBoth");
        private static final Field NUMBER_COMPLEX = new Field("NumberComplex");
        private static final Field NUMBER_GLOBAL = new Field("NumberGlobal");
        private static final Field NUMBER_PROJECT = new Field("NumberProject");
        private static final Field NUMBER_TYPE = new Field("NumberType");

        private static final Field IMPORT_BOTH = new Field("ImportBoth");
        private static final Field IMPORT_COMPLEX = new Field("ImportComplex");
        private static final Field IMPORT_GLOBAL = new Field("ImportGlobal");
        private static final Field IMPORT_PROJECT = new Field("ImportProject");
        private static final Field IMPORT_TYPE = new Field("ImportType");

        private static final Field PROJECT_BOTH = new Field("ProjectBoth");
        private static final Field PROJECT_COMPLEX = new Field("ProjectComplex");
        private static final Field PROJECT_GLOBAL = new Field("ProjectGlobal");
        private static final Field PROJECT_PROJECT = new Field("ProjectProject");
        private static final Field PROJECT_TYPE = new Field("ProjectType");

        private static final Field SINGLE_VERSION_BOTH = new Field("SingleVersionBoth");
        private static final Field SINGLE_VERSION_COMPLEX = new Field("SingleVersionComplex");
        private static final Field SINGLE_VERSION_GLOBAL = new Field("SingleVersionGlobal");
        private static final Field SINGLE_VERSION_PROJECT = new Field("SingleVersionProject");
        private static final Field SINGLE_VERSION_TYPE = new Field("SingleVersionType");

        private static final Field MULTI_VERSION_BOTH = new Field("MultiVersionBoth");
        private static final Field MULTI_VERSION_COMPLEX = new Field("MultiVersionComplex");
        private static final Field MULTI_VERSION_GLOBAL = new Field("MultiVersionGlobal");
        private static final Field MULTI_VERSION_PROJECT = new Field("MultiVersionProject");
        private static final Field MULTI_VERSION_TYPE = new Field("MultiVersionType");

        private static final Field SELECT_LIST_COMPLEX = new Field("SelectListComplex");
        private static final Field SELECT_LIST_PROJECT_GLOBAL = new Field("SelectListProjectGlobal");
        private static final Field SELECT_LIST_TYPE = new Field("SelectListType");

        private static final Field RADIO_COMPLEX = new Field("RadioComplex");
        private static final Field RADIO_PROJECT_GLOBAL = new Field("RadioProjectGlobal");
        private static final Field RADIO_TYPE = new Field("RadioType");

        private static final Field CHECKBOX_COMPLEX = new Field("CheckboxComplex");
        private static final Field CHECKBOX_PROJECT_GLOBAL = new Field("CheckboxProjectGlobal");
        private static final Field CHECKBOX_TYPE = new Field("CheckboxType");

        private static final Field MULTI_SELECT_COMPLEX = new Field("MultiSelectComplex");
        private static final Field MULTI_SELECT_PROJECT_GLOBAL = new Field("MultiSelectProjectGlobal");
        private static final Field MULTI_SELECT_TYPE = new Field("MultiSelectType");

        private static final Field CASCADING_SELECT_COMPLEX = new Field("CascasingSelectComplex");
        private static final Field CASCADING_SELECT_PROJECT = new Field("CascadingSelectProject");

        private static final Field INVISIBLE_FIELD = new Field("InvisibleField");

        private final String fieldName;

        private Field(String fieldName)
        {
            this.fieldName = fieldName;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        @Override
        public String toString()
        {
            return String.format("Field Config: %s .%n", fieldName);
        }
    }
}
