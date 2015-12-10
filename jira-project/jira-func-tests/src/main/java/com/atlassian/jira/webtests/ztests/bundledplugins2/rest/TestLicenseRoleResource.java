package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.backdoor.LicenseRoleControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.matcher.LicenseRoleBeanMatcher;
import com.atlassian.jira.testkit.client.restclient.Response;
import org.hamcrest.Matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestLicenseRoleResource extends RestFuncTest
{
    private static final String ROLE = "businessuser";
    private static final String USERS = "jira-users";
    private static final String DEVELOPERS = "jira-developers";
    private static final String ADMINS = "jira-administrators";

    private LicenseRoleControl roleClient;

    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        roleClient = backdoor.licenseRoles();
    }

    /**
     * Happy path test of the end-point. Edge cases covered in the unit-tests.
     */
    public void testAdminHappyPath()
    {
        LicenseRoleControl.LicenseRoleBean roleBean = roleClient.getRole(ROLE);

        //No roles by default.
        LicenseRoleBeanMatcher matcher = LicenseRoleBeanMatcher.forBusinessUser();
        assertThat(roleBean, matcher);
        assertThat(roleClient.getRolesMap(), Matchers.hasEntry(equalTo(ROLE), matcher));

        //Add some roles.
        roleBean = roleClient.putRole(ROLE, USERS, DEVELOPERS);
        assertThat(roleBean, matcher.setGroups(USERS, DEVELOPERS));
        assertThat(roleClient.getRole(ROLE), matcher);
        assertThat(roleClient.getRolesMap(), Matchers.hasEntry(equalTo(ROLE), matcher));

        //Add some, remove some.
        roleBean = roleClient.putRole(ROLE, USERS, ADMINS);
        assertThat(roleBean, matcher.setGroups(USERS, ADMINS));
        assertThat(roleClient.getRole(ROLE), matcher);
        assertThat(roleClient.getRolesMap(), Matchers.hasEntry(equalTo(ROLE), matcher));

        //Remove all roles.
        roleBean = roleClient.putRole(ROLE);
        assertThat(roleBean, matcher.setGroups());
        assertThat(roleClient.getRole(ROLE), matcher);
        assertThat(roleClient.getRolesMap(), Matchers.hasEntry(equalTo(ROLE), matcher));
    }

    public void testWebsudo()
    {
        backdoor.websudo().enable();
        try
        {
            Response<?> response = roleClient.getRoleResponse(ROLE);
            assertThat(response.statusCode, equalTo(401));

            response = roleClient.getRolesResponse();
            assertThat(response.statusCode, equalTo(401));

            response = roleClient.putRoleResponse(ROLE, USERS);
            assertThat(response.statusCode, equalTo(401));

        }
        finally
        {
            backdoor.websudo().disable();
        }
    }

    public void test403ReturnedForNonAdmin()
    {
        Response<?> response = roleClient.loginAs(FRED_USERNAME).getRoleResponse(ROLE);
        assertThat(response.statusCode, equalTo(403));

        response = roleClient.getRolesResponse();
        assertThat(response.statusCode, equalTo(403));

        response = roleClient.putRoleResponse(ROLE, USERS);
        assertThat(response.statusCode, equalTo(403));
    }
}
