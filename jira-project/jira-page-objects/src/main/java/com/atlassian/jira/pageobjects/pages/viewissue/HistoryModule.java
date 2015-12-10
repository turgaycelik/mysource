package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;

/**
 *
 *
 * @since v5.1
 */
public class HistoryModule
{
    @Inject protected PageElementFinder pageElementFinder;
    @Inject protected ExtendedElementFinder extendedElementFinder;
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;

    @ElementBy(id = "issue_actions_container")
    PageElement historyContainer;

    protected final Function<String, By> itemLocator;

    public HistoryModule()
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
    }

    public TimedQuery<Iterable<IssueHistoryData>> getHistoryItems()
    {
        return transformTimed(timeouts, pageBinder,
                extendedElementFinder.within(historyContainer).newQuery(itemLocator.apply(null)).supplier(),
                IssueHistoryData.class);
    }

    public TimedQuery<Boolean> hasLabels()
    {
        return historyContainer.find(itemLocator.apply(null)).timed().isPresent();
    }

    public static class IssueHistoryData
    {
        private final PageElement item;

        public IssueHistoryData(final PageElement item)
        {
            this.item = item;
        }

        public String getId()
        {
            return item.getAttribute("id");
        }

        public String getFieldName()
        {
            return item.find(By.className("activity-name")).getText();
        }

        public String getActionDescription()
        {
            return item.find(By.className("action-details")).getText();
        }

        public String getOldValue()
        {
            return item.find(By.className("activity-old-val")).getText();
        }

        public String getNewValue()
        {
            return item.find(By.className("activity-new-val")).getText();
        }

        public Iterable<String> getAvatarUrls()
        {
            return Iterables.transform(item.findAll(By.cssSelector(".aui-avatar-inner img")), new Function<PageElement, String>()
            {
                @Override
                public String apply(final PageElement pageElement)
                {
                    return pageElement.getAttribute("src");
                }
            });
        }

        public Iterable<String> getLinks()
        {
            return Iterables.transform(item.findAll(By.cssSelector("a")), new Function<PageElement, String>()
            {
                @Override
                public String apply(final PageElement pageElement)
                {
                    return pageElement.getAttribute("href");
                }
            });
        }
    }


}
