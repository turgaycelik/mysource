package com.atlassian.jira.webtest.webdriver.tests.admin.quicksearch;

import java.util.List;

import javax.inject.Inject;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.MultiSelectSuggestion;
import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage;
import com.atlassian.jira.pageobjects.pages.admin.ViewProjectsPage;
import com.atlassian.jira.pageobjects.pages.admin.user.UserBrowserPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;

import org.junit.Test;

import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.containsSuggestion;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.4
 */
@WebTest ( { Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.IE_INCOMPATIBLE })
@RestoreOnce ("xml/TestAdminQuickSearch.xml")
public class TestAdminQuickSearch extends BaseJiraWebTest
{
    // NOTE: this is read-only test that restores data once per whole test class for speed (@RestoreOnce)
    // DO NOT put any backdoor data modification operations here
    // just use another test class for that

    @Inject
    private PageBinder binder;

    @Test
    public void testNavigation()
    {
        final ViewIssueTypesPage config = jira.quickLoginAsSysadmin(ViewIssueTypesPage.class);
        config.execKeyboardShortcut("g", "g");
        ShifterDialog shifterDialog = binder.bind(ShifterDialog.class);
        AutoComplete autoComplete = shifterDialog.getAutoComplete();

        final List<MultiSelectSuggestion> suggestions = autoComplete.query("Projects").getSuggestions();
        assertEquals(2, suggestions.size());
        autoComplete.acceptUsingMouse(autoComplete.getActiveSuggestion());

        final ViewProjectsPage projectsPage = binder.bind(ViewProjectsPage.class);

        projectsPage.isAt(); // assert we have loaded the correct page

        config.execKeyboardShortcut("g", "g");
        shifterDialog = binder.bind(ShifterDialog.class);
        autoComplete = shifterDialog.getAutoComplete();

        autoComplete.query("edit user profile");

        autoComplete.acceptUsingKeyboard(autoComplete.getActiveSuggestion());
        binder.bind(UserBrowserPage.class);
    }

    @Test
    public void testNoHeaderQuickSearchOnNonAdminPages()
    {
        jira.quickLoginAsSysadmin(DashboardPage.class);

        jira.goToViewIssue("HSP-1");

        assertFalse("Expected Admin Quick Search NOT to be on view issue page",
                getAdminQuickSearchFromHeader().isPresent());
    }

    @Test
    public void testQuickSearchFromDialog()
    {
        final ViewIssueTypesPage config = jira.quickLoginAsSysadmin(ViewIssueTypesPage.class);
        config.execKeyboardShortcut("g", "g");
        final ShifterDialog shifterDialog = binder.bind(ShifterDialog.class);
        final AutoComplete autoComplete = shifterDialog.getAutoComplete();
        autoComplete.query("Pro");
        waitUntil(autoComplete.getTimedSuggestions(), containsSuggestion("Projects"));
        autoComplete.acceptUsingKeyboard(autoComplete.getActiveSuggestion());
        // TODO matches by main label are not prioritized - JRADEV-6356
        binder.bind(ViewProjectsPage.class);
    }

    @Test
    public void testQuickSearchFromDialogUsingKeywords()
    {
        final ViewIssueTypesPage config = jira.quickLoginAsSysadmin(ViewIssueTypesPage.class);
        config.execKeyboardShortcut("g", "g");
        final ShifterDialog shifterDialog = binder.bind(ShifterDialog.class);
        final AutoComplete autoComplete = shifterDialog.getAutoComplete();
        autoComplete.query("edit user profile");
        autoComplete.acceptUsingKeyboard(autoComplete.getActiveSuggestion());
        binder.bind(UserBrowserPage.class);
    }

    @Test
    public void shouldFindByMainSectionName()
    {
        final ViewIssueTypesPage config = jira.quickLoginAsSysadmin(ViewIssueTypesPage.class);
        config.execKeyboardShortcut("g", "g");
        final ShifterDialog shifterDialog = binder.bind(ShifterDialog.class);
        final AutoComplete autoComplete = shifterDialog.getAutoComplete();
        autoComplete.query("User management");
        final TimedQuery<Iterable<MultiSelectSuggestion>> suggestionsQuery = autoComplete.getTimedSuggestions();
        waitUntil(suggestionsQuery, containsSuggestion("Users", "User management"));
        waitUntil(suggestionsQuery, containsSuggestion("Groups", "User management"));
        waitUntil(suggestionsQuery, containsSuggestion("JIRA User Server", "User management"));
        waitUntil(suggestionsQuery, containsSuggestion("User Directories", "User management"));
    }

    private AutoComplete getAdminQuickSearchFromHeader()
    {
        return pageBinder.bind(JiraHeader.class).getAdminQuickSearch();
    }
}
