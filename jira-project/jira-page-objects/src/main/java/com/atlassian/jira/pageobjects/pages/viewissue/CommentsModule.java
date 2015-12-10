package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 *
 *
 * @since v6.0
 */
public class CommentsModule
{
    @Inject protected PageElementFinder pageElementFinder;
    @Inject protected ExtendedElementFinder extendedElementFinder;
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;

    @ElementBy(id = "issue_actions_container")
    PageElement commentsContainer;

    @Inject
    private PageBinder binder;

    protected final Function<String, By> itemLocator;

    private final String issueKey;

    public CommentsModule(String issueKey)
    {
        this.itemLocator = new Function<String, By>()
        {
            @Override
            public By apply(@Nullable String itemId)
            {
                //means find all items
                if(itemId == null)
                {
                    return By.cssSelector(".issue-data-block");
                }
                else
                {
                    return By.cssSelector("issue-data-block[id=\"" + itemId + "\"]");
                }

            }
        };

        this.issueKey = issueKey;
    }

    public List<CommentData> getComments()
    {
        return Lists.transform(commentsContainer.findAll(By.cssSelector("div.activity-comment")), new Function<PageElement, CommentData>()
        {
            @Override
            public CommentData apply(@Nullable PageElement commentElement)
            {
                return new CommentData(commentElement.find(By.className("actionContainer")), issueKey);
            }
        });
    }

    public class CommentData
    {
        private final PageElement item;
        private final String issueKey;

        public CommentData(final PageElement item, String issueKey)
        {
            this.item = item;
            this.issueKey = issueKey;
        }

        public String getId()
        {
            return item.getAttribute("id");
        }

        public DeleteCommentConfirmationDialog openDeleteDialog()
        {
            //Need to use JS because element is only visible on :hover
            item.find(By.className("icon-delete")).javascript().execute("AJS.$(arguments[0]).click();");
            return binder.bind(DeleteCommentConfirmationDialog.class, issueKey);
        }
    }

}
