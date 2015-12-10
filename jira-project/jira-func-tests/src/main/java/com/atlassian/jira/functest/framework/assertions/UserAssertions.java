package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

/**
 * @since v4.3
 */
public class UserAssertions extends AbstractFuncTestUtil
{
    private final Assertions assertions;

    public UserAssertions(final WebTester tester, final JIRAEnvironmentData environmentData, final Assertions assertions)
    {
        super(tester, environmentData, 2);
        this.assertions = assertions;
    }

    public boolean userExists(final String username)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
        return tester.getDialog().getResponseText().contains("Account information");
    }

    public void assertUserExists(String username)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
        tester.assertTextNotPresent("This user does not exist");
    }

    public void assertUserDoesNotExist(String username)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
        tester.assertTextPresent("This user does not exist");
    }

    public void assertUserDetails(final String username, final String displayName, final String emailAddress, final String directoryName)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
        tester.assertTextNotPresent("This user does not exist");
        assertions.assertNodeByIdEquals("username", username);
        assertions.assertNodeByIdEquals("displayName", displayName);
        tester.assertTextPresent("href=\"mailto:" + emailAddress + '"');
        assertions.assertNodeByIdEquals("directory", directoryName);
    }

    public void assertGroupExists(String name)
    {
        tester.gotoPage("/secure/admin/user/ViewGroup.jspa?name=" + name);
        tester.assertTextNotPresent("The group does not exist");
    }

    public void assertGroupDoesNotExist(String name)
    {
        tester.gotoPage("/secure/admin/user/ViewGroup.jspa?name=" + name);
        tester.assertTextPresent("The group does not exist");
    }

    public void assertUserBelongsToGroup(final String username, final String groupname)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
        assertions.assertNodeByIdHasText("groups", groupname);
    }

    public void assertUserDoesNotBelongToGroup(final String username, final String groupname)
    {
        tester.gotoPage("/secure/admin/user/ViewUser.jspa?name=" + username);
        assertions.assertNodeByIdDoesNotHaveText("groups", groupname);
    }
}
