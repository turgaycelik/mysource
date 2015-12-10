package com.atlassian.jira.webtest.webdriver.tests.calendar;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.CalendarPicker;
import com.atlassian.jira.pageobjects.components.CalendarPopup;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.dialogs.LogWorkDialog;
import com.atlassian.jira.pageobjects.dialogs.quickedit.WorkflowTransitionDialog;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.model.WorkflowIssueAction;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import org.junit.After;
import org.junit.Test;

import javax.inject.Inject;

import static org.hamcrest.Matchers.startsWith;

/**
 * Webdriver test for the Calendar Popup
 *
 * @since v5.2
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.CUSTOM_FIELDS })
public class TestCalendarPopUp extends BaseJiraWebTest
{
    @Inject private PageElementFinder pageElementFinder;

    private static final String ISSUE = "HSP-1";
    private static final WorkflowIssueAction TEST_TRANSITION = new WorkflowIssueAction(711L, "Test");

    private FormDialog openDialog;

    @After
    public void closeDialog()
    {
        if (openDialog != null)
        {
            openDialog.close();
        }

    }

    private <T> T getOpenDialog(Class<T> dialogClass)
    {
        return dialogClass.cast(openDialog);
    }

    /**
     * JRADEV-2725, JRADEV-2747: Make sure the calendar works in dialogs on the view issue.
     *
     */
    @Test
    @Restore("xml/TestCalendarInDialog.xml")
    public void testDateTimePickerInLogWorkDialogFromViewIssue() throws Exception
    {
        final ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, ISSUE);

        openDialog = viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LOG_WORK, LogWorkDialog.class);
        final LogWorkDialog logWorkDialog = getOpenDialog(LogWorkDialog.class);
        Poller.waitUntilTrue("Log Work Dialog did not open successfully", logWorkDialog.isOpen());
        testCalendarPopupSelectsDayByClick(logWorkDialog.getDateStarted());
    }

    @Test
    @Restore("xml/TestCalendarInDialog.xml")
    public void testDateTimePickerInCloseIssueDialogFromViewIssue() throws Exception
    {
        final ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, ISSUE);

        openDialog = viewIssuePage.getIssueMenu().invokeWorkflowAction(WorkflowIssueAction.CLOSE_ISSUE);
        final WorkflowTransitionDialog closeIssue = getOpenDialog(WorkflowTransitionDialog.class);
        testCalendarPopupSelectsDayByClick(closeIssue.getCustomField(CalendarPicker.class, 10000));
        testCalendarPopupSelectsDayByClick(closeIssue.getCustomField(CalendarPicker.class, 10001));
    }


    @Test
    @Restore("xml/TestCalendarInDialog.xml")
    public void testDateTimePickerInTestDialogFromViewIssue() throws Exception
    {
        final ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, ISSUE);

        openDialog = viewIssuePage.getIssueMenu().invokeWorkflowAction(TEST_TRANSITION);
        final WorkflowTransitionDialog testIssue = getOpenDialog(WorkflowTransitionDialog.class);
        testCalendarPopupSelectsDayByClick(testIssue.getCustomField(CalendarPicker.class, 10000));
        testCalendarPopupSelectsDayByClick(testIssue.getCustomField(CalendarPicker.class, 10001));
    }

    private void testCalendarPopupSelectsDayByClick(CalendarPicker calendarPicker)
    {
        final CalendarPopup calendarPopup = calendarPicker.openCalendarPopup();
        Poller.waitUntilTrue("Calender Popup did not open successfully", calendarPopup.isOpen());

        final int expectedDay = selectOtherDay(calendarPopup);
        Poller.waitUntilTrue(calendarPopup.isClosed());
        Poller.waitUntil("Calendar date was not set on the input field.", calendarPicker.getDateValue(), startsWith(Integer.toString(expectedDay)));
    }

    private int selectOtherDay(CalendarPopup popup)
    {
        final int selectedDay = popup.getSelectedDay().now();
        for (int day=1; day<=31; day++)
        {
            if (popup.hasDay(day) && day != selectedDay)
            {
                popup.selectDay(day);
                return day;
            }
        }
        throw new AssertionError("Ooops, this test is not smart enough");
    }
}
