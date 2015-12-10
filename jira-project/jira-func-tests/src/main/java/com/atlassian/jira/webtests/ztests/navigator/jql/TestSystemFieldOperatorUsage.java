package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests the validity of all the operators against all the system field clauses.
 *
 * This is not intended to be an exhaustive test of each clause. It's primary goals are to:
 *
 *  - ensure that the operators that should work with a clause don't cause a validation error, and that some kind of
 *    sensible result is returned
 *
 *  - if the operator does not validate, that an appropriate error is displayed
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSystemFieldOperatorUsage extends FuncTestCase
{
    public void testAffectedVersion() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsAffectedVersion.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "affectedVersion";
        final String singleLit = "'New Version 4'";
        final String listLit = "('New Version 1', 'New Version 4')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "AFF-2"));
        matrix.put("!=", new Result(singleLit, "AFF-3", "AFF-1"));
        matrix.put("in", new Result(listLit, "AFF-2", "AFF-1"));
        matrix.put("not in", new Result(listLit, "AFF-3"));
        matrix.put("is", new Result("EMPTY", "AFF-4"));
        matrix.put("is not", new Result("EMPTY", "AFF-3", "AFF-2", "AFF-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "AFF-1"));
        matrix.put("<=", new Result(singleLit, "AFF-2", "AFF-1"));
        matrix.put(">", new Result(singleLit, "AFF-3"));
        matrix.put(">=", new Result(singleLit, "AFF-3", "AFF-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testAssignee() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsAssignee.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "assignee";
        final String singleLit = FRED_USERNAME;
        final String listLit = "(fred, admin)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "ASS-2"));
        matrix.put("!=", new Result(singleLit, "ASS-1"));
        matrix.put("in", new Result(listLit, "ASS-2", "ASS-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "ASS-3"));
        matrix.put("is not", new Result("EMPTY", "ASS-2", "ASS-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testCategory() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsCategory.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "category";
        final String singleLit = "Category1";
        final String listLit = "(Category1, Category2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "CAT-1"));
        matrix.put("!=", new Result(singleLit, "CATTWO-1"));
        matrix.put("in", new Result(listLit, "CATTWO-1", "CAT-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "NOT-1"));
        matrix.put("is not", new Result("EMPTY", "CATTWO-1", "CAT-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testComment() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsComment.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "comment";
        final String singleLit = "TestComment1";
        final String listLit = "(TestComment1)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", Result.errorEmptyNotSupported("EMPTY"));
        matrix.put("is not", Result.errorEmptyNotSupported("EMPTY"));
        matrix.put("~", new Result(singleLit, "COM-1"));
        matrix.put("!~", new Result(singleLit, "COM-2"));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testComponent() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsComponent.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "component";
        final String singleLit = "'Component 1'";
        final String listLit = "('Component 1', 'Component 2')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "CMP-1"));
        matrix.put("!=", new Result(singleLit, "CMP-2"));
        matrix.put("in", new Result(listLit, "CMP-2", "CMP-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "CMP-3"));
        matrix.put("is not", new Result("EMPTY", "CMP-2", "CMP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testCreated() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsCreated.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "created";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit));
        matrix.put("!=", new Result(singleLit, "CRE-3", "CRE-2", "CRE-1"));
        matrix.put("in", new Result(listLit));
        matrix.put("not in", new Result(listLit, "CRE-3", "CRE-2", "CRE-1"));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "CRE-3", "CRE-2", "CRE-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "CRE-1"));
        matrix.put("<=", new Result(singleLit, "CRE-1"));
        matrix.put(">", new Result(singleLit, "CRE-3", "CRE-2"));
        matrix.put(">=", new Result(singleLit, "CRE-3", "CRE-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testCreatedDate() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsCreated.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "createddate";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit));
        matrix.put("!=", new Result(singleLit, "CRE-3", "CRE-2", "CRE-1"));
        matrix.put("in", new Result(listLit));
        matrix.put("not in", new Result(listLit, "CRE-3", "CRE-2", "CRE-1"));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "CRE-3", "CRE-2", "CRE-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "CRE-1"));
        matrix.put("<=", new Result(singleLit, "CRE-1"));
        matrix.put(">", new Result(singleLit, "CRE-3", "CRE-2"));
        matrix.put(">=", new Result(singleLit, "CRE-3", "CRE-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testDescription() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsDescription.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "description";
        final String singleLit = "fun";
        final String listLit = "(fun)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", new Result("EMPTY", "HSP-3"));
        matrix.put("is not", new Result("EMPTY", "HSP-2", "HSP-1"));
        matrix.put("~", new Result(singleLit, "HSP-1"));
        matrix.put("!~", new Result(singleLit, "HSP-2"));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testDue() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsDueDate.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "due";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-13', '2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testDueDate() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsDueDate.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "duedate";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-13', '2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testEnvironment() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsEnvironment.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "environment";
        final String singleLit = "fun";
        final String listLit = "(fun)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", new Result("EMPTY", "HSP-3"));
        matrix.put("is not", new Result("EMPTY", "HSP-2", "HSP-1"));
        matrix.put("~", new Result(singleLit, "HSP-1"));
        matrix.put("!~", new Result(singleLit, "HSP-2"));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testFixVersion() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsFixVersion.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "fixVersion";
        final String singleLit = "'New Version 4'";
        final String listLit = "('New Version 1', 'New Version 4')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testIssue() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsIssue.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "issue";
        final String singleLit = "'HSP-2'";
        final String listLit = "('HSP-1', 'HSP-2')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));
        matrix.put("is", Result.error("EMPTY"));
        matrix.put("is not", Result.error("EMPTY"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testLevel() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsLevel.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "level";
        final String singleLit = "'Level 2'";
        final String listLit = "('Level 2')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2"));
        matrix.put("not in", new Result(listLit, "HSP-1"));
        matrix.put("is", new Result("EMPTY", "HSP-3"));
        matrix.put("is not", new Result("EMPTY", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testOriginalEstimate() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsTime.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "originalEstimate";
        final String singleLit = "120";
        final String listLit = "(60, 120)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testParent() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsParent.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "parent";
        final String singleLit = "'HSP-2'";
        final String listLit = "('HSP-1', 'HSP-2')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-4"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-4", "HSP-3"));
        matrix.put("not in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("is", Result.error("EMPTY"));
        matrix.put("is not", Result.error("EMPTY"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testPriority() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsPriority.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "priority";
        final String singleLit = "Critical";
        final String listLit = "(Blocker, Critical)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-3"));
        matrix.put("<=", new Result(singleLit, "HSP-3", "HSP-2"));
        matrix.put(">", new Result(singleLit, "HSP-1"));
        matrix.put(">=", new Result(singleLit, "HSP-2", "HSP-1"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testProject() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsProject.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "project";
        final String singleLit = "homosapien";
        final String listLit = "(monkey, homosapien)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-1"));
        matrix.put("!=", new Result(singleLit, "MKY-1"));
        matrix.put("in", new Result(listLit, "MKY-1", "HSP-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "MKY-1", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testRemainingEstimate() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsTime.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "remainingEstimate";
        final String singleLit = "60";
        final String listLit = "(40, 60)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-3", "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-3", "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testReporter() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsReporter.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "reporter";
        final String singleLit = ADMIN_USERNAME;
        final String listLit = "(fred, admin)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-1"));
        matrix.put("!=", new Result(singleLit, "HSP-2"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testResolution() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "resolution";
        final String singleLit = "\"Won't Fix\"";
        final String listLit = "(\"Won't Fix\", Fixed)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testResolved() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "resolved";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("in", new Result(listLit));
        matrix.put("not in", new Result(listLit, "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3", "HSP-2"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testResolutionDate() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "resolutionDate";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("in", new Result(listLit));
        matrix.put("not in", new Result(listLit, "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3", "HSP-2"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testSavedFilter() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsSavedFilter.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "savedFilter";
        final String singleLit = "Bugs";
        final String listLit = "(Bugs, Tasks)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-1"));
        matrix.put("!=", new Result(singleLit, "HSP-2"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", Result.error("EMPTY"));
        matrix.put("is not", Result.error("EMPTY"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testStatus() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsStatus.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "status";
        final String singleLit = "Open";
        final String listLit = "(Open, 'In Progress')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-1"));
        matrix.put("!=", new Result(singleLit, "HSP-2"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testSummary() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsSummary.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "summary";
        final String singleLit = "OneTwoThree";
        final String listLit = "(OneTwoThree)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "HSP-2", "HSP-1"));
        matrix.put("~", new Result(singleLit, "HSP-1"));
        matrix.put("!~", new Result(singleLit, "HSP-2"));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testText() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsSummary.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "text";
        final String singleLit = "OneTwoThree";
        final String listLit = "(OneTwoThree)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", Result.error("EMPTY"));
        matrix.put("is not", Result.error("EMPTY"));
        matrix.put("~", new Result(singleLit, "HSP-1"));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testTimeSpent() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsTime.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "timeSpent";
        final String singleLit = "60";
        final String listLit = "(20, 60)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testType() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsType.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "type";
        final String singleLit = "Bug";
        final String listLit = "(Bug, 'New Feature')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-1"));
        matrix.put("!=", new Result(singleLit, "HSP-2"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testUpdated() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "updated";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit));
        matrix.put("!=", new Result(singleLit, "HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("in", new Result(listLit));
        matrix.put("not in", new Result(listLit, "HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-4", "HSP-3", "HSP-2"));
        matrix.put(">=", new Result(singleLit, "HSP-4", "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testUpdatedDate() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "updateddate";
        final String singleLit = "'2009-05-14'";
        final String listLit = "('2009-05-14')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit));
        matrix.put("!=", new Result(singleLit, "HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("in", new Result(listLit));
        matrix.put("not in", new Result(listLit, "HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("is", new Result("EMPTY"));
        matrix.put("is not", new Result("EMPTY", "HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-4", "HSP-3", "HSP-2"));
        matrix.put(">=", new Result(singleLit, "HSP-4", "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testVotes() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsVotes.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "votes";
        final String singleLit = "1";
        final String listLit = "(1, 2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-1"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-2"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", Result.error("EMPTY"));
        matrix.put("is not", Result.error("EMPTY"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-3"));
        matrix.put("<=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-2"));
        matrix.put(">=", new Result(singleLit, "HSP-2", "HSP-1"));

        _testOperatorMatrix(fieldName, matrix);
    }

    public void testWorkRatio() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsTime.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "workRatio";
        final String singleLit = "50";
        final String listLit = "(33, 50)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "HSP-2"));
        matrix.put("!=", new Result(singleLit, "HSP-3", "HSP-1"));
        matrix.put("in", new Result(listLit, "HSP-2", "HSP-1"));
        matrix.put("not in", new Result(listLit, "HSP-3"));
        matrix.put("is", new Result("EMPTY", "HSP-4"));
        matrix.put("is not", new Result("EMPTY", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "HSP-1"));
        matrix.put("<=", new Result(singleLit, "HSP-2", "HSP-1"));
        matrix.put(">", new Result(singleLit, "HSP-3"));
        matrix.put(">=", new Result(singleLit, "HSP-3", "HSP-2"));

        _testOperatorMatrix(fieldName, matrix);
    }

    private void _testOperatorMatrix(final String fieldName, final Map<String, Result> matrix)
    {
        for (Map.Entry<String, Result> testEntry : matrix.entrySet())
        {
            final String operator = testEntry.getKey();
            final Result result = testEntry.getValue();

            final String operand = result.operand;
            final String jqlQuery = String.format("%s %s %s", fieldName, operator, operand);
            final String[] keys = result.issueKeys;
            final ErrorType errorType = result.errorType;
            if (keys == null && errorType != null)
            {
                final String errorMsg = errorType.formatError(fieldName, operator);
                issueTableAssertions.assertSearchWithError(jqlQuery, errorMsg);
            }
            else
            {
                assertSearchResults(jqlQuery, keys);
            }
        }
    }

    /**
     * Executes a JQL query search and asserts the expected issue keys from the result set.
     *
     * @param jqlQuery the query to execute
     * @param issueKeys the issue keys expected in the result set. Ordering is important, and the number of keys specified
     * will be asserted against the number of results
     */
    private void assertSearchResults(final String jqlQuery, final String... issueKeys)
    {
        final List<SearchResultsCondition> conditions = new ArrayList<SearchResultsCondition>();
        conditions.add(new ContainsIssueKeysCondition(assertions.getTextAssertions(), issueKeys));

        navigation.issueNavigator().createSearch(jqlQuery);
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);
    }

    private static class Result
    {
        private String operand;
        private String[] issueKeys;
        private ErrorType errorType;

        private static Result error(final String operand)
        {
            return new Result(operand, null, ErrorType.OPERATOR_NOT_SUPPORTED);
        }

        private static Result errorEmptyNotSupported(final String operand)
        {
            return new Result(operand, null, ErrorType.EMPTY_NOT_SUPPORTED);
        }

        private Result(final String operand, final String... issueKeys)
        {
            this.operand = operand;
            this.issueKeys = issueKeys;
            this.errorType = null;
        }

        private Result(final String operand, final String[] issueKeys, final ErrorType errorType)
        {
            this.operand = operand;
            this.issueKeys = issueKeys;
            this.errorType = errorType;
        }
    }

    private static enum ErrorType
    {
        EMPTY_NOT_SUPPORTED()
                {
                    String formatError(final String fieldName, final String operator)
                    {
                        return String.format("The field '%s' does not support searching for EMPTY values.", fieldName);
                    }
                },
        OPERATOR_NOT_SUPPORTED()
                {
                    String formatError(final String fieldName, final String operator)
                    {
                        return String.format("The operator '%s' is not supported by the '%s' field.", operator, fieldName);
                    }
                };

        abstract String formatError(String fieldName, String operator);
    }
}
