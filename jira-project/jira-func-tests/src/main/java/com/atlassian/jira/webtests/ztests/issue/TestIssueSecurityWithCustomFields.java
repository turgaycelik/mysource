package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/*
 * Tests issue security schemes based on Custom Fields.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS, Category.ISSUES, Category.SECURITY })
public class TestIssueSecurityWithCustomFields extends FuncTestCase
{
    private static final String GROUP_CUSTOM_FIELD_ID = "customfield_10001";
    private static final String USER_CUSTOM_FIELD_ID = "customfield_10010";
    private static final String GROUP_CF_ISSUE_ID = "HSP-1";
    private static final String USER_CF_ISSUE_ID = "MKY-1";
    private static final String SELECT_CUSTOM_ISSUE_ID_BIGADMIN = "MKY-3";
    private static final String SELECT_CUSTOM_ISSUE_ID_LITTLEADMIN = "MKY-2";
    private static final String PERMISSION_VIOLATION_ERROR = "It seems that you have tried to perform an operation which you are not permitted to perform.";
    private static final String DISPLAYING_ISSUES_COUNT = "Displaying issues <span class=\"results-count-start\">1</span> to 1";


    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestIssueSecurityWithCustomFields.xml");
        navigation.login(ADMIN_USERNAME);

    }

    /*
     * Test user is prevented from seeing an issue when security scheme is based on a custom field
     */
    public void testIssueSecurityWithCustomField()
    {
        navigation.issueNavigator().displayAllIssues();

        text.assertTextPresent(GROUP_CF_ISSUE_ID);
        navigation.issue().gotoIssue(GROUP_CF_ISSUE_ID);
        text.assertTextNotPresent(PERMISSION_VIOLATION_ERROR);

        // Login as a user and check we cannot see the issue
        navigation.logout();
        navigation.login(FRED_USERNAME, FRED_PASSWORD);
        navigation.issueNavigator().displayAllIssues();
        text.assertTextNotPresent(GROUP_CF_ISSUE_ID);
        navigation.issue().gotoIssue(GROUP_CF_ISSUE_ID);
        text.assertTextPresent(PERMISSION_VIOLATION_ERROR);
    }

    /**
     * Test no system error occurs when a security scheme is based on a
     * group selector custom field that has been deleted.
     */
    public void testViewIssueWithGroupCustomFieldDeleted()
    {
        deleteCustomField(GROUP_CUSTOM_FIELD_ID);
        text.assertTextPresent("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): My Issue Security Scheme");
    }

    /**
     * Test no system error occurs when a security scheme is based on a
     * user selector custom field that has been deleted.
     */
    public void testViewIssueWithUserCustomFieldDeleted()
    {
        deleteCustomField(USER_CUSTOM_FIELD_ID);
        text.assertTextPresent("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): My Issue Security Scheme");
    }

    private void _testUserOnlySeesCorrectIssue(final String userName, final String issueKey, final String expectedText)
    {
        navigation.logout();
        navigation.login(userName);
        navigation.issueNavigator().displayAllIssues();
        text.assertTextNotPresent(issueKey);
        text.assertTextPresent(expectedText);
    }

    /**
     * Test search results when a security scheme is based on a
     * group selector custom field that has been deleted.
     * TODO: reenable when JRA-12448 is fixed
     */
    public void _testSearchWithGroupCustomFieldDeleted() {
        deleteCustomField(GROUP_CUSTOM_FIELD_ID);
        navigation.issueNavigator().displayAllIssues();
        text.assertTextNotPresent(GROUP_CF_ISSUE_ID);
    }

    /**
     * Test search results when a security scheme is based on a
     * user selector custom field that has been deleted.
     * TODO: reenable when JRA-12448 is fixed
     */
    public void _testSearchWithUserCustomFieldDeleted()
    {
        deleteCustomField(USER_CUSTOM_FIELD_ID);
        navigation.issueNavigator().displayAllIssues();
        text.assertTextNotPresent(USER_CF_ISSUE_ID);
    }

    private void deleteCustomField(String fieldId)
    {
        administration.customFields().removeCustomField(fieldId);
    }

}
