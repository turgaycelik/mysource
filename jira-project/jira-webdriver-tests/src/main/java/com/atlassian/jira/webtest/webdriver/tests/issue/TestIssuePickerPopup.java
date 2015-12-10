package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.IssuePickerPopup;
import com.atlassian.jira.pageobjects.components.fields.IssuePickerRowMatchers;
import com.atlassian.jira.pageobjects.model.DefaultIssueActions;
import com.atlassian.jira.pageobjects.pages.viewissue.ConvertIssuePage;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveSubtaskChooseOperation;
import com.atlassian.jira.pageobjects.pages.viewissue.MoveSubtaskParentPage;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.inject.Inject;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.hasItemThat;
import static com.atlassian.jira.pageobjects.components.fields.IssuePickerRowMatchers.containsIssueKeys;
import static com.atlassian.jira.pageobjects.components.fields.IssuePickerRowMatchers.hasIssueKey;

/**
 * Webdriver test for the 'Issue Picker' popup
 *
 * @since v5.2
 */
@WebTest ({Category.WEBDRIVER_TEST, Category.ISSUES})
public class TestIssuePickerPopup extends BaseJiraWebTest
{
    @Inject private PageElementFinder pageElementFinder;

    private static final String SEARCH_REQUEST_ID = "10000";
    private static final String DEFAULT_FILTER_ID = "-1";

    private IssuePickerPopup currentPicker;
    private ConvertIssuePage currentConvertIssuePage;
    private MoveSubtaskParentPage currentMoveSubtaskParentPage;

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testConvertToSubtask()
    {
        currentConvertIssuePage = goToConvertToSubtask(Issue.HSP1);
        shouldSelectHsp2AsHsp1Parent();

    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testConvertToSubtask2()
    {
        currentConvertIssuePage = goToConvertToSubtask(Issue.HSP2);
        shouldSelectHsp1AsHsp2Parent();
    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testConvertToSubtaskViaSearchRequestMode()
    {
        currentConvertIssuePage = goToConvertToSubtask(Issue.HSP3);
        shouldSelectViaSearchRequestMode();
    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testConvertIssueParentFromSearchRequest()
    {
        currentConvertIssuePage = goToConvertToSubtask(Issue.HSP1);
        shouldHandleSwitchingToSearchRequestAndBack();
    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testConvertToSubtaskWithSwitchingWhenSetToDefault()
    {
        currentConvertIssuePage = goToConvertToSubtask(Issue.HSP1);
        shouldSwitchToRecentIssuesGivenFilterSelectSetToDefaultValue();
    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testMoveIssueParent()
    {
        currentMoveSubtaskParentPage = goToMoveSubtaskParent(Issue.HSP5);
        shouldPickMoveIssueFromPicker();
    }


    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testMoveIssueParent2()
    {
        currentMoveSubtaskParentPage = goToMoveSubtaskParent(Issue.HSP5);
        shouldPickMoveIssueFromPicker2();
    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testMoveIssueParentFromSearchRequest()
    {
        currentMoveSubtaskParentPage = goToMoveSubtaskParent(Issue.HSP5);
        shouldPickMoveIssueFromPickerFromSearchRequest();
    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testMoveIssueParentAfterSwitching()
    {
        currentMoveSubtaskParentPage = goToMoveSubtaskParent(Issue.HSP5);
        shouldPickMoveIssueFromPickerAfterSwitching();
    }

    @Test
    @Restore ("TestIssuePickerPopup.xml")
    public void testMoveIssueParentAfterSwitchingWhenSetToDefault()
    {
        currentMoveSubtaskParentPage = goToMoveSubtaskParent(Issue.HSP5);
        shouldPickMoveIssueFromPickerWhenSetToDefault();
    }

    private void shouldPickMoveIssueFromPickerWhenSetToDefault()
    {
        currentPicker = currentMoveSubtaskParentPage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP1\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP1.key()))));

        currentPicker.triggerSearchModeFilter(SEARCH_REQUEST_ID);
        Poller.waitUntilTrue(currentPicker.isInSearchModeFilter());
        Poller.waitUntil("Issue list should contain \"HSP3\".",
                currentPicker.getFilterIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP3.key()))));

        currentPicker.triggerSearchModeFilter(DEFAULT_FILTER_ID);
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP1\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP1.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP2.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP2\" expected to be selected on \"HSP5\" convert issue page",
                currentMoveSubtaskParentPage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP2.key()));
    }

    private void shouldPickMoveIssueFromPickerAfterSwitching()
    {
        currentPicker = currentMoveSubtaskParentPage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP1.key()))));

        currentPicker.triggerSearchModeFilter(SEARCH_REQUEST_ID);
        Poller.waitUntilTrue(currentPicker.isInSearchModeFilter());
        Poller.waitUntil("Issue list should contain \"HSP3\".",
                currentPicker.getFilterIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP3.key()))));

        currentPicker.triggerSearchModeRecent();
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP1.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP2.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP2\" expected to be selected on \"HSP5\" convert issue page",
                currentMoveSubtaskParentPage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP2.key()));
    }

    private void shouldPickMoveIssueFromPickerFromSearchRequest()
    {
        currentPicker = currentMoveSubtaskParentPage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP1\", \"HSP3\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP1.key(), Issue.HSP3.key()))));

        currentPicker.triggerSearchModeFilter(SEARCH_REQUEST_ID);
        Poller.waitUntilTrue(currentPicker.isInSearchModeFilter());
        Poller.waitUntil("Issue list should contain \"HSP3\".",
                currentPicker.getFilterIssuesSection().getIssueRows(),
                hasItemThat(hasIssueKey(Issue.HSP3.key())));

        currentPicker.getFilterIssuesSection().getIssueRow(Issue.HSP3.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP3\" expected to be selected on \"HSP5\" move issue page",
                currentMoveSubtaskParentPage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP3.key()));
    }



    private void shouldPickMoveIssueFromPicker()
    {
        currentPicker = currentMoveSubtaskParentPage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP1\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP1.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP1.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP1\" expected to be selected on \"HSP5\" move issue page",
                currentMoveSubtaskParentPage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP1.key()));
    }

    private void shouldPickMoveIssueFromPicker2()
    {
        currentPicker = currentMoveSubtaskParentPage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP1\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP1.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP3.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP3\" expected to be selected on \"HSP5\" move issue page",
                currentMoveSubtaskParentPage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP3.key()));
    }

    private void shouldSwitchToRecentIssuesGivenFilterSelectSetToDefaultValue()
    {
        currentPicker = currentConvertIssuePage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP4.key()))));

        currentPicker.triggerSearchModeFilter(SEARCH_REQUEST_ID);
        Poller.waitUntilTrue(currentPicker.isInSearchModeFilter());
        Poller.waitUntil("Issue list should contain \"HSP3\", \"HSP4\".",
                currentPicker.getFilterIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP3.key(), Issue.HSP4.key()))));

        currentPicker.triggerSearchModeFilter(DEFAULT_FILTER_ID);
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP4.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP3.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP3\" expected to be selected on \"HSP1\" convert issue page",
                currentConvertIssuePage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP3.key()));
    }

    private void shouldHandleSwitchingToSearchRequestAndBack()
    {
        currentPicker = currentConvertIssuePage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP4.key()))));

        currentPicker.triggerSearchModeFilter(SEARCH_REQUEST_ID);
        Poller.waitUntilTrue(currentPicker.isInSearchModeFilter());
        Poller.waitUntil("Issue list should contain \"HSP3\", \"HSP4\".",
                currentPicker.getFilterIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP3.key(), Issue.HSP4.key()))));

        currentPicker.triggerSearchModeRecent();
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP4.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP3.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP3\" expected to be selected on \"HSP1\" convert issue page",
                currentConvertIssuePage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP3.key()));
    }

    private void shouldSelectHsp2AsHsp1Parent()
    {
        currentPicker = currentConvertIssuePage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP3\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(IssuePickerRowMatchers.containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP3.key(), Issue.HSP4.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP2.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP2\" expected to be selected on \"HSP1\" convert issue page",
                currentConvertIssuePage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP2.key()));
    }

    private void shouldSelectViaSearchRequestMode()
    {
        currentPicker = currentConvertIssuePage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP2\", \"HSP1\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP2.key(), Issue.HSP1.key(), Issue.HSP4.key()))));

        currentPicker.triggerSearchModeFilter(SEARCH_REQUEST_ID);
        Poller.waitUntilTrue(currentPicker.isInSearchModeFilter());
        Poller.waitUntil("Issue list should contain \"HSP4\".",
                currentPicker.getFilterIssuesSection().getIssueRows(),
                hasItemThat(hasIssueKey(Issue.HSP4.key())));

        currentPicker.getFilterIssuesSection().getIssueRow(Issue.HSP4.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP4\" expected to be selected on \"HSP3\" convert issue page",
                currentConvertIssuePage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP4.key()));
    }

    private void shouldSelectHsp1AsHsp2Parent()
    {
        currentPicker = currentConvertIssuePage.openIssuePickerPopup();
        Poller.waitUntilTrue("Issue picker popup did not load.", currentPicker.isOpen());
        assertInRecentIssues();
        Poller.waitUntil("Issue list should contain \"HSP3\", \"HSP1\", \"HSP4\".",
                currentPicker.getRecentIssuesSection().getIssueRows(),
                hasItemThat(containsIssueKeys(expectedKeyBuilder(String.class, Issue.HSP3.key(), Issue.HSP1.key(), Issue.HSP4.key()))));

        currentPicker.getRecentIssuesSection().getIssueRow(Issue.HSP1.key()).selectRow();

        // switch back to the convert issue window
        currentPicker.switchBack();

        Poller.waitUntil("\"HSP1\" expected to be selected on \"HSP2\" convert issue page",
                currentConvertIssuePage.legacyPicker().getValue(),
                Matchers.startsWith(Issue.HSP1.key()));
    }

    private <T> Iterable<T> expectedKeyBuilder(final Class<T> targetClass, final Object... args)
    {
        final List<T> expectedKeys = new ArrayList<T>();
        for (final Object issueKey : args)
        {
            expectedKeys.add((T) issueKey);
        }
        return expectedKeys;
    }

    private void assertInRecentIssues()
    {
        Poller.waitUntilTrue(currentPicker.isInSearchModeRecent());
        Poller.waitUntil("Current Issues should be empty", currentPicker.getCurrentIssuesSection().getIssueRows(),
                IterableMatchers.emptyIterable(IssuePickerPopup.IssuePickerRow.class));
    }

    private ConvertIssuePage goToConvertToSubtask(final Issue issue)
    {
        final ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, issue.key());
        return viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.CONVERT_TO_SUBTASK, ConvertIssuePage.class, issue.id());
    }

    private MoveSubtaskParentPage goToMoveSubtaskParent(final Issue issue)
    {
        final ViewIssuePage viewIssuePage = jira.goTo(ViewIssuePage.class, issue.key());
        final MoveSubtaskChooseOperation moveSubtaskStep1 =
                viewIssuePage.getIssueMenu().invoke(DefaultIssueActions.MOVE, MoveSubtaskChooseOperation.class, issue.id());
        return moveSubtaskStep1.selectChangeParentRadio().next();
    }

    private static enum Issue
    {
        HSP1("HSP-1", 10000),
        HSP2("HSP-2", 10001),
        HSP3("HSP-3", 10002),
        HSP4("HSP-4", 10003),
        HSP5("HSP-5", 10010, HSP4),
        MKY1("MKY-1", 10020);

        private final String key;
        private final long id;
        private final Issue parent;

        private Issue(final String key, final long id)
        {
            this(key, id, null);
        }

        private Issue(final String key, final long id, final Issue parent)
        {
            this.key = key;
            this.id = id;
            this.parent = parent;
        }

        public String key()
        {
            return key;
        }

        public long id()
        {
            return id;
        }

    }

}
