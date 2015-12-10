package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryRecord;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;

import java.util.List;

/**
 * A set of "extended" assertions within JIRA web functional testing.  Also note we have a very similar interface
 * called {@link TextAssertions} that deals with text assertions specifically.
 *
 * @since v3.13
 */
public interface Assertions
{
    /**
     * This will "dump" the current contents of the {@link net.sourceforge.jwebunit.WebTester} including the Throwable
     * 'cause'.
     * <p/>
     * DONT LEAVE THESE CALLS IN THE TESTS. You should really only use this if you are debugging your test code.  if you
     * dont remove them, then you will generate "unwarranted" test artifacts in Bamboo.
     *
     * @param testCase the TestCase in play
     * @param tester the WebTester in play
     * @param cause an optional cause for dumping the response
     * @deprecated not strictly deprecated but shouldn't be used in the code you check into main repo.
     */
    public void dumpResponse(TestCase testCase, WebTester tester, Throwable cause);

    /**
     * Useful to check that a particular change history item exists for a given issue.
     *
     * @param issueKey                    The issue to check
     * @param expectedChangeHistoryRecord A {@link com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryRecord}
     * item
     */
    void assertLastChangeHistoryRecords(String issueKey, ExpectedChangeHistoryRecord expectedChangeHistoryRecord);

    /**
     * Useful to check that a number of change history items exist for a given issue.
     *
     * @param issueKey                     The issue to check
     * @param expectedChangeHistoryRecords A list of {@link com.atlassian.jira.webtests.ztests.workflow.ExpectedChangeHistoryRecord}s
     */
    void assertLastChangeHistoryRecords(String issueKey, List expectedChangeHistoryRecords);

    /**
     * Return an object that can be used to make assertions about the navigator.
     *
     * @return an object that can be used to make assertions about the navigator.
     */
    IssueNavigatorAssertions getIssueNavigatorAssertions();

    /**
     * Returns an object that can be used to make assertions on the "View Issue" page.
     *
     * <p> It is assumed that you are already on the View Issue page.
     * If not, then use something like <code>navigation.issue().viewIssue("RAT-1")</code> to get to the required Issue. 
     *
     * @return an object that can be used to make assertions on the "View Issue" page.
     * @see com.atlassian.jira.functest.framework.navigation.IssueNavigation#viewIssue(String)
     */
    ViewIssueAssertions getViewIssueAssertions();

    /**
     * @return an object that can be used to make assertions about Dashboard pages
     */
    DashboardAssertions getDashboardAssertions();

    /**
     * @return an object that can be used to make assertions about JIRA forms
     */
    JiraFormAssertions getJiraFormAssertions();

    /**
     * @return an object that can be used to make assertions about JIRA messages
     */
    JiraMessageAssertions getJiraMessageAssertions();

    /**
     * @return an object that can be used to make assertions about the current JIRA url.
     */
    URLAssertions getURLAssertions();

    /**
     * @return an object that can be used for text assertions.
     */
    TextAssertions getTextAssertions();

    /**
     * @return an object that can be used to make assertions about links.
     */
    LinkAssertions getLinkAssertions();

    /**
     * @return an object that can be used to make assertions about labels.
     */
    LabelAssertions getLabelAssertions();

    /**
     * @return an object that can be used to make assertions about users.
     */
    UserAssertions getUserAssertions();

    /**
     * @return an object that can be used to make assertions about tables.
     */
    TableAssertions getTableAssertions();

    void assertEquals(String failureMesg, List expected, List actual);

    /**
     * Checks that a profile link with the given ID and link Text exists on the page.  Also clicks the link and checks
     * that the user's profile with linkText shows up.
     *
     * @param id       The id of the profileLink
     * @param linkText The Text of the profileLink (usually the user's full name).
     */
    void assertProfileLinkPresent(String id, String linkText);

    /**
     * Asserts that a node exists under the xpath expression given
     *
     * @param xpath The xpath expression to evaluate
     */
    public void assertNodeExists(final String xpath);

    /**
     * Asserts that a node does not exist under the xpath expression given
     *
     * @param xpath The xpath expression to evaluate
     */
    public void assertNodeDoesNotExist(final String xpath);

    /**
     * Asserts that a node under the xpath expression given contains the given text
     *
     * @param xpath      The xpath expression to evaluate
     * @param textToTest The text to test for
     */
    public void assertNodeHasText(final String xpath, final String textToTest);

    /**
     * Asserts that a node under the xpath expression given does not contain the given text
     *
     * @param xpath      The xpath expression to evaluate
     * @param textToTest The text to test for
     */
    public void assertNodeDoesNotHaveText(final String xpath, final String textToTest);

    /**
     * Asserts that a given node exists
     *
     * @param id the id of the node
     */
    void assertNodeByIdExists(String id);

    /**
     * Asserts that a given node does not exists
     *
     * @param id the id of the node
     */
    void assertNodeByIdDoesNotExist(String id);

    /**
     * Asserts that a given node exactly equals the given text
     *
     * @param id         The id of the node to test
     * @param textToTest The text to test for
     */
    void assertNodeByIdEquals(String id, String textToTest);

    /**
     * Asserts that a given node contains the given text
     *
     * @param id         The id of the node to test
     * @param textToTest The text to test for
     */
    void assertNodeByIdHasText(String id, String textToTest);

    /**
     * Asserts that a given node exists but does not contain the given text
     *
     * @param id         The id of the node to test
     * @param textToTest The text to test for
     */
    void assertNodeByIdDoesNotHaveText(String id, String textToTest);


    /**
     * Asserts that a given node exists
     *
     * @param locator the locator for the node
     */
    void assertNodeExists(Locator locator);

    /**
     * Asserts that a given node does not exists
     *
     * @param locator the locator for the node
     */
    void assertNodeDoesNotExist(Locator locator);

    /**
     * Asserts that a given node exactly equals the given text
     *
     * @param locator    The locator that contains text
     * @param textToTest The text to test for
     */
    void assertNodeEquals(Locator locator, String textToTest);

    /**
     * Asserts that a given node contains the given text
     *
     * @param locator    The locator that contains text
     * @param textToTest The text to test for
     */
    void assertNodeHasText(Locator locator, String textToTest);

    /**
     * Asserts that a given node exists but does not contain the given text
     *
     * @param locator    The locator to test
     * @param textToTest The text to test for
     */
    void assertNodeDoesNotHaveText(Locator locator, String textToTest);

    /**
     * Check that the button with the id provided has the text provided as its value.
     * @param buttonId The id of the button to locate
     * @param buttonText The text the button should contain as its value
     */
    void assertSubmitButtonPresentWithText(String buttonId, String buttonText);

    /**
     * Returns an object that can be used to make assertions on a specified group of comments.
     * @param comments The comments that will be used in the assertions.
     * @return a {CommentAssertions} object that can be used to make assertions on a specified group of comments.
     */
    CommentAssertions comments(Iterable<String> comments);

    /**
     * HTML assertions.
     *
     * @return HTML assertions.
     */
    HTMLAssertions html();

    /**
     * Text assertions. Shortcut for {@link #getTextAssertions()}.
     *
     * @return text assertions.
     */
    TextAssertions text();

    /**
     * Link assertions. Shortcut for {@link #getLinkAssertions()}.
     *
     * @return link assertions.
     */
    LinkAssertions link();

    /**
     * Form assertions. Shortcut for {@link #getJiraFormAssertions()}.
     *
     * @return form assertions
     */
    JiraFormAssertions forms();

}
