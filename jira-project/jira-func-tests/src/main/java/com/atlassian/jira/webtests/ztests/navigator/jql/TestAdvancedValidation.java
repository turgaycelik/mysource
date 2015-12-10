package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.Lists;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestAdvancedValidation extends FuncTestCase
{
    private static final int MAX_ERRORS = 10;

    public void testFunctionsInListCustomFields() throws Exception
    {
        administration.restoreData("TestAllCustomFields.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        _testOptionFieldFunctionInList("cascadeselect");
        _testOptionFieldFunctionInList("multiselect");
        _testOptionFieldFunctionInList("multiselectlist");
        _testOptionFieldFunctionInList("multicheckboxes");
        _testOptionFieldFunctionInList("radiobuttons");
        _testOptionFieldFunctionInList("selectlist");

        _testNumberFieldFunctionInList("numberfield");
        _testNumberFieldFunctionInList("numberrange");
        _testFieldFunctionInList("projectpicker");

        _testGroupFieldFunctionInList("grouppicker");
        _testGroupFieldFunctionInList("multigrouppicker");

        _testDateFieldFunctionInList("datetime");
        _testRelativeDateFieldFunctionInList("datepicker");

        _testFieldFunctionInList("singleversionpicker");
        _testFieldFunctionInList("versionpicker");
        _testFieldFunctionInListWarning("usergrouppicker");
        _testFieldFunctionInListWarning("userpicker");
        _testFieldFunctionInListWarning("multiuserpicker");

        _testChangeHistory("status");

    }

    public void testFunctionsInListSystemFields() throws Exception
    {
        administration.restoreBlankInstance();
        _testFieldFunctionInList("project");
        _testFieldFunctionInList("status");
        _testFieldFunctionInList("resolution");
        _testFieldFunctionInList("priority");
        _testFieldFunctionInList("affectedVersion");
        _testFieldFunctionInList("fixVersion");
        _testFieldFunctionInList("component");
        _testFieldFunctionInList("type");
        _testRelativeDateFieldFunctionInList("duedate");
        _testDateFieldFunctionInList("created");
        _testDateFieldFunctionInList("updated");
        _testDateFieldFunctionInList("resolutiondate");
    }

    // enable when JRA-30668 is fixed
//    public void testMaximumValidationErrors() throws Exception
//    {
//        administration.restoreBlankInstance();
//        String jqlString = createInvalidJql(30);
//        System.out.println(jqlString);
//        issueTableAssertions.assertMaxErrors(jqlString, MAX_ERRORS);
//    }

    private void _testNumberFieldFunctionInList(String field)
    {
        issueTableAssertions.assertSearchWithErrors(field + " in (echo('a', 'b'), BLAH)",
                Lists.newArrayList("A value provided by the function 'echo' for the field '"+field+"' is not a valid number.",
                        "Value 'BLAH' for the '"+field+"' field is not a valid number."));
    }

    private void _testGroupFieldFunctionInList(String field)
    {
        issueTableAssertions.assertSearchWithErrors(field + " in (echo('a', 'b'), BLAH)",
                Lists.newArrayList("A group provided by the function 'echo' for the field '"+field+"' does not exist.",
                        "The group 'BLAH' for field '"+field+"' does not exist."));
    }

    private void _testFieldFunctionInList(String field)
    {
        issueTableAssertions.assertSearchWithErrors(field + " in (echo('a', 'b'), BLAH)",
                Lists.newArrayList("A value provided by the function 'echo' is invalid for the field '"+field+"'.",
                "The value 'BLAH' does not exist for the field '"+field+"'."));
    }

    private void _testFieldFunctionInListWarning(String field)
    {
        issueTableAssertions.assertSearchWithWarnings(field + " in (echo('a', 'b'), BLAH)",
                Lists.newArrayList("A value provided by the function 'echo' is invalid for the field '"+field+"'.",
                "The value 'BLAH' does not exist for the field '"+field+"'."));
    }

    private void _testDateFieldFunctionInList(String field)
    {
        issueTableAssertions.assertSearchWithErrors(field + " in (echo('a', 'b'), BLAH)",
                Lists.newArrayList("A date for the field '"+field+"' provided by the function 'echo' is not valid.",
                "Date value 'BLAH' for field '"+field+"' is invalid. Valid formats include: 'yyyy/MM/dd HH:mm', 'yyyy-MM-dd HH:mm', 'yyyy/MM/dd', 'yyyy-MM-dd', or a period format e.g. '-5d', '4w 2d'."));
    }

    private void _testRelativeDateFieldFunctionInList(String field)
    {
        issueTableAssertions.assertSearchWithErrors(field + " in (echo('a', 'b'), BLAH)",
                Lists.newArrayList("A date for the field '"+field+"' provided by the function 'echo' is not valid.",
                "Date value 'BLAH' for field '"+field+"' is invalid. Valid formats include: 'YYYY/MM/DD', 'YYYY-MM-DD', or a period format e.g. '-5d', '4w 2d'."));
    }

    private void _testOptionFieldFunctionInList(String field)
    {
        issueTableAssertions.assertSearchWithErrors(field + " in (echo('a', 'b'), BLAH)",
                Lists.newArrayList("An option provided by the function 'echo' for the field '"+field+"' does not exist.",
                "The option 'BLAH' for field '"+field+"' does not exist."));
    }

    private void _testChangeHistory(String field)
    {
        issueTableAssertions.assertSearchWithError(field + " was nonexistent", "The value 'nonexistent' does not exist for the field '"+field+"'.");
    }


    private String createInvalidJql(final int errors)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < errors; i++)
        {
            builder.append("project = nonexistent");
            builder.append(i);
            if (i != errors-1)
            {
                builder.append(" or ");
            }
        }
        String jqlString = builder.toString();
        return jqlString;
    }
}
