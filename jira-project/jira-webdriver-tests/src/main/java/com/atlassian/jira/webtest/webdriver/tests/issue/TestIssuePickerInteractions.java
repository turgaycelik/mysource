package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.fields.LozengeMatchers;
import com.atlassian.jira.pageobjects.components.fields.Suggestion;
import com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.linkissue.LinkIssueDialog;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.inject.Inject;
import org.junit.After;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

/**
 * Webdriver test for the Issue Picker Interactions
 *
 * @since v5.2
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ISSUES, Category.ISSUE_LINKS })
public class TestIssuePickerInteractions extends BaseJiraWebTest
{
    @Inject private PageElementFinder pageElementFinder;
    private static final String HSP_PREFIX = "HSP-";
    private static final String HSP_1 = "HSP-1";
    private static final String HSP_2 = "HSP-2";
    private static final String HSP_3 = "HSP-3";
    private static final String HSP_4 = "HSP-4";
    private static final String ISSUE_WITH_T_PRECEDED_BY_PARENTHESIS = HSP_4;
    private static final String ISSUE_WITH_T_IN_SKIPPED_KEYWORDS = HSP_3;
    private static final String MK_1 = "MK-1";

    private LinkIssueDialog linkIssueDialog;

    public LinkIssueDialog openDialogFromViewIssue()
    {
        ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, HSP_1);
        LinkIssueDialog dialog = viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE, LinkIssueDialog.class, HSP_1);
        Poller.waitUntilTrue("Link Issue Dialog did not open successfully", dialog.isOpen());
        return dialog;
    }

    public LinkIssueDialog openDialogFromIssueNavigator()
    {
        jira.goToIssueNavigator().enterQuery("").submit();
        ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, HSP_1);
        LinkIssueDialog dialog = viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.LINK_ISSUE, LinkIssueDialog.class, HSP_1);
        Poller.waitUntilTrue("Link Issue Dialog did not open successfully", dialog.isOpen());
        return dialog;
    }

    @After
    public void closeDialog()
    {
        if (linkIssueDialog != null)
        {
            linkIssueDialog.close();
        }
    }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testNumericFiltering()
    {
        linkIssueDialog = openDialogFromViewIssue();

        // should match 1 to MK_1
        linkIssueDialog.issuePicker().clearQuery().query("1");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().hasActiveSuggestion());

        Poller.waitUntil("Active history search suggestion was expected to start with " + MK_1,
                linkIssueDialog.issuePicker().getActiveSuggestionText(), startsWith(MK_1));
        linkIssueDialog.issuePicker().selectActiveSuggestion();

        Poller.waitUntil("Active history search selected input was expected to start with " + MK_1,
                linkIssueDialog.issuePicker().getItems(), IterableMatchers.hasItemThat(LozengeMatchers.withName(MK_1)));
   }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testMultiIssueInput()
    {
        linkIssueDialog = openDialogFromViewIssue();
        linkIssueDialog.issuePicker().clearQuery().query("HSP-2 mk-1");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().isSuggestionsPresent());

        Poller.waitUntilTrue("No lozenges were loaded", linkIssueDialog.issuePicker().awayFromQueryInputArea().hasItems());
        Poller.waitUntil("Element picked was expected to be " + HSP_2,
                linkIssueDialog.issuePicker().getItems(), IterableMatchers.hasItemThat(LozengeMatchers.withName(HSP_2)));

        Poller.waitUntil("Element picked was expected to be " + MK_1,
                linkIssueDialog.issuePicker().getItems(), IterableMatchers.hasItemThat(LozengeMatchers.withName(MK_1))); // should be converted to uppercase

        // checking that items do not appear in suggestions
        linkIssueDialog.issuePicker().clearQuery().query(HSP_2);
        Poller.waitUntilFalse(linkIssueDialog.issuePicker().isSuggestionsLoading());
        Poller.waitUntilFalse(linkIssueDialog.issuePicker().hasActiveSuggestion());

        // lower case queries should be mapped to upper case searches.
        linkIssueDialog.issuePicker().clearQuery().query("mk-1");
        Poller.waitUntilFalse(linkIssueDialog.issuePicker().isSuggestionsLoading());
        Poller.waitUntilFalse(linkIssueDialog.issuePicker().hasActiveSuggestion());
    }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testSuggestionsAppear()
    {
        linkIssueDialog = openDialogFromViewIssue();
        linkIssueDialog.issuePicker().clearQuery().query("H");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().isSuggestionsPresent());

        Poller.waitUntil("History Search doesn't contain " + HSP_2,
                linkIssueDialog.issuePicker().getHistorySearchSuggestions().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.containsSubstring(HSP_2)));
    }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testSuggestionsCloseWithEsc()
    {
        linkIssueDialog = openDialogFromViewIssue();
        linkIssueDialog.issuePicker().triggerSuggestions();
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().isSuggestionsPresent());
        linkIssueDialog.issuePicker().query(Keys.ESCAPE);
        // check that the query input has been cleared
        Poller.waitUntilEquals("Escape key didn't close suggestions", "", linkIssueDialog.issuePicker().getActiveSuggestionText());
    }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testRoofAndFloorWrapping()
    {
        linkIssueDialog = openDialogFromViewIssue();
        linkIssueDialog.issuePicker().clearQuery().query(HSP_PREFIX);
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().isSuggestionsPresent());
        Poller.waitUntil("Active history search suggestion was expected to start with " + HSP_PREFIX,
                linkIssueDialog.issuePicker().getActiveSuggestionText(), startsWith(HSP_PREFIX));
        linkIssueDialog.issuePicker().query(Keys.DOWN);
        Poller.waitUntil("Active history search suggestion was expected to start with " + HSP_PREFIX,
                linkIssueDialog.issuePicker().getActiveSuggestionText(), startsWith(HSP_PREFIX));
        linkIssueDialog.issuePicker().query(Keys.DOWN);
        Poller.waitUntil("Active history search suggestion was expected to start with " + HSP_PREFIX,
                linkIssueDialog.issuePicker().getActiveSuggestionText(), startsWith(HSP_PREFIX));
        linkIssueDialog.issuePicker().query(Keys.DOWN);
        String floorString = "Enter issue key";
        Poller.waitUntil("Active history search suggestion was expected to start with " + floorString,
                linkIssueDialog.issuePicker().getUserInputtedOptionSuggestions().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.containsSubstring(floorString)));
        linkIssueDialog.issuePicker().query(Keys.DOWN);
        Poller.waitUntil("Active history search suggestion was expected to start with " + HSP_PREFIX,
                linkIssueDialog.issuePicker().getActiveSuggestionText(), startsWith(HSP_PREFIX));
    }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testNonExistantLabelWIthLowerCaseKey()
    {
        linkIssueDialog = openDialogFromViewIssue();
        linkIssueDialog.issuePicker().clearQuery().query("nick-1");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().isSuggestionsPresent());
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().hasActiveSuggestion());
        linkIssueDialog.issuePicker().query(Keys.RETURN);

        Poller.waitUntilTrue("No lozenges were loaded", linkIssueDialog.issuePicker().hasItems());

        Poller.waitUntil("Element picked was expected to be NICK-1",
                linkIssueDialog.issuePicker().getItems(), IterableMatchers.hasItemThat(LozengeMatchers.withName("NICK-1")));

        linkIssueDialog.clickLinkButton();
        Poller.waitUntil("Error prompt was not present.", linkIssueDialog.getErrorPageElement().timed().getText(), containsString("NICK-1"));
    }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testPreviousSuggestionsGetReplacedInsteadOfMerged()
    {
        linkIssueDialog = openDialogFromViewIssue();
        linkIssueDialog.issuePicker().clearQuery().query("t");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().isSuggestionsPresent());
        Poller.waitUntil("History Search contains " + ISSUE_WITH_T_IN_SKIPPED_KEYWORDS,
                linkIssueDialog.issuePicker().getHistorySearchSuggestions().getSuggestions(),
                IterableMatchers.hasNoItemThat(SuggestionMatchers.containsSubstring(ISSUE_WITH_T_IN_SKIPPED_KEYWORDS)));
        linkIssueDialog.issuePicker().clearQuery().query("").awayFromQueryInputArea();
        linkIssueDialog.issuePicker().clearQuery().query("h");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().hasActiveSuggestion());
        Poller.waitUntil("History Search doesn't contain " + ISSUE_WITH_T_IN_SKIPPED_KEYWORDS,
                linkIssueDialog.issuePicker().getHistorySearchSuggestions().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.containsSubstring(ISSUE_WITH_T_IN_SKIPPED_KEYWORDS)));
        linkIssueDialog.issuePicker().clearQuery().query("").awayFromQueryInputArea();
        linkIssueDialog.issuePicker().clearQuery().query("t");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().isSuggestionsPresent());
        Poller.waitUntil("History Search contains " + ISSUE_WITH_T_IN_SKIPPED_KEYWORDS,
                linkIssueDialog.issuePicker().getHistorySearchSuggestions().getSuggestions(),
                IterableMatchers.hasNoItemThat(SuggestionMatchers.containsSubstring(ISSUE_WITH_T_IN_SKIPPED_KEYWORDS)));
    }

    @Test
    @Restore("xml/TestIssuePickerInteractions.xml")
    public void testIssuesWithMatchingWordsPrecededByParenthesesAreDisplayed()
    {
        linkIssueDialog = openDialogFromViewIssue();

        linkIssueDialog.issuePicker().clearQuery().query("t");
        Poller.waitUntilTrue(linkIssueDialog.issuePicker().hasActiveSuggestion());
        Poller.waitUntil("History Search doesn't contain " + ISSUE_WITH_T_PRECEDED_BY_PARENTHESIS,
                linkIssueDialog.issuePicker().getHistorySearchSuggestions().getSuggestions(),
                IterableMatchers.hasItemThat(SuggestionMatchers.containsSubstring(ISSUE_WITH_T_PRECEDED_BY_PARENTHESIS)));
        Poller.waitUntil(linkIssueDialog.issuePicker().getHistorySearchSuggestions().getSuggestions(),
                IterableMatchers.iterableWithSize(3, Suggestion.class));
    }
}
