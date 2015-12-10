package com.atlassian.jira.webtest.webdriver.tests.ajaxuserpicker;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.fields.SelectedItemMatchers;
import com.atlassian.jira.pageobjects.components.fields.Suggestion;
import com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.admin.roles.AbstractActorSelectionPage;
import com.atlassian.jira.pageobjects.pages.admin.roles.GroupRoleActorActionPage;
import com.atlassian.jira.pageobjects.pages.admin.roles.UserRoleActorActionPage;
import com.atlassian.jira.pageobjects.pages.admin.roles.ViewProjectRolesPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ManageWatchersPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.query.Poller;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;

/**
 * Webdriver test for the Admin section user picker.
 *
 * @since v5.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION})
public class TestAdminSectionUserPicker extends BaseJiraWebTest
{

    protected static final String NOUSERPICKERUSER = "nouserpickeruser";

    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditDefaultRoleMembership()
    {
        final UserRoleActorActionPage userRoleActorActionPage = navigateToUserRoleActorActionPage();
        testEditSingleUser(userRoleActorActionPage);
    }

    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditMultipleDefaultRoleMembership()
    {
        final UserRoleActorActionPage userRoleActorActionPage = navigateToUserRoleActorActionPage();
        testEditMultipleUsers(userRoleActorActionPage);
    }

    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditDefaultRoleMembershipNoResult()
    {
        final UserRoleActorActionPage userRoleActorActionPage = navigateToUserRoleActorActionPage();
        testEditNoResult(userRoleActorActionPage);
    }

    //quick test to ensure that no dropdown is shown in the edit group screen.
    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditDefaultGroupRoleMembership()
    {
        final GroupRoleActorActionPage groupRoleActorActionPage = navigateToGroupRoleActorActionPage();
        testEditNoResult(groupRoleActorActionPage);
    }

    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditSingleWatcher()
    {
        final ManageWatchersPage manageWatchersPage = navigateToManageWatchersPage("HSP-1");
        testEditSingleUser(manageWatchersPage);
    }

    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditMultipleWatcher()
    {
        final ManageWatchersPage manageWatchersPage = navigateToManageWatchersPage("HSP-1");
        testEditMultipleUsers(manageWatchersPage);
    }

    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditWatchersNoPermission()
    {
        addUserToGroup("jira-administrators", NOUSERPICKERUSER);

        jira.quickLogin(NOUSERPICKERUSER, NOUSERPICKERUSER);

        final ManageWatchersPage manageWatchersPage = navigateToManageWatchersPage("HSP-1");
        testEditNoResult(manageWatchersPage);
    }

    @Test
    @Restore ("TestAjaxUserPicker.xml")
    public void testEditDefaultRoleMembershipWithNoUserPickerPermission()
    {
        addUserToGroup("jira-administrators", NOUSERPICKERUSER);

        jira.quickLogin(NOUSERPICKERUSER, NOUSERPICKERUSER);

        final UserRoleActorActionPage userRoleActorActionPage = navigateToUserRoleActorActionPage();
        testEditNoResult(userRoleActorActionPage);

    }

    private void testEditMultipleUsers(final AbstractActorSelectionPage editPage)
    {
        editPage.getPicker().query("water");
        Poller.waitUntilFalse(editPage.getPicker().isSuggestionsPresent());
        Poller.waitUntil("expected to contain suggestion \"rastusw\".",
                editPage.getPicker().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.idContainsSubstring("rastusw", Suggestion.class)));
        Poller.waitUntil("expected to contain suggestion \"waterm\".",
                editPage.getPicker().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.idContainsSubstring("waterm", Suggestion.class)));
        editPage.getPicker().getSuggestionWithId("waterm").click();
        editPage.add();

        editPage.getPicker().query("john");
        Poller.waitUntilFalse(editPage.getPicker().isSuggestionsPresent());

        Poller.waitUntil("expected to contain suggestion \"johnr\".",
                editPage.getPicker().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.idContainsSubstring("johnr", Suggestion.class)));
        editPage.getPicker().getSuggestionWithId("johnr").click();
        editPage.getPicker().query("fre");
        Poller.waitUntilFalse(editPage.getPicker().isSuggestionsPresent());

        Poller.waitUntil("expected to contain suggestion \"fred\".",
                editPage.getPicker().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.idContainsSubstring("fred", Suggestion.class)));
        editPage.getPicker().getSuggestionWithId("fred").click();
        editPage.add();

        Poller.waitUntil("Expected to have Water Seed as a watcher.",
                editPage.selectedItemList().getSelectedItems(),
                IterableMatchers.hasItemThat(SelectedItemMatchers.hasWatcherKey("waterm")));
        Poller.waitUntil("Expected to have Fred Normal as a watcher.",
                editPage.selectedItemList().getSelectedItems(),
                IterableMatchers.hasItemThat(SelectedItemMatchers.hasWatcherKey("fred")));
        Poller.waitUntil("Expected to have John Rotten as a watcher.",
                editPage.selectedItemList().getSelectedItems(),
                IterableMatchers.hasItemThat(SelectedItemMatchers.hasWatcherKey("johnr")));
    }

    private void testEditSingleUser(final AbstractActorSelectionPage editPage)
    {
        editPage.getPicker().query("water");
        Poller.waitUntilFalse(editPage.getPicker().isSuggestionsPresent());

        Poller.waitUntil("expected to contain suggestion \"rastusw\".",
                editPage.getPicker().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.idContainsSubstring("rastusw", Suggestion.class)));
        Poller.waitUntil("expected to contain suggestion \"waterm\".",
                editPage.getPicker().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.idContainsSubstring("waterm", Suggestion.class)));

        editPage.getPicker().getSuggestionWithId("waterm").click();

        Poller.waitUntil("Expected to have selected \"waterm\".",
                editPage.getPicker().getValue(),
                containsString("waterm"));

        editPage.add();
        Poller.waitUntil("Expected to have Water Seed as a watcher.",
                editPage.selectedItemList().getSelectedItems(),
                IterableMatchers.hasItemThat(SelectedItemMatchers.hasWatcherKey("waterm")));
    }

    private void testEditNoResult(final AbstractActorSelectionPage editPage)
    {
        editPage.getPicker().query("water");
        Poller.waitUntilFalse(editPage.getPicker().isSuggestionsPresent());

        Poller.waitUntilFalse("Should contain no suggestions.", editPage.getPicker().isSuggestionsPresent());

        editPage.add();
    }

    public UserRoleActorActionPage navigateToUserRoleActorActionPage()
    {
        return jira.goTo(ViewProjectRolesPage.class)
                .getProjectRole("Users")
                .manageDefaultMembers()
                .editUserActors();
    }

    public GroupRoleActorActionPage navigateToGroupRoleActorActionPage()
    {
        return jira.goTo(ViewProjectRolesPage.class)
                .getProjectRole("Users")
                .manageDefaultMembers()
                .editGroupActors();
    }

    public ManageWatchersPage navigateToManageWatchersPage (final String issueKey)
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue(issueKey);
        return viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.MANAGE_WATCHERS, ManageWatchersPage.class, issueKey);
    }

    public void addUserToGroup(final String groupName, final String username)
    {
        backdoor.usersAndGroups().addUserToGroup(username, groupName);
    }

}
