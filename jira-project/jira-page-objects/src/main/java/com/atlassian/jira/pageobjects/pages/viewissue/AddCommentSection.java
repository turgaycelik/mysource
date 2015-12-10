package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.userpicker.MentionsUserPicker;
import com.atlassian.jira.pageobjects.util.TraceContext;
import com.atlassian.jira.pageobjects.util.Tracer;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents add comment section on view issue page.
 *
 * @since v5.0
 */
public class AddCommentSection
{
    @Inject protected AtlassianWebDriver webDriver;
    @Inject protected PageBinder pageBinder;


    @ElementBy (id = "comment")
    protected PageElement comment;
    @ElementBy(id = "issue-comment-add")
    protected PageElement commentForm;
    @ElementBy(id = "issue-comment-add-submit")
    protected PageElement add;
    @ElementBy(id = "issue-comment-add-cancel")
    protected PageElement cancel;
    @ElementBy(id = "comment-preview_link")
    protected PageElement previewLink;

    @Inject
    private PageElementFinder pageElementFinder;

    @Inject
    private TraceContext traceContext;

    private ViewIssuePage parent;

    public AddCommentSection(ViewIssuePage parent)
    {
        this.parent = parent;
    }

    public AddCommentSection typeComment(CharSequence... text)
    {
        comment.type(text);
        return this;
    }

    public String getComment()
    {
        return comment.getValue();
    }

    public TimedQuery<String> getCommentTimed()
    {
        return comment.timed().getValue();
    }

    public MentionsUserPicker mentions()
    {
        return pageBinder.bind(MentionsUserPicker.class, this.comment);
    }

    public AddCommentSection selectMention(String userId)
    {
        Poller.waitUntilTrue(mentions().hasSuggestion(userId));
        mentions().getSuggestion(userId).click();
        return this;
    }

    /**
     * Adds and waits for issue to refresh
     */
    public ViewIssuePage addAndWait()
    {
        Tracer tracer = traceContext.checkpoint();
        add.click();
        Assert.assertTrue("".equals(getErrors()));
        return parent.waitForAjaxRefresh(tracer);
    }

    public String addWithErrors()
    {
        add.click();
        waitUntilTrue(errorElement().timed().isVisible());
        Assert.assertFalse("".equals(getErrors()));
        return getErrors();
    }

    /**
     * Comment errors get displayed on the dialog. After receiving an error use this method to make sure the dialog
     * gets closed.
     *
     * @return this comment section
     */
    public AddCommentSection closeErrors()
    {
        pageElementFinder.find(By.tagName("body")).type(Keys.ESCAPE);
        waitUntilFalse(errorElement().timed().isPresent());
        return this;
    }

    public ViewIssuePage cancel()
    {
        cancel.click();
        waitUntilFalse(commentForm.timed().isPresent());
        waitUntilFalse(comment.timed().isPresent());
        return parent;
    }

    public TimedQuery<Boolean> isInPreviewMode()
    {
        return previewLink.timed().hasClass("selected");
    }

    public AddCommentSection previewMode()
    {
        previewLink.click();
        waitUntilTrue(isInPreviewMode());
        return this;
    }

    private String getErrors()
    {
        final PageElement errorElement = errorElement();
        if (errorElement.isPresent())
        {
            return errorElement.getText();
        }

        return "";
    }

    private PageElement errorElement()
    {
        // we should be looking for errors on the AddCommentDialog (for which we don't have page object yaay!)
        // like this: By.id(comment-add).find(By.className("error"));, BUT it fails due to SELENIUM-185
        // (another yaaaaay)
        return pageElementFinder.find(By.className("error"));
    }
}
