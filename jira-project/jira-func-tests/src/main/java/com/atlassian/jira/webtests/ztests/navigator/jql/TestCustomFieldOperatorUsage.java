package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests the validity of all the operators against all the custom field clauses.
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
public class TestCustomFieldOperatorUsage extends FuncTestCase
{
    private static final ThreadLocal<AtomicBoolean> dataSetUp = new ThreadLocal<AtomicBoolean>() {
        @Override
        protected AtomicBoolean initialValue()
        {
            return new AtomicBoolean(false);
        }
    };

    @Override
    protected void setUpTest()
    {
        super.setUpTest();

        if (!dataSetUp.get().getAndSet(true))
        {
            administration.restoreData("TestCustomFieldOperators.xml");
            backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        }
    }

    public void testCascadingSelect() throws Exception
    {
        final String fieldName = "CSF";
        final String projKey = "ONE";
        final String singleLit = "'child'";
        final String listLit = "('child', 'child2')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "ONE-3"));
        matrix.put("!=", new Result(singleLit, "ONE-4", "ONE-2"));
        matrix.put("in", new Result(listLit, "ONE-4", "ONE-3"));
        matrix.put("not in", new Result(listLit, "ONE-2"));
        matrix.put("is", new Result("EMPTY", "ONE-1"));
        matrix.put("is not", new Result("EMPTY", "ONE-4", "ONE-3", "ONE-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }
    
    public void testDatePicker() throws Exception
    {
        final String fieldName = "DP";
        final String projKey = "TWO";
        final String singleLit = "'2009-05-13'";
        final String listLit = "('2009-05-11', '2009-05-13')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "TWO-3"));
        matrix.put("!=", new Result(singleLit, "TWO-4", "TWO-2"));
        matrix.put("in", new Result(listLit, "TWO-3", "TWO-2"));
        matrix.put("not in", new Result(listLit, "TWO-4"));
        matrix.put("is", new Result("EMPTY", "TWO-1"));
        matrix.put("is not", new Result("EMPTY", "TWO-4", "TWO-3", "TWO-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "TWO-2"));
        matrix.put("<=", new Result(singleLit, "TWO-3", "TWO-2"));
        matrix.put(">", new Result(singleLit, "TWO-4"));
        matrix.put(">=", new Result(singleLit, "TWO-4", "TWO-3"));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testDateTime() throws Exception
    {
        final String fieldName = "DT";
        final String projKey = "THREE";
        final String singleLit = "'2009-05-11 12:00'";
        final String listLit = "('2009-05-11 09:00', '2009-05-11 12:00')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "THREE-3"));
        matrix.put("!=", new Result(singleLit, "THREE-4", "THREE-2"));
        matrix.put("in", new Result(listLit, "THREE-3", "THREE-2"));
        matrix.put("not in", new Result(listLit, "THREE-4"));
        matrix.put("is", new Result("EMPTY", "THREE-1"));
        matrix.put("is not", new Result("EMPTY", "THREE-4", "THREE-3", "THREE-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "THREE-2"));
        matrix.put("<=", new Result(singleLit, "THREE-3", "THREE-2"));
        matrix.put(">", new Result(singleLit, "THREE-4"));
        matrix.put(">=", new Result(singleLit, "THREE-4", "THREE-3"));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testFreeTextField() throws Exception
    {
        final String fieldName = "FTF";
        final String projKey = "FOUR";
        final String singleLit = "fun";
        final String listLit = "(fun)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", new Result("EMPTY", "FOUR-1"));
        matrix.put("is not", new Result("EMPTY", "FOUR-3", "FOUR-2"));
        matrix.put("~", new Result(singleLit, "FOUR-2"));
        matrix.put("!~", new Result(singleLit, "FOUR-3"));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testGroupPicker() throws Exception
    {
        final String fieldName = "GP";
        final String projKey = "FIVE";
        final String singleLit = "'jira-users'";
        final String listLit = "('jira-users', 'jira-administrators')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "FIVE-2"));
        matrix.put("!=", new Result(singleLit, "FIVE-3"));
        matrix.put("in", new Result(listLit, "FIVE-3", "FIVE-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "FIVE-1"));
        matrix.put("is not", new Result("EMPTY", "FIVE-3", "FIVE-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testImportId() throws Exception
    {
        final String fieldName = "II";
        final String projKey = "SIX";
        final String singleLit = "4";
        final String listLit = "(2, 4)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "SIX-3"));
        matrix.put("!=", new Result(singleLit, "SIX-4", "SIX-2"));
        matrix.put("in", new Result(listLit, "SIX-3", "SIX-2"));
        matrix.put("not in", new Result(listLit, "SIX-4"));
        matrix.put("is", new Result("EMPTY", "SIX-1"));
        matrix.put("is not", new Result("EMPTY", "SIX-4", "SIX-3", "SIX-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "SIX-2"));
        matrix.put("<=", new Result(singleLit, "SIX-3", "SIX-2"));
        matrix.put(">", new Result(singleLit, "SIX-4"));
        matrix.put(">=", new Result(singleLit, "SIX-4", "SIX-3"));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testMultiCheckboxes() throws Exception
    {
        final String fieldName = "MC";
        final String projKey = "SEVEN";
        final String singleLit = "opt1";
        final String listLit = "(opt1, opt2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "SEVEN-4", "SEVEN-2"));
        matrix.put("!=", new Result(singleLit, "SEVEN-3"));
        matrix.put("in", new Result(listLit, "SEVEN-4", "SEVEN-3", "SEVEN-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "SEVEN-1"));
        matrix.put("is not", new Result("EMPTY", "SEVEN-4", "SEVEN-3", "SEVEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testMultiGroupPicker() throws Exception
    {
        final String fieldName = "MGP";
        final String projKey = "EIGHT";
        final String singleLit = "'jira-users'";
        final String listLit = "('jira-users', 'jira-administrators')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "EIGHT-4", "EIGHT-2"));
        matrix.put("!=", new Result(singleLit, "EIGHT-3"));
        matrix.put("in", new Result(listLit, "EIGHT-4", "EIGHT-3", "EIGHT-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "EIGHT-1"));
        matrix.put("is not", new Result("EMPTY", "EIGHT-4", "EIGHT-3", "EIGHT-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testMultiSelect() throws Exception
    {
        final String fieldName = "MS";
        final String projKey = "NINE";
        final String singleLit = "select1";
        final String listLit = "(select1, select2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "NINE-4", "NINE-2"));
        matrix.put("!=", new Result(singleLit, "NINE-3"));
        matrix.put("in", new Result(listLit, "NINE-4", "NINE-3", "NINE-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "NINE-1"));
        matrix.put("is not", new Result("EMPTY", "NINE-4", "NINE-3", "NINE-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testMultiUserPicker() throws Exception
    {
        final String fieldName = "MUP";
        final String projKey = "TEN";
        final String singleLit = FRED_USERNAME;
        final String listLit = "(fred, admin)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "TEN-4", "TEN-2"));
        matrix.put("!=", new Result(singleLit, "TEN-3"));
        matrix.put("in", new Result(listLit, "TEN-4", "TEN-3", "TEN-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "TEN-1"));
        matrix.put("is not", new Result("EMPTY", "TEN-4", "TEN-3", "TEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testNumberField() throws Exception
    {
        final String fieldName = "NF";
        final String projKey = "ELEVEN";
        final String singleLit = "20";
        final String listLit = "(10, 20)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "ELEVEN-3"));
        matrix.put("!=", new Result(singleLit, "ELEVEN-4", "ELEVEN-2"));
        matrix.put("in", new Result(listLit, "ELEVEN-3", "ELEVEN-2"));
        matrix.put("not in", new Result(listLit, "ELEVEN-4"));
        matrix.put("is", new Result("EMPTY", "ELEVEN-1"));
        matrix.put("is not", new Result("EMPTY", "ELEVEN-4", "ELEVEN-3", "ELEVEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "ELEVEN-2"));
        matrix.put("<=", new Result(singleLit, "ELEVEN-3", "ELEVEN-2"));
        matrix.put(">", new Result(singleLit, "ELEVEN-4"));
        matrix.put(">=", new Result(singleLit, "ELEVEN-4", "ELEVEN-3"));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testProjectPicker() throws Exception
    {
        final String fieldName = "PP";
        final String projKey = "TWELVE";
        final String singleLit = "three";
        final String listLit = "(three, four)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "TWELVE-2"));
        matrix.put("!=", new Result(singleLit, "TWELVE-3"));
        matrix.put("in", new Result(listLit, "TWELVE-3", "TWELVE-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "TWELVE-1"));
        matrix.put("is not", new Result("EMPTY", "TWELVE-3", "TWELVE-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testRadioButton() throws Exception
    {
        final String fieldName = "RB";
        final String projKey = "THIRTEEN";
        final String singleLit = "rad1";
        final String listLit = "(rad1, rad2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "THIRTEEN-2"));
        matrix.put("!=", new Result(singleLit, "THIRTEEN-3"));
        matrix.put("in", new Result(listLit, "THIRTEEN-3", "THIRTEEN-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "THIRTEEN-1"));
        matrix.put("is not", new Result("EMPTY", "THIRTEEN-3", "THIRTEEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testReadOnlyTextField() throws Exception
    {
        final String fieldName = "ROTF";
        final String projKey = "FOURTEEN";
        final String singleLit = "fun";
        final String listLit = "(fun)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", new Result("EMPTY", "FOURTEEN-1"));
        matrix.put("is not", new Result("EMPTY", "FOURTEEN-3", "FOURTEEN-2"));
        matrix.put("~", new Result(singleLit, "FOURTEEN-2"));
        matrix.put("!~", new Result(singleLit, "FOURTEEN-3"));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testSelectList() throws Exception
    {
        final String fieldName = "SL";
        final String projKey = "FIFTEEN";
        final String singleLit = "select1";
        final String listLit = "(select1, select2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "FIFTEEN-2"));
        matrix.put("!=", new Result(singleLit, "FIFTEEN-3"));
        matrix.put("in", new Result(listLit, "FIFTEEN-3", "FIFTEEN-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "FIFTEEN-1"));
        matrix.put("is not", new Result("EMPTY", "FIFTEEN-3", "FIFTEEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testSingleVersionPicker() throws Exception
    {
        final String fieldName = "SVP";
        final String projKey = "SIXTEEN";
        final String singleLit = "v2";
        final String listLit = "(v1, v2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "SIXTEEN-3"));
        matrix.put("!=", new Result(singleLit, "SIXTEEN-4", "SIXTEEN-2"));
        matrix.put("in", new Result(listLit, "SIXTEEN-3", "SIXTEEN-2"));
        matrix.put("not in", new Result(listLit, "SIXTEEN-4"));
        matrix.put("is", new Result("EMPTY", "SIXTEEN-1"));
        matrix.put("is not", new Result("EMPTY", "SIXTEEN-4", "SIXTEEN-3", "SIXTEEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "SIXTEEN-2"));
        matrix.put("<=", new Result(singleLit, "SIXTEEN-3", "SIXTEEN-2"));
        matrix.put(">", new Result(singleLit, "SIXTEEN-4"));
        matrix.put(">=", new Result(singleLit, "SIXTEEN-4", "SIXTEEN-3"));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testTextField() throws Exception
    {
        final String fieldName = "TF";
        final String projKey = "SEVENTEEN";
        final String singleLit = "fun";
        final String listLit = "(fun)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", Result.error(singleLit));
        matrix.put("!=", Result.error(singleLit));
        matrix.put("in", Result.error(listLit));
        matrix.put("not in", Result.error(listLit));
        matrix.put("is", new Result("EMPTY", "SEVENTEEN-1"));
        matrix.put("is not", new Result("EMPTY", "SEVENTEEN-3", "SEVENTEEN-2"));
        matrix.put("~", new Result(singleLit, "SEVENTEEN-2"));
        matrix.put("!~", new Result(singleLit, "SEVENTEEN-3"));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testUrlField() throws Exception
    {
        final String fieldName = "URL";
        final String projKey = "EIGHTEEN";
        final String singleLit = "'" + URLEncoder.encode("http://www.atlassian.com", "UTF-8") + "'";
        final String listLit = "('" + URLEncoder.encode("http://www.atlassian.com", "UTF-8") + "')";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "EIGHTEEN-2"));
        matrix.put("!=", new Result(singleLit, "EIGHTEEN-3"));
        matrix.put("in", new Result(listLit, "EIGHTEEN-2"));
        matrix.put("not in", new Result(listLit, "EIGHTEEN-3"));
        matrix.put("is", new Result("EMPTY", "EIGHTEEN-1"));
        matrix.put("is not", new Result("EMPTY", "EIGHTEEN-3", "EIGHTEEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testUserPicker() throws Exception
    {
        final String fieldName = "UP";
        final String projKey = "NINETEEN";
        final String singleLit = FRED_USERNAME;
        final String listLit = "(fred, admin)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "NINETEEN-2"));
        matrix.put("!=", new Result(singleLit, "NINETEEN-3"));
        matrix.put("in", new Result(listLit, "NINETEEN-3", "NINETEEN-2"));
        matrix.put("not in", new Result(listLit));
        matrix.put("is", new Result("EMPTY", "NINETEEN-1"));
        matrix.put("is not", new Result("EMPTY", "NINETEEN-3", "NINETEEN-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", Result.error(singleLit));
        matrix.put("<=", Result.error(singleLit));
        matrix.put(">", Result.error(singleLit));
        matrix.put(">=", Result.error(singleLit));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    public void testVersionPicker() throws Exception
    {
        final String fieldName = "VP";
        final String projKey = "TWENTY";
        final String singleLit = "v2";
        final String listLit = "(v1, v2)";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("=", new Result(singleLit, "TWENTY-5", "TWENTY-3"));
        matrix.put("!=", new Result(singleLit, "TWENTY-4", "TWENTY-2"));
        matrix.put("in", new Result(listLit, "TWENTY-5", "TWENTY-3", "TWENTY-2"));
        matrix.put("not in", new Result(listLit, "TWENTY-4"));
        matrix.put("is", new Result("EMPTY", "TWENTY-1"));
        matrix.put("is not", new Result("EMPTY", "TWENTY-5", "TWENTY-4", "TWENTY-3", "TWENTY-2"));
        matrix.put("~", Result.error(singleLit));
        matrix.put("!~", Result.error(singleLit));
        matrix.put("<", new Result(singleLit, "TWENTY-5", "TWENTY-2"));
        matrix.put("<=", new Result(singleLit, "TWENTY-5", "TWENTY-3", "TWENTY-2"));
        matrix.put(">", new Result(singleLit, "TWENTY-5", "TWENTY-4"));
        matrix.put(">=", new Result(singleLit, "TWENTY-5", "TWENTY-4", "TWENTY-3"));

        _testOperatorMatrix(fieldName, projKey, matrix);
    }

    private void _testOperatorMatrix(final String fieldName, final String projKey, final Map<String, Result> matrix)
    {
        for (Map.Entry<String, Result> testEntry : matrix.entrySet())
        {
            final String operator = testEntry.getKey();
            final Result result = testEntry.getValue();

            final String operand = result.operand;
            final String jqlQuery = String.format("project = %s AND %s %s %s", projKey, fieldName, operator, operand);
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
        conditions.add(new NumberOfIssuesCondition(assertions.getTextAssertions(), issueKeys.length));

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