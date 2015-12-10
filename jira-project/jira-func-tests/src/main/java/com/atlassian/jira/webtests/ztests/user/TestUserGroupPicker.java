package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.USERS_AND_GROUPS })
public class TestUserGroupPicker extends JIRAWebTest
{
    private static final String USER_FIELD_NAME = "User field";
    private static final String SUMMARY_FRED = "Issue with user picker fred";
    private static final String SUMMARY_BOTH = "Issue with user picker admin, fred";
    private static final String SUMMARY_ADMIN = "Issue with user picker admin";
    private static final String THREE_ISSUES = "all 3 issue(s)";
    private static final String TWO_ISSUES = "all 2 issue(s)";
    private static final String USER_FIELD_ID = "customfield_10000";

    public TestUserGroupPicker(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        administration.restoreData("blankprojects.xml");
        grantGlobalPermission(BULK_CHANGE, Groups.USERS);
    }

    public void tearDown()
    {
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
        super.tearDown();
    }

    public void testUserGroupPicker() throws Exception
    {
        _testCustomFieldSetup();
        _testCreateIssueWithField();
    }

    private void _testCustomFieldSetup()
    {
        logSection("Adding Mutli user field and Group searcher");

        gotoAdmin();
        clickLink("view_custom_fields");
        clickLink("add_custom_fields");
        checkCheckbox("fieldType", "com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker");
        submit(BUTTON_NAME_NEXT);
        setFormElement("fieldName", USER_FIELD_NAME);
        selectOption("searcher", "User Picker & Group Searcher");
        submit(BUTTON_NAME_NEXT);
        checkCheckbox("associatedScreens", "1");
        submit("Update");
    }

    private void _testCreateIssueWithField()
    {
        logSection("Creating issues with Mutli user field and Group searcher");

        _testCreateIssueWithUser(ADMIN_USERNAME, ADMIN_FULLNAME);
        _testCreateIssueWithUser(FRED_USERNAME, FRED_FULLNAME);

        _createIssueWithUser(ADMIN_USERNAME + ", " + FRED_USERNAME);
        assertTextPresentBeforeText(USER_FIELD_NAME, ADMIN_FULLNAME);
        assertTextPresentBeforeText(USER_FIELD_NAME, FRED_FULLNAME);
        assertTextPresentBeforeText(ADMIN_FULLNAME, FRED_FULLNAME);

    }

    // -------------------------------------------------------------------------------------------------- private helpers
    private void _testCreateIssueWithUser(final String username, final String fullName)
    {
        _createIssueWithUser(username);

        assertTextPresentBeforeText(USER_FIELD_NAME, fullName);
        assertTextPresent("Issue with user picker " + username);
    }

    private void _createIssueWithUser(String username)
    {
        getNavigation().issue().goToCreateIssueForm(PROJECT_HOMOSAP, ISSUE_TYPE_BUG);
        setFormElement("summary", "Issue with user picker " + username);
        assertTextPresent(USER_FIELD_NAME);
        setFormElement(USER_FIELD_ID, username);
        submit("Create");
    }

}
