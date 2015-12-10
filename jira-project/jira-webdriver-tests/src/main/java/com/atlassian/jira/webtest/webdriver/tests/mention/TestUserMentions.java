package com.atlassian.jira.webtest.webdriver.tests.mention;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.components.userpicker.MentionsUserPicker;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.pageobjects.pages.viewissue.AddCommentSection;
import org.junit.Test;
import org.openqa.selenium.Keys;

import static com.atlassian.jira.functest.framework.matchers.IterableMatchers.hasItemThat;
import static com.atlassian.jira.pageobjects.components.fields.SuggestionMatchers.hasId;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilEquals;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertFalse;

/**
 * Test for user mentions in JIRA
 *
 * @since v5.2
 */
@Restore("TestMentions.xml")
@WebTest( {Category.WEBDRIVER_TEST, Category.USERS_AND_GROUPS, Category.ISSUES })
public class TestUserMentions extends BaseJiraWebTest
{
    public void setUp()
    {
    }

    @Test
    public void testMentionAutoCompleteWorksOnLowerCommentField()
    {
        final AddCommentSection commentSection = jira.goToViewIssue("HSP-1").comment();
        commentSection.typeComment("This is test comment for @adm");
        waitUntilTrue(commentSection.mentions().isOpen());
        waitUntil(commentSection.mentions().suggestions(), hasItemThat(hasId("admin", MentionsUserPicker.UserSuggestion.class)));
        commentSection.selectMention("admin");
        waitUntilEquals("This is test comment for [~admin]", commentSection.getCommentTimed());
    }

    @Test
    @LoginAs(user = "bob")
    public void testMentionAutoCompleteDoesNotShowWithUserWithoutBrowseUsers()
    {
        final AddCommentSection commentSection = jira.goToViewIssue("HSP-1").comment();
        commentSection.typeComment("This is test comment for @adm");
        waitUntilFalse("Expected mentions dropdown to not show", commentSection.mentions().isOpen());
    }

    @Test
    public void testMentionWorksQueriesContainingSpaceCharacter()
    {
        final AddCommentSection commentSection = jira.goToViewIssue("HSP-1").comment();
        commentSection.typeComment("This is @Bob B");
        waitUntilTrue("Mentions should be open after query with space", commentSection.mentions().isOpen());
        commentSection.typeComment(Keys.ARROW_LEFT); // caret back one position to the left
        waitUntilTrue("Mentions should still be open after caret is right after the space", commentSection.mentions().isOpen());
        waitUntilTrue("Bob should be selected", commentSection.mentions().hasActiveSuggestion("bob"));
        commentSection.mentions().selectActiveSuggestion();
        waitUntilEquals("Bob should be selected", "This is [~bob]B", commentSection.getCommentTimed());
    }

    @Test
    public void testMentionRemovesSuggestionsAfterEnteringCharacterWhenNoResults()
    {
        final AddCommentSection commentSection = jira.goToViewIssue("HSP-1").comment();
        commentSection.typeComment("This is @y");
        waitUntilTrue("Mentions should be open after query with space", commentSection.mentions().isOpen());
        assertFalse("Should have no mentions", commentSection.mentions().hasSuggestions());

        commentSection.typeComment("x");
        waitUntilFalse("Expected mentions dropdown to not show", commentSection.mentions().isOpen());
    }

    @Test
    public void testMentionRedisplaysSuggestionsWithNoResults()
    {
        final AddCommentSection commentSection = jira.goToViewIssue("HSP-1").comment();
        commentSection.typeComment("This is @y");
        waitUntilTrue("Mentions should be open after query with space", commentSection.mentions().isOpen());
        assertFalse("Should have no mentions", commentSection.mentions().hasSuggestions());

        commentSection.typeComment(Keys.BACK_SPACE);
        waitUntilFalse("Expected mentions dropdown to not show", commentSection.mentions().isOpen());
        commentSection.typeComment("y");
        waitUntilTrue("Mentions should be open after query with space", commentSection.mentions().isOpen());
    }
}
