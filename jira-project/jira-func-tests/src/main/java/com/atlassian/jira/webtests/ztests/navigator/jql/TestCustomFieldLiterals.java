package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.Splitable;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests the interpretations of different literals (operands) for each custom field clause.
 *
 * This does not test resolution of domain objects based on permissions. It does however test resolution of domain
 * objects based on ids vs names.
 *
 * @since v4.0
 */
@Splitable
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestCustomFieldLiterals extends AbstractJqlFuncTest
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
            administration.restoreData("TestCustomFieldLiterals.xml");
            backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
        }
    }

    public void testCascadingSelect() throws Exception
    {
        final String fieldName = "CSF";
        final String projKey = "ONE";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'first option'", new Result("ONE-6", "ONE-5"));
        matrix.put("'10020'", new Result("ONE-7"));
        matrix.put("'10022'", new Result("ONE-7"));
        matrix.put("'Bad String'", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("10020", new Result("ONE-6", "ONE-5"));
        matrix.put("10022", new Result("ONE-7"));
        matrix.put("999", new Result("ONE-6"));
        matrix.put("666", new Result(ErrorType.OPTION_NOT_FOUND_WITH_QUOTES));
        matrix.put("EMPTY", new Result());

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testDatePicker() throws Exception
    {
        final String fieldName = "DP";
        final String projKey = "TWO";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("1242604510000", new Result("TWO-4", "TWO-3", "TWO-2"));
        matrix.put("'1d'", new Result("TWO-4", "TWO-3", "TWO-2"));
        matrix.put("'dd'", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));
        matrix.put("'2009/05/13'", new Result("TWO-2"));
        matrix.put("'2009-05-13 18:50'", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));
        matrix.put("'2009/05/14'", new Result("TWO-3", "TWO-2"));
        matrix.put("'2009-05-14'", new Result("TWO-3", "TWO-2"));
        matrix.put("'14/May/09'", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));
        matrix.put("'2009/05/14 bad'", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));
        matrix.put("'2010/02/35'", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_RELATIVE_DATE_FORMAT));

        _testLiteralMatrix(projKey, fieldName, "<", matrix);
    }

    public void testDateTime() throws Exception
    {
        final String fieldName = "DT";
        final String projKey = "THREE";

        Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("1242604510000", new Result("THREE-4", "THREE-3", "THREE-2"));
        matrix.put("'1d'", new Result("THREE-4", "THREE-3", "THREE-2"));
        matrix.put("'dd'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/11 13:00'", new Result("THREE-3", "THREE-2"));
        matrix.put("'2009-05-11 13:00'", new Result("THREE-3", "THREE-2"));
        matrix.put("'2009/05/12'", new Result("THREE-4", "THREE-3", "THREE-2"));
        matrix.put("'2009-05-12'", new Result("THREE-4", "THREE-3", "THREE-2"));
        matrix.put("'11/May/09'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'09/1/1'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2009/05/14 bad'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("'2010/02/35'", new Result(ErrorType.INVALID_DATE_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_DATE_FORMAT));

        _testLiteralMatrix(projKey, fieldName, "<", matrix);

        matrix = new LinkedHashMap<String, Result>();
        matrix.put("'2009/05/11 12:00'", new Result("THREE-3"));
        matrix.put("'2009-05-11 12:00'", new Result("THREE-3"));
        // generalising to a date will not match a date time
        matrix.put("'2009/05/11'", new Result());
        matrix.put("'2009-05-11'", new Result());

        _testLiteralMatrix(projKey, fieldName, "=", matrix);
    }

    public void testFreeTextField() throws Exception
    {
        final String fieldName = "FTF";
        final String projKey = "FOUR";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Test'", new Result("FOUR-5"));
        matrix.put("'-Test'", new Result("FOUR-6"));
        matrix.put("'123456'", new Result("FOUR-6"));
        matrix.put("'?abc'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'*abc'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'abc ?xyz'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'abc *xyz'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("Test", new Result("FOUR-5"));
        matrix.put("123456", new Result("FOUR-6"));
        matrix.put("EMPTY", new Result("FOUR-4"));

        _testLiteralMatrix(projKey, fieldName, "~", matrix);
    }

    public void testGroupPicker() throws Exception
    {
        final String fieldName = "GP";
        final String projKey = "FIVE";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'jira-users'", new Result("FIVE-2"));
        matrix.put("'Bad String'", new Result(ErrorType.GROUP_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.GROUP_NOT_FOUND));
        matrix.put("EMPTY", new Result("FIVE-1"));
        matrix.put("123456", new Result("FIVE-3"));
        matrix.put("'123456'", new Result("FIVE-3"));
        matrix.put("666", new Result(ErrorType.GROUP_NOT_FOUND_WITH_QUOTES));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testImportId() throws Exception
    {
        final String fieldName = "II";
        final String projKey = "SIX";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'0'", new Result());
        matrix.put("'-2'", new Result());
        matrix.put("'4'", new Result("SIX-3"));
        matrix.put("'Bad String'", new Result(ErrorType.INVALID_NUMBER_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_NUMBER_FORMAT));
        matrix.put("EMPTY", new Result("SIX-1"));
        matrix.put("6", new Result("SIX-4"));
        matrix.put("666", new Result());

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testMultiCheckboxes() throws Exception
    {
        final String fieldName = "MC";
        final String projKey = "SEVEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'opt1'", new Result("SEVEN-4", "SEVEN-2"));
        matrix.put("'Bad String'", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("EMPTY", new Result("SEVEN-1"));
        matrix.put("2222", new Result("SEVEN-4", "SEVEN-3"));
        matrix.put("'2222'", new Result("SEVEN-4", "SEVEN-3"));
        matrix.put("666", new Result(ErrorType.OPTION_NOT_FOUND_WITH_QUOTES));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testMultiGroupPicker() throws Exception
    {
        final String fieldName = "MGP";
        final String projKey = "EIGHT";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'jira-users'", new Result("EIGHT-4", "EIGHT-2"));
        matrix.put("'Bad String'", new Result(ErrorType.GROUP_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.GROUP_NOT_FOUND));
        matrix.put("EMPTY", new Result("EIGHT-1"));
        matrix.put("123456", new Result("EIGHT-4", "EIGHT-3"));
        matrix.put("'123456'", new Result("EIGHT-4", "EIGHT-3"));
        matrix.put("666", new Result(ErrorType.GROUP_NOT_FOUND_WITH_QUOTES));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testMultiSelect() throws Exception
    {
        final String fieldName = "MS";
        final String projKey = "NINE";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'select1'", new Result("NINE-4", "NINE-2"));
        matrix.put("'Bad String'", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("EMPTY", new Result("NINE-1"));
        matrix.put("2222222", new Result("NINE-4", "NINE-3"));
        matrix.put("'2222222'", new Result("NINE-4", "NINE-3"));
        matrix.put("666", new Result(ErrorType.OPTION_NOT_FOUND_WITH_QUOTES));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testMultiUserPicker() throws Exception
    {
        final String fieldName = "MUP";
        final String projKey = "TEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'fred'", new Result("TEN-4", "TEN-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND, true));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND, true));
        matrix.put("EMPTY", new Result("TEN-1"));
        matrix.put("987654", new Result("TEN-4", "TEN-3"));
        matrix.put("'987654'", new Result("TEN-4", "TEN-3"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND, true));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testNumberField() throws Exception
    {
        final String fieldName = "NF";
        final String projKey = "ELEVEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'0'", new Result());
        matrix.put("'-2'", new Result());
        matrix.put("'20'", new Result("ELEVEN-3"));
        matrix.put("'Bad String'", new Result(ErrorType.INVALID_NUMBER_FORMAT));
        matrix.put("''", new Result(ErrorType.INVALID_NUMBER_FORMAT));
        matrix.put("EMPTY", new Result("ELEVEN-1"));
        matrix.put("30", new Result("ELEVEN-4"));
        matrix.put("666", new Result());

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testProjectPicker() throws Exception
    {
        final String fieldName = "PP";
        final String projKey = "TWELVE";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'10029'", new Result("TWELVE-2"));
        matrix.put("'10030'", new Result("TWELVE-2"));
        matrix.put("'SH'", new Result("TWELVE-3"));
        matrix.put("'SHORT'", new Result("TWELVE-4"));
        matrix.put("'NotAsShort'", new Result("TWELVE-4"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10029", new Result());
        matrix.put("10030", new Result("TWELVE-2"));
        matrix.put("987654", new Result("TWELVE-5"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result("TWELVE-1"));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testRadioButton() throws Exception
    {
        final String fieldName = "RB";
        final String projKey = "THIRTEEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'rad1'", new Result("THIRTEEN-2"));
        matrix.put("'Bad String'", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("EMPTY", new Result("THIRTEEN-1"));
        matrix.put("4444", new Result("THIRTEEN-3"));
        matrix.put("'4444'", new Result("THIRTEEN-3"));
        matrix.put("666", new Result(ErrorType.OPTION_NOT_FOUND_WITH_QUOTES));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testReadOnlyTextField() throws Exception
    {
        final String fieldName = "ROTF";
        final String projKey = "FOURTEEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Test'", new Result("FOURTEEN-2"));
        matrix.put("'-Test'", new Result("FOURTEEN-3"));
        matrix.put("'123456'", new Result("FOURTEEN-3"));
        matrix.put("'?abc'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'*abc'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'abc ?xyz'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'abc *xyz'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("Test", new Result("FOURTEEN-2"));
        matrix.put("123456", new Result("FOURTEEN-3"));
        matrix.put("EMPTY", new Result("FOURTEEN-1"));

        _testLiteralMatrix(projKey, fieldName, "~", matrix);
    }

    public void testSelectList() throws Exception
    {
        final String fieldName = "SL";
        final String projKey = "FIFTEEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'select1'", new Result("FIFTEEN-2"));
        matrix.put("'Bad String'", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.OPTION_NOT_FOUND));
        matrix.put("EMPTY", new Result("FIFTEEN-1"));
        matrix.put("5555555", new Result("FIFTEEN-3"));
        matrix.put("'5555555'", new Result("FIFTEEN-3"));
        matrix.put("666", new Result(ErrorType.OPTION_NOT_FOUND_WITH_QUOTES));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testSingleVersionPicker() throws Exception
    {
        final String fieldName = "SVP";
        final String projKey = "SIXTEEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'10010'", new Result("SIXTEEN-3"));
        matrix.put("'10011'", new Result("SIXTEEN-3"));
        matrix.put("'v1'", new Result("SIXTEEN-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10010", new Result("SIXTEEN-2"));
        matrix.put("10011", new Result("SIXTEEN-3"));
        matrix.put("224466", new Result("SIXTEEN-4"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result("SIXTEEN-1"));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testTextField() throws Exception
    {
        final String fieldName = "TF";
        final String projKey = "SEVENTEEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'Test'", new Result("SEVENTEEN-2"));
        matrix.put("'-Test'", new Result("SEVENTEEN-3"));
        matrix.put("'123456'", new Result("SEVENTEEN-3"));
        matrix.put("'?abc'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'*abc'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'abc ?xyz'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'abc *xyz'", new Result(ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK));
        matrix.put("'BAD +'", new Result(ErrorType.CANT_PARSE_QUERY));
        matrix.put("''", new Result(ErrorType.EMPTY_STRING_NOT_SUPPORTED));
        matrix.put("Test", new Result("SEVENTEEN-2"));
        matrix.put("123456", new Result("SEVENTEEN-3"));
        matrix.put("EMPTY", new Result("SEVENTEEN-1"));

        _testLiteralMatrix(projKey, fieldName, "~", matrix);
    }

    public void testUrlField() throws Exception
    {
        final String fieldName = "URL";
        final String projKey = "EIGHTEEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'http://www.atlassian.com'", new Result("EIGHTEEN-2"));
        matrix.put("'Bad String'", new Result());
        matrix.put("''", new Result());
        matrix.put("666", new Result());
        matrix.put("EMPTY", new Result("EIGHTEEN-1"));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testUserPicker() throws Exception
    {
        final String fieldName = "UP";
        final String projKey = "NINETEEN";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'fred'", new Result("NINETEEN-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND, true));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND, true));
        matrix.put("EMPTY", new Result("NINETEEN-1"));
        matrix.put("987654", new Result("NINETEEN-3"));
        matrix.put("'987654'", new Result("NINETEEN-3"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND, true));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    public void testVersionPicker() throws Exception
    {
        final String fieldName = "VP";
        final String projKey = "TWENTY";

        final Map<String, Result> matrix = new LinkedHashMap<String, Result>();
        matrix.put("'10013'", new Result("TWENTY-5", "TWENTY-3"));
        matrix.put("'10014'", new Result("TWENTY-5", "TWENTY-3"));
        matrix.put("'v1'", new Result("TWENTY-5", "TWENTY-2"));
        matrix.put("'Bad String'", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("''", new Result(ErrorType.NAME_NOT_FOUND));
        matrix.put("10013", new Result("TWENTY-5", "TWENTY-2"));
        matrix.put("10014", new Result("TWENTY-5", "TWENTY-3"));
        matrix.put("335577", new Result("TWENTY-5", "TWENTY-4"));
        matrix.put("666", new Result(ErrorType.ID_NOT_FOUND));
        matrix.put("EMPTY", new Result("TWENTY-1"));

        _testLiteralMatrix(projKey, fieldName, matrix);
    }

    private void _testLiteralMatrix(final String projKey, final String fieldName, final Map<String, Result> matrix)
    {
        _testLiteralMatrix(projKey, fieldName, "=", matrix);
    }

    private void _testLiteralMatrix(final String projKey, final String fieldName, final String operator, final Map<String, Result> matrix)
    {
        for (final Map.Entry<String, Result> entry : matrix.entrySet())
        {
            final String operand = entry.getKey();
            final String jqlQuery = String.format("project = %s AND %s %s %s", projKey, fieldName, operator, operand);
            final Result result = entry.getValue();

            System.out.println("QUERY: "  +jqlQuery);
            if (result.issueKeys == null && result.errorType != null)
            {
                final String errorMsg = result.errorType.formatError(fieldName, operand);
                if (result.isWarning) {
                    assertSearchWithWarning(jqlQuery, errorMsg);
                } else {
                    assertSearchWithError(jqlQuery, errorMsg);
                }
            }
            else
            {
                assertSearchWithResults(jqlQuery, result.issueKeys);
            }
        }
    }

    private static class Result
    {
        private String[] issueKeys = null;
        private ErrorType errorType = null;
        private Boolean isWarning;

        private Result(final String... issueKeys)
        {
            this.issueKeys = issueKeys;
            this.isWarning = false;
        }

        private Result(final ErrorType errorType)
        {
            this.errorType = errorType;
            this.isWarning = false;
        }

        private Result(final ErrorType errorType, final Boolean isWarning)
        {
            this.errorType = errorType;
            this.isWarning = isWarning;
        }
    }

    private static enum ErrorType
    {
        EMPTY_STRING_NOT_SUPPORTED()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The field '%s' does not support searching for an empty string.", fieldName);
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
        WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The text query %s for field '%s' is not valid: "
                                + "the '*' and '?' are not allowed as first character in wildcard query.", operand, fieldName);
                    }
                },
        INVALID_NUMBER_FORMAT()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("Value %s for the '%s' field is not a valid number.", operand, fieldName);
                    }
                },
        GROUP_NOT_FOUND()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The group %s for field '%s' does not exist.", operand, fieldName);
                    }
                },
        GROUP_NOT_FOUND_WITH_QUOTES()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The group '%s' for field '%s' does not exist.", operand, fieldName);
                    }
                },
        OPTION_NOT_FOUND()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The option %s for field '%s' does not exist.", operand, fieldName);
                    }
                },
        OPTION_NOT_FOUND_WITH_QUOTES()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The option '%s' for field '%s' does not exist.", operand, fieldName);
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
