package com.atlassian.jira.webtests.ztests.timezone;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.TIME_ZONES;

/**
 * @since v4.4
 */
@WebTest ( { FUNC_TEST, TIME_ZONES })
public class TestIssueChangeHistory extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueChangeHistory.xml");
        administration.generalConfiguration().setDefaultUserTimeZone("Australia/Sydney");
    }

    public void testChangeDueDate() throws Exception
    {
        navigation.login("admin");
        navigation.issue().viewIssue("MKY-2");

        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("duedate", "12/May/09");
        tester.submit("Update");
        tester.clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);
        // Now we are on the View Issue Page again - lets check the Change history.
        WebTable changehistory_10300 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10300");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10300.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10300, new String[] { "Due Date", "", "12/May/09 EST" });

        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("duedate", "11/May/11");
        tester.submit("Update");

        WebTable changehistory_10301 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10301");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10301.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10301, new String[] { "Due Date", "12/May/09 EST", "11/May/11 EST" });

        //Now let's use a user in a different time zone and ensure the due date value hasn't changed.
        navigation.login("berlin");
        navigation.issue().viewIssue("MKY-2");
        changehistory_10300 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10300");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10300.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10300, new String[] { "F\u00E4lligkeitsdatum", "", "12/Mai/09 EST" });

        changehistory_10301 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10301");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10301.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10301, new String[] { "F\u00E4lligkeitsdatum", "12/Mai/09 EST", "11/Mai/11 EST" });
    }

    public void testDateCF() throws Exception
    {
        navigation.login("admin");
        navigation.issue().viewIssue("MKY-2");
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("customfield_10000", "03/May/11");
        tester.submit("Update");
        tester.clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);

        // Now we are on the View Issue Page again - lets check the Change history.
        WebTable changehistory_10300 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10300");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10300.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10300, new String[] { "DatePickerCF", "", "3/May/11 EST" });

        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("customfield_10000", "01/Jan/09");
        tester.submit("Update");

        WebTable changehistory_10301 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10301");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10301.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10301, new String[] { "DatePickerCF", "3/May/11 EST", "1/Jan/09 EST" });

        //Now let's use a user in a different time zone and ensure the due date value hasn't changed.
        navigation.login("berlin");
        navigation.issue().viewIssue("MKY-2");
        changehistory_10300 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10300");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10300.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10300, new String[] { "DatePickerCF", "", "3/Mai/11 EST" });

        changehistory_10301 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10301");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10301.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10301, new String[] { "DatePickerCF", "3/Mai/11 EST", "1/Jan/09 EST" });
    }

    public void testDateTimeCF() throws Exception
    {
        navigation.login("admin");
        navigation.issue().viewIssue("MKY-2");
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("customfield_10001", "3/May/11 08:40 AM");
        tester.submit("Update");
        tester.clickLinkWithText(ISSUE_TAB_CHANGE_HISTORY);

        // Now we are on the View Issue Page again - lets check the Change history.
        WebTable changehistory_10300 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10300");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10300.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10300, new String[] { "DateTimeCF", "", "03/May/11 8:40 AM" });

        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        tester.setFormElement("customfield_10001", "3/May/10 03:40 AM");
        tester.submit("Update");

        WebTable changehistory_10301 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10301");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10301.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10301, new String[] { "DateTimeCF", "03/May/11 8:40 AM", "03/May/10 3:40 AM" });

        //Now let's use a user in a different time zone and ensure the due date value hasn't changed.
        navigation.login("berlin");
        navigation.issue().viewIssue("MKY-2");
        changehistory_10300 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10300");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10300.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10300, new String[] { "DateTimeCF", "", "03/Mai/11 12:40 AM" });

        changehistory_10301 = tester.getDialog().getWebTableBySummaryOrId("changehistory_10301");
        assertEquals("Expected table to have 2 rows, but found " + changehistory_10301.getRowCount(), 2, changehistory_10300.getRowCount());
        assertions.getTableAssertions().assertTableContainsRowOnce(changehistory_10301, new String[] { "DateTimeCF", "03/Mai/11 12:40 AM ", "02/Mai/10 7:40 PM " });
    }


}
