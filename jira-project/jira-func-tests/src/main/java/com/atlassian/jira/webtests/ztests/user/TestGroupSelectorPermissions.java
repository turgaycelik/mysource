/*
 * Copyright (c) 2002-2005
 * All rights reserved.
 */

package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.webtests.WebTestCaseWrapper.logSection;

/**
 * Tests the GroupCF permission type, which allows a select-list (or radiobutton, etc) custom field to specify a group,
 * which is then granted certain permissions.
 */
@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS, Category.USERS_AND_GROUPS })
public class TestGroupSelectorPermissions extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("GroupSelectorPermissions.xml");
        backdoor.darkFeatures().enableForSite("ka.NO_GLOBAL_SHORTCUT_LINKS");
        // Ensure attachments are enabled
        administration.attachments().enable();
    }

    public void testWorkflowPermissions()
    {
        // In this data we grant the 'comment' permission to a 'GroupRadio' selector, and 'attach' permission to an 'Assigned Groups' selector
        // Initially, GroupRadio is set to 'HelpDesk' and 'Assigned Groups' is unset.
        logSection("Testing group permission selector");

        log("Testing that 'GroupRadio' selection (helpdesk) can comment, can't attach");
        navigation.login("helpdesk", "helpdesk");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkPresent("comment-issue");
        tester.assertLinkPresent("footer-comment-button");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");

        log("Check that webadmin users cannot do anything");
        navigation.login("webadmin", "webadmin");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkNotPresent("comment-issue");
        tester.assertLinkNotPresent("footer-comment-button");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");

        log("Check that unixadmin users cannot do anything");
        navigation.login("unixadmin", "unixadmin");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkNotPresent("comment-issue");
        tester.assertLinkNotPresent("footer-comment-button");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");

        log("Testing that regular users can't comment, can't attach");
        navigation.login("test", "test");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkNotPresent("comment-issue");
        tester.assertLinkNotPresent("footer-comment-button");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");

        navigation.login("dba", "dba");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkNotPresent("comment-issue");
        tester.assertLinkNotPresent("footer-comment-button");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");

        // Now we change the GroupRadio (comment) selector and set the 'Assigned groups' selector
        log("Editing fields: setting GroupRadio (comment perm) to WebAdmin, and 'Assigned Groups' (attach perm) to helpdesk");
        navigation.issue().gotoIssue("NP-1");
        tester.clickLink("edit-issue");
        tester.assertOptionValuesEqual("customfield_10010", new String[] {"-1", "10000", "10001", "10002", "10003"}); //{"-1", "dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        tester.assertOptionValuesEqual("customfield_10030", new String[] {"-1", "10020", "10021", "10022", "10023"}); //{"-1", "dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        tester.assertOptionValuesEqual("customfield_10040", new String[] {"10030", "10031", "10032", "10033"}); //{"dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        tester.assertOptionValuesEqual("customfield_10041", new String[] {"-1", "10034", "10035", "10036", "10037"}); // {"-1", "dba-user-group", "help-desk-group", "unix-admin-group", "webadmin-group"}
        tester.selectOption("customfield_10010", "help-desk-group"); // Assigned Group (attach)
        tester.checkCheckbox("customfield_10030", "10023"); // GroupRadio (comment) "webadmin-group"
        tester.setFormElement("customfield_10040", "10030"); // Assigned Group (move)
        tester.selectOption("customfield_10041", "unix-admin-group"); // Assigned Group (delete)
        tester.submit();
        final String response = tester.getDialog().getResponseText();
        assertions.text().assertTextSequence(response, "Assigned Groups:", "help-desk-group"); // attach
        assertions.text().assertTextSequence(response, "GroupRadio:", "webadmin-group"); // comment
        assertions.text().assertTextSequence(response, "Multi Checkboxes:", "dba-user-group"); // move
        assertions.text().assertTextSequence(response, "Select List:", "unix-admin-group"); // delete

        log("Testing that 'GroupRadio' selection (now webadmin) can comment, can't attach");
        navigation.login("webadmin", "webadmin");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkPresent("comment-issue");
        tester.assertLinkPresent("footer-comment-button");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");

        log("Testing that helpdesk can no longer comment, but can attach");
        navigation.login("helpdesk", "helpdesk");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkPresent("attach-file");
        tester.assertLinkNotPresent("comment-issue");
        tester.assertLinkNotPresent("footer-comment-button");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");

        log("Check that DBA can delete issues");
        navigation.login("dba", "dba");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkNotPresent("comment-issue");
        tester.assertLinkNotPresent("footer-comment-button");
        tester.assertLinkNotPresent("delete-issue");
        tester.assertLinkPresent("move-issue");

        log("Check that Unix admin can move issues");
        navigation.login("unixadmin", "unixadmin");
        navigation.issue().gotoIssue("NP-1");
        tester.assertLinkNotPresent("attach-file");
        tester.assertLinkNotPresent("comment-issue");
        tester.assertLinkNotPresent("footer-comment-button");
        tester.assertLinkPresent("delete-issue");
        tester.assertLinkNotPresent("move-issue");
    }
}
