package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.ISSUES;
import static com.atlassian.jira.functest.framework.suite.Category.TIME_ZONES;
import static com.atlassian.jira.permission.ProjectPermissions.DELETE_ALL_WORKLOGS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

@WebTest ({ FUNC_TEST, ISSUES })
public class TestViewIssue extends JIRAWebTest
{
    private static final String CLASSNAME_SUBTASK_PERCENTAGE_CELL = "progress";

    public TestViewIssue(String name)
    {
        super(name);
    }

    public void testSpeed()
    {
        administration.restoreBlankInstance();
    }

    /**
     * Ensure that non-existant assignee and reporter users dont break the view issue page - JRA-12360
     */
    public void testViewIssuePageWithInvalidAssigneeAndReporters()
    {
        //import pro/ent data (with subtasks)
        restoreData("TestViewIssueWithInvalidUsersProEnt.xml");
        administration.attachments().enable(); //set the attachment paths to be valid
        _testViewIssuePageWithInvalidAssigneeAndReportersStandard();
        _testViewIssuePageWithInvalidAssigneeAndReportersProEnt();
    }

    /**
     * check that if the date formats end with a double quot and a space that the UI does not break and the quotes are
     * html encoded. - JRA-13104
     */
    public void testEditedCommentVisibleWithDoubleQuotesInDateFormat()
    {
        restoreData("TestEditedCommentAndWorklogWithMalformedDateFormat.xml");

        gotoIssueTabPanel("HSP-1", ISSUE_TAB_COMMENTS);
        assertTextPresentBeforeText("this comment is edited", "this comment will not be edited");
        assertTextPresent("title=\"" + ADMIN_FULLNAME + " - 24/Jul/07 09:47 AM&quot; \"");
        // make sure stuff is encoded
        assertThat(locator.css(".commentdate_10012_concise span time").getText(), containsString("25/Jul/07 09:48 AM\""));
        assertThat(locator.css(".commentdate_10012_verbose span time").getText(), containsString("25/Jul/07 09:48 AM\""));

        gotoIssueTabPanel("HSP-1", ISSUE_TAB_WORK_LOG);
        assertTextPresentBeforeText("this work log is edited", "this work log will not be edited");
        assertTextPresent("title=\"" + ADMIN_FULLNAME + " - 18/Jul/07 09:42 AM&quot; \"");
        assertThat(locator.css("#worklog-10001 .actionContainer .action-details .date").getText(), containsString("18/Jul/07 09:43 AM\""));
    }

    private void _testViewIssuePageWithInvalidAssigneeAndReportersStandard()
    {
        gotoIssue("HSP-1");
        assertTextPresent("Issue with valid user"); //summary
        //check that the assignee and reporter are shown
        Locator locator = new IdLocator(tester, "assignee-val");
        text.assertTextPresent(locator, ADMIN_FULLNAME);
        locator = new IdLocator(tester, "reporter-val");
        text.assertTextPresent(locator, ADMIN_FULLNAME);

        //check that the custom user pickers are displayed correctly

        text.assertTextPresent(new XPathLocator(tester, "//span[@id='customfield_10000-val']"), ADMIN_FULLNAME);

        text.assertTextPresent(new XPathLocator(tester, "//span[@id='customfield_10001-val']"), ADMIN_FULLNAME);

        //check that the workflow actions are available
        assertLinkPresentWithText("Stop Progress");
        assertLinkPresentWithText("Resolve Issue");
        assertLinkPresentWithText("Close Issue");

        //check that the operations are available
        assertLinkPresentWithText("Assign");
        assertLinkPresentWithText("Attach file");
        assertLinkPresentWithText("Clone");
        assertLinkPresentWithText("Comment");
        assertLinkPresentWithText("Delete");
        assertLinkPresentWithText("Edit");
        assertLinkPresent("view-voters");
        assertLinkPresent("manage-watchers");
        assertLinkPresentWithText("Log work");

        gotoIssue("HSP-2");
        assertTextPresent("Issue with invalid users"); //summary
        //check that the assignee and reporter are shown
        text.assertTextPresent(new IdLocator(tester, "assignee-val"), "deletedassignee");
        text.assertTextPresent(new IdLocator(tester, "reporter-val"), "deletedreporter");

        //check that the custom user pickers are displayed correctly
        assertElementPresent("customfield_10000-val");
        assertElementPresent("customfield_10001-val");
        assertions.assertNodeDoesNotExist("//dd[@id='customfield_10001-val']//a");

        //check that the workflow actions are available
        assertLinkPresentWithText("Stop Progress");
        assertLinkPresentWithText("Resolve Issue");
        assertLinkPresentWithText("Close Issue");

        //check that the operations are available
        assertLinkPresentWithText("Assign");
        assertLinkPresentWithText("Attach file");
        assertLinkPresentWithText("Clone");
        assertLinkPresentWithText("Comment");
        assertLinkPresentWithText("Delete");
        assertLinkPresentWithText("Edit");
        assertLinkPresent("view-voters");
        assertLinkPresent("manage-watchers");
        assertLinkPresentWithText("Log work");
    }

    /**
     * Additional asserts to {@link #_testViewIssuePageWithInvalidAssigneeAndReportersStandard()} for professional and
     * enterprise editions
     */
    private void _testViewIssuePageWithInvalidAssigneeAndReportersProEnt()
    {
        gotoIssue("HSP-2");

        //check additional operations are available
        assertLinkPresentWithText("sub-task");

        //check link to the subtask is available
        assertLinkPresentWithText("Sub task with invalid user");


        gotoIssue("HSP-3");
        assertTextPresent("Sub task with invalid user"); //summary
        //check that the assignee and reporter are shown
        text.assertTextPresent(new IdLocator(tester, "assignee-val"), "deletedsubtaskuser");
        text.assertTextPresent(new IdLocator(tester, "reporter-val"), "deletedsubtaskuser");

        //check that the custom user pickers are displayed correctly
        assertElementPresent("customfield_10000-val");
        assertElementPresent("customfield_10001-val");
        assertions.assertNodeDoesNotExist("//dd[@id='customfield_10001-val']//a");


        //check that the workflow actions are available
        assertLinkNotPresentWithText("Stop Progress");
        assertLinkPresentWithText("Resolve Issue");
        assertLinkPresentWithText("Close Issue");

        //check that the operations are available
        assertLinkPresentWithText("Assign");
        assertLinkPresentWithText("Attach file");
        assertLinkPresentWithText("Clone");
        assertLinkPresentWithText("Comment");
        assertLinkPresentWithText("Delete");
        assertLinkPresentWithText("Edit");
        assertLinkPresent("view-voters");
        assertLinkPresent("manage-watchers");
        assertLinkPresentWithText("Log work");
    }

    /**
     * Tests the correct conditions under which the subtask progress percentage graph is present. The graph should be
     * present if *any* subtask has any timetracking data - either original estimate or work logged.
     */
    public void testSubtaskPercentageGraphPresence()
    {
        restoreBlankInstance();
        activateSubTasks();
        activateTimeTracking();
        String parentKey = addIssue(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, "bonobo");
        String sub1Key = addSubTaskToIssue(parentKey, ISSUE_TYPE_SUB_TASK, "sub1", "subdesc1");
        gotoIssue(parentKey);
        assertFalse(isSubtaskPercentageGraphPresent());
        // add a second subtask to check we see percentage bars even though not all subtasks get issue tracking
        addSubTaskToIssue(parentKey, ISSUE_TYPE_SUB_TASK, "sub2", "subdesc2");
        gotoIssue(parentKey);
        assertFalse(isSubtaskPercentageGraphPresent());
        // now change the original estimate of a subtask - it should make the subtask progress graph visible
        gotoIssue(sub1Key);
        setEstimate("1m");
        gotoIssue(parentKey);
        assertTrue(isSubtaskPercentageGraphPresent());
        // log some work so complete subtask timetracking is present (orig, remaining and time spent)
        logWork(sub1Key, "1m", "1m");
        clickLink("parent_issue_summary");
        assertTrue(isSubtaskPercentageGraphPresent());
        assertTextPresent("50%");

        // remove the logged work on the subtask and confirm the graph remains
        enableDeleteAllWorklogInDefaultPermissionScheme("jira-administrators");
        gotoIssue(sub1Key);
        // now delete the worklog
        if (getDialog().isLinkPresentWithText("Work Log"))
        {
            // get to the worklog issue tab panel
            clickLinkWithText("Work Log");
        }
        clickLink("delete_worklog_10000");
        submit("Delete");
        gotoIssue(parentKey);
        assertTrue("Expected to still see graph after worklog deletion, original estimate remains",
                isSubtaskPercentageGraphPresent());
    }

    /**
     * JRA-14794: if user does not have permission to browse a project, the project CF value should not be linked
     */
    public void testProjectCFNotLinkedWithNoPermission()
    {
        // data contains:
        // * 2 projects HSP, MKY. HSP has default permissions, MKY is only browsable by admin user
        // * project CF configured for all projects
        // * 2 issues: one which has project CF set to MKY, one with it set to HSP
        // since 'fred' cannot see MKY, the project CF value should not be linked
        restoreData("TestProjectCFWithNoPermission.xml");
        login(FRED_USERNAME);

        // project CF is visible in HSP-2
        gotoIssue("HSP-2");
        assertLinkWithTextExists("project CF is visible in HSP-2", "homosapien");

        // project CF is not visible in HSP-1
        gotoIssue("HSP-1");
        assertLinkWithTextNotPresent("project CF is not visible in HSP-1", "monkey");
        assertTextPresent("monkey");

        // project CF is visible in HSP-1 to admin
        login(ADMIN_USERNAME);
        gotoIssue("HSP-1");
        assertLinkWithTextExists("project CF is visible in HSP-1 to admin", "monkey");
    }

    /**
     * JRA-15011 - ensure ordering of Components is fixed
     */
    public void testComponentOrdering()
    {
        restoreBlankInstance();

        log("Testing ordering for components field");
        navigation.issue().goToCreateIssueForm(null, null);
        tester.setFormElement("summary", "Test issue");

        // issue initially has Componet 1 & 3 selected
        selectMultiOption("components", "New Component 1");
        selectMultiOption("components", "New Component 3");
        tester.submit("Create");
        assertTextSequence(new String[] { "Component/s", "New Component 1", "New Component 3" });

        // change components to 2 & 3
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        selectMultiOption("components", "New Component 2");
        selectMultiOption("components", "New Component 3");
        tester.submit("Update");
        assertTextSequence(new String[] { "Component/s", "New Component 2", "New Component 3" });

        // change components to 1 & 2
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        selectMultiOption("components", "New Component 1");
        selectMultiOption("components", "New Component 2");
        tester.submit("Update");
        assertTextSequence(new String[] { "Component/s", "New Component 1", "New Component 2" });

        // change components to 1 & 2 & 3
        tester.clickLink("edit-issue");
        tester.setWorkingForm("issue-edit");
        selectMultiOption("components", "New Component 1");
        selectMultiOption("components", "New Component 2");
        selectMultiOption("components", "New Component 3");
        tester.submit("Update");
        assertTextSequence(new String[] { "Component/s", "New Component 1", "New Component 2", "New Component 3" });
    }

    /**
     * JRA-15011 - ensure ordering of Versions is fixed
     */
    public void testVersionOrdering()
    {
        restoreBlankInstance();

        final String[] versionFields = new String[] { "versions", "fixVersions" };
        final String[] versionFieldNames = new String[] { "Affects Version/s", "Fix Version/s" };
        for (int i = 0; i < versionFields.length; i++)
        {
            String versionField = versionFields[i];
            String versionFieldName = versionFieldNames[i];
            log("Testing ordering for versions field '" + versionField + "'");
            tester.clickLink("create_link");
            tester.submit("Next");
            tester.setFormElement("summary", "Test issue");

            // issue initially has Version 1 & 3 selected
            selectMultiOption(versionField, "New Version 1");
            selectMultiOption(versionField, "New Version 5");
            tester.submit("Create");
            assertTextSequence(new String[] { versionFieldName, "New Version 1", "New Version 5" });

            // change versions to 2 & 3
            tester.clickLink("edit-issue");
            tester.setWorkingForm("issue-edit");
            selectMultiOption(versionField, "New Version 4");
            selectMultiOption(versionField, "New Version 5");
            tester.submit("Update");
            assertTextSequence(new String[] { versionFieldName, "New Version 4", "New Version 5" });

            // change versions to 1 & 2
            tester.clickLink("edit-issue");
            tester.setWorkingForm("issue-edit");
            selectMultiOption(versionField, "New Version 1");
            selectMultiOption(versionField, "New Version 4");
            tester.submit("Update");
            assertTextSequence(new String[] { versionFieldName, "New Version 1", "New Version 4" });

            // change versions to 1 & 2 & 3
            tester.clickLink("edit-issue");
            tester.setWorkingForm("issue-edit");
            selectMultiOption(versionField, "New Version 1");
            selectMultiOption(versionField, "New Version 4");
            selectMultiOption(versionField, "New Version 5");
            tester.submit("Update");
            assertTextSequence(new String[] { versionFieldName, "New Version 1", "New Version 4", "New Version 5" });
        }
    }

    /*
     * Test the state of custom field tabs.
     */
    public void testFieldTabs() throws Exception
    {
        restoreData("TestIssueFields.xml");

        //This issue should have no custom fields.
        gotoIssue("HSP-1");
        assertElementNotPresent("customfieldmodule");

        //This issue should have two tabs.
        gotoIssue("HSP-3");
        assertElementPresent("customfieldmodule");
        text.assertTextPresent(createCFTabLocator("customfield-panel-1"), "Tab1");
        text.assertTextPresent(createCFValueLocator("customfield-panel-1", 10000), "Tab1CF");
        text.assertTextPresent(createCFTabLocator("customfield-panel-2"), "Tab2");
        text.assertTextPresent(createCFValueLocator("customfield-panel-2", 10001), "Tab2CF");

        //This issue should have only one tab.
        gotoIssue("HSP-2");
        assertElementPresent("customfieldmodule");
        assertLinkWithTextNotPresent("This issue should have only one tab.", "Tab2");
        text.assertTextPresent(createCFValueLocator("customfield-panel-1", 10001), "Tab2CF");

    }

    /*
     * Test the state of custom field tabs when working with date custom fields. The custom field dates are not rendered
     * with the other fields.
     */
    public void testDateCustomFieldTabs() throws Exception
    {
        restoreData("TestIssueFields.xml");

        //Make sure that we don't display any date custom fields when there are no values.
        gotoIssue("HSP-3");
        assertions.assertNodeDoesNotExist("//*[@id='datesmodule']//*[contains(@id, 'customfield')]");

        //This issue has a date custom field value but no others.
        gotoIssue("HSP-4");
        assertElementNotPresent("customfieldmodule");
        text.assertTextSequence(new IdLocator(tester, "datesmodule"), "DateCFTab2", "26/Jan/10");

        //This issue has two date fields on different tabs. They should both be displayed.
        gotoIssue("HSP-5");
        assertElementNotPresent("customfieldmodule");
        text.assertTextSequence(new IdLocator(tester, "datesmodule"), "DateTimeCFTab1", "03/Jan/10", "DateTimeCFTab2", "29/Jan/10");

        //This issuse have multiple custom field tabs and date fields.
        gotoIssue("HSP-6");
        assertElementPresent("customfieldmodule");
        text.assertTextPresent(createCFTabLocator("customfield-panel-1"), "Tab1");
        text.assertTextPresent(createCFValueLocator("customfield-panel-1", 10000), "Tab1CF");
        text.assertTextPresent(createCFTabLocator("customfield-panel-2"), "Tab2");
        text.assertTextPresent(createCFValueLocator("customfield-panel-2", 10001), "Tab2CF");
        text.assertTextSequence(new IdLocator(tester, "datesmodule"), "DateTimeCFTab1", "04/Jan/10", "DateTimeCFTab2", "12/Jan/10", "DateCFTab2", "19/Jan/10");
    }

    /*
     * Test the state of custom field tabs when working with user custom fields. The custom field dates are not rendered
     * with the other fields.
     */
    public void testUserCustomFieldTabs() throws Exception
    {
        restoreData("TestIssueFields.xml");

        //Make sure that we don't display any user custom fields when there are no values.
        gotoIssue("HSP-3");
        assertions.assertNodeDoesNotExist("//*[@id='peopledetails']//*[contains(@id, 'customfield')]");


        //This issue has a user custom field value but no others.
        gotoIssue("HSP-7");
        assertElementNotPresent("customfieldmodule");
        text.assertTextSequence(new IdLocator(tester, "peopledetails"), "UserPickerCF", ADMIN_FULLNAME);

        //This issuse have multiple custom field tabs and user fields fields.
        gotoIssue("HSP-8");
        assertElementPresent("customfieldmodule");
        text.assertTextPresent(createCFTabLocator("customfield-panel-1"), "Tab1");
        text.assertTextPresent(createCFValueLocator("customfield-panel-1", 10000), "Tab1CF");
        text.assertTextPresent(createCFTabLocator("customfield-panel-2"), "Tab2");
        text.assertTextPresent(createCFValueLocator("customfield-panel-2", 10001), "Tab2CF");

        text.assertTextSequence(new IdLocator(tester, "peopledetails"), "UserPickerCF", ADMIN_FULLNAME,
                "MultiGroupPickerCF", "jira-developers", "jira-users", "GroupPickerCF", "jira-administrators",
                "MultiUserPickerCF", ADMIN_FULLNAME, FRED_FULLNAME);
    }

    /*
     * Test the state of custom field tabs when working with user custom fields. The custom field dates are not rendered
     * with the other fields.
     */
    public void testUserCustomFieldWithMissingUsers() throws Exception
    {
        restoreData("TestIssueFields.xml");

        //This issuse have multiple custom field tabs and user fields fields.
        gotoIssue("HSP-9");
        assertElementPresent("customfieldmodule");
        text.assertTextPresent(createCFTabLocator("customfield-panel-1"), "Tab1");
        text.assertTextPresent(createCFValueLocator("customfield-panel-1", 10000), "Tab1CF");
        text.assertTextPresent(createCFTabLocator("customfield-panel-2"), "Tab2");
        text.assertTextPresent(createCFValueLocator("customfield-panel-2", 10001), "Tab2CF");

        text.assertTextSequence(new IdLocator(tester, "peopledetails"), "UserPickerCF", "admin-xx",
                "MultiGroupPickerCF", "jira-developers", "jira-users", "GroupPickerCF", "jira-administrators",
                "MultiUserPickerCF", ADMIN_FULLNAME, "fred-xx");

        assertLinkNotPresentWithText("admin-xx");
        assertLinkNotPresentWithText("fred-xx");
    }

    // JRA-14238
    public void testXssInImageUrls() throws Exception
    {
        administration.restoreData("TestImageUrlXss.xml");
        navigation.issue().gotoIssue("HSP-1");

        // priority icon URL
        tester.assertTextNotPresent("\"'/><script>alert('prioritiezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('prioritiezz');&lt;/script&gt;");

        // issue type icon URL
        tester.assertTextNotPresent("\"'/><script>alert('issue typezz');</script>");
        tester.assertTextPresent("&quot;'/&gt;&lt;script&gt;alert('issue typezz');&lt;/script&gt;");
    }

    @WebTest (TIME_ZONES)
    public void testDatesShouldBeDisplayedInUserTimeZoneInViewIssuePage() throws Exception
    {
        final String MKY_1 = "MKY-1";

        final String HONG_KONG = "Asia/Hong_Kong";
        final String HONOLULU = "Pacific/Honolulu";
        final String PAPUA_NEW_GUINEA = "Pacific/Port_Moresby";

        final String CREATED_HK = "06/Mar/11 9:36 AM";
        final String CREATED_HON = "05/Mar/11 3:36 PM";
        final String CREATED_PNG = "06/Mar/11 11:36 AM";

        final String UPDATED_HK = "06/Mar/11 9:43 AM";
        final String UPDATED_HON = "05/Mar/11 3:43 PM";
        final String UPDATED_PNG = "06/Mar/11 11:43 AM";

        final String RESOLVED_HK = "06/Mar/11 9:41 AM";
        final String RESOLVED_HON = "05/Mar/11 3:41 PM";
        final String RESOLVED_PNG = "06/Mar/11 11:41 AM";

        final String DATETIME_CF_HK = "09/Jun/11 6:30 AM";
        final String DATETIME_CF_HON = "08/Jun/11 12:30 PM";
        final String DATETIME_CF_PNG = "09/Jun/11 8:30 AM";

        final String WORKLOG_HK = "06/Mar/11 9:38 AM";
        final String WORKLOG_HON = "05/Mar/11 3:38 PM";
        final String WORKLOG_PNG = "06/Mar/11 11:38 AM";

        final String HISTORY_HK = "06/Mar/11 9:38 AM";
        final String HISTORY_HON = "05/Mar/11 3:38 PM";
        final String HISTORY_PNG = "06/Mar/11 11:38 AM";

        administration.restoreData("TestIssueFields.xml");
        administration.generalConfiguration().setDefaultUserTimeZone(HONG_KONG);

        // admin is in Hong Kong (GMT+11)
        navigation.issue().gotoIssue(MKY_1);
        assertThat(createdDate(), equalTo(CREATED_HK));
        assertThat(updatedDate(), equalTo(UPDATED_HK));
        assertThat(resolvedDate(), equalTo(RESOLVED_HK));
        assertThat(dateTimeCfTab1(), equalTo(DATETIME_CF_HK));
        navigation.issue().gotoIssueWorkLog(MKY_1);
        assertThat(worklogDate(), equalTo(WORKLOG_HK));
        navigation.issue().gotoIssueChangeHistory(MKY_1);
        assertThat(changeHistoryDate(), equalTo(HISTORY_HK));

        // admin now moves to Port Moresby
        navigation.userProfile().changeUserTimeZone(PAPUA_NEW_GUINEA);
        navigation.issue().gotoIssue(MKY_1);
        assertThat(createdDate(), equalTo(CREATED_PNG));
        assertThat(updatedDate(), equalTo(UPDATED_PNG));
        assertThat(resolvedDate(), equalTo(RESOLVED_PNG));
        assertThat(dateTimeCfTab1(), equalTo(DATETIME_CF_PNG));
        navigation.issue().gotoIssueWorkLog(MKY_1);
        assertThat(worklogDate(), equalTo(WORKLOG_PNG));
        navigation.issue().gotoIssueChangeHistory(MKY_1);
        assertThat(changeHistoryDate(), equalTo(HISTORY_PNG));

        // fred is in Honolulu (GMT-10)
        navigation.login("fred");
        navigation.userProfile().changeUserTimeZone(HONOLULU);
        navigation.issue().gotoIssue(MKY_1);
        assertThat(createdDate(), equalTo(CREATED_HON));
        assertThat(updatedDate(), equalTo(UPDATED_HON));
        assertThat(resolvedDate(), equalTo(RESOLVED_HON));
        assertThat(dateTimeCfTab1(), equalTo(DATETIME_CF_HON));
        navigation.issue().gotoIssueWorkLog(MKY_1);
        assertThat(worklogDate(), equalTo(WORKLOG_HON));
        navigation.issue().gotoIssueChangeHistory(MKY_1);
        assertThat(changeHistoryDate(), equalTo(HISTORY_HON));

        // anonymous should see HK time, which is the default
        navigation.logout();
        navigation.issue().gotoIssue(MKY_1);
        assertThat(createdDate(), equalTo(CREATED_HK));
        assertThat(updatedDate(), equalTo(UPDATED_HK));
        assertThat(resolvedDate(), equalTo(RESOLVED_HK));
        assertThat(dateTimeCfTab1(), equalTo(DATETIME_CF_HK));
        navigation.issue().gotoIssueWorkLog(MKY_1);
        assertThat(worklogDate(), equalTo(WORKLOG_HK));
        navigation.issue().gotoIssueChangeHistory(MKY_1);
        assertThat(changeHistoryDate(), equalTo(HISTORY_HK));
    }

    @WebTest (TIME_ZONES)
    public void testDueDateShouldBeDisplayedInSystemTimeZone() throws Exception
    {
        final String MKY_1 = "MKY-1";

        final String HONG_KONG = "Asia/Hong_Kong";
        final String HONOLULU = "Pacific/Honolulu";
        final String PAPUA_NEW_GUINEA = "Pacific/Port_Moresby";

        final String DUE_DATE_STRING = "10/Jan/99";

        administration.restoreData("TestIssueFields.xml");

        // set due date and read it back
        navigation.issue().gotoIssue(MKY_1);
        navigation.issue().setDueDate(MKY_1, DUE_DATE_STRING);
        assertThat(dueDate(), equalTo(DUE_DATE_STRING));

        // change the default user time zone, then make sure due date is not affected by this change
        administration.generalConfiguration().setDefaultUserTimeZone(HONG_KONG);
        navigation.issue().gotoIssue(MKY_1);
        assertThat(dueDate(), equalTo(DUE_DATE_STRING));

        // setting it should work as before
        navigation.issue().setDueDate(MKY_1, DUE_DATE_STRING);
        assertThat(dueDate(), equalTo(DUE_DATE_STRING));

        // change the test user's time zone, then make sure due date was not affected
        navigation.userProfile().changeUserTimeZone(PAPUA_NEW_GUINEA);
        navigation.issue().gotoIssue(MKY_1);
        assertThat(dueDate(), equalTo(DUE_DATE_STRING));

        // setting it should still work in the system time zone
        navigation.issue().setDueDate(MKY_1, DUE_DATE_STRING);
        assertThat(dueDate(), equalTo(DUE_DATE_STRING));
    }

    private String createdDate()
    {
        return locator.css("#create-date").getText();
    }

    private String dueDate()
    {
        return locator.css("#due-date").getText();
    }

    private String updatedDate()
    {
        return locator.css("#updated-date").getText();
    }

    private String resolvedDate()
    {
        return locator.css("#resolved-date").getText();
    }

    private String dateTimeCfTab1()
    {
        return locator.css("#customfield_10010-val").getText();
    }

    private String worklogDate()
    {
        return locator.xpath("//*[@id='worklog-10000']//*[@class='subText']").getText();
    }

    private String changeHistoryDate()
    {
        return locator.css("#changehistorydetails_10030 time").getText();
    }

    private Locator createCFValueLocator(String tab, int id)
    {
        return new XPathLocator(tester, String.format("//*[@id='%s']//*[@id='customfield_%d-val']", tab, id));
    }

    private Locator createCFTabLocator(String tab)
    {
        return new XPathLocator(tester, String.format("//a[@href='#%s']", tab));
    }

    private void enableDeleteAllWorklogInDefaultPermissionScheme(String groupName)
    {
        log("enabling delete all worklog deletion in default permission scheme for group " + groupName);
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLink("add_perm_" + DELETE_ALL_WORKLOGS.permissionKey());
        checkCheckbox("type", "group");
        selectOption("group", groupName);
        submit(" Add ");

    }

    private boolean isSubtaskPercentageGraphPresent()
    {
        WebTable issueSummary = getDialog().getWebTableBySummaryOrId("issuetable");
        TableCell percentageCell = issueSummary.getTableCell(0, 5);
        return CLASSNAME_SUBTASK_PERCENTAGE_CELL.equals(percentageCell.getClassName());
    }
}
