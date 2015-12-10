package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.LocatorFactory;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.admin.user.EditUserPage;
import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import net.sourceforge.jwebunit.WebTester;

/**
 * Admin operations for users and groups
 *
 * @since v3.13
 */
public class UsersAndGroupsImpl implements UsersAndGroups
{
    private static final String PAGE_USER_BROWSER = "/secure/admin/user/UserBrowser.jspa";
    private static final String DELETE_GROUP = "/secure/admin/user/DeleteGroup!default.jspa?name=";
    private static final String PAGE_VIEW_USER = "/secure/admin/user/ViewUser.jspa?name=";

    private final TextAssertions text;
    private final Navigation navigation;
    private final LocatorFactory locators;
    private final WebTester tester;

    public UsersAndGroupsImpl(WebTester tester, final Navigation navigation, final TextAssertions text, final LocatorFactory locators)
    {
        this.tester = tester;
        this.text = text;
        this.navigation = navigation;
        this.locators = locators;
    }

    /**
     * Creates a user with the given username and the same password, fullname and an email address username@example.com
     *
     * @param username the username.
     */
    public void addUser(String username)
    {
        addUser(username, username, username, username + "@example.com");
    }

    public boolean userExists(final String username)
    {
        tester.gotoPage(PAGE_USER_BROWSER);
        return tester.getDialog().isLinkPresent(username);              
    }

    public void addUser(String username, String password, String fullname, String emailAddress)
    {
        addUser(username, password, fullname, emailAddress, false);
    }

    public void addUser(String username, String password, String fullname, String emailAddress, boolean sendEmail)
    {
        addUserWithoutVerifyingResult(username, password, fullname, emailAddress, sendEmail);
        text.assertTextSequence(new WebPageLocator(tester), new String[] {"Username:", username, "Full Name:", fullname, "Email:", emailAddress});
    }

    public void addUserWithoutVerifyingResult(String username, String password, String fullname, String emailAddress)
    {
        addUserWithoutVerifyingResult(username, password, fullname, emailAddress, false);
    }

    public void addUserWithoutVerifyingResult(String username, String password, String fullname, String emailAddress, boolean sendEmail)
    {
        tester.gotoPage(PAGE_USER_BROWSER);
        tester.clickLink("create_user");
        tester.setFormElement("username", username);
        tester.setFormElement("password", password);
        tester.setFormElement("confirm", password);
        tester.setFormElement("fullname", fullname);
        tester.setFormElement("email", emailAddress);
        if (sendEmail)
        {
            tester.setFormElement("sendEmail", "true");
        }
        else
        {
            tester.uncheckCheckbox("sendEmail");
        }
        tester.submit("Create");
    }

    public void deleteUser(final String username)
    {
        tester.gotoPage(PAGE_USER_BROWSER);
        tester.clickLink("deleteuser_link_" + username);
        tester.assertTextPresent("Delete User: " + username);
        tester.submit("Delete");
        gotoViewUser(username);
        tester.assertTextPresent("User does not exist");
    }

    public void deleteGroup(final String groupname)
    {
        tester.gotoPage(DELETE_GROUP + groupname);
        tester.assertTextPresent("Delete Group: " + groupname);
        tester.submit("Delete");
    }

    public void gotoViewUser(final String username)
    {
        tester.gotoPage(PAGE_VIEW_USER + username);
    }

    @Override
    public void gotoUserBrowser()
    {
        tester.gotoPage(PAGE_USER_BROWSER);
    }

    @Override
    public EditUserPage gotoEditUser(String username)
    {
        navigation.gotoPage("secure/admin/user/EditUser!default.jspa?editName=" + username);
        return new EditUserPage(tester);
    }

    public void addUserToGroup(final String userName, final String groupName)
    {
        gotoViewUser(userName);
        tester.clickLink("editgroups_link");
        try
        {
            // use tester direct so we don't dump the page (as the super version does from WebTestCase)
            tester.selectOption("groupsToJoin", groupName);
            tester.submit("join");
        }
        catch (Throwable ignoreItMeansTheyAreAlreadyInTheGroup)
        {
            // do nothing
        }

        gotoViewUser(userName);
        tester.assertTextPresent(groupName);
    }

    public void addGroup(final String groupName)
    {
        tester.gotoPage("/secure/admin/user/GroupBrowser.jspa");
        if (tester.getDialog().isLinkPresentWithText(groupName))
        {
            tester.clickLink("del_" + groupName);
            tester.submit("Delete");
        }

        tester.setFormElement("addName", groupName);
        tester.submit();
        tester.assertLinkPresentWithText(groupName);
    }

    @Override
    public void removeUserFromGroup(String userName, String groupName)
    {
        gotoViewUser(userName);
        tester.clickLink("editgroups_link");
        tester.selectOption("groupsToLeave", groupName);
        tester.submit("leave");
        gotoViewUser(userName);
        text.assertTextNotPresent(locators.id("groups"), groupName);
    }
}
