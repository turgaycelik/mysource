package com.atlassian.jira.webtest.webdriver.tests.admin.groupbrowser;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.BulkEditGroupMembersPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION})
@RestoreOnce ("xml/TestBulkEditGroupMembers.xml")
public class TestBulkEditGroupMembers extends BaseJiraWebTest
{
    private static final String PLEASE_REFRESH_MEMBERS_LIST = "Newly selected group(s) may have different members.";
    private static final String ASSIGN = "assign";
    private static final String FIELD_USERS_TO_ASSIGN = "usersToAssignStr";
    private static final String FIELD_SELECTED_GROUPS = "selectedGroupsStr";

    private static final String ERROR_CANNOT_ADD_USER_INVALID = "Cannot add user. 'invalid' does not exist";
    private static final String ERROR_ADMIN_ALREADY_MEMBER_OF_ALL = "Cannot add user 'admin', user is already a member of all the selected group(s)";
    private static final String ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN = "Cannot add user 'admin', user is already a member of 'jira-administrators'";

    private BulkEditGroupMembersPage bulkEditGroupMembersPage;

    @Ignore ("JRADEV-18305 Needs to be refactored so that usersToAssignStr uses the new MultiSelect control usersToAssignMultiSelect instead.")
    @Test
    public void testBulkEditGroupMembersPruning()
    {
        //select groups and add invalid user
        bulkEditGroupMembersPage = jira.goTo(BulkEditGroupMembersPage.class, Lists.asList("jira-developers",new String[] {"jira-users"}));
        assertTrue(bulkEditGroupMembersPage.groupHeadingText().contains("Selected 2 of 5 Groups"));
        bulkEditGroupMembersPage.addNewMember("invalid").submitMembersToAdd();
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains(ERROR_CANNOT_ADD_USER_INVALID));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("invalid"));
        bulkEditGroupMembersPage.addNewMember("");

        //add a existing member to a group
        bulkEditGroupMembersPage = jira.goTo(BulkEditGroupMembersPage.class, Lists.asList("jira-administrators", new String[]{}));
        bulkEditGroupMembersPage.addNewMember("admin").submitMembersToAdd();
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("admin"));
        bulkEditGroupMembersPage.addNewMember("");

        //add a existing member to multiple groups
        bulkEditGroupMembersPage = jira.goTo(BulkEditGroupMembersPage.class, Lists.asList("jira-developers",new String[] {"jira-users"}));
        bulkEditGroupMembersPage.addNewMember("admin").submitMembersToAdd();
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains(ERROR_ADMIN_ALREADY_MEMBER_OF_ALL));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("admin"));
        bulkEditGroupMembersPage.addNewMember("");

        //add a existing member and non existing member to a group
        bulkEditGroupMembersPage = jira.goTo(BulkEditGroupMembersPage.class, Lists.asList("jira-administrators", new String[]{}));
        bulkEditGroupMembersPage.addNewMembers(Lists.asList("admin",new String[] {"dev"})).submitMembersToAdd();
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("admin"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("dev"));
        bulkEditGroupMembersPage.addNewMember("");

        //add a existing member and non existing member to all groups
        bulkEditGroupMembersPage = jira.goTo(BulkEditGroupMembersPage.class, Lists.asList("jira-administrators",new String[] {"jira-developers","jira-users"}));
        bulkEditGroupMembersPage.addNewMembers(Lists.asList("admin",new String[] {"dev"})).submitMembersToAdd();
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains(ERROR_ADMIN_ALREADY_MEMBER_OF_ALL));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("admin"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("dev"));
        bulkEditGroupMembersPage.addNewMember("");

        //attempt to add various user members and test pruning a large set of usernames
        bulkEditGroupMembersPage = jira.goTo(BulkEditGroupMembersPage.class, Lists.asList("jira-administrators", new String[]{}));
        bulkEditGroupMembersPage.addNewMembers(Lists.asList("admin",new String[] {"user", "admin", "duplicate", "invalid", "dev", "duplicate", "duplicate", "error", "user"})).submitMembersToAdd();
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains(ERROR_ADMIN_ALREADY_MEMBER_OF_JIRA_ADMIN));
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains("Cannot add user. 'duplicate' does not exist"));
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains("Cannot add user. 'invalid' does not exist"));
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains("Cannot add user. 'duplicate' does not exist"));
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains("Cannot add user. 'duplicate' does not exist"));
        assertTrue(bulkEditGroupMembersPage.errorMessageText().contains("Cannot add user. 'error' does not exist"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("user"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("admin"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("admin"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("duplicate"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("invalid"));
        assertTrue(bulkEditGroupMembersPage.newMembersText().contains("error"));
        bulkEditGroupMembersPage.addNewMember("");

    }
}
