package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SECURITY })
public class TestIssueSecurityActions extends JIRAWebTest
{
    private static final String DEFAULT_FIELD_CONFIG = "Default Field Configuration";

    public TestIssueSecurityActions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
        if (projectExists(PROJECT_HOMOSAP))
        {
            log("Project 'homospaien' exists");
            if (!(componentExists(COMPONENT_NAME_ONE, PROJECT_HOMOSAP)))
            {
                addComponent(PROJECT_HOMOSAP, COMPONENT_NAME_ONE);
            }
            if (!(versionExists(VERSION_NAME_ONE, PROJECT_HOMOSAP)))
            {
                addVersion(PROJECT_HOMOSAP, VERSION_NAME_ONE, "Version 1");
            }
        }
        else
        {
            addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
            addComponent(PROJECT_HOMOSAP, COMPONENT_NAME_ONE);
            addVersion(PROJECT_HOMOSAP, VERSION_NAME_ONE, "Version 1");
        }
        if (projectExists(PROJECT_NEO))
        {
            log("Project '" + PROJECT_NEO + "' exists");
        }
        else
        {
            addProject(PROJECT_NEO, PROJECT_NEO_KEY, ADMIN_USERNAME);
        }

        if (securtiySchemeExists(SECURITY_SCHEME_NAME))
        {
            associateSecuritySchemeToProject(PROJECT_HOMOSAP, "None");
            associateSecuritySchemeToProject(PROJECT_NEO, "None");
            deleteSecurityScheme(SECURITY_SCHEME_NAME);
        }

        clickOnAdminPanel("admin.usersgroups", "user_browser");
        try
        {
            assertLinkPresentWithText(BOB_USERNAME);
        }
        catch (Throwable t)
        {
            addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        }

        resetFields();
    }

    public void tearDown()
    {
        try
        {
            deleteAllIssuesInAllPages();
            resetFields();
        }
        catch (Throwable t)
        {
            log("Some problem in tear down of " + getClass().getName(), t);
        }
        super.tearDown();
    }

    public void testIssueSecurityActions()
    {
        String issueKeyWithoutSecurity = addIssue(PROJECT_NEO, PROJECT_NEO_KEY, "Bug", "test without issue security", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description without issue security", null, null, null);
        String issueKeyNormal = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test with components", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description with components", null, null, null);

        issueSecurityCreateScheme();
        issueSecurityFieldSchemesAddDuplicateScheme();
        issueSecurityFieldSchemesAddInvalidScheme();
        issueSecurityAddSecurityLevel();
        issueSecurityAssociateSchemeToProject();

        issueSecurityAddGroupToSecurityLevel();

        String issueKeyWithSecurity = issueSecurityCreateIssueWithSecurity();
        issueSecurityCreateIssueWithoutIssueSecurity();
        issueSecurityCreateIssueWithSecurityRequired();

        issueSecurityEditIssueWithIssueSecurity(issueKeyWithSecurity);
        issueSecurityEditIssueWithoutIssueSecurity(issueKeyWithoutSecurity);
        issueSecurityEditIssueWithSecurityRequired(issueKeyNormal);
        issueSecuritySecurityViolation(issueKeyWithSecurity);

        issueSecurityMoveIssueAwayFromIssueSecurity(issueKeyWithSecurity);
        issueSecurityMoveIssueToIssueSecurity(issueKeyWithSecurity);
        issueSecurityMoveIssueWithDefaultSecurity(issueKeyWithoutSecurity);
        issueSecurityMoveIssueWithSameSecurity(issueKeyWithoutSecurity);

        issueSecurityCreateSubTaskWithSecurity(issueKeyWithSecurity);

        issueSecurityRemoveAssociationWithProject();
        issueSecurityRemoveGroupFromSecurityLevel();
        issueSecurityDeleteSecurityLevel();
        issueSecurityDeleteScheme();
    }

    public void testProjectRoleIssueSecurityType()
    {
        restoreData("TestIssueSecurityScheme.xml");

        gotoIssueSecuritySchemes();

        // Goto the issue security scheme
        navigation.gotoAdmin();
        clickLink("security_schemes");
        clickLinkWithText("Security Levels");
        assertTextNotPresent("(Administrators)");

        // Goto level 1 security level
        clickLink("add_level 1");

        // Select a project role security type
        checkCheckbox("type", "projectrole");
        selectOption("projectrole", "Administrators");
        submit(" Add ");

        assertTextPresent("(Administrators)");
    }

    public void issueSecurityCreateScheme()
    {
        log("Issue Security: Creating a security scheme");
        createSecurityScheme(SECURITY_SCHEME_NAME, SECURITY_SCHEME_DESC);
        assertTextPresent("Issue Security Schemes");
        assertLinkPresentWithText(SECURITY_SCHEME_NAME);
    }

    public void issueSecurityDeleteScheme()
    {
        log("Issue Security: Deleting a security scheme");
        deleteSecurityScheme(SECURITY_SCHEME_NAME);
        assertLinkNotPresentWithText(SECURITY_SCHEME_NAME);
        assertTextPresent("You do not currently have any issue security schemes configured.");
    }

    public void issueSecurityAddSecurityLevel()
    {
        log("Issue Security: Adding a security level to a security scheme");
        createSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_ONE_NAME, SECURITY_LEVEL_ONE_DESC);
        assertTextSequence(new String[] { SECURITY_LEVEL_ONE_NAME, SECURITY_LEVEL_ONE_DESC });
        assertTextNotPresent(SECURITY_LEVEL_TWO_NAME);
        assertTextNotPresent(SECURITY_LEVEL_TWO_DESC);
        assertTextNotPresent(SECURITY_LEVEL_THREE_NAME);
        assertTextNotPresent(SECURITY_LEVEL_THREE_DESC);

        createSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_TWO_NAME, SECURITY_LEVEL_TWO_DESC);
        assertTextSequence(new String[] {
                SECURITY_LEVEL_TWO_NAME, SECURITY_LEVEL_TWO_DESC,
                SECURITY_LEVEL_ONE_NAME, SECURITY_LEVEL_ONE_DESC
        });
        assertTextNotPresent(SECURITY_LEVEL_THREE_NAME);
        assertTextNotPresent(SECURITY_LEVEL_THREE_DESC);

        createSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME, SECURITY_LEVEL_THREE_DESC);
        assertTextSequence(new String[] {
                SECURITY_LEVEL_THREE_NAME, SECURITY_LEVEL_THREE_DESC,
                SECURITY_LEVEL_TWO_NAME, SECURITY_LEVEL_TWO_DESC,
                SECURITY_LEVEL_ONE_NAME, SECURITY_LEVEL_ONE_DESC
        });

        // Check it is added
        gotoIssueSecuritySchemes();
        clickLinkWithText(SECURITY_SCHEME_NAME);
        assertLinkPresent("add_" + SECURITY_LEVEL_ONE_NAME);
        assertLinkPresent("add_" + SECURITY_LEVEL_TWO_NAME);
        assertLinkPresent("add_" + SECURITY_LEVEL_THREE_NAME);
    }

    public void issueSecurityDeleteSecurityLevel()
    {
        log("Issue Security: Deleting a security level to a security scheme");
        deleteSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_ONE_NAME);

        gotoIssueSecuritySchemes();
        clickLinkWithText(SECURITY_SCHEME_NAME);
        assertLinkNotPresent("add_" + SECURITY_LEVEL_ONE_NAME);
        assertLinkPresent("add_" + SECURITY_LEVEL_TWO_NAME);
        assertLinkPresent("add_" + SECURITY_LEVEL_THREE_NAME);
    }

    public void issueSecurityAssociateSchemeToProject()
    {
        log("Issue Security: Associate a Project to a Scheme");

        associateSecuritySchemeToProject(PROJECT_HOMOSAP, SECURITY_SCHEME_NAME);

        assertThat(backdoor.project().getSchemes(PROJECT_HOMOSAP_KEY).issueSecurityScheme.name, equalTo(SECURITY_SCHEME_NAME));
    }

    public void issueSecurityRemoveAssociationWithProject()
    {
        log("Issue Security: Remove association between a Project and a Scheme");

        associateSecuritySchemeToProject(PROJECT_HOMOSAP, "None");

        assertThat(backdoor.project().getSchemes(PROJECT_HOMOSAP_KEY).issueSecurityScheme, nullValue());
    }

    public void issueSecurityAddGroupToSecurityLevel()
    {
        log("Issue Security: Add groups to issue security level");

        addGroupToSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_ONE_NAME, Groups.ADMINISTRATORS);
        addGroupToSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_TWO_NAME, Groups.DEVELOPERS);
        addGroupToSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_THREE_NAME, Groups.USERS);

        gotoIssueSecuritySchemes();
        clickLinkWithText(SECURITY_SCHEME_NAME);
        assertLinkPresent("delGroup_" + Groups.ADMINISTRATORS + "_" + SECURITY_LEVEL_ONE_NAME);
        assertLinkPresent("delGroup_" + Groups.DEVELOPERS + "_" + SECURITY_LEVEL_TWO_NAME);
        assertLinkPresent("delGroup_" + Groups.USERS + "_" + SECURITY_LEVEL_THREE_NAME);
    }

    public void issueSecurityRemoveGroupFromSecurityLevel()
    {
        log("Issue Security: Remove groups from issue security level");

        removeGroupFromSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_ONE_NAME, Groups.ADMINISTRATORS);

        gotoIssueSecuritySchemes();
        clickLinkWithText(SECURITY_SCHEME_NAME);
        assertLinkNotPresent("delGroup_" + Groups.ADMINISTRATORS + "_" + SECURITY_LEVEL_ONE_NAME);
        assertLinkPresent("delGroup_" + Groups.DEVELOPERS + "_" + SECURITY_LEVEL_TWO_NAME);
        assertLinkPresent("delGroup_" + Groups.USERS + "_" + SECURITY_LEVEL_THREE_NAME);
    }

    /**
     * Tests the error handling if a duplicate scheme is made
     */
    public void issueSecurityFieldSchemesAddDuplicateScheme()
    {
        log("Issue Security: Creating a duplicate security scheme");
        createSecurityScheme(SECURITY_SCHEME_NAME, SECURITY_SCHEME_DESC);
        assertTextPresent("A Scheme with this name already exists.");
    }

    /**
     * Tests the error handling if a scheme with an invalid nameis made
     */
    public void issueSecurityFieldSchemesAddInvalidScheme()
    {
        log("Issue Security: Creating a duplicate security scheme");
        createSecurityScheme("", "");
        assertTextPresent("Please specify a name for this Scheme.");
    }

    /**
     * Creating issue with issue security settings
     */
    public String issueSecurityCreateIssueWithSecurity()
    {
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test with issue security", "Minor", new String[] { COMPONENT_NAME_ONE }, new String[] { VERSION_NAME_ONE }, new String[] { VERSION_NAME_ONE }, ADMIN_FULLNAME, "test environment 9", "test description 9", null, "Red", null);
        assertTextPresent("test with issue security");
        assertTextPresent("Minor");
        assertTextPresent("Bug");
        assertTextPresent("Security Level:");
        assertTextPresent("Red");
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        return issueKey;
    }

    /**
     * Tests if the 'Security Level' field is available with an project WITHOUT an associated 'Issue Security Scheme'
     */
    public void issueSecurityCreateIssueWithoutIssueSecurity()
    {
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        log("Create Issue: Tests the availability of the 'Security Level' field");
        getNavigation().issue().goToCreateIssueForm(PROJECT_NEO, "Bug");

        assertTextPresent("CreateIssueDetails.jspa");

        assertTextPresent(PROJECT_NEO);
        assertFormElementNotPresent("security");
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
    }

    /**
     * Tests for error handling with 'Security Level' required
     */
    public void issueSecurityCreateIssueWithSecurityRequired()
    {
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        setSecurityLevelToRequried();

        log("Create Issue: Test the ability to create an issue with 'Security Level' required");
        createIssueStep1();

        setFormElement("summary", "This is a summary");

        submit("Create");

        assertTextPresent("CreateIssueDetails.jspa");
        assertTextPresent("Security Level is required.");

        resetFields();
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
    }

    /**
     * Tests if the user is able to alter the security level of an issue
     */
    public void issueSecurityEditIssueWithIssueSecurity(String issueKey)
    {
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        log("Edit Issue: Test ability to change Security Level");

        gotoIssue(issueKey);
        clickLink("edit-issue");

        selectOption("security", SECURITY_LEVEL_TWO_NAME);
        submit();
        assertTextPresent(issueKey);
        assertTextPresent(SECURITY_LEVEL_TWO_NAME);
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
    }

    /**
     * Tests if the 'Security Level' Link is available with an project WITHOUT an associated 'Issue Security Scheme'
     */
    public void issueSecurityEditIssueWithoutIssueSecurity(String issueKey)
    {
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        log("Edit Issue: Test availability of 'Security Level' field");
        gotoIssue(issueKey);
        clickLink("edit-issue");

        assertFormElementNotPresent("security");
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
    }

    /**
     * Tests for error handling with 'Security Level' required
     */
    public void issueSecurityEditIssueWithSecurityRequired(String issueKey)
    {
        setSecurityLevelToRequried();
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);

        log("Edit Issue: Test the ability to update an issue with 'Security Level' required");
        gotoIssue(issueKey);
        clickLink("edit-issue");

        submit("Update");

        assertTextPresent("Edit Issue");
        assertTextPresent("Security Level is required.");

        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        resetFields();
    }

    /**
     * Tests if a user can breach Issue Security()
     */
    public void issueSecuritySecurityViolation(String issueKey)
    {
        log("Edit Issue: Test the availabilty of an issue for which a user is not permitted to view.");
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        logout();
        login(BOB_USERNAME, BOB_PASSWORD);
        gotoIssue(issueKey);
        assertTextPresent("Permission Violation");
        assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
    }

    /**
     * Tests the availabilty of 'Issue Security' when moving an issue to a project WITHOUT issue security
     */
    public void issueSecurityMoveIssueAwayFromIssueSecurity(String issueKey)
    {
        log("Move Operation: Moving an issue to a project without 'Issue Security");
        gotoIssue(issueKey);

        clickLink("move-issue");
        assertTextPresent("Move Issue");

        selectOption("pid", PROJECT_NEO);
        submit();

        assertTextPresent("Step 3 of 4");
        assertTextNotPresent("All fields will be updated automatically.");
        getDialog().setWorkingForm("jiraform");
        submit();

        assertTextPresent("Step 4 of 4");
        assertTextPresent(PROJECT_NEO);
        assertTextPresent(PROJECT_HOMOSAP);
        getDialog().setWorkingForm("jiraform");
        submit("Move");

        assertTextNotPresent("Security Level");
        assertTextNotPresent("Red");
    }

    /**
     * Tests the availabilty of 'Issue Security' when moving an issue to a project WITH issue security
     */
    public void issueSecurityMoveIssueToIssueSecurity(String issueKey)
    {
        log("Move Operation: Moving an issue to a project with 'Issue Security");
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);

        setRequiredFieldsOnEnterprise(DEFAULT_FIELD_CONFIG, SECURITY_LEVEL_FIELD_ID);
        gotoIssue(issueKey);

        clickLink("move-issue");
        assertTextPresent("Move Issue");

        selectOption("pid", PROJECT_HOMOSAP);
        submit();

        assertTextPresent("Step 3 of 4");
        selectOption("security", SECURITY_LEVEL_TWO_NAME);

        submit();
        assertTextPresent("Step 4 of 4");
        submit("Move");

        assertTextPresent("Security Level:");
        assertTextPresent("Orange");

        setOptionalFieldsOnEnterprise(DEFAULT_FIELD_CONFIG, SECURITY_LEVEL_FIELD_ID);
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
    }

    /**
     * Tests that default security level is used if user does not have permission to set security
     */
    public void issueSecurityMoveIssueWithDefaultSecurity(String issueKey)
    {
        log("Move Operation: Moving an issue with default security levels");
        setRequiredFieldsOnEnterprise(DEFAULT_FIELD_CONFIG, SECURITY_LEVEL_FIELD_ID);
        gotoIssue(issueKey);

        clickLink("move-issue");
        assertTextPresent("Move Issue");

        selectOption("pid", PROJECT_HOMOSAP);
        submit();

        assertTextPresent("Move Issue: Update Fields");
        getDialog().setWorkingForm("jiraform");
        submit();

        assertTextPresent("Security Level: Security Level is required.");

        setDefaultSecurityLevel(SECURITY_SCHEME_NAME, SECURITY_LEVEL_ONE_NAME);
        gotoIssue(issueKey);

        clickLink("move-issue");
        assertTextPresent("Move Issue");

        selectOption("pid", PROJECT_HOMOSAP);
        submit();

        assertTextPresent("Move Issue: Update Fields");
        setWorkingForm("jiraform");
        submit();

        assertTextPresent("Move Issue: Confirm");
        setWorkingForm("jiraform");
        submit();

        assertTextPresent("Security Level:");
        assertTextPresent(SECURITY_LEVEL_ONE_NAME);
        setDefaultSecurityLevel(SECURITY_SCHEME_NAME, null);
        setOptionalFieldsOnEnterprise(DEFAULT_FIELD_CONFIG, SECURITY_LEVEL_FIELD_ID);
    }

    /**
     * Test that security level stays the same if moved between the same security scheme
     */
    public void issueSecurityMoveIssueWithSameSecurity(String issueKey)
    {
        log("Move Operation: Move an issue to a project with the same issue security scheme.");
        associateSecuritySchemeToProject(PROJECT_NEO, SECURITY_SCHEME_NAME);
        gotoIssue(issueKey);

        clickLink("move-issue");
        assertTextPresent("Move Issue");

        selectOption("pid", PROJECT_NEO);
        submit();

        assertTextPresent("Move Issue: Update Fields");
        setWorkingForm("jiraform");
        submit();

        assertTextPresent("Move Issue: Confirm");
        setWorkingForm("jiraform");
        submit();

        assertTextPresent("Security Level:");
        assertTextPresent(SECURITY_LEVEL_ONE_NAME);

        associateSecuritySchemeToProject(PROJECT_NEO, "None");
    }

    /**
     * Tests if a sub task has its security level automatically allocated
     */
    public void issueSecurityCreateSubTaskWithSecurity(String issueKey)
    {
        log("Sub Task Create: Create a sub task from an issue with a security level");
//        grantGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
        createSubTaskStep1(issueKey, SUB_TASK_DEFAULT_TYPE);
        setFormElement("summary", SUB_TASK_SUMMARY);
        submit();
        assertTextPresent("test with issue security");
        assertTextPresent(SUB_TASK_SUMMARY);
        assertTextPresent(SECURITY_LEVEL_TWO_NAME);

        deleteCurrentIssue();

        deactivateSubTasks();
//        removeGroupPermission(SET_ISSUE_SECURITY, JIRA_ADMIN);
    }

    private void setDefaultSecurityLevel(String scheme_name, String securityLevel)
    {
        gotoIssueSecuritySchemes();
        clickLinkWithText(scheme_name);
        if (securityLevel != null)
        {
            clickLink("default_" + securityLevel);
        }
        else
        {
            clickLinkWithText("Change default security level to \"None\"");
        }
    }
}
