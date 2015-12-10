package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestDateInputValidationOnCreateIssue extends FuncTestCase
{
    private static final String ISSUE_SUMMARY_FORM_ELEMENT = "summary";
    private static final String ISSUE_DUE_DATE_FORM_ELEMENT = "duedate";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testDueDateInputValidation()
    {
        _testDueDateValidationWith("00/ABC/07");
        _testDueDateValidationWith("A/06/07");
        _testDueDateValidationWith("30/00/07");
        _testDueDateValidationWith("XX/XX/XX");
        _testDueDateValidationWith("0/00/07");
        _testDueDateValidationWith("-15/JUL/07");
        _testDueDateValidationWith("50/JAN/07");
        _testDueDateValidationWith("163/06/07");
        _testDueDateValidationWith("50/JAN/06");
        _testDueDateValidationWith("100/FEB/05");
        _testDueDateValidationWith("32/DEC/06");
    }

    public void _testDueDateValidationWith(String dueDate)
    {
        navigation.issue().goToCreateIssueForm(null,null);
        tester.setFormElement(ISSUE_SUMMARY_FORM_ELEMENT, "Testing Date");
        tester.setFormElement(ISSUE_DUE_DATE_FORM_ELEMENT, dueDate);
        tester.submit("Create");

        text.assertTextPresent(locator.page(), "You did not enter a valid date");
    }
}

