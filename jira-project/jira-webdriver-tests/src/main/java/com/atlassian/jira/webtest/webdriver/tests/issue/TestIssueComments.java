package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.ResetData;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.AddCommentSection;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.testkit.client.restclient.Comment;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.google.common.collect.Iterables;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Test adding comments on the view issue page
 *
 * @since v4.4
 */
@ResetData
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
public class TestIssueComments extends BaseJiraWebTest
{
    private static final long PROJECT_ID = 10001L;
    public static final int CHARACTER_LIMIT_VALUE = 10;
    public static final String TEXT_TOO_LONG_ERROR_MESSAGE = "The entered text is too long. It exceeds the allowed limit of 10 characters.";
    private static final String SHORT_COMMENT = "all ok";
    private static final String TOO_LONG_COMMENT = "This is too long comment";
    public static final String TOO_LONG_MULTILINE_COMMENT = "123456789\n";

    @Inject
    private PageElementFinder pageElementFinder;

    /**
     * JRADEV-8225: adding a mention clears the rest of the line
     */
    @Test
    public void testMentionDoesNotClearLine()
    {
        final IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(PROJECT_ID, "Monkeys everywhere :(");

        final ViewIssuePage viewIssuePage = jira.goToViewIssue(issueCreateResponse.key());
        final AddCommentSection commentSection = viewIssuePage.comment();
        final String line1 = "This is the remainder of the line\n";
        final String line2 = "This is the next line";
        commentSection.typeComment(line1 + line2).typeComment(Keys.UP);
        //go to the beginning of the line. Keys.HOME doesn't work on all platforms :(
        for (int i = 0; i < line1.length(); i++)
        {
            commentSection.typeComment(Keys.LEFT);
        }
        final String comment = commentSection.typeComment("@fred").selectMention("fred").getComment();

        assertEquals(comment, "[~fred]" + line1 + line2);

        //add the comment here to make sure the next test that runs avoids the dirty form warning.
        commentSection.addAndWait();
    }

    @Test
    public void testCommentExceedsLimit()
    {
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(10);
        final IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(PROJECT_ID, "Monkeys everywhere :(");

        final AddCommentSection commentSection = jira.goToViewIssue(issueCreateResponse.key()).comment();
        final String errors = commentSection.typeComment(TOO_LONG_COMMENT).addWithErrors();
        assertEquals(TEXT_TOO_LONG_ERROR_MESSAGE, errors);
        commentSection.closeErrors().cancel();
    }

    @Test
    public void testMultilineCommentExceedsLimit()
    {
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(CHARACTER_LIMIT_VALUE);
        final IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(PROJECT_ID, "Monkeys everywhere :(");

        final AddCommentSection commentSection = jira.goToViewIssue(issueCreateResponse.key()).comment();
        final String errors = commentSection.typeComment(TOO_LONG_MULTILINE_COMMENT).addWithErrors();
        // regular error message and not a "communications breakdown"
        assertEquals(TEXT_TOO_LONG_ERROR_MESSAGE, errors);
        commentSection.closeErrors().cancel();
    }

    @Test
    public void testMultilineCommentWhileEditingIssueExceedsLimit()
    {
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(10);
        final IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(PROJECT_ID, "Monkeys everywhere :(");

        final EditIssueDialog editIssueDialog = jira.goToViewIssue(issueCreateResponse.key()).editIssue();
        editIssueDialog.setComment(TOO_LONG_MULTILINE_COMMENT);
        boolean open =  editIssueDialog.submit();

        List<String> formErrorList = editIssueDialog.getFormErrorList();
        assertThat(formErrorList, Matchers.contains(Matchers.equalToIgnoringCase(TEXT_TOO_LONG_ERROR_MESSAGE)));
        editIssueDialog.escape();
    }

    @Test
    public void testCommentShorterThanLimit()
    {
        backdoor.advancedSettings().setTextFieldCharacterLengthLimit(10);
        final IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(PROJECT_ID, "Monkeys everywhere :(");

        final AddCommentSection commentSection = jira.goToViewIssue(issueCreateResponse.key()).comment();
        commentSection.typeComment(SHORT_COMMENT).addAndWait();

        final List<Comment> comments = backdoor.issues().getIssue(issueCreateResponse.key()).getComments();
        final Comment comment = Iterables.getOnlyElement(comments);
        assertEquals(SHORT_COMMENT, comment.body);
    }

    @Test
    public void testCommentNotInPreviewModeAfterCanel()
    {
        backdoor.fieldConfiguration().setFieldRenderer("Default Field Configuration", "comment", "Wiki Style Renderer");
        final IssueCreateResponse issueCreateResponse = backdoor.issues().createIssue(PROJECT_ID, "Monkeys everywhere :(");
        final AddCommentSection commentSection = jira.goToViewIssue(issueCreateResponse.key()).comment();

        commentSection
                .typeComment(SHORT_COMMENT)
                .previewMode()
                .cancel()
                .comment();

        assertFalse("The comment is not in preview mode", commentSection.isInPreviewMode().byDefaultTimeout());
    }

    @Test
    public void testOlderCommentsAreExpandingWithoutPageReload()
    {
        final IssueCreateResponse issue = backdoor.issues().createIssue(PROJECT_ID, "Issue with a loooot of comments");
        generateComments(issue, 30);

        final ViewIssuePage issuePage = jira.visit(ViewIssuePage.class, issue.key);
        final String requestIdBeforeExpand = getRequestId();

        issuePage.expandHiddenComments();

        Iterable<com.atlassian.jira.pageobjects.pages.viewissue.link.activity.Comment> comments = issuePage.getComments();

        final String requestIdAfterExpand = getRequestId();

        assertThat(requestIdAfterExpand, Matchers.equalTo(requestIdBeforeExpand));
        assertThat(comments, Matchers.<com.atlassian.jira.pageobjects.pages.viewissue.link.activity.Comment>iterableWithSize(30));
    }

    private void generateComments(IssueCreateResponse issue, int n)
    {
        for (int i = 0; i < n; i++)
        {
            backdoor.issues().commentIssue(issue.key, "comment no " + i);
        }
    }

    private String getRequestId()
    {
        return pageElementFinder.find(By.id("jira_request_timing_info")).find(By.cssSelector("input[title=\"jira.request.id\"]")).getValue();
    }

}
