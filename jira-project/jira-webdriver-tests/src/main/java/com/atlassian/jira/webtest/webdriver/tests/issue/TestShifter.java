package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.MultiSelectSuggestion;
import com.atlassian.jira.pageobjects.dialogs.ShifterDialog;
import com.atlassian.jira.pageobjects.navigator.AdvancedSearch;
import com.atlassian.jira.pageobjects.pages.viewissue.IssueMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;
import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.hasNumberSuggestions;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for shifter actions
 *
 * @since v6.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@RestoreOnce ("xml/TestsSelectedIssueOperations.xml")
public class TestShifter extends BaseJiraWebTest
{
    @Inject
    private TraceContext traceContext;

    @Inject
    private PageBinder binder;

    @Before
    public void setUp()
    {
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, "admin");
    }

    @Test
    public void testDialogHasMessageAboutDeletedIssue() throws Exception
    {
        final AdvancedSearch advancedSearch = jira.goTo(AdvancedSearch.class)
                .enterQuery("")
                .submit();

        backdoor.issues().deleteIssue("MKY-2",true);

        advancedSearch.execKeyboardShortcut("g", "g");

        final ShifterDialog shifterDialog = binder.bind(ShifterDialog.class);
        final AutoComplete autoComplete = shifterDialog.getAutoComplete();
        autoComplete.query("Edit");

        assertTrue("Shifter dialog message is visible", shifterDialog.messageIsVisible());

        assertTrue(shifterDialog.isFooterHintVisible());
    }

    @Test
    public void testAnonymousUserHasSuggestions()
    {
        jira.logout();
        final AdvancedSearch advancedSearch = jira.goToIssueNavigator().enterQuery("").submit();
        advancedSearch.execKeyboardShortcut(".");

        final ShifterDialog shifterDialog = binder.bind(ShifterDialog.class);
        final AutoComplete autoComplete = shifterDialog.getAutoComplete();

        //This is not ideal. Originally I had it checking that the suggestion is present but I was getting could not
        //find the a tag in the selection. However just checking that there are suggestions does the same thing as I know
        //from the pulled in data set that the only suggestion would be Comment, so it does the exact same thing.
        TimedQuery<Iterable<MultiSelectSuggestion>> suggestionsQuery = autoComplete.getTimedSuggestions();
        waitUntil(suggestionsQuery, hasNumberSuggestions(1));

        autoComplete.query("Comment");
        suggestionsQuery = autoComplete.getTimedSuggestions();
        waitUntil(suggestionsQuery, hasNumberSuggestions(1));

        autoComplete.clearQuery();
        suggestionsQuery = autoComplete.getTimedSuggestions();
        waitUntil(suggestionsQuery, hasNumberSuggestions(1));

        assertTrue(shifterDialog.isFooterHintVisible());
    }

    @Test
    public void testMoreActionsDropdownIsClosedOnShifter()
    {
        final ViewIssuePage viewIssuePage = jira.goToViewIssue("ANA-1");
        final IssueMenu issuesMenu = viewIssuePage.getIssueMenu();

        issuesMenu.openMoreActions();
        assertTrue(issuesMenu.isMoreActionsOpened());

        viewIssuePage.execKeyboardShortcut(".");
        final ShifterDialog shifterDialog = binder.bind(ShifterDialog.class);

        assertTrue(shifterDialog.isOpen());
        assertFalse(issuesMenu.isMoreActionsOpened());
    }
}
