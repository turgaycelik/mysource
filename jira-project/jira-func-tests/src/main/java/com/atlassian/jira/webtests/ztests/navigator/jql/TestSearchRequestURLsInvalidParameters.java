package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import com.google.common.collect.ImmutableList;
import com.meterware.httpunit.HttpUnitOptions;

import java.util.List;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestSearchRequestURLsInvalidParameters extends FuncTestCase
{
    private static final String QUERY = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?tempMax=1000&";

    public void setUpTest()
    {
        administration.restoreData("TestSearchRequestURLsInvalidParameters.xml");
    }

    @Override
    protected void setUpHttpUnitOptions()
    {
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
        super.setUpHttpUnitOptions();
    }

    public void testEverySingleField()
    {
        // system fields
        assertErrorResponse(affectedVersion());
        assertErrorResponse(text());
        assertErrorResponse(component());
        assertErrorResponse(created());
        assertErrorResponse(dueDate());
        assertErrorResponse(updated());
        assertErrorResponse(resolutionDate());
        assertErrorResponse(fixVersion());
        assertErrorResponse(priority());
        assertErrorResponse(project());
        assertErrorResponse(resolution());
        assertErrorResponse(status());
        assertErrorResponse(type());
        assertErrorResponse(workRatio());

        // custom fields
        assertErrorResponse(datePicker());
        assertErrorResponse(dateTime());
        assertErrorResponse(freeTextField());
        assertErrorResponse(readOnlyTextField());
        assertErrorResponse(textField());
        assertErrorResponse(groupPicker());
        assertErrorResponse(importIdExactSearcher());
        assertErrorResponse(importIdRangeSearcher());
        assertErrorResponse(cascadingSelect());
        assertErrorResponse(numberFieldExactSearcher());
        assertErrorResponse(numberFieldRangeSearcher());
        assertErrorResponse(singleVersionPicker());
        assertErrorResponse(versionPicker());
        assertErrorResponse(projectPicker());
        assertErrorResponse(multiGroupPicker());
        assertErrorResponse(multiCheckboxes());
        assertErrorResponse(multiSelect());
        assertErrorResponse(selectList());
        assertErrorResponse(radioButton());
    }

    public void testEverySingleWarningField()
    {
        // system fields
        assertWarningResponse(assignee());
        assertWarningResponse(reporter());

        // custom fields
        assertWarningResponse(userPicker());
        assertWarningResponse(multiUserPicker());
    }

    private List<Scenario> affectedVersion()
    {
        return ImmutableList.of(
                new Scenario("version=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("affectedVersion", "yes")),
                new Scenario("version=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("affectedVersion", "50000")),
                new Scenario("version=10011&version=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("affectedVersion", "50000"))
        );
    }

    private List<Scenario> assignee()
    {
        return buildUserScenarios("assignee", "assignee");
    }

    private List<Scenario> text()
    {
        return buildSystemTextScenario("text");
    }

    private List<Scenario> component()
    {
        return ImmutableList.of(
                new Scenario("component=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("component", "yes")),
                new Scenario("component=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("component", "50000")),
                new Scenario("component=component&component=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("component", "50000"))
        );
    }

    private List<Scenario> created()
    {
        return buildDateScenarios("created");
    }

    private List<Scenario> dueDate()
    {
        return buildRelativeDateScenarios("duedate", "due");
    }

    private List<Scenario> updated()
    {
        return buildDateScenarios("updated");
    }

    private List<Scenario> resolutionDate()
    {
        return buildDateScenarios("resolutiondate", "resolved");
    }

    private List<Scenario> fixVersion()
    {
        return ImmutableList.of(
                new Scenario("fixfor=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("fixVersion", "yes")),
                new Scenario("fixfor=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("fixVersion", "50000")),
                new Scenario("fixfor=10011&fixfor=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("fixVersion", "50000"))
        );
    }

    private List<Scenario> priority()
    {
        return buildIdScenarios("priority", "priority", "1");
    }

    private List<Scenario> project()
    {
        return buildIdScenarios("pid", "project", "10010");
    }

    private List<Scenario> resolution()
    {
        return buildIdScenarios("resolution", "resolution", "1");
    }

    private List<Scenario> status()
    {
        return buildIdScenarios("status", "status", "1");
    }

    private List<Scenario> type()
    {
        return buildIdScenarios("type", "issuetype", "1");
    }

    private List<Scenario> reporter()
    {
        return buildUserScenarios("reporter", "reporter");
    }

    private List<Scenario> workRatio()
    {
        final String field = "workratio";

        return ImmutableList.of(
                new Scenario(field + ":min=20&" + field + ":max=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate")),
                new Scenario(field + ":min=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate")),
                new Scenario(field + ":max=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate")),
                new Scenario(field + ":max=20&" + field + ":min=baddate", ErrorType.REQUIRES_INTEGER.formatError(field, "baddate"))
        );
    }

    private List<Scenario> datePicker()
    {
        return buildRelativeDateScenarios("customfield_10001", "DP");
    }

    private List<Scenario> dateTime()
    {
        final String urlParam = "customfield_10002";
        final String field = "DT";

        return ImmutableList.of(
                new Scenario(urlParam + ":before=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":before=11/May/08+11:34+AM&" + urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=1h&" + urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH"))
        );
    }

    private List<Scenario> freeTextField()
    {
        return buildTextScenario("customfield_10003", "FTF");
    }

    private List<Scenario> readOnlyTextField()
    {
        return buildTextScenario("customfield_10013", "ROTF");
    }

    private List<Scenario> textField()
    {
        return buildTextScenario("customfield_10016", "TF");
    }

    private List<Scenario> groupPicker()
    {
        final String urlParam = "customfield_10004";
        final String field = "GP";
        return ImmutableList.of(
                new Scenario(urlParam + "=1", ErrorType.GROUP_DOESNT_EXIST.formatError(field, "1"))
        );
    }

    private List<Scenario> importIdExactSearcher()
    {
        final String urlParam = "customfield_10005";
        final String field = "II exact";

        return buildExactNumberScenarios(urlParam, field);
    }

    private List<Scenario> importIdRangeSearcher()
    {
        final String urlParam = "customfield_10020";
        final String field = "II range";

        return buildRangeNumberScenarios(urlParam, field);
    }

    private List<Scenario> cascadingSelect()
    {
        final String urlParam = "customfield_10000";
        final String field = "CSF";

        return ImmutableList.of(
                new Scenario(urlParam + "=10010&" + urlParam + ":1=invalid", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "invalid")),
                new Scenario(urlParam + "=yes", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlParam + "=20", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "20")),
                new Scenario(urlParam + ":1=yes", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlParam + ":1=20", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "20"))
        );
    }

    private List<Scenario> numberFieldExactSearcher()
    {
        return buildExactNumberScenarios("customfield_10010", "NF exact");
    }

    private List<Scenario> numberFieldRangeSearcher()
    {
        return buildRangeNumberScenarios("customfield_10021", "NF range");
    }

    private List<Scenario> userPicker()
    {
        return buildUserScenarios("customfield_10018", "UP");
    }

    private List<Scenario> singleVersionPicker()
    {
        return ImmutableList.of(
                new Scenario("customfield_10015=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("SVP", "yes")),
                new Scenario("customfield_10015=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("SVP", "50000")),
                new Scenario("customfield_10015=10011&customfield_10015=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("SVP", "50000"))
        );
    }

    private List<Scenario> versionPicker()
    {
        return ImmutableList.of(
                new Scenario("customfield_10019=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("VP", "yes")),
                new Scenario("customfield_10019=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("VP", "50000")),
                new Scenario("customfield_10019=10011&customfield_10019=50000", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError("VP", "50000"))
        );
    }

    private List<Scenario> projectPicker()
    {
        final String urlName = "customfield_10011";
        final String field = "PP";
        return ImmutableList.of(
                new Scenario(urlName + "=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlName + "=50000", ErrorType.NUMBER_VALUE_DOESNT_EXIST.formatError(field, "50000"))
        );
    }

    private List<Scenario> multiGroupPicker()
    {
        final String urlParam = "customfield_10007";
        final String field = "MGP";
        return ImmutableList.of(
                new Scenario(urlParam + "=1", ErrorType.GROUP_DOESNT_EXIST.formatError(field, "1"))
        );
    }

    private List<Scenario> multiUserPicker()
    {
        return buildUserScenarios("customfield_10009", "MUP");
    }

    private List<Scenario> multiCheckboxes()
    {
        final String urlParam = "customfield_10006";
        final String field = "MC";
        return ImmutableList.of(
                new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1")),
                new Scenario(urlParam + "=opt1&" + urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1"))
        );
    }

    private List<Scenario> multiSelect()
    {
        final String urlParam = "customfield_10008";
        final String field = "MS";
        return ImmutableList.of(
                new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1")),
                new Scenario(urlParam + "=select1&" + urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1"))
        );
    }

    private List<Scenario> selectList()
    {
        final String urlParam = "customfield_10014";
        final String field = "SL";
        return ImmutableList.of(new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1")));
    }

    private List<Scenario> radioButton()
    {
        final String urlParam = "customfield_10012";
        final String field = "RB";
        return ImmutableList.of(new Scenario(urlParam + "=1", ErrorType.OPTION_DOESNT_EXIST.formatError(field, "1")));
    }

    private List<Scenario> buildUserScenarios(final String urlParam, final String field)
    {
        return ImmutableList.of(new Scenario(urlParam + "=1", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError(field, "1")));
    }

    private List<Scenario> buildSystemTextScenario(final String field)
    {
        return buildTextScenario("text", field);
    }

    private List<Scenario> buildTextScenario(final String urlParam, final String field)
    {
        return ImmutableList.of(
                new Scenario(urlParam + "=%3Fbadtextquery", ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK.formatError(field, "?badtextquery")),
                new Scenario(urlParam + "=*badtextquery", ErrorType.WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK.formatError(field, "*badtextquery"))
        );
    }

    private List<Scenario> buildDateScenarios(final String field)
    {
        return buildDateScenarios(field, field);
    }

    private List<Scenario> buildRelativeDateScenarios(final String urlParam, final String field)
    {
        return ImmutableList.of(
                new Scenario(urlParam + ":before=baddate", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":after=baddate", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":next=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":equals=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":before=11/May/08&" + urlParam + ":after=baddate", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=1h&" + urlParam + ":next=HHH", ErrorType.INVALID_RELATIVE_DATE.formatError(field, "HHH"))
        );
    }

    private List<Scenario> buildDateScenarios(final String urlParam, final String field)
    {
        return ImmutableList.of(
                new Scenario(urlParam + ":before=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH")),
                new Scenario(urlParam + ":before=11/May/08&" + urlParam + ":after=baddate", ErrorType.INVALID_DATE.formatError(field, "baddate")),
                new Scenario(urlParam + ":previous=1h&" + urlParam + ":next=HHH", ErrorType.INVALID_DATE.formatError(field, "HHH"))
        );
    }

    private List<Scenario> buildIdScenarios(final String urlName, final String field, final String validId)
    {
        return ImmutableList.of(
                new Scenario(urlName + "=yes", ErrorType.STRING_VALUE_DOESNT_EXIST.formatError(field, "yes")),
                new Scenario(urlName + "=50000", ErrorType.NUMBER_VALUE_DOESNT_EXIST.formatError(field, "50000")),
                new Scenario(urlName + "=" + validId + "&" + urlName + "=50000", ErrorType.NUMBER_VALUE_DOESNT_EXIST.formatError(field, "50000"))
        );
    }

    private List<Scenario> buildExactNumberScenarios(final String urlParam, final String field)
    {
        return ImmutableList.of(new Scenario(urlParam + "=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber")));
    }

    private List<Scenario> buildRangeNumberScenarios(final String urlParam, final String field)
    {
        return ImmutableList.of(
                new Scenario(urlParam + ":greaterThan=20&" + urlParam + ":lessThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber")),
                new Scenario(urlParam + ":greaterThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber")),
                new Scenario(urlParam + ":lessThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber")),
                new Scenario(urlParam + ":lessThan=20&" + urlParam + ":greaterThan=badnumber", ErrorType.REQUIRES_NUMBER.formatError(field, "badnumber"))
        );
    }

    private void assertErrorResponse(final List<Scenario> scenarios)
    {
        for (final Scenario scenario : scenarios)
        {
            log(QUERY + scenario.urlParams);
            tester.gotoPage(QUERY + scenario.urlParams);
            assertEquals(400, tester.getDialog().getResponse().getResponseCode());
            assertions.html().assertResponseContains(tester, scenario.msg);
        }
    }

    private void assertWarningResponse(final List<Scenario> scenarios)
    {
        for (final Scenario scenario : scenarios)
        {
            log(QUERY + scenario.urlParams);
            tester.gotoPage(QUERY + scenario.urlParams);
            assertEquals(200, tester.getDialog().getResponse().getResponseCode());
        }
    }

    private static class Scenario
    {
        private final String urlParams;
        private final String msg;

        private Scenario(final String urlParams, final String msg)
        {
            this.urlParams = urlParams;
            this.msg = msg;
        }
    }

    private static enum ErrorType
    {
        TEXT_START_CHAR()
                {
                    // ignore the actual start char for brevity
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The text query '%s' for field '%s' is not allowed to start with ", value, fieldName);
                    }
                },
        WILDCARD_CANNOT_START_WITH_STAR_OR_QUESTIONMARK()
                {
                    String formatError(final String fieldName, final String operand)
                    {
                        return String.format("The text query '%s' for field '%s' is not valid: "
                                + "the '*' and '?' are not allowed as first character in wildcard query.", operand, fieldName);
                    }
                },
        INVALID_DATE()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("Date value '%s' for field '%s' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'.", value, fieldName);
                    }
                },
        INVALID_RELATIVE_DATE()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("Date value '%s' for field '%s' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'.", value, fieldName);
                    }
                },
        GROUP_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The group '%s' for field '%s' does not exist.", value, fieldName);
                    }
                },
        STRING_VALUE_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The value '%s' does not exist for the field '%s'.", value, fieldName);
                    }
                },
        NUMBER_VALUE_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("A value with ID '%s' does not exist for the field '%s'.", value, fieldName);
                    }
                },
        REQUIRES_INTEGER()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The value '%s' for field '%s' is invalid - please specify an integer.", value, fieldName);
                    }
                },
        REQUIRES_NUMBER()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("Value '%s' for the '%s' field is not a valid number.", value, fieldName);
                    }
                },
        OPTION_DOESNT_EXIST()
                {
                    String formatError(final String fieldName, final String value)
                    {
                        return String.format("The option '%s' for field '%s' does not exist.", value, fieldName);
                    }};

        abstract String formatError(String fieldName, String value);
    }
}
