package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestInlineIssueLinking extends JIRAWebTest
{

    String summary1;
    String summary2;
    String issueKey1;
    String issueKey2;
    private float jiraJavaVersion;
    private static final float MIN_JAVA_VERSION = 1.3f;

    public TestInlineIssueLinking(String name)
    {
        super(name);
    }

    public void _setUp()
    {
        restoreBlankInstance();
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        if (projectExists(PROJECT_HOMOSAP))
        {
            log("Project: " + PROJECT_HOMOSAP + " exists");
        }
        else
        {
            addProject(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, ADMIN_USERNAME);
        }

        createSecurityScheme("admin_only_scheme", "");
        createSecurityLevel("admin_only_scheme", "admin_only", "");
        addGroupToSecurityLevel("admin_only_scheme", "admin_only", Groups.ADMINISTRATORS);
        associateSecuritySchemeToProject(PROJECT_HOMOSAP, "admin_only_scheme");
        grantGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);

        addUser("link_user", "link_user", "link_user", "link_user@local");

        // Check summary does not show when there is no user
        grantGroupPermission(BROWSE, "Anyone");

        summary1 = "summary1";
        summary2 = "summary2";
        issueKey1 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", summary1, "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description to clone 1", null, "admin_only", null);
        issueKey2 = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", summary2, "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", issueKey1, null, null, null);

    }

    public void testSummaryOnLink()
    {
        jiraJavaVersion = getJiraJavaVersion();
        if (jiraJavaVersion > MIN_JAVA_VERSION)
        {
            _setUp();
            checkForUserWithPerm();
            checkForNoUser();
            checkForUserNoPerm();

            logout();
            login(ADMIN_USERNAME, ADMIN_PASSWORD);

            setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, "description", WIKI_STYLE_RENDERER);

            checkForUserWithPerm();
            checkForNoUser();
            checkForUserNoPerm();

            _tearDown();
        }
    }

    private void checkForUserNoPerm()
    {
        // Check summary does not exist for user without permission
        logout();
        login("link_user", "link_user");
        gotoIssue(issueKey2);
        assertTextPresent("Log Out");
        assertTextNotPresent("title=\"" + summary1 + "\""); // check summary is not there
        assertLinkWithTextNotPresent("Link should not be there", issueKey1);            // check link is not there (JRA-14893)
    }

    private void checkForNoUser()
    {
        logout();

        gotoIssue(issueKey2);
        assertTextPresent("Log In");
        assertTextNotPresent("title=\"" + summary1 + "\""); // check summary is not there
        assertLinkWithTextNotPresent("Link should not be there", issueKey1);            // check link is not there (JRA-14893)
    }

    private void checkForUserWithPerm()
    {
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        gotoIssue(issueKey2);
        assertTextPresent("title=\"" + summary1 + "\"");    // check summary is there
        assertTextPresent("/browse/" + issueKey1);          //check link is there
    }

    public void _tearDown()
    {
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);

        //undo all changes
        setFieldConfigurationFieldToRenderer(DEFAULT_FIELD_CONFIGURATION, "description", DEFAULT_TEXT_RENDERER);
        removeGroupPermission(BROWSE, "Anyone");
        removeGroupPermission(SET_ISSUE_SECURITY, Groups.ADMINISTRATORS);
        associateSecuritySchemeToProject(PROJECT_HOMOSAP, "None");
        deleteSecurityScheme("admin_only_scheme");
    }

}
