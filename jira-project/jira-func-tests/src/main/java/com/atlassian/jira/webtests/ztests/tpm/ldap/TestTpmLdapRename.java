package com.atlassian.jira.webtests.ztests.tpm.ldap;

import javax.naming.NamingException;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Issue;

import org.apache.commons.lang.RandomStringUtils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * To be run against Active Directory or Open LDAP in TPM.
 * This test relies on TestTpmLdapSetup being run first in order to create the appropriate LDAP User Directory.
 * Check the scenario details at: https://extranet.atlassian.com/pages/viewpage.action?spaceKey=JIRADEV&title=Detect+rename+user+in+LDAP+Scenarios
 *
 * @since v6.1
 */
@WebTest ({ Category.FUNC_TEST, Category.LDAP, Category.TPM })
public class TestTpmLdapRename extends AbstractTpmLdapTest
{
    public void testScenario1() throws Exception
    {

        final String projectKey = generateProjectKey();
        setUpScenarioData(projectKey);

        final String issueKey = createIssueForUser(projectKey, "ron");

        final LdapOperationsHelper ldapOperationsHelper = getLdapOperationsHelper();
        ldapOperationsHelper.removeUser("john");
        ldapOperationsHelper.renameUser("ron","john");

        navigation.logout();
        navigation.login("john","password");

        final Issue issue = backdoor.issues().getIssue(projectKey + "-1");
        assertThat(issue.fields.assignee.name, equalTo("john"));

        clearScenarioData(projectKey, issueKey);
    }


    public void testCheckIfRenameIsTrackedInDefaultConfiguration() throws Exception
    {
        addUser("wilma");
        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Test");
        tester.setFormElement("username", "wilma");
        tester.setFormElement("password", "password");
        tester.submit("test");

        assertions.getTextAssertions().assertTextNotPresent("Test user rename is configured and tracked : Succeeded");
    }

    private String createIssueForUser(final String projectKey, final String username)
    {
        //add issue
        final IssueCreateResponse issueResponse = backdoor.issues().createIssue(projectKey, "Testing LDAP rename", "ron");
        return issueResponse.key;
    }

    private LdapOperationsHelper getLdapOperationsHelper()
    {
        //adjust params to use local LDAP
        return new LdapOperationsHelper(getLdapServer(),getUserDn(),getPassword(),getBaseDn(), isActiveDirectory());
        //new LdapOperationsHelper("rabbit", "cn=admin,dc=atlassian,dc=pl", "admin123", "dc=atlassian,dc=pl");
    }

    private void setUpScenarioData(final String projectKey) throws NamingException, InterruptedException
    {
        //add project
        backdoor.project().addProject(projectKey + "  test project", projectKey, "admin");

        //add users
        addUser("ron");
        addUser("john");

        navigation.gotoAdminSection("user_directories");
        tester.clickLinkWithText("Synchronise");

        //add users to groups
        backdoor.usersAndGroups().addUserToGroup("ron", "jira-users");
        backdoor.usersAndGroups().addUserToGroup("ron","jira-developers");
        backdoor.usersAndGroups().addUserToGroup("john","jira-users");
        backdoor.usersAndGroups().addUserToGroup("john","jira-developers");
    }

    private void clearScenarioData(final String projectKey, final String issueKey) throws NamingException
    {
        backdoor.issues().deleteIssue(issueKey,true);
        backdoor.project().deleteProject(projectKey);

        deleteUserIfExists("ron");
        deleteUserIfExists("john");
    }

    private void gotoViewUser(final String username)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
    }

    private void addUser(final String username)
    {
        deleteUserIfExists(username);
        assertions.getUserAssertions().assertUserDoesNotExist(username);

        navigation.gotoAdminSection("user_browser");

        // Add a User
        tester.clickLink("create_user");
        tester.setFormElement("username", username);
        tester.setFormElement("fullname", generateUserFullName(username));
        tester.setFormElement("password", "password");
        tester.setFormElement("confirm", "password");
        tester.setFormElement("email", username + "@nicedomain.com");
        tester.submit("Create");

        assertions.getUserAssertions().assertUserExists(username);
        assertions.getUserAssertions().assertUserDetails(username, generateUserFullName(username), username + "@nicedomain.com", "LDAP Directory");
    }

    private void deleteUserIfExists(final String username)
    {
        if (assertions.getUserAssertions().userExists(username))
        {
            log("User " + username + "  was found - attempting to clean up before running test.");
            gotoViewUser(username);
            // Click Link 'Delete User' (id='deleteuser_link').
            tester.clickLink("deleteuser_link");
            tester.submit("Delete");
        }
    }

    private String generateUserFullName(final String username) {
        return username.subSequence(0,1).toString().toUpperCase() + username.substring(1,username.length()) + " Nicelastname";
    }

    private String generateProjectKey()
    {
        return RandomStringUtils.randomAlphabetic(5).toUpperCase();
    }
}
