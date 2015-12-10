package com.atlassian.jira.pageobjects.components;

import com.atlassian.jira.functest.framework.matchers.IterableMatchers;
import com.atlassian.jira.pageobjects.components.userpicker.LegacyPicker;
import com.atlassian.jira.pageobjects.components.userpicker.PickerPopup;
import com.atlassian.jira.pageobjects.components.userpicker.PickerType;
import com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder;
import com.atlassian.jira.pageobjects.framework.elements.PageElements;
import com.atlassian.jira.util.lang.GuavaPredicates;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.DataAttributeFinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.utils.by.ByDataAttribute;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.components.fields.IssuePickerRowMatchers.hasIssueKey;
import static com.atlassian.jira.pageobjects.framework.elements.PageElements.transformTimed;
import static com.atlassian.pageobjects.elements.query.Conditions.forMatcher;

/**
 * Issue picker popup
 *
 * @since v5.1
 */
public class IssuePickerPopup extends PickerPopup<PickerPopup.PickerRow>
{

    @Inject Timeouts timeouts;
    @Inject PageBinder pageBinder;
    @Inject PageElementFinder elementFinder;

    @ElementBy (id = "issuepicker-source")
    PageElement issuePickerSource;

    @ElementBy (id = "current-issues")
    PageElement currentIssuesSection;

    @ElementBy (id = "recent-issues")
    PageElement recentIssuesSection;

    @ElementBy (id = "filter-issues")
    PageElement filterIssuesSection;

    public IssuePickerPopup(LegacyPicker parent)
    {
        super(parent, PickerType.ISSUE_PICKER, PickerRow.class);
    }



    public TimedQuery<Boolean> isInSearchModeRecent()
    {
        return elementFinder.find(By.id("issue-source-recent")).timed().isSelected();
    }

    public IssuePickerPopup triggerSearchModeRecent()
    {
        elementFinder.find(By.id("issue-source-recent")).click();
        return this;
    }

    public TimedQuery<Boolean> isInSearchModeFilter()
    {
        return elementFinder.find(By.id("issue-source-search")).timed().isSelected();
    }

    public IssuePickerPopup triggerSearchModeFilter()
    {
        elementFinder.find(By.id("issue-source-search")).click();
        return this;
    }

    public IssuePickerPopup triggerSearchModeFilter(CharSequence filterOption)
    {
        triggerSearchModeFilter();
        for (PageElement option : elementFinder.find(By.id("searchRequestId")).findAll(By.tagName("option")))
        {
            if (option.getValue().contains(filterOption))
            {
                option.click();
            }
        }
        return this;
    }

    public IssuePickerSection getCurrentIssuesSection()
    {
        return pageBinder.bind(IssuePickerSection.class, currentIssuesSection);
    }

    public IssuePickerSection getRecentIssuesSection()
    {
        return pageBinder.bind(IssuePickerSection.class, recentIssuesSection);
    }

    public IssuePickerSection getFilterIssuesSection()
    {
        return pageBinder.bind(IssuePickerSection.class, filterIssuesSection);
    }

    public static class IssuePickerRow
    {
        protected final PageElement rowElement;
        protected final PageElement issueKeyCell;
        
        public IssuePickerRow(PageElement rowElement)
        {
            this.rowElement = rowElement;
            this.issueKeyCell = rowElement.find(ByDataAttribute.byData("cell-type", "issue-key"));
        }

        public IssuePickerRow selectRow()
        {
            issueKeyCell.click();
            return this;
        }

        public TimedQuery<String> getRowKey()
        {
            return DataAttributeFinder.query(rowElement).timed().getDataAttribute("row-for");
        }
    }

    public static class IssuePickerSection
    {
        @Inject protected Timeouts timeouts;
        @Inject protected PageBinder pageBinder;
        @Inject protected ExtendedElementFinder extendedFinder;

        private PageElement container;

        public IssuePickerSection(PageElement container)
        {
            this.container = container;
        }

        public TimedQuery<String> getHeader()
        {
            return container.find(By.className("toggle-title")).timed().getText();
        }

        public TimedQuery<Iterable<IssuePickerRow>> getIssueRows()
        {
            return transformTimed(timeouts, pageBinder,
                    extendedFinder.within(container.find(By.className("mod-content"))).newQuery(By.tagName("tr"))
                            .filter(PageElements.hasClass("issue-picker-row"))
                            .supplier(),
                    IssuePickerRow.class);
        }
        
        public TimedCondition hasIssueRow(String issueKey)
        {
            return forMatcher(getIssueRows(), IterableMatchers.hasItemThat(hasIssueKey(issueKey)));
        }

        public IssuePickerRow getIssueRow(String issueKey)
        {
            Poller.waitUntilTrue(hasIssueRow(issueKey));
            return Iterables.find(getIssueRows().now(), GuavaPredicates.forMatcher(hasIssueKey(issueKey)));
        }
        
    }


}
