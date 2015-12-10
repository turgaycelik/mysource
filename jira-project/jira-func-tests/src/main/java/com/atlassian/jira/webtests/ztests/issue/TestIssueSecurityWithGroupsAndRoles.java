package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.admin.Project;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 * Tests that modifications to group or role membership will flush the cache by checking that the security level field
 * has the correct values reflected.
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.ROLES, Category.SECURITY, Category.USERS_AND_GROUPS })
public class TestIssueSecurityWithGroupsAndRoles extends JIRAWebTest
{
    private static final String[] EXPECTED_SECURITY_LEVEL_NONE = new String[] { "None" };
    private static final String[] EXPECTED_SECURITY_LEVEL_RED = new String[] { "None", SECURITY_LEVEL_ONE_NAME };
    private static final String[] EXPECTED_SECURITY_LEVEL_GREEN = new String[] { "None", SECURITY_LEVEL_THREE_NAME };
    private static final String NEW_PROJECT_NAME = "New Project";
    private static final String NEW_GROUP_NAME = "securityGroup";
    private static final String NEW_SECURITY_SCHEME_NAME = "security scheme copy";

    public TestIssueSecurityWithGroupsAndRoles(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueSecurityWithGroupsAndRoles.xml");
    }

    public void testAddingAndRemovingUserFromGroupFlushesSecurityCache()
    {
        //initially no roles are added, so we can only see 'None'
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //add 'developers' group to role, so it is displayed on the field
        addGroupToProjectRole(Groups.DEVELOPERS, PROJECT_HOMOSAP, JIRA_ADMIN_ROLE);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);

        //remove user from the developers group, so we are back to the single value 'None'
        removeUserFromGroup(ADMIN_USERNAME, Groups.DEVELOPERS);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //re-add user to the developers group, to check we have the security level again
        addUserToGroup(ADMIN_USERNAME, Groups.DEVELOPERS);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);
    }

    /**
     * Check if user is added or removed from a role, the issue security level cache is flushed
     */
    public void testAddingAndRemovingUserFromRolesFlushesIssueSecurityCache()
    {
        //initially no roles are added, so we can only see 'None'
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //add a new role, so it is displayed on the field
        addUserToProjectRole(ADMIN_USERNAME, PROJECT_HOMOSAP, JIRA_ADMIN_ROLE);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);

        //remove the role from the project, so we are back to the single value 'None'
        removeUserFromProjectRole(ADMIN_USERNAME, PROJECT_HOMOSAP, JIRA_ADMIN_ROLE);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
    }

    /**
     * Check if group is added or removed from a role, the issue security level cache is flushed
     */
    public void testAddingAndRemovingGroupRolesFromProjectFlushesIssueSecurityCache()
    {
        //initially no roles are added, so we can only see 'None'
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //add group to the security, so it is displayed on the field
        addGroupToProjectRole(Groups.USERS, PROJECT_HOMOSAP, JIRA_ADMIN_ROLE);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);

        //remove the role from the project, so we are back to the single value 'None'
        removeGroupFromProjectRole(Groups.USERS, PROJECT_HOMOSAP, JIRA_ADMIN_ROLE);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
    }

    /**
     * Check if a role is removed from jira, the issue security level cache is flushed
     */
    public void testRemovingRoleFlushesIssueSecurityCache()
    {
        //initially no roles are added, so we can only see 'None'
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //add group to the securitty, so it is displayed on the field
        addGroupToProjectRole(Groups.USERS, PROJECT_HOMOSAP, JIRA_ADMIN_ROLE);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);

        //remove the role, ie. remove the role from all schemes and check the only value is 'None'
        gotoProjectRolesScreen();
        clickLink("delete_" + JIRA_ADMIN_ROLE);
        submit("Delete");
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
    }

    /**
     * Check if a group is removed from jira, the issue security level cache is flushed
     */
    public void testRemovingGroupFlushesIssueSecurityCache()
    {
        //initially no roles are added, so we can only see 'None'
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //add a new group, so it is displayed on the field
        createGroup(NEW_GROUP_NAME);
        addGroupToSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_ONE_NAME, NEW_GROUP_NAME, new String[] { "", Groups.ADMINISTRATORS, Groups.DEVELOPERS, Groups.USERS, NEW_GROUP_NAME });
        addUserToGroup(ADMIN_USERNAME, NEW_GROUP_NAME);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);

        //remove the group, ie. remove the group from all schemes and check the only value is 'None'
        removeGroup(NEW_GROUP_NAME);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
    }

    /**
     * Check that newly created projects have default roles setup by checking admin can see the security level and users
     * can only see None
     */
    public void testNewProjectsWithDefaultRolesHaveCorrectSecurityLevel1()
    {
        //create and setup a new project (so it gets the default roles)
        addProject(NEW_PROJECT_NAME, "NEW", ADMIN_USERNAME);
        associateSecuritySchemeToProject(NEW_PROJECT_NAME, SECURITY_SCHEME_NAME);
        _testNewProjectsWithDefaultRolesHaveCorrectSecurityLevel();
    }

    /**
     * same as {@link #testNewProjectsWithDefaultRolesHaveCorrectSecurityLevel1()} but instead of associating the
     * security scheme with the project after its created, we do it during the creation
     */
    public void testNewProjectsWithDefaultRolesHaveCorrectSecurityLevel2()
    {
        //create and setup a new project (so it gets the default roles)
        Project project = administration.project();
        project.addProject(NEW_PROJECT_NAME, "NEW", ADMIN_USERNAME);
        associateSecuritySchemeToProject(NEW_PROJECT_NAME, SECURITY_SCHEME_NAME);
        _testNewProjectsWithDefaultRolesHaveCorrectSecurityLevel();
    }

    /**
     * Check updating the security scheme does not affect the security level options.
     */
    public void testRenamingSecuritySchemeFlushesCache()
    {
        //check we can see more than 'None' for the security level
        addGroupToProjectRole(Groups.USERS, PROJECT_HOMOSAP, JIRA_ADMIN_ROLE);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);

        //update the security scheme and check we can still see the same security
        navigation.gotoAdmin();
        clickLink("security_schemes");
        clickLink("edit_10000");//there should only be one scheme
        setFormElement("name", SECURITY_SCHEME_NAME + "edited");
        submit("Update");
        assertLinkWithTextExists("Expected scheme to have been renamed", SECURITY_SCHEME_NAME + "edited");
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);
    }

    /**
     * Check that if the associated security scheme of a project is unassociated, that the security levels are no longer
     * visible visible
     */
    public void testAddingAndRemovingSecuritySchemeFromProject()
    {
        //initiailly we can see the security level field
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //once we unassociate the security scheme, we can no longer see the field
        removeAssociationOfSecuritySchemeFromProject(PROJECT_HOMOSAP);
        createIssueStep1();
        getDialog().setWorkingForm("issue-create");
        assertFormElementNotPresent("security");

        //associate the secruity scheme again and check its back
        associateSecuritySchemeToProject(PROJECT_HOMOSAP, SECURITY_SCHEME_NAME);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
    }

    /**
     * Swap the security scheme with a new scheme and check that the security levels are updated
     */
    public void testSwappingSecuritySchemeFlushesCache()
    {
        //initiailly we can see the security level field
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //create a new security scheme
        createSecurityScheme(NEW_SECURITY_SCHEME_NAME, "new scheme for testing");
        //add a visible security level so its different to the original scheme
        createSecurityLevel(NEW_SECURITY_SCHEME_NAME, SECURITY_LEVEL_TWO_NAME, SECURITY_LEVEL_TWO_DESC);
        addGroupToSecurityLevel(NEW_SECURITY_SCHEME_NAME, SECURITY_LEVEL_TWO_NAME, Groups.USERS);
        createSecurityLevel(NEW_SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME, SECURITY_LEVEL_THREE_DESC);
        addGroupToSecurityLevel(NEW_SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME, Groups.DEVELOPERS);

        //associate the project with the new scheme and check the levels are updated
        associateSecuritySchemeToProject(PROJECT_HOMOSAP, NEW_SECURITY_SCHEME_NAME);
        assertIssueSecurityLevelOptions(new String[] { "None", SECURITY_LEVEL_THREE_NAME, SECURITY_LEVEL_TWO_NAME });

        //associate back to the original scheme and check the levels are updated again
        associateSecuritySchemeToProject(PROJECT_HOMOSAP, SECURITY_SCHEME_NAME);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
    }

    public void testAddingAndRemovingSecurityLevelFlushesCache()
    {
        //initiailly we can see the security level field
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //add a new security level to the scheme
        createSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME, SECURITY_LEVEL_THREE_DESC);
        //there is no user/role/group associated yet, so should not have changed
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
        //add group to security level that we can see with
        addGroupToSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME, Groups.USERS);
        //now check that the level has been added
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_GREEN);

        //add another level (this time with no permission to view it)
        createSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_TWO_NAME, SECURITY_LEVEL_TWO_DESC);
        //this should be the same as the previous
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_GREEN);
        //add role that we are not part of
        addRoleToSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME, JIRA_ADMIN_ROLE);
        //now check that the level has not changed (since we cannot view the level)
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_GREEN);

        //remove the security level we can see and check it updates the list
        deleteSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //remove the security level we cannot see and check it didnt change anything
        deleteSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_TWO_NAME);
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);
    }

    public void testGroupToRoleMappingSchemeToolFlushesCache()
    {
        //initially no roles are added, so we can only see 'None'
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //Map jira-developers and jira-admins to the developers role (no changes to the security level)
        mapGroupToRoles("permission schemes", DEFAULT_PERM_SCHEME, EasyMap.build(Groups.DEVELOPERS, JIRA_DEV_ROLE, Groups.ADMINISTRATORS, JIRA_DEV_ROLE));
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_NONE);

        //grant jira-users the administrators role (ie. can see the security level)
        mapGroupToRoles("permission schemes", DEFAULT_PERM_SCHEME, EasyMap.build(Groups.USERS, JIRA_ADMIN_ROLE));
        assertIssueSecurityLevelOptions(EXPECTED_SECURITY_LEVEL_RED);
    }

    private void _testNewProjectsWithDefaultRolesHaveCorrectSecurityLevel()
    {
        //check that as the admin we can see all security levels
        assertIssueSecurityLevelOptions(NEW_PROJECT_NAME, ISSUE_TYPE_BUG, EXPECTED_SECURITY_LEVEL_RED);

        //login as a user and check we can only see 'None'
        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        assertIssueSecurityLevelOptions(NEW_PROJECT_NAME, ISSUE_TYPE_BUG, EXPECTED_SECURITY_LEVEL_NONE);
    }

    private void assertIssueSecurityLevelOptions(String[] expectedSecurityLevels)
    {
        assertIssueSecurityLevelOptions(PROJECT_HOMOSAP, ISSUE_TYPE_BUG, expectedSecurityLevels);
    }

    /**
     * checks that the security level select field on the create issue details page has the expectedSecurityLevels
     *
     * @param projectName name of the project to select in the create issue page
     * @param issueType issue type to select in the create issue page
     * @param expectedSecurityLevels the expected values of the security level select field
     */
    private void assertIssueSecurityLevelOptions(String projectName, String issueType, String[] expectedSecurityLevels)
    {
        createIssueStep1(projectName, issueType);
        getDialog().setWorkingForm("issue-create");
        assertOptionsEqual("security", expectedSecurityLevels);
        assertOptionSelected("security", "None");
    }
}
