package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.functest.framework.admin.TimeTracking.Format;
import static com.atlassian.jira.functest.framework.admin.TimeTracking.Mode;
import static com.atlassian.jira.functest.framework.admin.TimeTracking.Unit;
import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests the interpretations of different literals (operands) for each system field clause.
 *
 * This does not test resolution of domain objects based on permissions. It does however test resolution of domain
 * objects based on ids vs names.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSystemFieldLiterals extends AbstractJqlFuncTest
{
    public static final String ORIGINAL_ESTIMATE = "originalEstimate";
    public static final String REMAINING_ESTIMATE = "remainingEstimate";
    public static final String TIME_SPENT = "timeSpent";

    public void testAffectedVersion() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsVersion.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "affectedVersion";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'New Version 1'", new Result("HSP-1"));
        matrix.put("'10000'", new Result("HSP-2"));
        matrix.put("'10001'", new Result("HSP-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10000", new Result("HSP-1"));
        matrix.put("10001", new Result("HSP-2"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result("HSP-3"));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testAssignee() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsAssignee.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "assignee";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("administrator", new Result("HSP-1"));
        matrix.put(ADMIN_USERNAME, new Result("HSP-2"));
        matrix.put("'" + FRED_FULLNAME + "'", new Result("HSP-3"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("EMPTY", new Result("HSP-4"));
        matrix.put("12345", new Result("HSP-5"));
        matrix.put("'12345'", new Result("HSP-5"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testCategory() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsCategory.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "category";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Category1'", new Result("CAT-1"));
        matrix.put("'10000'", new Result("CATTWO-1"));
        matrix.put("'10001'", new Result("CATTWO-1"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10000", new Result("CAT-1"));
        matrix.put("10001", new Result("CATTWO-1"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result("NOT-1"));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testComment() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsComment.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "comment";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Test'", new Result("COM-1"));
        matrix.put("'-Test'", new Result("COM-2"));
        matrix.put("'123456'", new Result("COM-2"));
        matrix.put("'?'", new Result(ErrorType.INVALID_START_CHAR));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("Test", new Result("COM-1"));
        matrix.put("123456", new Result("COM-2"));
        matrix.put("EMPTY", new Result(ErrorType.EMPTY_NOT_SUPPORTED));

        _testLiteralMatrix(fieldName, "~", matrix);
    }

    public void testComponent() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsComponent.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "component";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Component 1'", new Result("CMP-1"));
        matrix.put("'10010'", new Result("CMP-2"));
        matrix.put("'10011'", new Result("CMP-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10010", new Result("CMP-1"));
        matrix.put("10011", new Result("CMP-2"));
        matrix.put("EMPTY", new Result("CMP-3"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testCreated() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsCreated.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "created";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("1242604510000", new Result("CRE-3", "CRE-2", "CRE-1"));
        matrix.put("'1d'", new Result("CRE-3", "CRE-2", "CRE-1"));
        matrix.put("'dd'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/13 18:50'", new Result("CRE-1"));
        matrix.put("'2009-05-13 18:50'", new Result("CRE-1"));
        matrix.put("'2009/05/14'", new Result("CRE-1"));
        matrix.put("'2009-05-14'", new Result("CRE-1"));
        matrix.put("'14/May/09'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'09/1/1'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/14 bad'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2010/02/35'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_DATE_FORMAT));

        _testLiteralMatrix(fieldName, "<", matrix);
    }

    public void testDescription() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsDescription.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "description";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Test'", new Result("HSP-1"));
        matrix.put("'-Test'", new Result("HSP-2"));
        matrix.put("'123456'", new Result("HSP-2"));
        matrix.put("'?'", new Result(ErrorType.INVALID_START_CHAR));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("Test", new Result("HSP-1"));
        matrix.put("123456", new Result("HSP-2"));
        matrix.put("EMPTY", new Result("HSP-3"));

        _testLiteralMatrix(fieldName, "~", matrix);
    }

    public void testDueDate() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsDueDate.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "due";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("1242204510000", new Result("HSP-1"));

        matrix.put("'dd'", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));
        matrix.put("'2009/05/13'", new Result("HSP-1"));
        matrix.put("'2009-05-13'", new Result("HSP-1"));
        matrix.put("'2009/05/14'", new Result("HSP-2"));
        matrix.put("'2009-05-14'", new Result("HSP-2"));
        matrix.put("'14/May/09'", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));

        _testLiteralMatrix(fieldName, "=", matrix);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("'1d'", new Result("HSP-3", "HSP-2", "HSP-1"));
        _testLiteralMatrix(fieldName, "<", matrix);
    }

    public void testEnvironment() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsEnvironment.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "environment";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Test'", new Result("HSP-1"));
        matrix.put("'-Test'", new Result("HSP-2"));
        matrix.put("'123456'", new Result("HSP-2"));
        matrix.put("'?'", new Result(ErrorType.INVALID_START_CHAR));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("Test", new Result("HSP-1"));
        matrix.put("123456", new Result("HSP-2"));
        matrix.put("EMPTY", new Result("HSP-3"));

        _testLiteralMatrix(fieldName, "~", matrix);
    }

    public void testFixVersion() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsVersion.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "fixVersion";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'New Version 1'", new Result("HSP-1"));
        matrix.put("'10000'", new Result("HSP-2"));
        matrix.put("'10001'", new Result("HSP-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10000", new Result("HSP-1"));
        matrix.put("10001", new Result("HSP-2"));
        matrix.put("EMPTY", new Result("HSP-3"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testIssue() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsIssue.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "issue";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'10000'", new Result(ErrorType.ISSUE_KEY_INVALID));
        matrix.put("''", new Result(ErrorType.ISSUE_KEY_INVALID));
        matrix.put("'HSP-2'", new Result("HSP-2"));
        matrix.put("10000", new Result("HSP-1"));
        matrix.put("10001", new Result("HSP-2"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result());

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testLevel() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsLevel.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "level";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Level 1'", new Result("HSP-1"));
        matrix.put("'10000'", new Result("HSP-2"));
        matrix.put("'10010'", new Result("HSP-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10000", new Result("HSP-1"));
        matrix.put("10010", new Result("HSP-2"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result("HSP-3"));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testTimeTracking() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsTime.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'60'", new Result("HSP-1"));
        matrix.put("'2h'", new Result("HSP-2"));
        matrix.put("'0h'", new Result("HSP-5"));
        matrix.put("'hh'", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("240", new Result("HSP-3"));
        matrix.put("0", new Result("HSP-5"));
        matrix.put("'-240'", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("EMPTY", new Result("HSP-4"));
        //JRA-25572: 1d = 8 hrs as configured by time tracking.
        matrix.put("1d", new Result("HSP-7"));
        //JRA-25572: 1w = 40 hrs as configured by time tracking.
        matrix.put("1w", new Result("HSP-8"));
        _testLiteralMatrix(ORIGINAL_ESTIMATE, matrix);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("'40'", new Result("HSP-1"));
        matrix.put("'1h'", new Result("HSP-3", "HSP-2"));
        matrix.put("'0m'", new Result("HSP-5"));
        matrix.put("'hh'", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("60", new Result("HSP-3", "HSP-2"));
        matrix.put("0", new Result("HSP-5"));
        matrix.put("'-60'", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("EMPTY", new Result("HSP-4"));
        //JRA-25572: 1d = 8 hrs as configured by time tracking.
        matrix.put("1d", new Result("HSP-7"));
        //JRA-25572: 1w = 40 hrs as configured by time tracking.
        matrix.put("1w", new Result("HSP-8"));
        _testLiteralMatrix(REMAINING_ESTIMATE, matrix);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("'20'", new Result("HSP-1"));
        matrix.put("'1h'", new Result("HSP-2"));
        matrix.put("'0d'", new Result());
        matrix.put("'hh'", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("180", new Result("HSP-3"));
        matrix.put("0", new Result());
        matrix.put("'-180'", new Result(ErrorType.INVALID_DURATION_FORMAT));
        matrix.put("EMPTY", new Result("HSP-4"));
        //JRA-25572: 1d = 8 hrs as configured by time tracking.
        matrix.put("1d", new Result("HSP-7"));
        //JRA-25572: 1w = 40 hrs as configured by time tracking.
        matrix.put("1w", new Result("HSP-8"));
        _testLiteralMatrix(TIME_SPENT, matrix);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("'33'", new Result("HSP-1"));
        matrix.put("'33.3'", new Result(ErrorType.INVALID_INTEGER_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_INTEGER_FORMAT));
        matrix.put("0", new Result());
        matrix.put("EMPTY", new Result("HSP-4"));
        _testLiteralMatrix("workRatio", matrix);

        //JRA-25572: We are going to change time tracking configuration to 1d = 6d; 1w = 2d; default unit = hours.
        administration.timeTracking().disable();
        administration.timeTracking().enable("6", "2", Format.PRETTY, Unit.HOUR, Mode.MODERN);
        administration.reIndex();

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("1d", new Result("HSP-6"));
        matrix.put("1w", new Result("HSP-10"));
        matrix.put("8", new Result("HSP-7"));

        _testLiteralMatrix(TIME_SPENT, matrix);
        _testLiteralMatrix(ORIGINAL_ESTIMATE, matrix);
        _testLiteralMatrix(REMAINING_ESTIMATE, matrix);
    }

    public void testParent() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsParent.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "parent";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'10000'", new Result(ErrorType.ISSUE_KEY_INVALID));
        matrix.put("''", new Result(ErrorType.ISSUE_KEY_INVALID));
        matrix.put("'HSP-2'", new Result("HSP-4"));
        matrix.put("10000", new Result("HSP-3"));
        matrix.put("10001", new Result("HSP-4"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result(ErrorType.EMPTY_NOT_SUPPORTED));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testPriority() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsPriority.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "priority";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Blocker'", new Result("HSP-1"));
        matrix.put("'1'", new Result("HSP-2", "HSP-1"));
        matrix.put("'2'", new Result("HSP-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("1", new Result("HSP-1"));
        matrix.put("2", new Result("HSP-2"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result());

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testProject() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsProject.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "project";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'10010'", new Result("NUM-1"));
        matrix.put("'10011'", new Result("NUM-1"));
        matrix.put("'DUP'", new Result("DUP-1"));
        matrix.put("'HSP'", new Result("HSP-1"));
        matrix.put("'homosapien'", new Result("HSP-1"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10010", new Result("HSP-1"));
        matrix.put("10011", new Result("NUM-1"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result());

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testReporter() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsAssignee.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "reporter";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("administrator", new Result("HSP-1"));
        matrix.put(ADMIN_USERNAME, new Result("HSP-2"));
        matrix.put("'" + FRED_FULLNAME + "'", new Result("HSP-3"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("EMPTY", new Result("HSP-4"));
        matrix.put("12345", new Result("HSP-5"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("'12345'", new Result("HSP-5"));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testResolution() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "resolution";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Fixed'", new Result("HSP-1"));
        matrix.put("'1'", new Result("HSP-2", "HSP-1"));
        matrix.put("'3'", new Result("HSP-3"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("1", new Result("HSP-1"));
        matrix.put("2", new Result("HSP-2"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result("HSP-4"));

        _testLiteralMatrix(fieldName, matrix);

        // test the correctness of results using the "unresolved" operand

        assertSearchResults("resolution = unresolved", "HSP-4");
        assertSearchResults("resolution in (unresolved)", "HSP-4");
        assertSearchResults("resolution in (unresolved, Fixed)", "HSP-4", "HSP-1");
        assertSearchResults("resolution != unresolved", "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in (unresolved, Fixed)", "HSP-3", "HSP-2");
        assertSearchAndErrorMessagePresent("resolution = '\"unresolved\"'", ErrorType.NAME_NOT_FOUND.formatError(fieldName, "'\"unresolved\"'"));

        // add "unresolved"
        administration.resolutions().addResolution("unRESOLVED");

        assertSearchResults("resolution = unresolved", "HSP-4");
        assertSearchResults("resolution in (unresolved)", "HSP-4");
        assertSearchResults("resolution in (unresolved, Fixed)", "HSP-4", "HSP-1");
        assertSearchResults("resolution != unresolved", "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in (unresolved)", "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in (unresolved, Fixed)", "HSP-3", "HSP-2");

        assertSearchResults("resolution = '\"unresolved\"'");
        assertSearchResults("resolution in ('\"unresolved\"')");
        assertSearchResults("resolution in ('\"unresolved\"', Fixed)", "HSP-1");
        assertSearchResults("resolution != '\"unresolved\"'", "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in ('\"unresolved\"')", "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in ('\"unresolved\"', Fixed)", "HSP-3", "HSP-2");

        // set the resolution of an issue to "unresolved"
        String issue = navigation.issue().createIssue("homosapien", "Bug", "test");
        navigation.issue().resolveIssue(issue, "unRESOLVED", "test");

        assertSearchResults("resolution = unresolved", "HSP-4");
        assertSearchResults("resolution in (unresolved)", "HSP-4");
        assertSearchResults("resolution in (unresolved, Fixed)", "HSP-4", "HSP-1");
        assertSearchResults("resolution != unresolved", issue, "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in (unresolved)", issue, "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in (unresolved, Fixed)", issue, "HSP-3", "HSP-2");

        assertSearchResults("resolution = '\"unresolved\"'", issue);
        assertSearchResults("resolution in ('\"unresolved\"')", issue);
        assertSearchResults("resolution in ('\"unresolved\"', Fixed)", issue, "HSP-1");
        assertSearchResults("resolution != '\"unresolved\"'", "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in ('\"unresolved\"')", "HSP-3", "HSP-2", "HSP-1");
        assertSearchResults("resolution not in ('\"unresolved\"', Fixed)", "HSP-3", "HSP-2");

        // add "'unresolved'"
        assertSearchAndErrorMessagePresent("resolution = \"\\\"\\\"unresolved\\\"\\\"\"", ErrorType.NAME_NOT_FOUND.formatError(fieldName, "'\"\"unresolved\"\"'"));

        administration.resolutions().addResolution("\"unresolved\"");

        assertSearchResults("resolution = \"\\\"\\\"unresolved\\\"\\\"\"");

        issue = navigation.issue().createIssue("homosapien", "Bug", "test");
        navigation.issue().resolveIssue(issue, "\"unresolved\"", "test");

        assertSearchResults("resolution = \"\\\"\\\"unresolved\\\"\\\"\"", issue);
    }

    public void testResolutionDate() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "resolved";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("1242604510000", new Result("HSP-3", "HSP-2", "HSP-1"));
        matrix.put("'1d'", new Result("HSP-3", "HSP-2", "HSP-1"));
        matrix.put("'dd'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/13 18:50'", new Result("HSP-1"));
        matrix.put("'2009-05-13 18:50'", new Result("HSP-1"));
        matrix.put("'2009/05/14'", new Result("HSP-1"));
        matrix.put("'2009-05-14'", new Result("HSP-1"));
        matrix.put("'14/May/09'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'09/1/1'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/14 bad'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2010/02/35'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_DATE_FORMAT));

        _testLiteralMatrix(fieldName, "<", matrix);
    }

    public void testSavedFilter() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsSavedFilter.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "savedFilter";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'10000'", new Result("HSP-2"));
        matrix.put("'10001'", new Result("HSP-2"));
        matrix.put("'Bugs'", new Result("HSP-1"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10000", new Result("HSP-1"));
        matrix.put("10001", new Result("HSP-2"));
        matrix.put("999", new Result("HSP-3"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result(ErrorType.EMPTY_NOT_SUPPORTED));

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testStatus() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsStatus.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "status";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'1'", new Result("HSP-2", "HSP-1"));
        matrix.put("'3'", new Result("HSP-2"));
        matrix.put("'Open'", new Result("HSP-1"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("1", new Result("HSP-1"));
        matrix.put("3", new Result("HSP-2"));
        matrix.put("999", new Result("HSP-3"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result());

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testSummary() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsSummary.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "summary";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'OneTwoThree'", new Result("HSP-1"));
        matrix.put("'-OneTwoThree'", new Result("HSP-2"));
        matrix.put("'456'", new Result("HSP-2"));
        matrix.put("OneTwoThree", new Result("HSP-1"));
        matrix.put("456", new Result("HSP-2"));
        matrix.put("-456", new Result("HSP-1"));
        matrix.put("'?'", new Result(ErrorType.INVALID_START_CHAR));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("EMPTY", new Result());

        _testLiteralMatrix(fieldName, "~", matrix);
    }

    public void testText() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsSummary.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "text";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'OneTwoThree'", new Result("HSP-1"));
        matrix.put("'-OneTwoThree'", new Result("HSP-2"));
        matrix.put("'456'", new Result("HSP-2"));
        matrix.put("OneTwoThree", new Result("HSP-1"));
        matrix.put("456", new Result("HSP-2"));
        matrix.put("-456", new Result("HSP-1"));
        matrix.put("'?'", new Result(ErrorType.INVALID_START_CHAR));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("EMPTY", new Result(ErrorType.EMPTY_NOT_SUPPORTED));

        _testLiteralMatrix(fieldName, "~", matrix);
    }

    public void testType() throws Exception
    {
        administration.restoreData("TestSystemFieldLiteralsType.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "type";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'1'", new Result("HSP-2", "HSP-1"));
        matrix.put("'2'", new Result("HSP-2"));
        matrix.put("'Bug'", new Result("HSP-1"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("1", new Result("HSP-1"));
        matrix.put("2", new Result("HSP-2"));
        matrix.put("999", new Result("HSP-3"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result());

        _testLiteralMatrix(fieldName, matrix);
    }

    public void testUpdated() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsResolution.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "updated";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("1242604510000", new Result("HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("'1d'", new Result("HSP-4", "HSP-3", "HSP-2", "HSP-1"));
        matrix.put("'dd'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/13 18:50'", new Result("HSP-1"));
        matrix.put("'2009-05-13 18:50'", new Result("HSP-1"));
        matrix.put("'2009/05/14'", new Result("HSP-1"));
        matrix.put("'2009-05-14'", new Result("HSP-1"));
        matrix.put("'14/May/09'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'09/1/1'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/14 bad'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2010/02/35'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_DATE_FORMAT));

        _testLiteralMatrix(fieldName, "<", matrix);
    }

    public void testVotes() throws Exception
    {
        administration.restoreData("TestSystemFieldOperatorsVotes.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        final String fieldName = "votes";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'0'", new Result("HSP-3"));
        matrix.put("'1'", new Result("HSP-1"));
        matrix.put("'-3'", new Result(ErrorType.INVALID_VOTES_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_VOTES_FORMAT));
        matrix.put("2", new Result("HSP-2"));
        matrix.put("-3", new Result(ErrorType.INVALID_VOTES_FORMAT_WITH_QUOTES));
        matrix.put("EMPTY", new Result(ErrorType.INVALID_VOTES_FORMAT_WITH_QUOTES));

        _testLiteralMatrix(fieldName, matrix);
    }

    private void _testLiteralMatrix(final String fieldName, final Map<String, Result> matrix)
    {
        _testLiteralMatrix(fieldName, "=", matrix);
    }

    private void _testLiteralMatrix(final String fieldName, final String operator, final Map<String, Result> matrix)
    {
        for (Map.Entry<String, Result> entry : matrix.entrySet())
        {
            final String operand = entry.getKey();
            final String jqlQuery = String.format("%s %s %s", fieldName, operator, operand);
            final Result result = entry.getValue();

            if (result.issueKeys == null && result.errorType != null)
            {
                final String errorMsg = result.errorType.formatError(fieldName, operand);
                assertSearchAndErrorMessagePresent(jqlQuery, errorMsg);
            }
            else
            {
                assertSearchResults(jqlQuery, result.issueKeys);
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
        conditions.add(new NumberOfIssuesCondition(assertions.getTextAssertions(), issueKeys.length));

        navigation.issueNavigator().createSearch(jqlQuery);
        assertions.getIssueNavigatorAssertions().assertSearchResults(conditions);
    }

    private void assertSearchAndErrorMessagePresent(final String jqlQuery, final String msg)
    {
        navigation.issueNavigator().createSearch(jqlQuery);
        //Ideally this should be checking the actual error message, but this requires conversion to webdriver
        tester.assertElementNotPresent("issuetable");
    }

    private void assertErrorMessagePresent(final String msg)
    {
        assertions.getTextAssertions().assertTextPresent(new WebPageLocator(tester), msg);
    }

    private static class Result
    {
        private String[] issueKeys = null;
        private ErrorType errorType = null;

        private Result(final String... issueKeys)
        {
            this.issueKeys = issueKeys;
        }

        private Result(final ErrorType errorType)
        {
            this.errorType = errorType;
        }
    }

    private static enum ErrorType
    {
        EMPTY_NOT_SUPPORTED()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The field '%s' does not support searching for EMPTY values.", fieldName);
                    }
                },
        EMPTY_STRING_NOT_SUPPORTED()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The field '%s' does not support searching for an empty string.", fieldName);
                    }
                },
        ISSUE_KEY_INVALID()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The issue key %s for field '%s' is invalid.", operand, fieldName);
                    }
                },
        INVALID_VOTES_FORMAT()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("Value %s is invalid for the '%s' field. Votes must be a positive whole number.", operand, fieldName);
                    }
                },
        INVALID_VOTES_FORMAT_WITH_QUOTES()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("Value '%s' is invalid for the '%s' field. Votes must be a positive whole number.", operand, fieldName);
                    }
                },
        INVALID_INTEGER_FORMAT()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The value %s for field '%s' is invalid - please specify an integer.", operand, fieldName);
                    }
                },
        INVALID_DURATION_FORMAT()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The value %s for field '%s' is invalid. Please specify a positive duration format; for example: '1h 30m', '2d'.", operand, fieldName);
                    }
                },
        INVALID_DATE_FORMAT()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("Date value %s for field '%s' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.", operand, fieldName);
                    }
                },
        INVALID_RELATIVE_DATE_FORMAT()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("Date value %s for field '%s' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'.", operand, fieldName);
                    }
                },
        CANT_PARSE_QUERY()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("Unable to parse the text %s for field '%s'.", operand, fieldName);
                    }
                },
        INVALID_START_CHAR()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The text query %s for field '%s' is not allowed to start with %1$s.", operand, fieldName);
                    }
                },
        NAME_NOT_FOUND()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The value %s does not exist for the field '%s'.", operand, fieldName);
                    }
                },
        ID_NOT_FOUND()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("A value with ID '%s' does not exist for the field '%s'.", operand, fieldName);
                    }
                };

        abstract String formatError(String fieldName, String operand);
    }
}
