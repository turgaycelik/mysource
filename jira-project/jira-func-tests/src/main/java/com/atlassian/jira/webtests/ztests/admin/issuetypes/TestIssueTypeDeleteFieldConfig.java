package com.atlassian.jira.webtests.ztests.admin.issuetypes;

import com.atlassian.jira.testkit.client.IssueTypeControl;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * tests fix for JRA-14009
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.BROWSING, Category.ISSUE_TYPES })
public class TestIssueTypeDeleteFieldConfig extends FuncTestCase
{
    /**
     * we want to delete an IssueType that has some obsolete field configs (dud data) associated with it and make sure nothing bad happens
     */
    public void testIssueTypeDeleteWithDanglingFieldConfigs()
    {
        administration.restoreData("TestDeleteIssueTypeWithDanglingFieldConfig.xml");
        navigation.gotoAdminSection("issue_types");

        tester.assertTextPresent("Issue Type To Delete");
        tester.gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=5");
        tester.submit("Delete");
        text.assertTextPresent(locator.css("h2"), "Issue Types");
        tester.assertTextNotPresent("Issue Type To Delete");
    }

    public void testIssueTypeDeleteWithExistingFieldConfigs()
    {
        administration.restoreBlankInstance();

        final IssueTypeControl.IssueType type = backdoor.issueType().createIssueType("Defunkt Issue Type");

        navigation.gotoAdminSection("view_custom_fields");

        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:textfield");
        tester.submit("nextBtn");
        tester.setFormElement("fieldName", "Silly Field");
        // Select 'Defunkt Issue Type' from select box 'issuetypes'.
        tester.selectOption("issuetypes", "Defunkt Issue Type");
        tester.submit("nextBtn");
        tester.submit("Update");
        // Click Link 'Issue Types' (id='issue_types').
        tester.gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=" + type.getId());
        tester.submit("Delete");
        text.assertTextPresent(locator.css("h2"), "Issue Types");
        tester.assertTextNotPresent("Defunkt Issue Type");
    }

    public void testIssueTypeDeleteWithDeletedFieldConfigs()
    {
        administration.restoreBlankInstance();

        final IssueTypeControl.IssueType type = backdoor.issueType().createIssueType("Defunkt Issue Type");

        navigation.gotoAdminSection("view_custom_fields");

        tester.clickLink("add_custom_fields");
        tester.checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:textfield");
        tester.submit("nextBtn");
        tester.setFormElement("fieldName", "Silly Field");
        // Select 'Defunkt Issue Type' from select box 'issuetypes'.
        tester.selectOption("issuetypes", "Defunkt Issue Type");
        tester.submit("nextBtn");
        tester.submit("Update");

        tester.clickLink("del_customfield_10000");
        tester.submit("Delete");

        // Click Link 'Issue Types' (id='issue_types').
        tester.gotoPage("/secure/admin/DeleteIssueType!default.jspa?id=" + type.getId());
        tester.submit("Delete");
        text.assertTextPresent(locator.css("h2"), "Issue Types");
        tester.assertTextNotPresent("Defunkt Issue Type");
    }
}